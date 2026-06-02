//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/validators/annotations/RequirePermission.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * =========================================================================
 * ANNOTATION PHÂN QUYỀN ĐỘNG: ĐÁNH DẤU ENDPOINT CẦN KIỂM TRA QUYỀN HẠT LỰU
 * =========================================================================
 * Đặt trên method controller để PermissionInterceptor tự động kiểm tra
 * xem người dùng hiện tại có quyền tương ứng trong DB hay không.
 *
 * - SUPERADMIN (roleLevel = 0) luôn BYPASS mọi kiểm tra.
 * - Method KHÔNG có annotation này sẽ KHÔNG bị kiểm tra (backward compatible).
 * - Mã quyền (value) phải khớp với PERMISSION_CODE trong bảng PERMISSIONS.
 *
 * Ví dụ: @RequirePermission("POST_CREATE") trên endpoint tạo bài viết.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    // Mã quyền hạt lựu cần kiểm tra (VD: "POST_CREATE", "EVENT_DELETE")
    String value();
}
