//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostDetailResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.management.relation.RoleInfo;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: DTO ĐÓNG GÓI CHI TIẾT BÀI VIẾT Y KHOA
 * =========================================================================
 * Mục đích: Vận chuyển khối lượng dữ liệu khổng lồ của một ấn phẩm khoa học
 * cho trang chi tiết.
 * Đặc tả kiến trúc: Đã gỡ bỏ các trường thông tin tác giả rời rạc, áp dụng 
 * kỹ thuật nhúng DTO (Nested DTO) để tái sử dụng cấu trúc PublicProfileResponse.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {

    /** Mã định danh độc lập của Bài viết */
    private Long id;

    /** Tiêu đề chính của Bài viết */
    private String title;

    /** Đường dẫn tĩnh thân thiện với công cụ tìm kiếm (SEO) */
    private String slug;

    /** Đoạn văn bản giới thiệu ngắn gọn (Lead-in) nhằm thu hút độc giả */
    private String summary;

    /** * Nội dung HTML/Markdown đầy đủ của bài viết.
     * CỰC KỲ QUAN TRỌNG: Chỉ trả về nội dung khi user có quyền truy cập (hasFullAccess = true).
     * Nếu không, giá trị này bắt buộc phải là null để chống rò rỉ dữ liệu qua Network tab.
     */
    private String content;

    /** * Cờ tín hiệu chỉ thị cho Frontend kích hoạt giao diện Khóa nội dung (Paywall) 
     * để bảo vệ nội dung chuyên gia (Gated Content).
     */
    private boolean hasFullAccess;

    /** Hình ảnh minh họa (Thumbnail) chính của Bài viết */
    private String thumbnailUrl;

    /** Danh sách quyền yêu cầu để mở khóa bài viết (Paywall Động) */
    private List<RoleInfo> requiredRoles;

    /** Cờ chỉ thị bài viết đã được kiểm duyệt và cho phép đưa ra công chúng */
    private boolean isPublished;

    /** Tiêu đề tối ưu hóa hiển thị cho bộ máy tìm kiếm Google (SEO Title) */
    private String seoTitle;

    /** Khái quát nội dung tối ưu hóa cho bộ máy tìm kiếm Google (SEO Description) */
    private String seoDescription;

    /** Mã định danh Chuyên mục lưu trữ Bài viết */
    private Integer categoryId;

    /** Tên Chuyên mục lưu trữ Bài viết (VD: Nghiên cứu lâm sàng) */
    private String categoryName;

    /** Đường dẫn tĩnh của Chuyên mục */
    private String categorySlug;

    /** * Gói dữ liệu Hồ sơ công khai của Tác giả (Tích hợp kiến trúc nhúng).
     * Chịu sự chi phối của "Lá chắn quyền riêng tư" tại Tầng Service.
     * Nếu tác giả bật ẩn danh, Object này sẽ trả về dữ liệu ảo "Tác giả ẩn danh".
     */
    private PublicProfileResponse authorProfile;

    /** Danh sách các Thẻ chủ đề (Tags) được gắn vào Bài viết */
    private List<TagResponse> tags;

    /** Danh mục hình ảnh minh họa đi kèm được sắp xếp theo đúng thứ tự hiển thị */
    private List<PostImageResponse> images;

    /** Thư viện tài liệu đính kèm (PDF, Word) cho phép đối tác/bác sĩ tải xuống */
    private List<PostFileResponse> files;

    /** Tổng lưu lượng độc giả đã truy cập trang chi tiết này */
    private long viewCount;

    /** Tổng số lượt tải xuống thành công của tất cả tài liệu đính kèm */
    private long downloadCount;

    /** Tổng số lượng Bình luận gốc hợp lệ đang hiển thị trên bài viết */
    private long commentCount;

    /** * Bản đồ tần suất Cảm xúc của cộng đồng mạng trên bài viết này.
     * Cấu trúc: Key là Mã cảm xúc (VD: 'LIKE', 'LOVE'), Value là Số lượng (VD: 150)
     */
    private Map<String, Long> reactionCounts;

    /** Dấu ấn cảm xúc cá nhân của chính độc giả đang xem bài (Trả về null nếu chưa tương tác) */
    private String currentUserReaction;
    
    /** Danh sách các Bài viết có độ tương đồng cao trong cùng Chuyên mục (Dành cho phần Đề xuất) */
    private List<PostResponse> relatedPosts;

    /** Mốc thời gian hệ thống khởi tạo bản nháp đầu tiên */
    private LocalDateTime createdAt;

    /** Mốc thời gian Tác giả hoặc Admin cập nhật nội dung lần gần nhất */
    private LocalDateTime updatedAt;

    @Getter 
    @Setter 
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class RoleInfo {
        private String roleName;
        private String description;
    }
}

