//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPhCmtReportModLog.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_PH_CMT_REPORT_MOD_LOG (ÁNH XẠ BẢNG [CT_PH_CMT_REPORT_MOD_LOG])
 * =========================================================================
 * Mục đích: Sổ tay ghi án (Audit Log) lưu vết tiến trình xử lý đơn báo cáo
 * dành cho Phản hồi thứ cấp (Reply Comments).
 * Đặc tả: Thiết kế đối xứng hoàn toàn với CT_CMT_REPORT_MOD_LOG để đảm bảo 
 * tính đồng nhất trong kiến trúc kiểm toán của hệ thống.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_PH_CMT_REPORT_MOD_LOG")
public class CtPhCmtReportModLog {

    /** Mã định danh độc lập của từng lệnh kiểm duyệt đơn báo cáo phản hồi */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Đơn báo cáo phản hồi mục tiêu bị Quản trị viên xử lý (Khóa ngoại trỏ về CT_PH_CMT_REPORTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID", nullable = false)
    private CtPhCmtReport report;

    /** Mã hành vi kiểm duyệt (Khóa ngoại trỏ về MODERATION_ACTIONS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID", nullable = false)
    private ModerationAction action;

    /** Tài khoản Quản trị viên đưa ra phán quyết (Khóa ngoại trỏ về USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODERATOR_ID", nullable = false)
    private User moderator;

    /** Trạng thái nguyên bản của đơn báo cáo trước khi bị can thiệp */
    @Column(name = "OLD_STATUS", nullable = false, length = 20)
    private String oldStatus;

    /** Trạng thái mới của đơn báo cáo sau khi hoàn tất can thiệp */
    @Column(name = "NEW_STATUS", nullable = false, length = 20)
    private String newStatus;

    /** Lý do đưa ra phán quyết (Bắt buộc phải ghi nhận) */
    @Column(name = "REASON", nullable = false, length = 255)
    private String reason;

    /** Dấu vết mạng (IP) của Quản trị viên thực thi lệnh */
    @Column(name = "IP_ADDRESS", nullable = false, length = 50)
    private String ipAddress;

    /** Thông tin chữ ký thiết bị (Browser / OS) của Quản trị viên */
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    /** Mốc thời gian hệ thống chính thức đóng hồ sơ xử lý báo cáo */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}