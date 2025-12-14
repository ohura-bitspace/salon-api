package jp.bitspace.salon.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.security.JwtUtils;
import jp.bitspace.salon.service.CustomerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {
	
	private final CustomerService customerService;
    private final JwtUtils jwtUtils;

//    // LINEログイン（本番用）
//    @PostMapping("/line")
//    public ResponseEntity<LoginResponse> loginWithLine(@RequestBody LineLoginRequest request) {
//        // LINEのアクセストークン検証 -> Customer取得/作成 -> JWT発行
//        return ResponseEntity.ok(...);
//    }
    
    /**
     * 開発用: 顧客IDでログイン（LINE連携スキップ）
     */
    @PostMapping("/dev/customer-login")
    public ResponseEntity<?> devCustomerLogin(@RequestBody Map<String, Long> request) {
        Long customerId = request.get("customerId");
        
        Optional<Customer> customer = customerService.loginByDevId(customerId);
        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Customer not found"));
    }

//    // 開発用バックドア（テスト用）
//    @PostMapping("/dev/login")
//    public ResponseEntity<LoginResponse> devLogin(@RequestBody Map<String, Long> request) {
//        Long customerId = request.get("customerId");
//        // ... CustomerServiceから取得 -> JWT発行 ...
//    }

}
