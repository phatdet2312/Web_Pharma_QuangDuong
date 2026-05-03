//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/MaTranNhiPhanNguyenTu.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MaTranNhiPhanNguyenTu {
    
    // Ma trận thưa lưu trữ trên RAM: Chỉ lưu những người có "biến động" về quyền hoặc bị khóa.
    // Key (Long): Là sự kết hợp giữa [Mã Kỷ Nguyên] và [Số hàng].
    // Value (Long): Chuỗi 64 bit đại diện cho 64 User. Bit 1 là "Cần kiểm tra", Bit 0 là "An toàn".
    private static final ConcurrentHashMap<Long, Long> MA_TRAN_BIT = new ConcurrentHashMap<>();
    
    // Mốc thời gian đánh dấu sự ra đời của phiên làm việc Server hiện tại.
    // Dùng để so sánh với thời gian tạo (issuedAt) của Token bay lên.
    //lùi quá khứ 60s vì THOI_DIEM_KHOI_DONG chỉ thực sự chạy chạy khi MaTranNhiPhanNguyenTu được gọi đến lần đầu.
    //nếu không lùi 60s thì khi mát điện server khởi động theo logic tokenCreatedAt sẽ được chạy trước
    //sau đó mới đen tokenCreatedAt so sánh THOI_DIEM_KHOI_DONG, lúc này MaTranNhiPhanNguyenTu mới được gọi
    //đến lần đầu thì THOI_DIEM_KHOI_DONG được khởi tạo nên sẽ luôn nhỏ hơn tokenCreatedAt 
    //-> tất cả user đăng nhập trong khoản thời gian trước khi MaTranNhiPhanNguyenTu 
    // được gọi đến lần đầu sẽ bị đánh dấu là "đời cũ" bị check db dư thừa -> không hợp lý gây nặng csdl.
    public static final long THOI_DIEM_KHOI_DONG = System.currentTimeMillis() - 60000;
    
    // Số định danh Kỷ nguyên (Era ID): Mỗi lần restart server sẽ sinh ra 1 con số ngẫu nhiên khác nhau.
    // Điều này giúp "nhận diện" các hàng dữ liệu cũ trong RAM nếu server restart nhanh đến mức trùng mili giây.
    private static final long MA_KI_HIEU_KY_NGUYEN = new Random().nextInt(1000000);

    // Hàm tạo Key cho Map: Trộn 32 bit của Kỷ nguyên vào 32 bit của Số hàng.
    // Đảm bảo dữ liệu của Kỷ nguyên này không bao giờ bị lẫn với dữ liệu của đời Server trước.
    private static long taoKeyMaTran(long hang) {
        return (MA_KI_HIEU_KY_NGUYEN << 32) | hang;
    }

    // 1. Hàm gọi khi Admin khóa hoặc đổi quyền một User cụ thể.
    public static synchronized void danhDauViPham(Long userId) {
        long hang = userId / 64;            // Xác định hàng (Cứ 64 ID chung 1 hàng)
        int viTriBit = (int) (userId % 64); // Xác định vị trí bit cụ thể từ 0 đến 63
        long khoaKey = taoKeyMaTran(hang);

        Long giaTriHienTai = MA_TRAN_BIT.get(khoaKey);
        if (giaTriHienTai == null) {
            giaTriHienTai = 0L; // Nếu hàng này chưa có ai bị khóa thì khởi tạo bằng 0
        }

        // Dùng phép toán OR (|) kết hợp dịch bit (<<) để bật bit của User này lên thành 1.
        // Các bit của 63 người khác trong hàng này sẽ được giữ nguyên không đổi.
        long matNaBit = 1L << viTriBit;
        MA_TRAN_BIT.put(khoaKey, giaTriHienTai | matNaBit);
         
        System.out.println("[MaTranNhiPhanNguyenTu] Đã bật bit cảnh báo cho User ID: " + userId + " trong Kỷ nguyên hiện tại.");
    }

    // 2. Hàm kiểm tra xem User này có nằm trong danh sách "Nghi vấn" của RAM không.
    public static synchronized  boolean checkNghiVan(Long userId) {
        long hang = userId / 64;
        int viTriBit = (int) (userId % 64);
        long khoaKey = taoKeyMaTran(hang);

        Long giaTriHienTai = MA_TRAN_BIT.get(khoaKey);
        
        // Nếu hàng này không tồn tại trong RAM -> Chắc chắn User này sạch 100% trong Kỷ nguyên này.
        if (giaTriHienTai == null) {
            return false;
        }

        // Dùng phép toán AND (&) để trích xuất đúng giá trị bit tại vị trí của User.
        // Nếu kết quả khác 0 tức là bit đó đang là 1 -> Có biến động quyền hạn!
        long matNaBit = 1L << viTriBit;
        return (giaTriHienTai & matNaBit) != 0;
    }

    // 3. Hàm xóa dấu vết: Gọi sau khi User đã được check DB và cấp Token mới ("Chữa lành").
    public static synchronized void xoaDauVet(Long userId) {
        long hang = userId / 64;
        int viTriBit = (int) (userId % 64);
        long khoaKey = taoKeyMaTran(hang);

        Long giaTriHienTai = MA_TRAN_BIT.get(khoaKey);
        if (giaTriHienTai != null) {
            // Dùng phép AND (&) với đảo bit (~) để dập tắt bit của User này về 0.
            long matNaBit = 1L << viTriBit;
            long giaTriMoi = giaTriHienTai & ~matNaBit;

            // TỐI ƯU CỰC HẠN: Nếu cả hàng 64 người đều đã sạch (bằng 0), 
            // ta hủy luôn cái hàng này khỏi RAM để giải phóng bộ nhớ tuyệt đối.
            if (giaTriMoi == 0L) {
                MA_TRAN_BIT.remove(khoaKey);
            } else {
                MA_TRAN_BIT.put(khoaKey, giaTriMoi);
            }
        }
    }
}















