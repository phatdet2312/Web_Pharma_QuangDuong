//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/EmailServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEmailService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

/**
 * THỰC THI DỊCH VỤ EMAIL
 * Xử lý logic kết nối tới SMTP Server (Google) để gửi mail.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    // Tiêm (Inject) công cụ gửi mail của Spring Boot
    private final JavaMailSender mailSender;

    @Async // Chạy bất đồng bộ (luồng riêng) để người dùng không phải chờ đợi
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true: Bật chế độ dịch HTML
            
            mailSender.send(message);
            System.out.println("[EmailService] Đã gửi email thành công tới: " + to);
            
        } catch (MessagingException e) {
            // Ném ra RuntimeException để GlobalExceptionHandler (Đợt 1) tự động tóm lấy
            throw new RuntimeException("Hệ thống gửi email đang gặp sự cố: " + e.getMessage(), e);
        }
    }

    /**
     * Hàm nội bộ: Đọc file mẫu HTML từ thư mục resources
     */
    private String readEmailTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + templateName + ".html");
            byte[] byteData = resource.getInputStream().readAllBytes();
            return new String(byteData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[EmailService] Không tìm thấy file template: " + templateName);
            return "<p>Lỗi hiển thị Template</p>";
        }
    }
}