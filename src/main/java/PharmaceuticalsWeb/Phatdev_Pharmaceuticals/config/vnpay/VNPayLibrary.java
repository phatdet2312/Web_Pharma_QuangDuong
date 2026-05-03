//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/vnpay/VNPayLibrary.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.vnpay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VNPayLibrary {

    // Dữ liệu tạo yêu cầu thanh toán
    private final Map<String, String> duLieuYeuCau = new HashMap<>();
    // Dữ liệu kết quả VNPay trả về
    private final Map<String, String> duLieuPhanHoi = new HashMap<>();

    // Thêm tham số đầu vào để chuẩn bị tạo URL
    public void themDuLieuYeuCau(String key, String value) {
        if (value != null && !value.isEmpty()) {
            duLieuYeuCau.put(key, value);
        }
    }

    // Thêm tham số đầu ra (từ API Callback) để chuẩn bị kiểm tra chữ ký
    public void themDuLieuPhanHoi(String key, String value) {
        if (value != null && !value.isEmpty()) {
            duLieuPhanHoi.put(key, value);
        }
    }

    // Lấy một giá trị kết quả bất kỳ
    public String layGiaTriPhanHoi(String key) {
        if (duLieuPhanHoi.containsKey(key)) {
            return duLieuPhanHoi.get(key);
        }
        return "";
    }

    // Build URL thanh toán (Bắt buộc phải sắp xếp Key theo Alphabet)
    public String taoUrlThanhToan(String baseUrl, String hashSecret) {
        List<String> danhSachKhoa = new ArrayList<>(duLieuYeuCau.keySet());
        Collections.sort(danhSachKhoa); // Sắp xếp Alphabet theo chuẩn VNPay

        StringBuilder duLieuHash = new StringBuilder();
        StringBuilder queryHienThi = new StringBuilder();

        // Dùng vòng for cổ điển để xử lý dấu '&' chính xác ở phần tử cuối
        for (int i = 0; i < danhSachKhoa.size(); i++) {
            String khoa = danhSachKhoa.get(i);
            String giaTri = duLieuYeuCau.get(khoa);

            if (giaTri != null && !giaTri.isEmpty()) {
                String encodedGiaTri = URLEncoder.encode(giaTri, StandardCharsets.US_ASCII);

                duLieuHash.append(khoa).append('=').append(encodedGiaTri);
                queryHienThi.append(URLEncoder.encode(khoa, StandardCharsets.US_ASCII)).append('=').append(encodedGiaTri);

                // Thêm dấu '&' nếu chưa phải tham số cuối
                if (i < danhSachKhoa.size() - 1) {
                    queryHienThi.append('&');
                    duLieuHash.append('&');
                }
            }
        }

        String maBaoMat = VNPayUtils.hmacSHA512(hashSecret, duLieuHash.toString());
        return baseUrl + "?" + queryHienThi.toString() + "&vnp_SecureHash=" + maBaoMat;
    }

    // Kiểm tra tính toàn vẹn của dữ liệu trả về (Chống giả mạo)
    public boolean kiemTraChuKyHopLe(String inputHash, String hashSecret) {
        List<String> danhSachKhoa = new ArrayList<>(duLieuPhanHoi.keySet());
        Collections.sort(danhSachKhoa);

        StringBuilder duLieuHash = new StringBuilder();

        for (int i = 0; i < danhSachKhoa.size(); i++) {
            String khoa = danhSachKhoa.get(i);
            String giaTri = duLieuPhanHoi.get(khoa);

            // Phải bỏ qua 2 biến này trước khi băm lại chuỗi
            if (khoa.equals("vnp_SecureHashType") || khoa.equals("vnp_SecureHash")) {
                continue;
            }

            if (giaTri != null && !giaTri.isEmpty()) {
                duLieuHash.append(khoa).append('=').append(URLEncoder.encode(giaTri, StandardCharsets.US_ASCII));
                if (i < danhSachKhoa.size() - 1) {
                    duLieuHash.append('&');
                }
            }
        }

        // Fix lỗi lỡ dư dấu '&' ở cuối cùng do lệnh continue bên trên
        String chuoiCanHash = duLieuHash.toString();
        if (chuoiCanHash.endsWith("&")) {
            chuoiCanHash = chuoiCanHash.substring(0, chuoiCanHash.length() - 1);
        }

        String chuKyCuaToi = VNPayUtils.hmacSHA512(hashSecret, chuoiCanHash);
        return chuKyCuaToi.equalsIgnoreCase(inputHash);
    }
}