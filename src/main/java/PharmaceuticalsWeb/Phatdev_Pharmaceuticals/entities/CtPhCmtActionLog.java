//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPhCmtActionLog.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_PH_CMT_ACTION_LOG (ÁNH XẠ BẢNG [CT_PH_CMT_ACTION_LOG])
 * =========================================================================
 * Mục đích: Nhật ký kiểm toán hành vi Đăng tải và Cập nhật áp dụng riêng 
 * cho các nhánh Phản hồi (Bình luận cấp 2 trở lên).
 * Thiết kế đối xứng hoàn toàn với CT_CMT_ACTION_LOG.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_PH_CMT_ACTION_LOG")
public class CtPhCmtActionLog {

    /** Khóa chính độc lập của từng phiên tác động */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Mã định danh của Phản hồi bị tác động */
    @Column(name = "PH_CMT_ID", nullable = false)
    private Long phCmtId;

    /** Tài khoản thực thi hành vi */
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    /** Phân loại hành vi (CREATE_PH_CMT, UPDATE_PH_CMT) */
    @Column(name = "ACTION_ID", nullable = false)
    private Integer actionId;

    /** Nội dung văn bản CŨ trước khi sửa */
    @Column(name = "OLD_PAYLOAD", columnDefinition = "NVARCHAR(MAX)")
    private String oldPayload;

    /** Nội dung văn bản MỚI sau khi lưu */
    @Column(name = "NEW_PAYLOAD", columnDefinition = "NVARCHAR(MAX)")
    private String newPayload;

    /** Địa chỉ IP thực thi hành vi */
    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    /** Trình duyệt và hệ điều hành của phiên kết nối */
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    /** Thời điểm ghi log */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}