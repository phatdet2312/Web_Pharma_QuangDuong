//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Adapter/ISecurityUserAdapter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter;

import java.util.Collections;
import java.util.List;

/**
 * Mặt nạ giao tiếp giữa thư viện bảo mật và ứng dụng chủ.
 */
public interface ISecurityUserAdapter {

    Long layIdNguoiDung();

    String layTenDangNhap();

    String layEmail();

    String layTenDayDu();

    /**
     * Hợp đồng cũ: danh sách quyền tổng hợp.
     */
    List<String> layDanhSachQuyen();

    boolean kiemTraTaiKhoanBiKhoa();

    /**
     * Danh sách chức vụ của người dùng. Mặc định giữ tương thích với contract cũ.
     */
    default List<String> layDanhSachChucVu() {
        return layDanhSachQuyen();
    }

    /**
     * Danh sách quyền thao tác hiệu lực, không có tiền tố ROLE_.
     */
    default List<String> layDanhSachQuyenThaoTac() {
        return Collections.emptyList();
    }

    /**
     * Cấp bậc mạnh nhất của người dùng. Số càng nhỏ quyền càng mạnh.
     */
    default Integer layCapBacQuyenLuc() {
        return 999;
    }

    /**
     * Danh sách quyền thao tác đang bị blacklist riêng cho người dùng.
     */
    default List<String> layDanhSachQuyenBiChan() {
        return Collections.emptyList();
    }
}
