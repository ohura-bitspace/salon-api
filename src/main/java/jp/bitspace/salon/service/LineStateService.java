package jp.bitspace.salon.service;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

/**
 * LINE Login の state/nonce 管理（セッション保存）.
 * <p>
 * 本番は Redis 等の共有ストア推奨。
 */
@Service
public class LineStateService {

    private static final String SESSION_KEY = "lineLoginStates";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final SecureRandom secureRandom = new SecureRandom();

    // redirect の許可リスト（オリジン単位）
    @Value("${line.login.allowed-redirect-origins:http://localhost:5173}")
    private String allowedRedirectOrigins;

    public CreatedState createAndStore(HttpSession session, Long salonId, String redirectUri) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is required");
        }
        if (salonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "redirectUri is required");
        }

        validateRedirectUri(redirectUri);

        String state = randomUrlSafeString(32);
        String nonce = randomUrlSafeString(32);

        Map<String, StateEntry> states = getStateMap(session);
        states.put(state, new StateEntry(salonId, redirectUri, nonce, Instant.now()));
        session.setAttribute(SESSION_KEY, states);

        return new CreatedState(state, nonce, redirectUri);
    }

    /**
     * state を検証し、使い捨てとして削除して返します.
     */
    public StateEntry validateAndConsume(HttpSession session, String state, Long salonId) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is required");
        }
        if (state == null || state.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");
        }

        Map<String, StateEntry> states = getStateMap(session);
        StateEntry entry = states.remove(state);
        session.setAttribute(SESSION_KEY, states);

        if (entry == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");
        }
        if (isExpired(entry)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");
        }
        if (salonId != null && entry.salonId() != null && !entry.salonId().equals(salonId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");
        }
        return entry;
    }

    private boolean isExpired(StateEntry entry) {
        return entry.createdAt().plus(TTL).isBefore(Instant.now());
    }

    private Map<String, StateEntry> getStateMap(HttpSession session) {
        Object obj = session.getAttribute(SESSION_KEY);
        if (obj instanceof Map<?, ?> mapObj) {
            @SuppressWarnings("unchecked")
            Map<String, StateEntry> cast = (Map<String, StateEntry>) mapObj;
            return new HashMap<>(cast);
        }
        return new HashMap<>();
    }

    private String randomUrlSafeString(int bytes) {
        byte[] buf = new byte[bytes];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private void validateRedirectUri(String redirectUri) {
        URI uri;
        try {
            uri = URI.create(redirectUri);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect");
        }

        int port = uri.getPort();
        String origin = scheme + "://" + host + ((port == -1) ? "" : ":" + port);

        List<String> allowList = List.of(allowedRedirectOrigins.split(","));
        boolean ok = allowList.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(allowed -> allowed.equals(origin));

        if (!ok) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect");
        }
    }

    public record CreatedState(String state, String nonce, String redirectUri) {}

    public record StateEntry(Long salonId, String redirectUri, String nonce, Instant createdAt) {}
}
