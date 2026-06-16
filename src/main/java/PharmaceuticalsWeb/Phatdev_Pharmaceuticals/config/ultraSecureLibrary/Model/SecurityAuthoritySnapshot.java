//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Model/SecurityAuthoritySnapshot.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Anh chup bao mat trung lap: thu vien chi thay authorities va fingerprint opaque.
 */
public class SecurityAuthoritySnapshot {

    private final List<String> authorities;
    private final List<SecurityTokenClaim> additionalClaims;
    private final String securityFingerprint;

    public SecurityAuthoritySnapshot(List<String> authorities, List<SecurityTokenClaim> additionalClaims,
            String securityFingerprint) {
        this.authorities = chuanHoaDanhSach(authorities);
        this.additionalClaims = saoChepClaims(additionalClaims);
        this.securityFingerprint = SecurityTokenVersion.chuanHoaFingerprint(securityFingerprint);
    }

    public static SecurityAuthoritySnapshot tuDanhSachQuyen(List<String> danhSachQuyen) {
        List<String> authorities = chuanHoaDanhSach(danhSachQuyen);
        String fingerprint = taoFingerprintTuAuthorities(authorities);
        return new SecurityAuthoritySnapshot(authorities, new ArrayList<>(), fingerprint);
    }

    public static SecurityAuthoritySnapshot tuDanhSachQuyenKieuCu(List<String> danhSachQuyen) {
        List<String> authorities = new ArrayList<>();
        if (danhSachQuyen != null) {
            Object[] mangQuyen = danhSachQuyen.toArray();
            for (int i = 0; i < mangQuyen.length; i = i + 1) {
                Object raw = mangQuyen[i];
                if (raw != null) {
                    String rawRole = raw.toString().trim();
                    if (rawRole.isEmpty() == false) {
                        String cleanRole = rawRole.replace("ROLE_", "");
                        themNeuChuaCo(authorities, "ROLE_" + cleanRole);
                    }
                }
            }
        }
        String fingerprint = taoFingerprintTuAuthorities(authorities);
        return new SecurityAuthoritySnapshot(authorities, new ArrayList<>(), fingerprint);
    }

    public List<String> layAuthoritiesChuanHoa() {
        return chuanHoaDanhSach(authorities);
    }

    public List<SecurityTokenClaim> layClaimsBoSung() {
        return saoChepClaims(additionalClaims);
    }

    public String laySecurityFingerprint() {
        return securityFingerprint;
    }

    public static List<String> chuanHoaDanhSach(List<String> danhSachGoc) {
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

    private static String taoFingerprintTuAuthorities(List<String> authorities) {
        List<String> danhSachChuan = chuanHoaDanhSach(authorities);
        return "authorities=" + danhSachChuan.toString();
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

    private static List<SecurityTokenClaim> saoChepClaims(List<SecurityTokenClaim> claims) {
        List<SecurityTokenClaim> ketQua = new ArrayList<>();
        if (claims == null) {
            return ketQua;
        }
        Object[] mangClaim = claims.toArray();
        for (int i = 0; i < mangClaim.length; i = i + 1) {
            Object raw = mangClaim[i];
            if (raw instanceof SecurityTokenClaim) {
                ketQua.add((SecurityTokenClaim) raw);
            }
        }
        return ketQua;
    }
}
