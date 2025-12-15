package jp.bitspace.salon.security;

/**
 * 管理側JWT認証で使うPrincipal.
 */
public class AdminPrincipal {
    private final Long staffId;
    private final String email;
    private final Long salonId;
    private final String role;

    public AdminPrincipal(Long staffId, String email, Long salonId, String role) {
        this.staffId = staffId;
        this.email = email;
        this.salonId = salonId;
        this.role = role;
    }

    public Long getStaffId() {
        return staffId;
    }

    public String getEmail() {
        return email;
    }

    public Long getSalonId() {
        return salonId;
    }

    public String getRole() {
        return role;
    }
}
