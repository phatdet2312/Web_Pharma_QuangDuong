//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Service/MaTranLuoiLocNghiaTrangQuyenHanCu.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLongArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;

/**
 * =========================================================================================
 * NGHĨA TRANG QUYỀN HẠN (ROLE DNA GRAVEYARD) - KIẾN TRÚC BẢO MẬT KHÔNG TRẠNG THÁI
 * (STATELESS)
 * =========================================================================================
 *
 * 1. MỤC ĐÍCH TỒN TẠI:
 * - Khi Admin hạ quyền hoặc khóa một User, Quyền hạn cũ (DNA Quyền) của User đó trở thành "Bóng
 * ma" (vẫn còn hạn theo tuổi thọ của Token).
 * - Nghĩa trang này có nhiệm vụ chôn cất và phong ấn vĩnh viễn các DNA Quyền đó,
 * khiến Hacker không thể dùng lại Token chứa Quyền cũ để vượt mặt hệ thống. Mọi thứ xử lý trên RAM,
 * không gọi Database!
 *
 * 2. CƠ CHẾ VẬT LÝ (SỰ KẾT HỢP CỦA 3 KỲ QUAN KHOA HỌC MÁY TÍNH):
 * - [Bloom Filter]: Dùng ma trận Bit để lưu vết DNA Quyền. Chỉ tốn 256KB RAM quản
 * lý hàng triệu DNA Quyền.
 * - [Ring Buffer]: Vòng lặp thời gian tính theo NGÀY. Quyền sống 7 ngày -> Có 8
 * Khu mộ.
 * - [Lock-Free CAS]: Không dùng `synchronized`. Hàng chục ngàn luồng có thể
 * chôn cất/dọn rác
 * cùng lúc bằng lệnh phần cứng (Compare-And-Swap) mà không làm nghẽn CPU.
 *
 * 3. BẢN CHẤT LƯU TRỮ VÀ ÁNH XẠ TỌA ĐỘ (CÁCH TÍNH VỊ TRÍ ADN):
 * Hệ thống sử dụng một mảng 1 chiều khổng lồ chứa các số Long
 * (LUOI_BIT_QUANG_PHO).
 * Để tìm ra đúng 1 bit đại diện cho 1 phần ADN của Quyền Hạn, hệ thống thực hiện 3
 * bước:
 * + Bước A (Tính Khe Ngày): Lấy Ngày Hiện Tại & MAT_NA_KHE (ví dụ: ngày hiện
 * tại 10 & 7 = 2)
 * để ra được số thứ tự =2 tức vị trí khe 2 của Khu mộ (Từ 0 -> số ke).
 * + Bước B (Tính Tọa Độ ADN): Dùng máy xay MurmurHash3 băm DNA Quyền ra 1 con số
 * khổng lồ.
 * Dùng phép & (MAT_NA_BIT_TRONG_KHE) để ép con số đó lọt thỏm vào độ rộng của 1
 * Khu mộ
 * (Ví dụ: từ 0 đến 262.143 bit).
 * + Bước C (Định vị Vật lý): Tại hàm `kiemTraBit/batBit`:
 * -> Xác định Index của số Long: = (vị trí Khe * Số Long mỗi Khe) + (Tọa độ ADN
 * / 64).
 * Phép chia 64 được tối ưu bằng phép dịch bit `>> 6`.
 * -> Xác định Vị trí Bit bên trong số Long đó: = (Tọa độ ADN % 64).
 * Phép chia lấy dư 64 được tối ưu bằng phép `& 63`.
 *
 * 4. LUỒNG HOẠT ĐỘNG (FAIL-FAST & LAZY CLEANUP):
 * - CHÔN CẤT: Hôm nay là ngày X. DNA Quyền bị hủy sẽ bị băm 3 lần (MurmurHash3)
 * thành 3 tọa độ bit
 * và ném vào "Khu mộ của Ngày X".
 * - LỤC SOÁT: Vì hệ thống không biết DNA Quyền bị hủy vào ngày nào trong quá khứ,
 * nên khi soi xét,
 * nó sẽ quét TOÀN BỘ 8 Khu mộ. Chỉ cần thấy xác ở 1 khu -> Chặn ngay lập tức!
 * - CẢI TÁNG (Tự dọn rác): Token (mang Quyền) sống N ngày. Khi thời gian trôi qua, vòng lặp
 * quay lại đúng
 * Khu mộ số 0. Lúc này, rác dưới mộ chắc chắn đã HẾT HẠN VẬT LÝ. Luồng đầu tiên
 * chạm vào sẽ
 * dùng cờ ÂM (-ngayHienTai) khóa cửa và đổ 0L xóa sạch hố đó. Không cần Garbage
 * Collector!
 * =========================================================================================
 */
