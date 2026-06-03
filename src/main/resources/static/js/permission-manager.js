// src/main/resources/static/js/permission-manager.js

/**
 * =========================================================================
 * PERMISSION MANAGER — QUẢN LÝ QUYỀN HẠT LỰU PHÍA FRONTEND
 * =========================================================================
 * Gọi API lấy danh sách quyền của user hiện tại, cache kết quả trong RAM.
 * Cung cấp hàm helper để ẩn/hiện UI theo quyền.
 * Đây CHỈ là UX (làm đẹp giao diện) — backend vẫn enforce ở PermissionInterceptor.
 *
 * SUPERADMIN (isSuperAdmin = true) luôn thấy TẤT CẢ.
 */
var PermissionManager = (function () {

    // Biến cache nội bộ — nạp 1 lần khi trang load
    var _permissions = [];
    var _roles = [];
    var _isSuperAdmin = false;
    var _roleLevel = 999;
    var _daNap = false;

    /**
     * Nạp danh sách quyền từ server (gọi 1 lần duy nhất khi trang load).
     * Sau khi nạp xong, tự động gọi renderMenuTheoQuyen() để ẩn/hiện sidebar.
     */
    function napQuyenTuServer(callback) {
        var token = localStorage.getItem('jwtToken');

        $.ajax({
            url: '/api/admin/role-management/my-permissions',
            type: 'GET',
            headers: { 'Authorization': 'Bearer ' + token },
            success: function (response) {
                if (response !== null && response.data !== null && response.data !== undefined) {
                    var data = response.data;
                    _permissions = data.permissions || [];
                    _roles = data.roles || [];
                    _isSuperAdmin = data.superAdmin === true;
                    _roleLevel = data.roleLevel || 999;
                }
                _daNap = true;

                // Tự động ẩn/hiện menu sidebar sau khi nạp xong
                renderMenuTheoQuyen();

                if (typeof callback === 'function') {
                    callback();
                }
            },
            error: function () {
                // Nạp thất bại — danh sách quyền rỗng, menu bị ẩn (fail-closed — đúng chiều an toàn)
                _daNap = true;
                if (typeof callback === 'function') {
                    callback();
                }
            }
        });
    }

    /**
     * Kiểm tra user hiện tại có quyền hạt lựu cụ thể hay không.
     * SUPERADMIN luôn trả về true.
     */
    function coQuyen(permissionCode) {
        if (_isSuperAdmin === true) {
            return true;
        }
        for (var i = 0; i < _permissions.length; i = i + 1) {
            if (_permissions[i] === permissionCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra user có BẤT KỲ quyền nào trong danh sách hay không.
     * SUPERADMIN luôn trả về true.
     */
    function coMotTrongCacQuyen(danhSachQuyen) {
        if (_isSuperAdmin === true) {
            return true;
        }
        for (var i = 0; i < danhSachQuyen.length; i = i + 1) {
            var permissionCode = danhSachQuyen[i];
            if (permissionCode !== null && permissionCode !== undefined) {
                permissionCode = permissionCode.trim();
            }
            if (permissionCode !== '' && coQuyen(permissionCode) === true) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra user hiện tại có chức vụ (role) cụ thể hay không.
     */
    function coRole(roleName) {
        if (_isSuperAdmin === true) {
            return true;
        }
        for (var i = 0; i < _roles.length; i = i + 1) {
            if (_roles[i] === roleName) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra user hiện tại có phải SUPERADMIN hay không.
     */
    function laSuperAdmin() {
        return _isSuperAdmin;
    }

    /**
     * Ẩn 1 element HTML nếu user KHÔNG có quyền tương ứng.
     * Nếu user có quyền hoặc là SUPERADMIN → giữ nguyên hiển thị.
     */
    function anNeuKhongCoQuyen(element, permissionCode) {
        if (element === null || element === undefined) {
            return;
        }
        if (coQuyen(permissionCode) === false) {
            element.style.display = 'none';
        }
    }

    /**
     * Ẩn tất cả element khớp CSS selector nếu user KHÔNG có quyền.
     */
    function anTatCaNeuKhongCoQuyen(selector, permissionCode) {
        var elements = document.querySelectorAll(selector);
        for (var i = 0; i < elements.length; i = i + 1) {
            anNeuKhongCoQuyen(elements[i], permissionCode);
        }
    }

    /**
     * Duyệt tất cả element có attribute data-permission trong DOM.
     * Ẩn element nếu user không có quyền tương ứng.
     * Hỗ trợ data-permission="QUYEN_A,QUYEN_B" (cần ÍT NHẤT 1 quyền).
     */
    function renderMenuTheoQuyen() {
        var elements = document.querySelectorAll('[data-permission]');
        for (var i = 0; i < elements.length; i = i + 1) {
            var el = elements[i];
            var requiredPerms = el.getAttribute('data-permission');

            if (requiredPerms === null || requiredPerms === '') {
                continue;
            }

            // Hỗ trợ nhiều quyền cách nhau bởi dấu phẩy (cần ÍT NHẤT 1)
            var danhSachQuyen = requiredPerms.split(',');
            if (coMotTrongCacQuyen(danhSachQuyen) === false) {
                el.style.display = 'none';
            }
        }
    }

    /**
     * Kiểm tra quyền đã được nạp từ server chưa.
     */
    function daNapXong() {
        return _daNap;
    }

    // Public API
    return {
        napQuyenTuServer: napQuyenTuServer,
        coQuyen: coQuyen,
        coMotTrongCacQuyen: coMotTrongCacQuyen,
        coRole: coRole,
        laSuperAdmin: laSuperAdmin,
        anNeuKhongCoQuyen: anNeuKhongCoQuyen,
        anTatCaNeuKhongCoQuyen: anTatCaNeuKhongCoQuyen,
        renderMenuTheoQuyen: renderMenuTheoQuyen,
        daNapXong: daNapXong
    };
})();
