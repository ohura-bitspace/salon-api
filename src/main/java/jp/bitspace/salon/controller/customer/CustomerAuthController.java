package jp.bitspace.salon.controller.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.request.CustomerDevLoginRequest;
import jp.bitspace.salon.dto.request.LineCallbackRequest;
import jp.bitspace.salon.dto.response.CustomerAuthResponse;
import jp.bitspace.salon.dto.response.CustomerDto;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.security.CustomerPrincipal;
import jp.bitspace.salon.security.JwtUtils;
import jp.bitspace.salon.service.CustomerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {
	
	private final CustomerService customerService;
    private final JwtUtils jwtUtils;

    /**
     * 開発用ログイン（デバッグ用途）.
     * <p>
     * 指定の customerId を検索し、JWT を発行して返します。
     */
    @PostMapping("/dev-login")
    public ResponseEntity<CustomerAuthResponse> devLogin(@RequestBody CustomerDevLoginRequest request) {
        Customer customer = customerService.findByIdAndSalonIdOrThrow(request.getCustomerId(), request.getSalonId());
        String token = jwtUtils.generateToken(customer.getId(), customer.getSalonId());
        return ResponseEntity.ok(new CustomerAuthResponse(token, toDto(customer)));
    }

    /**
     * LINE認証コールバック（スタブ）.
     * <p>
     * TODO: code/state でLINEトークン交換・検証を実装する。
     */
    @PostMapping("/line/callback")
    public ResponseEntity<CustomerAuthResponse> lineCallback(@RequestBody LineCallbackRequest request) {
        // TODO: 本来は request.getCode()/getState() を使って LINE 側の検証を行う
        Customer customer = customerService.getOrCreateStubCustomer(request.getSalonId());
        String token = jwtUtils.generateToken(customer.getId(), customer.getSalonId());
        return ResponseEntity.ok(new CustomerAuthResponse(token, toDto(customer)));
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

}
