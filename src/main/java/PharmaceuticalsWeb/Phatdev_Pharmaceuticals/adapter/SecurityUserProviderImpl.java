//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/adapter/SecurityUserProviderImpl.java


package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Provider.ISecurityUserProvider;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

// XÓA IUserRepository. BẮT BUỘC gọi IUserService để kích hoạt hàm nạp 6 bảng
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;

/**
 * =========================================================================
 * BỘ CUNG CẤP NGƯỜI DÙNG CHO THƯ VIỆN BẢO MẬT (PROVIDER)
 * =========================================================================
 */
@Service
@RequiredArgsConstructor
public class SecurityUserProviderImpl implements ISecurityUserProvider {

    private final IUserService userService;

    @Override
    public ISecurityUserAdapter timNguoiDungTheoId(Long id) {
        
        User user = null;
        try {
            // Tìm User thông qua Tầng Service để đảm bảo Danh sách Nhóm Quyền 
            // và Quyền hạt lựu đã được nạp đầy đủ từ CSDL vào Transient.
            user = userService.findById(id);
        } catch (Exception e) {
            // Nếu lỗi (Ví dụ: ID không tồn tại), biến user vẫn là null
            user = null;
        }
        
        if (user != null) {
            // Bọc nó vào Adapter rồi trả cho Thư viện Ultra Secure
            return new UserSecurityAdapter(user);
        }
        
        return null;
    }
}