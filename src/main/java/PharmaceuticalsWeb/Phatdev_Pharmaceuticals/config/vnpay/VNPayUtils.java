//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/vnpay/VNPayUtils.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VNPayUtils {

    // Thuật toán mã hóa chữ ký (Hiện tại VNPAY dùng HMAC-SHA512)
    // Nếu tương lai VNPAY đổi thuật toán, chỉ cần sửa duy nhất chỗ này
   public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                return "";
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (int i = 0; i < result.length; i++) {
                sb.append(String.format("%02x", result[i] & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Lấy IP thực của khách hàng (Đề phòng server chạy qua Nginx, proxy bị ẩn IP)
    public static String layDiaChiIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    // Từ điển dịch toàn bộ mã lỗi của VNPAY sang Tiếng Việt
    // Cực kỳ quan trọng để lưu Log và báo lỗi cho khách dễ hiểu
    public static String dichMaLoiVNPay(String maLoi) {
        if (maLoi == null) return "Không xác định được mã lỗi";

        switch (maLoi) {
            case "00": return "Giao dịch thành công.";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09": return "Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ Internet Banking tại ngân hàng.";
            case "10": return "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.";
            case "11": return "Đã hết hạn chờ thanh toán. Xin mời quý khách thực hiện lại giao dịch.";
            case "12": return "Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13": return "Khách hàng nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24": return "Khách hàng hủy giao dịch.";
            case "51": return "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65": return "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75": return "Ngân hàng thanh toán đang bảo trì.";
            case "79": return "Khách hàng nhập sai mật khẩu thanh toán quá số lần quy định. Xin mời quý khách thực hiện lại giao dịch.";
            case "99": return "Lỗi hệ thống từ VNPAY hoặc ngân hàng.";
            default: return "Lỗi không xác định. Mã lỗi: " + maLoi;
        }
    }
}