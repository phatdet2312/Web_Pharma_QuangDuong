//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Model/SecurityTokenVersion.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model;

/**
 * Xu ly fingerprint opaque cua token, khong hieu y nghia nghiep vu ben trong.
 */
public final class SecurityTokenVersion {

    private static final String EMPTY_FINGERPRINT = "EMPTY_SECURITY_STATE";

    private SecurityTokenVersion() {
    }

    public static String chuanHoaFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.trim().isEmpty()) {
            return EMPTY_FINGERPRINT;
        }
        return fingerprint.trim();
    }

    public static String taoDna(Long userId, String fingerprint, Integer vAdn) {
        return userId + "|" + chuanHoaFingerprint(fingerprint) + "|v" + docSoNguyen(vAdn, 0);
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
}
