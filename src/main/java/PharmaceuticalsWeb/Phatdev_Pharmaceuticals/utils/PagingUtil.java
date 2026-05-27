//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/utils/PagingUtil.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils;

/**
 * =========================================================================
 * HÀM TIỆN ÍCH CHUẨN HÓA PHÂN TRANG (PAGING UTILITY)
 * =========================================================================
 * Dùng chung cho toàn bộ phân hệ (Admin, Public, API Controller).
 * Đảm bảo PageRequest luôn nhận giá trị hợp lệ, tránh lỗi 500
 * khi client gửi page âm hoặc size bằng 0.
 *
 * Quy ước:
 * - DEFAULT_PAGE_SIZE = 12: mặc định khi client không truyền hoặc truyền <= 0
 * - MAX_PAGE_SIZE = 100: giới hạn tối đa để tránh query/response quá lớn
 */
public final class PagingUtil {

    /** Số phần tử mặc định trên mỗi trang khi client không chỉ định */
    public static final int DEFAULT_PAGE_SIZE = 12;

    /** Số phần tử tối đa cho phép trên mỗi trang để bảo vệ hiệu năng */
    public static final int MAX_PAGE_SIZE = 100;

    /** Không cho phép khởi tạo instance — lớp tiện ích thuần tĩnh */
    private PagingUtil() {
    }

    /**
     * Chuẩn hóa chỉ số trang: đảm bảo page luôn >= 0.
     * Nếu client truyền giá trị âm, trả về trang đầu tiên (0).
     */
    public static int chuanHoaPage(int page) {
        if (page < 0) {
            return 0;
        }
        return page;
    }

    /**
     * Chuẩn hóa kích thước trang: đảm bảo size nằm trong [1, MAX_PAGE_SIZE].
     * - size <= 0: trả DEFAULT_PAGE_SIZE
     * - size > MAX_PAGE_SIZE: trả MAX_PAGE_SIZE
     */
    public static int chuanHoaSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return size;
    }
}
