package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;
 
import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
 
/**
 * =====================================================================================
 * XƯỞNG SINH URL NGỤY TRANG (CAMOUFLAGE URL FACTORY)
 * =====================================================================================
 *
 * MỤC ĐÍCH TỒN TẠI:
 * Khi Server A cần gửi lệnh đồng bộ (Ban IP, Khai tử DNA Quyền...) sang Server B,
 * gói tin đó phải trông HOÀN TOÀN BÌNH THƯỜNG — không khác gì một request của User thật.
 *
 * NGUYÊN LÝ HOẠT ĐỘNG (LẬT NGƯỢC VẤN ĐỀ):
 * - Hacker biết gói tin đặc biệt vì nó KHÁC BIỆT (URL cố định, method lạ).
 * - Giải pháp: Làm nó BÌNH THƯỜNG HƠN CẢ BÌNH THƯỜNG.
 * - Gói tin ngụy trang sẽ mang đầy đủ chữ ký HMAC giống hệt JS gửi lên,
 *   để JwtAuthenticationFilter xử lý như một request hợp lệ thật sự.
 * - Lệnh đồng bộ thật được giấu BÊN TRONG các Header đặc biệt đã được mã hóa.
 *
 * TẠI SAO KHÔNG SỢ WAF/NGINX BLOCK:
 * - WAF chỉ block method nếu Dev cấu hình rule cụ thể (e.g. chỉ cho POST /login).
 * - Mặc định Nginx/Cloudflare KHÔNG chặn PUT/PATCH/DELETE trên URL ứng dụng.
 * - Ta tránh hoàn toàn GET (vì GET + URL tĩnh bị JwtFilter thả trôi).
 *
 * TẠI SAO ĐUÔI URL KHÔNG BỊ PHÁT HIỆN ENTROPY:
 * - Không dùng chuỗi ngẫu nhiên vô nghĩa (abc123xjq).
 * - Dùng các từ có ngữ nghĩa thật: ID dạng số, action word, page number...
 * - Các Firewall xịn phân tích Entropy của chuỗi vô nghĩa, không phân tích ngữ nghĩa.
 *
 * HẰNG SỐ RUNTIME (TÍNH 1 LẦN LÚC BOOT):
 * - POOL_URL_NGUYEN_MAU: Gộp từ 3 mảng staticUrls + publicUrls + protectedUrls.
 * - DANH_SACH_METHOD: Chỉ các method có body (POST, PUT, PATCH, DELETE).
 * - Không gộp mảng, không lọc lại ở mỗi request → O(1) hoàn toàn.
 *
 * =====================================================================================
 */
@Component
public class InterClusterSyncCamouflage {
 
    // =====================================================================================
    // HẰNG SỐ RUNTIME — TÍNH ĐÚNG 1 LẦN LÚC SERVER KHỞI ĐỘNG
    // =====================================================================================
 
    // Pool URL gốc đã được làm sạch (cắt bỏ **, /*, kết hợp từ cả 3 mảng)
    private final String[] POOL_URL_NGUYEN_MAU;
 
    // Danh sách method an toàn: Tránh GET (GET + URL tĩnh bị JwtFilter thả trôi)
    // Chỉ dùng method có Body để DynamicRoleFilter bắt và xác thực chữ ký HMAC
    private final String[] DANH_SACH_METHOD = {"POST", "PUT", "PATCH", "DELETE"};
 
    // Danh sách đuôi URL có nghĩa — giả thật, không phải chuỗi rác entropy cao
    // Gồm: ID dạng số, action word, tên hành động nghiệp vụ phổ biến
    private static final String[] POOL_DUOI_URL_CO_NGHIA = {
        // Dạng ID số (Cực kỳ phổ biến trong REST API thật)
        "/1", "/2", "/3", "/5", "/8", "/13", "/21",
        "/10", "/20", "/50", "/100", "/200",
        // Dạng hành động nghiệp vụ (Action words thật)
        "/update", "/delete", "/save", "/submit", "/confirm",
        "/cancel", "/approve", "/reject", "/process", "/execute",
        // Dạng sub-resource phổ biến trong REST
        "/detail", "/info", "/status", "/list", "/data",
        "/profile", "/setting", "/config", "/history", "/log",
        // Dạng page/sort (Query-like path)
        "/page/1", "/page/2", "/sort/asc", "/sort/desc",
        // Dạng UUID viết tắt (4-8 ký tự hex — hợp lý, không quá vô nghĩa)
        "/a1b2", "/c3d4", "/e5f6", "/7a8b", "/9c0d",
        "/1a2b3c", "/4d5e6f", "/7890ab", "/cdef01"
    };
 
