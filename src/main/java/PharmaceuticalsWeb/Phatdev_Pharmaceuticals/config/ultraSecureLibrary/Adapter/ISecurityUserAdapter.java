//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Adapter/ISecurityUserAdapter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter;

import java.util.List;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityAuthoritySnapshot;

/**
 * Mat na giao tiep voi moi ung dung chu.
 * Thu vien chi nhan anh chup bao mat typed, khong hieu nghiep vu phan quyen.
 */
public interface ISecurityUserAdapter {

    Long layIdNguoiDung();

    String layTenDangNhap();

    String layEmail();

    String layTenDayDu();

    /**
     * Hop dong cu cua phien ban chuan: danh sach quyen goc se duoc quy doi thanh ROLE_*.
     */
    List<String> layDanhSachQuyen();

    boolean kiemTraTaiKhoanBiKhoa();

    /**
     * Hop dong moi: app chu dong goi thong tin bao mat thanh snapshot typed.
     * Mac dinh giu nguyen luong cu bang layDanhSachQuyen().
     */
    default SecurityAuthoritySnapshot layAnhChupBaoMat() {
        return SecurityAuthoritySnapshot.tuDanhSachQuyenKieuCu(layDanhSachQuyen());
    }
}
