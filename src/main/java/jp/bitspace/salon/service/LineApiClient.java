package jp.bitspace.salon.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * LINE Login v2.1 API クライアント.
 */
@Service
@RequiredArgsConstructor
public class LineApiClient {

    @Value("${line.login.channel-id}")
    private String channelId;

    @Value("${line.login.channel-secret}")
    private String channelSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 認可コードからアクセストークン等を取得します.
     */
    public TokenResponse exchangeToken(String code, String redirectUri) {
        String url = "https://api.line.me/oauth2/v2.1/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", channelId);
        form.add("client_secret", channelSecret);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(form, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }

            JsonNode json = objectMapper.readTree(response.getBody());
            String accessToken = json.path("access_token").asText(null);
            String idToken = json.path("id_token").asText(null);
            if (accessToken == null || idToken == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }
            return new TokenResponse(accessToken, idToken);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
        }
    }

    /**
     * LINEプロフィールを取得します.
     */
    public ProfileResponse fetchProfile(String accessToken) {
        String url = "https://api.line.me/v2/profile";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch LINE profile");
            }

            JsonNode json = objectMapper.readTree(response.getBody());
            String userId = json.path("userId").asText(null);
            String displayName = json.path("displayName").asText(null);
            String pictureUrl = json.path("pictureUrl").asText(null);
            String statusMessage = json.path("statusMessage").asText(null);

            if (userId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch LINE profile");
            }
            return new ProfileResponse(userId, displayName, pictureUrl, statusMessage);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch LINE profile");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch LINE profile");
        }
    }

    /**
     * id_token の最低限の検証（署名検証は未実装）.
     * <p>
     * TODO: JWK を取得して署名検証する。
     */
    public IdTokenClaims decodeAndValidateIdToken(String idToken, String expectedNonce) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String iss = payload.path("iss").asText();
            if (!"https://access.line.me".equals(iss)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }

            String aud = payload.path("aud").asText();
            if (aud == null || !aud.equals(channelId)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }

            long exp = payload.path("exp").asLong(0);
            if (exp <= 0 || Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }

            String nonce = payload.path("nonce").asText(null);
            if (expectedNonce != null && nonce != null && !expectedNonce.equals(nonce)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
            }

            String email = payload.path("email").asText(null);
            return new IdTokenClaims(email);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINE authentication failed");
        }
    }

    public record TokenResponse(String accessToken, String idToken) {}

    public record ProfileResponse(String userId, String displayName, String pictureUrl, String statusMessage) {}

    public record IdTokenClaims(String email) {}
}
