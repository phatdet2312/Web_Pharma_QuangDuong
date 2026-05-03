//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Service/TramPhatSongVoTuyenP2P.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

/**
 * ====================================================================================
 * TRẠM PHÁT SÓNG VÔ TUYẾN KÝ SINH (P2P MULTICAST RADIO)
 * - Tự động thiết lập Lãnh địa và Mạng nội bộ cho Cụm Server (Không cần Redis).
 * - Thuật toán: Hằng số Runtime (Tính 1 lần lúc Boot). Hash (UserDir + JWT).
 * - Mục đích: Truyền tin "Giết DNA Quyền" (Chôn cất) cho các Server anh em với
 * tốc
 * độ ánh sáng.
 * ====================================================================================
 */
@Component
public class TramPhatSongVoTuyenP2P {

    private final String clusterIdThucTe;
    private final String ipVoTuyenThucTe;
    private final int portVoTuyenThucTe;
    private final String jwtSecretGoc;

    private final MaTranLuoiLocNghiaTrangQuyenHanCu nghiaTrangQuyenHan;
    private final MaTranLuoiLocChongPhatLai maTranLuoiLocChongPhatLai;
    private final SecurityLibraryProperties props;
    private MulticastSocket socketNhan;
    private InetAddress diaChiNhom;

    private final boolean choPhepDongBoXuyenLucDia;
    private final String[] danhSachUrlGocCumServer;

    private final InterClusterSyncCamouflage camouflage;
    private final JwtService jwtService;
    private final String BODY_HASH_RONG;

    @Autowired
    public TramPhatSongVoTuyenP2P(SecurityLibraryProperties props,
            MaTranLuoiLocNghiaTrangQuyenHanCu nghiaTrangQuyenHan,
            MaTranLuoiLocChongPhatLai maTranLuoiLocChongPhatLai,
            InterClusterSyncCamouflage camouflage,
            JwtService jwtService) {
        this.nghiaTrangQuyenHan = nghiaTrangQuyenHan;
        this.maTranLuoiLocChongPhatLai = maTranLuoiLocChongPhatLai;
        this.jwtSecretGoc = props.getJwtSecret();
        this.props = props;
        this.camouflage = camouflage;
        this.jwtService = jwtService;

        // 1. TẠO CHUỖI DNA ỨNG DỤNG (Tuyệt đối độc nhất cho từng Source Code)
        String thuMucCode = System.getProperty("user.dir"); // Lấy đường dẫn thư mục gốc chạy App
        String adnUngDung = thuMucCode + "|||" + this.jwtSecretGoc;

        this.clusterIdThucTe = props.dauAnLanhDia();

        // 3. TÍNH TOÁN IP MULTICAST (Dải 239.x.y.z dành cho Local Network)
        if (props.getMulticastIp() == null || props.getMulticastIp().trim().isEmpty()) {
            int hashIp = Math.abs(bamMurmurHash3(adnUngDung.getBytes(), 8888));

            // Lấy từng dải 8-bit bằng phép & 0xFF (Tương đương số 255)
            // Kết quả chắc chắn nằm từ 0 -> 255
            int x = (hashIp >> 16) & 0xFF;
            int y = (hashIp >> 8) & 0xFF;
            int z = hashIp & 0xFF;

            // Tránh dải 255 (IP Broadcast đặc biệt) để an toàn tuyệt đối
            if (x == 255)
                x = 254;
            if (y == 255)
                y = 254;
            if (z == 255)
                z = 254;

            this.ipVoTuyenThucTe = "239." + x + "." + y + "." + z;
        } else {
            this.ipVoTuyenThucTe = props.getMulticastIp().trim();
        }

        // 4. TÍNH TOÁN PORT MULTICAST (Từ 10000 đến 65000)
        if (props.getMulticastPort() <= 0) {
            int hashPort = Math.abs(bamMurmurHash3(adnUngDung.getBytes(), 9999));
            // 0x7FFF là 32767. Phép & 0x7FFF ép số nằm gọn trong khoảng 0 -> 32767.
            // Cộng 10000 => Dải Port sẽ luôn nằm từ 10000 đến 42767 (Cực kỳ an toàn, không
            // đụng Port hệ thống)
            this.portVoTuyenThucTe = 10000 + (hashPort & 0x7FFF);
        } else {
            this.portVoTuyenThucTe = props.getMulticastPort();
        }

        // 5. TÍNH TOÁN BODY HASH RỖNG DÙNG CHO HTTP SYNC
        this.BODY_HASH_RONG = camouflage.tinhBodyHashRong();

        // =========================================================================
        // ĐÓNG BĂNG HẰNG SỐ LIÊN CỤM O(1) ĐỂ BẢO VỆ CPU LÚC RUNTIME
        // =========================================================================
        this.choPhepDongBoXuyenLucDia = props.isEnableInterClusterSync();

        if (this.choPhepDongBoXuyenLucDia && props.getRemoteClusterUrls() != null
                && !props.getRemoteClusterUrls().isEmpty()) {
            int soLuongCum = props.getRemoteClusterUrls().size();
            this.danhSachUrlGocCumServer = new String[soLuongCum];

            // Nối sẵn chuỗi URL Đích 1 lần duy nhất!
            for (int i = 0; i < soLuongCum; i++) {
                this.danhSachUrlGocCumServer[i] = props.getRemoteClusterUrls().get(i);
            }
            System.out.println(
                    "[TramPhatSongP2P] Đã đóng băng cấu hình Ngoại Giao Xuyên Lục Địa: " + soLuongCum + " cụm.");
        } else {
            this.danhSachUrlGocCumServer = new String[0];
            System.out.println("[TramPhatSongP2P] Tính năng Xuyên Lục Địa không khả dụng hoặc bị tắt.");
        }
             
        System.out.println("[TramPhatSongP2P] LÃNH ĐỊA ĐƯỢC THIẾT LẬP:");
        System.out.println("   -> Cluster ID : " + this.clusterIdThucTe);
        System.out.println("   -> Tần số Radio: " + this.ipVoTuyenThucTe + ":" + this.portVoTuyenThucTe);

        // 6. MỞ ĐÀI PHÁT THANH (CHẠY NGẦM KHÔNG BLOCKED THREAD CHÍNH)
        khoiDongDaiPhatThanh();
    }

