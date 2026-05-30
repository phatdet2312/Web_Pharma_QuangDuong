//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AdminPostServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CategoryRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PostRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.TagRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminPostDictionaryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminPostMediaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostCommentPreviewResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostFileResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostImageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostLinkedEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostReactionSummary;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleOptionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Category;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Cmt;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostRole.CtPostRoleId;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostTag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostFile;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostImage;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICategoryRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtFileDownloadRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtLikePostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostTagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostFileRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostImageRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostViewLogRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostCmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ITagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: THỰC THI NGHIỆP VỤ QUẢN TRỊ BÀI VIẾT (ADMIN)
 * =========================================================================
 * Kiểm soát nội dung Y khoa, quản lý Phân quyền đọc (Gated Content) và Tag.
 * Tích hợp chặt chẽ việc dọn dẹp các bảng phụ (Images, Files, Tags) khi xóa bài.
 * Đảm bảo kiến trúc sạch bóng Lambda, tuân thủ ghi chú tại từng khối logic.
 */
@Service
@RequiredArgsConstructor
public class AdminPostServiceImpl implements IAdminPostService {

    private final IPostRepository postRepository;
    private final ICategoryRepository categoryRepository;
    private final ITagRepository tagRepository;
    private final ICtPostTagRepository ctPostTagRepository;
    private final IPostViewLogRepository postViewLogRepository;
    private final IPostImageRepository postImageRepository;
    private final IPostFileRepository postFileRepository;
    private final ICtFileDownloadRepository ctFileDownloadRepository;
    private final ICmtRepository cmtRepository;
    private final ICtLikePostRepository ctLikePostRepository;
    private final IUserRepository userRepository;
    private final ICtPostRoleRepository ctPostRoleRepository;
    private final IUserRoleRepository userRoleRepository;
    private final ICtPostCmtRepository ctPostCmtRepository;
    private final ICtPostEventRepository ctPostEventRepository;
    private final ICtEventRepository ctEventRepository;

    @Value("${pharma.upload.base-path:./uploads}")
    private String uploadBasePath;

    /**
     * Báo cáo toàn cảnh sức khỏe kho tàng Content Marketing.
     * Tối ưu I/O: Lược bỏ 3 lần gọi CSDL rời rạc, gộp thành 1 lệnh IN (...) cho nhóm Đặc quyền.
     */
    @Override
    public PostStatsResponse layThongKeAdmin() {
        long totalAll = postRepository.count();
        long totalPublished = postRepository.countByIsPublishedTrue();
        long totalDraft = totalAll - totalPublished;
        
        long totalGated = postRepository.demBaiVietGated(); 
                
        long totalCategories = categoryRepository.countByIsActiveTrue();
        long totalTags = ctPostTagRepository.demTongSoTagDangDung();
        long totalDownloads = ctFileDownloadRepository.demTongLuotTai();
        long totalReactions = ctLikePostRepository.demTongReaction();
        long totalViews = postViewLogRepository.demTongLuotXem();
        long totalComments = cmtRepository.demTongCmt();
        long totalFeatured = postRepository.countByIsFeaturedTrue();

        PostStatsResponse stats = new PostStatsResponse();
        stats.setTotalAll(totalAll);
        stats.setTotalPublished(totalPublished);
        stats.setTotalDraft(totalDraft);
        stats.setTotalGated(totalGated);
        stats.setTotalCategories(totalCategories);
        stats.setTotalTags(totalTags);
        stats.setTotalDownloads(totalDownloads);
        stats.setTotalReactions(totalReactions);
        stats.setTotalViews(totalViews);
        stats.setTotalComments(totalComments);
        stats.setTotalFeatured(totalFeatured);
        return stats;
    }

    /**
     * Bộ máy tìm kiếm đa chiều dành cho Khối quản trị nội dung.
     * Hỗ trợ khai thác tất cả ấn phẩm, bao gồm cả những bản lưu nháp.
     */
    @Override
    public Page<PostResponse> layDanhSachBaiViet(String keyword, Integer categoryId, Integer roleId, Boolean isPublished, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }
        

        Page<Post> posts = postRepository.timKiemBaiVietAdmin(kw, categoryId, roleId, isPublished, pageable);
        
        List<Post> postListTuDb = posts.getContent();
        List<PostResponse> responseList = chuyenDoiNhieuPostResponse(postListTuDb);

