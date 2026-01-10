package jp.bitspace.salon.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.request.LoginRequest;
import jp.bitspace.salon.dto.response.LoginResponse;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.model.User;
import jp.bitspace.salon.security.JwtUtils;
import jp.bitspace.salon.service.StaffService;

/**
 * 管理側認証系コントローラ.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
	
	/** スタッフサービス. */
    private final StaffService staffService;
    /** JwtUtils. */
    private final JwtUtils jwtUtils;
    
    // TODO expiration-msが切れた場合の処理を検討

    public AdminAuthController(StaffService staffService, JwtUtils jwtUtils) {
        this.staffService = staffService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 管理者ログイン
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 認証処理
        // (Service内でUser認証 -> 所属店舗確認 -> 先頭のStaffを返す、という流れになっている前提)
        Staff currentStaff = staffService.authenticate(request.email(), request.password());
        
        // Userエンティティを取得
        User user = currentStaff.getUser();

        // 2. JWTトークンの発行
        // (emailなどは User エンティティから取得するように変更)
        String token = jwtUtils.generateToken(
                user.getId(),          // User IDを入れるのが一般的だが、実装に合わせて調整
                user.getEmail(),       // user.getEmail()
                currentStaff.getSalon().getId(),
                currentStaff.getRole().name()
        );

        // 3. TODO 【多店舗対応】もし「他の店舗」も持っているならリスト化する
        // (LoginResponseにリストを持たせるフィールドがある場合のみ使用)
        // List<Staff> allAffiliations = staffService.findByUserId(user.getId());
        
        // 4. レスポンス生成
        // 名前も User から取得
        LoginResponse response = new LoginResponse(
                token,
                user.getName(),        // user.getName()
                currentStaff.getRole().name(),
                currentStaff.getSalon().getId(),
                currentStaff.getSalon().getName()
                // availableSalons // ← DTOを更新したらここに追加
        );

        return ResponseEntity.ok(response);
    }
}
