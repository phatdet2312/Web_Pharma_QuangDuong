//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Model/SecurityTokenClaim.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Claim bo sung duoc ung dung chu khai bao ro kieu truoc khi dua vao JWT.
 */
public class SecurityTokenClaim {

    private final String claimName;
    private final SecurityClaimType claimType;
    private final String stringValue;
    private final Long longValue;
    private final Boolean booleanValue;
    private final List<String> stringListValue;
    private final boolean exposeAsRequestAttribute;

    private SecurityTokenClaim(String claimName, SecurityClaimType claimType, String stringValue, Long longValue,
            Boolean booleanValue, List<String> stringListValue, boolean exposeAsRequestAttribute) {
        if (claimName == null || claimName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten claim bao mat khong duoc de trong");
        }
        if (claimType == null) {
            throw new IllegalArgumentException("Kieu claim bao mat khong duoc de trong");
        }
        this.claimName = claimName.trim();
        this.claimType = claimType;
        this.stringValue = stringValue;
        this.longValue = longValue;
        this.booleanValue = booleanValue;
        this.stringListValue = saoChepDanhSach(stringListValue);
        this.exposeAsRequestAttribute = exposeAsRequestAttribute;
    }

    public static SecurityTokenClaim chuoi(String claimName, String value, boolean exposeAsRequestAttribute) {
        return new SecurityTokenClaim(claimName, SecurityClaimType.STRING, value, null, null, null,
                exposeAsRequestAttribute);
    }

    public static SecurityTokenClaim so(String claimName, Long value, boolean exposeAsRequestAttribute) {
        return new SecurityTokenClaim(claimName, SecurityClaimType.LONG, null, value, null, null,
                exposeAsRequestAttribute);
    }

    public static SecurityTokenClaim bool(String claimName, Boolean value, boolean exposeAsRequestAttribute) {
        return new SecurityTokenClaim(claimName, SecurityClaimType.BOOLEAN, null, null, value, null,
                exposeAsRequestAttribute);
    }

    public static SecurityTokenClaim danhSachChuoi(String claimName, List<String> value,
            boolean exposeAsRequestAttribute) {
        return new SecurityTokenClaim(claimName, SecurityClaimType.STRING_LIST, null, null, null, value,
                exposeAsRequestAttribute);
    }

    public String getClaimName() {
        return claimName;
    }

    public SecurityClaimType getClaimType() {
        return claimType;
    }

    public boolean isExposeAsRequestAttribute() {
        return exposeAsRequestAttribute;
    }

    public String layGiaTriChuoi() {
        return stringValue;
    }

    public Long layGiaTriSo() {
        return longValue;
    }

    public Boolean layGiaTriBoolean() {
        return booleanValue;
    }

    public List<String> layGiaTriDanhSachChuoi() {
        return saoChepDanhSach(stringListValue);
    }

    private static List<String> saoChepDanhSach(List<String> danhSachGoc) {
        List<String> ketQua = new ArrayList<>();
        if (danhSachGoc == null) {
            return ketQua;
        }
        Object[] mangGiaTri = danhSachGoc.toArray();
        for (int i = 0; i < mangGiaTri.length; i = i + 1) {
            Object raw = mangGiaTri[i];
            if (raw != null) {
                ketQua.add(raw.toString());
            }
        }
        return ketQua;
    }
}
