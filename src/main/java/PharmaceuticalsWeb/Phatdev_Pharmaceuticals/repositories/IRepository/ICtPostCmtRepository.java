//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPostCmtRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostCmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository bảng định tuyến Bài viết ↔ Comment gốc.
 */
@Repository
public interface ICtPostCmtRepository extends JpaRepository<CtPostCmt, CtPostCmt.CtPostCmtId> {

    /** Xóa liên kết comment khỏi bài viết */
    @Modifying
    @Query("DELETE FROM CtPostCmt cpc WHERE cpc.cmt.id = :cmtId")
    void xoaLienKetTheoCmt(@Param("cmtId") Long cmtId);

    /** Kiểm tra comment đã thuộc bài viết này chưa */
    boolean existsById_PostIdAndId_CmtId(Long postId, Long cmtId);

    /** Đếm tổng bình luận gốc thuộc bài viết */
    @Query("SELECT COUNT(cpc) FROM CtPostCmt cpc")
    long demTong();

    /**
     * Truy vấn Batch: Ánh xạ danh sách cmtId sang thông tin Bài viết nguồn gốc.
     * Trả về mảng Object[]{cmtId, postId, postTitle, postSlug} cho mỗi liên kết.
     * Tránh N+1: Thực thi 1 query cho cả trang thay vì 1 query mỗi bình luận.
     */
    @Query("SELECT cpc.cmt.id, p.id, p.title, p.slug FROM CtPostCmt cpc " +
           "JOIN cpc.post p WHERE cpc.cmt.id IN :cmtIds")
    List<Object[]> layCmtIdToPostMapping(@Param("cmtIds") List<Long> cmtIds);

    /** Xóa tất cả liên kết comment của bài viết (cascade delete trước khi xóa post) */
    @Modifying
    @Query("DELETE FROM CtPostCmt cpc WHERE cpc.post.id = :postId")
    void xoaHetCmtCuaBaiViet(@Param("postId") Long postId);

    /**
     * Batch: Đếm số comment của nhiều bài viết trong 1 query.
     * Trả về Object[]{postId, count} cho mỗi bài viết có comment.
     */
    @Query("SELECT cpc.post.id, COUNT(cpc) FROM CtPostCmt cpc WHERE cpc.post.id IN :postIds GROUP BY cpc.post.id")
    List<Object[]> demCmtTheoNhieuBaiViet(@Param("postIds") List<Long> postIds);
}
