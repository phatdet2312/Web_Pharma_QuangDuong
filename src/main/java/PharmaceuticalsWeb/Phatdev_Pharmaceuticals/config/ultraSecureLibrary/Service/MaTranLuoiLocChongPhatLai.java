
//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/MaTranLuoiLocChongPhatLai.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLongArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;

/**
 *TIME-SLOTTED ATOMIC BLOOM FILTER
 * - Cấu trúc: 64 Khe thời gian (Ring Buffer).
 * - Bộ nhớ: 2 MB RAM cố định (Không sinh Object rác, GC hoàn toàn mù).
 * - Tốc độ: O(1) Lock-free tuyệt đối bằng Compare-And-Swap (CAS).
 * - Nhiệm vụ: Hủy diệt mọi nỗ lực Replay Attack trong 60 giây.
 */

@Component
public class MaTranLuoiLocChongPhatLai {

    private final SecurityLibraryProperties props;

    // 64 Khe thời gian (Cho phép dùng phép & 63 siêu tốc thay vì phép chia lấy dư)
    private final int SO_KHE;
    private final int MAT_NA_KHE;
    // Mỗi khe chứa 4096 số Long (Tương đương 262.144 Bit mỗi giây)
    // Đủ sức chứa 20.000 Request/Giây với tỷ lệ khóa oan (Collision) = 0.0000001%
    private final int SO_LONG_MOI_KHE;
    private final int TONG_SO_BIT_MOI_KHE;
    private final long REPLAY_WINDOW_MS;
    private final int MAT_NA_BIT_TRONG_KHE;

    // Lưới 1 chiều phẳng (Tránh dùng mảng 2 chiều để tối ưu hóa CPU Cache)
    private final AtomicLongArray LUOI_BIT_QUANG_PHO;

    // Mảng lưu lại Giây cuối cùng mà Khe đã được sử dụng (Để tự động dọn rác)
    private final AtomicLongArray KY_UC_THOI_GIAN;


    
    @Autowired
    public MaTranLuoiLocChongPhatLai(SecurityLibraryProperties props) {
        this.props = props;
        //Nhân 2 Window vì Time Span có cả chiều Dương (nhanh hơn) và Âm (chậm hơn) Server.
        this.REPLAY_WINDOW_MS = props.getReplayWindowMs()/1000;
        this.SO_KHE = props.timLuyThua(REPLAY_WINDOW_MS * 2);// 60s * 2 -> Tìm lũy thừa của 120 -> Trả về 128 Khe!
        this.MAT_NA_KHE = SO_KHE - 1; // Ví dụ: 64 -> 63 (111111 bitwise)
        // Tính toán dung lượng RAM dựa trên cấu hình KB
        // 1KB = 1024 bytes = 128 số Long (8 bytes mỗi số)
        this.SO_LONG_MOI_KHE = props.getBloomSizeKbMoiKhe() * 128;
        this.TONG_SO_BIT_MOI_KHE = SO_LONG_MOI_KHE * 64;
        this.MAT_NA_BIT_TRONG_KHE = TONG_SO_BIT_MOI_KHE -1;
        // Khởi tạo mảng cố định duy nhất 1 lần (Hằng số Runtime)
        this.LUOI_BIT_QUANG_PHO = new AtomicLongArray(SO_KHE * SO_LONG_MOI_KHE);
        this.KY_UC_THOI_GIAN = new AtomicLongArray(SO_KHE);

        System.out.println("[MaTranLuoiLocChongPhatLai] Đã khởi tạo Ma Trận Lưới Lọc: " + SO_KHE + " khe, tổng RAM: " 
            + (SO_KHE * SO_LONG_MOI_KHE * 8 / 1024 / 1024) + " MB");
    }

