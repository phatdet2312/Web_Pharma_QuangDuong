//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PublicProfileResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI ĐA HÌNH: HỒ SƠ CÔNG KHAI (REUSABLE DTO)
 * =========================================================================
 * Mục đích: Gói dữ liệu tiêu chuẩn dùng chung cho toàn bộ hệ thống ở bất kỳ 
 * nơi nào cần hiển thị thông tin Tác giả, Chuyên gia hoặc Bác sĩ.
 * Khả năng tái sử dụng: Có thể nhúng trực tiếp vào các DTO lớn hơn (như Bài viết).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileResponse {

    /** Mã định danh độc lập của Hồ sơ công khai (Khóa chính bảng PUBLIC_PROFILES) */
    private Long id;

    /** Mã định danh tài khoản cốt lõi trên hệ thống */
    private Long userId;

    /** Họ và tên đầy đủ (Được truy xuất chéo từ bảng USERS) */
    private String fullName;

    /** Chức danh chuyên môn (VD: Dược sĩ lâm sàng) */
    private String professionalTitle;

    /** Đơn vị công tác hiện tại */
    private String workplace;

    /** Tiểu sử và kinh nghiệm chuyên môn */
    private String bio;

    /** Đường dẫn hiển thị hình ảnh đại diện cá nhân */
    private String avatarUrl;

    /** Trạng thái quyết định cho phép hệ thống phân phối dữ liệu này */
    private boolean isVisible;

    /** Tổng lưu lượng bài viết khoa học mà cá nhân này đã xuất bản thành công */
    private long totalPublishedPosts;
}