    // SecureRandom dùng chung — Thread-safe, không tạo mới mỗi request
    private final SecureRandom RANDOM = new SecureRandom();
 
    // =====================================================================================
    // CONSTRUCTOR — KHỞI TẠO HẰNG SỐ 1 LẦN DUY NHẤT
    // =====================================================================================
    public InterClusterSyncCamouflage(SecurityLibraryProperties props) {
 
        // --- BƯỚC 1: GỘP 3 MẢNG URL THÀNH 1 POOL DUY NHẤT ---
        // Không gộp mỗi request, gộp đúng 1 lần ở đây và đóng băng thành hằng số.
        List<String> poolTamThoi = new ArrayList<>();
 
        //List<String> danhSachStatic    = props.getStaticUrls();
       //List<String> danhSachPublic    = props.getPublicUrls();
        List<String> danhSachProtected = props.getProtectedUrls();
 
        // Thêm URL tĩnh vào pool (Ví dụ: /css/, /js/, /images/)
       /*  for (int i = 0; i < danhSachStatic.size(); i = i + 1) {
            String urlSach = lamSachUrl(danhSachStatic.get(i));
            if (urlSach != null && urlSach.isEmpty() == false) {
                poolTamThoi.add(urlSach);
            }
        }
        */
       /*
        // Thêm URL công khai vào pool (Ví dụ: /login, /register)
        for (int i = 0; i < danhSachPublic.size(); i = i + 1) {
            String urlSach = lamSachUrl(danhSachPublic.get(i));
            if (urlSach != null && urlSach.isEmpty() == false) {
                poolTamThoi.add(urlSach);
            }
        }
        */
        // Thêm URL được bảo vệ vào pool (Ví dụ: /api, /admin, /checkout)
        for (int i = 0; i < danhSachProtected.size(); i = i + 1) {
            String urlSach = lamSachUrl(danhSachProtected.get(i));
            if (urlSach != null && urlSach.isEmpty() == false) {
                poolTamThoi.add(urlSach);
            }
        }
 
        // Nếu pool rỗng (Dev cấu hình thiếu), dùng fallback an toàn
        if (poolTamThoi.isEmpty() == true) {
            poolTamThoi.add("/api");
            poolTamThoi.add("/admin");
        }
 
        // Chuyển List sang Array để truy xuất O(1) bằng index, không tạo Iterator
        this.POOL_URL_NGUYEN_MAU = poolTamThoi.toArray(new String[0]);
 
        System.out.println("[InterClusterSyncCamouflage] Đã khởi tạo Pool URL Ngụy Trang: "
                + this.POOL_URL_NGUYEN_MAU.length + " URL gốc.");
        System.out.println("[InterClusterSyncCamouflage] Đã khởi tạo Pool Đuôi URL: "
                + POOL_DUOI_URL_CO_NGHIA.length + " lựa chọn đuôi.");
    }
 
    // =====================================================================================
    // HÀM CÔNG KHAI: SINH RA MỘT URL VÀ METHOD NGỤY TRANG
    // =====================================================================================
 
    /**
     * Sinh ra một cặp [Method, FullUrl] ngụy trang để gửi lệnh đồng bộ.
     *
     * QUY TẮC BẮT BUỘC:
     * 1. Method KHÔNG BAO GIỜ là GET (GET + URL tĩnh bị JwtFilter thả trôi).
     * 2. URL = [URL gốc từ pool] + [Đuôi có nghĩa] → trông như request REST thật.
     * 3. Mỗi lần gọi cho ra kết quả KHÁC NHAU (random cả URL lẫn Method).
     *
     * @return Mảng 2 phần tử: [0] = Method, [1] = Full URL
     */
    public String[] sinhCapMethodVaUrl() {
 
        // --- BƯỚC 1: RANDOM METHOD (Tránh GET) ---
        int viTriMethod = RANDOM.nextInt(DANH_SACH_METHOD.length);
        String methodNguyTrang = DANH_SACH_METHOD[viTriMethod];
 
        // --- BƯỚC 2: RANDOM URL GỐC TỪ POOL ---
        int viTriUrl = RANDOM.nextInt(POOL_URL_NGUYEN_MAU.length);
        String urlGoc = POOL_URL_NGUYEN_MAU[viTriUrl];
 
        // --- BƯỚC 3: RANDOM ĐUÔI URL CÓ NGỮ NGHĨA ---
        int viTriDuoi = RANDOM.nextInt(POOL_DUOI_URL_CO_NGHIA.length);
        String duoiUrl = POOL_DUOI_URL_CO_NGHIA[viTriDuoi];
 
        // --- BƯỚC 4: GHÉP URL HOÀN CHỈNH ---
        // Ví dụ: /api + /update → /api/update
        //        /admin + /5   → /admin/5
        //        /login + /confirm → /login/confirm
        String fullUrl = urlGoc + duoiUrl;
 
        return new String[]{methodNguyTrang, fullUrl};
    }
 
