// src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPostRoleRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICtPostRoleRepository extends JpaRepository<CtPostRole, CtPostRole.CtPostRoleId> {

    /** Lấy danh sách các Quyền (Roles) được phép truy cập Bài viết này */
    @Query("SELECT cpr.role FROM CtPostRole cpr WHERE cpr.post.id = :postId")
    List<UserRole> layDanhSachQuyenCuaBaiViet(@Param("postId") Long postId);

    /** Dọn dẹp bảng định tuyến khi cập nhật quyền mới hoặc xóa bài viết */
    @Modifying
    @Query("DELETE FROM CtPostRole cpr WHERE cpr.post.id = :postId")
    void xoaHetQuyenCuaBaiViet(@Param("postId") Long postId);
}