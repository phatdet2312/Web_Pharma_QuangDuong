//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ProfileStatsResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

/**
 * DTO trả về các chỉ số thống kê hiển thị trên thẻ tóm tắt bên phải Profile.
 * Các module chưa xây dựng trả về 0 — không mock dữ liệu ảo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileStatsResponse {

    /** Số địa chỉ doanh nghiệp đã đăng ký — dữ liệu thật từ ADDRESSES */
    private long addressCount;

    /** Số yêu cầu báo giá đã gửi — placeholder 0 cho đến khi Module 2 hoàn thiện */
    private long quoteSent;

    /** Số sự kiện đã tham gia — placeholder 0 cho đến khi Module 4 hoàn thiện */
    private long eventsAttended;

    /** Số bài viết đã xem — placeholder 0 cho đến khi Module 3 hoàn thiện */
    private long postsViewed;
}
