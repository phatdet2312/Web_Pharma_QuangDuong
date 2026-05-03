//src/main/resources/static/js/auth-sync.js
function syncSecuritySecret() {
    // Hàm đọc cookie bằng tên
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    // 1. Kiểm tra xem có cookie tạm từ Google Login gửi về không
    const tempSecret = getCookie('temp_secret');

    if (tempSecret) {
        // 2. Lưu vào localStorage
        sessionStorage.setItem('clientSecret', tempSecret);
        // 3. Xóa cookie ngay lập tức bằng cách cho nó hết hạn (Expire)
        document.cookie = "temp_secret=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        console.log("[Security] Đã đồng bộ chìa khóa bảo mật từ Google.");
    }
}

// Chạy ngay khi file JS này được load
syncSecuritySecret();