//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtFileDownload.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_FILE_DOWNLOADS (ÁNH XẠ BẢNG [CT_FILE_DOWNLOADS])
 * =========================================================================
 * Theo dõi lượt tải file tài liệu. PK kép (FILE_ID + USER_ID).
 * Dùng để đếm tổng lượt tải và kiểm tra user đã tải chưa.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_FILE_DOWNLOADS")
public class CtFileDownload {

    @EmbeddedId
    private CtFileDownloadId id;

    /** Tài liệu được tải (FK → POST_FILES) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
    @JoinColumn(name = "FILE_ID")
    private PostFile postFile;

    /** Người dùng tải tài liệu (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder.Default
    @Column(name = "DOWNLOADED_AT", nullable = false)
    private LocalDateTime downloadedAt = LocalDateTime.now();

    /**
     * Khóa chính kép: (FILE_ID, USER_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtFileDownloadId implements Serializable {

        @Column(name = "FILE_ID")
        private Long fileId;

        @Column(name = "USER_ID")
        private Long userId;
    }
}