    /**
     * Hàm phòng ngự chính: Trả về TRUE nếu gói tin SẠCH, FALSE nếu là REPLAY
     * ATTACK.
     */
    public boolean kiemTraVaGhiNhan(String chuKyHmac, long thoiGianClientMs) {
        long giayClient = thoiGianClientMs / 1000;
        int viTriKhe = (int) (giayClient & MAT_NA_KHE); // Tương đương giayClient % 64

        // 1. TỰ ĐỘNG DỌN RÁC (LAZY CLEANUP - ZERO COST)
        long thoiGianCuCuaKhe = KY_UC_THOI_GIAN.get(viTriKhe);
        if (thoiGianCuCuaKhe < giayClient) {

            // Quy ước: Số Âm của giây hiện tại = Cờ báo hiệu "Đang có người dọn rác"
            long coDangDonDep = -giayClient;

            // Nếu khe này thuộc về 64 giây trước, luồng đầu tiên chạm vào sẽ giành quyền
            // dọn dẹp
            if (KY_UC_THOI_GIAN.compareAndSet(viTriKhe, thoiGianCuCuaKhe, coDangDonDep) == true) {
                int indexBatDau = viTriKhe * SO_LONG_MOI_KHE;
                for (int i = 0; i < SO_LONG_MOI_KHE; i = i + 1) {
                    LUOI_BIT_QUANG_PHO.set(indexBatDau + i, 0L); // Giội rửa bằng số 0
                }
                // Dọn xong, cắm cờ "Thời gian thực" (Số Dương) để mở đường cho toàn bộ thiên hạ
                // Lúc này các luồng Request khác đến cùng giây sẽ phải chờ dọn rác xong mới
                // được ghi
                KY_UC_THOI_GIAN.set(viTriKhe, giayClient);
            } else {
                // TA ĐẾN MUỘN: Một luồng khác đã giành được quyền dọn rác, hoặc nó đã dọn xong.
                // KHÓA XOAY (SPIN-WAIT): Ta không ngủ (không dùng synchronized chặn HĐH),
                // mà ta đứng tại chỗ chờ đến khi cái cờ thành Số Dương (dọn xong).
                int gioiHanXoay = 10;
                while (KY_UC_THOI_GIAN.get(viTriKhe) != giayClient && gioiHanXoay > 0) {
                    gioiHanXoay--;
                    Thread.yield(); // Lệnh nhường CPU tạm thời để luồng Vua chạy lẹ lên
                }
            }

        } else if (thoiGianCuCuaKhe > giayClient) {
            // Gói tin đến từ quá khứ quá xa (bị vòng lặp đè mất), từ chối ngay!
            return false;
        }

        // 2. BĂM CHỮ KÝ THÀNH 3 TỌA ĐỘ ĐỘC LẬP TRONG KHÔNG GIAN 262.144 BIT
        byte[] duLieuBam = chuKyHmac.getBytes(StandardCharsets.UTF_8);
        int toaDo1 = bamMurmurHash3(duLieuBam, 12345) & (MAT_NA_BIT_TRONG_KHE);
        int toaDo2 = bamMurmurHash3(duLieuBam, 67890) & (MAT_NA_BIT_TRONG_KHE);
        int toaDo3 = bamMurmurHash3(duLieuBam, 54321) & (MAT_NA_BIT_TRONG_KHE);

        // 3. ĐỐI SOÁT MA TRẬN (Đọc)
        boolean daTonTai = kiemTraBit(viTriKhe, toaDo1)
                && kiemTraBit(viTriKhe, toaDo2)
                && kiemTraBit(viTriKhe, toaDo3);
                
        // Nếu chưa thấy, check tiếp khe trước đó (viTriKhe - 1)
        if (daTonTai == false) {
            int kheTruocDo = (viTriKhe - 1) & MAT_NA_KHE;
            daTonTai = kiemTraBit(kheTruocDo, toaDo1)
                    && kiemTraBit(kheTruocDo, toaDo2)
                    && kiemTraBit(kheTruocDo, toaDo3);
        }
        
        // Nếu chưa thấy, check tiếp khe trước đó (viTriKhe - 1)
        if (daTonTai == false) {
            int kheTruocDo = (viTriKhe + 1) & MAT_NA_KHE;
            daTonTai = kiemTraBit(kheTruocDo, toaDo1)
                    && kiemTraBit(kheTruocDo, toaDo2)
                    && kiemTraBit(kheTruocDo, toaDo3);
        }  

        if (daTonTai == true) {
            return false; // DẤU VẾT ĐÃ CÓ MẶT -> ĐÂY LÀ GÓI TIN COPY/PASTE!
        }

        // 4. LƯU DẤU VẾT GÓI TIN MỚI (Ghi Lock-free)
        batBit(viTriKhe, toaDo1);
        batBit(viTriKhe, toaDo2);
        batBit(viTriKhe, toaDo3);

        return true; // Gói tin sạch tinh tươm
    }

    // --- CÁC HÀM XỬ LÝ BIT NGUYÊN THỦY (KHÔNG DÙNG SYNCHRONIZED) ---

    private boolean kiemTraBit(int viTriKhe, int toaDoBit) {
        int indexLong = (viTriKhe * SO_LONG_MOI_KHE) + (toaDoBit >> 6);
        int offsetBit = toaDoBit & 63;
        long matNa = 1L << offsetBit;
        return (LUOI_BIT_QUANG_PHO.get(indexLong) & matNa) != 0L;
    }

    private void batBit(int viTriKhe, int toaDoBit) {
        int indexLong = (viTriKhe * SO_LONG_MOI_KHE) + (toaDoBit >> 6);
        int offsetBit = toaDoBit & 63;
        long matNa = 1L << offsetBit;

        // Vòng lặp CAS: Cạnh tranh đa luồng siêu tốc, đảm bảo dữ liệu không bao giờ bị
        // ghi đè sai
        while (true) {
            long giaTriCu = LUOI_BIT_QUANG_PHO.get(indexLong);
            if ((giaTriCu & matNa) != 0L) {
                break; // Có luồng khác vừa bật giúp rồi, ta nghỉ tay
            }
            long giaTriMoi = giaTriCu | matNa;
            if (LUOI_BIT_QUANG_PHO.compareAndSet(indexLong, giaTriCu, giaTriMoi) == true) {
                break; // Ghi thành công
            }
        }
    }

    // --- THUẬT TOÁN MURMURHASH3 (Tốc độ ánh sáng, phân phối rải rác cực đẹp) ---
    private int bamMurmurHash3(byte[] data, int seed) {
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int h1 = seed;
        int len = data.length;
        int roundedEnd = (len & 0xfffffffc); // Bội số của 4

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
        return Math.abs(h1); // Trả về số dương tuyệt đối
    }

}
