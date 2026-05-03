//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/tomcatIO/PublicApiSizeFilter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.tomcatIO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * ================================================================
 * TẦNG 3 (Spring Filter) — XỬ LÝ CHUNKED TRANSFER ENCODING
 * ================================================================
 *
 * MỤC ĐÍCH TỒN TẠI:
 * Mặc dù Valve (Tầng 2) đã chặn được Payload quá kích thước, nhưng Valve chỉ
 * đọc Header (Content-Length). Nếu Hacker gửi dữ liệu theo dạng "Chunked"
 * (Tức là Content-Length = -1, chia gói tin thành nhiều mảnh vô tận),
 * Valve sẽ bị mù và bỏ qua.
 *
 * Filter này sẽ bọc InputStream lại, đếm từng Byte đi qua thực tế. Nếu phát
 * hiện tổng số Byte vượt giới hạn cho phép -> Lập tức đá Hacker ra ngoài!
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Chạy ngay sau Valve, trước mọi Filter khác

public class PublicApiSizeFilter extends OncePerRequestFilter {

    private final long GIOI_HAN_JSON_FORM;
    private final long GIOI_HAN_MULTIPART;
    private final long GIOI_HAN_TAI_TRONG_LON;
    private final long GIOI_HAN_THOI_GIAN_DOC_MS;
    private final SecurityLibraryProperties thietLapLoi;

    // Constructor Injection từ Spring Boot
    public PublicApiSizeFilter(SecurityLibraryProperties props) {
        this.GIOI_HAN_JSON_FORM = props.getLimitJsonFormKb() * 1024L;
        this.GIOI_HAN_MULTIPART = props.getLimitMultipartMb() * 1024L * 1024L;
        this.GIOI_HAN_TAI_TRONG_LON = props.getLimitLargePayloadMb() * 1024L * 1024L;
        this.GIOI_HAN_THOI_GIAN_DOC_MS = props.getMaxRequestReadTimeMs();
        this.thietLapLoi = props;
    }

    private static final Logger log = Logger.getLogger(PublicApiSizeFilter.class.getName());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String method = request.getMethod();
        boolean laPhuongThucCanKiemTra = false;

        // Bước 1: Chỉ kiểm tra các Method có mang Body thay đổi dữ liệu
        if ("POST".equalsIgnoreCase(method) == true) {
            laPhuongThucCanKiemTra = true;
        } else if ("PUT".equalsIgnoreCase(method) == true) {
            laPhuongThucCanKiemTra = true;
        } else if ("PATCH".equalsIgnoreCase(method) == true) {
            laPhuongThucCanKiemTra = true;
        } else if ("DELETE".equalsIgnoreCase(method) == true) {
            laPhuongThucCanKiemTra = true;
        }

        if (laPhuongThucCanKiemTra == false) {
            chain.doFilter(request, response);
            return;
        }

        // Bước 2: Phân loại giới hạn dung lượng dựa trên URI và Content-Type
        long gioiHanDuLieu = GIOI_HAN_JSON_FORM; // Mặc định là 10KB
        String uri = request.getRequestURI();
        
        // MỞ ĐƯỜNG LỚN CHO CÁC API ĐẶC THÙ
        if (thietLapLoi.kiemTraLaUrlTaiTrongLon(uri) == true) {
            gioiHanDuLieu = GIOI_HAN_TAI_TRONG_LON;
        }

        String contentType = request.getContentType();
        long gioiHanThoiGianMs = GIOI_HAN_THOI_GIAN_DOC_MS; // Mặc định 60 giây để đọc hết Body

        if (contentType != null) {
            if (contentType.toLowerCase().startsWith("multipart/form-data") == true) {
                gioiHanDuLieu = GIOI_HAN_MULTIPART; // Upload File được lên tới 50MB
            }
        }

