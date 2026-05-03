//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/BodyIntegrityFilter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * BODY INTEGRITY FILTER - PHIÊN BẢN TỐI THƯỢNG TÍCH HỢP (KÈM LOG DEBUG CHI
 * TIẾT)
 * ===========================================================
 * HIỆU NĂNG:
 * - CPU: Không tạo SHA-256 khi EMPTY_BODY. Kiểm tra Trojan bằng 1 phép toán
 * bitwise (& 0xFF) > 32.
 * - RAM: O(1) tuyệt đối - băm on-the-fly, không sao chép mảng byte toàn bộ
 * body.
 * - Tốc độ: Throw sớm nhất có thể khi phát hiện Trojan hoặc DoS (không đọc hết
 * body rác).
 * - DoS Protection: Giới hạn ép đọc 10KB, giới hạn khoảng trắng 1KB cho
 * EMPTY_BODY.
 *
 * BẢO MẬT (CHỐNG TẤT CẢ CÁC LOẠI TẤN CÔNG):
 * - Chống Trojan Horse: Khai báo EMPTY_BODY nhưng nhét mã độc -> phát hiện ngay
 * tức thì.
 * - Chống Whitespace DoS: Giới hạn tổng khoảng trắng <= 1KB.
 * - Chống Infinite Stream DoS: Giới hạn ép đọc <= 10KB.
 * - Chống Lazy Reader Attack: Ép đọc cạn sau khi Controller chạy xong.
 * - Chống Timing Attack: So sánh hash bằng MessageDigest.isEqual().
 * - Chống Content-Length Fake: Kiểm tra mâu thuẫn header sớm trước khi mở
 * stream.
 * - Chống Byte Sign Abuse: Ép số dương chuẩn bằng (giaTriByte & 0xFF) trước khi
 * so sánh ASCII.
 * - Chống Bypass HMAC: Lấy hash từ attribute EXPECTED_BODY_HASH (đã được
 * DynamicRoleFilter xác thực chữ ký HMAC).
 *
 * TIÊU CHUẨN CODE:
 * - Tên biến/tên lớp/tên phương thức: Tường minh 100%, tiếng Việt rõ nghĩa,
 * không viết tắt.
 * - Mọi khối if/else đều có ngoặc nhọn đầy đủ {} (dễ đọc, dễ debug).
 * - Logic một chiều: Luôn dùng == true / == false, tránh toán tử 3 ngôi.
 * - Vòng lặp for truyền thống với biến khai báo rõ ràng.
 * - Comment chi tiết từng bước, từng lớp, từng logic quan trọng.
 * - Xuống dòng hợp lý để code không dính trên một hàng.
 *
 * LUỒNG HOẠT ĐỘNG TỔNG QUÁT:
 * 1. Kiểm tra method (chỉ POST/PUT/PATCH/DELETE).
 * 2. Bỏ qua multipart/form-data hoặc x-www-form-urlencoded.
 * 3. Kiểm tra attribute FORMDATA_CHECKED (để tránh trùng lặp với Interceptor).
 * 4. Lấy EXPECTED_BODY_HASH từ attribute (an toàn hơn header).
 * 5. Nếu EMPTY_BODY: Kiểm tra Content-Length > 0 -> chặn sớm.
 * 6. Bọc Request bằng Wrapper -> quyết định luồng đọc (EMPTY hay HASH).
 * 7. Chuyển tiếp cho Controller.
 * 8. Sau Controller: Ép đọc cạn nếu chưa xác thực (chống Lazy Reader).
 * 9. Throw nếu hash không khớp hoặc phát hiện rác/Trojan.
 */

@Component
public class BodyIntegrityFilter extends OncePerRequestFilter {

    // Cấu hình các giới hạn an toàn để chống tấn công từ chối dịch vụ (DoS)
    private final int GIOI_HAN_JSON_FORM_BYTES;
    private final int GIOI_HAN_TAI_TRONG_LON_BYTES;
    private final SecurityLibraryProperties thietLapLoi;
    
    // Các thông số kỹ thuật lõi tuyệt đối cấm Dev ngoài cấu hình
    private static final int GIOI_HAN_KHOANG_TRANG_TOI_DA = 1024; // 1 KB
    private static final int KICH_THUOC_BO_DEM_MANG = 4096; // 4 KB buffer đọc
    private static final int MOC_ASCII_KHOANG_TRANG_HOP_LE = 32; // Chỉ cho phép <= 32 (space, tab, LF, CR)

