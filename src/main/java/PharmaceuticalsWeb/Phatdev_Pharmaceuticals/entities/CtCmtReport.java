//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtCmtReport.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_CMT_REPORTS (ÁNH XẠ BẢNG [CT_CMT_REPORTS])
 * =========================================================================
 * Mục đích: Sổ tay ghi nhận mọi cảnh báo vi phạm từ cộng đồng đối với Bình luận gốc.
 * Kiến trúc bảo mật: Chủ đích KHÔNG SỬ DỤNG khóa kép (Composite Key). 
 * Cho phép ghi nhận vô hạn số lần báo cáo từ cùng một người dùng (hoặc bot) 
 * để hệ thống thu thập đủ Dữ liệu thô (Raw Data) phục vụ thuật toán phân tích 
 * tần suất Spam và ngăn chặn tấn công Sybil dựa trên địa chỉ IP.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_CMT_REPORTS")
public class CtCmtReport {

    /** Mã định danh độc lập của từng lượt báo cáo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Bình luận gốc bị báo cáo (Khóa ngoại trỏ về bảng CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMT_ID", nullable = false)
    private Cmt cmt;

    /** Tài khoản phát lệnh báo cáo (Khóa ngoại trỏ về bảng USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /** Lý do báo cáo chi tiết được chọn từ giao diện (VD: 'Thông tin sai lệch', 'Spam') */
    @Column(name = "REASON", nullable = false, length = 255)
    private String reason;

    /** Dấu vết IP mạng để đối soát và thiết lập rào chắn tường lửa khi có tấn công có tổ chức */
    @Column(name = "REPORTER_IP", nullable = false, length = 50)
    private String reporterIp;

    /** * Trạng thái tiến trình xử lý của Quản trị viên. 
     * Các mốc tiêu chuẩn: PENDING (Chờ xử lý), RESOLVED (Đã giải quyết), REJECTED (Bác bỏ) 
     */
    @Column(name = "STATUS", length = 20)
    private String status = "PENDING";

    /** Mốc thời gian hệ thống ghi nhận gói tin báo cáo */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}