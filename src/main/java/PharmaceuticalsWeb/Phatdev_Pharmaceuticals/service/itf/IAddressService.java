//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IAddressService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.AddressRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AddressDetailResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.DistrictResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ProvinceResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.WardResponse;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN: DỊCH VỤ QUẢN LÝ ĐỊA CHỈ DOANH NGHIỆP (CHUẨN MÙ)
 * =========================================================================
 * Toàn bộ nghiệp vụ CRUD địa chỉ và tra cứu danh mục hành chính.
 */
public interface IAddressService {

    /** Lấy danh sách tất cả tỉnh/thành phố — phục vụ populate dropdown Tỉnh */
    List<ProvinceResponse> layDanhSachTinh();

    /** Lấy danh sách quận/huyện theo tỉnh — phục vụ populate dropdown Quận phụ thuộc */
    List<DistrictResponse> layDanhSachQuan(Integer provinceId);

    /** Lấy danh sách phường/xã theo quận — phục vụ populate dropdown Phường phụ thuộc */
    List<WardResponse> layDanhSachPhuong(Integer districtId);

    /**
     * Lấy toàn bộ địa chỉ của tài khoản hiện tại.
     * Địa chỉ mặc định được trả về đầu tiên.
     */
    List<AddressDetailResponse> layDanhSachDiaChi();

    /**
     * Thêm địa chỉ mới.
     * Nếu isDefault = true, tự động reset các địa chỉ khác về isDefault = false trước.
     * @return DTO địa chỉ vừa tạo (đã giải mã tên Phường/Quận/Tỉnh)
     */
    AddressDetailResponse themDiaChi(AddressRequest request);

    /**
     * Cập nhật địa chỉ theo ID.
     * Kiểm tra IDOR: địa chỉ phải thuộc đúng hồ sơ đối tác của tài khoản hiện tại.
     */
    AddressDetailResponse suaDiaChi(Long addressId, AddressRequest request);

    /**
     * Xóa địa chỉ theo ID.
     * Kiểm tra IDOR: địa chỉ phải thuộc đúng hồ sơ đối tác của tài khoản hiện tại.
     */
    void xoaDiaChi(Long addressId);

    /**
     * Đặt một địa chỉ làm mặc định.
     * Reset toàn bộ địa chỉ khác về isDefault = false trước khi set địa chỉ này = true.
     * Kiểm tra IDOR trước khi thực hiện.
     */
    void datDiaChiMacDinh(Long addressId);
}
