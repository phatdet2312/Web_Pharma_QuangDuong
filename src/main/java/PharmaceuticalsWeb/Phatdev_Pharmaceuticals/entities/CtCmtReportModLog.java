//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtCmtReportModLog.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_CMT_REPORT_MOD_LOG (ÁNH XẠ BẢNG [CT_CMT_REPORT_MOD_LOG])
 * =========================================================================
 * Mục đích: Sổ tay ghi án (Audit Log) lưu vết tiến trình xử lý đơn báo cáo
 * dành cho Bình luận gốc. 
 * Đặc tả: Đóng vai trò kép — vừa là nhật ký truy vết (Audit Trail) để đối soát
 * chống lạm quyền, vừa là chi tiết giao dịch (Transaction Detail) lưu giữ 
 * lý do và sự chuyển đổi trạng thái làm bằng chứng pháp lý.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_CMT_REPORT_MOD_LOG")
public class CtCmtReportModLog {

    /** Mã lệnh kiểm duyệt đơn báo cáo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Đơn báo cáo mục tiêu bị xử lý (Khóa ngoại) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID", nullable = false)
    private CtCmtReport report;

    /** Mã hành vi kiểm duyệt (Khóa ngoại trỏ về MODERATION_ACTIONS, VD: RESOLVE_REPORT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID", nullable = false)
    private ModerationAction action;

    /** Quản trị viên ra phán quyết (Khóa ngoại) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODERATOR_ID", nullable = false)
    private User moderator;

    /** Trạng thái của đơn báo cáo trước khi bị can thiệp (VD: 'PENDING') */
    @Column(name = "OLD_STATUS", nullable = false, length = 20)
    private String oldStatus;

    /** Trạng thái của đơn báo cáo sau khi hoàn tất can thiệp (VD: 'RESOLVED', 'REJECTED') */
    @Column(name = "NEW_STATUS", nullable = false, length = 20)
    private String newStatus;

    /** Lý do đưa ra phán quyết (Bắt buộc phải ghi nhận để giải trình) */
    @Column(name = "REASON", nullable = false, length = 255)
    private String reason;

    /** Dấu vết mạng (IP) của Quản trị viên thực thi lệnh để đối soát rủi ro */
    @Column(name = "IP_ADDRESS", nullable = false, length = 50)
    private String ipAddress;

    /** Thông tin chữ ký thiết bị (Browser / OS) của Quản trị viên */
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    /** Mốc thời gian hệ thống đóng hồ sơ xử lý */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}