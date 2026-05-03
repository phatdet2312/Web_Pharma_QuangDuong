//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/PublicReportServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentReportRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Cmt;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtCmtReport;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPhCmtReport;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PhCmt;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtCmtReportRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPhCmtReportRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPhCmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IPublicReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * =========================================================================
 * THỰC THI DỊCH VỤ: BÁO CÁO VI PHẠM (PUBLIC)
 * =========================================================================
 * Tuân thủ Chuẩn mù: Controller không truyền IP. Service tự móc IP từ Request.
 */
@Service
@RequiredArgsConstructor
public class PublicReportServiceImpl implements IPublicReportService {

    private final ICmtRepository cmtRepository;
    private final IPhCmtRepository phCmtRepository;
    private final IUserRepository userRepository;
    private final ICtCmtReportRepository cmtReportRepository;
    private final ICtPhCmtReportRepository phCmtReportRepository;

    @Override
    @Transactional
    public void guiBaoCao(CommentReportRequest request, Long userId) {
        // Tự động móc IP trực tiếp từ Request Context (Chuẩn mù)
        String[] netInfo = trichXuatThongTinMang();
        String ipAddress = netInfo[0];

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent() == false) {
            throw new AppException(401, "Danh tính người báo cáo không hợp lệ.");
        }
        User user = optUser.get();

        // Rẽ nhánh: Báo cáo Bình luận Cấp 1
        if (request.getTargetType().equals("CMT") == true) {
            Optional<Cmt> optCmt = cmtRepository.findById(request.getTargetId());
            if (optCmt.isPresent() == false) {
                throw new AppException(404, "Không tìm thấy nội dung bình luận để báo cáo.");
            }

            CtCmtReport report = new CtCmtReport();
            report.setCmt(optCmt.get());
            report.setUser(user);
            report.setReason(request.getReason());
            report.setReporterIp(ipAddress);
            report.setStatus("PENDING");
            
            cmtReportRepository.save(report);
        } 
        // Rẽ nhánh: Báo cáo Phản hồi Cấp 2
        else if (request.getTargetType().equals("PH_CMT") == true) {
            Optional<PhCmt> optPh = phCmtRepository.findById(request.getTargetId());
            if (optPh.isPresent() == false) {
                throw new AppException(404, "Không tìm thấy nội dung phản hồi để báo cáo.");
            }

            CtPhCmtReport report = new CtPhCmtReport();
            report.setPhCmt(optPh.get());
            report.setUser(user);
            report.setReason(request.getReason());
            report.setReporterIp(ipAddress);
            report.setStatus("PENDING");

            phCmtReportRepository.save(report);
        } 
        else {
            throw new AppException(400, "Cấu trúc phân loại mục tiêu không được hệ thống hỗ trợ.");
        }
    }

    /**
     * Hàm nội bộ: Bóc tách IP xuyên qua Proxy để chống giả mạo.
     */
    private String[] trichXuatThongTinMang() {
        String ipAddress = "Unknown";
        String userAgent = "Unknown";

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && forwardedFor.isEmpty() == false) {
                ipAddress = forwardedFor.split(",")[0].trim();
            } else {
                ipAddress = request.getRemoteAddr();
            }
            String uaHeader = request.getHeader("User-Agent");
            if (uaHeader != null) {
                userAgent = uaHeader.length() > 490 ? uaHeader.substring(0, 490) : uaHeader;
            }
        }
        return new String[]{ipAddress, userAgent};
    }
}