    // Khởi tạo lấy dữ liệu từ Config Library
    public BodyIntegrityFilter(SecurityLibraryProperties props) {
        // Ta dùng chung biến dung lượng JSON vì nếu quá 10KB thì bản chất nó đã là một Request quá khổ.
        this.GIOI_HAN_JSON_FORM_BYTES = (int) (props.getLimitJsonFormKb() * 1024L);
        this.GIOI_HAN_TAI_TRONG_LON_BYTES = (int) (props.getLimitLargePayloadMb() * 1024L * 1024L);
        this.thietLapLoi = props;
    }

    // TẠO LỚP EXCEPTION CHUYÊN BIỆT ĐỂ KHÔNG NUỐT LỖI CỦA SPRING
    public static class LoiBaoMatIntegrity extends RuntimeException {
        public LoiBaoMatIntegrity(String thongDiep) {
            super(thongDiep);
        }

        public LoiBaoMatIntegrity(String thongDiep, Throwable nguyenNhan) {
            super(thongDiep, nguyenNhan);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String phuongThuc = request.getMethod();
        String loaiNoiDung = request.getContentType();
        String uri = request.getRequestURI();

        // =========================================================
        // BƯỚC 1: CHỈ KIỂM TRA NHỮNG PHƯƠNG THỨC CÓ THAY ĐỔI DỮ LIỆU
        // (POST, PUT, PATCH, DELETE) - Các method GET/HEAD/OPTIONS không cần hash body
        // =========================================================
        boolean laPhuongThucCanKiemTra = false;

        if (phuongThuc.equalsIgnoreCase("POST") == true) {
            laPhuongThucCanKiemTra = true;
        } else if (phuongThuc.equalsIgnoreCase("PUT") == true) {
            laPhuongThucCanKiemTra = true;
        } else if (phuongThuc.equalsIgnoreCase("PATCH") == true) {
            laPhuongThucCanKiemTra = true;
        } else if (phuongThuc.equalsIgnoreCase("DELETE") == true) {
            laPhuongThucCanKiemTra = true;
        }

        if (laPhuongThucCanKiemTra == false) {
            // Không phải method thay đổi dữ liệu -> cho qua ngay, không tốn tài nguyên
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================
        // BƯỚC 2: CHỐNG KIỂM TRA TRÙNG LẶP NẾU FILTER/INTERCEPTOR TRƯỚC ĐÃ XỬ LÝ
        // =========================================================
        Boolean daKiemTraXongRoi = (Boolean) request.getAttribute("FORMDATA_CHECKED");
        // Lấy mã băm mà DynamicRoleFilter đã chứng thực
        String maBamKyVong = (String) request.getAttribute("EXPECTED_BODY_HASH");

        if ("API_CONG_KHAI".equals(maBamKyVong) == true) {
            System.out.println("[BodyIntegrityFilter] đây là API công khai. Bỏ qua");
            filterChain.doFilter(request, response);
            return;
        }

        if (Boolean.TRUE.equals(daKiemTraXongRoi) == true) {
            System.out.println("[BodyIntegrityFilter] Dữ liệu đã được kiểm tra trước đó. Bỏ qua.");
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================
        // BƯỚC 3: LẤY CHỮ KÝ BẢO MẬT TỪ ATTRIBUTE NỘI BỘ
        // (An toàn tuyệt đối, không thể spoof từ header bên ngoài)
        // =========================================================
        if (maBamKyVong == null) {
            System.err.println("[BodyIntegrityFilter] NGUY HIỂM: Hệ thống chưa cung cấp chữ ký bảo mật nội bộ.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Hệ thống từ chối: Thiếu dấu vân tay bảo mật.");
            return;
        }

        maBamKyVong = maBamKyVong.trim();
        if (maBamKyVong.isEmpty() == true) {
            System.err.println("[BodyIntegrityFilter] NGUY HIỂM: Chữ ký bảo mật nội bộ bị rỗng.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Hệ thống từ chối: Dấu vân tay bảo mật vô giá trị.");
            return;
        }

        System.out.println("[BodyIntegrityFilter] Khởi tạo chốt chặn bảo mật cho URI: " + uri + " ["
                + phuongThuc + "]");

        // =========================================================
        // BƯỚC 4: LÁ CHẮN SỚM - CHỐNG MÂU THUẪN CONTENT-LENGTH VÀ CHỮ KÝ RỖNG
        // Bất chấp Content-Type là gì, nếu khai báo rỗng thì phải bọc luồng bắt Trojan!
        // (Phát hiện sớm trước khi mở stream, tiết kiệm CPU)
        // =========================================================
        boolean laYeuCauRong = false;
        if (maBamKyVong.equals("EMPTY_BODY") == true) {
            laYeuCauRong = true;

            int chieuDaiNoiDung = request.getContentLength();
            if (chieuDaiNoiDung > 0) {
                System.err.println(
                        "[BodyIntegrityFilter] CHẶN: Khai báo rỗng nhưng cố tình gửi " + chieuDaiNoiDung + " bytes.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Hệ thống chặn: Mâu thuẫn giữa khai báo rỗng và dữ liệu thực gửi.");
                return;
            }
            // Không kiểm tra Content-Type ở đây.
            // Dù là multipart, text/plain hay bất cứ gì, nếu khai báo EMPTY -> phải bọc
            // Wrapper.
            System.out.println(
                    "[BodyIntegrityFilter] EMPTY_BODY hợp lệ. Bọc Wrapper chống Trojan (bỏ qua Content-Type check).");
        }

        // =========================================================
        // BƯỚC 5: NHƯỜNG QUYỀN CHO INTERCEPTOR ĐỐI VỚI DỮ LIỆU FORM
        // NẾU KHÔNG RỖNG MÀ LÀ DỮ LIỆU FORM -> MỚI NHƯỜNG QUYỀN
        // Bước này chỉ chạy khi laYeuCauRong == false (maBamKyVong là hash thực).
        // Lúc này, nếu là multipart -> nhường Interceptor xử lý hash của form.
        // (multipart/form-data hoặc application/x-www-form-urlencoded)
        // =========================================================
        if (laYeuCauRong == false) {
            if (loaiNoiDung != null) {
                String loaiNoiDungChuThuong = loaiNoiDung.toLowerCase();
                boolean laDinhDangForm = false;

                if (loaiNoiDungChuThuong.startsWith("multipart/form-data") == true) {
                    laDinhDangForm = true;
                } else if (loaiNoiDungChuThuong.startsWith("application/x-www-form-urlencoded") == true) {
                    laDinhDangForm = true;
                }

                if (laDinhDangForm == true) {
                    System.out.println(
                            "[BodyIntegrityFilter] Phát hiện định dạng Form/Multipart. Nhường quyền cho Interceptor.");
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }

        RequestBaoMatWrapper requestBaoMat = null;

        try {
            // CẤP QUYỀN ÉP ĐỌC THEO MỨC ĐỘ CỦA URI
            int gioiHanEpDocHienTai = GIOI_HAN_JSON_FORM_BYTES;
            if (thietLapLoi.kiemTraLaUrlTaiTrongLon(uri) == true) {
                gioiHanEpDocHienTai = GIOI_HAN_TAI_TRONG_LON_BYTES;
            }

            // =========================================================
            // BƯỚC 6: BỌC LẠI REQUEST BẰNG LỚP GIÁP BẢO VỆ STREAM
            // (Factory pattern: Quyết định luồng đọc dựa trên EMPTY hay HASH)
            // =========================================================
            System.out.println("[BodyIntegrityFilter] Đang bọc request bằng RequestBaoMatWrapper...");
            requestBaoMat = new RequestBaoMatWrapper(request, maBamKyVong, laYeuCauRong, gioiHanEpDocHienTai);
        } catch (Exception loiKhoiTao) {
            System.err.println("[BodyIntegrityFilter] Lỗi khởi tạo luồng bảo mật: " + loiKhoiTao.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ.");
            return;

        }

        try {
            // Chuyển luồng xử lý tiếp theo cho Controller
            filterChain.doFilter(requestBaoMat, response);

            // =========================================================
            // BƯỚC 7: KIỂM TRA CHÉO SAU CONTROLLER (CHỐNG LAZY READER ATTACK)
            // =========================================================
            if (requestBaoMat.kiemTraDaXacThucChua() == false) {
                System.err.println(
                        "[BodyIntegrityFilter] CẢNH BÁO: Controller không đọc cạn luồng dữ liệu (Lazy Reader).");
                System.err.println("[BodyIntegrityFilter] Kích hoạt ép vét dữ liệu để đối soát mã băm...");

                requestBaoMat.epDocVetChoCanLuong();

                if (requestBaoMat.kiemTraDaXacThucChua() == false) {
                    System.err.println("[BodyIntegrityFilter] LỖI NGHIÊM TRỌNG: Mã băm không khớp sau khi ép đọc cạn.");
                    throw new LoiBaoMatIntegrity("Dữ liệu Body không toàn vẹn. Lỗi mã băm SHA-256 không khớp.");
                }
                System.out.println("[BodyIntegrityFilter] Ép vét thành công. Dữ liệu hợp lệ.");
            }

        } catch (LoiBaoMatIntegrity loiHauKiem) {
            System.err.println("[BodyIntegrityFilter] ĐÃ NGĂN CHẶN TẤN CÔNG: " + loiHauKiem.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Truy cập bị chặn: Phát hiện bất thường trong dữ liệu.");
        }
    }

    // =================================================================================
    // LỚP NỘI BỘ 1: BỌC REQUEST (HttpServletRequestWrapper) - Lớp trung gian bảo vệ
    // =================================================================================
    private static class RequestBaoMatWrapper extends HttpServletRequestWrapper {

        private final String maBamKyVong;
        private final boolean laYeuCauRong;
        private final int GIOI_HAN_EP_DOC;

        private LuongDocBaoMatGoc luongDocBaoMat = null;
        private BufferedReader boDocKyTu = null;

        public RequestBaoMatWrapper(HttpServletRequest request, String maBamKyVong, boolean laYeuCauRong, int gioiHanEpDoc) {
            super(request);
            this.maBamKyVong = maBamKyVong;
            this.laYeuCauRong = laYeuCauRong;
            this.GIOI_HAN_EP_DOC = gioiHanEpDoc;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (boDocKyTu != null) {
                throw new IllegalStateException("Lỗi: Hàm getReader() đã được gọi trước đó.");
            }
            if (luongDocBaoMat == null) {
                luongDocBaoMat = taoLuongDocChuyenBiet();
            }
            return luongDocBaoMat;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (luongDocBaoMat != null) {
                throw new IllegalStateException("Lỗi: Hàm getInputStream() đã được gọi trước đó.");
            }
            if (boDocKyTu == null) {
                luongDocBaoMat = taoLuongDocChuyenBiet();
                boDocKyTu = new BufferedReader(new InputStreamReader(luongDocBaoMat, StandardCharsets.UTF_8));
            }
            return boDocKyTu;
        }

        // Factory method: Quyết định loại luồng đọc dựa trên laYeuCauRong
        private LuongDocBaoMatGoc taoLuongDocChuyenBiet() throws IOException {
            if (laYeuCauRong == true) {
                System.out.println("[BodyIntegrityFilter] Tạo luồng kiểm duyệt rỗng (LuongKiemDuyetRong).");
                return new LuongKiemDuyetRong(super.getInputStream());
            } else {
                System.out.println("[BodyIntegrityFilter] Tạo luồng băm SHA-256 (LuongBamHash).");
                return new LuongBamHash(super.getInputStream(), maBamKyVong);
            }
        }

        public boolean kiemTraDaXacThucChua() {
            if (luongDocBaoMat != null) {
                return luongDocBaoMat.kiemTraDaXacThucChua();
            }
            if (laYeuCauRong == true) {
                return true;
            } else {
                return false;
            }
        }

        public void epDocVetChoCanLuong() throws IOException {
            if (luongDocBaoMat == null) {
                this.getInputStream(); // Mở luồng nếu chưa mở
            }

            if (luongDocBaoMat.kiemTraDaXacThucChua() == false) {
                byte[] boDem = new byte[KICH_THUOC_BO_DEM_MANG];
                int soByteVuaDoc = 0;
                int tongSoByteDaEpDoc = 0;

                while (true) {
                    soByteVuaDoc = luongDocBaoMat.read(boDem);

                    if (soByteVuaDoc == -1) {
                        break;
                    }

                    tongSoByteDaEpDoc = tongSoByteDaEpDoc + soByteVuaDoc;

                    if (tongSoByteDaEpDoc > GIOI_HAN_EP_DOC) {
                        System.err.println("[BodyIntegrityFilter] LỖI BẢO MẬT: Phát hiện luồng rác vô tận (DoS).");
                        throw new LoiBaoMatIntegrity("Tấn công DoS: Luồng rác vượt quá giới hạn an toàn.");
                    }
                }
            }
        }
    }

    // =================================================================================
    // LỚP NỘI BỘ 2: ỐNG NƯỚC GỐC TRỪU TƯỢNG (Kế thừa ServletInputStream)
    // - Định nghĩa interface chung cho các luồng đọc bảo mật
    // =================================================================================
    private static abstract class LuongDocBaoMatGoc extends ServletInputStream {
        public abstract boolean kiemTraDaXacThucChua();

        @Override
        public void close() throws IOException {
            // Lớp con sẽ ghi đè để dọn dẹp tài nguyên nếu cần
        }
    }

    // =================================================================================
    // LỚP NỘI BỘ 3: KIỂM DUYỆT RỖNG (Chống Trojan Horse & Whitespace DoS)
    // - Không dùng SHA-256 -> tiết kiệm CPU
    // - Chỉ cho phép space, tab, LF, CR; throw nếu byte > 32
    // =================================================================================
    private static class LuongKiemDuyetRong extends LuongDocBaoMatGoc {

        private final ServletInputStream luongGoc;
        private boolean daKetThucStream = false;
        private boolean daXacThucThanhCong = false;
        private int tongSoKhoangTrang = 0;

        public LuongKiemDuyetRong(ServletInputStream luongGoc) {
            this.luongGoc = luongGoc;
        }

        private void kiemTraGiaTriDuongChuan(int giaTriDuongChuan) throws IOException {
            // Ký tự > 32 (ASCII) là ký tự in được, không thể xuất hiện trong Request rỗng
            if (giaTriDuongChuan > MOC_ASCII_KHOANG_TRANG_HOP_LE) {
                System.err.println(
                        "[BodyIntegrityFilter] PHÁT HIỆN Trojan: Byte 0x" + Integer.toHexString(giaTriDuongChuan));
                throw new LoiBaoMatIntegrity(
                        "Tấn công Trojan: Phát hiện dữ liệu lén lút mã Hex 0x" + Integer.toHexString(giaTriDuongChuan));
            }

            tongSoKhoangTrang = tongSoKhoangTrang + 1;
            if (tongSoKhoangTrang > GIOI_HAN_KHOANG_TRANG_TOI_DA) {
                System.err.println("[BodyIntegrityFilter] PHÁT HIỆN Whitespace DoS.");
                throw new LoiBaoMatIntegrity("Tấn công DoS: Lượng khoảng trắng vượt ngưỡng 1KB.");
            }
        }

        private void chotKetQuaKhiCanLuong() {
            if (daKetThucStream == false) {
                daKetThucStream = true;
                daXacThucThanhCong = true;
                System.out.println("[BodyIntegrityFilter] Xác thực yêu cầu RỖNG thành công.");
            }
        }

        @Override
        public int read() throws IOException {
            int duLieu = luongGoc.read();
            if (duLieu == -1) {
                chotKetQuaKhiCanLuong();
            } else {
                kiemTraGiaTriDuongChuan(duLieu);
            }
            return duLieu;
        }

        @Override
        public int read(byte[] mangByte, int viTriBatDau, int doDai) throws IOException {
            int soByteDocDuoc = luongGoc.read(mangByte, viTriBatDau, doDai);

            if (soByteDocDuoc > 0) {
                for (int i = viTriBatDau; i < viTriBatDau + soByteDocDuoc; i = i + 1) {
                    // TỐI ƯU: Ép kiểu dương bitwise đúng 1 lần trên mỗi byte
                    int giaTriDuongChuan = mangByte[i] & 0xFF;
                    kiemTraGiaTriDuongChuan(giaTriDuongChuan);
                }
            } else if (soByteDocDuoc == -1) {
                chotKetQuaKhiCanLuong();
            }
            return soByteDocDuoc;
        }

        @Override
        public int read(byte[] mangByte) throws IOException {
            return read(mangByte, 0, mangByte.length);
        }

        @Override
        public boolean kiemTraDaXacThucChua() {
            return daXacThucThanhCong;
        }

        @Override
        public boolean isFinished() {
            return luongGoc.isFinished();
        }

        @Override
        public boolean isReady() {
            return luongGoc.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            luongGoc.setReadListener(listener);
        }

        @Override
        public void close() throws IOException {
            luongGoc.close();
        }
    }

    // =================================================================================
    // LỚP NỘI BỘ 4: BĂM HASH TỰ ĐỘNG (Bảo vệ RAM và Chống Timing Attack)
    // - Sử dụng DigestInputStream để băm on-the-fly
    // - So sánh hash cuối cùng bằng MessageDigest.isEqual()
    // =================================================================================
    private static class LuongBamHash extends LuongDocBaoMatGoc {

        private final ServletInputStream luongGoc;
        private final DigestInputStream luongDongHoBam;
        private final MessageDigest boBamSHA256;
        private final String maBamKyVong;

        private boolean daKetThucStream = false;
        private boolean daXacThucThanhCong = false;

        public LuongBamHash(ServletInputStream luongGoc, String maBamKyVong) throws IOException {
            this.luongGoc = luongGoc;
            this.maBamKyVong = maBamKyVong;
            try {
                this.boBamSHA256 = MessageDigest.getInstance("SHA-256");
                this.luongDongHoBam = new DigestInputStream(luongGoc, this.boBamSHA256);
                System.out.println("[BodyIntegrityFilter] Khởi tạo DigestInputStream (SHA-256).");
            } catch (NoSuchAlgorithmException e) {
                System.err.println("[BodyIntegrityFilter] LỖI: Thuật toán SHA-256 không tồn tại.");
                throw new LoiBaoMatIntegrity("Lỗi hệ thống: Không tìm thấy thư viện SHA-256 lõi.", e);
            }
        }

        private void kiemTraVaDoiSoatKhiCanNuoc(int soByteVuaDoc) throws IOException {
            if (soByteVuaDoc == -1) {
                if (daKetThucStream == false) {
                    daKetThucStream = true;

                    String maBamThucTe = layChuoiHexDaBam();
                    byte[] mangByteKyVong = maBamKyVong.toLowerCase().getBytes(StandardCharsets.UTF_8);
                    byte[] mangByteThucTe = maBamThucTe.toLowerCase().getBytes(StandardCharsets.UTF_8);

                    System.out.println("[BodyIntegrityFilter] Đối soát mã băm:");
                    System.out.println("   >> Kỳ vọng (từ Attribute): " + maBamKyVong);
                    System.out.println("   >> Thực tế (băm từ luồng) : " + maBamThucTe);

                    // MessageDigest.isEqual: Chống Timing Attack bằng so sánh thời gian cố định
                    if (MessageDigest.isEqual(mangByteKyVong, mangByteThucTe) == false) {
                        System.err.println("[BodyIntegrityFilter] XÁC THỰC THẤT BẠI: Nội dung đã bị chỉnh sửa!");
                        throw new LoiBaoMatIntegrity("Lỗi toàn vẹn: Mã băm SHA-256 thực tế không khớp với chữ ký.");
                    }

                    System.out.println("[BodyIntegrityFilter] XÁC THỰC THÀNH CÔNG: Dữ liệu toàn vẹn.");
                    daXacThucThanhCong = true;
                }
            }
        }

        private String layChuoiHexDaBam() {
            byte[] ketQuaBam = boBamSHA256.digest();
            StringBuilder chuoiHex = new StringBuilder();

            for (int i = 0; i < ketQuaBam.length; i = i + 1) {
                int giaTriDuong = 0xFF & ketQuaBam[i];
                String hex = Integer.toHexString(giaTriDuong);

                if (hex.length() == 1) {
                    chuoiHex.append("0");
                }
                chuoiHex.append(hex);
            }
            return chuoiHex.toString();
        }

        @Override
        public int read() throws IOException {
            try {
                int duLieu = luongDongHoBam.read();
                if (duLieu == -1) {
                    kiemTraVaDoiSoatKhiCanNuoc(-1);
                } else {
                    // Đã đọc dữ liệu, nhưng chưa kết thúc
                    kiemTraVaDoiSoatKhiCanNuoc(1);
                }
                return duLieu;
            } catch (LoiBaoMatIntegrity e) {
                throw e; // Ném thẳng lên để filter catch
            } catch (Exception e) {
                throw new IOException(e); // Giữ nguyên lỗi luồng gốc
            }
        }

        @Override
        public int read(byte[] mangByte, int viTriBatDau, int doDai) throws IOException {
            try {
                int soByteDocDuoc = luongDongHoBam.read(mangByte, viTriBatDau, doDai);
                kiemTraVaDoiSoatKhiCanNuoc(soByteDocDuoc);
                return soByteDocDuoc;
            } catch (LoiBaoMatIntegrity e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public int read(byte[] mangByte) throws IOException {
            return read(mangByte, 0, mangByte.length);
        }

        @Override
        public boolean kiemTraDaXacThucChua() {
            return daXacThucThanhCong;
        }

        @Override
        public boolean isFinished() {
            return luongGoc.isFinished();
        }

        @Override
        public boolean isReady() {
            return luongGoc.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            luongGoc.setReadListener(listener);
        }

        @Override
        public void close() throws IOException {
            luongGoc.close();
            luongDongHoBam.close();
        }
    }
}