//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventAttendeePublicResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: DTO TÓM TẮT KHÁCH MỜI DÀNH CHO CÔNG CHÚNG
 * =========================================================================
 * Đóng gói dữ liệu Social Proof giúp kích thích tỷ lệ chuyển đổi Marketing.
 * Dữ liệu đã được xử lý "Masking" (Che giấu) ngay tại Backend để đảm bảo 
 * tuyệt đối không rò rỉ danh tính thực của Bác sĩ/Đối tác ra môi trường Public.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeePublicResponse {

    /** Tổng số lượng người đã đăng ký thành công trên toàn hệ thống */
    private long totalCount;

    /** Danh sách thông tin khách mời đã được che giấu một phần */
    private List<AttendeeMaskedInfo> attendees;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendeeMaskedInfo {
        /** Chữ cái đầu để vẽ Avatar (VD: N, T, L) */
        private String initial;
        
        /** Tên đã che (VD: Nguyễn V*** A***) */
        private String maskedName;
        
        /** Số điện thoại đã che (VD: 098****123) */
        private String maskedPhone;
        
        /** Nơi công tác (Nếu có) */
        private String workplace;

        /** Thời điểm ghi nhận đăng ký thành công */
        private LocalDateTime registeredAt;
    }
}