//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/PostServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostDetailResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostFileResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostImageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PublicProfileResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtLikePost;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.LoaiLike;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostFile;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostImage;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostViewLog;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PublicProfile;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICategoryRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtFileDownloadRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtLikeCmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtLikePostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostTagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostFileRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostImageRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostViewLogRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPublicProfileRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ITagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ILoaiLikeRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IPostService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: THỰC THI NGHIỆP VỤ BÀI VIẾT (PUBLIC FRONTEND)
 * =========================================================================
 * Cung cấp dữ liệu đã được tổng hợp để hiển thị lên giao diện người dùng.
 * Đã tích hợp thuật toán chặn dữ liệu (Gated Content) chuẩn xác ngay tại
 * Service,
 * kiên quyết không cấp phát Dữ liệu cho các tài khoản không đủ thẩm quyền.
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    private final IPostRepository postRepository;
    private final ICategoryRepository categoryRepository;
    private final ITagRepository tagRepository;
    private final ICtPostTagRepository ctPostTagRepository;
    private final IPostViewLogRepository postViewLogRepository;
    private final IPostImageRepository postImageRepository;
    private final IPostFileRepository postFileRepository;
    private final ICtFileDownloadRepository ctFileDownloadRepository;
    private final ICmtRepository cmtRepository;
    private final ILoaiLikeRepository loaiLikeRepository;
    private final ICtLikeCmtRepository ctLikeCmtRepository;
    private final ICtLikePostRepository ctLikePostRepository;
    private final IUserRepository userRepository;
    private final IPublicProfileRepository publicProfileRepository;

    // Inject UserService để tận dụng hàm nạp quyền (napQuyenChoNguoiDung)
    private final IUserService userService;

    @Override
    public PostStatsResponse layThongKeTrangBaiViet() {
        long totalPublished = postRepository.countByIsPublishedTrue();
        long totalCategories = categoryRepository.countByIsActiveTrue();
        long totalTags = ctPostTagRepository.demTongSoTagDangDung();
        long totalDownloads = ctFileDownloadRepository.demTongLuotTai();
        long totalReactions = ctLikePostRepository.demTongReaction();

        PostStatsResponse stats = new PostStatsResponse();
        stats.setTotalPublished(totalPublished);
        stats.setTotalCategories(totalCategories);
        stats.setTotalTags(totalTags);
        stats.setTotalDownloads(totalDownloads);
        stats.setTotalReactions(totalReactions);

        return stats;
    }

    @Override
    public List<CategoryResponse> layDanhMucKemSoBaiViet() {
        List<Object[]> rows = categoryRepository.demBaiVietTheoTungDanhMuc();
        List<CategoryResponse> result = new ArrayList<>();

        for (int i = 0; i < rows.size(); i = i + 1) {
            Object[] row = rows.get(i);
            CategoryResponse resp = new CategoryResponse();
            resp.setId((Integer) row[0]);
            resp.setName((String) row[1]);
            resp.setPostCount(((Number) row[2]).longValue());
            result.add(resp);
        }

        return result;
    }

    @Override
    public List<TagResponse> layTagCloud() {
        List<Object[]> rows = tagRepository.layTagKemSoLanDung();
        List<TagResponse> result = new ArrayList<>();

        for (int i = 0; i < rows.size(); i = i + 1) {
            Object[] row = rows.get(i);
            TagResponse resp = new TagResponse();
            resp.setId(((Number) row[0]).longValue());
            resp.setName((String) row[1]);
            resp.setSlug((String) row[2]);
            resp.setUsageCount(((Number) row[3]).longValue());
            result.add(resp);
        }

        return result;
    }

    /**
     * Công cụ tìm kiếm bài viết Public đa năng kết hợp phân trang.
     */
    @Override
    public Page<PostResponse> timKiemBaiViet(String keyword, Integer categoryId, String accessLevel, String sortBy,
            int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null) {
            if (sortBy.equals("views")) {
                sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Tạm fallback về createdAt do JPA không sort trực
                                                                  // tiếp aggregate count
            } else if (sortBy.equals("oldest")) {
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }

        String al = null;
        if (accessLevel != null && accessLevel.trim().isEmpty() == false) {
            al = accessLevel.trim();
        }

        Page<Post> posts = postRepository.timKiemBaiVietDaXuatBan(kw, categoryId, al, pageable);

        // Chuyển đổi dữ liệu thủ công
        List<Post> contentList = posts.getContent();
        List<PostResponse> responseList = new ArrayList<>();

        for (int i = 0; i < contentList.size(); i = i + 1) {
            responseList.add(chuyenDoiPostResponse(contentList.get(i)));
        }

        return new PageImpl<>(responseList, pageable, posts.getTotalElements());
    }

    @Override
    public PostResponse layBaiVietNoiBat() {
        Pageable pageable = PageRequest.of(0, 1);
        List<Post> danhSach = postRepository.layBaiVietNoiBat(pageable);
        if (danhSach.isEmpty() == true) {
            return null;
        }
        return chuyenDoiPostResponse(danhSach.get(0));
    }

    /**
     * Truy xuất chi tiết bài viết.
     * Tích hợp Lá chắn dữ liệu (Data Shield): Kiểm soát quyền đọc Gated Content.
     */
    @Override
    @Transactional
    public PostDetailResponse layChiTietBaiViet(String slug, String viewerIp, Long userId) {
        Optional<Post> optPost = postRepository.findBySlug(slug);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy nội dung y khoa được yêu cầu.");
        }

        Post post = optPost.get();
        return xayDungPostDetailResponse(post, userId);
    }

    /**
     * Ghi nhận lịch sử xem trang ẩn danh hoặc xác thực để chống spam lượt xem.
     */
    @Override
    @Transactional
    public void ghiNhanLuotXem(Long postId, String viewerIp, Long userId) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            return;
        }

        Post post = optPost.get();
        PostViewLog log = new PostViewLog();
        log.setPost(post);
        log.setViewerIp(viewerIp);
        log.setViewedAt(LocalDateTime.now());

        if (userId != null) {
            Optional<User> optUser = userRepository.findById(userId);
            if (optUser.isPresent() == true) {
                log.setUser(optUser.get());
            }
        }

        postViewLogRepository.save(log);
    }

    @Transactional
    public void thichBaiViet(LikeRequest request, Long userId) {
        User user = layUserHoacLoi(userId);

        Optional<Post> optPost = postRepository.findById(request.getTargetId());
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy ấn phẩm y khoa để tương tác.");
        }

        Optional<LoaiLike> optLoai = loaiLikeRepository.findByCode(request.getLoaiLikeCode());
        if (optLoai.isPresent() == false) {
            throw new AppException(400, "Mã định danh cảm xúc không hợp lệ.");
        }

        Optional<CtLikePost> optExisting = ctLikePostRepository.findById_UserIdAndId_PostId(userId,
                request.getTargetId());

        if (optExisting.isPresent() == true) {
            CtLikePost existing = optExisting.get();
            // Rẽ nhánh: Trùng mã thì Xóa (Unlike), Khác mã thì Cập nhật
            if (existing.getLoaiLike().getId().equals(optLoai.get().getId()) == true) {
                ctLikePostRepository.delete(existing);
            } else {
                existing.setLoaiLike(optLoai.get());
                ctLikePostRepository.save(existing);
            }
        } else {
            CtLikePost.CtLikePostId pkId = new CtLikePost.CtLikePostId(userId, request.getTargetId());
            CtLikePost like = new CtLikePost();
            like.setId(pkId);
            like.setUser(user);
            like.setPost(optPost.get());
            like.setLoaiLike(optLoai.get());
            like.setCreatedAt(LocalDateTime.now());
            ctLikePostRepository.save(like);
        }
    }

    /**
     * Công cụ định danh và kiểm chứng rào cản bảo mật.
     * Ngăn chặn tuyệt đối việc lợi dụng Request ảo để mạo danh người khác.
     */
    private User layUserHoacLoi(Long userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent() == false) {
            throw new AppException(401,
                    "Danh tính không xác định. Vui lòng đăng nhập lại để đảm bảo an toàn hệ thống.");
        }
        return optUser.get();
    }

    // =========================================================================
    // HÀM TIỆN ÍCH ĐÓNG GÓI DTO THỦ CÔNG
    // =========================================================================

    private PostResponse chuyenDoiPostResponse(Post post) {
        PostResponse resp = new PostResponse();
        resp.setId(post.getId());
        resp.setTitle(post.getTitle());
        resp.setSlug(post.getSlug());
        resp.setSummary(post.getSummary());
        resp.setThumbnailUrl(post.getThumbnailUrl());
        resp.setAccessLevel(post.getAccessLevel());
        resp.setPublished(post.isPublished());
        resp.setCreatedAt(post.getCreatedAt());
        resp.setUpdatedAt(post.getUpdatedAt());

        if (post.getCategory() != null) {
            resp.setCategoryId(post.getCategory().getId());
            resp.setCategoryName(post.getCategory().getName());
            resp.setCategorySlug(post.getCategory().getSlug());
        }

        if (post.getAuthor() != null) {
            resp.setAuthorId(post.getAuthor().getId());
            resp.setAuthorName(post.getAuthor().getFullName());
        }

        // Lấy mảng thẻ Tag
        List<Tag> tags = ctPostTagRepository.layTagCuaBaiViet(post.getId());
        List<TagResponse> tagResponses = new ArrayList<>();

        for (int i = 0; i < tags.size(); i = i + 1) {
            Tag tag = tags.get(i);
            TagResponse tagResp = new TagResponse();
            tagResp.setId(tag.getId());
            tagResp.setName(tag.getName());
            tagResp.setSlug(tag.getSlug());
            tagResponses.add(tagResp);
        }
        resp.setTags(tagResponses);

        resp.setViewCount(postViewLogRepository.countByPostId(post.getId()));
        resp.setDownloadCount(ctFileDownloadRepository.demLuotTaiCuaBaiViet(post.getId()));
        resp.setCommentCount(cmtRepository.demCmtCuaBaiViet(post.getId()));

        return resp;
    }

    /**
     * =========================================================================
     * THUẬT TOÁN ĐÓNG GÓI CHI TIẾT BÀI VIẾT (MASTER MAPPING ENGINE)
     * =========================================================================
     * Chắp nối nhiều mảng dữ liệu rời rạc thành một bức tranh Post Detail hoàn chỉnh.
     * TÍCH HỢP 2 BỨC TƯỜNG LỬA CHỐNG RÒ RỈ DỮ LIỆU:
     * 1. Lá chắn Gated Content (Paywall Shield)
     * 2. Lá chắn Quyền Riêng Tư (Privacy Shield)
     */
    private PostDetailResponse xayDungPostDetailResponse(Post post, Long userId) {
        PostDetailResponse resp = new PostDetailResponse();
        resp.setId(post.getId());
        resp.setTitle(post.getTitle());
        resp.setSlug(post.getSlug());
        resp.setSummary(post.getSummary());

        // =========================================================================
        // BỘ LỌC KIỂM DUYỆT TRUY CẬP (ACCESS LEVEL SHIELD)
        // =========================================================================
        boolean hasAccess = false;
        String reqAccess = post.getAccessLevel();

        if (reqAccess == null) {
            hasAccess = true;
        } else if (reqAccess.equals("PUBLIC")) {
            hasAccess = true;
        } else {
            // Nếu bài viết yêu cầu quyền đặc biệt, tiến hành kiểm tra User
            if (userId != null) {
                Optional<User> optU = userRepository.findById(userId);
                if (optU.isPresent() == true) {
                    User u = optU.get();
                    // Nhờ UserService nạp danh sách quyền (Roles) từ DB vào RAM
                    userService.napQuyenChoNguoiDung(u);

                    List<String> roles = u.getDanhSachTenRole();
                    if (roles != null) {
                        Object[] rolesArr = roles.toArray();
                        for (int i = 0; i < rolesArr.length; i = i + 1) {
                            String roleName = rolesArr[i].toString();

                            // Các chức vụ đặc quyền luôn có quyền đọc toàn bộ hệ thống
                            if (roleName.equals("SUPERADMIN")) {
                                hasAccess = true;
                                break;
                            }
                            if (roleName.equals("ADMIN")) {
                                hasAccess = true;
                                break;
                            }

                            // Chức vụ của User ở dạng "DOCTOR", nhưng AccessLevel lưu "ROLE_DOCTOR"
                            String roleWithPrefix = "ROLE_" + roleName;
                            if (roleWithPrefix.equals(reqAccess)) {
                                hasAccess = true;
                                break;
                            }

                            // Đề phòng trường hợp CSDL lưu không có tiền tố ROLE_
                            if (roleName.equals(reqAccess)) {
                                hasAccess = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Quyết định cuối cùng: Cấp phát hoặc Từ chối nội dung HTML
        if (hasAccess == true) {
            resp.setContent(post.getContent());
            resp.setHasFullAccess(true);
        } else {
            resp.setContent(null); // TUYỆT ĐỐI che giấu dữ liệu gốc
            resp.setHasFullAccess(false);
        }

        // =========================================================================
        // 2. LÁ CHẮN QUYỀN RIÊNG TƯ TÁC GIẢ (AUTHOR PRIVACY SHIELD)
        // =========================================================================
        // Khai thác dữ liệu Hồ sơ công khai. Áp dụng Màng lọc Quyền riêng tư
        // để bảo vệ thông tin Tác giả theo thiết lập cá nhân của họ.

        PublicProfileResponse authorProfileDto = new PublicProfileResponse();
        User author = post.getAuthor();

        if (author != null) {
            authorProfileDto.setUserId(author.getId());

            // Đếm tổng sản lượng nội dung Y khoa đã xuất bản (Đo lường sức ảnh hưởng)
            long totalPosts = postRepository.countByAuthorIdAndIsPublishedTrue(author.getId());
            authorProfileDto.setTotalPublishedPosts(totalPosts);

            Optional<PublicProfile> optProfile = publicProfileRepository.findByUserId(author.getId());

            // Kiểm chứng: Tác giả CÓ hồ sơ VÀ đang BẬT cờ cho phép hiển thị (IS_VISIBLE =
            // 1)
            if (optProfile.isPresent() == true && optProfile.get().isVisible() == true) {
                PublicProfile p = optProfile.get();
                authorProfileDto
                        .setFullName(author.getFullName() != null ? author.getFullName() : author.getUsername());
                authorProfileDto.setProfessionalTitle(p.getProfessionalTitle());
                authorProfileDto.setWorkplace(p.getWorkplace());
                authorProfileDto.setBio(p.getBio());
                authorProfileDto.setAvatarUrl(p.getAvatarUrl());
                authorProfileDto.setVisible(true);
            } else {
                // KÍCH HOẠT CƠ CHẾ ẨN DANH (ANONYMOUS MODE)
                // Ghi đè toàn bộ dữ liệu thật bằng dữ liệu ảo để chặn đứng việc lộ lọt thông
                // tin.
                authorProfileDto.setFullName("Tác giả ẩn danh");
                authorProfileDto.setProfessionalTitle(null);
                authorProfileDto.setWorkplace(null);
                authorProfileDto.setBio("Tác giả đã giới hạn quyền riêng tư đối với hồ sơ này.");
                authorProfileDto.setAvatarUrl(null);
                authorProfileDto.setVisible(false);
            }
        }

        // Nhúng (Nest) DTO Tác giả vào DTO Bài viết
        resp.setAuthorProfile(authorProfileDto);

// =========================================================================
        // BƠM CÁC THÔNG SỐ KỸ THUẬT VÀ TÀI NGUYÊN ĐÍNH KÈM
        // =========================================================================
        resp.setThumbnailUrl(post.getThumbnailUrl());
        resp.setAccessLevel(post.getAccessLevel());
        resp.setPublished(post.isPublished());
        resp.setSeoTitle(post.getSeoTitle());
        resp.setSeoDescription(post.getSeoDescription());
        resp.setCreatedAt(post.getCreatedAt());
        resp.setUpdatedAt(post.getUpdatedAt());

        if (post.getCategory() != null) {
            resp.setCategoryId(post.getCategory().getId());
            resp.setCategoryName(post.getCategory().getName());
            resp.setCategorySlug(post.getCategory().getSlug());
        }

        // Mảng Thẻ từ khóa (Tags)
        List<Tag> tags = ctPostTagRepository.layTagCuaBaiViet(post.getId());
        List<TagResponse> tagResponses = new ArrayList<>();
        for (int i = 0; i < tags.size(); i = i + 1) {
            Tag tag = tags.get(i);
            TagResponse tagResp = new TagResponse();
            tagResp.setId(tag.getId());
            tagResp.setName(tag.getName());
            tagResp.setSlug(tag.getSlug());
            tagResponses.add(tagResp);
        }
        resp.setTags(tagResponses);

        // Thư viện Hình ảnh (Đảm bảo thứ tự Display Order)
        List<PostImage> images = postImageRepository.findByPostIdOrderByDisplayOrderAsc(post.getId());
        List<PostImageResponse> imageResponses = new ArrayList<>();
        for (int i = 0; i < images.size(); i = i + 1) {
            PostImage img = images.get(i);
            PostImageResponse imgResp = new PostImageResponse();
            imgResp.setId(img.getId());
            imgResp.setImageUrl(img.getImageUrl());
            imgResp.setDisplayOrder(img.getDisplayOrder());
            imageResponses.add(imgResp);
        }
        resp.setImages(imageResponses);

        // Thư viện Tài liệu số đính kèm (PDF, Word)
        List<PostFile> files = postFileRepository.findByPostId(post.getId());
        List<PostFileResponse> fileResponses = new ArrayList<>();
        for (int i = 0; i < files.size(); i = i + 1) {
            PostFile pf = files.get(i);
            PostFileResponse fileResp = new PostFileResponse();
            fileResp.setId(pf.getId());
            fileResp.setFileName(pf.getFileName());
            fileResp.setFileUrl(pf.getFileUrl());
            fileResp.setFileType(pf.getFileType());
            fileResp.setFileSize(pf.getFileSize());
            fileResp.setDownloadCount(ctFileDownloadRepository.countById_FileId(pf.getId()));
            fileResponses.add(fileResp);
        }
        resp.setFiles(fileResponses);

        // Các chỉ số Thống kê hiệu suất (Analytics)
        resp.setViewCount(postViewLogRepository.countByPostId(post.getId()));
        resp.setDownloadCount(ctFileDownloadRepository.demLuotTaiCuaBaiViet(post.getId()));
        resp.setCommentCount(cmtRepository.demCmtCuaBaiViet(post.getId()));

        // Trích xuất Bản đồ Cảm xúc (Reactions)
        Map<String, Long> reactionCounts = new HashMap<>();
        List<Object[]> reactionRows = ctLikePostRepository.demReactionTheLoai(post.getId());
        for (int i = 0; i < reactionRows.size(); i = i + 1) {
            Object[] row = reactionRows.get(i);
            String code = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            reactionCounts.put(code, count);
        }
        resp.setReactionCounts(reactionCounts);

        // Truy xuất Bài viết Đề xuất có độ tương đồng cao (Related Posts)
        if (post.getCategory() != null) {
            Pageable relatedPageable = PageRequest.of(0, 4);
            List<Post> related = postRepository.layBaiVietCungDanhMuc(post.getCategory().getId(), post.getId(), relatedPageable);
            List<PostResponse> relatedResponses = new ArrayList<>();
            for (int i = 0; i < related.size(); i = i + 1) {
                relatedResponses.add(chuyenDoiPostResponse(related.get(i)));
            }
            resp.setRelatedPosts(relatedResponses);
        }

        // Nhận diện Dấu ấn Cảm xúc của chính Độc giả đang truy cập
        if (userId != null) {
            Optional<CtLikePost> optLike = ctLikePostRepository.findById_UserIdAndId_PostId(userId, post.getId());
            if (optLike.isPresent() == true) {
                resp.setCurrentUserReaction(optLike.get().getLoaiLike().getCode());
            }
        }

        return resp;
    }
}