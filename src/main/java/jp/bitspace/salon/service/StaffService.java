package jp.bitspace.salon.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.request.CreateStaffRequest;
import jp.bitspace.salon.dto.request.UpdateStaffRequest;
import jp.bitspace.salon.dto.response.StaffResponse;
import jp.bitspace.salon.model.Role;
import jp.bitspace.salon.model.Salon;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.model.User;
import jp.bitspace.salon.repository.SalonRepository;
import jp.bitspace.salon.repository.StaffRepository;
import jp.bitspace.salon.repository.UserRepository;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(
            StaffRepository staffRepository,
            UserRepository userRepository,
            SalonRepository salonRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.salonRepository = salonRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    public Optional<Staff> findById(Long id) {
        return staffRepository.findById(id);
    }

    public Staff save(Staff staff) {
        if (staff.getIsPractitioner() == null) {
            staff.setIsPractitioner(true);
        }
        return staffRepository.save(staff);
    }

    public List<Staff> findBySalonId(Long salonId) {
        return staffRepository.findBySalonId(salonId);
    }

    /**
     * 管理画面用: 所属サロンの全スタッフをレスポンスDTOに整形して返す.
     */
    public List<StaffResponse> findStaffResponseBySalonId(Long salonId) {
        return staffRepository.findBySalonId(salonId)
                .stream()
                .map(this::toStaffResponse)
                .toList();
    }

    /**
     * 予約画面用: 施術者フラグがTRUEのスタッフのみ返す.
     */
    public List<Staff> findPractitionersBySalonId(Long salonId) {
        return staffRepository.findBySalonIdAndIsPractitionerTrue(salonId);
    }

    /**
     * 予約画面用: 施術者フラグがTRUEのスタッフをレスポンスDTOに整形して返す.
     */
    public List<StaffResponse> findPractitionersResponseBySalonId(Long salonId) {
        return staffRepository.findBySalonIdAndIsPractitionerTrue(salonId)
                .stream()
                .map(this::toStaffResponse)
                .toList();
    }

    private StaffResponse toStaffResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .salonId(staff.getSalon() != null ? staff.getSalon().getId() : null)
                .salonName(staff.getSalon() != null ? staff.getSalon().getName() : null)
                .userId(staff.getUser() != null ? staff.getUser().getId() : null)
                .email(staff.getUser() != null ? staff.getUser().getEmail() : null)
                .userName(staff.getUser() != null ? staff.getUser().getName() : null)
                .role(staff.getRole() != null ? staff.getRole().name() : null)
                .isActive(staff.getIsActive())
                .isPractitioner(staff.getIsPractitioner())
                .build();
    }

    /**
     * スタッフ詳細取得（編集画面で使用）
     * salonId との一致をチェックしてからレスポンスを返す
     */
    public StaffResponse findStaffResponseById(Long staffId, Long salonId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found: " + staffId));

        // salonIdのチェック
        if (staff.getSalon() == null || !staff.getSalon().getId().equals(salonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff does not belong to the specified salon");
        }

        return toStaffResponse(staff);
    }

    /**
     * スタッフ登録（最低限: 既存Userを紐付ける想定）
     */
    @Transactional
    public Staff createStaff(CreateStaffRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getSalonId() == null) {
            throw new IllegalArgumentException("salonId is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));
        Salon salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new IllegalArgumentException("Salon not found: " + request.getSalonId()));

        Staff staff = Staff.builder()
                .user(user)
                .salon(salon)
                .role(request.getRole() != null ? Role.valueOf(request.getRole()) : Role.STAFF)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isPractitioner(request.getIsPractitioner() != null ? request.getIsPractitioner() : true)
                .build();

        return staffRepository.save(staff);
    }

    /**
     * スタッフ更新（パスワード変更にも対応）
     */
    @Transactional
    public Staff updateStaff(Long staffId, UpdateStaffRequest request) {
        if (staffId == null) {
            throw new IllegalArgumentException("staffId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staffId));

        if (request.getRole() != null) {
            staff.setRole(Role.valueOf(request.getRole()));
        }
        if (request.getIsActive() != null) {
            staff.setIsActive(request.getIsActive());
        }
        if (request.getIsPractitioner() != null) {
            staff.setIsPractitioner(request.getIsPractitioner());
        }
        if (staff.getIsPractitioner() == null) {
            staff.setIsPractitioner(true);
        }

        User user = staff.getUser();
        boolean userUpdated = false;
        if (user != null) {
            if (request.getUserName() != null) {
                user.setName(request.getUserName());
                userUpdated = true;
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
                userUpdated = true;
            }
        }

        // パスワード更新（設定されている場合のみ）
        if (user != null && request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            userUpdated = true;
        }

        if (userUpdated) {
            userRepository.save(user);
        }

        return staffRepository.save(staff);
    }

    public void deleteById(Long id) {
        staffRepository.deleteById(id);
    }

    /**
     * 指定メールのユーザーが所属する最初の Staff を返す（互換メソッド）
     */
    public Optional<Staff> findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Optional.empty();
        List<Staff> list = staffRepository.findByUser(userOpt.get());
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    /**
     * パスワード照合ロジック.
     * 本番環境ではBCryptPasswordEncoderを使用
     */
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
    
    /**
     * 管理者ログイン処理
     * 1. User を email で検索
     * 2. パスワード照合
     * 3. user の所属店舗一覧を取得し、先頭を返す
     */
    @Transactional
    public Staff authenticate(String email, String password) {
    	
    	// ユーザ取得
        User user = userRepository.findByEmail(email)
        		.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います"));

        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new IllegalArgumentException("User is inactive");
        }

        if (!verifyPassword(user, password)) {
        	throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います");
        }

        List<Staff> affiliations = staffRepository.findByUser(user);
        if (affiliations == null || affiliations.isEmpty()) {
            throw new IllegalArgumentException("No staff affiliation for user");
        }
        
        // 重要: 所属店舗の有効チェックも入れるのがベター
        // Streamを使って「有効な所属」の先頭を探す
        // TODO 要多店舗対応
        Staff activeStaff = affiliations.stream()
            .filter(s -> s.getIsActive() != null && s.getIsActive())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No active staff affiliation found"));
        
        // 参考にした部分: 最終ログイン日時を更新する（Userテーブルにカラムがあれば）
        // user.setLastLoginAt(LocalDateTime.now());
        // userRepository.save(user); // トランザクション内なのでsaveしなくても更新されるが、明示してもOK

        // 重要: ここでUserの中身を触ってロードさせておく（おまじない）
        //activeStaff.getUser().getEmail(); 

        return activeStaff;
    }

    /**
     * 互換用: Optionalで欲しい場合はこちらを使用.
     */
    public Optional<Staff> authenticateOptional(String email, String password) {
        try {
            return Optional.of(authenticate(email, password));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
