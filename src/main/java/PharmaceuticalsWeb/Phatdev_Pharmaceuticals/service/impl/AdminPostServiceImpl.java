//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AdminPostServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CategoryRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PostRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.TagRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Category;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostTag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICategoryRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtFileDownloadRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtLikePostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostTagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostFileRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostImageRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostViewLogRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ITagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        PostStatsResponse stats = new PostStatsResponse();
        stats.setTotalAll(totalAll);
        stats.setTotalPublished(totalPublished);
        stats.setTotalDraft(totalDraft);
        stats.setTotalGated(totalGated);
        stats.setTotalCategories(totalCategories);
        stats.setTotalTags(totalTags);
        stats.setTotalDownloads(totalDownloads);
        stats.setTotalReactions(totalReactions);
        return stats;
    }

    /**
     * Bộ máy tìm kiếm đa chiều dành cho Khối quản trị nội dung.
     * Hỗ trợ khai thác tất cả ấn phẩm, bao gồm cả những bản lưu nháp.
     */
    @Override
    public Page<PostResponse> layDanhSachBaiViet(String keyword, Integer categoryId, String accessLevel, Boolean isPublished, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }
        
        String al = null;
        if (accessLevel != null && accessLevel.trim().isEmpty() == false) {
            al = accessLevel.trim();
        }

        Page<Post> posts = postRepository.timKiemBaiVietAdmin(kw, categoryId, al, isPublished, pageable);
        
        List<Post> postListTuDb = posts.getContent();
        List<PostResponse> responseList = new ArrayList<>();
        
        for (int i = 0; i < postListTuDb.size(); i = i + 1) {
            Post p = postListTuDb.get(i);
            PostResponse dto = chuyenDoiPostResponse(p);
            responseList.add(dto);
        }
        
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
        
        if (request.getAccessLevel() != null && request.getAccessLevel().isEmpty() == false) {
            post.setAccessLevel(request.getAccessLevel());
        } else {
            post.setAccessLevel("PUBLIC");
        }
        
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
        
        if (request.getAccessLevel() != null && request.getAccessLevel().isEmpty() == false) {
            post.setAccessLevel(request.getAccessLevel());
        }
        
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
        
        Object[] idsArray = request.getIds().toArray();
        for (int i = 0; i < idsArray.length; i = i + 1) {
            Long postId = (Long) idsArray[i];
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
        Object[] idsArray = request.getIds().toArray();
        for (int i = 0; i < idsArray.length; i = i + 1) {
            Long postId = (Long) idsArray[i];
            
            if (postRepository.existsById(postId) == true) {
                ctPostTagRepository.xoaHetTagCuaBaiViet(postId);
                postImageRepository.xoaHetAnhCuaBaiViet(postId);
                postFileRepository.xoaHetFileCuaBaiViet(postId);
                postRepository.deleteById(postId);
            }
        }
    }

    // =========================================================================
    // CÔNG CỤ BỔ TRỢ NỘI BỘ (INTERNAL MAPPERS)
    // =========================================================================

    /**
     * Neo kết các Thẻ chủ đề (Tags) vào Bài viết thông qua Bảng định tuyến CT_POST_TAGS.
     */
    private void ganTagChoBaiViet(Post post, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty() == true) {
            return;
        }

        Object[] tagIdArr = tagIds.toArray();
        for (int i = 0; i < tagIdArr.length; i = i + 1) {
            Long tagId = (Long) tagIdArr[i];
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