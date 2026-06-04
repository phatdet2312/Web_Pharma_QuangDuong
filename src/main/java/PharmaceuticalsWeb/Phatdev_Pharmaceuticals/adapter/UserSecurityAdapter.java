//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/adapter/UserSecurityAdapter.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.adapter;

import java.util.List;
import java.util.ArrayList;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

/**
 * =========================================================================
 * LỚP ADAPTER BẢO MẬT (THÔNG DỊCH VIÊN)
 * =========================================================================
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
        
        // 1. Nạp danh sách Tên Nhóm Quyền (Roles)
        List<String> danhSachTenRole = this.user.getDanhSachTenRole();
        if (danhSachTenRole != null) {
            // TUÂN THỦ QUY TẮC: Ép mảng và lặp For nguyên thủy
            Object[] rolesArray = danhSachTenRole.toArray();
            for (int i = 0; i < rolesArray.length; i = i + 1) {
                tatCaQuyen.add(rolesArray[i].toString());
            }
        }
        
        // 2. Nạp danh sách Tên Quyền Hạt Lựu (Permissions)
        List<String> danhSachTenPermission = this.user.getDanhSachTenPermission();
        if (danhSachTenPermission != null) {
            // TUÂN THỦ QUY TẮC: Ép mảng và lặp For nguyên thủy
            Object[] permsArray = danhSachTenPermission.toArray();
            for (int j = 0; j < permsArray.length; j = j + 1) {
                tatCaQuyen.add(permsArray[j].toString());
            }
        }
        
        return tatCaQuyen;
    }

    @Override
    public List<String> layDanhSachChucVu() {
        return this.user.getDanhSachTenRole();
    }

    @Override
    public List<String> layDanhSachQuyenThaoTac() {
        return this.user.getDanhSachTenPermission();
    }

    @Override
    public Integer layCapBacQuyenLuc() {
        if (this.user.getCapBacQuyenLuc() == null) {
            return 999;
        }
        return this.user.getCapBacQuyenLuc();
    }

    @Override
    public List<String> layDanhSachQuyenBiChan() {
        return this.user.getDanhSachTenPermissionBlacklist();
    }
}
