//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/RegisterTemp.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTemp {
    private String fullName;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;
    private LocalDate birthDate;
}