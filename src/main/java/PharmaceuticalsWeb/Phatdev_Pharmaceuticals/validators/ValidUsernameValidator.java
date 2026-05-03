//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/validators/ValidUsernameValidator.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.ValidUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private final IUserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            return true; // Để @NotBlank xử lý
        }
        return !userRepository.existsByUsername(username);
    }
}