//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/RolesServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IRolesRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IRolesService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =========================================================================
 * THỰC THI DỊCH VỤ PHÂN QUYỀN VÀ TRUY XUẤT ĐỊNH DANH (CHUẨN MÙ)
 * =========================================================================
 */
@Service
@RequiredArgsConstructor
public class RolesServiceImpl implements IRolesService {

    private final IRolesRepository rolesRepository;
    private final IUserService userService;
    private final IAuditService auditService;

    @Override
    public List<User> getAllUsersPaged(int pageNo, int pageSize, String sortBy) {
        List<User> users = rolesRepository.findAllWithPagination(pageNo, pageSize, sortBy);
        
        if (users != null) {
            Object[] usersArray = users.toArray();
            for (int i = 0; i < usersArray.length; i = i + 1) {
                User u = (User) usersArray[i];
                userService.napQuyenChoNguoiDung(u);
            }
        }
        
        return users;
    }

    @Override
    public int getTotalPages(int pageSize) {
        return rolesRepository.getTotalPages(pageSize);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        List<User> users = rolesRepository.searchUsers(keyword);
        
        if (users != null) {
            Object[] usersArray = users.toArray();
            for (int i = 0; i < usersArray.length; i = i + 1) {
                User u = (User) usersArray[i];
                userService.napQuyenChoNguoiDung(u);
            }
        }
        
        return users;
    }

    @Override
    public List<User> searchAdvanced(String keyword, String status, String roleName, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        List<User> users = rolesRepository.searchAdvanced(keyword, status, roleName, pageable);
        
        if (users != null) {
            Object[] usersArray = users.toArray();
            for (int i = 0; i < usersArray.length; i = i + 1) {
                User u = (User) usersArray[i];
                userService.napQuyenChoNguoiDung(u);
            }
        }
        
        return users;
    }

    @Override
    public long countSearchAdvanced(String keyword, String status, String roleName) {
        return rolesRepository.countSearchAdvanced(keyword, status, roleName);
    }

    @Override
    @Transactional
    public void bulkLockUnlock(List<Long> userIds, boolean lock, String reason, User currentUser) {
        if (userIds != null) {
            Object[] idArray = userIds.toArray();
            String actionCode = "";
            if (lock == true) {
                actionCode = "LOCK_USER";
            } else {
                actionCode = "UNLOCK_USER";
            }

            for (int i = 0; i < idArray.length; i = i + 1) {
                Long targetId = Long.valueOf(idArray[i].toString());
                
                // Gọi IUserService để thực thi khóa (bao gồm cả đối soát Level)
                userService.lockUnlockUser(targetId, lock, currentUser);
                
                // Ghi nhận vào Sổ tay Kiểm toán
                auditService.logAction(targetId, actionCode, null, currentUser.getId(), reason);
            }
        }
    }
}