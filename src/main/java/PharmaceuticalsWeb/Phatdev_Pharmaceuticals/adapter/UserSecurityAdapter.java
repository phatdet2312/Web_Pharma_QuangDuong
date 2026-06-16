//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/adapter/UserSecurityAdapter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.adapter;

import java.util.ArrayList;
import java.util.List;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.rbac.RbacSecuritySnapshot;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityAuthoritySnapshot;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

/**
 * Adapter app Pharma: bien User/RBAC thanh contract trung lap cho ultraSecureLibrary.
 */
public class UserSecurityAdapter implements ISecurityUserAdapter {

    private final User user;

    public UserSecurityAdapter(User user) {
        this.user = user;
    }

    @Override
    public Long layIdNguoiDung() {
        return this.user.getId();
    }

    @Override
    public String layTenDangNhap() {
        return this.user.getUsername();
    }

    @Override
    public String layEmail() {
        return this.user.getEmail();
    }

    @Override
    public String layTenDayDu() {
        return this.user.getFullName();
    }

    @Override
    public boolean kiemTraTaiKhoanBiKhoa() {
        return this.user.isLocked();
    }

    @Override
    public List<String> layDanhSachQuyen() {
        List<String> tatCaQuyen = new ArrayList<>();

        List<String> danhSachTenRole = this.user.getDanhSachTenRole();
        if (danhSachTenRole != null) {
            Object[] rolesArray = danhSachTenRole.toArray();
            for (int i = 0; i < rolesArray.length; i = i + 1) {
                tatCaQuyen.add(rolesArray[i].toString());
            }
        }

        List<String> danhSachTenPermission = this.user.getDanhSachTenPermission();
        if (danhSachTenPermission != null) {
            Object[] permsArray = danhSachTenPermission.toArray();
            for (int j = 0; j < permsArray.length; j = j + 1) {
                tatCaQuyen.add(permsArray[j].toString());
            }
        }

        return tatCaQuyen;
    }

    @Override
    public SecurityAuthoritySnapshot layAnhChupBaoMat() {
        return RbacSecuritySnapshot.taoSnapshot(this.user);
    }
}
