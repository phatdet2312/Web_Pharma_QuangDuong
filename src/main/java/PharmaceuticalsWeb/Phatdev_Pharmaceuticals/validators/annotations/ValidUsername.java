//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/validators/annotations/ValidUsername.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.ValidUsernameValidator;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidUsernameValidator.class)
@Documented
public @interface ValidUsername {
    String message() default "Username đã tồn tại";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}