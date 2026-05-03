//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IEmailService.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

/**
 * GIAO DIỆN DỊCH VỤ EMAIL
 * Khai báo các chức năng liên quan đến việc gửi thư điện tử.
 */
public interface IEmailService {
    
    /**
     * Hàm gửi email cơ bản hỗ trợ định dạng HTML.
     * * @param to Địa chỉ email người nhận
     * @param subject Tiêu đề email
     * @param content Nội dung email (có thể chứa thẻ HTML)
     */
    void sendEmail(String to, String subject, String content);
}