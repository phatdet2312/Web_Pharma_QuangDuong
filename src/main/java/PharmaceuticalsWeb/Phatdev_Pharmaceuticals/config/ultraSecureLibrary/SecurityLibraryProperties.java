//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/SecurityLibraryProperties.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Component
@ConfigurationProperties(prefix = "htphat.security")
public class SecurityLibraryProperties {

    // --- CÁC THÔNG SỐ TÙY BIẾN ĐƯỢC MAPPING TỪ application.properties ---

    // --- DANH SÁCH API CÔNG KHAI (hệ thống sẽ bỏ qua kiểm duyệt các api này) nếu
    // không có jwt token ---
    private List<String> publicUrls = Arrays.asList("/login", "/register", "/api/auth/");
    // Danh sách URL cấu hình LibraryWebMvcAutoConfig (hệ thống sẽ đặt ống ngầm để
    // kiểm soát đối với các lệnh là file)
    private List<String> protectedUrls = Arrays.asList("/api/**", "/admin/**", "/checkout/**");
    // danh sách Tài nguyên tĩnh (hệ thống sẽ bỏ qua kiểm duyệt các api này nếu là
    // get)
    private List<String> staticUrls = Arrays.asList("/css/", "/js/", "/images/", "/favicon.ico");
    
    // =========================================================================
    // [NÂNG CẤP MỚI] DANH SÁCH API TẢI TRỌNG LỚN (LARGE PAYLOAD)
    // Phục vụ các nghiệp vụ gửi chuỗi văn bản siêu dài (VD: Prompt LLM, Base64 Docs).
    // Giúp nới lỏng giới hạn 10KB mà không cần phải chuyển sang định dạng Multipart.
    // =========================================================================
    private List<String> largePayloadUrls = Arrays.asList("/api/ai/analyze", "/api/ai/stream-analyze", "/api/ai-nexus/");
    
    // Danh sách Tên miền (Domain) Frontend/Client được phép gửi gói tin đến Server
    // này
    // Dev có thể cấu hình trong application.properties:
    // htphat.security.trusted-domains=phatdet2312.id.vn, localhost:3000
    private List<String> trustedDomains = Arrays.asList("localhost:3000", "localhost:11434", "phatdet2312.id.vn","localhost:8080", "quangduong.olutech.net"); 

    // Đường dẫn báo lỗi ra trang login
    private String loginUrl = "/login?locked"; // Mặc định

    private long limitJsonFormKb = 5000;// Mặc định là 10KB (Đã sửa demo thành 5000)
    private long limitMultipartMb = 50;// Mặc định là 50MB
    private long limitLargePayloadMb = 5;// Mặc định là 5MB cho các URL tải trọng lớn

    // --- RATE LIMIT TOMCAT VALVE ---
    private int rateLimitPerSec = 100;// Mặc định là 20 Request / Giây / IP
    private int rateLimitBanThreshold = 25;// Mặc định là 5 lần vi phạm sẽ bị khóa
    private long rateLimitBanTimeMs = 60000;// Mặc định là 60 giây
    private long rateLimitIdleTimeoutMs = 600000;// Mặc định là 10 phút không hoạt động -> Xóa IP
    private long rateLimitCleanupMs = 300000;// Mặc định là 5 phút chạy dọn dẹp 1 lần
    private int rateLimitMaxMapSize = 100000;// Mặc định là 100.000 IP trong RAM
    // Giới hạn thời gian tối đa để đọc hết Body (Chống Slowloris ngâm luồng)
    private long maxRequestReadTimeMs = 60000; // Mặc định 60 giây

    // --- CẤU HÌNH MA TRAN CHỐNG PHÁT LẠI (REPLAY ATTACK) ---
    private long replayWindowMs = 60000; // Mặc định 60 giây
    // ví dụ: Mỗi giây được cấp 32KB RAM để nhớ chữ ký tương đương ~20k requess/giây
    private int bloomSizeKbMoiKhe = 32;

    // --- CẤU HÌNH MA TRẬN NGHĨA TRANG TOKEN (TOKEN GRAVEYARD) ---
    // Cấp RAM cho Nghĩa trang Token (Mặc định 32KB ~ lưu trữ hàng triệu Token bị
    // hủy mỗi ngày)
    private int graveyardBloomSizeKbMoiKhe = 32;

    // --- JWT ---
    private String jwtSecret = "DayLaMotSecretKeyRatDaiVaAnToanChoHS256JwtToken2025!";// Mặc định
    private long jwtExpirationMs = 604800000; // Mặc định là 1 tuần (7 ngày)
    private String jwtIssuer = "HtPhat_Library";// Mặc định
    private String jwtAudience = "WebClient";// Mặc định

    // --- COOKIE ---
    private String cookieName = "JWT_TOKEN"; // Mặc định
    private boolean cookieSecure = false;// Mặc định là false khi chạy HTTPS (SSL) đôi thành true khi chạy HTTPS
    // Giới hạn phạm vi Cookie
    // Mặc định là "/", nhưng Dev có thể cấu hình htphat.security.cookie-path=/api/
    private String cookiePath = "/";

