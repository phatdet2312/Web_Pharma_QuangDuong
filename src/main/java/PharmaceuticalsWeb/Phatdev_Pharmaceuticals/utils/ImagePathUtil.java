//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/utils/ImagePathUtil.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;

/**
 * =========================================================================
 * HÀM TIỆN ÍCH CHUẨN HÓA ĐƯỜNG DẪN ẢNH (IMAGE PATH UTILITY)
 * =========================================================================
 * Dùng chung cho toàn bộ phân hệ quản trị sự kiện (Campaign, Speaker).
 * Validate URL ảnh từ API để chỉ nhận file do upload server cấp,
 * chặn mọi đường dẫn bên ngoài hoặc quá dài.
 *
 * Thiết kế tập trung — tránh duplicate logic giữa AdminEventServiceImpl
 * và SpeakerAgendaServiceImpl.
 */
public final class ImagePathUtil {

    /** Không cho phép khởi tạo instance — lớp tiện ích thuần tĩnh */
    private ImagePathUtil() {
    }

    /**
     * Chuẩn hóa đường dẫn ảnh với các tham số tùy chỉnh.
     * - rawUrl null hoặc rỗng → trả null (ảnh không bắt buộc)
     * - Kiểm tra độ dài tối đa theo ràng buộc cột DB
     * - Kiểm tra prefix hợp lệ để đảm bảo file nằm trong server upload
     *
     * @param rawUrl       Đường dẫn ảnh gốc từ client
     * @param maxLength    Giới hạn ký tự theo cột DB
     * @param prefixHopLe  Tiền tố đường dẫn hợp lệ (VD: "/uploads/events/campaigns/")
     * @return URL đã chuẩn hóa hoặc null
     */
    public static String chuanHoaDuongDanAnh(String rawUrl, int maxLength, String prefixHopLe) {
        if (rawUrl == null || rawUrl.trim().isEmpty() == true) {
            return null;
        }
        String url = rawUrl.trim();
        if (url.length() > maxLength) {
            throw new AppException(400, "Đường dẫn ảnh vượt quá giới hạn " + maxLength + " ký tự.");
        }
        if (url.startsWith(prefixHopLe) == true) {
            return url;
        }
        throw new AppException(400, "Ảnh sự kiện phải được tải lên qua hệ thống quản trị.");
    }

    /**
     * Chuẩn hóa đường dẫn ảnh diễn giả (Speaker Avatar).
     * Áp dụng mặc định: maxLength = 255, prefix = "/uploads/events/speakers/".
     *
     * @param rawUrl Đường dẫn ảnh gốc từ client
     * @return URL đã chuẩn hóa hoặc null
     */
    public static String chuanHoaDuongDanAnh(String rawUrl) {
        return chuanHoaDuongDanAnh(rawUrl, 255, "/uploads/events/speakers/");
    }
}
