//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Provider/ISecurityUserProvider.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Provider;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;

public interface ISecurityUserProvider {
    // Thư viện sẽ gọi hàm này để mượn App chủ truy vấn DB
    ISecurityUserAdapter timNguoiDungTheoId(Long id); 
}
