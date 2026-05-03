//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtEventRegistration.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_EVENT_REGISTRATIONS (ÁNH XẠ BẢNG [CT_EVENT_REGISTRATIONS])
 * =========================================================================
 * Đăng ký tham dự sự kiện. Hỗ trợ cả user đăng nhập lẫn khách không đăng nhập.
 * USER_ID nullable — khách ẩn danh điền thông tin GUEST_*.
 * STATUS: PENDING | CONFIRMED | ATTENDED | CANCELLED.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_EVENT_REGISTRATIONS")
public class CtEventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Buổi sự kiện đăng ký (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CT_EVENT_ID", nullable = false)
    private CtEvent ctEvent;

    /** Người dùng đăng nhập (nullable — khách ẩn danh không có) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    /** Tên khách ẩn danh */
    @Column(name = "GUEST_NAME", length = 100)
    private String guestName;

    /** Email khách ẩn danh */
    @Column(name = "GUEST_EMAIL", length = 100)
    private String guestEmail;

    /** Điện thoại khách ẩn danh */
    @Column(name = "GUEST_PHONE", length = 20)
    private String guestPhone;

    /** Nơi công tác */
    @Column(name = "WORKPLACE", length = 200)
    private String workplace;

    /** Trạng thái đăng ký: PENDING | CONFIRMED | ATTENDED | CANCELLED */
    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "PENDING";

    @Builder.Default
    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();
}
