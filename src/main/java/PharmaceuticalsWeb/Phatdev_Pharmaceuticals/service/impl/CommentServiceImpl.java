//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/CommentServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentModerationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LoaiLikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ReplyRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminCmtContextResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtActionLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CommentStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LoaiLikeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PhCmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ICommentService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * =========================================================================
 * THỰC THI NGHIỆP VỤ ĐIỀU PHỐI BÌNH LUẬN VÀ LƯU VẾT HÀNH VI TỰ THÂN
 * =========================================================================
 * Quản trị toàn bộ vòng đời dữ liệu của hệ sinh thái nội dung tương tác cộng đồng.
 * Đặc tả kiến trúc:
 * - Bảo vệ tính toàn vẹn của Dữ liệu Y khoa thông qua Sổ tay Tự thân (Action Logs),
 * nhằm niêm phong IP và dữ liệu thô khi người dùng thực thi các thao tác Write.
 * - Triển khai thuật toán Đệ quy để đóng gói Phản hồi lồng cấp.
 * - Tuân thủ kỷ luật mảng nguyên thủy (For-loop, Object[]), cấm sử dụng Lambda/Stream.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final ICmtRepository cmtRepository;
    private final IPhCmtRepository phCmtRepository;
    private final ICtPostCmtRepository ctPostCmtRepository;
    private final ICtEventCmtRepository ctEventCmtRepository;
    private final ICtCmtReportRepository ctCmtReportRepository;
    private final ICtLikeCmtRepository ctLikeCmtRepository;
    private final ICtLikePhCmtRepository ctLikePhCmtRepository;
    private final ILoaiLikeRepository loaiLikeRepository;
    private final ICtCmtModerationLogRepository cmtModerationLogRepository;
    private final ICtPhCmtModerationLogRepository phCmtModerationLogRepository;
    private final IModerationActionRepository moderationActionRepository;
    private final IUserRepository userRepository;
    private final ICtUserRoleRepository ctUserRoleRepository;
    private final IUserRoleRepository userRoleRepository;
    private final IPostRepository postRepository;
    private final ICtEventRepository ctEventRepository;
    private final IUserService userService;
    private final IAuditService auditService;
    
    /** Không gian lưu trữ Sổ tay kiểm toán bảo mật do chính người dùng tác động */
    private final ICtCmtActionLogRepository actionLogRepository;
    private final ICtPhCmtActionLogRepository phActionLogRepository;

    // =========================================================================
    // KHỐI GIAO DIỆN NGƯỜI DÙNG: TRUY XUẤT BÌNH LUẬN CÔNG KHAI
    // =========================================================================

    /**
     * Vận hành Cỗ máy trích xuất Bình luận Phân trang dành riêng cho Bài viết.
     * Áp dụng thuật toán định tuyến cơ sở dữ liệu để chọn lọc nhánh Sắp xếp
     * (Thích nhiều nhất hoặc Mới nhất) dựa trên tham số đầu vào.
     */
    @Override
    public Page<CmtResponse> layCmtCuaBaiViet(Long postId, String sortBy, int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cmt> cmtPage;

        if ("popular".equals(sortBy) == true) {
            cmtPage = cmtRepository.layCmtCuaBaiVietTheoLuotThich(postId, pageable);
        } else {
            cmtPage = cmtRepository.layCmtCuaBaiVietTheoThoiGian(postId, pageable);
        }

        List<Cmt> cmtListTuDb = cmtPage.getContent();
        List<CmtResponse> responseList = new ArrayList<>();
        
        Object[] arr = cmtListTuDb.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            responseList.add(xayDungCmtResponse((Cmt) arr[i], userId));
        }
        
        return new PageImpl<>(responseList, pageable, cmtPage.getTotalElements());
    }

    /**
     * Vận hành Cỗ máy trích xuất Bình luận Phân trang dành riêng cho Trạm sự kiện.
     * Chuyển tải đối tượng Lõi sang dạng Đối tượng truyền tải (DTO) để 
     * triệt tiêu toàn bộ thuộc tính nhạy cảm trước khi cung cấp cho Client-side.
     */
    @Override
    public Page<CmtResponse> layCmtCuaBuoi(Long ctEventId, String sortBy, int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cmt> cmtPage;

        if ("popular".equals(sortBy) == true) {
            cmtPage = cmtRepository.layCmtCuaSuKienTheoLuotThich(ctEventId, pageable);
        } else {
            cmtPage = cmtRepository.layCmtCuaSuKienTheoThoiGian(ctEventId, pageable);
        }

        List<Cmt> cmtListTuDb = cmtPage.getContent();
        List<CmtResponse> responseList = new ArrayList<>();
        
        Object[] arr = cmtListTuDb.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            responseList.add(xayDungCmtResponse((Cmt) arr[i], userId));
        }
        
        return new PageImpl<>(responseList, pageable, cmtPage.getTotalElements());
    }

    // =========================================================================
    // KHỐI QUẢN TRỊ TƯƠNG TÁC & NIÊM PHONG SỔ TAY TỰ THÂN (ACTION LOG)
    // =========================================================================

    /**
     * Kiến tạo Bình luận gốc cho một Bài viết y khoa.
     * Thiết lập cầu nối Định tuyến Đa hình (5NF), đồng thời trích xuất IP Address 
     * để niêm phong dữ liệu thô vào Nhật ký tự thân, tạo bằng chứng pháp lý.
     */
    @Override
    @Transactional
    public CmtResponse guiCmtBaiViet(CommentRequest request, Long userId) {
        if (postRepository.existsById(request.getTargetId()) == false) {
            throw new AppException(404, "Không tìm thấy Bài viết mục tiêu để thiết lập liên kết dữ liệu.");
        }

        User user = layUserHoacLoi(userId);

        Cmt cmt = new Cmt();
        cmt.setUser(user);
        cmt.setContent(request.getContent().trim());
        cmt.setCreatedAt(LocalDateTime.now());
        cmt.setUpdatedAt(LocalDateTime.now());
        
        Cmt saved = cmtRepository.save(cmt);

        CtPostCmt.CtPostCmtId pkId = new CtPostCmt.CtPostCmtId(request.getTargetId(), saved.getId());
        CtPostCmt bridge = new CtPostCmt();
        bridge.setId(pkId);
        bridge.setPost(postRepository.findById(request.getTargetId()).get());
        bridge.setCmt(saved);
        ctPostCmtRepository.save(bridge);

        ghiNhanNhatKyTuThanCmt(saved, user, "CREATE_CMT", null, taoJsonPayloadCmt(saved));

        return xayDungCmtResponse(saved, userId);
    }

    /**
     * Kiến tạo Bình luận gốc cho một Trạm Sự kiện.
     * Áp dụng quy trình bảo mật và niêm phong tương tự bài viết.
     */
    @Override
    @Transactional
    public CmtResponse guiCmtSuKien(CommentRequest request, Long userId) {
        if (ctEventRepository.existsById(request.getTargetId()) == false) {
            throw new AppException(404, "Không tìm thấy Trạm sự kiện mục tiêu để thiết lập liên kết dữ liệu.");
        }

        User user = layUserHoacLoi(userId);

        Cmt cmt = new Cmt();
        cmt.setUser(user);
        cmt.setContent(request.getContent().trim());
        cmt.setCreatedAt(LocalDateTime.now());
        cmt.setUpdatedAt(LocalDateTime.now());
        
        Cmt saved = cmtRepository.save(cmt);

        CtEventCmt.CtEventCmtId pkId = new CtEventCmt.CtEventCmtId(request.getTargetId(), saved.getId());
        CtEventCmt bridge = new CtEventCmt();
        bridge.setId(pkId);
        bridge.setCtEvent(ctEventRepository.findById(request.getTargetId()).get());
        bridge.setCmt(saved);
        ctEventCmtRepository.save(bridge);

        ghiNhanNhatKyTuThanCmt(saved, user, "CREATE_CMT", null, taoJsonPayloadCmt(saved));

        return xayDungCmtResponse(saved, userId);
    }

    /**
     * Khởi tạo Phản hồi lồng cấp (Nested Reply).
     * Bắt buộc xác thực khóa ngoại Mỏ Neo (RootCmt) để duy trì cấu trúc Cây Bình Luận.
     */
    @Override
    @Transactional
    public PhCmtResponse guiPhCmt(ReplyRequest request, Long userId) {
        Optional<Cmt> optRootCmt = cmtRepository.findById(request.getRootCmtId());
        if (optRootCmt.isPresent() == false) {
            throw new AppException(404, "Tọa độ Mỏ neo của Bình luận gốc đã bị từ chối do không tồn tại.");
        }

        User user = layUserHoacLoi(userId);

        PhCmt phCmt = new PhCmt();
        phCmt.setRootCmt(optRootCmt.get());
        phCmt.setUser(user);
        phCmt.setContent(request.getContent().trim());
        phCmt.setCreatedAt(LocalDateTime.now());
        phCmt.setUpdatedAt(LocalDateTime.now());

        if (request.getParentPhId() != null) {
            Optional<PhCmt> optParent = phCmtRepository.findById(request.getParentPhId());
            if (optParent.isPresent() == true) {
                phCmt.setParentPh(optParent.get());
            }
        }

        PhCmt saved = phCmtRepository.save(phCmt);

        ghiNhanNhatKyTuThanPhCmt(saved, user, "CREATE_PH_CMT", null, taoJsonPayloadPhCmt(saved));

        return xayDungPhCmtResponse(saved, userId);
    }

    /**
     * Hiệu đính nội dung Bình luận gốc bởi chính Tác giả.
     * Thuật toán: Chụp lại Cấu trúc dữ liệu cũ (Old Payload) trước khi ghi đè, 
     * lưu trữ cả hai phiên bản vào Nhật ký để tạo vòng lặp đối soát.
     */
    @Override
    @Transactional
    public CmtResponse capNhatCmt(Long cmtId, CommentRequest request, Long userId) {
        Optional<Cmt> optCmt = cmtRepository.findById(cmtId);
        if (optCmt.isPresent() == false) {
            throw new AppException(404, "Bản ghi bình luận này không còn tồn tại trên cơ sở dữ liệu.");
        }

        Cmt cmt = optCmt.get();
        if (cmt.getUser().getId().equals(userId) == false) {
            throw new AppException(403, "Lá chắn bảo mật: Bạn không có quyền thao tác trên tài sản kỹ thuật số của người khác.");
        }

        String oldPayload = taoJsonPayloadCmt(cmt);

        cmt.setContent(request.getContent().trim());
        cmt.setUpdatedAt(LocalDateTime.now());
        Cmt saved = cmtRepository.save(cmt);

        String newPayload = taoJsonPayloadCmt(saved);
        ghiNhanNhatKyTuThanCmt(saved, cmt.getUser(), "UPDATE_CMT", oldPayload, newPayload);

        return xayDungCmtResponse(saved, userId);
    }

    /**
     * Tiêu hủy Bình luận gốc và gỡ bỏ Cầu nối Định tuyến đa hình.
     */
    @Override
    @Transactional
    public void xoaCmt(Long cmtId, Long userId) {
        Optional<Cmt> optCmt = cmtRepository.findById(cmtId);
        if (optCmt.isPresent() == false) {
            throw new AppException(404, "Hệ thống không tìm thấy bình luận được yêu cầu.");
        }

        Cmt cmt = optCmt.get();
        if (cmt.getUser().getId().equals(userId) == false) {
            throw new AppException(403, "Lá chắn bảo mật: Giao dịch bị từ chối do xung đột định danh.");
        }

        String oldPayload = taoJsonPayloadCmt(cmt);
        ghiNhanNhatKyTuThanCmt(cmt, cmt.getUser(), "DELETE_CMT", oldPayload, null);

        ctPostCmtRepository.xoaLienKetTheoCmt(cmtId);
        ctEventCmtRepository.xoaLienKetTheoCmt(cmtId);
        cmtRepository.delete(cmt);
    }

    /**
     * Hiệu đính nội dung Phản hồi thứ cấp. 
     * Triển khai cơ chế niêm phong tương tự như Bình luận gốc.
     */
    @Override
    @Transactional
    public PhCmtResponse capNhatPhCmt(Long phCmtId, ReplyRequest request, Long userId) {
        Optional<PhCmt> optPh = phCmtRepository.findById(phCmtId);
        if (optPh.isPresent() == false) {
            throw new AppException(404, "Phân nhánh phản hồi này không còn tồn tại.");
        }

        PhCmt ph = optPh.get();
        if (ph.getUser().getId().equals(userId) == false) {
            throw new AppException(403, "Lá chắn bảo mật: Giao dịch bị từ chối do xung đột định danh.");
        }

        String oldPayload = taoJsonPayloadPhCmt(ph);

        ph.setContent(request.getContent().trim());
        ph.setUpdatedAt(LocalDateTime.now());
        PhCmt saved = phCmtRepository.save(ph);

        String newPayload = taoJsonPayloadPhCmt(saved);
        ghiNhanNhatKyTuThanPhCmt(saved, ph.getUser(), "UPDATE_PH_CMT", oldPayload, newPayload);

        return xayDungPhCmtResponse(saved, userId);
    }

    /**
     * Tiêu hủy Phản hồi thứ cấp.
     */
    @Override
    @Transactional
    public void xoaPhCmt(Long phCmtId, Long userId) {
        Optional<PhCmt> optPh = phCmtRepository.findById(phCmtId);
        if (optPh.isPresent() == false) {
            throw new AppException(404, "Hệ thống không tìm thấy phản hồi được yêu cầu.");
        }

        PhCmt ph = optPh.get();
        if (ph.getUser().getId().equals(userId) == false) {
            throw new AppException(403, "Lá chắn bảo mật: Giao dịch bị từ chối do xung đột định danh.");
        }

        String oldPayload = taoJsonPayloadPhCmt(ph);
        ghiNhanNhatKyTuThanPhCmt(ph, ph.getUser(), "DELETE_CMT", oldPayload, null);

        phCmtRepository.delete(ph);
    }

    /**
     * Kích hoạt Cơ chế thả Cảm xúc trên Bình luận Gốc.
     * Vận dụng Khóa Kép (Composite Key) để chèn hoặc gỡ bỏ Cảm xúc, tránh tình trạng lặp dữ liệu.
     */
    @Override
    @Transactional
    public void thichCmt(LikeRequest request, Long userId) {
        User user = layUserHoacLoi(userId);

        Optional<Cmt> optCmt = cmtRepository.findById(request.getTargetId());
        if (optCmt.isPresent() == false) {
            throw new AppException(404, "Đối tượng bình luận không hợp lệ.");
        }

        Optional<LoaiLike> optLoai = loaiLikeRepository.findByCode(request.getLoaiLikeCode());
        if (optLoai.isPresent() == false) {
            throw new AppException(400, "Mã định danh cảm xúc từ Frontend không nằm trong từ điển cho phép.");
        }

        Optional<CtLikeCmt> optExisting = ctLikeCmtRepository.findById_UserIdAndId_CmtId(userId, request.getTargetId());

        if (optExisting.isPresent() == true) {
            CtLikeCmt existing = optExisting.get();
            if (existing.getLoaiLike().getId().equals(optLoai.get().getId()) == true) {
                ctLikeCmtRepository.delete(existing);
            } else {
                existing.setLoaiLike(optLoai.get());
                ctLikeCmtRepository.save(existing);
            }
        } else {
            CtLikeCmt.CtLikeCmtId pkId = new CtLikeCmt.CtLikeCmtId(userId, request.getTargetId());
            CtLikeCmt like = new CtLikeCmt();
            like.setId(pkId);
            like.setUser(user);
            like.setCmt(optCmt.get());
            like.setLoaiLike(optLoai.get());
            like.setCreatedAt(LocalDateTime.now());
            ctLikeCmtRepository.save(like);
        }
    }

    /**
     * Kích hoạt Cơ chế thả Cảm xúc trên Phản hồi.
     */
    @Override
    @Transactional
    public void thichPhCmt(LikeRequest request, Long userId) {
        User user = layUserHoacLoi(userId);

        Optional<PhCmt> optPhCmt = phCmtRepository.findById(request.getTargetId());
        if (optPhCmt.isPresent() == false) {
            throw new AppException(404, "Đối tượng phản hồi không hợp lệ.");
        }

        Optional<LoaiLike> optLoai = loaiLikeRepository.findByCode(request.getLoaiLikeCode());
        if (optLoai.isPresent() == false) {
            throw new AppException(400, "Mã định danh cảm xúc không hợp lệ.");
        }

        Optional<CtLikePhCmt> optExisting = ctLikePhCmtRepository.findById_UserIdAndId_PhCmtId(userId, request.getTargetId());

        if (optExisting.isPresent() == true) {
            CtLikePhCmt existing = optExisting.get();
            if (existing.getLoaiLike().getId().equals(optLoai.get().getId()) == true) {
                ctLikePhCmtRepository.delete(existing);
            } else {
                existing.setLoaiLike(optLoai.get());
                ctLikePhCmtRepository.save(existing);
            }
        } else {
            CtLikePhCmt.CtLikePhCmtId pkId = new CtLikePhCmt.CtLikePhCmtId(userId, request.getTargetId());
            CtLikePhCmt like = new CtLikePhCmt();
            like.setId(pkId);
            like.setUser(user);
            like.setPhCmt(optPhCmt.get());
            like.setLoaiLike(optLoai.get());
            like.setCreatedAt(LocalDateTime.now());
            ctLikePhCmtRepository.save(like);
        }
    }

    // =========================================================================
    // TRÍCH XUẤT NHẬT KÝ TỰ THÂN (GIẢI QUYẾT NÚT THẮT QUẢN TRỊ BẢO MẬT)
    // =========================================================================
    
    /**
     * Bóc tách toàn bộ lịch sử chỉnh sửa văn bản của một Bình luận.
     * Cung cấp bằng chứng minh bạch phục vụ cho cả Quản trị viên và người viết.
     */
    @Override
    public List<CmtActionLogResponse> layLichSuTuThanCmt(Long cmtId) {
        List<CtCmtActionLog> logs = actionLogRepository.findByCmtIdOrderByCreatedAtDesc(cmtId);
        List<CmtActionLogResponse> responseList = new ArrayList<>();
        
        Object[] arr = logs.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtCmtActionLog log = (CtCmtActionLog) arr[i];
            
            String actionName = "Không xác định";
            String actionCode = "UNKNOWN";
            Optional<ModerationAction> optAction = moderationActionRepository.findById(log.getActionId());
            if (optAction.isPresent() == true) {
                actionName = optAction.get().getName();
                actionCode = optAction.get().getCode();
            }

            String authorName = "Tài khoản bị xóa";
            Optional<User> optUser = userRepository.findById(log.getUserId());
            if (optUser.isPresent() == true) {
                if (optUser.get().getFullName() != null) {
                    authorName = optUser.get().getFullName();
                } else {
                    authorName = optUser.get().getUsername();
                }
            }

            CmtActionLogResponse dto = new CmtActionLogResponse();
            dto.setId(log.getId());
            dto.setTargetId(log.getCmtId());
            dto.setUserId(log.getUserId());
            dto.setAuthorName(authorName);
            dto.setActionCode(actionCode);
            dto.setActionName(actionName);
            dto.setOldPayload(log.getOldPayload());
            dto.setNewPayload(log.getNewPayload());
            dto.setIpAddress(log.getIpAddress());
            dto.setUserAgent(log.getUserAgent());
            dto.setCreatedAt(log.getCreatedAt());
            
            responseList.add(dto);
        }
        return responseList;
    }

    /**
     * Bóc tách toàn bộ lịch sử chỉnh sửa văn bản của một Phản hồi.
     */
    @Override
    public List<CmtActionLogResponse> layLichSuTuThanPhCmt(Long phCmtId) {
        List<CtPhCmtActionLog> logs = phActionLogRepository.findByPhCmtIdOrderByCreatedAtDesc(phCmtId);
        List<CmtActionLogResponse> responseList = new ArrayList<>();
        
        Object[] arr = logs.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtPhCmtActionLog log = (CtPhCmtActionLog) arr[i];
            
            String actionName = "Không xác định";
            String actionCode = "UNKNOWN";
            Optional<ModerationAction> optAction = moderationActionRepository.findById(log.getActionId());
            if (optAction.isPresent() == true) {
                actionName = optAction.get().getName();
                actionCode = optAction.get().getCode();
            }

            String authorName = "Tài khoản bị xóa";
            Optional<User> optUser = userRepository.findById(log.getUserId());
            if (optUser.isPresent() == true) {
                if (optUser.get().getFullName() != null) {
                    authorName = optUser.get().getFullName();
                } else {
                    authorName = optUser.get().getUsername();
                }
            }

            CmtActionLogResponse dto = new CmtActionLogResponse();
            dto.setId(log.getId());
            dto.setTargetId(log.getPhCmtId());
            dto.setUserId(log.getUserId());
            dto.setAuthorName(authorName);
            dto.setActionCode(actionCode);
            dto.setActionName(actionName);
            dto.setOldPayload(log.getOldPayload());
            dto.setNewPayload(log.getNewPayload());
            dto.setIpAddress(log.getIpAddress());
            dto.setUserAgent(log.getUserAgent());
            dto.setCreatedAt(log.getCreatedAt());
            
            responseList.add(dto);
        }
        return responseList;
    }

    // =========================================================================
    // KHỐI QUẢN TRỊ ADMIN (KIỂM DUYỆT VÀ LỌC ĐA CHIỀU)
    // =========================================================================

    /**
     * G4: Tìm kiếm đa chiều — trả về AdminCmtContextResponse có context nguồn gốc.
     * Batch-load mapping POST/EVENT sau khi query để tránh N+1.
     */
    @Override
    public Page<AdminCmtContextResponse> timKiemBinhLuanAdmin(String keyword, String status, LocalDateTime startDate, LocalDateTime endDate, Long targetId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }
        String st = null;
        if (status != null && status.trim().isEmpty() == false) {
            st = status.trim();
        }

        Page<Cmt> cmtPage = cmtRepository.timKiemBinhLuanNangCao(kw, st, startDate, endDate, targetId, pageable);
        return dinhKemContextVaTaoTrang(cmtPage, pageable);
    }

    /**
     * G4: Lấy danh sách phân trang theo tab — trả về AdminCmtContextResponse có context nguồn gốc.
     */
    @Override
    public Page<AdminCmtContextResponse> layDanhSachBinhLuan(String status, String targetType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cmt> cmts;

        if ("EVENT".equals(targetType) == true) {
            cmts = cmtRepository.layCmtSuKienPage(pageable);
        } else if ("POST".equals(targetType) == true) {
            cmts = cmtRepository.layCmtBaiVietPage(pageable);
        } else if ("PENDING".equals(status) == true) {
            cmts = cmtRepository.layCmtChuaDuyetPage(pageable);
        } else if ("HIDE".equals(status) == true || "WARN".equals(status) == true) {
            cmts = cmtRepository.layCmtTheoTrangThai(status, pageable);
        } else {
            cmts = cmtRepository.layTatCaCmt(pageable);
        }

        return dinhKemContextVaTaoTrang(cmts, pageable);
    }

    /**
     * G4: Hàm Batch-Load Context — Dùng chung cho cả 2 method admin search.
     * Thuật toán: Thu thập toàn bộ cmtId của trang → batch query 2 bảng bridge
     * → xây dựng Map tra cứu → gắn context vào từng bình luận.
     * Tránh N+1: Chỉ thực thi 2 query phụ cho cả trang, không phải 1 query mỗi record.
     */
   private Page<AdminCmtContextResponse> dinhKemContextVaTaoTrang(Page<Cmt> cmtPage, Pageable pageable) {
        List<Cmt> cmtList = cmtPage.getContent();

        if (cmtList.isEmpty() == true) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        // Bước 1: Thu thập danh sách cmtId của trang hiện tại
        List<Long> cmtIds = new ArrayList<>();
        Object[] cmtArr = cmtList.toArray();
        for (int i = 0; i < cmtArr.length; i = i + 1) {
            cmtIds.add(((Cmt) cmtArr[i]).getId());
        }

        // Bước 2: Batch-load mapping từ bảng bridge CT_POST_CMT
        // Lưu ý: Lưu trực tiếp toàn bộ dòng dữ liệu (Row) nguyên bản từ CSDL
        Map<Long, Object[]> postMapping = new HashMap<>();
        List<Object[]> postRows = ctPostCmtRepository.layCmtIdToPostMapping(cmtIds);
        Object[] postRowArr = postRows.toArray();
        for (int i = 0; i < postRowArr.length; i = i + 1) {
            Object[] row = (Object[]) postRowArr[i];
            Long cmtId = ((Number) row[0]).longValue();
            postMapping.put(cmtId, row); // KHÔNG tạo mảng mới, bảo toàn cấu trúc SQL
        }

        // Bước 3: Batch-load mapping từ bảng bridge CT_EVENT_CMT
        Map<Long, Object[]> eventMapping = new HashMap<>();
        List<Object[]> eventRows = ctEventCmtRepository.layCmtIdToCtEventMapping(cmtIds);
        Object[] eventRowArr = eventRows.toArray();
        for (int i = 0; i < eventRowArr.length; i = i + 1) {
            Object[] row = (Object[]) eventRowArr[i];
            Long cmtId = ((Number) row[0]).longValue();
            eventMapping.put(cmtId, row); // KHÔNG tạo mảng mới, bảo toàn cấu trúc SQL
        }

        // Bước 4: Gắn context vào từng bình luận
        List<AdminCmtContextResponse> responseList = new ArrayList<>();
        for (int i = 0; i < cmtArr.length; i = i + 1) {
            Cmt cmt = (Cmt) cmtArr[i];
            AdminCmtContextResponse ctx = new AdminCmtContextResponse();
            ctx.setCmtData(xayDungCmtResponse(cmt, null));

            Long cmtId = cmt.getId();
            
            // Khai thác đúng tọa độ dựa trên câu lệnh SQL chuẩn
            // SELECT cpc.cmt.id (0), p.id (1), p.title (2), p.slug (3)
            if (postMapping.containsKey(cmtId) == true) {
                Object[] row = postMapping.get(cmtId);
                ctx.setTargetType("POST");
                ctx.setTargetId(((Number) row[1]).longValue());
                ctx.setTargetTitle((String) row[2]);
                ctx.setTargetSlug((String) row[3]);
            } 
            else if (eventMapping.containsKey(cmtId) == true) {
                Object[] row = eventMapping.get(cmtId);
                ctx.setTargetType("EVENT");
                ctx.setTargetId(((Number) row[1]).longValue());
                ctx.setTargetTitle((String) row[2]);
                ctx.setTargetSlug((String) row[3]);
            }

            responseList.add(ctx);
        }

        return new PageImpl<>(responseList, pageable, cmtPage.getTotalElements());
    }

    @Override
    public List<LoaiLikeResponse> layTatCaLoaiLike() {
        List<LoaiLike> danhSach = loaiLikeRepository.findAllByOrderByIdAsc();
        List<LoaiLikeResponse> result = new ArrayList<>();
        
        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            LoaiLike ll = (LoaiLike) arr[i];
            LoaiLikeResponse resp = new LoaiLikeResponse();
            resp.setId(ll.getId());
            resp.setCode(ll.getCode());
            resp.setName(ll.getName());
            resp.setIconUrl(ll.getIconUrl());
            result.add(resp);
        }
        return result;
    }

    @Override
    public CommentStatsResponse layThongKeBinhLuan() {
        long totalCmt = cmtRepository.demTongCmt();
        long totalPhCmt = phCmtRepository.demTongPhCmt();

        long pendingCmt = cmtRepository.demCmtChuaDuyet();
        long hiddenCmt = cmtModerationLogRepository.demCmtDangAnHien();
        long hiddenPhCmt = phCmtModerationLogRepository.demPhCmtDangAnHien();
        long totalReactions = ctLikeCmtRepository.demTongReaction();

        // G5: Đếm số bình luận đang bị báo cáo chờ xử lý
        long reportedCmt = ctCmtReportRepository.demCmtCoBaoCaoChoXuLy();

        CommentStatsResponse stats = new CommentStatsResponse();
        stats.setTotalCmt(totalCmt);
        stats.setTotalPhCmt(totalPhCmt);
        stats.setPendingCmt(pendingCmt);
        stats.setHiddenCmt(hiddenCmt);
        stats.setHiddenPhCmt(hiddenPhCmt);
        stats.setTotalReactions(totalReactions);
        stats.setReportedCmt(reportedCmt);

        return stats;
    }

    @Override
    public List<CmtResponse> layCmtChuaDuyet() {
        List<Cmt> danhSach = cmtRepository.layCmtChuaDuyet();
        List<CmtResponse> result = new ArrayList<>();
        
        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            result.add(xayDungCmtResponse((Cmt) arr[i], null));
        }
        return result;
    }

    @Override
    @Transactional
    public void kiemDuyetBinhLuan(CommentModerationRequest request, Long moderatorId) {
        User moderator = layUserHoacLoi(moderatorId);

        Optional<ModerationAction> optAction = moderationActionRepository.findById(request.getActionId());
        if (optAction.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy mã lệnh kiểm duyệt.");
        }

        Long viPhamUserId = null;

        if ("CMT".equals(request.getTargetType()) == true) {
            Optional<Cmt> optCmt = cmtRepository.findById(request.getTargetId());
            if (optCmt.isPresent() == false) {
                throw new AppException(404, "Không tìm thấy bình luận.");
            }
            
            viPhamUserId = optCmt.get().getUser().getId();
            
            CtCmtModerationLog log = new CtCmtModerationLog();
            log.setCmt(optCmt.get());
            log.setAction(optAction.get());
            log.setModerator(moderator);
            log.setReason(request.getReason());
            log.setCreatedAt(LocalDateTime.now());
            
            cmtModerationLogRepository.save(log);
            
        } else if ("PH_CMT".equals(request.getTargetType()) == true) {
            Optional<PhCmt> optPhCmt = phCmtRepository.findById(request.getTargetId());
            if (optPhCmt.isPresent() == false) {
                throw new AppException(404, "Không tìm thấy phản hồi.");
            }
            
            viPhamUserId = optPhCmt.get().getUser().getId();
            
            CtPhCmtModerationLog log = new CtPhCmtModerationLog();
            log.setPhCmt(optPhCmt.get());
            log.setAction(optAction.get());
            log.setModerator(moderator);
            log.setReason(request.getReason());
            log.setCreatedAt(LocalDateTime.now());
            
            phCmtModerationLogRepository.save(log);
            
        } else {
            throw new AppException(400, "Phân loại thực thể mục tiêu không hợp lệ: " + request.getTargetType());
        }

        if (request.isLockUser() == true && viPhamUserId != null) {
            userService.lockUnlockUser(viPhamUserId, true, moderator);
            
            String lockReason = "Khóa tài khoản do vi phạm nội dung. Lý do kiểm duyệt: " + request.getReason();
            if (request.getLockDurationDays() != null && request.getLockDurationDays() > 0) {
                lockReason = lockReason + " (Thời hạn phạt: " + request.getLockDurationDays() + " ngày)";
            } else {
                lockReason = lockReason + " (Hình phạt: Khóa vĩnh viễn)";
            }
            
            auditService.logAction(viPhamUserId, "LOCK_USER", null, moderator.getId(), lockReason);
        }
    }

    @Override
    @Transactional
    public void kiemDuyetNhieu(BulkActionRequest request, Long moderatorId) {
        User moderator = layUserHoacLoi(moderatorId);
        Optional<ModerationAction> optAction = moderationActionRepository.findByCode(request.getAction());
        
        if (optAction.isPresent() == false) {
            throw new AppException(400, "Mã hành vi kiểm duyệt không tồn tại: " + request.getAction());
        }
        
        ModerationAction action = optAction.get();
        Object[] ids = request.getIds().toArray();
        
        for (int i = 0; i < ids.length; i = i + 1) {
            Long cmtId = (Long) ids[i];
            Optional<Cmt> optCmt = cmtRepository.findById(cmtId);
            
            if (optCmt.isPresent() == true) {
                CtCmtModerationLog log = new CtCmtModerationLog();
                log.setCmt(optCmt.get());
                log.setAction(action);
                log.setModerator(moderator);
                log.setCreatedAt(LocalDateTime.now());
                cmtModerationLogRepository.save(log);
            }
        }
    }

    @Override
    @Transactional
    public void xoaCmtVatLy(Long cmtId) {
        if (cmtRepository.existsById(cmtId) == false) {
            throw new AppException(404, "Không tìm thấy bình luận để xóa.");
        }
        ctPostCmtRepository.xoaLienKetTheoCmt(cmtId);
        ctEventCmtRepository.xoaLienKetTheoCmt(cmtId);
        
        if (actionLogRepository != null) {
            actionLogRepository.xoaNhatKyTheoCmtId(cmtId);
        }
        cmtRepository.deleteById(cmtId);
    }

    @Override
    @Transactional
    public void xoaPhCmtVatLy(Long phCmtId) {
        if (phCmtRepository.existsById(phCmtId) == false) {
            throw new AppException(404, "Không tìm thấy phản hồi để xóa.");
        }
        if (phActionLogRepository != null) {
            phActionLogRepository.xoaNhatKyTheoPhCmtId(phCmtId);
        }
        phCmtRepository.deleteById(phCmtId);
    }

    @Override
    @Transactional
    public void xoaNhieuCmt(BulkActionRequest request) {
        Object[] ids = request.getIds().toArray();
        for (int i = 0; i < ids.length; i = i + 1) {
            Long cmtId = (Long) ids[i];
            if (cmtRepository.existsById(cmtId) == true) {
                ctPostCmtRepository.xoaLienKetTheoCmt(cmtId);
                ctEventCmtRepository.xoaLienKetTheoCmt(cmtId);
                if (actionLogRepository != null) {
                    actionLogRepository.xoaNhatKyTheoCmtId(cmtId);
                }
                cmtRepository.deleteById(cmtId);
            }
        }
    }

    // =========================================================================
    // G2: CRUD LOẠI PHẢN ỨNG (ADMIN)
    // =========================================================================

    @Override
    @Transactional
    public LoaiLikeResponse taoLoaiLike(LoaiLikeRequest request) {
        if (loaiLikeRepository.existsByCode(request.getCode()) == true) {
            throw new AppException(409, "Mã loại phản ứng đã tồn tại: " + request.getCode());
        }

        LoaiLike loaiLike = new LoaiLike();
        loaiLike.setCode(request.getCode().trim().toUpperCase());
        loaiLike.setName(request.getName().trim());
        loaiLike.setIconUrl(request.getIconUrl());

        LoaiLike saved = loaiLikeRepository.save(loaiLike);

        LoaiLikeResponse resp = new LoaiLikeResponse();
        resp.setId(saved.getId());
        resp.setCode(saved.getCode());
        resp.setName(saved.getName());
        resp.setIconUrl(saved.getIconUrl());
        return resp;
    }

    @Override
    @Transactional
    public LoaiLikeResponse capNhatLoaiLike(Integer id, LoaiLikeRequest request) {
        Optional<LoaiLike> opt = loaiLikeRepository.findById(id);
        if (opt.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy loại phản ứng với ID: " + id);
        }

        LoaiLike loaiLike = opt.get();

        String newCode = request.getCode().trim().toUpperCase();
        if (newCode.equals(loaiLike.getCode()) == false) {
            if (loaiLikeRepository.existsByCode(newCode) == true) {
                throw new AppException(409, "Mã loại phản ứng đã tồn tại: " + newCode);
            }
            loaiLike.setCode(newCode);
        }

        loaiLike.setName(request.getName().trim());
        loaiLike.setIconUrl(request.getIconUrl());

        LoaiLike saved = loaiLikeRepository.save(loaiLike);

        LoaiLikeResponse resp = new LoaiLikeResponse();
        resp.setId(saved.getId());
        resp.setCode(saved.getCode());
        resp.setName(saved.getName());
        resp.setIconUrl(saved.getIconUrl());
        return resp;
    }

    @Override
    @Transactional
    public void xoaLoaiLike(Integer id) {
        if (loaiLikeRepository.existsById(id) == false) {
            throw new AppException(404, "Không tìm thấy loại phản ứng với ID: " + id);
        }
        loaiLikeRepository.deleteById(id);
    }

    // =========================================================================
    // HÀM TIỆN ÍCH LÕI VÀ ĐÓNG GÓI DỮ LIỆU BẢO MẬT (HELPER METHODS)
    // =========================================================================

    /**
     * Khai thác định danh đối tượng người dùng từ Cơ sở dữ liệu.
     * Thiết lập rào chắn bảo mật, từ chối cấp phát tài nguyên nếu định danh không hợp lệ.
     */
    private User layUserHoacLoi(Long userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent() == false) {
            throw new AppException(401, "Danh tính không xác định. Vui lòng định danh lại hệ thống.");
        }
        return optUser.get();
    }

    /**
     * Bóc tách địa chỉ IP và định dạng Thiết bị xuyên qua các tầng Proxy nội bộ.
     * Cung cấp Dấu vết Mạng chuẩn xác để lưu vào Sổ tay Kiểm toán.
     */
    private String[] trichXuatThongTinMang() {
        String ipAddress = "Unknown";
        String userAgent = "Unknown";

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && forwardedFor.trim().isEmpty() == false) {
                ipAddress = forwardedFor.split(",")[0].trim();
            } else {
                ipAddress = request.getRemoteAddr();
            }
            String uaHeader = request.getHeader("User-Agent");
            if (uaHeader != null) {
                if (uaHeader.length() > 490) {
                    userAgent = uaHeader.substring(0, 490);
                } else {
                    userAgent = uaHeader;
                }
            }
        }
        return new String[]{ipAddress, userAgent};
    }

    /**
     * Gói Dữ liệu thô của Bình luận thành Chuỗi JSON phục vụ việc Đối soát kiểm toán.
     */
    private String taoJsonPayloadCmt(Cmt cmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (cmt.getContent() != null) {
            sb.append("\"content\":\"").append(cmt.getContent().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
        } else {
            sb.append("\"content\":\"\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Gói Dữ liệu thô của Phản hồi thứ cấp thành Chuỗi JSON.
     */
    private String taoJsonPayloadPhCmt(PhCmt phCmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (phCmt.getContent() != null) {
            sb.append("\"content\":\"").append(phCmt.getContent().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
        } else {
            sb.append("\"content\":\"\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Đóng ấn bản ghi vào Sổ tay Kiểm toán Tự thân đối với Bình luận gốc.
     */
    private void ghiNhanNhatKyTuThanCmt(Cmt cmt, User user, String actionCode, String oldPayload, String newPayload) {
        Optional<ModerationAction> optAction = moderationActionRepository.findByCode(actionCode);
        if (optAction.isPresent() == false) return;

        String[] netInfo = trichXuatThongTinMang();
        
        CtCmtActionLog log = new CtCmtActionLog();
        log.setCmtId(cmt.getId());
        log.setUserId(user.getId());
        log.setActionId(optAction.get().getId());
        log.setOldPayload(oldPayload);
        log.setNewPayload(newPayload);
        log.setIpAddress(netInfo[0]);
        log.setUserAgent(netInfo[1]);
        
        actionLogRepository.save(log);
    }

    /**
     * Đóng ấn bản ghi vào Sổ tay Kiểm toán Tự thân đối với Phản hồi.
     */
    private void ghiNhanNhatKyTuThanPhCmt(PhCmt phCmt, User user, String actionCode, String oldPayload, String newPayload) {
        Optional<ModerationAction> optAction = moderationActionRepository.findByCode(actionCode);
        if (optAction.isPresent() == false) return;

        String[] netInfo = trichXuatThongTinMang();
        
        CtPhCmtActionLog log = new CtPhCmtActionLog();
        log.setPhCmtId(phCmt.getId());
        log.setUserId(user.getId());
        log.setActionId(optAction.get().getId());
        log.setOldPayload(oldPayload);
        log.setNewPayload(newPayload);
        log.setIpAddress(netInfo[0]);
        log.setUserAgent(netInfo[1]);
        
        phActionLogRepository.save(log);
    }

    // =========================================================================
    // CÔNG CỤ XÂY DỰNG DTO ĐỆ QUY KHÔNG LAMBDA
    // =========================================================================

    private CmtResponse xayDungCmtResponse(Cmt cmt, Long userId) {
        CmtResponse resp = new CmtResponse();
        resp.setId(cmt.getId());
        resp.setContent(cmt.getContent());
        resp.setCreatedAt(cmt.getCreatedAt());
        resp.setUpdatedAt(cmt.getUpdatedAt());

        if (cmt.getUser() != null) {
            resp.setUserId(cmt.getUser().getId());
            
            if (cmt.getUser().getFullName() != null && cmt.getUser().getFullName().trim().isEmpty() == false) {
                resp.setUserFullName(cmt.getUser().getFullName());
            } else {
                resp.setUserFullName(cmt.getUser().getUsername());
            }
            
            List<CtUserRole> userRoles = ctUserRoleRepository.findByUserId(cmt.getUser().getId());
            if (userRoles.isEmpty() == false) {
                Object[] rolesArr = userRoles.toArray();
                CtUserRole firstRole = (CtUserRole) rolesArr[0];
                Optional<UserRole> optRole = userRoleRepository.findById(firstRole.getRoleId());
                if (optRole.isPresent() == true) {
                    resp.setUserRoleName(optRole.get().getRoleName());
                }
            }
        }

        Optional<CtCmtModerationLog> optLog = cmtModerationLogRepository.layHanhDongMoiNhat(cmt.getId());
        if (optLog.isPresent() == true) {
            resp.setCurrentStatus(optLog.get().getAction().getCode());
        }

        resp.setReactions(layReactionsCmt(cmt.getId()));

        if (userId != null) {
            Optional<CtLikeCmt> optLike = ctLikeCmtRepository.findById_UserIdAndId_CmtId(userId, cmt.getId());
            if (optLike.isPresent() == true) {
                resp.setCurrentUserReaction(optLike.get().getLoaiLike().getCode());
            }
        }

        List<PhCmt> replies = phCmtRepository.findByRootCmtIdOrderByCreatedAtAsc(cmt.getId());
        List<PhCmtResponse> replyResponses = new ArrayList<>();
        
        Object[] repliesArr = replies.toArray();
        for (int i = 0; i < repliesArr.length; i = i + 1) {
            replyResponses.add(xayDungPhCmtResponse((PhCmt) repliesArr[i], userId));
        }
        resp.setReplies(replyResponses);
        
        return resp;
    }

    private PhCmtResponse xayDungPhCmtResponse(PhCmt ph, Long userId) {
        PhCmtResponse resp = new PhCmtResponse();
        resp.setId(ph.getId());
        resp.setRootCmtId(ph.getRootCmt().getId());
        resp.setContent(ph.getContent());
        resp.setCreatedAt(ph.getCreatedAt());
        resp.setUpdatedAt(ph.getUpdatedAt());

        if (ph.getParentPh() != null) {
            resp.setParentPhId(ph.getParentPh().getId());
        }

        if (ph.getUser() != null) {
            resp.setUserId(ph.getUser().getId());
            if (ph.getUser().getFullName() != null && ph.getUser().getFullName().trim().isEmpty() == false) {
                resp.setUserFullName(ph.getUser().getFullName());
            } else {
                resp.setUserFullName(ph.getUser().getUsername());
            }
        }

        Optional<CtPhCmtModerationLog> optLog = phCmtModerationLogRepository.layHanhDongMoiNhat(ph.getId());
        if (optLog.isPresent() == true) {
            resp.setCurrentStatus(optLog.get().getAction().getCode());
        }

        resp.setReactions(layReactionsPhCmt(ph.getId()));

        if (userId != null) {
            Optional<CtLikePhCmt> optLike = ctLikePhCmtRepository.findById_UserIdAndId_PhCmtId(userId, ph.getId());
            if (optLike.isPresent() == true) {
                resp.setCurrentUserReaction(optLike.get().getLoaiLike().getCode());
            }
        }
        return resp;
    }

    private List<LoaiLikeResponse> layReactionsCmt(Long cmtId) {
        List<Object[]> rows = ctLikeCmtRepository.demReactionTheLoai(cmtId);
        List<LoaiLikeResponse> result = new ArrayList<>();
        
        Object[] arr = rows.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Object[] row = (Object[]) arr[i];
            LoaiLikeResponse resp = new LoaiLikeResponse();
            resp.setCode((String) row[0]);
            resp.setCount(((Number) row[1]).longValue());
            result.add(resp);
        }
        return result;
    }

    private List<LoaiLikeResponse> layReactionsPhCmt(Long phCmtId) {
        List<Object[]> rows = ctLikePhCmtRepository.demReactionTheLoai(phCmtId);
        List<LoaiLikeResponse> result = new ArrayList<>();
        
        Object[] arr = rows.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Object[] row = (Object[]) arr[i];
            LoaiLikeResponse resp = new LoaiLikeResponse();
            resp.setCode((String) row[0]);
            resp.setCount(((Number) row[1]).longValue());
            result.add(resp);
        }
        return result;
    }
}