    // Expose ClusterID ra cho JwtService đóng dấu vào Token
    public String getClusterId() {
        return this.clusterIdThucTe;
    }

    /**
     * ====================================================================
     * KÊNH 1: BÁO ĐỘNG ĐỎ MA TRẬN NHỊ PHÂN (Bắn ngay khi Admin bấm nút)
     * ====================================================================
     */
    // 1A: Dành cho Admin gọi (Là người bắn -> Tự nhét thời gian mới vào)
    public void phatLenhGanCoUserUDPVaToanCau(Long userId) {
        long thoiDiemGoc = System.currentTimeMillis();
        phatLenhGanCoUserUDPVaToanCau(userId, thoiDiemGoc);
    }

    // 1B: Dành cho Filter gọi (Là người truyền tin -> Đọc thời gian từ gói tin)
    public void phatLenhGanCoUserUDPVaToanCau(Long userId, long thoiDiemGoc) {
        String data = userId.toString();
        // 1. Bắn UDP cho anh em trong cùng Data Center
        banSongVoTuyenUdpNoiBo("USER_BAN", data, thoiDiemGoc);
        // 2. Bắn HTTP Async sang các Châu lục khác (Nếu Dev bật)
        banSongHttpXuyenLucDia("USER_BAN", data, thoiDiemGoc);
    }

    /**
     * ====================================================================
     * KÊNH 2: BẮN DNA QUYỀN VÀO NGHĨA TRANG (Khai tử nhóm Token có chung quyền)
     * ====================================================================
     */

    // 2A: Admin gọi
    public void phatLenhTruyNaDnaQuyenUDPVaToanCau(String maDnaQuyenHanBiGiet) {
        long thoiDiemGoc = System.currentTimeMillis();
        phatLenhTruyNaDnaQuyenUDPVaToanCau(maDnaQuyenHanBiGiet, thoiDiemGoc);
    }