        // Bước 3: Xem Valve có xử lý được không?
        long contentLength = request.getContentLengthLong();
        if (contentLength >= 0) {
            if (contentLength > gioiHanDuLieu) {
                tuChoiRequest(response, 413, "Payload vượt giới hạn.");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // =========================================================================
        // Bước 4: CHỈ CHẠY VÀO ĐÂY KHI CHUNKED (Content-Length = -1)
        // Tiến hành bọc luồng InputStream để đếm thủ công từng Byte
        // =========================================================================
        log.fine("[PublicApiSizeFilter] Phát hiện Chunked Transfer. Bắt đầu đếm dung lượng...");
        DoiSoatHttpRequest wrappedRequest = new DoiSoatHttpRequest(request, gioiHanDuLieu, gioiHanThoiGianMs);

        try {
            // Cho Request có bọc vỏ bảo vệ đi tiếp vào Controller
            chain.doFilter(wrappedRequest, response);

        } catch (Exception e) {
            boolean laLoiDungLuong = false;
            Throwable nguyenNhanGoc = e;

            // Truy tìm nguyên nhân gốc của Lỗi (Tránh kẹt lặp vô tận)
            for (int i = 0; i < 15; i = i + 1) {
                if (nguyenNhanGoc == null) {
                    break;
                }

                // Nếu tìm thấy Exception do ống nước bảo vệ ném ra
                if (nguyenNhanGoc instanceof PayloadTooLargeException) {
                    laLoiDungLuong = true;
                    break;
                }
                nguyenNhanGoc = nguyenNhanGoc.getCause();
            }

            // Xử lý ném lỗi trả về cho Hacker
            if (laLoiDungLuong == true) {
                System.err.println("[PublicApiSizeFilter] Đã ngắt kết nối Chunked. Lý do: Tràn dung lượng ("
                        + gioiHanDuLieu + " bytes).");
                tuChoiRequest(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "Chunked payload vượt giới hạn cho phép.");
            } else {
                // Nếu không phải do tràn dung lượng, ném ngược lại để Spring tự lo
                throw e;
            }
        }
    }

    private void tuChoiRequest(HttpServletResponse response, int statusCode, String thongDiep) throws IOException {
        if (response.isCommitted() == false) {
            response.setStatus(statusCode);
            response.setContentType("application/json;charset=UTF-8");
            String chuoiJson = "{\"error\":\"" + thongDiep + "\"}";
            byte[] body = chuoiJson.getBytes(StandardCharsets.UTF_8);
            response.setContentLength(body.length);
            response.getOutputStream().write(body);
            response.getOutputStream().flush();
        }
    }

    // --- LỚP EXCEPTION ĐỊNH NGHĨA RIÊNG ---
    static class PayloadTooLargeException extends IOException {
        PayloadTooLargeException(long totalBytes, long gioiHan) {
            super("Payload vượt giới hạn: " + totalBytes + "B > " + gioiHan + "B");
        }
    }

    // --- LỚP BỌC REQUEST (WRAPPER) ---
    private static class DoiSoatHttpRequest extends HttpServletRequestWrapper {
        private GiamSatStream cachedStream;
        private BufferedReader cachedReader = null;
        private final long gioiHanDuLieu;
        private final long gioiHanThoiGianMs;

        DoiSoatHttpRequest(HttpServletRequest request, long gioiHanDuLieu, long gioiHanThoiGianMs) {
            super(request);
            this.gioiHanDuLieu = gioiHanDuLieu;
            this.gioiHanThoiGianMs = gioiHanThoiGianMs;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (cachedStream == null) {
                // Truyền cả 2 giới hạn vào Stream
                cachedStream = new GiamSatStream(super.getInputStream(), gioiHanDuLieu, gioiHanThoiGianMs);
            }
            return cachedStream;
        }

       @Override
        public BufferedReader getReader() throws IOException {
            if (cachedStream != null) {
                throw new IllegalStateException("Hàm getInputStream() đã được gọi trước đó.");
            }
            if (cachedReader == null) {
                cachedStream = new GiamSatStream(super.getInputStream(), gioiHanDuLieu, gioiHanThoiGianMs);
                cachedReader = new BufferedReader(new InputStreamReader(cachedStream, StandardCharsets.UTF_8));
            }
            return cachedReader;
        }
    }

    // --- LỚP ỐNG NƯỚC ĐẾM SỐ LƯỢNG BYTE (STREAM) ---
    private static class GiamSatStream extends ServletInputStream {
        private final ServletInputStream original;
        private final long gioiHanDuLieu;
        private final long gioiHanThoiGianMs;
        private final AtomicLong tongSoByte = new AtomicLong(0L); // Thread-safe đếm số Byte
        // [VÁ LỖI SLOWLORIS]: Ghi lại mốc thời gian bắt đầu đọc
        private final long thoiDiemBatDau = System.currentTimeMillis();

       GiamSatStream(ServletInputStream original, long gioiHanDuLieu, long gioiHanThoiGianMs) {
            this.original = original;
            this.gioiHanDuLieu = gioiHanDuLieu;
            this.gioiHanThoiGianMs = gioiHanThoiGianMs;
        }

        // Hàm trái tim: Kiểm tra và cộng dồn số Byte sau mỗi chu kỳ múc dữ liệu
        private void kiemTraGioiHan(int bytesVuaDoc) throws IOException {
            // 1. Chống Tấn công Rỉ giọt (Slowloris Dribble) - Sử dụng biến linh hoạt
            if (System.currentTimeMillis() - thoiDiemBatDau > gioiHanThoiGianMs) {
                throw new IOException("Tấn công Slowloris bị chặn: Request ngâm luồng quá thời gian cho phép!");
            }

            // 2. Chống Tràn RAM (Chunked Bomb)
            if (bytesVuaDoc > 0) {
                long tong = tongSoByte.addAndGet(bytesVuaDoc);
                if (tong > gioiHanDuLieu) {
                    throw new PayloadTooLargeException(tong, gioiHanDuLieu);
                }
            }
        }

        @Override
        public int read() throws IOException {
            int byteDocDuoc = original.read();

            // Viết rõ ràng logic thay vì dùng toán tử 3 ngôi
            if (byteDocDuoc >= 0) {
                kiemTraGioiHan(1);
            } else {
                kiemTraGioiHan(0);
            }
            return byteDocDuoc;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int soByteVuaDoc = original.read(buf, off, len);
            kiemTraGioiHan(soByteVuaDoc);
            return soByteVuaDoc;
        }

        @Override
        public long skip(long soByteCanNhay) throws IOException {
            long soByteDaNhay = original.skip(soByteCanNhay);

            if (soByteDaNhay > 0) {
                long tong = tongSoByte.addAndGet(soByteDaNhay);
                if (tong > gioiHanDuLieu) {
                    throw new PayloadTooLargeException(tong, gioiHanDuLieu);
                }
            }
            return soByteDaNhay;
        }

        @Override
        public boolean isFinished() {
            return original.isFinished();
        }

        @Override
        public boolean isReady() {
            return original.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            original.setReadListener(listener);
        }

        @Override
        public void close() throws IOException {
            original.close();
        }
    }
}