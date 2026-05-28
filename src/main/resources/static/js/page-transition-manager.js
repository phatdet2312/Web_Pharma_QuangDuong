// static/js/page-transition-manager.js
// Tổng quan: Utility chống nháy trang — skeleton loading + fade-swap content.
// Dùng cho tất cả trang admin và user khi load dữ liệu từ API.
// Pattern: IIFE + prototype. Không dùng ES6 class, arrow function, ternary lồng.

var PageTransitionManager = (function () {
    'use strict';

    // ================================================================
    // CẤU HÌNH MẶC ĐỊNH
    // ================================================================

    /** Thời gian fade (ms) — đồng bộ với CSS .ptm-fade-container transition */
    var THOI_GIAN_FADE = 250;

    /** Thời gian timeout tối đa (ms) — tự tắt skeleton nếu API không phản hồi */
    var THOI_GIAN_TIMEOUT = 8000;

    // ================================================================
    // HÀM NỘI BỘ — SKELETON TEMPLATES
    // ================================================================

    /**
     * Tạo HTML skeleton cho grid thống kê (stats).
     * Mỗi ô là 1 khối skeleton hình stat card.
     * @param {number} soLuong — Số ô stat cần hiển thị
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonStatGrid(soLuong) {
        var html = '<div class="ptm-stat-grid">';
        for (var i = 0; i < soLuong; i = i + 1) {
            html = html + '<div class="ptm-skeleton ptm-skeleton-stat"></div>';
        }
        html = html + '</div>';
        return html;
    }

    /**
     * Tạo HTML skeleton cho danh sách card dọc (campaigns, list items).
     * Mỗi card là 1 khối skeleton hình chữ nhật.
     * @param {number} soLuong — Số card cần hiển thị
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonCardList(soLuong) {
        var html = '<div class="ptm-card-list">';
        for (var i = 0; i < soLuong; i = i + 1) {
            html = html + '<div class="ptm-skeleton ptm-skeleton-card"></div>';
        }
        html = html + '</div>';
        return html;
    }

    /**
     * Tạo HTML skeleton cho lưới card (article grid, event grid).
     * Card dạng lưới responsive theo CSS grid.
     * @param {number} soLuong — Số card cần hiển thị
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonCardGrid(soLuong) {
        var html = '<div class="ptm-card-grid">';
        for (var i = 0; i < soLuong; i = i + 1) {
            html = html + '<div class="ptm-skeleton ptm-skeleton-card"></div>';
        }
        html = html + '</div>';
        return html;
    }

    /**
     * Tạo HTML skeleton cho phần hero (ảnh lớn + tiêu đề + metadata).
     * Dùng cho trang detail (bài viết, sự kiện).
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonDetailHero() {
        var html = '<div class="ptm-detail-layout">';
        html = html + '<div class="ptm-skeleton ptm-skeleton-hero"></div>';
        html = html + '<div class="ptm-skeleton ptm-skeleton-title" style="width:65%"></div>';
        html = html + '<div class="ptm-skeleton ptm-skeleton-text" style="width:40%"></div>';
        html = html + '<div class="ptm-skeleton ptm-skeleton-text" style="width:30%"></div>';
        html = html + '</div>';
        return html;
    }

    /**
     * Tạo HTML skeleton cho phần body nội dung (nhiều dòng paragraph).
     * Giả lập đoạn văn bản đang tải.
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonDetailBody() {
        var html = '<div class="ptm-detail-layout">';
        for (var i = 0; i < 6; i = i + 1) {
            var doRong = (i % 3 === 0) ? '100%' : ((i % 3 === 1) ? '90%' : '75%');
            html = html + '<div class="ptm-skeleton ptm-skeleton-paragraph" style="width:' + doRong + '"></div>';
        }
        html = html + '</div>';
        return html;
    }

    /**
     * Tạo HTML skeleton cho sidebar (author card, stats, links).
     * Dùng cho cột phải trang detail.
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonSidebar() {
        var html = '<div class="ptm-detail-layout">';
        html = html + '<div class="ptm-skeleton ptm-skeleton-stat"></div>';
        html = html + '<div class="ptm-skeleton ptm-skeleton-text" style="width:80%;margin-top:12px"></div>';
        html = html + '<div class="ptm-skeleton ptm-skeleton-text" style="width:60%"></div>';
        html = html + '<div class="ptm-skeleton ptm-skeleton-stat" style="margin-top:16px"></div>';
        html = html + '</div>';
        return html;
    }

    /**
     * Tạo HTML skeleton cho dòng bảng (table rows).
     * Dùng cho danh sách dạng bảng.
     * @param {number} soLuong — Số dòng cần hiển thị
     * @return {string} Chuỗi HTML skeleton
     */
    function taoSkeletonTableRows(soLuong) {
        var html = '';
        for (var i = 0; i < soLuong; i = i + 1) {
            html = html + '<div class="ptm-skeleton ptm-skeleton-row"></div>';
        }
        return html;
    }

    // ================================================================
    // BẢNG ÁNH XẠ TÊN TEMPLATE -> HÀM TẠO SKELETON
    // ================================================================

    /**
     * Ánh xạ tên template (string) sang hàm tạo HTML tương ứng.
     * Khi gọi showSkeleton, truyền tên template để chọn hình dạng phù hợp.
     */
    var BANG_TEMPLATE = {
        'statGrid':    taoSkeletonStatGrid,
        'cardList':    taoSkeletonCardList,
        'cardGrid':    taoSkeletonCardGrid,
        'detailHero':  taoSkeletonDetailHero,
        'detailBody':  taoSkeletonDetailBody,
        'sidebar':     taoSkeletonSidebar,
        'tableRows':   taoSkeletonTableRows
    };

    // ================================================================
    // HÀM NỘI BỘ — TIỆN ÍCH DOM
    // ================================================================

    /**
     * Lấy element theo ID, trả về null nếu không tìm thấy.
     * @param {string} id — ID của element
     * @return {HTMLElement|null}
     */
    function layElement(id) {
        return document.getElementById(id);
    }

    /**
     * Đảm bảo container có class ptm-fade-container để transition hoạt động.
     * Nếu chưa có thì thêm vào.
     * @param {HTMLElement} container — Element cần kiểm tra
     */
    function damBaoFadeContainer(container) {
        if (container.classList.contains('ptm-fade-container') === false) {
            container.classList.add('ptm-fade-container');
        }
    }

    // ================================================================
    // ĐỐI TƯỢNG CHÍNH — PUBLIC API
    // ================================================================

    var manager = {};

    /**
     * Hiển thị skeleton vào container, thay thế nội dung hiện tại.
     * Dùng khi bắt đầu gọi API, trước khi có dữ liệu.
     *
     * @param {string} containerId — ID của element chứa nội dung
     * @param {string} tenTemplate — Tên template skeleton ('statGrid', 'cardList', 'cardGrid', 'detailHero', 'detailBody', 'sidebar', 'tableRows')
     * @param {number} soLuong — Số lượng skeleton items (chỉ áp dụng cho statGrid, cardList, cardGrid, tableRows)
     */
    manager.showSkeleton = function (containerId, tenTemplate, soLuong) {
        var container = layElement(containerId);
        if (container === null) {
            return;
        }

        /* Lấy hàm tạo skeleton từ bảng ánh xạ */
        var hamTao = BANG_TEMPLATE[tenTemplate];
        if (typeof hamTao !== 'function') {
            return;
        }

        /* Tạo HTML skeleton — hàm không cần soLuong sẽ tự bỏ qua tham số thừa */
        var htmlSkeleton = hamTao(soLuong);

        /* Gán skeleton vào container */
        damBaoFadeContainer(container);
        container.classList.remove('ptm-fade-out');
        container.classList.add('ptm-fade-in');
        container.innerHTML = htmlSkeleton;
    };

    /**
     * Chuyển đổi nội dung container: fade-out → gọi hàm render → fade-in.
     * Đây là phương thức chính để chống nháy trang.
     *
     * Luồng hoạt động:
     *   1. Container hiện tại fade-out (opacity 0, 250ms)
     *   2. Sau khi fade-out xong, gọi hamRender() để ghi nội dung mới vào container
     *   3. Container fade-in (opacity 1, 250ms)
     *   4. Sau khi fade-in xong, gọi hamSauKhiHien() nếu có (dùng để trigger reveal observer)
     *
     * @param {string} containerId — ID của element chứa nội dung
     * @param {function} hamRender — Hàm render nội dung mới (gọi innerHTML bên trong)
     * @param {function} [hamSauKhiHien] — Hàm callback sau khi nội dung đã hiện (tùy chọn)
     */
    manager.swapContent = function (containerId, hamRender, hamSauKhiHien) {
        var container = layElement(containerId);
        if (container === null) {
            /* Không tìm thấy container — gọi render trực tiếp để không mất dữ liệu */
            if (typeof hamRender === 'function') {
                hamRender();
            }
            return;
        }

        damBaoFadeContainer(container);

        /* Bước 1: Fade-out container hiện tại */
        container.classList.add('ptm-fade-out');
        container.classList.remove('ptm-fade-in');

        /* Bước 2: Sau khi fade-out hoàn tất, ghi nội dung mới rồi fade-in */
        var daXuLy = false;
        var boHenGio = setTimeout(function () {
            if (daXuLy === true) {
                return;
            }
            daXuLy = true;
            xuLySauFadeOut(container, hamRender, hamSauKhiHien);
        }, THOI_GIAN_FADE);

        /* Lắng nghe sự kiện transitionend để xử lý chính xác hơn setTimeout */
        container.addEventListener('transitionend', function hamXuLyTransition(e) {
            /* Chỉ xử lý transition của chính container, không phải con */
            if (e.target !== container) {
                return;
            }
            container.removeEventListener('transitionend', hamXuLyTransition);

            if (daXuLy === true) {
                return;
            }
            daXuLy = true;
            clearTimeout(boHenGio);
            xuLySauFadeOut(container, hamRender, hamSauKhiHien);
        });
    };

    /**
     * Xử lý sau khi fade-out hoàn tất: render nội dung mới rồi fade-in.
     * Hàm nội bộ, không gọi trực tiếp từ bên ngoài.
     *
     * @param {HTMLElement} container — Element chứa nội dung
     * @param {function} hamRender — Hàm render nội dung mới
     * @param {function} [hamSauKhiHien] — Callback sau khi fade-in xong
     */
    function xuLySauFadeOut(container, hamRender, hamSauKhiHien) {
        /* Gọi hàm render để ghi nội dung mới vào container */
        if (typeof hamRender === 'function') {
            hamRender();
        }

        /* Dùng requestAnimationFrame để browser kịp paint nội dung mới trước khi fade-in */
        requestAnimationFrame(function () {
            requestAnimationFrame(function () {
                /* Fade-in container với nội dung mới */
                container.classList.remove('ptm-fade-out');
                container.classList.add('ptm-fade-in');

                /* Gọi callback sau khi fade-in hoàn tất */
                if (typeof hamSauKhiHien === 'function') {
                    setTimeout(function () {
                        hamSauKhiHien();
                    }, THOI_GIAN_FADE);
                }
            });
        });
    }

    /**
     * Cập nhật text của 1 element cụ thể mà không fade toàn container.
     * Dùng cho các ô thống kê (stats) được cập nhật thường xuyên.
     * Đây là surgical update — chỉ đổi 1 giá trị, không ảnh hưởng xung quanh.
     *
     * @param {string} elementId — ID của element cần cập nhật
     * @param {string|number} giaTri — Giá trị mới
     */
    manager.updateText = function (elementId, giaTri) {
        var element = layElement(elementId);
        if (element === null) {
            return;
        }
        element.textContent = giaTri;
    };

    /**
     * Kiểm tra xem 1 container có đang hiển thị skeleton hay không.
     * Hữu ích để tránh gọi showSkeleton trùng lặp.
     *
     * @param {string} containerId — ID của element cần kiểm tra
     * @return {boolean} true nếu container đang chứa skeleton
     */
    manager.dangHienSkeleton = function (containerId) {
        var container = layElement(containerId);
        if (container === null) {
            return false;
        }
        var phanTuSkeleton = container.querySelector('.ptm-skeleton');
        return (phanTuSkeleton !== null);
    };

    /**
     * Xóa skeleton và hiển thị nội dung trống (dùng khi API trả về empty).
     * Fade-in container trống thay vì để skeleton chạy mãi.
     *
     * @param {string} containerId — ID của element
     * @param {string} htmlTrong — HTML hiển thị khi không có dữ liệu
     */
    manager.hienThiTrong = function (containerId, htmlTrong) {
        var container = layElement(containerId);
        if (container === null) {
            return;
        }
        damBaoFadeContainer(container);
        container.classList.add('ptm-fade-out');

        setTimeout(function () {
            container.innerHTML = htmlTrong;
            requestAnimationFrame(function () {
                container.classList.remove('ptm-fade-out');
                container.classList.add('ptm-fade-in');
            });
        }, THOI_GIAN_FADE);
    };

    return manager;
})();