    public void phatLenhTruyNaDnaQuyenUDPVaToanCau(String maDnaQuyenHanBiGiet, long thoiDiemGoc) {
        // 1. Bắn UDP nội bộ cụm
        banSongVoTuyenUdpNoiBo("ROLE_KILL", maDnaQuyenHanBiGiet, thoiDiemGoc);
        // 2. Bắn HTTP Async sang cụm khác
        banSongHttpXuyenLucDia("ROLE_KILL", maDnaQuyenHanBiGiet, thoiDiemGoc);
    }

    /**
     * ====================================================================
     * KÊNH 3: BẮN LỆNH GIẢI PHÓNG USER KHỎI MA TRẬN SAU KHI CHỮA LÀNH, MỞ KHOÁ
     * ĐỒNG BỘ MỌI SERVER ĐỂ CHỐNG VÒNG LẶP BẮT HECK DB
     * ====================================================================
     */
    // 3A: Admin gọi
    public void phatLenhXoaCoUserUDPVaToanCau(Long userId) {
        long thoiDiemGoc = System.currentTimeMillis();
        phatLenhXoaCoUserUDPVaToanCau(userId, thoiDiemGoc);
    }
    
    public void phatLenhXoaCoUserUDPVaToanCau(Long userId, long thoiDiemGoc) {
        String data = userId.toString();
        // Báo anh em trong cụm
        banSongVoTuyenUdpNoiBo("USER_UNBAN", data, thoiDiemGoc);
        // Báo anh em quốc tế
        banSongHttpXuyenLucDia("USER_UNBAN", data, thoiDiemGoc);
    }

    /**
     * HÀM BẮN TÍN HIỆU (FIRE AND FORGET - O(1))
     * Khi Admin chém 1 DNA Quyền, hàm này bắn tin vào hư không, các Server anh em
     * sẽ tự
     * nghe thấy.
     */
    public void banSongVoTuyenUdpNoiBo(String loaiLenh, String duLieu, long thoiDiemGoc) {
        // CompletableFuture ném công việc này vào ThreadPool chạy nền
        CompletableFuture.runAsync(() -> {
            try {

                String payload = loaiLenh + "|||" + duLieu + "|||" + thoiDiemGoc;
                // Đóng mộc giáp lai bằng JWT Secret để chống Hacker tạo Tool giả mạo sóng UDP
                String chuKy = taoChuKyHmac(payload, this.jwtSecretGoc);

                String fullMessage = payload + "|||" + chuKy;
                byte[] data = fullMessage.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(data, data.length, diaChiNhom, portVoTuyenThucTe);
                // Dùng ngay cái Socket đang mở để bắn đi
                if (socketNhan != null && !socketNhan.isClosed()) {
                    socketNhan.send(packet);
                }
            } catch (Exception e) {
                System.err.println(
                        "[TramPhatSongP2P] Bắn tín hiệu vô tuyến nội bộ thất bại (không ảnh hưởng luồng chính).");
            }
        });
    }

