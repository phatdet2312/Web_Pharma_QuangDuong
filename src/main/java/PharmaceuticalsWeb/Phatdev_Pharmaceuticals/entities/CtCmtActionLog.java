//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtCmtActionLog.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_CMT_ACTION_LOG (ÁNH XẠ BẢNG [CT_CMT_ACTION_LOG])
 * =========================================================================
 * Mục đích: Sổ tay kiểm toán độc lập ghi lại mọi hành vi tự khởi tạo hoặc 
 * chỉnh sửa văn bản bình luận gốc của người dùng.
 * Phục vụ nghiệp vụ đối soát pháp lý khi người dùng thay đổi nội dung 
 * nhằm chối bỏ trách nhiệm về phát ngôn y khoa của mình.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_CMT_ACTION_LOG")
public class CtCmtActionLog {

    /** Khóa chính độc lập của từng phiên tác động dữ liệu */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Mã định danh của Bình luận gốc bị tác động */
    @Column(name = "CMT_ID", nullable = false)
    private Long cmtId;

    /** Tài khoản thực thi hành vi (Luôn là Tác giả của bình luận) */
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    /** * Khóa ngoại trỏ về từ điển MODERATION_ACTIONS để phân loại hành vi.
     * (Ví dụ: CREATE_CMT, UPDATE_CMT)
     */
    @Column(name = "ACTION_ID", nullable = false)
    private Integer actionId;

    /** Snapshot nguyên bản nội dung văn bản trước khi bị sửa đổi (Sẽ là Null nếu đây là hành vi Khởi tạo) */
    @Column(name = "OLD_PAYLOAD", columnDefinition = "NVARCHAR(MAX)")
    private String oldPayload;

    /** Snapshot nguyên bản nội dung văn bản sau khi được lưu mới */
    @Column(name = "NEW_PAYLOAD", columnDefinition = "NVARCHAR(MAX)")
    private String newPayload;

    /** Dấu vết IP thực thi để chống tình trạng Session Hijacking */
    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    /** Thông tin chữ ký thiết bị (Browser / OS) */
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    /** Thời điểm ghi nhận hành vi biến động dữ liệu */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}