    /**
     * Tạo Body rỗng hợp lệ cho lệnh đồng bộ.
     * Body phải rỗng vì lệnh thật được giấu trong Header, không phải Body.
     * Giá trị trả về là SHA-256 của chuỗi rỗng — khớp với cách JS tính bodyHash.
     *
     * @return Mã SHA-256 của chuỗi rỗng (64 ký tự hex)
     */
    public static String tinhBodyHashRong() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // SHA-256 của chuỗi rỗng "" — hằng số toán học, không đổi
            byte[] ketQua = digest.digest("".getBytes(StandardCharsets.UTF_8));
 
            StringBuilder chuoiHex = new StringBuilder();
            for (int i = 0; i < ketQua.length; i = i + 1) {
                int giaTriDuong = 0xFF & ketQua[i];
                String hex = Integer.toHexString(giaTriDuong);
                if (hex.length() == 1) {
                    chuoiHex.append("0");
                }
                chuoiHex.append(hex);
            }
            return chuoiHex.toString();
 
        } catch (Exception e) {
            // SHA-256 là thuật toán chuẩn Java, không bao giờ throw exception trong thực tế
            // Nếu xảy ra là JVM bị hỏng nặng → trả về giá trị cứng đã biết trước
            return "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        }
    }
 
    // =====================================================================================
    // HÀM NỘI BỘ: LÀM SẠCH URL TỪ MẢNG CẤU HÌNH
    // =====================================================================================
 
    /**
     * Làm sạch URL từ mảng cấu hình để lấy phần gốc có thể dùng được.
     *
     * Các trường hợp cần xử lý:
     * - "/api/**"   → "/api"      (Cắt bỏ /**)
     * - "/css/*"    → "/css"      (Cắt bỏ /*)
     * - "/admin/"   → "/admin"    (Cắt bỏ / thừa cuối)
     * - "/login"    → "/login"    (Giữ nguyên)
     * - "/**"       → null        (Quá chung chung, loại bỏ)
     * - "/*"        → null        (Quá chung chung, loại bỏ)
     *
     * @param urlGoc URL thô từ mảng cấu hình
     * @return URL đã làm sạch, hoặc null nếu URL quá chung chung không dùng được
     */
    private String lamSachUrl(String urlGoc) {
        if (urlGoc == null) {
            return null;
        }
 
        String urlDangXuLy = urlGoc.trim();
 
        if (urlDangXuLy.isEmpty() == true) {
            return null;
        }
 
        // --- BƯỚC 1: CẮT BỎ PHẦN WILDCARD CUỐI ---
        // Xử lý "/**" → ""
        if (urlDangXuLy.endsWith("/**") == true) {
            urlDangXuLy = urlDangXuLy.substring(0, urlDangXuLy.length() - 3);
        }
        // Xử lý "/*" → ""
        else if (urlDangXuLy.endsWith("/*") == true) {
            urlDangXuLy = urlDangXuLy.substring(0, urlDangXuLy.length() - 2);
        }
        // Xử lý "*" ở cuối không có dấu / (Hiếm gặp nhưng phải xử lý)
        else if (urlDangXuLy.endsWith("*") == true) {
            urlDangXuLy = urlDangXuLy.substring(0, urlDangXuLy.length() - 1);
        }
 
        // --- BƯỚC 2: CẮT BỎ DẤU / THỪA CUỐI ---
        // "/admin/" → "/admin"
        // Nhưng không cắt nếu chỉ còn mỗi "/" (URL gốc)
        if (urlDangXuLy.length() > 1 && urlDangXuLy.endsWith("/") == true) {
            urlDangXuLy = urlDangXuLy.substring(0, urlDangXuLy.length() - 1);
        }
 
        // --- BƯỚC 3: LOẠI BỎ URL QUÁ CHUNG CHUNG ---
        // Nếu sau khi làm sạch chỉ còn "/" hoặc rỗng → không dùng được
        if (urlDangXuLy.isEmpty() == true || urlDangXuLy.equals("/") == true) {
            return null;
        }
 
        // --- BƯỚC 4: ĐẢM BẢO BẮT ĐẦU BẰNG "/" ---
        if (urlDangXuLy.startsWith("/") == false) {
            urlDangXuLy = "/" + urlDangXuLy;
        }
 
        return urlDangXuLy;
    }
 
}
 