//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Service/PhienBanPhanQuyenBaoMat.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chuẩn hóa ảnh chụp phân quyền để JWT, nghĩa trang DNA và bộ lọc động dùng chung một định dạng.
 */
public class PhienBanPhanQuyenBaoMat {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_ROLE_LEVEL = "roleLevel";
    public static final String CLAIM_BLACKLIST = "blacklist";
    public static final String ATTR_ROLE_LEVEL = "JWT_ROLE_LEVEL";

    /**
     * Sao chép, loại trùng và sắp xếp danh sách để DNA không phụ thuộc thứ tự query DB.
     */
    public static List<String> chuanHoaDanhSach(List<?> danhSachGoc) {
        List<String> danhSachChuan = new ArrayList<>();
        if (danhSachGoc == null) {
            return danhSachChuan;
        }

        Object[] mangGiaTri = danhSachGoc.toArray();
        for (int i = 0; i < mangGiaTri.length; i = i + 1) {
            Object giaTriRaw = mangGiaTri[i];
            if (giaTriRaw != null) {
                themGiaTriNeuHopLe(danhSachChuan, giaTriRaw.toString());
            }
        }

        Collections.sort(danhSachChuan);
        return danhSachChuan;
    }

    /**
     * Chuẩn hóa roleLevel để mọi nơi cùng hiểu tài khoản không có cấp là cấp yếu nhất.
     */
    public static Integer chuanHoaCapBac(Integer roleLevel) {
        if (roleLevel == null) {
            return 999;
        }
        return roleLevel;
    }

    /**
     * Chuyển claim số về Integer an toàn cho cả JWT parser trả Integer, Long hoặc String.
     */
    public static Integer docSoNguyen(Object giaTriRaw, Integer giaTriMacDinh) {
        if (giaTriRaw == null) {
            return giaTriMacDinh;
        }
        if (giaTriRaw instanceof Number) {
            Number number = (Number) giaTriRaw;
            return Integer.valueOf(number.intValue());
        }
        try {
            return Integer.valueOf(giaTriRaw.toString());
        } catch (Exception e) {
            return giaTriMacDinh;
        }
    }

    /**
     * Tạo lõi DNA từ bốn mảnh phân quyền đã chuẩn hóa.
     */
    public static String taoLoiDna(Long userId, List<String> roles, List<String> permissions,
            Integer roleLevel, List<String> blacklist) {
        return userId + "|roles=" + roles.toString()
                + "|permissions=" + permissions.toString()
                + "|roleLevel=" + chuanHoaCapBac(roleLevel)
                + "|blacklist=" + blacklist.toString();
    }

    /**
     * Tạo DNA hoàn chỉnh có kèm phiên bản v_adn để né các DNA đã bị chôn.
     */
    public static String taoDna(Long userId, List<String> roles, List<String> permissions,
            Integer roleLevel, List<String> blacklist, Integer vAdn) {
        return taoLoiDna(userId, roles, permissions, roleLevel, blacklist) + "|v" + docSoNguyen(vAdn, 0);
    }

    /**
     * So sánh hai ảnh chụp phân quyền đã chuẩn hóa.
     */
    public static boolean trungPhienBan(List<String> rolesA, List<String> permissionsA, Integer roleLevelA,
            List<String> blacklistA, List<String> rolesB, List<String> permissionsB, Integer roleLevelB,
            List<String> blacklistB) {
        return rolesA.equals(rolesB)
                && permissionsA.equals(permissionsB)
                && chuanHoaCapBac(roleLevelA).equals(chuanHoaCapBac(roleLevelB))
                && blacklistA.equals(blacklistB);
    }

    /**
     * Kiểm tra token đã mang đủ bốn mảnh snapshot phân quyền hay chưa.
     */
    public static boolean coDuBonManh(Object permissions, Object roleLevel, Object blacklist) {
        return permissions != null && roleLevel != null && blacklist != null;
    }

    /**
     * Thêm giá trị hợp lệ vào danh sách, tránh trùng lặp bằng vòng lặp rõ ràng.
     */
    private static void themGiaTriNeuHopLe(List<String> danhSach, String giaTriRaw) {
        if (giaTriRaw == null) {
            return;
        }
        String giaTri = giaTriRaw.trim();
        if (giaTri.isEmpty() == true) {
            return;
        }
        Object[] mangGiaTri = danhSach.toArray();
        for (int i = 0; i < mangGiaTri.length; i = i + 1) {
            if (giaTri.equals(mangGiaTri[i].toString())) {
                return;
            }
        }
        danhSach.add(giaTri);
    }
}
