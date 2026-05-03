//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/CustomOidcUserService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final IUserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy user từ Google về
        OidcUser oidcUser = super.loadUser(userRequest);
        
        // 2. Lấy thông tin cơ bản
        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");

        // 3. Lưu hoặc cập nhật vào DB
        userService.saveGoogleUser(email, name);

        // 4. Lấy User từ DB lên (Lúc này Service đã tự động nạp quyền 6 bảng vào RAM)
        User dbUser = userService.findByEmail(email);

        // 5. Xây dựng danh sách quyền hạn cho Spring Security
        // TUÂN THỦ QUY TẮC TỐI CAO: Xóa bỏ Stream API, dùng mảng toArray() và vòng lặp For
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        if (dbUser.getAuthorities() != null) {
            Object[] authArray = dbUser.getAuthorities().toArray();
            for (int i = 0; i < authArray.length; i = i + 1) {
                mappedAuthorities.add((GrantedAuthority) authArray[i]);
            }
        }

        // 6. Gộp các thuộc tính (Attributes) bằng vòng lặp For
        Map<String, Object> combinedAttributes = new HashMap<>();
        if (oidcUser.getAttributes() != null) {
            Object[] keysArray = oidcUser.getAttributes().keySet().toArray();
            for (int j = 0; j < keysArray.length; j = j + 1) {
                String key = keysArray[j].toString();
                combinedAttributes.put(key, oidcUser.getAttributes().get(key));
            }
        }
        
        // 7. Nhét Username từ Database vào Map
        combinedAttributes.put("sub_username", dbUser.getUsername());
        combinedAttributes.put("db_id", dbUser.getId());
        
        // 8. Trả về OidcUser an toàn
        return new DefaultOidcUser(
            mappedAuthorities, 
            oidcUser.getIdToken(), 
            new OidcUserInfo(combinedAttributes), 
            "sub_username"
        );
    }
}