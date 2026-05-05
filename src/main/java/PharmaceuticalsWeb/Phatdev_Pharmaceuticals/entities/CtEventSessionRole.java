// src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtEventSessionRole.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_EVENT_SESSION_ROLES (ÁNH XẠ BẢNG [CT_EVENT_SESSION_ROLES])
 * =========================================================================
 * Bảng định tuyến chuẩn 5NF phân quyền Trạm/Buổi Sự kiện.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_EVENT_SESSION_ROLES")
public class CtEventSessionRole {

    @EmbeddedId
    private CtEventSessionRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ctEventId")
    @JoinColumn(name = "CT_EVENT_ID")
    private CtEvent ctEvent;

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
    public static class CtEventSessionRoleId implements Serializable {
        @Column(name = "CT_EVENT_ID")
        private Long ctEventId;

        @Column(name = "ROLE_ID")
        private Integer roleId;
    }
}