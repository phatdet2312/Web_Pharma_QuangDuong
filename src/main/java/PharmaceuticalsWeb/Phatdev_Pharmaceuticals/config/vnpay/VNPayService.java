//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/vnpay/VNPayService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Service
public class VNPayService {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.baseUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    // NGHIỆP VỤ 1: Đóng gói dữ liệu và tạo URL thanh toán
    public String taoDuongDanThanhToan(VNPayModels.ThanhToanRequest thongTinYeuCau, HttpServletRequest request) {
        VNPayLibrary vnPay = new VNPayLibrary();

        long soTienQuyDoi = (long) (thongTinYeuCau.getSoTien() * 100);

        vnPay.themDuLieuYeuCau("vnp_Version", "2.1.0");
        vnPay.themDuLieuYeuCau("vnp_Command", "pay");
        vnPay.themDuLieuYeuCau("vnp_TmnCode", vnp_TmnCode);
        vnPay.themDuLieuYeuCau("vnp_Amount", String.valueOf(soTienQuyDoi));
        vnPay.themDuLieuYeuCau("vnp_CurrCode", "VND");
        vnPay.themDuLieuYeuCau("vnp_TxnRef", String.valueOf(thongTinYeuCau.getHoaDonId()));
        vnPay.themDuLieuYeuCau("vnp_OrderInfo", thongTinYeuCau.getNoiDung());
        vnPay.themDuLieuYeuCau("vnp_OrderType", thongTinYeuCau.getLoaiDonHang());
        vnPay.themDuLieuYeuCau("vnp_Locale", "vn");

        // Lấy domain hiện tại (local hay server thực) để trỏ về trang HTML processing
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        vnPay.themDuLieuYeuCau("vnp_ReturnUrl", baseUrl + vnp_ReturnUrl);
        vnPay.themDuLieuYeuCau("vnp_IpAddr", thongTinYeuCau.getIpAddress());

        Calendar thoiGianHienTai = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat dinhDangThoiGian = new SimpleDateFormat("yyyyMMddHHmmss");
        vnPay.themDuLieuYeuCau("vnp_CreateDate", dinhDangThoiGian.format(thoiGianHienTai.getTime()));

        // Thiết lập hóa đơn chỉ sống 15 phút
        thoiGianHienTai.add(Calendar.MINUTE, 15);
        vnPay.themDuLieuYeuCau("vnp_ExpireDate", dinhDangThoiGian.format(thoiGianHienTai.getTime()));

        return vnPay.taoUrlThanhToan(vnp_PayUrl, vnp_HashSecret);
    }

    // NGHIỆP VỤ 2: Nhận Map JSON từ Web/App bắn lên, phân tích và trả về DTO kết quả
    public VNPayModels.ThanhToanResponse xuLyKetQuaThanhToan(Map<String, String> rawData) {
        VNPayLibrary vnPay = new VNPayLibrary();

        // Nhồi toàn bộ dữ liệu thô vào Library
        List<String> keys = new ArrayList<>(rawData.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = rawData.get(key);
            if (key != null && key.startsWith("vnp_")) {
                vnPay.themDuLieuPhanHoi(key, value);
            }
        }

        // Lấy ra các trường quan trọng
        String maHoaDon = vnPay.layGiaTriPhanHoi("vnp_TxnRef");
        String maGiaoDich = vnPay.layGiaTriPhanHoi("vnp_TransactionNo");
        String maPhanHoi = vnPay.layGiaTriPhanHoi("vnp_ResponseCode");
        String noiDung = vnPay.layGiaTriPhanHoi("vnp_OrderInfo");
        String maNganHang = vnPay.layGiaTriPhanHoi("vnp_BankCode");
        String maBaoMat = rawData.get("vnp_SecureHash");

        // Dịch lỗi ra Tiếng Việt và kiểm tra chữ ký
        String trangThaiDich = VNPayUtils.dichMaLoiVNPay(maPhanHoi);
        boolean chuKyHopLe = vnPay.kiemTraChuKyHopLe(maBaoMat, vnp_HashSecret);

        return VNPayModels.ThanhToanResponse.builder()
                .maHoaDon(maHoaDon)
                .maGiaoDich(maGiaoDich)
                .maPhanHoi(maPhanHoi)
                .noiDung(noiDung)
                .maNganHang(maNganHang)
                .trangThai(trangThaiDich)
                .thanhCong(chuKyHopLe && "00".equals(maPhanHoi))
                .build();
    }
}