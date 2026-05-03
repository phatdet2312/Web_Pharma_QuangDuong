//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtAgendaSpeaker.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_AGENDA_SPEAKERS (ÁNH XẠ BẢNG [CT_AGENDA_SPEAKERS])
 * =========================================================================
 * Bảng định tuyến N-N giữa Lịch trình và Diễn giả.
 * Xử lý bài toán Tọa đàm (Nhiều chuyên gia cùng tham gia một khung giờ).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_AGENDA_SPEAKERS")
public class CtAgendaSpeaker {

    @EmbeddedId
    private CtAgendaSpeakerId id;

    /** Mốc lịch trình hội thảo (FK → EVENT_AGENDA) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("agendaId")
    @JoinColumn(name = "AGENDA_ID")
    private EventAgenda agenda;

    /** Diễn giả phụ trách (FK → EVENT_SPEAKERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("speakerId")
    @JoinColumn(name = "SPEAKER_ID")
    private EventSpeaker speaker;

    /**
     * Khóa chính kép: (AGENDA_ID, SPEAKER_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtAgendaSpeakerId implements Serializable {

        @Column(name = "AGENDA_ID")
        private Long agendaId;

        @Column(name = "SPEAKER_ID")
        private Long speakerId;
    }
}