//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/tomcatIO/TomcatServerConfig.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.tomcatIO;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * ============================================================
 * TẦNG BẢO VỆ TOMCAT - CHỐNG DoS / SLOWLORIS / FLOOD ATTACK
 * ============================================================
 *
 * KIẾN TRÚC 3 LỚP:
 * * [Internet] ──► [Lớp 1: NIO Connector Config] ← Chống Slowloris, giới hạn global
 * │
 * ▼
 * [Lớp 2: Engine Valve] ← Chặn payload lớn O(1), chặn Rate Limit per-IP, lock-free
 * │
 * ▼
 * [Lớp 3: PublicApiSizeFilter] ← Xử lý chunked transfer vô hạn (ẩn Content-Length)
 * │
 * ▼
 * [Spring Boot / Security / Controller]
 *
 * NGUYÊN TẮC THIẾT KẾ CODE TRONG FILE NÀY:
 * - Happy path (request hợp lệ): KHÔNG tạo mới object (ZERO object allocation) để tiết kiệm RAM.
 * - Ghi log bất đồng bộ bằng java.util.logging.Logger, KHÔNG dùng System.err.println để tránh kẹt I/O.
 * - Rate limiter: Hoàn toàn lock-free bằng CAS (Compare-And-Swap) trên AtomicLong.
 * - Không dùng String.intern() (tránh khóa JVM StringPool toàn cục).
 */