    // --- PROXY ---
    private String trustedProxyPrefix = "10.";

    // --- CẤU HÌNH LIÊN MINH BẦY ĐÀN (FEDERATED SWARM - P2P) ---
    // Để trống -> Thư viện tự động tính toán dựa trên Thư mục Code + JwtSecret
    private String clusterId = "";
    private String multicastIp = "";
    private int multicastPort = 0;

    // --- CẤU HÌNH LIÊN CỤM XUYÊN LỤC ĐỊA (OPTIONAL - CHO DEV KHÔNG DÙNG REDIS) ---
    private boolean enableInterClusterSync = true; // Mặc định TẮT. Để Dev tự quyết định.
    private List<String> remoteClusterUrls = new ArrayList<>(); // Danh sách URL các cụm khác
    private List<String> trustedSyncIps = new ArrayList<>(); // Danh sách IP được phép gửi lệnh Sync

    // --- GETTER VÀ SETTER ---
    // (Bắt buộc phải có để Spring Boot tự động map dữ liệu)

    public List<String> getPublicUrls() { return publicUrls; }
    public void setPublicUrls(List<String> publicUrls) { this.publicUrls = publicUrls; }

    public List<String> getProtectedUrls() { return protectedUrls; }
    public void setProtectedUrls(List<String> protectedUrls) { this.protectedUrls = protectedUrls; }

    public List<String> getStaticUrls() { return staticUrls; }
    public void setStaticUrls(List<String> staticUrls) { this.staticUrls = staticUrls; }

    public List<String> getLargePayloadUrls() { return largePayloadUrls; }
    public void setLargePayloadUrls(List<String> largePayloadUrls) { this.largePayloadUrls = largePayloadUrls; }

    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

    public List<String> getTrustedDomains() { return trustedDomains; }
    public void setTrustedDomains(List<String> trustedDomains) { this.trustedDomains = trustedDomains; }

    public long getLimitJsonFormKb() { return limitJsonFormKb; }
    public void setLimitJsonFormKb(long limitJsonFormKb) { this.limitJsonFormKb = limitJsonFormKb; }

    public long getLimitMultipartMb() { return limitMultipartMb; }
    public void setLimitMultipartMb(long limitMultipartMb) { this.limitMultipartMb = limitMultipartMb; }

    public long getLimitLargePayloadMb() { return limitLargePayloadMb; }
    public void setLimitLargePayloadMb(long limitLargePayloadMb) { this.limitLargePayloadMb = limitLargePayloadMb; }

    public int getRateLimitPerSec() { return rateLimitPerSec; }
    public void setRateLimitPerSec(int rateLimitPerSec) { this.rateLimitPerSec = rateLimitPerSec; }

    public int getRateLimitBanThreshold() { return rateLimitBanThreshold; }
    public void setRateLimitBanThreshold(int rateLimitBanThreshold) { this.rateLimitBanThreshold = rateLimitBanThreshold; }

    public long getRateLimitBanTimeMs() { return rateLimitBanTimeMs; }
    public void setRateLimitBanTimeMs(long rateLimitBanTimeMs) { this.rateLimitBanTimeMs = rateLimitBanTimeMs; }

    public long getRateLimitIdleTimeoutMs() { return rateLimitIdleTimeoutMs; }
    public void setRateLimitIdleTimeoutMs(long rateLimitIdleTimeoutMs) { this.rateLimitIdleTimeoutMs = rateLimitIdleTimeoutMs; }

    public long getRateLimitCleanupMs() { return rateLimitCleanupMs; }
    public void setRateLimitCleanupMs(long rateLimitCleanupMs) { this.rateLimitCleanupMs = rateLimitCleanupMs; }

    public int getRateLimitMaxMapSize() { return rateLimitMaxMapSize; }
    public void setRateLimitMaxMapSize(int rateLimitMaxMapSize) { this.rateLimitMaxMapSize = rateLimitMaxMapSize; }

    public long getMaxRequestReadTimeMs() { return maxRequestReadTimeMs; }
    public void setMaxRequestReadTimeMs(long maxRequestReadTimeMs) { this.maxRequestReadTimeMs = maxRequestReadTimeMs; }

    public long getReplayWindowMs() { return replayWindowMs; }
    public void setReplayWindowMs(long replayWindowMs) { this.replayWindowMs = replayWindowMs; }

    public int getBloomSizeKbMoiKhe() { return bloomSizeKbMoiKhe; }
    public void setBloomSizeKbMoiKhe(int bloomSizeKbMoiKhe) { this.bloomSizeKbMoiKhe = bloomSizeKbMoiKhe; }

    public int getGraveyardBloomSizeKbMoiKhe() { return graveyardBloomSizeKbMoiKhe; }
    public void setGraveyardBloomSizeKbMoiKhe(int graveyardBloomSizeKbMoiKhe) { this.graveyardBloomSizeKbMoiKhe = graveyardBloomSizeKbMoiKhe; }

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

