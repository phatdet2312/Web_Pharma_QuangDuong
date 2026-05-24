package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NguCanhNguoiDungFactory {

    private final IUserRepository userRepository;
    private final IUserService userService;

    public NguCanhNguoiDung taoNguCanh(Long userId) {
        if (userId == null) {
            return new NguCanhNguoiDung(null, 999);
        }
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent() == false) {
            return new NguCanhNguoiDung(userId, 999);
        }
        int capBac = userService.layCapBacQuyenLucCaoNhat(optUser.get());
        return new NguCanhNguoiDung(userId, capBac);
    }
}
