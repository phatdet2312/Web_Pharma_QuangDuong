//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/FormDataHashCheckerInterceptor.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

@Component
public class FormDataHashCheckerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String phuongThuc = request.getMethod();

        // Bước 1: Chỉ lọc các request có khả năng thay đổi dữ liệu
        if (!phuongThuc.equalsIgnoreCase("POST") &&
                !phuongThuc.equalsIgnoreCase("PUT") &&
                !phuongThuc.equalsIgnoreCase("PATCH") &&
                !phuongThuc.equalsIgnoreCase("DELETE")) {
            return true;
        }

        String contentType = request.getContentType();
        // Khởi tạo mặc định là false
        boolean isMultipart = false;
        boolean isFormUrlEncoded = false;

        // Chỉ kiểm tra khi contentType không null
        if (contentType != null) {
            String lowerCaseContentType = contentType.toLowerCase();

            if (lowerCaseContentType.startsWith("multipart/form-data")) {
                isMultipart = true;
            } else if (lowerCaseContentType.startsWith("application/x-www-form-urlencoded")) {
                isFormUrlEncoded = true;
            }
        }

        // Bước 2: Kiểm tra xem có phải request gửi file (Multipart) không
        if (isMultipart != true && isFormUrlEncoded != true) {
            System.out.println(
                    "[FormDataHashCheckerInterceptor] Cảnh báo: Request không phải Multipart bỏ qua cho bodyIntegrityFilter xử lý.");
            return true; // Nếu là JSON thuần thì để BodyHashCheckerAdvice xử lý
        }

        // Bước 3: Lấy mã băm kỳ vọng từ Client gửi lên qua Header
        // BỎ QUA NẾU LÀ API CÔNG KHAI
        String maBamKyVongTuFilter = (String) request.getAttribute("EXPECTED_BODY_HASH");
        if ("API_CONG_KHAI".equals(maBamKyVongTuFilter) == true) {
            return true; // Cho qua toàn bộ
        }

        String maBamKhachGui = request.getHeader("X-Body-Hash");
        if (maBamKhachGui == null || maBamKhachGui.isEmpty()) {
            System.out
                    .println("[FormDataHashCheckerInterceptor] Cảnh báo: Request Multipart thiếu Header X-Body-Hash.");
            return true; // Để DynamicRoleFilter xử lý việc từ chối dựa trên chữ ký tổng
        }

        // Dùng hàm Helper để bóc tách URL một cách an toàn và gọn gàng
        List<String> danhSachKeyCuaURL = new ArrayList<>();
        try {
            danhSachKeyCuaURL = layDanhSachKeyTuUrl(request.getQueryString());
        } catch (IllegalArgumentException e) {
            //Chặn ngay lập tức nếu Hacker gửi mã Hex lỗi trên URL
            System.err.println("[FormDataHashCheckerInterceptor] TẤN CÔNG BỊ CHẶN: Mã URL-Decode không hợp lệ.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu URL chứa ký tự không hợp lệ!");
            return false;
        }
        // ==========================================================
        // XỬ LÝ TRƯỜNG HỢP KHÔNG CÓ BODY
        // ==========================================================
        if ("EMPTY_BODY".equals(maBamKhachGui)) {
            boolean coGiauThamSo = false;
            Map<String, String[]> mapThamSo = request.getParameterMap();
            List<String> danhSachKeyThamSo = new ArrayList<>(mapThamSo.keySet());

            for (int i = 0; i < danhSachKeyThamSo.size(); i++) {
                String tenKeyRaw = danhSachKeyThamSo.get(i);
                String tenKey = Normalizer.normalize(tenKeyRaw, Normalizer.Form.NFC);
                int soLanTrenUrl = 0;
                for (int q = 0; q < danhSachKeyCuaURL.size(); q++) {
                    if (danhSachKeyCuaURL.get(q).equals(tenKey)) {
                        soLanTrenUrl++;
                    }
                }
                String[] mangGiaTri = mapThamSo.get(tenKeyRaw);
                if (mangGiaTri.length > soLanTrenUrl) {
                    coGiauThamSo = true;
                    break;
                }
            }

            boolean coGiauFile = false;
            if (isMultipart == true) {
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                coGiauFile = !multiRequest.getFileMap().isEmpty();
            }

            if (coGiauThamSo == true || coGiauFile == true) {
                System.err.println(
                        "[FormDataHashCheckerInterceptor] NGUY HIỂM: Hacker nhồi dữ liệu Form vào Request rỗng!");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Dữ liệu rác được nhồi vào request rỗng!");
                return false;
            }

            System.out.println("[FormDataHashCheckerInterceptor] Phát hiện EMPTY_BODY hợp lệ.");
            request.setAttribute("FORMDATA_CHECKED", true);
            return true;
        }

        System.out.println(
                "[FormDataHashCheckerInterceptor] Bắt đầu quy trình kiểm tra DNA cho: " + request.getRequestURI());

        // if (request instanceof MultipartHttpServletRequest) {
        // Bước 4: Dùng TreeMap để lưu dữ liệu logic (Tự động sắp xếp key theo A-Z)
        // Việc sắp xếp này là bắt buộc để mã băm Client và Server trùng nhau
        Map<String, String> danhSachDuLieuLogic = new TreeMap<>();

        // 4.1: Duyệt và trích xuất các tham số dạng chữ (Text Parameters)
        Map<String, String[]> mapThamSo = request.getParameterMap();
        List<String> danhSachKeyThamSo = new ArrayList<>(mapThamSo.keySet());
        for (int i = 0; i < danhSachKeyThamSo.size(); i++) {
            String tenKeyRaw = danhSachKeyThamSo.get(i);
            String tenKey = Normalizer.normalize(tenKeyRaw, Normalizer.Form.NFC);
            /// Đếm xem Key này xuất hiện bao nhiêu lần trên URL
            int soLanTrenUrl = 0;
            for (int q = 0; q < danhSachKeyCuaURL.size(); q++) {
                if (danhSachKeyCuaURL.get(q).equals(tenKey)) {
                    soLanTrenUrl++;
                }
            }

            String[] mangGiaTri = mapThamSo.get(tenKeyRaw);

            // Trích xuất chính xác tham số thuộc Body, bỏ qua tham số URL
            // Tomcat gộp chung URL và Body. Tham số URL luôn nằm đầu mảng mangGiaTri.
            // Do đó ta chỉ duyệt từ vị trí thứ 'soLanTrenUrl' trở đi.
            int chiMucThucTeCuaBody = 0; // Index cho BodyHash

            for (int v = soLanTrenUrl; v < mangGiaTri.length; v++) {
                String giaTriThanhPhan = mangGiaTri[v];
                String giaTriChuan = Normalizer.normalize(giaTriThanhPhan, Normalizer.Form.NFC);
                String keyBam;

                if (isMultipart) {
                    keyBam = "T:" + tenKey + "[" + chiMucThucTeCuaBody + "]";
                } else {
                    keyBam = tenKey + "[" + chiMucThucTeCuaBody + "]";
                }
                danhSachDuLieuLogic.put(keyBam, giaTriChuan);
                chiMucThucTeCuaBody++;
                System.out.println(
                        "[FormDataHashCheckerInterceptor] Đã nhận tham số chữ: " + keyBam + " = "
                                + giaTriChuan);
            }

        }

        if (isMultipart == true) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            // 4.2: Duyệt và trích xuất dấu vân tay DNA của các File
            MultiValueMap<String, MultipartFile> mapMultiFile = multiRequest.getMultiFileMap();
            List<String> danhSachKeyFile = new ArrayList<>(mapMultiFile.keySet());
            System.out
                    .println("[FormDataHashCheckerInterceptor] Duyệt và trích xuất dấu vân tay DNA của các File");
            for (int j = 0; j < danhSachKeyFile.size(); j++) {
                String tenKeyFileRaw = danhSachKeyFile.get(j);
                String tenKeyFile = Normalizer.normalize(tenKeyFileRaw, Normalizer.Form.NFC);

                List<MultipartFile> danhSachFile = mapMultiFile.get(tenKeyFileRaw);

                for (int v = 0; v < danhSachFile.size(); v++) {
                    MultipartFile fileGoc = danhSachFile.get(v);
                    String keyBam = "F:" + tenKeyFile + "[" + v + "]";

                    if (fileGoc != null && !fileGoc.isEmpty()) {
                        // Trích xuất DNA: Tên + Size + Type + 1KB đầu + 1KB cuối
                        String maDNA = trichXuatDNAFile(fileGoc, 1, 1);
                        danhSachDuLieuLogic.put(keyBam, maDNA);
                        System.out.println(
                                "[FormDataHashCheckerInterceptor] Đã trích xuất DNA file: "
                                        + fileGoc.getOriginalFilename());
                    } else {
                        // Trả nguyên chuỗi String, không băm SHA-256
                        danhSachDuLieuLogic.put(keyBam, "empty_file");
                    }
                }
            }
        }

        // Bước 5: Nối chuỗi theo chuẩn URL Encode để chống tấn công Parameter Injection
        // Công thức: k1=v1&k2=v2&...
        StringBuilder chuoiDeBam = new StringBuilder();
        List<String> danhSachKeyDaSapXep = new ArrayList<>(danhSachDuLieuLogic.keySet());
        for (int k = 0; k < danhSachKeyDaSapXep.size(); k++) {
            String key = danhSachKeyDaSapXep.get(k);
            String value = danhSachDuLieuLogic.get(key);

            // Công thức: Chiều_dài_Key:Key|Chiều_dài_Value:Value|
            chuoiDeBam.append(key.length()).append(":")
                    .append(key).append("|")
                    .append(value.length()).append(":")
                    .append(value).append("|");

        }
        System.out.println("[FormDataHashCheckerInterceptor] Nối chuỗi hoàn thành");

        // Bước 6: Băm chuỗi dữ liệu logic vừa tạo
        String maBamServer = tinhToanSHA256(chuoiDeBam.toString());
        byte[] serverHashBytes = maBamServer.toLowerCase().getBytes(StandardCharsets.UTF_8);
        byte[] clientHashBytes = maBamKhachGui.toLowerCase().getBytes(StandardCharsets.UTF_8);

        // Bước 7: Đối soát cuối cùng (Sự thật tàn nhẫn)
        if (!MessageDigest.isEqual(serverHashBytes, clientHashBytes)) {
            System.err.println("[FormDataHashCheckerInterceptor] THẤT BẠI: Mã băm không khớp!");
            System.err.println("   >> Client gửi: " + clientHashBytes);
            System.err.println("   >> Server băm: " + serverHashBytes);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Can thiệp dữ liệu trái phép!");
            return false;
        }

        request.setAttribute("EXPECTED_BODY_HASH", maBamServer);
        System.err.println("   >> Client gửi: " + clientHashBytes);
        System.err.println("   >> Server băm: " + serverHashBytes);
        System.out
                .println(
                        "[FormDataHashCheckerInterceptor] Xác thực DNA File & Multipart: OK (Bảo mật cấp quân đội)");

        request.setAttribute("FORMDATA_CHECKED", true);
        return true;
        // }
        // return true;
    }

    // =========================================================================
    // HÀM BÓC TÁCH URL AN TOÀN (VÁ LỖI HIỆP 1 & 2)
    // =========================================================================
    private List<String> layDanhSachKeyTuUrl(String chuoiTruyVanURL) throws Exception {
        List<String> danhSachKey = new ArrayList<>();
        if (chuoiTruyVanURL == null || chuoiTruyVanURL.isEmpty()) {
            return danhSachKey;
        }

        String[] mangCapGiaTri = chuoiTruyVanURL.split("&");
        for (int p = 0; p < mangCapGiaTri.length; p++) {
            String cap = mangCapGiaTri[p];
            if (cap.isEmpty()) {
                continue; // Lọc rác "&&"
            }

            int viTriDauBang = cap.indexOf("=");
            String keyGocChuaNormalize;
            try {
                // >= 0 để bắt luôn trường hợp Key rỗng (VD: "=1")
                if (viTriDauBang >= 0) {
                    String keyGoc = cap.substring(0, viTriDauBang);
                    keyGocChuaNormalize = URLDecoder.decode(keyGoc, StandardCharsets.UTF_8.toString());
                } else {
                    keyGocChuaNormalize = URLDecoder.decode(cap, StandardCharsets.UTF_8.toString());
                }
            } catch (Exception e) {
                // Ném ngoại lệ để hàm preHandle bắt và trả về mã lỗi 400
                throw new IllegalArgumentException("Chuỗi mã hóa URL bị sai định dạng", e);
            }
            String keyGocNFC = Normalizer.normalize(keyGocChuaNormalize, Normalizer.Form.NFC);
            danhSachKey.add(keyGocNFC);
        }
        return danhSachKey;
    }

    // =========================================================================
    // CÔNG CỤ TRÍCH XUẤT DNA FILE (THUẬT TOÁN CỬA SỔ TRƯỢT - SLIDING WINDOW)
    // Đạt hiệu suất cao nhất: Không dùng skip(), Không lặp từng byte, RAM O(1)
    // =========================================================================
    private String trichXuatDNAFile(MultipartFile file, int kbDau, int kbCuoi) throws Exception {
        long tongKichThuoc = file.getSize();
        // VÁ LỖI HIỆP 1: Chống sát thủ 0 Byte (Đồng bộ với JS)
        if (tongKichThuoc == 0) {
            return "empty_file";
        }

        String tenFileRaw = file.getOriginalFilename();
        String loaiFileRaw = file.getContentType();
        if (tenFileRaw == null || tenFileRaw.trim().isEmpty()) {
            tenFileRaw = "unknown_filename";
        }
        if (loaiFileRaw == null || loaiFileRaw.trim().isEmpty()) {
            loaiFileRaw = "application/octet-stream";
        }

        String tenFile = Normalizer.normalize(tenFileRaw, Normalizer.Form.NFC);
        String loaiFile = Normalizer.normalize(loaiFileRaw, Normalizer.Form.NFC);
        int soByteDau = kbDau * 1024;
        int soByteCuoi = kbCuoi * 1024;

        // Xử lý trường hợp file nhỏ hơn dung lượng cần lấy
        int thucTeDau = (int) Math.min(soByteDau, tongKichThuoc);
        int thucTeCuoi = (int) Math.min(soByteCuoi, tongKichThuoc);

        byte[] mangByteDau = new byte[thucTeDau];
        byte[] mangByteCuoi = new byte[thucTeCuoi];

        // Dùng một "Gầu múc" 8KB để múc nước ào ạt, không lặp từng byte
        byte[] gauMuc = new byte[8192];

        try (InputStream is = file.getInputStream()) {
            int soByteMucDuoc;
            int tongSoByteDaChayQua = 0;

            // Vòng lặp múc nước: Chạy cho đến khi giọt nước cuối cùng rơi xuống
            while ((soByteMucDuoc = is.read(gauMuc)) != -1) {

                // ----------------------------------------------------
                // 1. NHIỆM VỤ HỨNG 1KB ĐẦU TIÊN
                // ----------------------------------------------------
                if (tongSoByteDaChayQua < thucTeDau) {
                    int soByteConThieu = thucTeDau - tongSoByteDaChayQua;
                    int soByteCanLay = Math.min(soByteMucDuoc, soByteConThieu);

                    // Hàm copy native C++ siêu tốc của Java
                    System.arraycopy(gauMuc, 0, mangByteDau, tongSoByteDaChayQua, soByteCanLay);
                }

                // ----------------------------------------------------
                // 2. NHIỆM VỤ HỨNG 1KB CUỐI CÙNG (CỬA SỔ TRƯỢT)
                // ----------------------------------------------------
                if (soByteMucDuoc >= thucTeCuoi) {
                    // Nếu gầu múc lần này bự hơn 1KB, ta vứt hết đồ cũ,
                    // chỉ lấy đúng 1KB phần đuôi của cái gầu này thôi.
                    int viTriBatDauLay = soByteMucDuoc - thucTeCuoi;
                    System.arraycopy(gauMuc, viTriBatDauLay, mangByteCuoi, 0, thucTeCuoi);
                } else {
                    // Nếu gầu múc ít hơn 1KB, ta phải đùn dữ liệu cũ sang trái
                    // và nhét dữ liệu mới vào bên phải khay chứa.

                    // Bước 2.1: Dịch mảng cũ sang trái để lấy chỗ trống
                    int soByteGiuLai = thucTeCuoi - soByteMucDuoc;
                    System.arraycopy(mangByteCuoi, soByteMucDuoc, mangByteCuoi, 0, soByteGiuLai);

                    // Bước 2.2: Nhét dữ liệu mới múc được vào chỗ trống bên phải
                    System.arraycopy(gauMuc, 0, mangByteCuoi, soByteGiuLai, soByteMucDuoc);
                }

                // Cập nhật công tơ mét
                tongSoByteDaChayQua += soByteMucDuoc;
            }
        }

        // Chuyển mảng byte sang chuỗi Base64
        String dauB64 = Base64.getEncoder().encodeToString(mangByteDau);
        String cuoiB64 = Base64.getEncoder().encodeToString(mangByteCuoi);

        // Nối theo công thức DNA thống nhất với Client
        StringBuilder chuoiDNA = new StringBuilder();
        chuoiDNA.append(tenFile.length()).append(":").append(tenFile).append("|")
                .append(String.valueOf(tongKichThuoc).length()).append(":").append(tongKichThuoc).append("|")
                .append(loaiFile.length()).append(":").append(loaiFile).append("|")
                .append(dauB64.length()).append(":").append(dauB64).append("|")
                .append(cuoiB64.length()).append(":").append(cuoiB64).append("|");

        return tinhToanSHA256(chuoiDNA.toString());
    }

    private String tinhToanSHA256(String duLieu) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(duLieu.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
