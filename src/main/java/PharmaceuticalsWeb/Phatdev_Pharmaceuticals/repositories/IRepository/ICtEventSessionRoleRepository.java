// src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtEventSessionRoleRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventSessionRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICtEventSessionRoleRepository extends JpaRepository<CtEventSessionRole, CtEventSessionRole.CtEventSessionRoleId> {

    @Query("SELECT cesr.role FROM CtEventSessionRole cesr WHERE cesr.ctEvent.id = :ctEventId")
    List<UserRole> layDanhSachQuyenCuaBuoi(@Param("ctEventId") Long ctEventId);

    @Modifying
    @Query("DELETE FROM CtEventSessionRole cesr WHERE cesr.ctEvent.id = :ctEventId")
    void xoaHetQuyenCuaBuoi(@Param("ctEventId") Long ctEventId);
}