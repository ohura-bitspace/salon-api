package jp.bitspace.salon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.LoginRequest;
import jp.bitspace.salon.dto.LoginResponse;
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
    	// 認証処理
        Staff staff = staffService.authenticate(request.email(), request.password());
        // JWTトークンの発行
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
    
// TODO 多店舗設計
//    -- 1. ユーザーテーブル（人そのもの）
//    -- 認証情報（メアド、パスワード）はここに集約
//    CREATE TABLE users (
//        id BIGINT AUTO_INCREMENT PRIMARY KEY,
//        name VARCHAR(100) NOT NULL,
//        email VARCHAR(255) NOT NULL UNIQUE,
//        password_hash VARCHAR(255) NOT NULL,
//        created_at DATETIME ...
//    );
//
//    -- 2. スタッフ/所属テーブル（どの店で、何の権限を持つか）
//    -- ここにはメアドやパスワードを持たせない！
//    CREATE TABLE staffs (
//        id BIGINT AUTO_INCREMENT PRIMARY KEY,
//        user_id BIGINT NOT NULL,   -- 誰が (Users.id)
//        salon_id BIGINT NOT NULL,  -- どの店で (Salons.id)
//        role ENUM('OWNER', 'STAFF'), -- どんな役割か
//        
//        -- 1人は同じ店に1つしか所属を持てない
//        UNIQUE KEY uq_user_salon (user_id, salon_id),
//        FOREIGN KEY (user_id) REFERENCES users(id),
//        FOREIGN KEY (salon_id) REFERENCES salons(id)
//    );
}
