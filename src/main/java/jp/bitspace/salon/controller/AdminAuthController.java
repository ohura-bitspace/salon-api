package jp.bitspace.salon.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.StaffService;

/**
 * 管理側認証系コントローラ.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final StaffService staffService;

    public AdminAuthController(StaffService staffService, CustomerService customerService) {
        this.staffService = staffService;
    }

    /**
     * 管理者ログイン
     */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<Staff> staff = staffService.authenticate(email, password);
        if (staff.isPresent()) {
            return ResponseEntity.ok(staff.get());
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
}
