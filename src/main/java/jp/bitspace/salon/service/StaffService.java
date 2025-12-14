package jp.bitspace.salon.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.repository.StaffRepository;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    
    // クラスのフィールドに追加（またはBean注入）
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
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

    public Optional<Staff> findByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    /**
     * パスワード照合ロジック.</br>
     * 本番環境ではBCryptPasswordEncoderを使用
     */
    public boolean verifyPassword(Staff staff, String rawPassword) {
    	// matches(平文パスワード, ハッシュ化されたパスワード)
    	boolean matches = passwordEncoder.matches(rawPassword, staff.getPasswordHash());
    	//System.out.println( rawPassword + ","+ staff.getPasswordHash() + "," + matches);
        return matches;
    }

    /**
     * 管理者ログイン処理
     */
    public Staff authenticate(String email, String password) {
        Optional<Staff> staff = findByEmail(email);
        if (staff.isPresent() && staff.get().getIsActive() && verifyPassword(staff.get(), password)) {
            return staff.get();
        }
        throw new IllegalArgumentException("Invalid credentials");
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
