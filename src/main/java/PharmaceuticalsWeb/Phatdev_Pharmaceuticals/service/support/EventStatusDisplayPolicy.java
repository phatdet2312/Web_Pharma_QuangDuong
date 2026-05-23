//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/support/EventStatusDisplayPolicy.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support;

import org.springframework.stereotype.Component;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: CHÍNH SÁCH HIỂN THỊ TRẠNG THÁI SỰ KIỆN
 * =========================================================================
 * Chuyển đổi mã trạng thái vận hành của phiên sự kiện thành ngôn ngữ hiển thị
 * phù hợp với từng ngữ cảnh. Public chỉ nhận thông điệp thân thiện với người
 * dùng, còn Admin vẫn có thể đối chiếu mã gốc khi cần kiểm toán.
 */
@Component
public class EventStatusDisplayPolicy {

    /**
     * Dịch mã trạng thái sang nhãn công khai cho timeline người dùng cuối.
     */
    public String layNhanPublic(String statusCode) {
        String code = chuanHoaMaTrangThai(statusCode);

        if ("OPEN".equals(code) == true || "UPCOMING".equals(code) == true) {
            return "Đã mở đăng ký";
        }
        if ("FULL".equals(code) == true) {
            return "Đã đủ số lượng đăng ký";
        }
        if ("ONGOING".equals(code) == true) {
            return "Sự kiện đang diễn ra";
        }
        if ("FINISHED".equals(code) == true || "ENDED".equals(code) == true) {
            return "Sự kiện đã kết thúc";
        }
        if ("CANCELLED".equals(code) == true) {
            return "Sự kiện đã hủy";
        }
        if ("DRAFT".equals(code) == true) {
            return "Sự kiện đang chuẩn bị";
        }
        if ("LOCKED".equals(code) == true) {
            return "Phiên chuyên môn giới hạn";
        }

        return "Đã cập nhật trạng thái";
    }

    /**
     * Dịch mã trạng thái sang nhãn quản trị, giữ nghĩa vận hành rõ hơn public.
     */
    public String layNhanAdmin(String statusCode) {
        String code = chuanHoaMaTrangThai(statusCode);

        if ("DRAFT".equals(code) == true) {
            return "Bản nháp";
        }
        if ("OPEN".equals(code) == true) {
            return "Mở đăng ký";
        }
        if ("UPCOMING".equals(code) == true) {
            return "Sắp diễn ra";
        }

        return layNhanPublic(code);
    }

    /**
     * Xác định trạng thái có được phép xuất hiện trên timeline public hay không.
     */
    public boolean duocHienThiPublic(String statusCode) {
        String code = chuanHoaMaTrangThai(statusCode);
        if ("DRAFT".equals(code) == true) {
            return false;
        }
        return true;
    }

    /**
     * Chuẩn hóa mã trạng thái trước khi so khớp để tránh lỗi do khoảng trắng hoặc chữ thường.
     */
    private String chuanHoaMaTrangThai(String statusCode) {
        if (statusCode == null) {
            return "";
        }
        return statusCode.trim().toUpperCase();
    }
}
