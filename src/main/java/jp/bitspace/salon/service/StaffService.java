package jp.bitspace.salon.service;

import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.repository.StaffRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {
    private final StaffRepository staffRepository;

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
     * パスワード照合ロジック
     * TODO: 本番環境ではBCryptPasswordEncoderを使用すること
     * 現在は開発用として単純な文字列比較を実施
     */
    public boolean verifyPassword(Staff staff, String rawPassword) {
        // TODO: BCryptPasswordEncoder.matches(rawPassword, staff.getPasswordHash()) に置き換え
        return staff.getPasswordHash().equals(rawPassword);
    }

    /**
     * 管理者ログイン処理
     */
    public Optional<Staff> authenticate(String email, String password) {
        Optional<Staff> staff = findByEmail(email);
        if (staff.isPresent() && staff.get().getIsActive() && verifyPassword(staff.get(), password)) {
            return staff;
        }
        return Optional.empty();
    }
}
