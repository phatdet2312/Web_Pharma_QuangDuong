//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/WebMvcConfig.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL "/images/books/**" tới thư mục thực tế "uploads/books/"
        Path bookUploadDir = Paths.get("./uploads/books");
        String bookUploadPath = bookUploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/images/books/**")
                .addResourceLocations("file:/" + bookUploadPath + "/");

        // Ánh xạ URL "/images/partners/**" tới thư mục thực tế "uploads/partners/"
        // Phục vụ ảnh đại diện doanh nghiệp (avatars/) và giấy phép kinh doanh (licenses/)
        Path partnerUploadDir = Paths.get("./uploads/partners");
        String partnerUploadPath = partnerUploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/images/partners/**")
                .addResourceLocations("file:/" + partnerUploadPath + "/");
    }
}