    public long getJwtExpirationMs() { return jwtExpirationMs; }
    public void setJwtExpirationMs(long jwtExpirationMs) { this.jwtExpirationMs = jwtExpirationMs; }

    public String getJwtIssuer() { return jwtIssuer; }
    public void setJwtIssuer(String jwtIssuer) { this.jwtIssuer = jwtIssuer; }

    public String getJwtAudience() { return jwtAudience; }
    public void setJwtAudience(String jwtAudience) { this.jwtAudience = jwtAudience; }

    public String getCookieName() { return cookieName; }
    public void setCookieName(String cookieName) { this.cookieName = cookieName; }

    public boolean isCookieSecure() { return cookieSecure; }
    public void setCookieSecure(boolean cookieSecure) { this.cookieSecure = cookieSecure; }

    public String getCookiePath() { return cookiePath; }
    public void setCookiePath(String cookiePath) { this.cookiePath = cookiePath; }

    public String getTrustedProxyPrefix() { return trustedProxyPrefix; }
    public void setTrustedProxyPrefix(String trustedProxyPrefix) { this.trustedProxyPrefix = trustedProxyPrefix; }

    public String getClusterId() { return clusterId; }
    public void setClusterId(String clusterId) { this.clusterId = clusterId; }

    public String getMulticastIp() { return multicastIp; }
    public void setMulticastIp(String multicastIp) { this.multicastIp = multicastIp; }

    public int getMulticastPort() { return multicastPort; }
    public void setMulticastPort(int multicastPort) { this.multicastPort = multicastPort; }

    public boolean isEnableInterClusterSync() { return enableInterClusterSync; }
    public void setEnableInterClusterSync(boolean enableInterClusterSync) { this.enableInterClusterSync = enableInterClusterSync; }

    public List<String> getRemoteClusterUrls() { return remoteClusterUrls; }
    public void setRemoteClusterUrls(List<String> remoteClusterUrls) { this.remoteClusterUrls = remoteClusterUrls; }

    public List<String> getTrustedSyncIps() { return trustedSyncIps; }
    public void setTrustedSyncIps(List<String> trustedSyncIps) { this.trustedSyncIps = trustedSyncIps; }

    public int timLuyThua(long x) {
        int n = 1;
        while (n < x) {
            n = n << 1;
        }
        return n;
    }

    // --- HÀM CÔNG CỤ DÙNG CHUNG CHO THƯ VIỆN ---
    public boolean kiemTraLaUrlCongKhai(String uri) {
        if (uri == null) {
            return false;
        }
        for (int i = 0; i < publicUrls.size(); i = i + 1) {
            if (uri.startsWith(publicUrls.get(i)) == true) {
                return true;
            }
        }
        return false;
    }

    public boolean kiemTraLaUrlCanBaoVe(String uri) {
        if (uri == null) {
            return false;
        }
        for (int i = 0; i < protectedUrls.size(); i = i + 1) {
            if (uri.startsWith(protectedUrls.get(i)) == true) {
                return true;
            }
        }
        return false;
    }

    public boolean kiemTraLaUrlTinh(String uri) {
        if (uri == null) {
            return false;
        }
        for (int i = 0; i < staticUrls.size(); i = i + 1) {
            if (uri.startsWith(staticUrls.get(i)) == true) {
                return true;
            }
        }
        return false;
    }

    // Hàm nhận diện URL được phép truyền tải trọng lớn (Large Payload)
    public boolean kiemTraLaUrlTaiTrongLon(String uri) {
        if (uri == null) {
            return false;
        }
        for (int i = 0; i < largePayloadUrls.size(); i = i + 1) {
            if (uri.startsWith(largePayloadUrls.get(i)) == true) {
                return true;
            }
        }
        return false;
    }

    public boolean kiemTraDomainHopLe(String clientDomain) {
        if (clientDomain == null || clientDomain.trim().isEmpty()) {
            return false;
        }
        String cleanDomain = clientDomain.trim().toLowerCase();
        for (int i = 0; i < trustedDomains.size(); i++) {
            if (cleanDomain.equals(trustedDomains.get(i).trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean kiemTraIpDongBoHopLe(String ip) {
        if (ip == null || ip.trim().isEmpty()) return false;
        if (trustedSyncIps == null || trustedSyncIps.isEmpty()) return true;

        String cleanIp = ip.trim();
        for (int i = 0; i < trustedSyncIps.size(); i++) {
            if (cleanIp.equals(trustedSyncIps.get(i).trim())) {
                return true;
            }
        }
        return false;
    }

    public String dauAnLanhDia() {
        if (clusterId == null || clusterId.trim().isEmpty()) {
            String thuMucCode = System.getProperty("user.dir");
            String adnRaw = thuMucCode + "|||" + this.jwtSecret;

            this.clusterId = Integer.toHexString(Math.abs(bamMurmurHash3(adnRaw.getBytes(), 101)))
                    + Integer.toHexString(Math.abs(bamMurmurHash3(adnRaw.getBytes(), 202)));
        }
        return clusterId;
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
}