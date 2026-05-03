//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/exception/AppException.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception;

import lombok.Getter;

/**
 * NGOẠI LỆ TÙY CHỈNH CỦA ỨNG DỤNG
 * Dùng để ném ra các lỗi nghiệp vụ trong tầng Service
 */
@Getter
public class AppException extends RuntimeException {

    private final int status;

    public AppException(int status, String message) {
        super(message);
        this.status = status;
    }

    public AppException(String message) {
        super(message);
        this.status = 400; // Mặc định là lỗi yêu cầu không hợp lệ
    }
}