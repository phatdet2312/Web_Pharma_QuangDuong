//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/EventAgenda.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ EVENT_AGENDA (ÁNH XẠ BẢNG [EVENT_AGENDA])
 * =========================================================================
 * Quản lý cấu trúc thời gian biểu (Timeline) chi tiết cho Phiên sự kiện.
 * Đã loại bỏ cột SPEAKER_INFO để đảm bảo cấu trúc 1NF. Liên kết diễn giả
 * sẽ được xử lý độc lập qua bảng cầu nối CT_AGENDA_SPEAKERS.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EVENT_AGENDA")
public class EventAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Phiên sự kiện chứa lịch trình này (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CT_EVENT_ID", nullable = false)
    private CtEvent ctEvent;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    /** Chủ đề của mốc thời gian (VD: Khai mạc, Báo cáo chuyên đề 1...) */
    @Column(name = "SESSION_TITLE", nullable = false, length = 255)
    private String sessionTitle;

    @Column(name = "DESCRIPTION", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Builder.Default
    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder = 1;
}