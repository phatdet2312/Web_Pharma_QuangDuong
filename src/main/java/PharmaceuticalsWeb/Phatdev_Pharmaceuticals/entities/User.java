//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/User.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * =========================================================================
 * THỰC THỂ USERS (ÁNH XẠ BẢNG [USERS])
 * =========================================================================
 * Đã đập bỏ Enum Set<Role>. Dùng biến Transient để chứa danh sách quyền động.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USERS")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username từ 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username chỉ chứa chữ cái, số, gạch dưới hoặc gạch nối")
    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu từ 6-100 ký tự")
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "FULL_NAME", length = 100)
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải hợp lệ và chứa ký tự '@'")
    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PHONE", length = 15)
    private String phone;

    @Column(name = "ADDRESS", length = 255)
    private String address;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "PROVIDER", length = 50)
    private String provider;

    @Column(name = "LOCKED")
    private boolean locked = false;

    // Cột thời gian sẽ do CSDL SQL Server tự động sinh (DEFAULT GETDATE())
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // =====================================================================
    // TRƯỜNG DỮ LIỆU TRÊN RAM (KHÔNG LƯU XUỐNG DB)
    // Dùng để chứa danh sách Quyền sau khi Tầng Service query từ 6 bảng.
    // =====================================================================
    @Transient
    private List<String> danhSachTenRole = new ArrayList<>();
    
    @Transient
    private List<String> danhSachTenPermission = new ArrayList<>();

    // =====================================================================
    // CẤP QUYỀN CHO SPRING SECURITY (TUÂN THỦ VÒNG LẶP FOR)
    // =====================================================================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 1. Nạp danh sách Nhóm Quyền (Roles)
        if (this.danhSachTenRole != null) {
            Object[] mangRole = this.danhSachTenRole.toArray();
            for (int i = 0; i < mangRole.length; i = i + 1) {
                // Nhóm quyền bắt buộc phải có tiền tố ROLE_ để Spring Security nhận diện
                authorities.add(new SimpleGrantedAuthority("ROLE_" + mangRole[i].toString()));
            }
        }
        
        // 2. Nạp danh sách Quyền hạt lựu (Permissions)
        if (this.danhSachTenPermission != null) {
            Object[] mangPermission = this.danhSachTenPermission.toArray();
            for (int j = 0; j < mangPermission.length; j = j + 1) {
                // Quyền thao tác chi tiết KHÔNG CẦN tiền tố ROLE_
                authorities.add(new SimpleGrantedAuthority(mangPermission[j].toString()));
            }
        }
        
        return authorities;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}