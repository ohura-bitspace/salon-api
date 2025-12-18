package jp.bitspace.salon.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.model.User;
import jp.bitspace.salon.repository.StaffRepository;
import jp.bitspace.salon.repository.UserRepository;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    public Optional<Staff> findById(Long id) {
        return staffRepository.findById(id);
    }

    public Staff save(Staff staff) {
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
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new IllegalArgumentException("User is inactive");
        }

        if (!verifyPassword(user, password)) {
            throw new IllegalArgumentException("Invalid credentials");
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