    // --- HÀM LÕI HTTP XUYÊN LỤC ĐỊA ---
    private void banSongHttpXuyenLucDia(String loaiLenh, String duLieu, long thoiDiemGoc) {
        if (this.choPhepDongBoXuyenLucDia == false || this.danhSachUrlGocCumServer.length == 0) {
            return; // Tính năng tắt -> Tiết kiệm 100% tài nguyên
        }

        String payloadLenhAn = loaiLenh + "|||" + duLieu + "|||" + thoiDiemGoc;

        CompletableFuture.runAsync(() -> {
            try {
                String trojanJwt = jwtService.generateTrojanSyncToken(payloadLenhAn);

                for (int i = 0; i < this.danhSachUrlGocCumServer.length; i++) {
                    String baseUrl = this.danhSachUrlGocCumServer[i];
                    System.err.println("[TramPhatSongP2P]baseUrl " + baseUrl);
                    HttpURLConnection conn = null;
                    try {

                        String[] capMethodVaUrl = camouflage.sinhCapMethodVaUrl();
                        System.err.println("[TramPhatSongP2P]capMethodVaUrl " + capMethodVaUrl[0] + " " + capMethodVaUrl[1] );
                        String methodNguyTrang = capMethodVaUrl[0];
                        System.err.println("[TramPhatSongP2P]methodNguyTrang " + methodNguyTrang);
                        String pathNguyTrang = capMethodVaUrl[1];
                        System.err.println("[TramPhatSongP2P]pathNguyTrang " + pathNguyTrang);
                        String urlDich = baseUrl + pathNguyTrang;
                        System.err.println("[TramPhatSongP2P]urlDich " + urlDich);
                        String targetDomain = layDomainTuUrl(baseUrl);
                        System.err.println("[TramPhatSongP2P]targetDomain " + targetDomain);

                        long thoiGianVoBoc = System.currentTimeMillis();
                        String rawData = methodNguyTrang + "|" + targetDomain + "|" + pathNguyTrang + "|" + thoiGianVoBoc
                                + "|" + BODY_HASH_RONG;
                        String chuKyNguyTrang = taoChuKyHmac(rawData, this.jwtSecretGoc);

                        URL url = new URL(urlDich);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod(methodNguyTrang);

                        // Gửi dữ liệu qua Header để giấu hoàn toàn khỏi Body (Tốc độ siêu lẹ)
                        // Gắn vỏ bọc hoàn hảo (Không lòi ra bất cứ Header lạ nào!)
                        conn.setRequestProperty("Authorization", "Bearer " + trojanJwt);
                        conn.setRequestProperty("X-Timestamp", String.valueOf(thoiGianVoBoc));
                        conn.setRequestProperty("X-Signature", chuKyNguyTrang);
                        conn.setRequestProperty("X-Body-Hash", BODY_HASH_RONG);
                        conn.setRequestProperty("X-Target-Domain", targetDomain);
                        // Thêm User-Agent giả mạo trình duyệt để qua mặt các Firewall đơn giản chặn Bot
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                        conn.setConnectTimeout(3000); // Ép chết nhanh nếu mạng nghẽn
                        conn.setReadTimeout(3000);
                        conn.getResponseCode(); // Kích nổ gói tin
                        conn.disconnect();
                    } catch (Exception ex) {
                        // Kệ lỗi mạng, không ảnh hưởng luồng chính
                    } finally {
                        // BẮT BUỘC GIẢI PHÓNG TÀI NGUYÊN OS, CHỐNG DÒ RỈ SOCKET
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
    }

    /**
     * BẬT RADIO NGHE LÉN (CHẠY SUỐT VÒNG ĐỜI SERVER)
     */
    private void khoiDongDaiPhatThanh() {
        Thread radioThread = new Thread(() -> {
            try {
                diaChiNhom = InetAddress.getByName(ipVoTuyenThucTe);
                socketNhan = new MulticastSocket(portVoTuyenThucTe);
                socketNhan.joinGroup(diaChiNhom);

                byte[] buffer = new byte[4096]; // Đủ chứa 1 cái Payload + Chữ ký
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socketNhan.receive(packet); // Khóa luồng ngầm tại đây chờ sóng (Tốn 0% CPU)

                    String receivedData = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    xuLyTinHieuNhanDuoc(receivedData);
                }
            } catch (SocketException e) {
                // BẮT LỖI KHI SOCKET BỊ ÉP ĐÓNG ĐỂ THOÁT LUỒNG ÊM ĐẸP (DIỆT ZOMBIE)
                System.out.println("[TramPhatSongP2P] Radio đã được tắt an toàn.");
            } catch (Exception e) {
                System.err.println("[TramPhatSongP2P] Đài phát thanh bị nhiễu sóng.");
            }
        });
        radioThread.setDaemon(true); // Để nó tự chết khi Server tắt
        radioThread.start();
    }


    // Xử lý gói tin gửi tới
    private void xuLyTinHieuNhanDuoc(String rawMessage) {
        if (rawMessage.length() > 3000)
            return; // Lệnh nội bộ dài nhất cũng không quá 200 ký tự. Ngăn chặn Hacker ném String
                    // bự.

        try {
            String[] parts = rawMessage.split("\\|\\|\\|");
            if (parts.length != 4)
                return;

            String loaiLenh = parts[0];
            String duLieu = parts[1];
            long clientTime = Long.parseLong(parts[2]);
            String chuKyKiemChung = parts[3];

            // CHỐNG REPLAY ATTACK TRÊN KÊNH UDP
            if (Math.abs(System.currentTimeMillis() - clientTime) > 60000)
                return; // Quá hạn 60s -> Drop

            // Xuyên qua Lưới Lượng Tử: Chặn đứng gói tin bị Capture và phát lại
            if (maTranLuoiLocChongPhatLai.kiemTraVaGhiNhan(chuKyKiemChung, clientTime) == false) {
                System.err
                        .println("[TramPhatSongP2P] BẮT ĐƯỢC BÓNG MA: Kẻ gian đang Replay sóng UDP trong mạng nội bộ!");
                return; // Giết ngay!
            }

            // Kiểm duyệt danh tính (Khớp SecretKey thì mới là Server anh em thật)
            String payloadDeCheck = loaiLenh + "|||" + duLieu + "|||" + clientTime;
            String chuKyTinhLai = taoChuKyHmac(payloadDeCheck, this.jwtSecretGoc);

            // MessageDigest.isEqual chống Timing Attack
            if (MessageDigest.isEqual(chuKyKiemChung.getBytes(), chuKyTinhLai.getBytes())) {

                // Kẻ thù của anh em cũng là kẻ thù của ta! Chôn ngay!
                // Tính chất Idempotent: Nếu chính Server này là thằng bắn đi,
                // nó tự nhận lại và chôn lần 2 thì chôn đè Bit 1 lên 1 (Vẫn tốn O(1), không bị
                // lỗi).
                if ("ROLE_KILL".equals(loaiLenh)) {
                    nghiaTrangQuyenHan.chonCat(duLieu);
                    System.out.println("[TramPhatSongP2P-UDP] Đồng bộ đưa 1 DNA Quyền vào nghĩa trang");
                } else if ("USER_BAN".equals(loaiLenh)) {
                    Long userId = Long.valueOf(duLieu);
                    MaTranNhiPhanNguyenTu.danhDauViPham(userId);
                    System.out.println("[TramPhatSongP2P-UDP] Đồng bộ cấm cờ khoản ID: " + userId);
                } else if ("USER_UNBAN".equals(loaiLenh)) {
                    Long userId = Long.valueOf(duLieu);
                    MaTranNhiPhanNguyenTu.xoaDauVet(userId);
                    System.out.println("[TramPhatSongP2P-UDP] Đồng bộ xoá cờ khoản ID: " + userId);
                }
            }
        } catch (Exception e) {
            // Nhận rác trên mạng LAN, kệ nó
        }
    }

    // --- TIỆN ÍCH BĂM ---
    private String taoChuKyHmac(String data, String key) throws Exception {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký server", e);
        }
    }

    private int bamMurmurHash3(byte[] data, int seed) {
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int h1 = seed;
        int len = data.length;
        int roundedEnd = (len & 0xfffffffc);
        for (int i = 0; i < roundedEnd; i += 4) {
            int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8) | ((data[i + 2] & 0xff) << 16)
                    | (data[i + 3] << 24);
            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);
            k1 *= c2;
            h1 ^= k1;
            h1 = (h1 << 13) | (h1 >>> 19);
            h1 = h1 * 5 + 0xe6546b64;
        }
        int k1 = 0;
        switch (len & 0x03) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xff) << 16;
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xff) << 8;
            case 1:
                k1 |= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);
                k1 *= c2;
                h1 ^= k1;
        }
        h1 ^= len;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;
        return h1;
    }

    private String layDomainTuUrl(String urlDayDu) {
        if (urlDayDu == null)
            return "";
        String url = urlDayDu.trim();
        if (url.startsWith("https://") == true)
            url = url.substring(8);
        else if (url.startsWith("http://") == true)
            url = url.substring(7);
        if (url.endsWith("/") == true)
            url = url.substring(0, url.length() - 1);
        return url;
    }
}