package jp.bitspace.salon.controller.customer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jp.bitspace.salon.dto.request.CustomerDevLoginRequest;
import jp.bitspace.salon.dto.request.LineCallbackRequest;
import jp.bitspace.salon.dto.response.CustomerAuthResponse;
import jp.bitspace.salon.dto.response.CustomerDto;
import jp.bitspace.salon.dto.response.CustomerLineLoginResponse;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.security.CustomerPrincipal;
import jp.bitspace.salon.security.JwtUtils;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.LineApiClient;
import jp.bitspace.salon.service.LineStateService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {
	
	// TODO 全体的にセキュリティ面が抜けているので、2月以降にチェック
	
	private final CustomerService customerService;
    private final JwtUtils jwtUtils;
    private final LineStateService lineStateService;
    private final LineApiClient lineApiClient;

    @Value("${line.login.channel-id:}")
    private String lineChannelId;

    /**
     * 開発用ログイン（デバッグ用途）.
     * <p>
     * 指定の customerId を検索し、JWT を発行して返します。
     */
    @PostMapping("/dev-login")
    @org.springframework.context.annotation.Profile("dev")
    public ResponseEntity<CustomerAuthResponse> devLogin(@RequestBody CustomerDevLoginRequest request) {
    	
    	System.out.println("開発ログイン:" + request);
        Customer customer = customerService.findByIdAndSalonIdOrThrow(request.getCustomerId(), request.getSalonId());
        String token = jwtUtils.generateToken(customer.getId(), customer.getSalonId());
        return ResponseEntity.ok(new CustomerAuthResponse(token, toDto(customer)));
    }

    /**
     * LINE Login（Web）認証開始.
     * <p>
     * state/nonce を生成し、セッションに保存して authorize URL を返します。
     */
    @GetMapping("/line/authorize")
    public ResponseEntity<Map<String, String>> authorize(
            HttpSession session,
            @RequestParam Long salonId,
            @RequestParam(name = "redirect") String redirectUri) {
    	
    	// TODO 2/1以降に本番環境を実装

        LineStateService.CreatedState created = lineStateService.createAndStore(session, salonId, redirectUri);

        // LINE authorize URL
        String authUrl = "https://access.line.me/oauth2/v2.1/authorize"
                + "?response_type=code"
                + "&client_id=" + url(lineChannelId)
                + "&redirect_uri=" + url("https://kandace-icicled-unadmissibly.ngrok-free.dev/auth/callback")
                + "&state=" + url(created.state())
                + "&scope=" + url("profile openid email")
                + "&nonce=" + url(created.nonce());
        
        System.out.println("redirectUri:" + redirectUri);

        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    /**
     * LINE認証コールバック（スタブ）.
     * <p>
     * TODO: code/state でLINEトークン交換・検証を実装する。
     */
	@PostMapping("/line/callback")
	public ResponseEntity<CustomerLineLoginResponse> lineCallback(HttpSession session,
			@RequestBody LineCallbackRequest request) {
		
		// 1) state 検証（使い捨て）
		LineStateService.StateEntry entry = lineStateService.validateAndConsume(session, request.getState(),
				request.getSalonId());

		// 2) トークン交換
		LineApiClient.TokenResponse tokenResponse = lineApiClient.exchangeToken(request.getCode(), entry.redirectUri());

		// 3) id_token 最低限検証（iss/aud/exp/nonce）
		LineApiClient.IdTokenClaims claims = lineApiClient.decodeAndValidateIdToken(tokenResponse.idToken(),
				entry.nonce());
		System.out.println("claims:" + claims);
		// 4) プロフィール取得
		LineApiClient.ProfileResponse profile = lineApiClient.fetchProfile(tokenResponse.accessToken());
		System.out.println("profile:" + profile);
		

		// 5) 顧客作成/更新
		Customer customer = customerService.createOrUpdateByLineProfile(
				entry.salonId(),
				profile.userId(),
				profile.displayName(),
				profile.pictureUrl(),
				claims.email());

		// 6) JWT 発行
		String jwt = jwtUtils.generateToken(customer.getId(), customer.getSalonId());
		String name = (customer.getLastName() != null && customer.getFirstName() != null)
				? customer.getLastName() + " " + customer.getFirstName()
				: customer.getLineDisplayName();

		return ResponseEntity.ok(new CustomerLineLoginResponse(
				jwt,
				customer.getId(),
				name,
				customer.getEmail(),
				customer.getPhoneNumber()));
	}

    /**
     * 自分の情報取得（トークン確認用）.
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerDto> me(@AuthenticationPrincipal CustomerPrincipal principal) {
        Customer customer = customerService.findByIdAndSalonIdOrThrow(principal.getCustomerId(), principal.getSalonId());
        return ResponseEntity.ok(toDto(customer));
    }

    private CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getSalonId(),
                customer.getLineUserId(),
                customer.getLineDisplayName(),
                customer.getLinePictureUrl(),
                customer.getLastName(),
                customer.getFirstName(),
                customer.getEmail(),
                customer.getPhoneNumber()
        );
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
