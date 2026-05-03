//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AddressDetailResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

/**
 * DTO trả về một địa chỉ doanh nghiệp đã được giải mã tên Phường/Quận/Tỉnh.
 * Frontend nhận chuỗi tên đầy đủ — không cần tự resolve ID.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailResponse {

    private Long addressId;
    private String streetAddress;
    private String wardName;
    private String districtName;
    private String provinceName;

    /** ID phường để Frontend populate lại form khi sửa */
    private Integer wardId;
    private Integer districtId;
    private Integer provinceId;

    private boolean isDefault;
    private String addressType;

    /** Chuỗi địa chỉ đầy đủ được ghép sẵn — phục vụ hiển thị nhanh */
    private String diaChiDayDu;
}
