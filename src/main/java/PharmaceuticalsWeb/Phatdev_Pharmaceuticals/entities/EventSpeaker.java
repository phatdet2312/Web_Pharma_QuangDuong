//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/EventSpeaker.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ EVENT_SPEAKERS (ÁNH XẠ BẢNG [EVENT_SPEAKERS])
 * =========================================================================
 * Quản lý danh sách các chuyên gia, diễn giả khách mời cho từng Phiên sự kiện.
 * Phục vụ việc hiển thị hồ sơ năng lực học thuật trên giao diện Public.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EVENT_SPEAKERS")
public class EventSpeaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Phiên sự kiện mà diễn giả tham gia (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CT_EVENT_ID", nullable = false)
    private CtEvent ctEvent;

    @Column(name = "FULL_NAME", nullable = false, length = 100)
    private String fullName;

    /** Học hàm / Học vị (VD: PGS.TS, BS.CK2) */
    @Column(name = "ACADEMIC_TITLE", length = 100)
    private String academicTitle;

    /** Cơ quan / Tổ chức công tác (VD: Hội Tim mạch Việt Nam) */
    @Column(name = "ORGANIZATION", length = 255)
    private String organization;

    @Column(name = "AVATAR_URL", length = 255)
    private String avatarUrl;

    /** Tiểu sử tóm tắt thành tựu và kinh nghiệm lâm sàng */
    @Column(name = "BIO", columnDefinition = "NVARCHAR(MAX)")
    private String bio;
}