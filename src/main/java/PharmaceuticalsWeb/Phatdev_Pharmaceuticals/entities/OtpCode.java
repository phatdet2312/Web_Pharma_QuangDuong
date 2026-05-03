//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/OtpCode.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ OTP_CODES (ÁNH XẠ BẢNG [OTP_CODES])
 * =========================================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OTP_CODES")
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ATTEMPTS")
    private int attempts = 0;

    @Column(name = "CODE", nullable = false, length = 10)
    private String code;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "EXPIRY_AT", nullable = false)
    private LocalDateTime expiryAt;

    @Column(name = "USED")
    private boolean used = false;
    
    // Gán dữ liệu trước khi lưu vào DB (Nếu SQL không tự sinh)
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiryAt == null) {
            this.expiryAt = this.createdAt.plusMinutes(3); // Hiệu lực 3 phút
        }
    }
}