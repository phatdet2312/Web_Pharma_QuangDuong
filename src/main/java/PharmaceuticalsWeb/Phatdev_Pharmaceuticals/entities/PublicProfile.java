//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PublicProfile.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ PUBLIC_PROFILES (ÁNH XẠ BẢNG [PUBLIC_PROFILES])
 * =========================================================================
 * Mục đích tồn tại: 
 * - Tách biệt hoàn toàn thông tin định danh nội bộ (USERS) và thông tin
 * muốn phô bày ra ngoài công chúng (PUBLIC_PROFILES).
 * - Đạt chuẩn 4NF, quan hệ 1-1 với USERS.
 * - Cho phép Chuyên gia/Tác giả tự xây dựng thương hiệu cá nhân (Bio, Title)
 * mà không làm phình to bảng USERS gốc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PUBLIC_PROFILES")
public class PublicProfile {

    /** Mã định danh độc lập của Hồ sơ công khai */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** * Khóa ngoại liên kết 1-1 với tài khoản gốc.
     * Ràng buộc UNIQUE đảm bảo một người dùng chỉ có tối đa 1 Hồ sơ công khai.
     */
    @Column(name = "USER_ID", nullable = false, unique = true)
    private Long userId;

    /** Danh xưng chuyên môn hoặc Học hàm/Học vị (Ví dụ: 'ThS.BS', 'Dược sĩ CK1') */
    @Column(name = "PROFESSIONAL_TITLE", length = 100)
    private String professionalTitle;

    /** Đơn vị công tác hiện tại (Ví dụ: 'Bệnh viện Chợ Rẫy') */
    @Column(name = "WORKPLACE", length = 255)
    private String workplace;

    /** * Tiểu sử chuyên môn, thành tựu, kinh nghiệm.
     * Sử dụng NVARCHAR(MAX) để hỗ trợ lưu đoạn văn bản dài có định dạng.
     */
    @Column(name = "BIO", columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    /** Đường dẫn tới ảnh đại diện công khai (Có thể khác với Avatar nội bộ) */
    @Column(name = "AVATAR_URL", length = 255)
    private String avatarUrl;

    /** * Cờ Quyền riêng tư (Lá chắn dữ liệu): 
     * 1 (true) = Cho phép hiển thị trên Bài viết/Sự kiện.
     * 0 (false) = Chế độ ẩn danh, hệ thống Backend sẽ tự động che giấu mọi thông tin.
     */
    @Column(name = "IS_VISIBLE", nullable = false)
    private boolean isVisible = true;

    /** Mốc thời gian khởi tạo hồ sơ công khai lần đầu */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Mốc thời gian người dùng cập nhật thông tin hồ sơ gần nhất */
    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}