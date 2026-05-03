//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AdminReportServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ReportResolutionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CommentReportResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ReportModLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * =========================================================================
 * THỰC THI DỊCH VỤ: QUẢN TRỊ BÁO CÁO VÀ KIỂM TOÁN (ADMIN)
 * =========================================================================
 */
@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements IAdminReportService {

    private final IUserRepository userRepository;
    private final IModerationActionRepository actionRepository;

    private final ICtCmtReportRepository cmtReportRepository;
    private final ICtPhCmtReportRepository phCmtReportRepository;

    private final ICtCmtReportModLogRepository cmtReportModLogRepository;
    private final ICtPhCmtReportModLogRepository phCmtReportModLogRepository;

    @Override
    public List<CommentReportResponse> layDanhSachBaoCao(String targetType, String status) {
        List<CommentReportResponse> responseList = new ArrayList<>();

        if (targetType.equals("CMT") == true) {
            List<CtCmtReport> listTuDb = cmtReportRepository.findAll();
            Object[] arr = listTuDb.toArray(); // Tuân thủ vòng lặp nguyên thủy

            for (int i = 0; i < arr.length; i = i + 1) {
                CtCmtReport rp = (CtCmtReport) arr[i];
                
                boolean phuHopStatus = true;
                if (status != null && status.trim().isEmpty() == false) {
                    if (rp.getStatus().equals(status) == false) {
                        phuHopStatus = false;
                    }
                }

                if (phuHopStatus == true) {
                    CommentReportResponse dto = new CommentReportResponse();
                    dto.setId(rp.getId());
                    dto.setTargetId(rp.getCmt().getId());
                    dto.setTargetType("CMT");
                    dto.setTargetContent(rp.getCmt().getContent());
                    dto.setTargetAuthorName(rp.getCmt().getUser().getFullName() != null ? rp.getCmt().getUser().getFullName() : rp.getCmt().getUser().getUsername());
                    
                    dto.setReporterId(rp.getUser().getId());
                    dto.setReporterName(rp.getUser().getFullName() != null ? rp.getUser().getFullName() : rp.getUser().getUsername());
                    dto.setReporterEmail(rp.getUser().getEmail());
                    dto.setReporterIp(rp.getReporterIp());
                    dto.setReason(rp.getReason());
                    dto.setStatus(rp.getStatus());
                    dto.setCreatedAt(rp.getCreatedAt());

                    responseList.add(dto);
                }
            }
        } else if (targetType.equals("PH_CMT") == true) {
            List<CtPhCmtReport> listTuDb = phCmtReportRepository.findAll();
            Object[] arr = listTuDb.toArray();

            for (int i = 0; i < arr.length; i = i + 1) {
                CtPhCmtReport rp = (CtPhCmtReport) arr[i];
                
                boolean phuHopStatus = true;
                if (status != null && status.trim().isEmpty() == false) {
                    if (rp.getStatus().equals(status) == false) {
                        phuHopStatus = false;
                    }
                }

                if (phuHopStatus == true) {
                    CommentReportResponse dto = new CommentReportResponse();
                    dto.setId(rp.getId());
                    dto.setTargetId(rp.getPhCmt().getId());
                    dto.setTargetType("PH_CMT");
                    dto.setTargetContent(rp.getPhCmt().getContent());
                    dto.setTargetAuthorName(rp.getPhCmt().getUser().getFullName() != null ? rp.getPhCmt().getUser().getFullName() : rp.getPhCmt().getUser().getUsername());
                    
                    dto.setReporterId(rp.getUser().getId());
                    dto.setReporterName(rp.getUser().getFullName() != null ? rp.getUser().getFullName() : rp.getUser().getUsername());
                    dto.setReporterEmail(rp.getUser().getEmail());
                    dto.setReporterIp(rp.getReporterIp());
                    dto.setReason(rp.getReason());
                    dto.setStatus(rp.getStatus());
                    dto.setCreatedAt(rp.getCreatedAt());

                    responseList.add(dto);
                }
            }
        }

        return responseList;
    }

    @Override
    @Transactional
    public void xuLyBaoCao(ReportResolutionRequest request, Long moderatorId) {
        String[] netInfo = trichXuatThongTinMang();
        String ipAddress = netInfo[0];
        String userAgent = netInfo[1];

        Optional<User> optModerator = userRepository.findById(moderatorId);
        if (optModerator.isPresent() == false) {
            throw new AppException(401, "Danh tính Quản trị viên không hợp lệ.");
        }
        User moderator = optModerator.get();

        Optional<ModerationAction> optAction = actionRepository.findByCode(request.getActionCode());
        if (optAction.isPresent() == false) {
            throw new AppException(400, "Mã hành vi kiểm duyệt không tồn tại: " + request.getActionCode());
        }
        ModerationAction action = optAction.get();

        // Cập nhật CSDL và Đóng dấu Sổ tay Kiểm toán
        if (request.getTargetType().equals("CMT") == true) {
            Optional<CtCmtReport> optReport = cmtReportRepository.findById(request.getReportId());
            if (optReport.isPresent() == false) {
                throw new AppException(404, "Không tìm thấy hồ sơ báo cáo.");
            }
            CtCmtReport report = optReport.get();
            String oldStatus = report.getStatus();
            String newStatus = request.getActionCode().equals("RESOLVE_REPORT") ? "RESOLVED" : "REJECTED";

            report.setStatus(newStatus);
            cmtReportRepository.save(report);

            CtCmtReportModLog log = new CtCmtReportModLog();
            log.setReport(report);
            log.setAction(action);
            log.setModerator(moderator);
            log.setOldStatus(oldStatus);
            log.setNewStatus(newStatus);
            log.setReason(request.getReason());
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            
            cmtReportModLogRepository.save(log);

        } else if (request.getTargetType().equals("PH_CMT") == true) {
            Optional<CtPhCmtReport> optReport = phCmtReportRepository.findById(request.getReportId());
            if (optReport.isPresent() == false) {
                throw new AppException(404, "Không tìm thấy hồ sơ báo cáo.");
            }
            CtPhCmtReport report = optReport.get();
            String oldStatus = report.getStatus();
            String newStatus = request.getActionCode().equals("RESOLVE_REPORT") ? "RESOLVED" : "REJECTED";

            report.setStatus(newStatus);
            phCmtReportRepository.save(report);

            CtPhCmtReportModLog log = new CtPhCmtReportModLog();
            log.setReport(report);
            log.setAction(action);
            log.setModerator(moderator);
            log.setOldStatus(oldStatus);
            log.setNewStatus(newStatus);
            log.setReason(request.getReason());
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);

            phCmtReportModLogRepository.save(log);
        } else {
            throw new AppException(400, "Cấu trúc phân loại mục tiêu không hợp lệ.");
        }
    }

    @Override
    public List<ReportModLogResponse> layLichSuXuLy(Long reportId, String targetType) {
        List<ReportModLogResponse> responseList = new ArrayList<>();

        if (targetType.equals("CMT") == true) {
            List<CtCmtReportModLog> logs = cmtReportModLogRepository.findByReportIdOrderByCreatedAtDesc(reportId);
            Object[] arr = logs.toArray();
            for (int i = 0; i < arr.length; i = i + 1) {
                CtCmtReportModLog log = (CtCmtReportModLog) arr[i];
                ReportModLogResponse dto = new ReportModLogResponse();
                dto.setId(log.getId());
                dto.setReportId(log.getReport().getId());
                dto.setActionCode(log.getAction().getCode());
                dto.setActionName(log.getAction().getName());
                dto.setOldStatus(log.getOldStatus());
                dto.setNewStatus(log.getNewStatus());
                dto.setReason(log.getReason());
                dto.setModeratorId(log.getModerator().getId());
                dto.setModeratorName(log.getModerator().getFullName() != null ? log.getModerator().getFullName() : log.getModerator().getUsername());
                dto.setIpAddress(log.getIpAddress());
                dto.setUserAgent(log.getUserAgent());
                dto.setCreatedAt(log.getCreatedAt());
                
                responseList.add(dto);
            }
        } else if (targetType.equals("PH_CMT") == true) {
            List<CtPhCmtReportModLog> logs = phCmtReportModLogRepository.findByReportIdOrderByCreatedAtDesc(reportId);
            Object[] arr = logs.toArray();
            for (int i = 0; i < arr.length; i = i + 1) {
                CtPhCmtReportModLog log = (CtPhCmtReportModLog) arr[i];
                ReportModLogResponse dto = new ReportModLogResponse();
                dto.setId(log.getId());
                dto.setReportId(log.getReport().getId());
                dto.setActionCode(log.getAction().getCode());
                dto.setActionName(log.getAction().getName());
                dto.setOldStatus(log.getOldStatus());
                dto.setNewStatus(log.getNewStatus());
                dto.setReason(log.getReason());
                dto.setModeratorId(log.getModerator().getId());
                dto.setModeratorName(log.getModerator().getFullName() != null ? log.getModerator().getFullName() : log.getModerator().getUsername());
                dto.setIpAddress(log.getIpAddress());
                dto.setUserAgent(log.getUserAgent());
                dto.setCreatedAt(log.getCreatedAt());
                
                responseList.add(dto);
            }
        }

        return responseList;
    }

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