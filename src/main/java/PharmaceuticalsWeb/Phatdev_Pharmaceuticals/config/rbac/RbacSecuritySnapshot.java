//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/rbac/RbacSecuritySnapshot.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.rbac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityAuthoritySnapshot;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenClaim;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

/**
 * App-level RBAC snapshot: chi lop nay hieu roles, permissions, roleLevel va blacklist.
 */
public final class RbacSecuritySnapshot {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_ROLE_LEVEL = "roleLevel";
    public static final String CLAIM_BLACKLIST = "blacklist";
    public static final String ATTR_ROLE_LEVEL_LEGACY = "JWT_ROLE_LEVEL";

    private RbacSecuritySnapshot() {
    }

    public static SecurityAuthoritySnapshot taoSnapshot(User user) {
        List<String> roles = chuanHoaDanhSach(user.getDanhSachTenRole());
        List<String> permissions = chuanHoaDanhSach(user.getDanhSachTenPermission());
        List<String> blacklist = chuanHoaDanhSach(user.getDanhSachTenPermissionBlacklist());
        Integer roleLevel = chuanHoaCapBac(user.getCapBacQuyenLuc());

        List<String> authorities = new ArrayList<>();
        themAuthoritiesRole(authorities, roles);
        themAuthoritiesPermission(authorities, permissions);

        List<SecurityTokenClaim> claims = new ArrayList<>();
        claims.add(SecurityTokenClaim.danhSachChuoi(CLAIM_ROLES, roles, false));
        claims.add(SecurityTokenClaim.danhSachChuoi(CLAIM_PERMISSIONS, permissions, false));
        claims.add(SecurityTokenClaim.so(CLAIM_ROLE_LEVEL, Long.valueOf(roleLevel.longValue()), true));
        claims.add(SecurityTokenClaim.so(ATTR_ROLE_LEVEL_LEGACY, Long.valueOf(roleLevel.longValue()), true));
        claims.add(SecurityTokenClaim.danhSachChuoi(CLAIM_BLACKLIST, blacklist, false));

        String fingerprint = taoFingerprint(roles, permissions, roleLevel, blacklist);
        return new SecurityAuthoritySnapshot(authorities, claims, fingerprint);
    }

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

    private static String taoFingerprint(List<String> roles, List<String> permissions, Integer roleLevel,
            List<String> blacklist) {
        return "roles=" + roles.toString()
                + "|permissions=" + permissions.toString()
                + "|roleLevel=" + chuanHoaCapBac(roleLevel)
                + "|blacklist=" + blacklist.toString();
    }

    private static void themAuthoritiesRole(List<String> authorities, List<String> roles) {
        Object[] mangRole = roles.toArray();
        for (int i = 0; i < mangRole.length; i = i + 1) {
            String rawRole = mangRole[i].toString();
            String cleanRole = rawRole.replace("ROLE_", "");
            themNeuChuaCo(authorities, "ROLE_" + cleanRole);
        }
    }

    private static void themAuthoritiesPermission(List<String> authorities, List<String> permissions) {
        Object[] mangPermission = permissions.toArray();
        for (int i = 0; i < mangPermission.length; i = i + 1) {
            themNeuChuaCo(authorities, mangPermission[i].toString());
        }
    }

    private static List<String> chuanHoaDanhSach(List<String> danhSachGoc) {
        List<String> ketQua = new ArrayList<>();
        if (danhSachGoc == null) {
            return ketQua;
        }

        Object[] mangGiaTri = danhSachGoc.toArray();
        for (int i = 0; i < mangGiaTri.length; i = i + 1) {
            Object raw = mangGiaTri[i];
            if (raw != null) {
                themNeuChuaCo(ketQua, raw.toString());
            }
        }

        Collections.sort(ketQua);
        return ketQua;
    }

    private static Integer chuanHoaCapBac(Integer roleLevel) {
        if (roleLevel == null) {
            return 999;
        }
        return roleLevel;
    }

    private static void themNeuChuaCo(List<String> danhSach, String giaTriRaw) {
        if (giaTriRaw == null) {
            return;
        }
        String giaTri = giaTriRaw.trim();
        if (giaTri.isEmpty()) {
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
