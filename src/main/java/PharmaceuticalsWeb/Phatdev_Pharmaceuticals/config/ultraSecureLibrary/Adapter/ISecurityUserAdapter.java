//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Adapter/ISecurityUserAdapter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter;

import java.util.List;

/**
 * MẶT NẠ GIAO TIẾP VỚI MỌI CƠ SỞ DỮ LIỆU
 * Bất kỳ web nào dùng thư viện này phải tạo 1 class implements interface này.
 */
public interface ISecurityUserAdapter {
    Long layIdNguoiDung(); // ID bắt buộc quy về Long để nhét vào Ma Trận
    String layTenDangNhap();
    String layEmail();
    String layTenDayDu();
    List<String> layDanhSachQuyen();
    boolean kiemTraTaiKhoanBiKhoa();
}