@Component
public class MaTranLuoiLocNghiaTrangQuyenHanCu {

    // Tổng số khu mộ (Số khe) - Bắt buộc phải là Lũy thừa của 2 để dùng phép
    // Bitwise (&) thay cho phép chia lấy dư (%)
    private final int SO_KHE;
    // Mặt nạ dùng để tính nhẩm vị trí Khe siêu tốc (Ví dụ: 8 khe -> Mặt nạ là 7,
    // nhị phân 111)
    private final int MAT_NA_KHE;
    // Độ lớn của mỗi Khu mộ (Đo bằng số lượng biến Long 64-bit)
    private final int SO_LONG_MOI_KHE;
    // Tổng số lượng Bit thực tế trong 1 Khu mộ (Ví dụ 32KB RAM = 262.144 bit)
    private final int TONG_SO_BIT_MOI_KHE;
    // Mặt nạ Bit dùng cho tọa độ băm (Ví dụ TONG_SO_BIT_MOI_KHE là 2^18, thì mặt nạ
    // là 262.143)
    private final int MAT_NA_BIT_TRONG_KHE;

    // Lưới 1 chiều phẳng khổng lồ: Chứa tất cả các bit của toàn bộ Nghĩa trang
    // (Nhanh hơn mảng 2 chiều do CPU Cache)
    private final AtomicLongArray LUOI_BIT_QUANG_PHO;

    // Mảng ghi nhớ: Lưu lại xem mỗi Khu mộ được dọn rác lần cuối vào Ngày nào trong
    // Kỷ nguyên
    private final AtomicLongArray KY_UC_NGAY;

    @Autowired
    public MaTranLuoiLocNghiaTrangQuyenHanCu(SecurityLibraryProperties props) {
        // 1. Tính toán số ngày Quyền hạn sống dựa theo tuổi thọ cấu hình milliseconds của Token
        long thoiGianSongMs = props.getJwtExpirationMs();
        long soNgaySong = thoiGianSongMs / (1000L * 60 * 60 * 24);
        if (soNgaySong == 0) {
            soNgaySong = 1; // Tối thiểu 1 ngày để chia khe
        }

        // 2. Tìm lũy thừa của 2 lớn hơn hoặc bằng Số ngày sống (VD: 7 ngày -> 8 khe)
        this.SO_KHE = props.timLuyThua(soNgaySong);
        this.MAT_NA_KHE = SO_KHE - 1;

        // 3. Quy đổi dung lượng KB sang số lượng biến Long. (1 KB = 1024 Bytes = 128 số
        // Long 8-Bytes)
        this.SO_LONG_MOI_KHE = props.getGraveyardBloomSizeKbMoiKhe() * 128;
        this.TONG_SO_BIT_MOI_KHE = SO_LONG_MOI_KHE * 64; // Mỗi số Long chứa 64 bit
        this.MAT_NA_BIT_TRONG_KHE = TONG_SO_BIT_MOI_KHE - 1;

        // Khởi tạo Ma trận Flat nguyên tử
        this.LUOI_BIT_QUANG_PHO = new AtomicLongArray(SO_KHE * SO_LONG_MOI_KHE);
        this.KY_UC_NGAY = new AtomicLongArray(SO_KHE);

        System.out.println("[NghiaTrangQuyenHan] Đã khởi tạo Nghĩa Trang: " + SO_KHE + " khe ngày, tổng RAM: "
                + (SO_KHE * SO_LONG_MOI_KHE * 8 / 1024) + " KB");
    }

    // -------------------------------------------------------------------------
    // HÀM CÔNG CỤ: Xác định mốc thời gian tuyệt đối tính bằng NGÀY
    // -------------------------------------------------------------------------
    private long layNgayHienTai() {
        return System.currentTimeMillis() / (1000L * 60 * 60 * 24);
    }

