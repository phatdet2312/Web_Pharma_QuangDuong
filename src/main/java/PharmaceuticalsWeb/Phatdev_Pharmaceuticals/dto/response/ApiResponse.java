//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ApiResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * KHUÔN MẪU PHẢN HỒI API TOÀN CỤC
 * @param <T> Kiểu dữ liệu của nội dung phản hồi (data)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // Chỉ hiện các trường không bị null khi chuyển sang JSON
public class ApiResponse<T> {
    
    // Mã trạng thái tùy chỉnh (ví dụ: 200, 400, 404, 500)
    int status;
    
    // Thông điệp phản hồi cho người dùng hoặc lập trình viên
    String message;
    
    // Dữ liệu thực tế trả về (Object, List, String...)
    T data;

    /**
     * Phương thức tiện ích để tạo nhanh phản hồi thành công
     */
    public static <T> ApiResponse<T> thanhCong(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Phương thức tiện ích để tạo nhanh phản hồi lỗi
     */
    public static <T> ApiResponse<T> loi(int code, String message) {
        return ApiResponse.<T>builder()
                .status(code)
                .message(message)
                .build();
    }
}