//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/CustomOAuth2UserService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final IUserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("[DEBUG OAuth2] Bắt đầu loadUser từ Google");
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        System.out.println("[DEBUG OAuth2] Email từ Google: " + email);
        System.out.println("[DEBUG OAuth2] Name từ Google: " + name);
        
        userService.saveGoogleUser(email, name);
        return oauth2User;
    }
}