    /**
     * =========================================================================
     * 1. HÀM CHÔN CẤT: Đóng đinh DNA Quyền độc hại vào Nghĩa trang
     * - Được gọi bởi DynamicRoleFilter khi có lệnh đổi quyền hoặc khóa tài khoản.
     * =========================================================================
     */
    public void chonCat(String maDnaQuyenHan) {
        long ngayHienTai = layNgayHienTai();
        // Ánh xạ Ngày hiện tại vào đúng 1 trong 8 Khu mộ (Thuật toán Ring Buffer)
        int viTriKhe = (int) (ngayHienTai & MAT_NA_KHE);

        // Kích hoạt người gác cổng: Xem hố này có chứa rác quá hạn không, nếu có thì
        // dọn sạch
        donDepKheNeuSangNgayMoi(viTriKhe, ngayHienTai);

        byte[] duLieuBam = maDnaQuyenHan.getBytes(StandardCharsets.UTF_8);

        // Băm DNA Quyền 3 lần bằng 3 Hạt giống (Seed) để tạo 3 Tọa độ ngẫu nhiên
        // Dùng Bitwise '&' thay vì '%' để ép tọa độ nằm lọt trong phạm vi TONG_SO_BIT
        // của khe
        int toaDo1 = bamMurmurHash3(duLieuBam, 98765) & (MAT_NA_BIT_TRONG_KHE);
        int toaDo2 = bamMurmurHash3(duLieuBam, 43210) & (MAT_NA_BIT_TRONG_KHE);
        int toaDo3 = bamMurmurHash3(duLieuBam, 13579) & (MAT_NA_BIT_TRONG_KHE);

        // Bật 3 Bit tương ứng thành 1 (Đã chôn)
        batBit(viTriKhe, toaDo1);
        batBit(viTriKhe, toaDo2);
        batBit(viTriKhe, toaDo3);
        System.out.println("[NghiaTrangQuyenHan] đưa DNA quyền cũ vào khe: "
                + viTriKhe + "-> toạ độ " + toaDo1 + ", " + toaDo2 + ", " + toaDo3);
    }

    /**
     * =========================================================================
     * 2. HÀM KIỂM TRA: Lục soát toàn bộ Nghĩa trang
     * - Quét tất cả các khe vì ta không biết chắc DNA Quyền bị chôn cất vào ngày nào.
     * =========================================================================
     */
    public boolean kiemTraDaChet(String maDnaQuyenHan) {
        if (maDnaQuyenHan == null)
            return false;

        byte[] duLieuBam = maDnaQuyenHan.getBytes(StandardCharsets.UTF_8);

        // Tính toán trước 3 tọa độ (DNA) của Quyền Hạn - Chỉ tính 1 lần duy nhất để bảo vệ
        // CPU
        int toaDo1 = bamMurmurHash3(duLieuBam, 98765) & (MAT_NA_BIT_TRONG_KHE);
        int toaDo2 = bamMurmurHash3(duLieuBam, 43210) & (MAT_NA_BIT_TRONG_KHE);
        int toaDo3 = bamMurmurHash3(duLieuBam, 13579) & (MAT_NA_BIT_TRONG_KHE);

        // Lùng sục toàn bộ các khe ngày (Ví dụ: 8 vòng lặp)
        for (int i = 0; i < SO_KHE; i++) {
            // Kiểm tra xem tại Khe thứ i, cả 3 Bit này có đồng loạt là 1 không
            boolean timThayTrongKheNay = kiemTraBit(i, toaDo1)
                    && kiemTraBit(i, toaDo2)
                    && kiemTraBit(i, toaDo3);

            if (timThayTrongKheNay == true) {
                System.out.println("[NghiaTrangQuyenHan] Phát hiện xác DNA Quyền tại khe ngày: " + i);
                return true; // Tìm thấy xác -> Trả về TRUE (DNA Quyền đã chết, phải chặn lại!)
            }
        }

        return false; // Lục soát hết toàn bộ các ngày không thấy -> Quyền thực sự sạch
    }

