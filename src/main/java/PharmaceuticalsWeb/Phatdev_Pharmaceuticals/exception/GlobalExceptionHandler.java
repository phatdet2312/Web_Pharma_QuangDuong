//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/exception/GlobalExceptionHandler.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * TẤM LƯỚI BẮT LỖI TOÀN CỤC
 * Tự động chuyển đổi các Exception thành định dạng JSON ApiResponse chuẩn
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các lỗi nghiệp vụ do chúng ta chủ động ném ra (AppException)
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException exception) {
        ApiResponse<?> response = ApiResponse.loi(exception.getStatus(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    /**
     * Xử lý các lỗi vi phạm Validation (ví dụ: @NotBlank, @Size...)
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException exception) {
        
        // CHUẨN CODE PHỔ THÔNG: Gom lỗi bằng StringBuilder và vòng lặp For
        StringBuilder thongDiepLoi = new StringBuilder();
        List<FieldError> danhSachLoi = exception.getBindingResult().getFieldErrors();
        
        for (int i = 0; i < danhSachLoi.size(); i++) {
            FieldError loiChiTiet = danhSachLoi.get(i);
            thongDiepLoi.append(loiChiTiet.getField())
                        .append(": ")
                        .append(loiChiTiet.getDefaultMessage());
            
            // Thêm dấu phẩy nếu chưa phải là lỗi cuối cùng
            if (i < danhSachLoi.size() - 1) {
                thongDiepLoi.append(", ");
            }
        }

        ApiResponse<?> response = ApiResponse.loi(400, "Dữ liệu không hợp lệ: " + thongDiepLoi.toString());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý các lỗi runtime chưa xác định (Lỗi hệ thống nghiêm trọng)
     */
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException exception) {
        // Log lỗi tại đây để admin kiểm tra
        System.err.println("[HỆ THỐNG LỖI]: " + exception.getMessage());
        exception.printStackTrace();

        ApiResponse<?> response = ApiResponse.loi(500, "Đã xảy ra lỗi hệ thống ngoài ý muốn.");
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * Xử lý tất cả các loại Exception còn lại
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception exception) {
        ApiResponse<?> response = ApiResponse.loi(500, "Lỗi máy chủ: " + exception.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}