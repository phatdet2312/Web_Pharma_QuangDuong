//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AddressServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.AddressRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAddressService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * THỰC THI: DỊCH VỤ QUẢN LÝ ĐỊA CHỈ DOANH NGHIỆP
 * =========================================================================
 * Tuân thủ tuyệt đối Quy tắc: Không dùng Stream API, dùng vòng lặp For truyền thống.
 * Bảo vệ IDOR: mọi thao tác sửa/xóa đều kiểm tra address.partnerId == profile.id của user.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

    private final IUserService userService;
    private final IUserTrackingService trackingService;
    private final IPartnerProfileRepository partnerProfileRepository;
    private final IAddressRepository addressRepository;
    private final IProvinceRepository provinceRepository;
    private final IDistrictRepository districtRepository;
    private final IWardRepository wardRepository;

    // =========================================================================
    // DANH MỤC HÀNH CHÍNH
    // =========================================================================

    @Override
    public List<ProvinceResponse> layDanhSachTinh() {
        List<Province> danhSachTinh = provinceRepository.findAllByOrderByNameAsc();
        List<ProvinceResponse> responseList = new ArrayList<>();

        for (int i = 0; i < danhSachTinh.size(); i = i + 1) {
            Province tinh = danhSachTinh.get(i);
            ProvinceResponse dto = new ProvinceResponse();
            dto.setId(tinh.getId());
            dto.setName(tinh.getName());
            dto.setCode(tinh.getCode());
            responseList.add(dto);
        }

        return responseList;
    }

    @Override
    public List<DistrictResponse> layDanhSachQuan(Integer provinceId) {
        if (provinceId == null) {
            throw new AppException(400, "Mã tỉnh/thành phố không được để trống");
        }
        List<District> danhSachQuan = districtRepository.findByProvinceIdOrderByNameAsc(provinceId);
        List<DistrictResponse> responseList = new ArrayList<>();

        for (int i = 0; i < danhSachQuan.size(); i = i + 1) {
            District quan = danhSachQuan.get(i);
            DistrictResponse dto = new DistrictResponse();
            dto.setId(quan.getId());
            dto.setProvinceId(quan.getProvinceId());
            dto.setName(quan.getName());
            responseList.add(dto);
        }

        return responseList;
    }

    @Override
    public List<WardResponse> layDanhSachPhuong(Integer districtId) {
        if (districtId == null) {
            throw new AppException(400, "Mã quận/huyện không được để trống");
        }
        List<Ward> danhSachPhuong = wardRepository.findByDistrictIdOrderByNameAsc(districtId);
        List<WardResponse> responseList = new ArrayList<>();

        for (int i = 0; i < danhSachPhuong.size(); i = i + 1) {
            Ward phuong = danhSachPhuong.get(i);
            WardResponse dto = new WardResponse();
            dto.setId(phuong.getId());
            dto.setDistrictId(phuong.getDistrictId());
            dto.setName(phuong.getName());
            responseList.add(dto);
        }

        return responseList;
    }

    // =========================================================================
    // CRUD ĐỊA CHỈ
    // =========================================================================

    @Override
    public List<AddressDetailResponse> layDanhSachDiaChi() {
        PartnerProfile profile = layProfileCuaUserHienTai();
        List<Address> danhSachDiaChi = addressRepository.findByPartnerIdOrderByIsDefaultDescIdAsc(profile.getId());
        List<AddressDetailResponse> responseList = new ArrayList<>();

        for (int i = 0; i < danhSachDiaChi.size(); i = i + 1) {
            Address diaChi = danhSachDiaChi.get(i);
            responseList.add(dongGoiDiaChi(diaChi));
        }

        return responseList;
    }

    @Override
    @Transactional
    public AddressDetailResponse themDiaChi(AddressRequest request) {
        PartnerProfile profile = layProfileCuaUserHienTai();

        // Kiểm tra phường/xã tồn tại
        wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new AppException(400, "Phường/xã không tồn tại trong hệ thống"));

        // Nếu đánh dấu mặc định, reset tất cả địa chỉ khác trước
        if (request.isDefault()) {
            addressRepository.resetTatCaDiaDiChi(profile.getId());
        }

        Address diaChi = new Address();
        diaChi.setPartnerId(profile.getId());
        diaChi.setWardId(request.getWardId());
        diaChi.setStreetAddress(request.getStreetAddress().trim());
        diaChi.setDefault(request.isDefault());

        String loai = request.getAddressType();
        if (loai == null || loai.trim().isEmpty()) {
            loai = "OFFICE";
        }
        diaChi.setAddressType(loai.trim());

        Address saved = addressRepository.save(diaChi);

        trackingService.ghiHanhVi(profile.getUserId(), "ADD_ADDRESS", saved.getId(),
                null, taoJsonDiaChi(saved));

        return dongGoiDiaChi(saved);
    }

    @Override
    @Transactional
    public AddressDetailResponse suaDiaChi(Long addressId, AddressRequest request) {
        PartnerProfile profile = layProfileCuaUserHienTai();
        Address diaChi = layVaKiemTraSoHuu(addressId, profile.getId());

        // Snapshot trạng thái CŨ trước khi modify — phải capture trước setWardId/setStreetAddress
        String oldPayload = taoJsonDiaChi(diaChi);

        // Kiểm tra phường/xã tồn tại
        wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new AppException(400, "Phường/xã không tồn tại trong hệ thống"));

        if (request.isDefault() && diaChi.isDefault() == false) {
            addressRepository.resetTatCaDiaDiChi(profile.getId());
        }

        diaChi.setWardId(request.getWardId());
        diaChi.setStreetAddress(request.getStreetAddress().trim());
        diaChi.setDefault(request.isDefault());

        String loai = request.getAddressType();
        if (loai == null || loai.trim().isEmpty()) {
            loai = "OFFICE";
        }
        diaChi.setAddressType(loai.trim());

        Address saved = addressRepository.save(diaChi);

        trackingService.ghiHanhVi(profile.getUserId(), "UPDATE_ADDRESS", saved.getId(),
                oldPayload, taoJsonDiaChi(saved));

        return dongGoiDiaChi(saved);
    }

    @Override
    @Transactional
    public void xoaDiaChi(Long addressId) {
        PartnerProfile profile = layProfileCuaUserHienTai();
        Address diaChi = layVaKiemTraSoHuu(addressId, profile.getId());

        String oldPayload = taoJsonDiaChi(diaChi);
        addressRepository.delete(diaChi);

        trackingService.ghiHanhVi(profile.getUserId(), "DELETE_ADDRESS", addressId,
                oldPayload, null);
    }

    @Override
    @Transactional
    public void datDiaChiMacDinh(Long addressId) {
        PartnerProfile profile = layProfileCuaUserHienTai();
        Address diaChi = layVaKiemTraSoHuu(addressId, profile.getId());

        addressRepository.resetTatCaDiaDiChi(profile.getId());
        diaChi.setDefault(true);
        addressRepository.save(diaChi);

        trackingService.ghiHanhVi(profile.getUserId(), "SET_DEFAULT_ADDRESS", addressId,
                null, taoJsonDiaChi(diaChi));
    }

    // =========================================================================
    // HÀM TIỆN ÍCH NỘI BỘ
    // =========================================================================

    /**
     * Lấy hồ sơ đối tác của tài khoản đang đăng nhập.
     * Ném AppException 404 nếu chưa tạo hồ sơ — bắt buộc tạo hồ sơ trước khi thêm địa chỉ.
     */
    private PartnerProfile layProfileCuaUserHienTai() {
        User user = userService.getCurrentAuthenticatedUser();
        PartnerProfile profile = partnerProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            throw new AppException(404, "Vui lòng tạo hồ sơ doanh nghiệp trước khi thêm địa chỉ");
        }
        return profile;
    }

    /**
     * Lấy địa chỉ và kiểm tra quyền sở hữu (chống IDOR).
     * Ném AppException 403 nếu địa chỉ không thuộc hồ sơ của user hiện tại.
     */
    private Address layVaKiemTraSoHuu(Long addressId, Long profileId) {
        Address diaChi = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(404, "Địa chỉ không tồn tại"));

        if (diaChi.getPartnerId().equals(profileId) == false) {
            throw new AppException(403, "Bạn không có quyền thao tác trên địa chỉ này");
        }

        return diaChi;
    }

    /**
     * Đóng gói Address entity thành AddressDetailResponse.
     * Resolve tên Phường → Quận → Tỉnh thành chuỗi hiển thị.
     */
    private AddressDetailResponse dongGoiDiaChi(Address diaChi) {
        AddressDetailResponse dto = new AddressDetailResponse();
        dto.setAddressId(diaChi.getId());
        dto.setStreetAddress(diaChi.getStreetAddress());
        dto.setDefault(diaChi.isDefault());
        dto.setAddressType(diaChi.getAddressType());
        dto.setWardId(diaChi.getWardId());

        // Resolve chuỗi tên hành chính
        String tenPhuong = "";
        String tenQuan = "";
        String tenTinh = "";
        Integer districtId = null;
        Integer provinceId = null;

        Ward phuong = wardRepository.findById(diaChi.getWardId()).orElse(null);
        if (phuong != null) {
            tenPhuong = phuong.getName();
            districtId = phuong.getDistrictId();
            dto.setDistrictId(districtId);

            District quan = districtRepository.findById(districtId).orElse(null);
            if (quan != null) {
                tenQuan = quan.getName();
                provinceId = quan.getProvinceId();
                dto.setProvinceId(provinceId);

                Province tinh = provinceRepository.findById(provinceId).orElse(null);
                if (tinh != null) {
                    tenTinh = tinh.getName();
                }
            }
        }

        dto.setWardName(tenPhuong);
        dto.setDistrictName(tenQuan);
        dto.setProvinceName(tenTinh);

        // Ghép địa chỉ đầy đủ: Số nhà, Phường, Quận, Tỉnh
        String diaChiDayDu = diaChi.getStreetAddress();
        if (tenPhuong.isEmpty() == false) {
            diaChiDayDu = diaChiDayDu + ", " + tenPhuong;
        }
        if (tenQuan.isEmpty() == false) {
            diaChiDayDu = diaChiDayDu + ", " + tenQuan;
        }
        if (tenTinh.isEmpty() == false) {
            diaChiDayDu = diaChiDayDu + ", " + tenTinh;
        }
        dto.setDiaChiDayDu(diaChiDayDu);

        return dto;
    }

    // =========================================================================
    // PAYLOAD HELPERS — AUDIT TRAIL
    // =========================================================================

    /** Tạo JSON snapshot địa chỉ để ghi audit log. */
    private String taoJsonDiaChi(Address diaChi) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"wardId\":").append(diaChi.getWardId()).append(",");
        sb.append("\"streetAddress\":\"").append(chuoiAnToan(diaChi.getStreetAddress())).append("\",");
        sb.append("\"addressType\":\"").append(chuoiAnToan(diaChi.getAddressType())).append("\",");
        sb.append("\"isDefault\":").append(diaChi.isDefault());
        sb.append("}");
        return sb.toString();
    }

    private String chuoiAnToan(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