    /**
     * =========================================================================
     * 3. CƠ CHẾ DỌN RÁC THÔNG MINH (LAZY CLEANUP & LOCK-FREE)
     * - Chỉ dọn khi vòng lặp thời gian quay lại đè lên hố cũ. Không chạy ngầm.
     * =========================================================================
     */
    private void donDepKheNeuSangNgayMoi(int viTriKhe, long ngayHienTai) {
        long ngayCuCuaKhe = KY_UC_NGAY.get(viTriKhe);

        // Nếu cái hố này thuộc về quá khứ (Ví dụ 8 ngày trước)
        if (ngayCuCuaKhe < ngayHienTai) {

            // Dùng số Âm làm Lá cờ hiệu: "Đang có Vua dọn dẹp, cấm vào!"
            long coDangDonDep = -ngayHienTai;

            // Lệnh CAS (Compare-And-Swap): Nếu cờ vẫn là cờ cũ, tao sẽ giành quyền cắm cờ
            // mới.
            // Điều này đảm bảo trong 10.000 luồng, CHỈ CÓ 1 LUỒNG DUY NHẤT được vào dọn.
            if (KY_UC_NGAY.compareAndSet(viTriKhe, ngayCuCuaKhe, coDangDonDep) == true) {

                int indexBatDau = viTriKhe * SO_LONG_MOI_KHE;
                // Xóa trắng toàn bộ RAM của Khe này
                for (int i = 0; i < SO_LONG_MOI_KHE; i = i + 1) {
                    LUOI_BIT_QUANG_PHO.set(indexBatDau + i, 0L);
                }

                // Dọn xong, cắm cờ "Thời gian thực" (Số Dương) để thông quan cho thiên hạ
                KY_UC_NGAY.set(viTriKhe, ngayHienTai);

            } else {
                // XỬ LÝ TẮC NGHẼN (SPIN-WAIT CÓ GIỚI HẠN)
                // Ta là luồng đến muộn, thằng Vua đang dọn bên trong.
                // Ta đứng chờ nhường CPU (yield) tối đa 10 vòng (khoảng vài chục nano-giây).
                int gioiHanXoay = 10;
                while (KY_UC_NGAY.get(viTriKhe) != ngayHienTai && gioiHanXoay > 0) {
                    gioiHanXoay--;
                    Thread.yield();
                }

                // CẦU CHÌ SINH TỬ: Nếu hết 10 vòng mà thằng Vua vẫn chưa dọn xong (Do lag/lỗi).
                // Ta sẽ tự động đi qua hàm Check Bit luôn!
                // Lý do: Thà để lọt lưới 1 gói tin cũ, còn hơn là bắt hàng vạn luồng đứng chờ
                // làm SẬP SERVER!
            }
        }
    }

    /**
     * =========================================================================
     * 4. CÁC HÀM TƯƠNG TÁC MỨC ĐỘ THANH GHI CPU (BITWISE OPERATIONS)
     * =========================================================================
     */

    // Đọc 1 bit xem là 0 hay 1
    private boolean kiemTraBit(int viTriKhe, int toaDoBit) {
        // Tìm vị trí của số Long trong mảng 1 chiều (>> 6 tương đương chia cho 64)
        int indexLong = (viTriKhe * SO_LONG_MOI_KHE) + (toaDoBit >> 6);
        // Xác định vị trí của bit CẦN TÌM bên trong số Long đó (& 63 tương đương lấy
        // phần dư)
        long matNa = 1L << (toaDoBit & 63);
        return (LUOI_BIT_QUANG_PHO.get(indexLong) & matNa) != 0L;
    }

    // Bật 1 bit từ 0 thành 1 một cách an toàn đa luồng
    private void batBit(int viTriKhe, int toaDoBit) {
        int indexLong = (viTriKhe * SO_LONG_MOI_KHE) + (toaDoBit >> 6);
        long matNa = 1L << (toaDoBit & 63);

        while (true) {
            long giaTriCu = LUOI_BIT_QUANG_PHO.get(indexLong);
            // Nếu bit đã được thằng khác bật rồi thì ta nghỉ tay
            if ((giaTriCu & matNa) != 0L)
                break;

            // Lệnh CAS để đè giá trị mới lên. Nếu thất bại (do có luồng khác vừa ghi), nó
            // sẽ tự quay lại vòng while
            if (LUOI_BIT_QUANG_PHO.compareAndSet(indexLong, giaTriCu, giaTriCu | matNa))
                break;
        }
    }

    /**
     * =========================================================================
     * 5. MÁY XAY BĂM DỮ LIỆU SIÊU TỐC (MURMURHASH 3)
     * Dùng để rải đều các DNA Quyền ra khắp ma trận RAM. Tốc độ cao hơn SHA-256 rất
     * nhiều.
     * =========================================================================
     */
    private int bamMurmurHash3(byte[] data, int seed) {
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int h1 = seed;
        int len = data.length;
        int roundedEnd = (len & 0xfffffffc);

        for (int i = 0; i < roundedEnd; i += 4) {
            int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8)
                    | ((data[i + 2] & 0xff) << 16) | (data[i + 3] << 24);
            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);
            k1 *= c2;
            h1 ^= k1;
            h1 = (h1 << 13) | (h1 >>> 19);
            h1 = h1 * 5 + 0xe6546b64;
        }

        int k1 = 0;
        switch (len & 0x03) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xff) << 16;
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xff) << 8;
            case 1:
                k1 |= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);
                k1 *= c2;
                h1 ^= k1;
        }

        h1 ^= len;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;
        return Math.abs(h1);
    }
}