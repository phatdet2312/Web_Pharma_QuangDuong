//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/view/HomeViewController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "home/index";
    }

    @GetMapping({"/warforge", "/warforge/index"})
    public String openWarforge() {
        return "warforge/index"; 
    }
}