@Component
public class TomcatServerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private static final Logger log = Logger.getLogger(TomcatServerConfig.class.getName());

    //Bơm Thư viện cấu hình vào đây
    private final SecurityLibraryProperties props;

    // Khởi tạo bằng Constructor Injection để đảm bảo an toàn vòng đời Spring
    public TomcatServerConfig(SecurityLibraryProperties props) {
        this.props = props;
    }


    @Override
    public void customize(TomcatServletWebServerFactory factory) {

        // =========================================================================
        // ENGINE VALVE — Điểm chặn sớm nhất trong Tomcat
        // Chạy ngay khi packet TCP vừa được accept, trước khi Spring Boot kịp biết.
        // Truyền đối tượng props vào Constructor của Valve
        // =========================================================================
        factory.addEngineValves(new LinhGacTomcatValve(props));
        log.info("[TomcatServerConfig] Engine Valve đã triển khai tại tầng sớm nhất.");

        // =========================================================================
        // NIO CONNECTOR CONFIG — Giới hạn global cấu hình 1 lần lúc khởi động.
        // KHÔNG set per-request để tránh race condition giữa các luồng.
        // =========================================================================
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                
                // Chỉ áp dụng cho giao thức Http11NioProtocol
                if (connector.getProtocolHandler() instanceof Http11NioProtocol) {
                    Http11NioProtocol nio = (Http11NioProtocol) connector.getProtocolHandler();
                    
                    // Chống Slowloris: Ngắt kết nối nếu Client giữ header quá 5 giây
                    nio.setConnectionTimeout(5000);

                    // =========================================================================
                    //CHỐNG SLOWLORIS BODY (NGHIỀN NÁT NÚT THẮT CỔ CHAI ĐA LUỒNG)
                    // Bắt buộc Client không được phép ngâm luồng (nghỉ tay) quá 5 giây khi đang gửi Body
                    // =========================================================================
                    nio.setDisableUploadTimeout(false); // Bật đồng hồ đếm ngược tử thần cho việc Upload Body
                    nio.setConnectionUploadTimeout(3000); // Quá 5 giây không gửi thêm Byte nào -> Chém đứt kết nối!
                    
                    // Chống Body Flood: Cắt ngay socket TCP khi trả lỗi 4xx, không bú thêm data
                    nio.setMaxSwallowSize(0);
                    
                    // Tổng TCP connection active tối đa
                    nio.setMaxConnections(10000);
                    
                    // Giới hạn hàng chờ TCP tại Kernel OS
                    nio.setAcceptCount(100);
                    
                    log.info("[TomcatServerConfig] NIO: timeout=5s, maxSwallow=0B, maxConn=10000, acceptQueue=100");
                }
            }
        });
    }

    // ==================================================================================
    // ENGINE VALVE — LÍNH GÁC TẦNG TOMCAT (LỚP NỘI BỘ)
    // ==================================================================================
    private static class LinhGacTomcatValve extends ValveBase {

        private static final Pattern XFF_SPLIT = Pattern.compile("\\s*,\\s*");
        
        // Giới hạn 10KB cho mọi gói tin JSON/Form thông thường
        private final long GIOI_HAN_JSON_FORM;
        
        // Giới hạn 50MB CHỈ DÀNH RIÊNG cho upload File (Multipart)
        private final long GIOI_HAN_MULTIPART;

        // Giới hạn nới lỏng dành cho các Request Tải Trọng Lớn (Large Payload)
        private final long GIOI_HAN_TAI_TRONG_LON;

        // Cấu hình Rate Limit (Chống Flood)
        private final int RATE_LIMIT_PER_SECOND; // 20 Request / Giây / IP
        private final int NGUONG_BAN;            // 5 lần vi phạm sẽ bị cấm
        private final long THOI_GIAN_BAN_MS; // Thời gian cấm: 60 giây
        
        // Cấu hình dọn dẹp RAM
        private final long IDLE_TIMEOUT_MS; // 10 phút không hoạt động -> Xóa IP
        private final long DON_DEP_SAU_MS;   // 5 phút chạy dọn dẹp 1 lần
        private final int MAX_MAP_SIZE; // Tối đa 100.000 IP trong RAM

        private final String trustedProxyPrefix;
        private final SecurityLibraryProperties thietLapLoi;

        // Bảng theo dõi IP - Dùng ConcurrentHashMap để Thread-safe
        private static final ConcurrentHashMap<String, ThongTinIp> bangIpTracker = new ConcurrentHashMap<>(4096, 0.75f, 64);
        
        // Cờ thời gian dọn dẹp tiếp theo
        private static final AtomicLong thoiDiemDonDepTiepTheo = new AtomicLong(System.currentTimeMillis());

        // --- CẤU TRÚC DỮ LIỆU THEO DÕI IP ---
        private static class ThongTinIp {
            final AtomicLong giaySoReqPacked; // Lưu gộp [Giây] và [Số Request] để dùng CAS lock-free
            final AtomicInteger soLanViPham = new AtomicInteger(0);
            volatile long thoiDiemHetBan = 0L;
            volatile long thoiDiemCuoi;

            ThongTinIp(long nowMs) {
                long giay = nowMs / 1000;
                this.giaySoReqPacked = new AtomicLong(packGiaySoReq(giay, 1));
                this.thoiDiemCuoi = nowMs;
            }

            // Kỹ thuật gộp 2 số nguyên 32bit vào 1 số long 64bit
            static long packGiaySoReq(long giay, int soReq) {
                return (giay << 32) | (soReq & 0xFFFFFFFFL);
            }

            static long unpackGiay(long packed) {
                return packed >>> 32;
            }

            static int unpackSoReq(long packed) {
                return (int) (packed & 0xFFFFFFFFL);
            }
        }

        // --- CONSTRUCTOR ---
        // Gán giá trị hằng số từ cấu hình thư viện
        LinhGacTomcatValve(SecurityLibraryProperties props) {
            super(true); // ÉP TOMCAT VALVE HỖ TRỢ ASYNC STREAMING!
            this.GIOI_HAN_JSON_FORM = props.getLimitJsonFormKb() * 1024L;
            this.GIOI_HAN_MULTIPART = props.getLimitMultipartMb() * 1024L * 1024L;
            this.GIOI_HAN_TAI_TRONG_LON = props.getLimitLargePayloadMb() * 1024L * 1024L;
            this.RATE_LIMIT_PER_SECOND = props.getRateLimitPerSec();
            this.NGUONG_BAN = props.getRateLimitBanThreshold();
            this.THOI_GIAN_BAN_MS = props.getRateLimitBanTimeMs();
            this.IDLE_TIMEOUT_MS = props.getRateLimitIdleTimeoutMs();
            this.DON_DEP_SAU_MS = props.getRateLimitCleanupMs();
            this.MAX_MAP_SIZE = props.getRateLimitMaxMapSize();
            this.trustedProxyPrefix = props.getTrustedProxyPrefix();
            this.thietLapLoi = props;
            
            // Cập nhật lại mốc dọn dẹp đầu tiên
            thoiDiemDonDepTiepTheo.set(System.currentTimeMillis() + this.DON_DEP_SAU_MS);
        }

        // --- HÀM THỰC THI CHÍNH CỦA VALVE ---
        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            final String uri = request.getRequestURI();
            final String method = request.getMethod();
            final long now = System.currentTimeMillis();
            final String ipClient = layIpThuc(request);

            // =========================================================================
            // BƯỚC 1: QUẢN LÝ RATE LIMIT & BAN IP (Áp dụng cho mọi Request, kể cả GET)
            // =========================================================================
            ThongTinIp thongTin = bangIpTracker.get(ipClient);
            
            // Nếu là IP lạ lần đầu xuất hiện
            if (thongTin == null) {
                
                // Nếu bảng băm đã đầy (Nguy cơ bị tấn công DDos IP rác)
                if (bangIpTracker.size() >= MAX_MAP_SIZE) {
                    donDepBangIp(now); // Thử dọn dẹp trước
                    
                    // Nếu dọn dẹp xong vẫn đầy, chặn không cho tạo mới để bảo vệ RAM
                    if (bangIpTracker.size() >= MAX_MAP_SIZE) {
                        log.warning("[Valve] KHẨN CẤP: Map Tracker đầy. Chặn request từ IP lạ.");
                        tuChoiRequest(response, 429, "Hệ thống đang chịu tải cao. Vui lòng thử lại sau.");
                        return;
                    }
                }
                
                ThongTinIp moi = new ThongTinIp(now);
                // Dùng putIfAbsent để an toàn trong môi trường đa luồng
                thongTin = bangIpTracker.putIfAbsent(ipClient, moi);
                if (thongTin == null) {
                    thongTin = moi;
                }
            }

            // Cập nhật mốc thời gian hoạt động cuối cùng của IP này
            thongTin.thoiDiemCuoi = now;

            // Kiểm tra xem IP này có đang trong thời gian bị phạt (Ban) không
            if (thongTin.thoiDiemHetBan > now) {
                tuChoiRequest(response, 429, "IP tạm thời bị khóa do hành vi bất thường.");
                return;
            }

            // Kiểm tra xem IP này có gọi quá số Request/Giây cho phép không
            if (kiemTraVuotRateLimit(thongTin, now) == true) {
                ghiNhanViPham(thongTin, ipClient, -1, uri, now);
                tuChoiRequest(response, 429, "Quá nhiều yêu cầu. Vui lòng thử lại sau.");
                return;
            }

            // =========================================================================
            // BƯỚC 2: KIỂM TRA CONTENT-LENGTH (Chỉ áp dụng cho request có Body)
            // =========================================================================
            boolean laMethodCoBody = false;
            if ("POST".equalsIgnoreCase(method) == true) {
                laMethodCoBody = true;
            } else if ("PUT".equalsIgnoreCase(method) == true) {
                laMethodCoBody = true;
            } else if ("PATCH".equalsIgnoreCase(method) == true) {
                laMethodCoBody = true;
            } else if ("DELETE".equalsIgnoreCase(method) == true) {
                laMethodCoBody = true;
            }

            if (laMethodCoBody == true) {
                long contentLength = request.getContentLengthLong();
                
                // Nếu Client có khai báo độ dài Body
                if (contentLength > 0) {
                    long dungLuongToiDaChoPhep = GIOI_HAN_JSON_FORM; // Mặc định là 10KB
                    
                    // NỚI LỎNG CHO CÁC URL TẢI TRỌNG LỚN (LARGE PAYLOAD)
                    if (thietLapLoi.kiemTraLaUrlTaiTrongLon(uri) == true) {
                        dungLuongToiDaChoPhep = GIOI_HAN_TAI_TRONG_LON;
                    }

                    String contentType = request.getContentType();
                    if (contentType != null) {
                        if (contentType.toLowerCase().startsWith("multipart/form-data") == true) {
                            // Nếu phát hiện Upload File, nới lỏng giới hạn thành 50MB (Ưu tiên cao nhất)
                            dungLuongToiDaChoPhep = GIOI_HAN_MULTIPART;
                        }
                    }

                    // Nếu gói tin gửi lên to hơn giới hạn cho phép
                    if (contentLength > dungLuongToiDaChoPhep) {
                        ghiNhanViPham(thongTin, ipClient, contentLength, uri, now);
                        tuChoiRequest(response, 413, "Dữ liệu gửi lên vượt quá giới hạn hệ thống cho phép.");
                        return;
                    }
                }
            }

            // =========================================================================
            // BƯỚC 3: DỌN DẸP BỘ NHỚ VÀ CHUYỂN TIẾP VÀO TRONG
            // =========================================================================
            long thoiDiemDon = thoiDiemDonDepTiepTheo.get();
            if (now > thoiDiemDon) {
                // Đảm bảo chỉ có 1 Luồng duy nhất thực hiện việc dọn dẹp
                if (thoiDiemDonDepTiepTheo.compareAndSet(thoiDiemDon, now + DON_DEP_SAU_MS) == true) {
                    donDepBangIp(now);
                }
            }

            // Cho phép Request đi tiếp vào tầng Filter của Spring
            getNext().invoke(request, response);
        }

        // --- CÁC HÀM TIỆN ÍCH CỦA LÍNH GÁC ---
        private String layIpThuc(Request request) {
            String ipTrucTiep = request.getRemoteAddr();
            
            if (ipTrucTiep.startsWith(trustedProxyPrefix)) {
                // 1. Ưu tiên Header xịn của Cloudflare (Hacker fake sẽ bị CF đè mất)
                String cfIp = request.getHeader("CF-Connecting-IP");
                if (cfIp != null && !cfIp.isEmpty()) return cfIp;

                // 2. Ưu tiên Header xịn của Nginx / AWS
                String trueClientIp = request.getHeader("X-Real-IP");
                if (trueClientIp != null && !trueClientIp.isEmpty()) return trueClientIp;

                // 3. Nếu xài X-Forwarded-For, LUÔN LẤY IP Ở GẦN CUỐI NHẤT (Sát proxy nhà mình nhất)
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isEmpty()) {
                    String[] danhSachIp = XFF_SPLIT.split(xff.trim());
                    // Lấy IP gần cuối (Cách proxy nhà mình 1 nấc). Kệ xác bọn fake ở danhSachIp[0]
                    int viTriTinCay = Math.max(0, danhSachIp.length - 1); 
                    String ipThat = danhSachIp[viTriTinCay];
                    if (!ipThat.isEmpty()) {
                        return ipThat.toLowerCase();
                    }
                }
            }
            return ipTrucTiep;
        }

        private boolean kiemTraVuotRateLimit(ThongTinIp thongTin, long nowMs) {
            long giayHienTai = nowMs / 1000;
            
            // Vòng lặp bảo vệ CAS (Compare-And-Swap) chống chạy đua (Race Condition) đa luồng
            while (true) {
                long packed = thongTin.giaySoReqPacked.get();
                long giayCu = ThongTinIp.unpackGiay(packed);
                int soReqCu = ThongTinIp.unpackSoReq(packed);

                long newPacked;
                int soReqMoi;

                if (giayCu != giayHienTai) {
                    // Đã bước sang giây mới -> Reset đếm lại từ 1
                    soReqMoi = 1;
                    newPacked = ThongTinIp.packGiaySoReq(giayHienTai, soReqMoi);
                } else {
                    // Vẫn trong giây hiện tại -> Cộng dồn số request
                    soReqMoi = soReqCu + 1;
                    newPacked = ThongTinIp.packGiaySoReq(giayHienTai, soReqMoi);
                }

                // Cập nhật giá trị mới. Nếu thành công thì thoát vòng lặp
                if (thongTin.giaySoReqPacked.compareAndSet(packed, newPacked) == true) {
                    if (soReqMoi > RATE_LIMIT_PER_SECOND) {
                        return true; // Vượt quá giới hạn
                    } else {
                        return false; // Vẫn an toàn
                    }
                }
            }
        }

        private void ghiNhanViPham(ThongTinIp thongTin, String ip, long contentLength, String uri, long now) {
            int soLan = thongTin.soLanViPham.incrementAndGet();
            if (soLan >= NGUONG_BAN) {
                // Khóa mỏm IP này lại
                thongTin.thoiDiemHetBan = now + THOI_GIAN_BAN_MS;
                thongTin.soLanViPham.set(0); // Reset số lần vi phạm
                
                String thongDiepLog = "[Valve] BAN IP " + ip + " trong 60s | URI: " + uri;
                if (contentLength > 0) {
                    thongDiepLog = thongDiepLog + " | payload=" + contentLength + "B";
                } else {
                    thongDiepLog = thongDiepLog + " | lý do: rate flood";
                }
                log.warning(thongDiepLog);
            }
        }

        private void tuChoiRequest(Response response, int statusCode, String message) throws IOException {
            if (response.isCommitted() == false) {
                response.setStatus(statusCode);
                response.setContentType("application/json;charset=UTF-8");
                byte[] body = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
                response.setContentLength(body.length);
                response.getOutputStream().write(body);
                response.getOutputStream().flush();
            }
        }

        private void donDepBangIp(long nowMs) {
            long nguong = nowMs - IDLE_TIMEOUT_MS;
            int truoc = bangIpTracker.size();
            
            Iterator<Map.Entry<String, ThongTinIp>> iterator = bangIpTracker.entrySet().iterator();
            while (iterator.hasNext() == true) {
                Map.Entry<String, ThongTinIp> entry = iterator.next();
                ThongTinIp thongTin = entry.getValue();
                
                // Nếu IP đã không hoạt động quá thời gian nhàn rỗi
                if (thongTin.thoiDiemCuoi < nguong) {
                    // Và IP không nằm trong trạng thái đang bị phạt
                    if (thongTin.thoiDiemHetBan < nowMs) {
                        iterator.remove(); // Xóa khỏi bộ nhớ RAM
                    }
                }
            }
            
            int sau = bangIpTracker.size();
            if (truoc != sau) {
                log.info("[Valve] Dọn dẹp IP tracker: " + truoc + " → " + sau
                        + " entries (đã xóa " + (truoc - sau) + " IP nhàn rỗi).");
            }
        }
    }
}