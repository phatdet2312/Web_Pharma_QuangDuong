//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/vnpay/VNPayModels.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.vnpay;

import java.util.Map;

import lombok.Builder;
import lombok.Data;


public class VNPayModels {

    // Chứa dữ liệu đầu vào để tạo URL thanh toán gửi sang VNPay
    @Data
    @Builder
    public static class ThanhToanRequest {
        private Long hoaDonId;
        private Double soTien;
        private String noiDung;
        private String loaiDonHang; // Ví dụ: billpayment, topup, book_order...
        private String ipAddress;
    }

    // Chứa kết quả đã được Service phân tích và dịch ra tiếng Việt
    @Data
    @Builder
    public static class ThanhToanResponse {
        private String maPhanHoi;   // vnp_ResponseCode (Mã lỗi/thành công)
        private String maGiaoDich;  // vnp_TransactionNo (Mã GD trên hệ thống VNPAY)
        private String maHoaDon;    // vnp_TxnRef (Mã hóa đơn của hệ thống chúng ta)
        private String noiDung;     // vnp_OrderInfo
        private String trangThai;   // Thông báo đã được dịch sang tiếng Việt để hiện cho user
        private String maNganHang;  // vnp_BankCode
        private boolean thanhCong;  // True nếu giao dịch hợp lệ và thành công
    }

    // Lớp DTO hứng dữ liệu thô từ Frontend (Web AJAX hoặc App Mobile) bắn xuống API
    // Dùng Map để hứng bất chấp tương lai VNPAY có thêm bớt tham số gì cũng không bị lỗi
    @Data
    public static class VNPayCallback {
        private Map<String, String> rawData;
    }
}