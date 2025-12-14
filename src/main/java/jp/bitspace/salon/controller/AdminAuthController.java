package jp.bitspace.salon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.controller.dto.LoginRequest;
import jp.bitspace.salon.controller.dto.LoginResponse;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.security.JwtUtils;
import jp.bitspace.salon.service.StaffService;

/**
 * 管理側認証系コントローラ.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final StaffService staffService;
    private final JwtUtils jwtUtils;

    public AdminAuthController(StaffService staffService, JwtUtils jwtUtils) {
        this.staffService = staffService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 管理者ログイン
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Staff staff = staffService.authenticate(request.email(), request.password());

        String token = jwtUtils.generateToken(
                staff.getId(),
                staff.getEmail(),
                staff.getSalonId(),
                staff.getRole().name()
        );

        LoginResponse response = new LoginResponse(
                token,
                staff.getName(),
                staff.getRole().name(),
                staff.getSalonId()
        );

        return ResponseEntity.ok(response);
    }
}