        return new PageImpl<>(responseList, pageable, posts.getTotalElements());
    }

    /**
     * Bộ máy tìm kiếm nâng cao: hỗ trợ date range và dynamic sort.
     */
    @Override
    public Page<PostResponse> layDanhSachBaiViet(String keyword, Integer categoryId, Integer roleId,
                                                  Boolean isPublished, int page, int size,
                                                  String startDate, String endDate, String sort) {
        Sort sortSpec = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null) {
            if ("oldest".equals(sort)) {
                sortSpec = Sort.by(Sort.Direction.ASC, "createdAt");
            } else if ("title".equals(sort)) {
                sortSpec = Sort.by(Sort.Direction.ASC, "title");
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortSpec);

        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }

        LocalDate fromDate = null;
        LocalDate toDate = null;
        if (startDate != null && startDate.isEmpty() == false) {
            fromDate = LocalDate.parse(startDate);
        }
        if (endDate != null && endDate.isEmpty() == false) {
            toDate = LocalDate.parse(endDate);
        }

        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = (toDate != null) ? toDate.plusDays(1).atStartOfDay() : null;

        Page<Post> posts = postRepository.timKiemBaiVietAdminNangCao(kw, categoryId, roleId, isPublished, fromDateTime, toDateTime, pageable);
        List<PostResponse> responseList = chuyenDoiNhieuPostResponse(posts.getContent());
        return new PageImpl<>(responseList, pageable, posts.getTotalElements());
    }

    /**
     * Khởi tạo một ấn phẩm Y khoa mới trên Hệ thống.
     * Liên kết chặt chẽ Danh mục, Tác giả và thiết lập Định danh tĩnh (Slug) phục vụ SEO.
     */
    @Override
    @Transactional
    public PostResponse taoBaiViet(PostRequest request, Long authorId) {
        Optional<User> optUser = userRepository.findById(authorId);
        if (optUser.isPresent() == false) {
            throw new AppException(404, "Lỗi định danh: Không tìm thấy thông tin tác giả trong hệ thống.");
        }

        String slug = taoSlug(request.getTitle(), request.getSlug());

        if (postRepository.existsBySlug(slug) == true) {
            throw new AppException(400, "Đường dẫn (Slug) này đã tồn tại, vui lòng chọn tiêu đề khác: " + slug);
        }

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setSlug(slug);
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setThumbnailUrl(request.getThumbnailUrl());
        
        post.setPublished(request.isPublished());
        post.setSeoTitle(request.getSeoTitle());
        post.setSeoDescription(request.getSeoDescription());
        post.setAuthor(optUser.get());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Optional<Category> optCat = categoryRepository.findById(request.getCategoryId());
            if (optCat.isPresent() == true) {
                post.setCategory(optCat.get());
            }
        }

        Post saved = postRepository.save(post);
        ganQuyenChoBaiViet(saved, request.getRoleIds());
        ganTagChoBaiViet(saved, request.getTagIds());

        return chuyenDoiPostResponse(saved);
    }

    /**
     * Hiệu đính nội dung Bài viết.
     * Cập nhật URL Slug (nếu đổi) và quản lý tiến trình làm sạch Thẻ chủ đề (Tags).
     */
    @Override
    @Transactional
    public PostResponse capNhatBaiViet(Long postId, PostRequest request) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy bài viết y khoa cần hiệu đính.");
        }

        Post post = optPost.get();
        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setThumbnailUrl(request.getThumbnailUrl());
        
        post.setPublished(request.isPublished());
        post.setSeoTitle(request.getSeoTitle());
        post.setSeoDescription(request.getSeoDescription());
        post.setUpdatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Optional<Category> optCat = categoryRepository.findById(request.getCategoryId());
            if (optCat.isPresent() == true) {
                post.setCategory(optCat.get());
            }
        }

        if (request.getSlug() != null && request.getSlug().equals(post.getSlug()) == false) {
            if (postRepository.existsBySlug(request.getSlug()) == true) {
                throw new AppException(400, "Đường dẫn (Slug) cập nhật đã bị trùng với ấn phẩm khác.");
            }
            post.setSlug(request.getSlug());
        }

        Post saved = postRepository.save(post);

        // Quy trình thay máu cấu trúc phân loại: Xóa hoàn toàn bảng liên kết cũ
        ctPostRoleRepository.xoaHetQuyenCuaBaiViet(saved.getId()); // Dọn quyền cũ
        ganQuyenChoBaiViet(saved, request.getRoleIds()); // Gắn quyền mới
        ctPostTagRepository.xoaHetTagCuaBaiViet(saved.getId());
        ganTagChoBaiViet(saved, request.getTagIds());

        return chuyenDoiPostResponse(saved);
    }

    /**
     * Loại bỏ vĩnh viễn một Bài viết.
     * Giải phóng dữ liệu Thác đổ (Cascade) tại các bảng Tài nguyên số để tránh mồ côi Data.
     */
    @Override
    @Transactional
    public void xoaBaiViet(Long postId) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết cần xóa.");
        }

        // Xóa dữ liệu con theo thứ tự FK: bảng phụ thuộc trước, bảng chính sau
        ctFileDownloadRepository.xoaHetLuotTaiCuaBaiViet(postId);
        postViewLogRepository.xoaHetLuotXemCuaBaiViet(postId);
        ctLikePostRepository.xoaHetReactionCuaBaiViet(postId);
        ctPostCmtRepository.xoaHetCmtCuaBaiViet(postId);
        ctPostEventRepository.xoaHetSuKienCuaBaiViet(postId);
        ctPostRoleRepository.xoaHetQuyenCuaBaiViet(postId);
        ctPostTagRepository.xoaHetTagCuaBaiViet(postId);
        postImageRepository.xoaHetAnhCuaBaiViet(postId);
        postFileRepository.xoaHetFileCuaBaiViet(postId);
        postRepository.deleteById(postId);
    }

    /**
     * Chuyển đổi nhanh trạng thái hiển thị Công khai / Ẩn nháp.
     */
    @Override
    @Transactional
    public PostResponse doiTrangThaiXuatBan(Long postId, boolean isPublished) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy ấn phẩm để điều chỉnh trạng thái.");
        }

        Post post = optPost.get();
        post.setPublished(isPublished);
        post.setUpdatedAt(LocalDateTime.now());

        return chuyenDoiPostResponse(postRepository.save(post));
    }

    /**
     * Bật/tắt trạng thái bài viết nổi bật.
     */
    @Override
    @Transactional
    public PostResponse doiTrangThaiFeatured(Long postId, boolean isFeatured) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        Post post = optPost.get();
        post.setFeatured(isFeatured);
        post.setUpdatedAt(LocalDateTime.now());
        return chuyenDoiPostResponse(postRepository.save(post));
    }

    /**
     * Lấy chi tiết đầy đủ bài viết theo ID, bất kể trạng thái xuất bản.
     * Dùng cho admin xem/sửa bài viết (kể cả bản nháp).
     */
    @Override
    public PostResponse layChiTietBaiViet(Long postId) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        Post post = optPost.get();
        PostResponse resp = chuyenDoiPostResponse(post);
        resp.setContent(post.getContent());
        resp.setSeoTitle(post.getSeoTitle());
        resp.setSeoDescription(post.getSeoDescription());
        return resp;
    }

    /**
     * Lưu ảnh thumbnail bài viết do admin upload.
     * Validate định dạng và dung lượng trước khi ghi vào thư mục /uploads/posts/thumbnails/.
     */
    @Override
    public AdminPostMediaResponse uploadAnhBaiViet(MultipartFile file) {
        String phanMoRong = kiemTraFileAnhUploadPost(file);
        String tenFile = UUID.randomUUID().toString() + phanMoRong;
        Path thuMucLuu = Paths.get(uploadBasePath, "posts", "thumbnails").normalize();
        Path duongDanLuu = thuMucLuu.resolve(tenFile).normalize();

        if (duongDanLuu.startsWith(thuMucLuu) == false) {
            throw new AppException(400, "Đường dẫn upload ảnh bài viết không hợp lệ.");
        }
        try {
            Files.createDirectories(thuMucLuu);
            file.transferTo(duongDanLuu);
        } catch (IOException ex) {
            throw new AppException(500, "Không thể lưu file ảnh bài viết trên server.");
        }

        AdminPostMediaResponse resp = new AdminPostMediaResponse();
        resp.setUrl("/uploads/posts/thumbnails/" + tenFile);
        resp.setFileName(tenFile);
        return resp;
    }

    /** Kiểm tra định dạng và dung lượng ảnh trước khi ghi vào server. */
    private String kiemTraFileAnhUploadPost(MultipartFile file) {
        if (file == null || file.isEmpty() == true) {
            throw new AppException(400, "Vui lòng chọn file ảnh cần upload.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new AppException(400, "Tên file không hợp lệ.");
        }
        String lower = originalName.toLowerCase();
        if (lower.endsWith(".jpg") == false && lower.endsWith(".jpeg") == false
                && lower.endsWith(".png") == false && lower.endsWith(".gif") == false
                && lower.endsWith(".webp") == false) {
            throw new AppException(400, "Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp).");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(400, "Dung lượng file vượt quá 5MB.");
        }
        int lastDot = lower.lastIndexOf('.');
        return lower.substring(lastDot);
    }

    /**
     * Trả về danh mục từ điển nhóm quyền cho frontend populate dropdown.
     */
    @Override
    public AdminPostDictionaryResponse layDanhMucTuDien() {
        List<UserRole> allRoles = userRoleRepository.findAll();
        List<RoleOptionResponse> roleOptions = new ArrayList<>();

        for (int i = 0; i < allRoles.size(); i = i + 1) {
            UserRole r = allRoles.get(i);
            RoleOptionResponse opt = new RoleOptionResponse();
            opt.setId(r.getId());
            opt.setRoleName(r.getRoleName());
            opt.setRoleLevel(r.getRoleLevel());
            roleOptions.add(opt);
        }

        AdminPostDictionaryResponse resp = new AdminPostDictionaryResponse();
        resp.setRoles(roleOptions);
        return resp;
    }

    // =========================================================================
    // QUẢN TRỊ HỆ THỐNG PHÂN LOẠI (CATEGORIES VÀ TAGS)
    // =========================================================================

    @Override
    public List<CategoryResponse> layTatCaDanhMuc() {
        List<Category> danhSach = categoryRepository.findAll();
        List<CategoryResponse> result = new ArrayList<>();
        
        for (int i = 0; i < danhSach.size(); i = i + 1) {
            Category cat = danhSach.get(i);
            CategoryResponse resp = new CategoryResponse();
            resp.setId(cat.getId());
            resp.setName(cat.getName());
            resp.setSlug(cat.getSlug());
            resp.setDescription(cat.getDescription());
            resp.setActive(cat.isActive());
            resp.setPostCount(postRepository.countByCategoryId(cat.getId()));
            result.add(resp);
        }
        return result;
    }

    @Override
    @Transactional
    public CategoryResponse taoDanhMuc(CategoryRequest request) {
        String slug = taoSlug(request.getName(), request.getSlug());
        if (categoryRepository.existsBySlug(slug) == true) {
            throw new AppException(400, "Định danh (Slug) của danh mục này đã được sử dụng: " + slug);
        }

        Category cat = new Category();
        cat.setName(request.getName());
        cat.setSlug(slug);
        cat.setDescription(request.getDescription());
        cat.setActive(request.isActive());

        Category saved = categoryRepository.save(cat);
        
        CategoryResponse resp = new CategoryResponse();
        resp.setId(saved.getId());
        resp.setName(saved.getName());
        resp.setSlug(saved.getSlug());
        resp.setDescription(saved.getDescription());
        resp.setActive(saved.isActive());
        
        return resp;
    }

    @Override
    @Transactional
    public CategoryResponse capNhatDanhMuc(Integer categoryId, CategoryRequest request) {
        Optional<Category> optCat = categoryRepository.findById(categoryId);
        if (optCat.isPresent() == false) {
            throw new AppException(404, "Hệ thống không tìm thấy danh mục chuyên môn cần sửa.");
        }

        Category cat = optCat.get();
        cat.setName(request.getName());
        cat.setDescription(request.getDescription());
        cat.setActive(request.isActive());

        if (request.getSlug() != null && request.getSlug().equals(cat.getSlug()) == false) {
            if (categoryRepository.existsBySlug(request.getSlug()) == true) {
                throw new AppException(400, "Đường dẫn (Slug) này đã bị trùng lắp.");
            }
            cat.setSlug(request.getSlug());
        }

        Category saved = categoryRepository.save(cat);
        
        CategoryResponse resp = new CategoryResponse();
        resp.setId(saved.getId());
        resp.setName(saved.getName());
        resp.setSlug(saved.getSlug());
        resp.setDescription(saved.getDescription());
        resp.setActive(saved.isActive());
        
        return resp;
    }

    @Override
    @Transactional
    public void xoaDanhMuc(Integer categoryId) {
        if (categoryRepository.existsById(categoryId) == false) {
            throw new AppException(404, "Không tìm thấy danh mục để thực hiện lệnh xóa.");
        }
        long soLuongBaiViet = postRepository.countByCategoryId(categoryId);
        if (soLuongBaiViet > 0) {
            throw new AppException(409, "Không thể xóa danh mục đang có " + soLuongBaiViet + " bài viết sử dụng.");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<TagResponse> layTatCaTag() {
        List<Tag> danhSach = tagRepository.findAllByOrderByNameAsc();
        List<TagResponse> result = new ArrayList<>();
        
        for (int i = 0; i < danhSach.size(); i = i + 1) {
            Tag tag = danhSach.get(i);
            TagResponse resp = new TagResponse();
            resp.setId(tag.getId());
            resp.setName(tag.getName());
            resp.setSlug(tag.getSlug());
            resp.setUsageCount(ctPostTagRepository.demBaiVietDungTag(tag.getId()));
            result.add(resp);
        }
        return result;
    }

    @Override
    @Transactional
    public TagResponse taoTag(TagRequest request) {
        String slug = taoSlug(request.getName(), request.getSlug());
        if (tagRepository.existsBySlug(slug) == true) {
            throw new AppException(400, "Định danh thẻ (Slug) đã tồn tại: " + slug);
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setSlug(slug);

        Tag saved = tagRepository.save(tag);
        
        TagResponse resp = new TagResponse();
        resp.setId(saved.getId());
        resp.setName(saved.getName());
        resp.setSlug(saved.getSlug());
        
        return resp;
    }

    @Override
    @Transactional
    public TagResponse capNhatTag(Long tagId, TagRequest request) {
        Optional<Tag> optTag = tagRepository.findById(tagId);
        if (optTag.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy thẻ chủ đề (Tag).");
        }

        Tag tag = optTag.get();
        tag.setName(request.getName());

        if (request.getSlug() != null && request.getSlug().equals(tag.getSlug()) == false) {
            if (tagRepository.existsBySlug(request.getSlug()) == true) {
                throw new AppException(400, "Đường dẫn (Slug) thẻ đã được đăng ký.");
            }
            tag.setSlug(request.getSlug());
        }

        Tag saved = tagRepository.save(tag);
        
        TagResponse resp = new TagResponse();
        resp.setId(saved.getId());
        resp.setName(saved.getName());
        resp.setSlug(saved.getSlug());
        
        return resp;
    }

    @Override
    @Transactional
    public void xoaTag(Long tagId) {
        if (tagRepository.existsById(tagId) == false) {
            throw new AppException(404, "Không tìm thấy thẻ (Tag) để xóa.");
        }
        ctPostTagRepository.xoaHetBaiVietCuaTag(tagId);
        tagRepository.deleteById(tagId);
    }

    // =========================================================================
    // ĐỘNG CƠ XỬ LÝ HÀNG LOẠT (BULK ACTION ENGINE)
    // =========================================================================

    /**
     * Vận hành lệnh điều chỉnh Trạng thái xuất bản lên một tệp lớn Bài viết.
     */
    @Override
    @Transactional
    public void doiTrangThaiXuatBanNhieu(BulkActionRequest request) {
        boolean published = false;
        if (request.getPublished() != null && request.getPublished() == true) {
            published = true;
        }
        
        List<Long> ids = request.getIds();
        for (int i = 0; i < ids.size(); i = i + 1) {
            Long postId = ids.get(i);
            Optional<Post> optPost = postRepository.findById(postId);
            
            if (optPost.isPresent() == true) {
                Post post = optPost.get();
                post.setPublished(published);
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
            }
        }
    }

    /**
     * Vận hành lệnh xóa vĩnh viễn trên diện rộng.
     * Cần thận trọng: Mọi Dữ liệu con sẽ bị gỡ bỏ theo cơ chế Thác đổ (Cascade) thủ công.
     */
    @Override
    @Transactional
    public void xoaNhieuBaiViet(BulkActionRequest request) {
        List<Long> ids = request.getIds();
        for (int i = 0; i < ids.size(); i = i + 1) {
            Long postId = ids.get(i);

            if (postRepository.existsById(postId) == true) {
                ctFileDownloadRepository.xoaHetLuotTaiCuaBaiViet(postId);
                postViewLogRepository.xoaHetLuotXemCuaBaiViet(postId);
                ctLikePostRepository.xoaHetReactionCuaBaiViet(postId);
                ctPostCmtRepository.xoaHetCmtCuaBaiViet(postId);
                ctPostEventRepository.xoaHetSuKienCuaBaiViet(postId);
                ctPostRoleRepository.xoaHetQuyenCuaBaiViet(postId);
                ctPostTagRepository.xoaHetTagCuaBaiViet(postId);
                postImageRepository.xoaHetAnhCuaBaiViet(postId);
                postFileRepository.xoaHetFileCuaBaiViet(postId);
                postRepository.deleteById(postId);
            }
        }
    }

    // =========================================================================
    // QUẢN LÝ HÌNH ẢNH GALLERY
    // =========================================================================

    /**
     * Lấy danh sách ảnh gallery của bài viết sắp xếp theo DISPLAY_ORDER.
     */
    @Override
    public List<PostImageResponse> layAnhCuaBaiViet(Long postId) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        List<PostImage> danhSach = postImageRepository.findByPostIdOrderByDisplayOrderAsc(postId);
        List<PostImageResponse> result = new ArrayList<>();
        for (int i = 0; i < danhSach.size(); i = i + 1) {
            PostImage img = danhSach.get(i);
            PostImageResponse resp = new PostImageResponse();
            resp.setId(img.getId());
            resp.setImageUrl(img.getImageUrl());
            resp.setDisplayOrder(img.getDisplayOrder());
            result.add(resp);
        }
        return result;
    }

    /**
     * Upload ảnh vào gallery bài viết.
     * Validate định dạng (jpg/png/gif/webp) và dung lượng (tối đa 5MB).
     * Thứ tự hiển thị = tổng số ảnh hiện tại + 1.
     */
    @Override
    @Transactional
    public PostImageResponse uploadAnhGallery(Long postId, MultipartFile file) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        String phanMoRong = kiemTraFileAnhUploadPost(file);
        String tenFile = UUID.randomUUID().toString() + phanMoRong;
        Path thuMucLuu = Paths.get(uploadBasePath, "posts", "images").normalize();
        Path duongDanLuu = thuMucLuu.resolve(tenFile).normalize();

        if (duongDanLuu.startsWith(thuMucLuu) == false) {
            throw new AppException(400, "Đường dẫn upload ảnh gallery không hợp lệ.");
        }
        try {
            Files.createDirectories(thuMucLuu);
            file.transferTo(duongDanLuu);
        } catch (IOException ex) {
            throw new AppException(500, "Không thể lưu ảnh gallery trên server.");
        }

        long soAnhHienTai = postImageRepository.countByPostId(postId);
        PostImage img = new PostImage();
        img.setPost(optPost.get());
        img.setImageUrl("/uploads/posts/images/" + tenFile);
        img.setDisplayOrder((int) (soAnhHienTai + 1));
        PostImage saved = postImageRepository.save(img);

        PostImageResponse resp = new PostImageResponse();
        resp.setId(saved.getId());
        resp.setImageUrl(saved.getImageUrl());
        resp.setDisplayOrder(saved.getDisplayOrder());
        return resp;
    }

    /**
     * Xóa ảnh khỏi gallery.
     * Kiểm tra ảnh thuộc bài viết trước khi xóa.
     */
    @Override
    @Transactional
    public void xoaAnhGallery(Long postId, Long imageId) {
        Optional<PostImage> optImg = postImageRepository.findById(imageId);
        if (optImg.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy ảnh.");
        }
        PostImage img = optImg.get();
        if (img.getPost().getId().equals(postId) == false) {
            throw new AppException(400, "Ảnh không thuộc bài viết này.");
        }
        postImageRepository.deleteById(imageId);
    }

    /**
     * Đổi thứ tự hiển thị ảnh gallery.
     * Nhận danh sách [{id, displayOrder}] và cập nhật từng bản ghi.
     */
    @Override
    @Transactional
    public void doiThuTuAnh(Long postId, List<Map<String, Object>> reorderData) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        for (int i = 0; i < reorderData.size(); i = i + 1) {
            Map<String, Object> item = reorderData.get(i);
            Long imageId = Long.valueOf(item.get("id").toString());
            Integer displayOrder = Integer.valueOf(item.get("displayOrder").toString());
            Optional<PostImage> optImg = postImageRepository.findById(imageId);
            if (optImg.isPresent() == true && optImg.get().getPost().getId().equals(postId) == true) {
                PostImage img = optImg.get();
                img.setDisplayOrder(displayOrder);
                postImageRepository.save(img);
            }
        }
    }

    // =========================================================================
    // QUẢN LÝ FILE ĐÍNH KÈM
    // =========================================================================

    /**
     * Lấy danh sách file đính kèm của bài viết kèm lượt tải từng file.
     */
    @Override
    public List<PostFileResponse> layFileCuaBaiViet(Long postId) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        List<PostFile> danhSach = postFileRepository.findByPostId(postId);
        List<Object[]> luotTaiData = ctFileDownloadRepository.demLuotTaiTheoTungFile(postId);

        Map<Long, Long> luotTaiMap = new HashMap<>();
        for (int i = 0; i < luotTaiData.size(); i = i + 1) {
            Object[] row = luotTaiData.get(i);
            luotTaiMap.put((Long) row[0], (Long) row[1]);
        }

        List<PostFileResponse> result = new ArrayList<>();
        for (int i = 0; i < danhSach.size(); i = i + 1) {
            PostFile pf = danhSach.get(i);
            PostFileResponse resp = new PostFileResponse();
            resp.setId(pf.getId());
            resp.setFileName(pf.getFileName());
            resp.setFileUrl(pf.getFileUrl());
            resp.setFileType(pf.getFileType());
            resp.setFileSize(pf.getFileSize());
            resp.setDownloadCount(luotTaiMap.getOrDefault(pf.getId(), 0L));
            result.add(resp);
        }
        return result;
    }

    /**
     * Upload file đính kèm bài viết.
     * Validate định dạng (pdf/doc/docx/pptx/xlsx) và dung lượng (tối đa 10MB).
     */
    @Override
    @Transactional
    public PostFileResponse uploadFileDinhKem(Long postId, MultipartFile file) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        String phanMoRong = kiemTraFileTaiLieu(file);
        String tenFileGoc = file.getOriginalFilename();
        String tenFileLuu = UUID.randomUUID().toString() + phanMoRong;
        Path thuMucLuu = Paths.get(uploadBasePath, "posts", "files").normalize();
        Path duongDanLuu = thuMucLuu.resolve(tenFileLuu).normalize();

        if (duongDanLuu.startsWith(thuMucLuu) == false) {
            throw new AppException(400, "Đường dẫn upload file không hợp lệ.");
        }
        try {
            Files.createDirectories(thuMucLuu);
            file.transferTo(duongDanLuu);
        } catch (IOException ex) {
            throw new AppException(500, "Không thể lưu file đính kèm trên server.");
        }

        PostFile pf = new PostFile();
        pf.setPost(optPost.get());
        pf.setFileName(tenFileGoc != null ? tenFileGoc : tenFileLuu);
        pf.setFileUrl("/uploads/posts/files/" + tenFileLuu);
        pf.setFileType(phanMoRong.replace(".", ""));
        pf.setFileSize(file.getSize());
        PostFile saved = postFileRepository.save(pf);

        PostFileResponse resp = new PostFileResponse();
        resp.setId(saved.getId());
        resp.setFileName(saved.getFileName());
        resp.setFileUrl(saved.getFileUrl());
        resp.setFileType(saved.getFileType());
        resp.setFileSize(saved.getFileSize());
        resp.setDownloadCount(0L);
        return resp;
    }

    /**
     * Xóa file đính kèm.
     * Xóa CT_FILE_DOWNLOADS trước để tránh vi phạm FK, rồi xóa PostFile.
     */
    @Override
    @Transactional
    public void xoaFileDinhKem(Long postId, Long fileId) {
        Optional<PostFile> optFile = postFileRepository.findById(fileId);
        if (optFile.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy file đính kèm.");
        }
        PostFile pf = optFile.get();
        if (pf.getPost().getId().equals(postId) == false) {
            throw new AppException(400, "File không thuộc bài viết này.");
        }
        ctFileDownloadRepository.xoaHetLuotTaiCuaBaiViet(postId);
        postFileRepository.deleteById(fileId);
    }

    // =========================================================================
    // XEM BÌNH LUẬN / REACTIONS / SỰ KIỆN LIÊN KẾT
    // =========================================================================

    /**
     * Lấy bình luận preview của bài viết (phân trang) cho admin.
     * Content bị cắt ngắn tối đa 200 ký tự.
     */
    @Override
    public Page<PostCommentPreviewResponse> layCmtCuaBaiVietAdmin(Long postId, int page, int size) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Cmt> cmtPage = cmtRepository.layCmtCuaBaiViet(postId, pageable);
        List<Cmt> cmtList = cmtPage.getContent();
        List<PostCommentPreviewResponse> result = new ArrayList<>();

        for (int i = 0; i < cmtList.size(); i = i + 1) {
            Cmt cmt = cmtList.get(i);
            PostCommentPreviewResponse resp = new PostCommentPreviewResponse();
            resp.setCmtId(cmt.getId());

            if (cmt.getUser() != null) {
                resp.setAuthorName(cmt.getUser().getFullName());
                resp.setAuthorAvatar(null);
            }

            String noiDung = cmt.getContent();
            if (noiDung != null && noiDung.length() > 200) {
                noiDung = noiDung.substring(0, 200);
            }
            resp.setContent(noiDung);
            resp.setCreatedAt(cmt.getCreatedAt());
            result.add(resp);
        }
        return new PageImpl<>(result, pageable, cmtPage.getTotalElements());
    }

    /**
     * Lấy reactions chi tiết của bài viết, gom nhóm theo loại cảm xúc.
     */
    @Override
    public List<PostReactionSummary> layReactionsCuaBaiViet(Long postId) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        List<Object[]> data = ctLikePostRepository.demReactionChiTietCuaBaiViet(postId);
        List<PostReactionSummary> result = new ArrayList<>();

        for (int i = 0; i < data.size(); i = i + 1) {
            Object[] row = data.get(i);
            PostReactionSummary summary = new PostReactionSummary();
            summary.setCode((String) row[0]);
            summary.setName((String) row[1]);
            summary.setIconUrl((String) row[2]);
            summary.setCount((Long) row[3]);
            result.add(summary);
        }
        return result;
    }

    /**
     * Lấy danh sách buổi sự kiện liên kết với bài viết.
     */
    @Override
    public List<PostLinkedEventResponse> laySuKienLienKet(Long postId) {
        if (postRepository.existsById(postId) == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        List<CtEvent> danhSach = ctPostEventRepository.laySuKienCuaBaiViet(postId);
        List<PostLinkedEventResponse> result = new ArrayList<>();

        for (int i = 0; i < danhSach.size(); i = i + 1) {
            CtEvent ct = danhSach.get(i);
            PostLinkedEventResponse resp = new PostLinkedEventResponse();
            resp.setCtEventId(ct.getId());
            resp.setEventTitle(ct.getTitle());
            resp.setStartTime(ct.getStartTime());
            resp.setEndTime(ct.getEndTime());
            result.add(resp);
        }
        return result;
    }

    /**
     * Liên kết bài viết với buổi sự kiện.
     * Validate postId tồn tại, ctEventId tồn tại và chưa có liên kết trùng.
     */
    @Override
    @Transactional
    public void lienKetSuKien(Long postId, Long ctEventId) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy bài viết.");
        }
        Optional<CtEvent> optEvent = ctEventRepository.findById(ctEventId);
        if (optEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy buổi sự kiện.");
        }
        if (ctPostEventRepository.kiemTraLienKetTonTai(postId, ctEventId) == true) {
            throw new AppException(400, "Liên kết này đã tồn tại.");
        }

        CtPostEvent.CtPostEventId pkId = new CtPostEvent.CtPostEventId(ctEventId, postId);
        CtPostEvent lienKet = new CtPostEvent();
        lienKet.setId(pkId);
        lienKet.setPost(optPost.get());
        lienKet.setCtEvent(optEvent.get());
        ctPostEventRepository.save(lienKet);
    }

    /**
     * Xóa liên kết bài viết - sự kiện.
     */
    @Override
    @Transactional
    public void xoaLienKetSuKien(Long postId, Long ctEventId) {
        if (ctPostEventRepository.kiemTraLienKetTonTai(postId, ctEventId) == false) {
            throw new AppException(404, "Không tìm thấy liên kết bài viết - sự kiện.");
        }
        CtPostEvent.CtPostEventId pkId = new CtPostEvent.CtPostEventId(ctEventId, postId);
        ctPostEventRepository.deleteById(pkId);
    }

    /**
     * Kiểm tra định dạng và dung lượng tài liệu trước khi ghi vào server.
     * Chấp nhận: pdf, doc, docx, pptx, xlsx. Tối đa 10MB.
     */
    private String kiemTraFileTaiLieu(MultipartFile file) {
        if (file == null || file.isEmpty() == true) {
            throw new AppException(400, "Vui lòng chọn file tài liệu cần upload.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new AppException(400, "Tên file không hợp lệ.");
        }
        String lower = originalName.toLowerCase();
        if (lower.endsWith(".pdf") == false && lower.endsWith(".doc") == false
                && lower.endsWith(".docx") == false && lower.endsWith(".pptx") == false
                && lower.endsWith(".xlsx") == false) {
            throw new AppException(400, "Chỉ chấp nhận file tài liệu (pdf, doc, docx, pptx, xlsx).");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new AppException(400, "Dung lượng file vượt quá 10MB.");
        }
        int lastDot = lower.lastIndexOf('.');
        return lower.substring(lastDot);
    }

    // =========================================================================
    // CÔNG CỤ BỔ TRỢ NỘI BỘ (INTERNAL MAPPERS)
    // =========================================================================

    private void ganQuyenChoBaiViet(Post post, List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty() == true) {
            UserRole publicRole = userRoleRepository.findByRoleName("PUBLIC").orElse(null);
            if (publicRole != null) {
                CtPostRoleId pkId = new CtPostRoleId(post.getId(), publicRole.getId());
                CtPostRole bridge = new CtPostRole();
                bridge.setId(pkId);
                bridge.setPost(post);
                bridge.setRole(publicRole);
                ctPostRoleRepository.save(bridge);
            }
            return;
        }

        for (int i = 0; i < roleIds.size(); i = i + 1) {
            Integer rId = roleIds.get(i);
            Optional<UserRole> optRole = userRoleRepository.findById(rId);
            if (optRole.isPresent() == true) {
                CtPostRoleId pkId = new CtPostRoleId(post.getId(), rId);
                CtPostRole bridge = new CtPostRole();
                bridge.setId(pkId);
                bridge.setPost(post);
                bridge.setRole(optRole.get());
                ctPostRoleRepository.save(bridge);
            }
        }
    }
    /**
     * Neo kết các Thẻ chủ đề (Tags) vào Bài viết thông qua Bảng định tuyến CT_POST_TAGS.
     */
    private void ganTagChoBaiViet(Post post, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty() == true) {
            return;
        }

        for (int i = 0; i < tagIds.size(); i = i + 1) {
            Long tagId = tagIds.get(i);
            Optional<Tag> optTag = tagRepository.findById(tagId);
            
            if (optTag.isPresent() == true) {
                CtPostTag.CtPostTagId pkId = new CtPostTag.CtPostTagId(post.getId(), tagId);
                CtPostTag bridge = new CtPostTag();
                bridge.setId(pkId);
                bridge.setPost(post);
                bridge.setTag(optTag.get());
                ctPostTagRepository.save(bridge);
            }
        }
    }

    /**
     * Xử lý định dạng Text thành chuỗi tĩnh an toàn (Friendly URL).
     */
    private String taoSlug(String text, String customSlug) {
        if (customSlug != null && customSlug.trim().isEmpty() == false) {
            return customSlug.trim().toLowerCase();
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(normalized).replaceAll("");

        return noAccent.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Đóng gói danh sách bài viết bằng batch query (giảm N+1).
     * Thay vì 5 query/post, chỉ cần 5 query cho cả trang.
     */
    private List<PostResponse> chuyenDoiNhieuPostResponse(List<Post> posts) {
        if (posts.isEmpty() == true) {
            return new ArrayList<>();
        }

        List<Long> postIds = new ArrayList<>();
        for (int i = 0; i < posts.size(); i = i + 1) {
            postIds.add(posts.get(i).getId());
        }

        List<Object[]> rolesData = ctPostRoleRepository.layQuyenCuaNhieuBaiViet(postIds);
        List<Object[]> tagsData = ctPostTagRepository.layTagCuaNhieuBaiViet(postIds);
        List<Object[]> viewsData = postViewLogRepository.demLuotXemTheoNhieuBaiViet(postIds);
        List<Object[]> downloadsData = ctFileDownloadRepository.demLuotTaiTheoNhieuBaiViet(postIds);
        List<Object[]> commentsData = ctPostCmtRepository.demCmtTheoNhieuBaiViet(postIds);

        Map<Long, List<String>> roleMap = new HashMap<>();
        for (int i = 0; i < rolesData.size(); i = i + 1) {
            Object[] row = rolesData.get(i);
            Long postId = (Long) row[0];
            String roleName = (String) row[2];
            if (roleMap.containsKey(postId) == false) {
                roleMap.put(postId, new ArrayList<>());
            }
            roleMap.get(postId).add(roleName);
        }

        Map<Long, List<TagResponse>> tagMap = new HashMap<>();
        for (int i = 0; i < tagsData.size(); i = i + 1) {
            Object[] row = tagsData.get(i);
            Long postId = (Long) row[0];
            TagResponse tr = new TagResponse();
            tr.setId((Long) row[1]);
            tr.setName((String) row[2]);
            tr.setSlug((String) row[3]);
            if (tagMap.containsKey(postId) == false) {
                tagMap.put(postId, new ArrayList<>());
            }
            tagMap.get(postId).add(tr);
        }

        Map<Long, Long> viewMap = new HashMap<>();
        for (int i = 0; i < viewsData.size(); i = i + 1) {
            Object[] row = viewsData.get(i);
            viewMap.put((Long) row[0], (Long) row[1]);
        }

        Map<Long, Long> downloadMap = new HashMap<>();
        for (int i = 0; i < downloadsData.size(); i = i + 1) {
            Object[] row = downloadsData.get(i);
            downloadMap.put((Long) row[0], (Long) row[1]);
        }

        Map<Long, Long> commentMap = new HashMap<>();
        for (int i = 0; i < commentsData.size(); i = i + 1) {
            Object[] row = commentsData.get(i);
            commentMap.put((Long) row[0], (Long) row[1]);
        }

        List<PostResponse> result = new ArrayList<>();
        for (int i = 0; i < posts.size(); i = i + 1) {
            Post post = posts.get(i);
            PostResponse resp = new PostResponse();
            resp.setId(post.getId());
            resp.setTitle(post.getTitle());
            resp.setSlug(post.getSlug());
            resp.setSummary(post.getSummary());
            resp.setThumbnailUrl(post.getThumbnailUrl());
            resp.setPublished(post.isPublished());
            resp.setFeatured(post.isFeatured());
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

            List<String> roleNames = roleMap.get(post.getId());
            if (roleNames == null) {
                roleNames = new ArrayList<>();
            }
            resp.setAllowedRoleNames(roleNames);

            if (roleNames.isEmpty() == true || (roleNames.size() == 1 && "PUBLIC".equals(roleNames.get(0)))) {
                resp.setAccessLevel("Công khai");
            } else {
                String joined = "";
                for (int j = 0; j < roleNames.size(); j = j + 1) {
                    if (j > 0) {
                        joined = joined + ", ";
                    }
                    joined = joined + roleNames.get(j);
                }
                resp.setAccessLevel(joined);
            }

            List<TagResponse> tags = tagMap.get(post.getId());
            resp.setTags(tags != null ? tags : new ArrayList<>());
            resp.setViewCount(viewMap.getOrDefault(post.getId(), 0L));
            resp.setDownloadCount(downloadMap.getOrDefault(post.getId(), 0L));
            resp.setCommentCount(commentMap.getOrDefault(post.getId(), 0L));

            result.add(resp);
        }
        return result;
    }

    /**
     * Đóng gói bức tranh tổng quan của một Bài viết.
     * Liên kết dữ liệu đa hướng: Phân quyền, Thẻ phân loại và Các chỉ số Thống kê.
     */
    private PostResponse chuyenDoiPostResponse(Post post) {
        PostResponse resp = new PostResponse();
        resp.setId(post.getId());
        resp.setTitle(post.getTitle());
        resp.setSlug(post.getSlug());
        resp.setSummary(post.getSummary());
        resp.setThumbnailUrl(post.getThumbnailUrl());
        resp.setPublished(post.isPublished());
        resp.setFeatured(post.isFeatured());
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

        List<UserRole> roles = ctPostRoleRepository.layDanhSachQuyenCuaBaiViet(post.getId());
        List<String> roleNames = new ArrayList<>();
        if(roles != null) {
            for(int i = 0; i < roles.size(); i = i + 1) {
                roleNames.add(roles.get(i).getRoleName());
            }
        }
        resp.setAllowedRoleNames(roleNames);

        // Tính cấp độ truy cập tổng hợp cho hiển thị admin
        if (roleNames.isEmpty() == true || (roleNames.size() == 1 && "PUBLIC".equals(roleNames.get(0)))) {
            resp.setAccessLevel("Công khai");
        } else {
            String joined = "";
            for (int j = 0; j < roleNames.size(); j = j + 1) {
                if (j > 0) { joined = joined + ", "; }
                joined = joined + roleNames.get(j);
            }
            resp.setAccessLevel(joined);
        }

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
}