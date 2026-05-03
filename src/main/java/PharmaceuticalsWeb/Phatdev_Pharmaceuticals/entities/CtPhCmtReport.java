//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPhCmtReport.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_PH_CMT_REPORTS (ÁNH XẠ BẢNG [CT_PH_CMT_REPORTS])
 * =========================================================================
 * Mục đích: Ghi nhận các báo cáo vi phạm đối chiếu với tiêu chuẩn cộng đồng 
 * dành riêng cho phân vùng Phản hồi thứ cấp (Reply Comments).
 * Thiết kế đối xứng hoàn toàn với bảng CT_CMT_REPORTS để đảm bảo chuẩn 5NF.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_PH_CMT_REPORTS")
public class CtPhCmtReport {

    /** Mã định danh độc lập của từng lượt báo cáo phản hồi */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Nhánh phản hồi bị báo cáo (Khóa ngoại trỏ về bảng PH_CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PH_CMT_ID", nullable = false)
    private PhCmt phCmt;

    /** Tài khoản phát lệnh báo cáo (Khóa ngoại trỏ về bảng USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /** Lý do báo cáo chi tiết */
    @Column(name = "REASON", nullable = false, length = 255)
    private String reason;

    /** Dấu vết IP mạng của thiết bị gửi báo cáo */
    @Column(name = "REPORTER_IP", nullable = false, length = 50)
    private String reporterIp;

    /** Trạng thái tiến trình xử lý (PENDING, RESOLVED, REJECTED) */
    @Column(name = "STATUS", length = 20)
    private String status = "PENDING";

    /** Mốc thời gian hệ thống nhận được tín hiệu báo cáo */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}