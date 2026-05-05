// src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPostRole.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_POST_ROLES (ÁNH XẠ BẢNG [CT_POST_ROLES])
 * =========================================================================
 * Bảng định tuyến chuẩn 5NF phân quyền Bài viết.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_POST_ROLES")
public class CtPostRole {

    @EmbeddedId
    private CtPostRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "ROLE_ID")
    private UserRole role;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtPostRoleId implements Serializable {
        @Column(name = "POST_ID")
        private Long postId;

        @Column(name = "ROLE_ID")
        private Integer roleId;
    }
}