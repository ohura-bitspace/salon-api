package jp.bitspace.salon.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.StaffService;

/**
 * 認証系コントローラ.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final StaffService staffService;
    private final CustomerService customerService;

    public AuthController(StaffService staffService, CustomerService customerService) {
        this.staffService = staffService;
        this.customerService = customerService;
    }

    /**
     * 管理者ログイン
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<Staff> staff = staffService.authenticate(email, password);
        if (staff.isPresent()) {
            return ResponseEntity.ok(staff.get());
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

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
}
