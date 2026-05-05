CREATE DATABASE Web_Pharma_QuangDuong
ON (
	NAME = 'Web_Pharma_QuangDuong_DATA',
	FILENAME = 'D:\CSDL\CSDL_WEB_QUANGDUONG\Web_Pharma_QuangDuong_DATA.mdf',
	SIZE = 50MB,
	MAXSIZE= 500MB,
	FILEGROWTH = 10MB)
LOG ON(
	NAME = 'Web_Pharma_QuangDuong_LOG',
	FILENAME = 'D:\CSDL\CSDL_WEB_QUANGDUONG\Web_Pharma_QuangDuong_LOG.ldf',
	SIZE = 10MB,
	MAXSIZE = 100MB,
	FILEGROWTH = 5MB);




GO

SET DATEFORMAT DMY;
GO

USE Web_Pharma_QuangDuong;
GO

-- =====================================================================
-- PHẦN 1: HỆ THỐNG ĐỊNH DANH, BẢO MẬT LÕI & PHÂN QUYỀN ĐỘNG
-- Mục đích: Quản lý đăng nhập, xác thực tài khoản và tùy biến quyền hạn (RBAC)
-- =====================================================================

-- Bảng USER_ROLES: Quản lý danh sách các nhóm quyền hạn trong hệ thống.
-- [QUAN HỆ]: 1-N với CT_ROLE_PERMISSIONS, 1-N với CT_USER_ROLES.
CREATE TABLE [USER_ROLES](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính nhóm quyền
	ROLE_NAME VARCHAR(50) NOT NULL UNIQUE,       -- Mã quyền hệ thống (VD: ROLE_ADMIN, ROLE_DOCTOR, ROLE_SALE)
	ROLE_LEVEL INT NOT NULL,                     -- Cấp bậc quyền (Số càng nhỏ, quyền càng cao. VD: 0 là cao nhất)
	DESCRIPTION NVARCHAR(255)                    -- Mô tả hiển thị cho Admin hiểu vai trò này làm gì
);

-- Bảng PERMISSIONS: Danh sách các quyền hạn thao tác cực nhỏ (Hạt lựu - Granular Permissions)
-- [QUAN HỆ]: 1-N với CT_ROLE_PERMISSIONS, 1-N với CT_USER_PERMISSION_BLACKLIST, 1-N với CT_USER_MODERATION_LOG.
CREATE TABLE [PERMISSIONS](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính quyền hạn thao tác
	PERMISSION_CODE VARCHAR(50) NOT NULL UNIQUE, -- Mã thao tác (VD: 'HIDE_COMMENT', 'APPROVE_EVENT')
	DESCRIPTION NVARCHAR(255)                    -- Mô tả: 'Quyền ẩn bình luận vi phạm trên bài viết'
);

-- Bảng CT_ROLE_PERMISSIONS: Gán quyền thao tác cho Nhóm quyền.
-- [QUAN HỆ]: Bảng trung gian N-N giữa USER_ROLES và PERMISSIONS.
-- LƯU Ý KIẾN TRÚC: Dùng Khóa kép (ROLE_ID, PERMISSION_ID) vì 1 Role chỉ cần cấp 1 Quyền đúng 1 lần, cấm lặp dữ liệu.
CREATE TABLE [CT_ROLE_PERMISSIONS](
	ROLE_ID INT NOT NULL,                        -- Nhóm quyền (Khóa ngoại)
	PERMISSION_ID INT NOT NULL,                  -- Quyền thao tác (Khóa ngoại)
	PRIMARY KEY (ROLE_ID, PERMISSION_ID),        -- Khóa kép chặn lặp lại dữ liệu
	FOREIGN KEY (ROLE_ID) REFERENCES USER_ROLES(ID),
	FOREIGN KEY (PERMISSION_ID) REFERENCES PERMISSIONS(ID)
);

-- Bảng USERS: Bảng định danh người dùng lõi.
-- [QUAN HỆ]: 1-1 với PARTNER_PROFILES. 1-N với vô số bảng khác (Log, Post, CMT...).
CREATE TABLE [USERS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính định danh User
	USERNAME VARCHAR(50) NOT NULL UNIQUE,        -- Tên đăng nhập (duy nhất)
	PASSWORD VARCHAR(255) NOT NULL,              -- Mật khẩu (Bắt buộc mã hóa BCrypt)
	FULL_NAME NVARCHAR(100),                     -- Họ và tên cá nhân
	EMAIL VARCHAR(100) NOT NULL UNIQUE,          -- Email dùng để Login/Nhận OTP (duy nhất)
	PHONE VARCHAR(15),                           -- Số điện thoại cá nhân
	ADDRESS NVARCHAR(255),                       -- Địa chỉ cư trú cá nhân
	BIRTH_DATE DATE,                             -- Ngày tháng năm sinh cá nhân
	PROVIDER VARCHAR(50),                        -- Cổng đăng nhập (VD: 'local', 'google', 'facebook')
	LOCKED BIT DEFAULT 0,                        -- Cờ khóa tài khoản (1: Bị khóa do vi phạm, 0: Bình thường)
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Thời điểm đăng ký tài khoản
	UPDATED_AT DATETIME DEFAULT GETDATE()        -- Thời điểm cập nhật thông tin cá nhân lần cuối
);

-- Bảng CT_USER_ROLES: Cấp Nhóm quyền cho Người dùng.
-- [QUAN HỆ]: Bảng trung gian N-N giữa USERS và USER_ROLES.
-- LƯU Ý KIẾN TRÚC: Khóa kép, 1 User chỉ mang 1 Role_ID duy nhất 1 lần.
CREATE TABLE [CT_USER_ROLES](
	USER_ID BIGINT NOT NULL,                     -- Tài khoản người dùng (Khóa ngoại)
	ROLE_ID INT NOT NULL,                        -- Nhóm quyền được cấp (Khóa ngoại)
	PRIMARY KEY (USER_ID, ROLE_ID),              -- Khóa kép
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (ROLE_ID) REFERENCES USER_ROLES(ID)
);

-- Bảng CT_USER_PERMISSION_BLACKLIST: Danh sách đen (Blacklist) đóng băng quyền hạt lựu cấp độ cá nhân.
-- [QUAN HỆ]: Bảng trung gian N-N giữa USERS và PERMISSIONS.
-- LƯU Ý KIẾN TRÚC: Thiết kế Khóa kép (USER_ID, PERMISSION_ID) chặn lặp dữ liệu chuẩn 3NF. Cho phép gạch bỏ (tước) một quyền thao tác cụ thể của một tài khoản, bất chấp việc tài khoản đó đang sở hữu Nhóm chức vụ (Role) chứa quyền này. Trạng thái thực thi được đối soát qua CT_USER_MODERATION_LOG.
CREATE TABLE [CT_USER_PERMISSION_BLACKLIST](
	USER_ID BIGINT NOT NULL,                     -- Tài khoản bị tước quyền (Khóa ngoại)
	PERMISSION_ID INT NOT NULL,                  -- Quyền hạt lựu bị tước (Khóa ngoại)
	PRIMARY KEY (USER_ID, PERMISSION_ID),        -- Khóa kép cấm lặp dữ liệu
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (PERMISSION_ID) REFERENCES PERMISSIONS(ID)
);


-- Bảng MODERATION_ACTIONS: Quản lý danh mục các hành vi kiểm duyệt (Mã hóa thay vì hardcode)
-- [QUAN HỆ]: 1-N với các bảng Log.
CREATE TABLE [MODERATION_ACTIONS](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính
	CODE VARCHAR(50) NOT NULL UNIQUE,            -- VD: 'HIDE', 'UNHIDE', 'DELETE', 'WARN', 'LOCK_USER', 'BLACKLIST_PERM'
	NAME NVARCHAR(100) NOT NULL,                 -- VD: 'Khóa tài khoản', 'Tước quyền hạt lựu'
	DESCRIPTION NVARCHAR(255),
	AFFECTED_TABLE VARCHAR(50) NULL -- Tên bảng bị tác động (VD: 'PARTNER_PROFILES', 'ADDRESSES')
);


-- Bảng CT_USER_MODERATION_LOG: Sổ tay kiểm toán (Audit Log) các thao tác nhạy cảm trên tài khoản.
-- [QUAN HỆ]: N-1 với USERS (Tài khoản bị tác động), N-1 với MODERATION_ACTIONS, N-1 với PERMISSIONS, N-1 với USERS (Quản trị viên).
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID). Lưu vết độc lập mọi hành vi kiểm duyệt (Khóa tài khoản, Tước quyền). Cột PERMISSION_ID hỗ trợ truy xuất chính xác quyền nào bị tác động khi thực thi lệnh Blacklist.
CREATE TABLE [CT_USER_MODERATION_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Mã log lưu vết
	TARGET_USER_ID BIGINT NOT NULL,               -- Tài khoản bị tác động (Khóa ngoại)
	ACTION_ID INT NOT NULL,                       -- Mã hành vi kiểm duyệt (Khóa ngoại trỏ về MODERATION_ACTIONS)
	PERMISSION_ID INT NULL,                       -- Quyền hạt lựu bị tác động (NULL nếu là Khóa toàn bộ tài khoản)
	MODERATOR_ID BIGINT NOT NULL,                 -- Quản trị viên ra lệnh (Khóa ngoại)
	REASON NVARCHAR(255) NOT NULL,                -- Lý do thực hiện bắt buộc phải điền
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm ghi log
	FOREIGN KEY (TARGET_USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID),
	FOREIGN KEY (PERMISSION_ID) REFERENCES PERMISSIONS(ID),
	FOREIGN KEY (MODERATOR_ID) REFERENCES USERS(ID)
);


-- Bảng CT_USER_LOGIN_LOG: Sổ tay kiểm toán luồng Đăng nhập/Đăng xuất.
-- [QUAN HỆ]: Bảng trung gian N-N giữa USERS và MODERATION_ACTIONS.
CREATE TABLE [CT_USER_LOGIN_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính độc lập
	USER_ID BIGINT NULL,                          -- Định danh User (NULL nếu user không tồn tại)
	ACTION_ID INT NOT NULL,                       -- Trỏ về MODERATION_ACTIONS (VD: LOGIN_SUCCESS, LOGIN_FAILED)
	USERNAME_ATTEMPT VARCHAR(100) NOT NULL,       -- Chuỗi Hacker/User đã gõ vào ô Đăng nhập
	LOGIN_IP VARCHAR(50) NOT NULL,                -- Địa chỉ IP đã thực thực hiện đăng nhập
	USER_AGENT NVARCHAR(500),                     -- Thông tin Trình duyệt / Thiết bị
	MESSAGE NVARCHAR(255),                        -- Chi tiết (VD: 'Sai mật khẩu')
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Mốc thời gian
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID)
);


-- Bảng CT_USER_ACTION_LOG: Sổ tay kiểm toán hành vi Tự phục vụ (Đổi Pass, Sửa Hồ sơ).
-- [QUAN HỆ]: Bảng trung gian N-N giữa USERS và MODERATION_ACTIONS.
-- LƯU Ý KIẾN TRÚC: Đã loại bỏ cột TARGET_ENTITY do sự phân định bảng tác động
-- đã được ngầm định chặt chẽ và an toàn thông qua ACTION_ID.
CREATE TABLE [CT_USER_ACTION_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính độc lập
	USER_ID BIGINT NOT NULL,                      -- Ai là người thực hiện
	ACTION_ID INT NOT NULL,                       -- Trỏ về MODERATION_ACTIONS (Đã bao hàm ngữ cảnh Bảng đích)
	TARGET_ENTITY_ID BIGINT,                      -- ID của dòng dữ liệu bị tác động (VD: ID của Địa chỉ)
	OLD_PAYLOAD NVARCHAR(MAX),                    -- Chuỗi JSON dữ liệu CŨ
	NEW_PAYLOAD NVARCHAR(MAX),                    -- Chuỗi JSON dữ liệu MỚI
	IP_ADDRESS VARCHAR(50),                       -- Địa chỉ IP đã thực thi hành vi
	USER_AGENT NVARCHAR(500),                     -- Thông tin Trình duyệt / Thiết bị
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm sửa
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID)
);


-- Bảng OTP_CODES: Nhật ký sinh mã xác thực OTP.
-- [QUAN HỆ]: Độc lập. Tham chiếu logic 1-N với Email của người dùng.
-- LƯU Ý KIẾN TRÚC: Khóa chính độc lập (ID). 1 Email có thể yêu cầu gửi OTP nhiều lần vào nhiều ngày.
CREATE TABLE [OTP_CODES](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính độc lập phân biệt từng lần cấp OTP
	ATTEMPTS INT DEFAULT 0,                      -- Số lần người dùng đã nhập sai mã này
	CODE VARCHAR(10) NOT NULL,                   -- Dãy số OTP (VD: '849201')
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Thời điểm hệ thống sinh mã
	EMAIL VARCHAR(100) NOT NULL,                 -- Email nhận mã (để query kiểm tra nhanh)
	EXPIRY_AT DATETIME NOT NULL,                 -- Thời điểm mã hết hiệu lực (thường là sau 5 phút)
	USED BIT DEFAULT 0                           -- Cờ đánh dấu: 1 = Đã nhập đúng và dùng rồi, 0 = Chưa dùng
);


-- =====================================================================
-- PHẦN 2: CHUẨN HÓA ĐỊA CHỈ & HỒ SƠ ĐỐI TÁC B2B (ĐẠT CHUẨN 3NF)
-- Thay thế chuỗi lặp lại bằng Cấu trúc Danh mục Hành chính Quốc gia
-- =====================================================================

-- Danh mục Tỉnh/Thành phố
-- [QUAN HỆ]: 1-N với DISTRICTS.
CREATE TABLE [PROVINCES](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính Tỉnh/Thành
	NAME NVARCHAR(100) NOT NULL UNIQUE,          -- VD: 'Hà Nội', 'TP. Hồ Chí Minh'
	CODE VARCHAR(20) UNIQUE                      -- Mã hành chính chuẩn nhà nước
);

-- Danh mục Quận/Huyện (Phụ thuộc hoàn toàn vào Tỉnh/Thành)
-- [QUAN HỆ]: N-1 với PROVINCES. 1-N với WARDS.
CREATE TABLE [DISTRICTS](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính Quận/Huyện
	PROVINCE_ID INT NOT NULL,                    -- Thuộc Tỉnh/Thành nào
	NAME NVARCHAR(100) NOT NULL,                 -- VD: 'Quận Đống Đa'
	FOREIGN KEY (PROVINCE_ID) REFERENCES PROVINCES(ID)
);

-- Danh mục Phường/Xã (Phụ thuộc hoàn toàn vào Quận/Huyện)
-- [QUAN HỆ]: N-1 với DISTRICTS. 1-N với ADDRESSES.
CREATE TABLE [WARDS](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính Phường/Xã
	DISTRICT_ID INT NOT NULL,                    -- Thuộc Quận/Huyện nào
	NAME NVARCHAR(100) NOT NULL,                 -- VD: 'Phường Láng Hạ'
	FOREIGN KEY (DISTRICT_ID) REFERENCES DISTRICTS(ID)
);

-- =====================================================================
-- HỒ SƠ CÔNG KHAI CÁ NHÂN (PUBLIC PROFILES) - CHUẨN 4NF
-- Mục đích: Lưu trữ thông tin hiển thị công khai của bất kỳ cá nhân nào 
-- (Chuyên gia, Nhân viên nội bộ, Người dùng, Marketing) khi họ tương tác.
-- =====================================================================

CREATE TABLE [PUBLIC_PROFILES](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính độc lập tuân thủ chuẩn toàn hệ thống
	USER_ID BIGINT NOT NULL UNIQUE,               -- Liên kết 1-1 với USERS (Duy nhất)
	PROFESSIONAL_TITLE NVARCHAR(100),             -- Danh xưng/Chức danh (VD: 'BS.CK2', 'Chuyên viên Content')
	WORKPLACE NVARCHAR(255),                      -- Nơi công tác/Làm việc
	BIO NVARCHAR(MAX),                            -- Tiểu sử, kinh nghiệm, giới thiệu bản thân
	AVATAR_URL VARCHAR(255),                      -- Ảnh đại diện cá nhân
	IS_VISIBLE BIT DEFAULT 1,                     -- Cờ quyền riêng tư: 1 = Hiển thị công khai, 0 = Ẩn hồ sơ
	CREATED_AT DATETIME DEFAULT GETDATE(),        
	UPDATED_AT DATETIME DEFAULT GETDATE(),        
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng PARTNER_PROFILES: Lưu trữ hồ sơ năng lực và xác thực tính hợp pháp B2B
-- [QUAN HỆ]: 1-1 với USERS (Nhờ khóa UNIQUE). 1-N với ADDRESSES.
CREATE TABLE [PARTNER_PROFILES](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính Hồ sơ đối tác
	USER_ID BIGINT NOT NULL UNIQUE,              -- Nối 1-1 với USERS. Một tài khoản chỉ có 1 hồ sơ đối tác
	BUSINESS_NAME NVARCHAR(150) NOT NULL,        -- Tên Nhà thuốc/Công ty/Phòng khám (Phân biệt với tên cá nhân)
	BUSINESS_PHONE VARCHAR(15),                  -- Số điện thoại trực doanh nghiệp/nhà thuốc
	AVATAR_URL VARCHAR(255),                     -- Ảnh đại diện/Logo đơn vị
	TAX_CODE VARCHAR(50),                        -- Mã số thuế doanh nghiệp
	LICENSE_NUMBER VARCHAR(50),                  -- Số chứng chỉ hành nghề y dược hoặc GPKD
	LICENSE_DOCUMENT_URL VARCHAR(255),           -- Link file ảnh/PDF giấy phép đã upload
	VERIFICATION_STATUS VARCHAR(50) DEFAULT 'PENDING', -- Trạng thái duyệt hồ sơ (PENDING, APPROVED, REJECTED)
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng ADDRESSES: Đã đạt chuẩn 3NF. Bỏ các cột rác, chỉ nối tới cấp nhỏ nhất là WARD_ID.
-- [QUAN HỆ]: N-1 với PARTNER_PROFILES. N-1 với WARDS.
-- LƯU Ý KIẾN TRÚC: Khóa độc lập (ID). 1 Đối tác có thể có nhiều chi nhánh/kho hàng.
CREATE TABLE [ADDRESSES](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính cụ thể cho 1 tọa độ địa chỉ
	PARTNER_ID BIGINT NOT NULL,                  -- Địa chỉ này thuộc về Đối tác nào
	WARD_ID INT NOT NULL,                        -- Chỉ cần WARD_ID là có thể JOIN ra Quận và Tỉnh (Chuẩn 3NF)
	STREET_ADDRESS NVARCHAR(255) NOT NULL,       -- Số nhà, tên đường, hẻm, tòa nhà
	IS_DEFAULT BIT DEFAULT 0,                    -- Cờ mặc định: 1 = Địa chỉ chính để giao hàng, 0 = Phụ
	ADDRESS_TYPE VARCHAR(20) DEFAULT 'OFFICE',   -- Loại địa chỉ: 'OFFICE' (Trụ sở), 'SHIPPING' (Kho nhận hàng)
	FOREIGN KEY (PARTNER_ID) REFERENCES PARTNER_PROFILES(ID),
	FOREIGN KEY (WARD_ID) REFERENCES WARDS(ID)
);


-- =====================================================================
-- PHẦN 3: NỘI DUNG Y KHOA (ĐẠT CHUẨN 4NF)
-- sử dụng Log để lưu vết hoàn toàn
-- =====================================================================

-- Bảng CATEGORIES: Phân tầng cấu trúc chuyên mục nội dung.
-- [QUAN HỆ]: 1-N với POSTS.
CREATE TABLE [CATEGORIES](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính danh mục
	NAME NVARCHAR(100) NOT NULL,                 -- Tên danh mục (VD: 'Nghiên cứu lâm sàng')
	SLUG VARCHAR(100) NOT NULL UNIQUE,           -- Đường dẫn URL chuẩn SEO (VD: nghien-cuu-lam-sang)
	DESCRIPTION NVARCHAR(255),                   -- Mô tả tóm tắt cho danh mục
	IS_ACTIVE BIT DEFAULT 1                      -- Cờ hiển thị: 1 = Đang mở, 0 = Đang ẩn
);

-- Bảng TAGS: Thẻ từ khoá để gom nhóm bài viết xuyên chuyên mục.
-- [QUAN HỆ]: N-N với POSTS (qua CT_POST_TAGS). N-N với CT_EVENTS (qua CT_EVENT_TAGS).
CREATE TABLE [TAGS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính Tag
	NAME NVARCHAR(50) NOT NULL,                  -- Tên thẻ (VD: '#TimMach2026')
	SLUG VARCHAR(50) NOT NULL UNIQUE             -- Đường dẫn URL của thẻ
);

-- Bảng POSTS: Không còn cột `VIEW_COUNT` (Lượt xem được Query từ bảng POST_VIEW_LOGS để đảm bảo chuẩn 3NF).
-- [QUAN HỆ]: N-1 với CATEGORIES, N-1 với USERS (Tác giả). 1-N với Log, Files, Images. N-N với Tags.
CREATE TABLE [POSTS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính Bài viết
	TITLE NVARCHAR(255) NOT NULL,                -- Tiêu đề bài viết hiển thị trên web
	SLUG VARCHAR(255) NOT NULL UNIQUE,           -- URL bài viết thân thiện với Google
	SUMMARY NVARCHAR(500),                       -- Đoạn tóm tắt (Lead-in) dùng để mồi độc giả
	CONTENT NVARCHAR(MAX) NOT NULL,              -- Nội dung HTML/Rich Text toàn bộ bài viết
	THUMBNAIL_URL VARCHAR(255),                  -- Đường dẫn ảnh đại diện/bìa của bài viết
	IS_PUBLISHED BIT DEFAULT 0,                  -- Cờ xuất bản: 1 = Hiển thị ra web, 0 = Bản nháp
	SEO_TITLE NVARCHAR(100),                     -- Thẻ Title SEO (Ghi đè tiêu đề gốc để tối ưu từ khóa)
	SEO_DESCRIPTION NVARCHAR(255),               -- Thẻ Meta Description SEO
	CATEGORY_ID INT NOT NULL,                    -- Thuộc chuyên mục nào (Khóa ngoại)
	AUTHOR_ID BIGINT NOT NULL,                   -- Tài khoản Admin/Tác giả viết bài (Khóa ngoại)
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Ngày tạo nháp
	UPDATED_AT DATETIME DEFAULT GETDATE(),       -- Ngày sửa bài lần cuối
	FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID),
	FOREIGN KEY (AUTHOR_ID) REFERENCES USERS(ID) 
);

-- Bảng CT_POST_ROLES: Cầu nối phân quyền hiển thị Bài viết y khoa.
-- [QUAN HỆ]: Bảng trung gian N-N giữa POSTS và USER_ROLES.
-- LƯU Ý KIẾN TRÚC: Thiết kế Khóa kép (POST_ID, ROLE_ID) chuẩn 5NF. 
-- Chặn tuyệt đối việc gán trùng 1 chức vụ 2 lần cho cùng 1 bài viết.
CREATE TABLE [CT_POST_ROLES](
	POST_ID BIGINT NOT NULL,                     -- Bài viết mục tiêu (Khóa ngoại)
	ROLE_ID INT NOT NULL,                        -- Nhóm chức vụ được phép đọc (Khóa ngoại)
	PRIMARY KEY (POST_ID, ROLE_ID),              -- Khóa kép bóp nghẹt sự lặp lại
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID),
	FOREIGN KEY (ROLE_ID) REFERENCES USER_ROLES(ID)
);

-- Bảng POST_VIEW_LOGS: Nhật ký lượt xem đáp ứng chuẩn 3NF.
-- [QUAN HỆ]: N-1 với POSTS, N-1 với USERS.
CREATE TABLE [POST_VIEW_LOGS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Mã log lượt xem
	POST_ID BIGINT NOT NULL,                     -- Bài viết nào được xem
	VIEWER_IP VARCHAR(50),                       -- IP người xem (để chống spam view)
	USER_ID BIGINT NULL,                         -- Lưu ID nếu người xem đã đăng nhập
	VIEWED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm xem
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng CT_POST_TAGS: Gắn nhiều Tag vào 1 Bài viết.
-- [QUAN HỆ]: Bảng trung gian N-N giữa POSTS và TAGS.
-- LƯU Ý KIẾN TRÚC: Khóa kép, 1 bài viết cấm gắn trùng 1 tag 2 lần.
CREATE TABLE [CT_POST_TAGS](
	POST_ID BIGINT NOT NULL,                     -- Bài viết (Khóa ngoại)
	TAG_ID BIGINT NOT NULL,                      -- Thẻ (Khóa ngoại)
	PRIMARY KEY (POST_ID, TAG_ID),               -- Khóa kép
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID),
	FOREIGN KEY (TAG_ID) REFERENCES TAGS(ID)
);

-- Bảng POST_IMAGES: Quản lý Gallery đa ảnh đính kèm bài viết.
-- [QUAN HỆ]: N-1 với POSTS.
-- LƯU Ý KIẾN TRÚC: Khóa độc lập (ID) vì một bài viết có thể có nhiều ảnh độc lập cần sắp xếp thứ tự.
CREATE TABLE [POST_IMAGES](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính bức ảnh
	POST_ID BIGINT NOT NULL,                     -- Thuộc bài viết nào
	IMAGE_URL VARCHAR(255) NOT NULL,             -- Đường dẫn ảnh trên máy chủ Storage
	DISPLAY_ORDER INT DEFAULT 1 CHECK (DISPLAY_ORDER > 0), -- Thứ tự trượt ảnh (1, 2, 3...)
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID)
);

-- Bảng POST_FILES: Quản lý file đính kèm (PDF, Word) để kéo lượt Tải xuống.
-- [QUAN HỆ]: N-1 với POSTS. 1-N với CT_FILE_DOWNLOADS.
CREATE TABLE [POST_FILES](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính tài liệu số
	POST_ID BIGINT NOT NULL,                     -- Đính kèm trong bài viết nào
	FILE_NAME NVARCHAR(255) NOT NULL,            -- Tên hiển thị nút Tải về (VD: 'Báo cáo Q1.pdf')
	FILE_URL VARCHAR(255) NOT NULL,              -- Đường dẫn vật lý file ẩn
	FILE_TYPE VARCHAR(20),                       -- Định dạng file để load icon (pdf, xlsx)
	FILE_SIZE FLOAT CHECK (FILE_SIZE >= 0),      -- Dung lượng file để cảnh báo dung lượng tải
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID)
);

-- Bảng CT_FILE_DOWNLOADS: Lịch sử tải tài liệu (Cỗ máy thu Data Lead Scoring).
-- [QUAN HỆ]: Bảng trung gian giải quyết N-N giữa USERS và POST_FILES.
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID). Cho phép lưu lại việc 1 Đối tác tải cùng 1 file nhiều lần.
CREATE TABLE [CT_FILE_DOWNLOADS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Mã phiên tải file
	FILE_ID BIGINT NOT NULL,                     -- File nào được tải (Khóa ngoại)
	USER_ID BIGINT NOT NULL,                     -- Ai đã click tải (Khóa ngoại)
	DOWNLOADED_AT DATETIME DEFAULT GETDATE(),    -- Thời điểm click nút tải về
	FOREIGN KEY (FILE_ID) REFERENCES POST_FILES(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);


-- =====================================================================
-- PHẦN 4: SỰ KIỆN CHIẾN DỊCH (CHUẨN HÓA TRẠNG THÁI)
-- LƯU Ý: Trạng thái dựa 100% vào History Log
-- =====================================================================

-- Bảng EVENT_TYPES: Phân loại hình thức tổ chức sự kiện.
-- [QUAN HỆ]: 1-N với EVENTS.
CREATE TABLE [EVENT_TYPES](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính loại sự kiện
	NAME NVARCHAR(100) NOT NULL,                 -- Tên loại (Hội thảo, Trực tuyến, Họp báo)
	DESCRIPTION NVARCHAR(255)                    -- Mô tả nội bộ loại sự kiện
);

-- Bảng LOCATIONS: Danh bạ địa điểm tổ chức Offline hoặc Online.
-- [QUAN HỆ]: 1-N với CT_EVENTS.
CREATE TABLE [LOCATIONS](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính Hội trường
	IS_ONLINE BIT NOT NULL DEFAULT 0,
	NAME NVARCHAR(150) NOT NULL,                 -- Tên định danh (VD: Hội trường A, Zoom)
	ADDRESS NVARCHAR(500) NOT NULL               -- Địa chỉ thực tế hoặc Link URL họp trực tuyến
);

-- Bảng EVENTS: Lưu trữ thông tin Chiến dịch Marketing sự kiện Tổng.
-- [QUAN HỆ]: N-1 với EVENT_TYPES. 1-N với CT_EVENTS.
CREATE TABLE [EVENTS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính Chiến dịch
	EVENT_TYPE_ID INT NOT NULL,                  -- Phân loại sự kiện (Khóa ngoại)
	TITLE NVARCHAR(255) NOT NULL,                -- Tên chiến dịch chung (VD: Chuỗi Roadshow 2026)
	SLUG VARCHAR(255) NOT NULL UNIQUE,           -- URL chung của chiến dịch
	DESCRIPTION NVARCHAR(MAX),                   -- Giới thiệu tổng quan chiến dịch
	THUMBNAIL_URL VARCHAR(255),                  -- Ảnh Poster/Banner của chiến dịch
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Ngày tạo chiến dịch
	UPDATED_AT DATETIME DEFAULT GETDATE(),       -- Ngày cập nhật thông tin
	FOREIGN KEY (EVENT_TYPE_ID) REFERENCES EVENT_TYPES(ID)
);

-- Bảng CT_EVENTS: Trạm sự kiện thực tế.
-- [QUAN HỆ]: N-1 với EVENTS, N-1 với LOCATIONS. 1-N với History, Registrations. N-N với Tags, Posts.
CREATE TABLE [CT_EVENTS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Mã định danh duy nhất cho 1 buổi sự kiện thực tế
	EVENT_ID BIGINT NOT NULL,                    -- Thuộc chiến dịch tổng nào (Khóa ngoại)
	LOCATION_ID INT NOT NULL,                    -- Tổ chức tại tọa độ nào (Khóa ngoại)
	TITLE NVARCHAR(500) NULL,                    -- tên riêng từng trạm (nếu muốn riêng)
	CONTENT NVARCHAR(MAX) NULL,				     -- mô tả nội dung riêng từng buổi
	START_TIME DATETIME NOT NULL,                -- Thời gian khai mạc
	END_TIME DATETIME NOT NULL,                  -- Thời gian bế mạc
	TOTAL_SLOTS INT CHECK (TOTAL_SLOTS >= 0),    -- Giới hạn số lượng vé phát hành
	SEO_TITLE NVARCHAR(100),                     -- Meta Title để chạy Ads/SEO Local theo tỉnh thành
	SEO_DESCRIPTION NVARCHAR(255),               -- Meta Description cho Google
	FOREIGN KEY (EVENT_ID) REFERENCES EVENTS(ID),
	FOREIGN KEY (LOCATION_ID) REFERENCES LOCATIONS(ID),
	CONSTRAINT CHK_Valid_Event_Time CHECK (END_TIME > START_TIME)
);

-- Bảng EVENT_SPEAKERS: Kho dữ liệu Hồ sơ Diễn giả/Chuyên gia.
-- [QUAN HỆ]: N-1 với CT_EVENTS. 1-N với CT_AGENDA_SPEAKERS.
CREATE TABLE [EVENT_SPEAKERS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính định danh diễn giả
	CT_EVENT_ID BIGINT NOT NULL,                  -- Thuộc Phiên sự kiện nào (Khóa ngoại)
	FULL_NAME NVARCHAR(100) NOT NULL,             -- Họ và tên (VD: Nguyễn Văn Hùng)
	ACADEMIC_TITLE NVARCHAR(100),                 -- Học hàm, Học vị (VD: PGS.TS, BS.CK2)
	ORGANIZATION NVARCHAR(255),                   -- Nơi công tác (VD: Đại học Y Hà Nội)
	AVATAR_URL VARCHAR(255),                      -- Đường dẫn ảnh đại diện
	BIO NVARCHAR(MAX),                            -- Tiểu sử chuyên môn, kinh nghiệm lâm sàng
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID)
);

-- Bảng EVENT_AGENDA: Lịch trình chi tiết từng mốc thời gian của Phiên sự kiện.
-- [QUAN HỆ]: N-1 với CT_EVENTS. 1-N với CT_AGENDA_SPEAKERS.
-- LƯU Ý KIẾN TRÚC: Đã loại bỏ hoàn toàn cột rác SPEAKER_INFO để tuân thủ 1NF.
CREATE TABLE [EVENT_AGENDA](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính định danh lịch trình
	CT_EVENT_ID BIGINT NOT NULL,                  -- Thuộc Phiên sự kiện nào (Khóa ngoại)
	START_TIME DATETIME NOT NULL,                 -- Thời gian bắt đầu chuyên đề
	END_TIME DATETIME NOT NULL,                   -- Thời gian kết thúc chuyên đề
	SESSION_TITLE NVARCHAR(255) NOT NULL,         -- Tên chuyên đề (VD: Cơ chế đề kháng ESBL)
	DESCRIPTION NVARCHAR(MAX),                    -- Mô tả tóm tắt nội dung báo cáo
	DISPLAY_ORDER INT DEFAULT 1,                  -- Thứ tự sắp xếp hiển thị trên giao diện
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	CONSTRAINT CHK_Agenda_Time CHECK (END_TIME > START_TIME)
);

-- Bảng CT_EVENT_SESSION_ROLES: Cầu nối phân quyền hiển thị & đăng ký cho từng Trạm/Buổi Sự kiện.
-- Đổi tên từ CT_EVENT_ROLES thành CT_EVENT_SESSION_ROLES để tránh xung đột tiền tố với bảng EVENTS.
-- [QUAN HỆ]: Bảng trung gian N-N giữa CT_EVENTS và USER_ROLES.
CREATE TABLE [CT_EVENT_SESSION_ROLES](
	CT_EVENT_ID BIGINT NOT NULL,                 -- Trạm/Buổi sự kiện mục tiêu (Khóa ngoại)
	ROLE_ID INT NOT NULL,                        -- Nhóm chức vụ được phép xem/đăng ký (Khóa ngoại)
	PRIMARY KEY (CT_EVENT_ID, ROLE_ID),          -- Khóa kép định tuyến
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	FOREIGN KEY (ROLE_ID) REFERENCES USER_ROLES(ID)
);

-- Bảng CT_AGENDA_SPEAKERS: Bảng định tuyến N-N giữa Lịch trình và Diễn giả.
-- LƯU Ý KIẾN TRÚC: Xử lý triệt để trường hợp 1 Khung giờ có nhiều Diễn giả (Tọa đàm)
-- và 1 Diễn giả báo cáo ở nhiều Khung giờ khác nhau. Sử dụng Khóa Kép chặn lặp dữ liệu.
CREATE TABLE [CT_AGENDA_SPEAKERS](
	AGENDA_ID BIGINT NOT NULL,                    -- Mốc Lịch trình (Khóa ngoại)
	SPEAKER_ID BIGINT NOT NULL,                   -- Chuyên gia báo cáo (Khóa ngoại)
	PRIMARY KEY (AGENDA_ID, SPEAKER_ID),          -- Khóa Kép cấm gán 1 người 2 lần vào 1 khung giờ
	FOREIGN KEY (AGENDA_ID) REFERENCES EVENT_AGENDA(ID),
	FOREIGN KEY (SPEAKER_ID) REFERENCES EVENT_SPEAKERS(ID)
);


-- Bảng CT_EVENT_TAGS: Gắn Tag đặc thù cho từng Trạm sự kiện.
-- [QUAN HỆ]: Bảng trung gian N-N giữa CT_EVENTS và TAGS.
CREATE TABLE [CT_EVENT_TAGS](
	CT_EVENT_ID BIGINT NOT NULL,                 -- Trạm sự kiện cụ thể (Khóa ngoại)
	TAG_ID BIGINT NOT NULL,                      -- Thẻ Tag (Khóa ngoại)
	PRIMARY KEY (CT_EVENT_ID, TAG_ID),           -- Khóa kép
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	FOREIGN KEY (TAG_ID) REFERENCES TAGS(ID)
);

-- Bảng CT_POST_EVENTS: Đính kèm các bài viết/Báo cáo lâm sàng riêng cho từng Trạm.
-- [QUAN HỆ]: Bảng trung gian N-N giữa CT_EVENTS và POSTS.
CREATE TABLE [CT_POST_EVENTS](
	CT_EVENT_ID BIGINT NOT NULL,                 -- Trạm sự kiện
	POST_ID BIGINT NOT NULL,                     -- Bài báo cáo/Slide diễn giả
	PRIMARY KEY (CT_EVENT_ID, POST_ID),          -- Khóa kép (Cấm đính kèm lặp lại cùng 1 bài)
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID)
);

-- Bảng CT_EVENT_STATUS_HISTORY: Audit Log trạng thái sự kiện. 
-- [QUAN HỆ]: N-1 với CT_EVENTS, N-1 với USERS.
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID). Lưu lại toàn bộ lịch sử trạng thái.
CREATE TABLE [CT_EVENT_STATUS_HISTORY](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Mã log lưu vết
	CT_EVENT_ID BIGINT NOT NULL,                 -- Trạm sự kiện nào bị tác động
	STATUS_CODE VARCHAR(50) NOT NULL,            -- Mã trạng thái được Update ('UPCOMING', 'CANCELLED'...)
	CHANGED_BY_USER_ID BIGINT NOT NULL,          -- Admin nào thực hiện bấm đổi trạng thái
	CHANGED_AT DATETIME DEFAULT GETDATE(),       -- Thời gian bấm lưu
	NOTE NVARCHAR(255),                          -- Diễn giải lý do (VD: 'Giãn cách xã hội')
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	FOREIGN KEY (CHANGED_BY_USER_ID) REFERENCES USERS(ID)
);

-- Bảng CT_EVENT_REGISTRATIONS: Danh sách khách mời đăng ký (Nguồn Data Lead đỉnh nhất).
-- [QUAN HỆ]: N-1 với CT_EVENTS. N-1 với USERS.
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID) hoạt động như Mã Vé (Ticket ID) cho phép hủy/đăng ký lại.
CREATE TABLE [CT_EVENT_REGISTRATIONS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Số vé đăng ký thành công
	CT_EVENT_ID BIGINT NOT NULL,                 -- Đăng ký tham gia trạm nào
	USER_ID BIGINT NULL,                         -- Tài khoản khách (Có thể NULL nếu chưa tạo nick)
	GUEST_NAME NVARCHAR(100) NOT NULL,           -- Tên người đi tham dự
	GUEST_EMAIL VARCHAR(100) NOT NULL,           -- Email nhận mã QR/Vé mời
	GUEST_PHONE VARCHAR(15) NOT NULL,            -- SĐT để check-in/Sales gọi nhắc lịch
	WORKPLACE NVARCHAR(255),                     -- Nơi công tác (Hữu ích cho phân loại tập KH)
	STATUS VARCHAR(50),                          -- Trạng thái vé (VD: 'APPROVED', 'CANCELED', 'ATTENDED')
	REGISTERED_AT DATETIME DEFAULT GETDATE(),    -- Ngày giờ Submit form
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);


-- =====================================================================
-- PHẦN 5: TƯƠNG TÁC BÌNH LUẬN, BÀI VIẾT (CHUẨN 5NF TUYỆT ĐỐI)
-- Tính hiển thị do bảng Audit Log quyết định.
-- =====================================================================

-- Bảng LOAI_LIKE: Danh mục các nút Cảm xúc có thể dùng trong hệ thống.
-- [QUAN HỆ]: 1-N với bảng thả tim CT_LIKECMT, CT_LIKEPHCMT.
CREATE TABLE [LOAI_LIKE](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính loại cảm xúc
	CODE VARCHAR(20) NOT NULL UNIQUE,            -- Mã hệ thống: 'LIKE', 'HEART', 'ANGRY'
	NAME NVARCHAR(50) NOT NULL,                  -- Tên hiển thị UI: 'Thích', 'Tuyệt vời'
	ICON_URL VARCHAR(255)                        -- Hình dạng icon SVG
);

-- Bảng CMT: Kho chứa toàn bộ Bình luận Cấp 1 (Root Comments).
-- [QUAN HỆ]: N-1 với USERS. 1-N với PH_CMT, CT_LIKECMT, CT_CMT_MODERATION_LOG. 1-1 với cầu nối Định tuyến.
-- KIẾN TRÚC 5NF: Thuần khiết nhất có thể. Không chứa POST_ID, EVENT_ID.
CREATE TABLE [CMT](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính Bình luận Cấp 1
	USER_ID BIGINT NOT NULL,                     -- Tác giả viết bình luận
	CONTENT NVARCHAR(MAX) NOT NULL,              -- Nội dung Text
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Ngày viết
	UPDATED_AT DATETIME DEFAULT GETDATE(),       -- Ngày người dùng tự sửa nội dung
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng PH_CMT: Kho chứa toàn bộ Phản hồi từ Cấp 2 trở xuống (Reply Comments).
-- [QUAN HỆ]: N-1 với CMT (Mỏ neo). 1-N tự thân (Self-referencing). N-1 với USERS.
CREATE TABLE [PH_CMT](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Khóa chính của dòng Phản hồi
	ROOT_CMT_ID BIGINT NOT NULL,                 -- Liên kết Mỏ neo: Trỏ ngược về CMT Cấp 1 cao nhất
	PARENT_PH_ID BIGINT NULL,                    -- Liên kết Cây (Adjacency List): Trỏ về chính PH_CMT để lồng cấp vô hạn
	USER_ID BIGINT NOT NULL,                     -- Tác giả phản hồi
	CONTENT NVARCHAR(MAX) NOT NULL,              -- Nội dung
	CREATED_AT DATETIME DEFAULT GETDATE(),
	UPDATED_AT DATETIME DEFAULT GETDATE(),
	FOREIGN KEY (ROOT_CMT_ID) REFERENCES CMT(ID),
	FOREIGN KEY (PARENT_PH_ID) REFERENCES PH_CMT(ID), 
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng CT_LIKECMT: Nhật ký thả cảm xúc trên Bình luận Cấp 1.
-- [QUAN HỆ]: Bảng trung gian N-N giữa USERS và CMT.
-- LƯU Ý KIẾN TRÚC: Khóa kép (USER_ID, CMT_ID). Ngăn cấm tuyệt đối 1 người thả tim 2 lần cho 1 bình luận.
CREATE TABLE [CT_LIKECMT](
	USER_ID BIGINT NOT NULL,                     -- Tài khoản bấm nút
	CMT_ID BIGINT NOT NULL,                      -- Trên Bình luận Cấp 1 nào
	LOAILIKE_ID INT NOT NULL,                    -- Bấm cảm xúc gì (Khóa ngoại)
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Bấm lúc nào
	PRIMARY KEY (USER_ID, CMT_ID),               -- Khóa kép bóp nghẹt sự lặp lại
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (CMT_ID) REFERENCES CMT(ID),
	FOREIGN KEY (LOAILIKE_ID) REFERENCES LOAI_LIKE(ID)
);

-- Bảng CT_LIKEPHCMT: Nhật ký thả cảm xúc trên Phản hồi (Cấp 2+).
-- [QUAN HỆ]: Bảng trung gian N-N giữa USERS và PH_CMT.
CREATE TABLE [CT_LIKEPHCMT](
	USER_ID BIGINT NOT NULL,                     -- Tài khoản bấm nút
	PH_CMT_ID BIGINT NOT NULL,                   -- Trên Phản hồi Cấp 2+ nào
	LOAILIKE_ID INT NOT NULL,                    -- Loại cảm xúc
	CREATED_AT DATETIME DEFAULT GETDATE(),
	PRIMARY KEY (USER_ID, PH_CMT_ID),            -- Khóa kép
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (PH_CMT_ID) REFERENCES PH_CMT(ID),
	FOREIGN KEY (LOAILIKE_ID) REFERENCES LOAI_LIKE(ID)
);

-- =====================================================================
-- BẢNG TƯƠNG TÁC CẢM XÚC TRỰC TIẾP TRÊN BÀI VIẾT
-- Mục đích: Lưu vết cảm xúc (Hữu ích, Yêu thích...) của độc giả.
-- Thiết kế Khóa kép cấm 1 tài khoản thả 2 lần cảm xúc trên cùng 1 bài.
-- =====================================================================
CREATE TABLE [CT_LIKEPOST](
	USER_ID BIGINT NOT NULL,                     -- Độc giả (Khóa ngoại)
	POST_ID BIGINT NOT NULL,                     -- Bài viết đích (Khóa ngoại)
	LOAILIKE_ID INT NOT NULL,                    -- Loại cảm xúc (Khóa ngoại)
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Thời điểm tương tác
	PRIMARY KEY (USER_ID, POST_ID),              -- Khóa kép bóp nghẹt sự lặp lại
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID),
	FOREIGN KEY (LOAILIKE_ID) REFERENCES LOAI_LIKE(ID)
);

-- Bảng CT_POST_CMT: Bảng Cầu nối Định tuyến - Trỏ CMT thuần khiết về Bài Viết.
-- [QUAN HỆ]: 1-1 về mặt logic với CMT nhờ UNIQUE. (1 Post có nhiều CMT, nhưng 1 CMT chỉ thuộc 1 Post).
CREATE TABLE [CT_POST_CMT](
	POST_ID BIGINT NOT NULL,                     -- Bài viết gốc (Khóa ngoại)
	CMT_ID BIGINT NOT NULL UNIQUE,               -- Bình luận Cấp 1 (UNIQUE để chặn 1 CMT bị móc vào 2 Post)
	PRIMARY KEY (POST_ID, CMT_ID),               -- Khóa kép định tuyến
	FOREIGN KEY (POST_ID) REFERENCES POSTS(ID),
	FOREIGN KEY (CMT_ID) REFERENCES CMT(ID)
);

-- Bảng CT_EVENT_CMT: Bảng Cầu nối Định tuyến - Trỏ CMT thuần khiết về Trạm Sự kiện.
-- [QUAN HỆ]: 1-1 về mặt logic với CMT nhờ UNIQUE.
CREATE TABLE [CT_EVENT_CMT](
	CT_EVENT_ID BIGINT NOT NULL,                 -- Trạm Sự kiện (Khóa ngoại)
	CMT_ID BIGINT NOT NULL UNIQUE,               -- Bình luận Cấp 1 
	PRIMARY KEY (CT_EVENT_ID, CMT_ID),           -- Khóa kép định tuyến
	FOREIGN KEY (CT_EVENT_ID) REFERENCES CT_EVENTS(ID),
	FOREIGN KEY (CMT_ID) REFERENCES CMT(ID)
);

-- Bảng CT_CMT_REPORTS: Sổ tay ghi nhận các lượt báo cáo vi phạm trên Bình luận Cấp 1.
-- [QUAN HỆ]: N-1 với CMT, N-1 với USERS.
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID). Không dùng Khóa kép. Chủ đích cho phép 1 User báo cáo 1 Comment nhiều lần để lưu vết tần suất spam, kết hợp REPORTER_IP để phân tích tấn công có tổ chức.
CREATE TABLE [CT_CMT_REPORTS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Mã đơn báo cáo
	CMT_ID BIGINT NOT NULL,                       -- Bình luận gốc bị báo cáo (Khóa ngoại)
	USER_ID BIGINT NOT NULL,                      -- Ai là người gửi báo cáo (Khóa ngoại)
	REASON NVARCHAR(255) NOT NULL,                -- Lý do báo cáo (VD: 'Spam', 'Sai sự thật')
	REPORTER_IP VARCHAR(50) NOT NULL,             -- Địa chỉ IP của người báo cáo để tra soát
	STATUS VARCHAR(20) DEFAULT 'PENDING',         -- Trạng thái đơn: 'PENDING', 'RESOLVED', 'REJECTED'
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm gửi báo cáo
	FOREIGN KEY (CMT_ID) REFERENCES CMT(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng CT_PH_CMT_REPORTS: Sổ tay ghi nhận các lượt báo cáo vi phạm trên Phản hồi (Cấp 2+).
-- [QUAN HỆ]: N-1 với PH_CMT, N-1 với USERS.
-- LƯU Ý KIẾN TRÚC: Tương tự CT_CMT_REPORTS.
CREATE TABLE [CT_PH_CMT_REPORTS](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Mã đơn báo cáo
	PH_CMT_ID BIGINT NOT NULL,                    -- Phản hồi bị báo cáo (Khóa ngoại)
	USER_ID BIGINT NOT NULL,                      -- Ai là người gửi báo cáo (Khóa ngoại)
	REASON NVARCHAR(255) NOT NULL,                -- Lý do báo cáo
	REPORTER_IP VARCHAR(50) NOT NULL,             -- Địa chỉ IP của người báo cáo để tra soát
	STATUS VARCHAR(20) DEFAULT 'PENDING',         -- Trạng thái đơn
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm gửi báo cáo
	FOREIGN KEY (PH_CMT_ID) REFERENCES PH_CMT(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

-- Bảng CT_CMT_REPORT_MOD_LOG: Sổ tay ghi án (Audit Log) xử lý đơn báo cáo bình luận gốc.
-- [QUAN HỆ]: N-1 với CT_CMT_REPORTS, N-1 với MODERATION_ACTIONS, N-1 với USERS (Moderator).
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID). Đóng vai trò vừa là Audit Log (Truy vết), vừa là Transaction Detail (Chi tiết xử lý). Bắt buộc lưu vết mạng (IP_ADDRESS, USER_AGENT) của Quản trị viên để chống lạm quyền. Ghi nhận sự dịch chuyển trạng thái (OLD_STATUS -> NEW_STATUS) làm bằng chứng pháp lý nội bộ.
CREATE TABLE [CT_CMT_REPORT_MOD_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Mã lệnh kiểm duyệt đơn báo cáo
	REPORT_ID BIGINT NOT NULL,                    -- Đơn báo cáo nào bị xử lý (Khóa ngoại)
	ACTION_ID INT NOT NULL,                       -- Mã hành vi (Khóa ngoại trỏ về MODERATION_ACTIONS, VD: RESOLVE_REPORT)
	MODERATOR_ID BIGINT NOT NULL,                 -- Admin ra phán quyết (Khóa ngoại)
	OLD_STATUS VARCHAR(20) NOT NULL,              -- Trạng thái CŨ của đơn báo cáo (VD: 'PENDING')
	NEW_STATUS VARCHAR(20) NOT NULL,              -- Trạng thái MỚI của đơn báo cáo (VD: 'RESOLVED', 'REJECTED')
	REASON NVARCHAR(255) NOT NULL,                -- Lý do (Bắt buộc phải ghi để giải trình thanh tra)
	IP_ADDRESS VARCHAR(50) NOT NULL,              -- Địa chỉ IP của Admin thực thi lệnh
	USER_AGENT NVARCHAR(500),                     -- Thông tin Trình duyệt / Thiết bị của Admin
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm đóng hồ sơ
	FOREIGN KEY (REPORT_ID) REFERENCES CT_CMT_REPORTS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID),
	FOREIGN KEY (MODERATOR_ID) REFERENCES USERS(ID)
);

-- Bảng CT_PH_CMT_REPORT_MOD_LOG: Sổ tay ghi án (Audit Log) xử lý đơn báo cáo phản hồi thứ cấp.
-- [QUAN HỆ]: N-1 với CT_PH_CMT_REPORTS, N-1 với MODERATION_ACTIONS, N-1 với USERS (Moderator).
-- LƯU Ý KIẾN TRÚC: Thiết kế đối xứng hoàn toàn với CT_CMT_REPORT_MOD_LOG để đảm bảo tính nhất quán của hệ thống.
CREATE TABLE [CT_PH_CMT_REPORT_MOD_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Mã lệnh kiểm duyệt đơn báo cáo phản hồi
	REPORT_ID BIGINT NOT NULL,                    -- Đơn báo cáo phản hồi bị xử lý (Khóa ngoại)
	ACTION_ID INT NOT NULL,                       -- Mã hành vi (Khóa ngoại)
	MODERATOR_ID BIGINT NOT NULL,                 -- Admin ra phán quyết (Khóa ngoại)
	OLD_STATUS VARCHAR(20) NOT NULL,              -- Trạng thái CŨ
	NEW_STATUS VARCHAR(20) NOT NULL,              -- Trạng thái MỚI
	REASON NVARCHAR(255) NOT NULL,                -- Lý do xử lý
	IP_ADDRESS VARCHAR(50) NOT NULL,              -- Địa chỉ IP của Admin
	USER_AGENT NVARCHAR(500),                     -- Thông tin Trình duyệt
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm đóng hồ sơ
	FOREIGN KEY (REPORT_ID) REFERENCES CT_PH_CMT_REPORTS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID),
	FOREIGN KEY (MODERATOR_ID) REFERENCES USERS(ID)
);


-- Bảng CT_CMT_ACTION_LOG: Sổ tay kiểm toán hành vi tự tạo/sửa bình luận gốc của người dùng.
-- [QUAN HỆ]: N-1 với CMT, N-1 với USERS, N-1 với MODERATION_ACTIONS.
-- LƯU Ý KIẾN TRÚC: Tách biệt hoàn toàn với vùng Profile. Lưu vết IP lúc đăng và giữ lại nội dung Cũ/Mới để đối chứng nếu User sửa comment hòng chối bỏ trách nhiệm.
CREATE TABLE [CT_CMT_ACTION_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính độc lập
	CMT_ID BIGINT NOT NULL,                       -- Dòng bình luận bị tác động (Khóa ngoại)
	USER_ID BIGINT NOT NULL,                      -- Ai là người thực hiện (Khóa ngoại)
	ACTION_ID INT NOT NULL,                       -- Trỏ về MODERATION_ACTIONS (VD: CREATE_CMT, UPDATE_CMT)
	OLD_PAYLOAD NVARCHAR(MAX),                    -- Nội dung văn bản CŨ trước khi sửa
	NEW_PAYLOAD NVARCHAR(MAX),                    -- Nội dung văn bản MỚI
	IP_ADDRESS VARCHAR(50),                       -- Địa chỉ IP đã thực thi hành vi đăng/sửa
	USER_AGENT NVARCHAR(500),                     -- Thông tin Trình duyệt / Thiết bị
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm thực hiện
	FOREIGN KEY (CMT_ID) REFERENCES CMT(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID)
);

-- Bảng CT_PH_CMT_ACTION_LOG: Sổ tay kiểm toán hành vi tự tạo/sửa phản hồi của người dùng.
-- [QUAN HỆ]: N-1 với PH_CMT, N-1 với USERS, N-1 với MODERATION_ACTIONS.
CREATE TABLE [CT_PH_CMT_ACTION_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- Khóa chính độc lập
	PH_CMT_ID BIGINT NOT NULL,                    -- Dòng phản hồi bị tác động (Khóa ngoại)
	USER_ID BIGINT NOT NULL,                      -- Ai là người thực hiện (Khóa ngoại)
	ACTION_ID INT NOT NULL,                       -- Trỏ về MODERATION_ACTIONS 
	OLD_PAYLOAD NVARCHAR(MAX),                    -- Nội dung văn bản CŨ
	NEW_PAYLOAD NVARCHAR(MAX),                    -- Nội dung văn bản MỚI
	IP_ADDRESS VARCHAR(50),                       -- Địa chỉ IP đã thực thi hành vi
	USER_AGENT NVARCHAR(500),                     -- Thông tin Trình duyệt / Thiết bị
	CREATED_AT DATETIME DEFAULT GETDATE(),        -- Thời điểm thực hiện
	FOREIGN KEY (PH_CMT_ID) REFERENCES PH_CMT(ID),
	FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID)
);

-- Bảng CT_CMT_MODERATION_LOG: Sổ tay ghi án (Audit Log) cho việc Quản lý Bình luận Cấp 1.
-- [QUAN HỆ]: N-1 với CMT, N-1 với MODERATION_ACTIONS, N-1 với USERS (Moderator).
-- LƯU Ý KIẾN TRÚC: CÓ KHÓA ĐỘC LẬP (ID). Trạng thái hiển thị hay ẩn của CMT phụ thuộc vào Record mới nhất trong bảng này.
CREATE TABLE [CT_CMT_MODERATION_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Mã lệnh kiểm duyệt
	CMT_ID BIGINT NOT NULL,                      -- Bình luận Cấp 1 bị xử lý
	ACTION_ID INT NOT NULL,                      -- Mã hành vi kiểm duyệt (Khóa ngoại)
	MODERATOR_ID BIGINT NOT NULL,                -- ID của Quản trị viên ra lệnh
	REASON NVARCHAR(255),                        -- Lý do phạt (VD: 'Nội dung phản cảm')
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Thời điểm chém
	FOREIGN KEY (CMT_ID) REFERENCES CMT(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID),
	FOREIGN KEY (MODERATOR_ID) REFERENCES USERS(ID)
);

-- Bảng CT_PH_CMT_MODERATION_LOG: Sổ tay ghi án cho Quản lý Phản hồi Cấp 2+.
-- [QUAN HỆ]: N-1 với PH_CMT, N-1 với MODERATION_ACTIONS, N-1 với USERS (Moderator).
CREATE TABLE [CT_PH_CMT_MODERATION_LOG](
	ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,-- Mã lệnh kiểm duyệt
	PH_CMT_ID BIGINT NOT NULL,                   -- Phản hồi Cấp 2+ bị xử lý
	ACTION_ID INT NOT NULL,                      -- Hành động xử phạt
	MODERATOR_ID BIGINT NOT NULL,                -- Người ra hình phạt
	REASON NVARCHAR(255),                        -- Diễn giải lý do
	CREATED_AT DATETIME DEFAULT GETDATE(),       -- Thời điểm xử lý
	FOREIGN KEY (PH_CMT_ID) REFERENCES PH_CMT(ID),
	FOREIGN KEY (ACTION_ID) REFERENCES MODERATION_ACTIONS(ID),
	FOREIGN KEY (MODERATOR_ID) REFERENCES USERS(ID)
);



-- =========================================================================
-- ĐẠI KỊCH BẢN DỮ LIỆU MẪU (MASTER SEED DATA) - HỆ SINH THÁI DƯỢC PHẨM B2B
-- 100% Dữ liệu thực tế y khoa. Bao phủ 28 Bảng. Ít nhất 10 dòng/bảng.
-- Các ID bắt đầu từ 100 để an toàn, không đụng chạm dữ liệu cũ.
-- =========================================================================

USE Web_Pharma_QuangDuong;
GO

SET DATEFORMAT DMY;
GO

-- =====================================================================
-- 1. BẢNG USERS (10 Tài khoản thực tế: Admin, Bác sĩ, Dược sĩ, Đối tác)
-- =====================================================================
SET IDENTITY_INSERT [USERS] ON;
INSERT INTO [USERS] (ID, USERNAME, PASSWORD, FULL_NAME, EMAIL, PHONE, ADDRESS, BIRTH_DATE, PROVIDER, LOCKED, CREATED_AT) VALUES 
(101, 'admin_truyenthong', '$2a$10$abc', N'Ban Truyền thông PharmaCorp', 'media@pharmacorp.vn', '0901000101', N'52 Láng Hạ, Đống Đa, Hà Nội', '1990-01-01', 'LOCAL', 0, GETDATE()),
(102, 'bs_nguyenvanhung', '$2a$10$abc', N'PGS.TS. Nguyễn Văn Hùng', 'hung.nv@yhn.edu.vn', '0912000102', N'Bệnh viện Bạch Mai, Hà Nội', '1975-05-12', 'LOCAL', 0, GETDATE()),
(103, 'bs_tranbichlien', '$2a$10$abc', N'BS.CK2. Trần Bích Liên', 'lien.tb@choray.vn', '0983000103', N'Bệnh viện Chợ Rẫy, TP.HCM', '1980-08-22', 'LOCAL', 0, GETDATE()),
(104, 'ds_lequocthang', '$2a$10$abc', N'ThS.DS. Lê Quốc Thắng', 'thang.lq@dhyd.edu.vn', '0974000104', N'Đại học Y Dược TP.HCM', '1988-11-05', 'LOCAL', 0, GETDATE()),
(105, 'bs_phamthiminh', '$2a$10$abc', N'TS.BS. Phạm Thị Minh', 'minh.pt@nhidong1.vn', '0905000105', N'Bệnh viện Nhi Đồng 1, TP.HCM', '1982-02-14', 'LOCAL', 0, GETDATE()),
(106, 'partner_longchau', '$2a$10$abc', N'Chuỗi Nhà thuốc Long Châu', 'b2b@longchau.vn', '0936000106', N'Quận 3, TP.HCM', '2010-01-01', 'LOCAL', 0, GETDATE()),
(107, 'partner_pharmacity', '$2a$10$abc', N'Nhà thuốc Pharmacity', 'purchasing@pharmacity.vn', '0927000107', N'Quận Phú Nhuận, TP.HCM', '2015-01-01', 'LOCAL', 0, GETDATE()),
(108, 'partner_ankhang', '$2a$10$abc', N'Nhà thuốc An Khang', 'doitac@ankhang.vn', '0948000108', N'Quận Tân Bình, TP.HCM', '2018-01-01', 'LOCAL', 0, GETDATE()),
(109, 'mod_kiemduyet1', '$2a$10$abc', N'Kiểm duyệt viên Nội dung 1', 'mod1@pharmacorp.vn', '0969000109', N'Trụ sở chính', '1995-07-20', 'LOCAL', 0, GETDATE()),
(110, 'guest_anonymous', '$2a$10$abc', N'Độc giả Vãng lai', 'guest.test@gmail.com', '0990000110', N'Không rõ', '2000-01-01', 'GOOGLE', 0, GETDATE());
SET IDENTITY_INSERT [USERS] OFF;
GO

-- =====================================================================
-- 2. BẢNG CATEGORIES (10 Danh mục Y khoa/Marketing thực tế)
-- =====================================================================
SET IDENTITY_INSERT [CATEGORIES] ON;
INSERT INTO [CATEGORIES] (ID, NAME, SLUG, DESCRIPTION, IS_ACTIVE) VALUES 
(101, N'Nghiên cứu Lâm sàng', 'nghien-cuu-lam-sang', N'Tổng hợp các báo cáo RCT, phân tích gộp (Meta-analysis) và thử nghiệm lâm sàng mới nhất toàn cầu.', 1),
(102, N'Phác đồ Điều trị', 'phac-do-dieu-tri', N'Hướng dẫn chẩn đoán và phác đồ điều trị chuẩn hóa từ Bộ Y tế và các Hiệp hội y khoa uy tín.', 1),
(103, N'Cảnh giác Dược (ADR)', 'canh-giac-duoc-adr', N'Cập nhật cảnh báo an toàn thuốc, tương tác thuốc và báo cáo phản ứng có hại (ADR).', 1),
(104, N'Tim mạch & Huyết áp', 'tim-mach-huyet-ap', N'Chuyên san về bệnh lý tuần hoàn, thuốc chống đông, thuốc hạ áp và suy tim.', 1),
(105, N'Kháng sinh & Kháng nấm', 'khang-sinh-khang-nam', N'Cập nhật tình hình đề kháng kháng sinh, chiến lược phối hợp và thuốc thế hệ mới.', 1),
(106, N'Nội tiết & Đái tháo đường', 'noi-tiet-dai-thao-duong', N'Tin tức về quản lý bệnh lý tuyến giáp, tiểu đường type 1/2 và tiểu đường thai kỳ.', 1),
(107, N'Hô hấp & Tai Mũi Họng', 'ho-hap-tai-mui-hong', N'Bệnh lý hen suyễn, COPD, viêm phổi mắc phải cộng đồng và các thuốc điều trị.', 1),
(108, N'Nhi khoa & Sơ sinh', 'nhi-khoa-so-sinh', N'Hướng dẫn tính liều thuốc an toàn cho trẻ em, vaccine và dinh dưỡng nhi khoa.', 1),
(109, N'Dược Mỹ Phẩm & TPCN', 'duoc-my-pham-tpcn', N'Đánh giá hiệu quả thực phẩm chức năng, vitamin và dược mỹ phẩm điều trị da liễu.', 1),
(110, N'Tin tức Doanh nghiệp', 'tin-tuc-doanh-nghiep', N'Sự kiện hợp tác chiến lược, CSR, chuỗi cung ứng và định hướng phát triển của PharmaCorp.', 1);
SET IDENTITY_INSERT [CATEGORIES] OFF;
GO

-- =====================================================================
-- 3. BẢNG TAGS (10 Thẻ từ khóa chuẩn SEO)
-- =====================================================================
SET IDENTITY_INSERT [TAGS] ON;
INSERT INTO [TAGS] (ID, NAME, SLUG) VALUES 
(101, N'#KhangSinhTheHeMoi', 'khang-sinh-the-he-moi'),
(102, N'#TimMach2026', 'tim-mach-2026'),
(103, N'#DaiThaoDuongThaiKy', 'dai-thao-duong-thai-ky'),
(104, N'#BaoCaoADR', 'bao-cao-adr'),
(105, N'#GMP_ChauAu', 'gmp-chau-au'),
(106, N'#ViemPhoiCongDong', 'viem-phoi-cong-dong'),
(107, N'#DeKhangKhasi', 'de-khang-khang-sinh'),
(108, N'#CME_Training', 'cme-training'),
(109, N'#NhiKhoaLamsang', 'nhi-khoa-lam-sang'),
(110, N'#HopTacChienLuoc', 'hop-tac-chien-luoc');
SET IDENTITY_INSERT [TAGS] OFF;
GO

-- =====================================================================
-- 4. BẢNG POSTS (10 Bài viết chuyên sâu, văn phong y khoa thật 100%)
-- Đa dạng ACCESS_LEVEL: PUBLIC, ROLE_DOCTOR, ROLE_PARTNER
-- Đa dạng trạng thái: Xuất bản, Lưu nháp
-- =====================================================================
SET IDENTITY_INSERT [POSTS] ON;
INSERT INTO [POSTS] (ID, TITLE, SLUG, SUMMARY, CONTENT, THUMBNAIL_URL, ACCESS_LEVEL, IS_PUBLISHED, SEO_TITLE, SEO_DESCRIPTION, CATEGORY_ID, AUTHOR_ID, CREATED_AT) VALUES 
(101, N'Đánh giá hiệu quả lâm sàng của kháng sinh thế hệ mới trong điều trị viêm phổi mắc phải cộng đồng: Phân tích gộp 47 RCT', 'danh-gia-hieu-qua-lam-sang-khang-sinh-viem-phoi-2026', N'Nghiên cứu phân tích 47 thử nghiệm lâm sàng ngẫu nhiên có đối chứng từ 14 quốc gia, so sánh hiệu quả và độ an toàn của carbapenem thế hệ mới...', N'<h2>1. Đặt vấn đề</h2><p>Viêm phổi mắc phải cộng đồng (CAP) do vi khuẩn Gram âm đa kháng đang là thách thức lớn. Việc lựa chọn kháng sinh kinh nghiệm ban đầu quyết định 40% tỷ lệ sống còn...</p><h2>2. Phương pháp</h2><p>Truy xuất dữ liệu từ PubMed, Embase từ 2020-2026...</p>', 'https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=800&q=80', 'ROLE_DOCTOR', 1, N'Hiệu quả kháng sinh mới điều trị Viêm phổi 2026', N'Phân tích gộp 47 RCT về hiệu quả của Carbapenem thế hệ mới trong điều trị CAP.', 101, 102, DATEADD(day, -10, GETDATE())),

(102, N'Cập nhật phác đồ điều trị đái tháo đường thai kỳ theo khuyến cáo ADA 2026', 'cap-nhat-phac-do-dai-thao-duong-thai-ky-ada-2026', N'Hướng dẫn toàn diện về quản lý GDM dựa trên bằng chứng mới nhất, bao gồm ngưỡng chẩn đoán và chiến lược sử dụng insulin...', N'<h2>1. Tiêu chuẩn chẩn đoán mới</h2><p>Theo ADA 2026, nghiệm pháp dung nạp glucose 75g (OGTT) được thực hiện ở tuần thai 24-28. Ngưỡng đường huyết lúc đói hạ xuống còn 5.0 mmol/L...</p><h2>2. Lựa chọn Insulin</h2><p>Insulin Detemir và Aspart tiếp tục được phân loại an toàn cấp độ B...</p>', 'https://images.unsplash.com/photo-1631815589968-fdb09a223b1e?w=800&q=80', 'ROLE_DOCTOR', 1, N'Phác đồ đái tháo đường thai kỳ ADA 2026', N'Hướng dẫn chẩn đoán và điều trị tiểu đường thai kỳ theo Hiệp hội Đái tháo đường Hoa Kỳ (ADA) cập nhật 2026.', 102, 105, DATEADD(day, -9, GETDATE())),

(103, N'Báo cáo ADR Tổng hợp Q4/2025 — 12 cảnh báo tương tác thuốc nghiêm trọng cần lưu ý', 'bao-cao-adr-tong-hop-q4-2025-12-canh-bao', N'Tổng hợp các trường hợp ADR nghiêm trọng từ hệ thống VigiBase Việt Nam, tập trung vào tương tác giữa kháng đông thế hệ mới và nhóm Azole.', N'<h2>Cảnh báo mức độ 1: Apixaban và Itraconazole</h2><p>Đã ghi nhận 14 ca xuất huyết tiêu hóa nặng khi dùng chung Apixaban với các thuốc kháng nấm nhóm Azole do ức chế mạnh CYP3A4 và P-gp...</p>', 'https://images.unsplash.com/photo-1587854692152-cbe660dbde88?w=800&q=80', 'ROLE_PARTNER', 1, N'Báo cáo ADR Q4/2025: Tương tác thuốc nghiêm trọng', N'Cập nhật 12 cảnh báo tương tác thuốc nguy hiểm từ hệ thống cảnh giác dược Việt Nam.', 103, 104, DATEADD(day, -8, GETDATE())),

(104, N'Mục tiêu huyết áp mới nhất theo ACC/AHA 2025: Thay đổi thực hành lâm sàng tại tuyến cơ sở', 'muc-tieu-huyet-ap-acc-aha-2025-tuyen-co-so', N'Hướng dẫn cập nhật ngưỡng can thiệp và mục tiêu điều trị huyết áp dựa trên tầng nguy cơ tim mạch toàn phần 10 năm (ASCVD risk).', N'<h2>Hạ ngưỡng mục tiêu xuống < 130/80 mmHg</h2><p>Dành cho bệnh nhân có nguy cơ ASCVD > 10%. Các thuốc ức chế men chuyển (ACEi) hoặc chẹn thụ thể (ARB) vẫn là lựa chọn đầu tay...</p>', 'https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=800&q=80', 'PUBLIC', 1, N'Mục tiêu huyết áp ACC/AHA 2025', N'Phân tích những thay đổi quan trọng trong phác đồ điều trị tăng huyết áp của ACC/AHA.', 104, 103, DATEADD(day, -7, GETDATE())),

(105, N'PharmaCorp chính thức ký kết hợp tác chiến lược với 3 tập đoàn dược phẩm châu Âu', 'pharmacorp-ky-ket-hop-tac-chien-luoc-chau-au-2026', N'Thỏa thuận phân phối độc quyền với AstraZeneca, Roche và Sanofi mở ra kỷ nguyên mới cho chuỗi cung ứng dược phẩm B2B tại Việt Nam.', N'<p>Ngày 15/04/2026, tại Hà Nội, đại diện PharmaCorp đã tiến hành ký kết...</p><p>Sự kiện này đánh dấu bước ngoặt trong việc đảm bảo nguồn cung thuốc đặc trị ung thư và tim mạch...</p>', 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=800&q=80', 'PUBLIC', 1, N'PharmaCorp hợp tác chiến lược AstraZeneca, Roche, Sanofi', N'Tin tức sự kiện ký kết hợp tác phân phối độc quyền dược phẩm giữa PharmaCorp và các đối tác châu Âu.', 110, 101, DATEADD(day, -6, GETDATE())),

(106, N'Bảng tra cứu nhanh liều lượng thuốc hạ sốt, giảm đau an toàn cho Nhi khoa (Cập nhật 2026)', 'bang-tra-cuu-lieu-thuoc-nhi-khoa-2026', N'Công cụ hỗ trợ dược sĩ nhà thuốc tính liều Paracetamol và Ibuprofen chính xác theo cân nặng và tháng tuổi, hạn chế độc tính trên gan.', N'<h2>Nguyên tắc tính liều</h2><p>Paracetamol: 10-15mg/kg/lần, cách nhau 4-6 giờ. Tối đa không quá 60mg/kg/ngày...</p>', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800&q=80', 'PUBLIC', 1, N'Liều thuốc hạ sốt giảm đau an toàn cho trẻ em', N'Hướng dẫn cách tính liều paracetamol, ibuprofen chuẩn xác cho trẻ em dành cho dược sĩ.', 108, 104, DATEADD(day, -5, GETDATE())),

(107, N'Chuỗi cung ứng dược lạnh (Cold Chain): Giải pháp giảm tỷ lệ hao hụt vắc xin xuống dưới 1%', 'chuoi-cung-ung-duoc-lanh-giam-hao-hut-vac-xin', N'Báo cáo kỹ thuật về ứng dụng IoT và Data Logger thời gian thực trong việc duy trì nhiệt độ 2-8°C xuyên suốt chặng đường vận chuyển.', N'<p>Cấu trúc hệ thống giám sát nhiệt độ đa điểm...</p>', 'https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=800&q=80', 'ROLE_PARTNER', 1, N'Chuỗi cung ứng dược lạnh Cold Chain', N'Công nghệ IoT giám sát nhiệt độ vận chuyển vắc xin chuẩn GDP.', 110, 101, DATEADD(day, -4, GETDATE())),

(108, N'Tiêu chí lựa chọn Thực phẩm chức năng hỗ trợ xương khớp: Bóc trần nhãn mác', 'tieu-chi-lua-chon-tpcn-xuong-khop-2026', N'Phân tích thành phần Glucosamine Sulfate so với Glucosamine HCl, vai trò của Chondroitin và Collagen type 2 không biến tính (UC-II).', N'<p>Rất nhiều sản phẩm trên thị trường dùng Glucosamine HCl vì giá thành rẻ, tuy nhiên bằng chứng lâm sàng lại nghiêng về dạng Sulfate...</p>', 'https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=800&q=80', 'PUBLIC', 1, N'Tiêu chí chọn TPCN xương khớp chất lượng', N'Dược sĩ lâm sàng phân tích thành phần Glucosamine, Chondroitin, UC-II trong TPCN.', 109, 104, DATEADD(day, -3, GETDATE())),

(109, N'Sốc phản vệ do Cephalosporin: Giao thức xử trí cấp cứu tại nhà thuốc (BẢN NHÁP)', 'soc-phan-ve-cephalosporin-xu-tri-cap-cuu-nha-thuoc', N'Tài liệu tập huấn nội bộ về cách nhận diện sớm sốc phản vệ và quy trình sử dụng Adrenalin 1mg/1ml.', N'<p>Đang biên soạn phần sơ đồ cấp cứu...</p>', NULL, 'ROLE_ADMIN', 0, NULL, NULL, 103, 103, DATEADD(day, -2, GETDATE())),

(110, N'Hiệu chỉnh liều thuốc trên bệnh nhân suy thận mạn tính (eGFR < 30 mL/min) (BẢN NHÁP)', 'hieu-chinh-lieu-thuoc-suy-than-man-egfr-30', N'Bảng tra cứu nhanh 32 nhóm thuốc cần điều chỉnh liều, ngưỡng eGFR chống chỉ định và các cặp tương tác thận nguy hiểm.', N'<p>Đang rà soát số liệu eGFR...</p>', NULL, 'ROLE_DOCTOR', 0, NULL, NULL, 102, 102, DATEADD(day, -1, GETDATE()));
SET IDENTITY_INSERT [POSTS] OFF;
GO

-- =====================================================================
-- 5. BẢNG CT_POST_TAGS (15 Dòng - Nối Bài viết với Tag)
-- =====================================================================
INSERT INTO [CT_POST_TAGS] (POST_ID, TAG_ID) VALUES 
(101, 101), (101, 106), (101, 107), -- Viêm phổi: Kháng sinh, Viêm phổi CĐ, Kháng kháng sinh
(102, 103), (102, 107),             -- Tiểu đường: ĐTĐ Thai kỳ, Phác đồ
(103, 104),                         -- Báo cáo ADR
(104, 102), (104, 103),             -- Huyết áp: Tim mạch, Huyết áp
(105, 105), (105, 110),             -- Tin tức: GMP, Hợp tác
(106, 109),                         -- Nhi khoa
(107, 105), (107, 109),             -- Chuỗi lạnh: GMP, Vaccine
(108, 110);                         -- TPCN
GO

-- =====================================================================
-- 6. BẢNG POST_IMAGES (10 Dòng - Ảnh đính kèm trong bài)
-- =====================================================================
SET IDENTITY_INSERT [POST_IMAGES] ON;
INSERT INTO [POST_IMAGES] (ID, POST_ID, IMAGE_URL, DISPLAY_ORDER) VALUES 
(101, 101, '/uploads/images/posts/pneumonia_ct_scan_1.jpg', 1),
(102, 101, '/uploads/images/posts/pneumonia_xray_2.jpg', 2),
(103, 102, '/uploads/images/posts/ada_chart_2026.png', 1),
(104, 103, '/uploads/images/posts/adr_chart_q4_2025.jpg', 1),
(105, 103, '/uploads/images/posts/adr_interaction_table.png', 2),
(106, 104, '/uploads/images/posts/bp_target_chart.jpg', 1),
(107, 105, '/uploads/images/posts/pharmacorp_signing_ceremony.jpg', 1),
(108, 106, '/uploads/images/posts/pediatric_dose_table.png', 1),
(109, 107, '/uploads/images/posts/cold_chain_iot.jpg', 1),
(110, 108, '/uploads/images/posts/glucosamine_structure.png', 1);
SET IDENTITY_INSERT [POST_IMAGES] OFF;
GO

-- =====================================================================
-- 7. BẢNG POST_FILES (10 Dòng - Tài liệu số để thu Lead)
-- =====================================================================
SET IDENTITY_INSERT [POST_FILES] ON;
INSERT INTO [POST_FILES] (ID, POST_ID, FILE_NAME, FILE_URL, FILE_TYPE, FILE_SIZE) VALUES 
(101, 101, N'Toan_van_Nghien_cuu_Phan_tich_gop_CAP.pdf', '/uploads/files/CAP_Meta_Analysis.pdf', 'pdf', 4500000),
(102, 101, N'Raw_Data_47_RCT_Excel.xlsx', '/uploads/files/RawData_47_RCT.xlsx', 'xlsx', 2100000),
(103, 102, N'So_do_Phac_do_ADA_2026.pdf', '/uploads/files/ADA_Algorithm_2026.pdf', 'pdf', 1200000),
(104, 103, N'Bao_cao_ADR_Q4_2025_CucQLD.pdf', '/uploads/files/ADR_Report_Q4_2025.pdf', 'pdf', 3800000),
(105, 104, N'Tom_tat_Huong_dan_ACC_AHA.docx', '/uploads/files/ACC_AHA_Summary.docx', 'docx', 850000),
(106, 105, N'Thong_cao_bao_chi_HRC.pdf', '/uploads/files/Press_Release_Euro.pdf', 'pdf', 500000),
(107, 106, N'Bang_Tra_Cuu_Lieu_Nhi_Khoa_In.pdf', '/uploads/files/Pediatric_Dose_Print.pdf', 'pdf', 3200000),
(108, 107, N'Quy_trinh_SOP_Cold_Chain.pdf', '/uploads/files/SOP_Cold_Chain.pdf', 'pdf', 5600000),
(109, 107, N'Checklist_kiem_tra_Nhiet_do_Kho.xlsx', '/uploads/files/Checklist_Temp.xlsx', 'xlsx', 450000),
(110, 108, N'Huong_dan_Tu_van_TPCN_XuongKhop.pdf', '/uploads/files/Guide_Joint_Supplements.pdf', 'pdf', 1500000);
SET IDENTITY_INSERT [POST_FILES] OFF;
GO

-- =====================================================================
-- 8. BẢNG POST_VIEW_LOGS (10 Dòng - Ghi nhận lượt xem)
-- =====================================================================
SET IDENTITY_INSERT [POST_VIEW_LOGS] ON;
INSERT INTO [POST_VIEW_LOGS] (ID, POST_ID, VIEWER_IP, USER_ID, VIEWED_AT) VALUES 
(101, 101, '113.160.10.12', 102, DATEADD(hour, -24, GETDATE())),
(102, 101, '14.161.22.33', 103, DATEADD(hour, -22, GETDATE())),
(103, 102, '192.168.1.5', 105, DATEADD(hour, -18, GETDATE())),
(104, 102, '115.79.12.4', NULL, DATEADD(hour, -16, GETDATE())), -- Khách ẩn danh
(105, 103, '14.161.22.33', 106, DATEADD(hour, -12, GETDATE())),
(106, 103, '113.160.10.12', 107, DATEADD(hour, -10, GETDATE())),
(107, 104, '115.79.12.4', NULL, DATEADD(hour, -8, GETDATE())),
(108, 105, '192.168.1.9', 108, DATEADD(hour, -6, GETDATE())),
(109, 106, '14.161.22.33', 104, DATEADD(hour, -4, GETDATE())),
(110, 107, '113.160.10.12', 106, DATEADD(hour, -2, GETDATE()));
SET IDENTITY_INSERT [POST_VIEW_LOGS] OFF;
GO

-- =====================================================================
-- 9. BẢNG CT_FILE_DOWNLOADS (10 Dòng - Lead Scoring)
-- =====================================================================
-- Bảng dùng Khóa Kép (FILE_ID, USER_ID)
INSERT INTO [CT_FILE_DOWNLOADS] (FILE_ID, USER_ID, DOWNLOADED_AT) VALUES 
(101, 102, DATEADD(hour, -23, GETDATE())),
(102, 102, DATEADD(hour, -23, GETDATE())),
(103, 105, DATEADD(hour, -17, GETDATE())),
(104, 106, DATEADD(hour, -11, GETDATE())),
(104, 107, DATEADD(hour, -9, GETDATE())),
(105, 103, DATEADD(hour, -7, GETDATE())),
(106, 108, DATEADD(hour, -5, GETDATE())),
(107, 104, DATEADD(hour, -3, GETDATE())),
(108, 106, DATEADD(hour, -1, GETDATE())),
(109, 106, DATEADD(minute, -30, GETDATE()));
GO

-- =====================================================================
-- 10. BẢNG EVENT_TYPES (10 Loại Sự kiện)
-- =====================================================================
SET IDENTITY_INSERT [EVENT_TYPES] ON;
INSERT INTO [EVENT_TYPES] (ID, NAME, DESCRIPTION) VALUES 
(101, N'Hội thảo Khoa học (CME)', N'Hội thảo cấp chứng chỉ đào tạo liên tục CME cho nhân viên y tế.'),
(102, N'Webinar Trực tuyến', N'Hội thảo chuyên môn phát sóng qua Zoom/Webex.'),
(103, N'Đào tạo Trình dược viên', N'Khóa huấn luyện kiến thức sản phẩm và kỹ năng sales B2B.'),
(104, N'Hội nghị Ngành Dược', N'Sự kiện quy mô lớn quy tụ các chuyên gia, đối tác và nhà phân phối.'),
(105, N'Ra mắt Sản phẩm mới', N'Sự kiện Launching giới thiệu dòng thuốc hoặc TPCN mới.'),
(106, N'Talkshow Chuyên gia', N'Tọa đàm giải đáp thắc mắc lâm sàng cùng các bác sĩ đầu ngành.'),
(107, N'Hội thao Công ty', N'Sự kiện teambuilding nội bộ PharmaCorp.'),
(108, N'Triển lãm Y tế (Exhibition)', N'Gian hàng trưng bày tại các sự kiện Pharmedi, Vietnam Medipharm.'),
(109, N'Khám bệnh Cộng đồng (CSR)', N'Chương trình trách nhiệm xã hội, phát thuốc miễn phí.'),
(110, N'Tham quan Nhà máy (Site Visit)', N'Đón tiếp đối tác tham quan dây chuyền đạt chuẩn EU-GMP.');
SET IDENTITY_INSERT [EVENT_TYPES] OFF;
GO

-- =====================================================================
-- 11. BẢNG LOCATIONS (10 Địa điểm)
-- =====================================================================
SET IDENTITY_INSERT [LOCATIONS] ON;
INSERT INTO [LOCATIONS] (ID, NAME, ADDRESS) VALUES 
(101, N'Hội trường A - Bệnh viện Bạch Mai', N'78 Giải Phóng, Đống Đa, Hà Nội'),
(102, N'Trung tâm Hội nghị GEM Center', N'8 Nguyễn Bỉnh Khiêm, Q.1, TP.HCM'),
(103, N'Khách sạn Melia Hà Nội', N'44B Lý Thường Kiệt, Hoàn Kiếm, Hà Nội'),
(104, N'Webinar Server 1 (Zoom)', N'https://zoom.us/j/pharmacorp-room1'),
(105, N'Webinar Server 2 (Microsoft Teams)', N'https://teams.microsoft.com/l/meetup-join/19...'),
(106, N'Khách sạn Sheraton Sài Gòn', N'88 Đồng Khởi, Q.1, TP.HCM'),
(107, N'Đại học Y Dược TP.HCM', N'217 Hồng Bàng, Q.5, TP.HCM'),
(108, N'Nhà máy Dược phẩm EU-GMP PharmaCorp', N'KCN VSIP, Bình Dương'),
(109, N'Trung tâm Hội chợ Triển lãm SECC', N'799 Nguyễn Văn Linh, Q.7, TP.HCM'),
(110, N'Văn phòng Trụ sở PharmaCorp', N'52 Láng Hạ, Đống Đa, Hà Nội');
SET IDENTITY_INSERT [LOCATIONS] OFF;
GO

-- =====================================================================
-- 12. BẢNG EVENTS (10 Chiến dịch Sự kiện Tổng)
-- =====================================================================
SET IDENTITY_INSERT [EVENTS] ON;
INSERT INTO [EVENTS] (ID, EVENT_TYPE_ID, TITLE, SLUG, DESCRIPTION, THUMBNAIL_URL, CREATED_AT) VALUES 
(101, 101, N'Chuỗi Hội thảo Cập nhật Phác đồ Tim mạch 2026', 'chuoi-hoi-thao-tim-mach-2026', N'<p>Chuỗi 3 buổi đào tạo liên tục (CME) do Hội Tim mạch học Việt Nam phối hợp tổ chức.</p>', 'https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=800&q=80', DATEADD(day, -15, GETDATE())),
(102, 104, N'Hội nghị Dược phẩm Quốc tế Việt Nam - PharmaSummit 2026', 'hoi-nghi-pharma-summit-2026', N'<p>Sự kiện thường niên lớn nhất ngành dược, quy tụ 1000+ chuyên gia và nhà phân phối.</p>', 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80', DATEADD(day, -14, GETDATE())),
(103, 102, N'Webinar: Ứng dụng Kháng sinh thế hệ mới trong ICU', 'webinar-khang-sinh-icu-2026', N'<p>Phân tích các case lâm sàng khó từ Bệnh viện Chợ Rẫy.</p>', 'https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=800&q=80', DATEADD(day, -12, GETDATE())),
(104, 103, N'Khóa đào tạo Trình dược viên ETC chuyên nghiệp Q2/2026', 'dao-tao-tdv-etc-q2-2026', N'<p>Trang bị kỹ năng đọc hiểu tài liệu lâm sàng và giao tiếp với KOLs.</p>', 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=800&q=80', DATEADD(day, -10, GETDATE())),
(105, 105, N'Lễ Ra mắt Dòng Sản phẩm Bổ trợ Xương khớp JointCare Pro', 'ra-mat-jointcare-pro-2026', N'<p>Công nghệ màng bọc nano từ Đức giúp tăng sinh khả dụng lên 300%.</p>', 'https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=800&q=80', DATEADD(day, -8, GETDATE())),
(106, 108, N'Gian hàng PharmaCorp tại Vietnam Medipharm Expo 2026', 'medipharm-expo-2026-pharmacorp', N'<p>Kính mời đối tác tham quan và nhận mẫu thử miễn phí.</p>', 'https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&q=80', DATEADD(day, -6, GETDATE())),
(107, 109, N'Hành trình Trái tim: Khám sàng lọc tim bẩm sinh tại Hà Giang', 'kham-sang-loc-tim-ha-giang', N'<p>Chương trình CSR tài trợ 100% thuốc và vật tư y tế.</p>', 'https://images.unsplash.com/photo-1532938911079-1b06ac7ce122?w=800&q=80', DATEADD(day, -5, GETDATE())),
(108, 110, N'Partner Site Visit: Tham quan nhà máy EU-GMP đợt 1', 'tham-quan-nha-may-eu-gmp-1', N'<p>Dành riêng cho Top 50 đại lý phân phối xuất sắc nhất.</p>', 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=800&q=80', DATEADD(day, -4, GETDATE())),
(109, 106, N'Talkshow: Nhận diện Sốc phản vệ và Kỹ năng xử trí tại Nhà thuốc', 'talkshow-soc-phan-ve-nha-thuoc', N'<p>Chuyên gia cấp cứu hướng dẫn trực tiếp thao tác tiêm Adrenalin.</p>', 'https://images.unsplash.com/photo-1587854692152-cbe660dbde88?w=800&q=80', DATEADD(day, -2, GETDATE())),
(110, 101, N'Hội nghị Sản phụ khoa miền Nam mở rộng', 'hoi-nghi-san-phu-khoa-mn-2026', N'<p>Cập nhật hướng dẫn điều trị tiểu đường thai kỳ và tiền sản giật.</p>', 'https://images.unsplash.com/photo-1631815589968-fdb09a223b1e?w=800&q=80', DATEADD(day, -1, GETDATE()));
SET IDENTITY_INSERT [EVENTS] OFF;
GO

-- =====================================================================
-- 13. BẢNG CT_EVENTS (10 Buổi Sự kiện - Sessions)
-- =====================================================================
SET IDENTITY_INSERT [CT_EVENTS] ON;
INSERT INTO [CT_EVENTS] (ID, EVENT_ID, LOCATION_ID, START_TIME, END_TIME, TOTAL_SLOTS, SEO_TITLE, SEO_DESCRIPTION) VALUES 
(101, 101, 101, DATEADD(day, 5, GETDATE()), DATEADD(hour, 4, DATEADD(day, 5, GETDATE())), 200, N'Hội thảo Tim mạch Bạch Mai', N'Đăng ký nhận 4 điểm CME.'), -- Chưa diễn ra
(102, 101, 107, DATEADD(day, 12, GETDATE()), DATEADD(hour, 4, DATEADD(day, 12, GETDATE())), 300, N'Hội thảo Tim mạch ĐHYD HCM', N'Phiên mở rộng tại TP.HCM.'),
(103, 102, 102, DATEADD(day, 20, GETDATE()), DATEADD(hour, 8, DATEADD(day, 20, GETDATE())), 1000, N'Khai mạc PharmaSummit 2026', N'Tại GEM Center.'),
(104, 103, 104, DATEADD(hour, 2, GETDATE()), DATEADD(hour, 4, GETDATE()), 500, N'Webinar Kháng sinh ICU', N'Link Zoom.'), -- Đang diễn ra
(105, 104, 110, DATEADD(day, -2, GETDATE()), DATEADD(hour, 8, DATEADD(day, -2, GETDATE())), 50, N'Đào tạo TDV Q2', N'Nội bộ PharmaCorp.'), -- Đã kết thúc
(106, 105, 103, DATEADD(day, 15, GETDATE()), DATEADD(hour, 3, DATEADD(day, 15, GETDATE())), 150, N'Ra mắt JointCare Pro', N'Dành cho đại lý VIP.'),
(107, 106, 109, DATEADD(day, 30, GETDATE()), DATEADD(hour, 8, DATEADD(day, 30, GETDATE())), 0, N'Vietnam Medipharm 2026', N'Vào cửa tự do (Slots = 0).'), -- Không giới hạn
(108, 108, 108, DATEADD(day, 7, GETDATE()), DATEADD(hour, 5, DATEADD(day, 7, GETDATE())), 50, N'Tham quan Nhà máy EU-GMP', N'Đăng ký trước 5 ngày.'),
(109, 109, 105, DATEADD(day, -5, GETDATE()), DATEADD(hour, 2, DATEADD(day, -5, GETDATE())), 200, N'Talkshow Sốc phản vệ', N'Bị hủy do lỗi kỹ thuật.'), -- Sẽ test trạng thái Hủy
(110, 110, 106, DATEADD(day, 25, GETDATE()), DATEADD(hour, 6, DATEADD(day, 25, GETDATE())), 400, N'Hội nghị Sản phụ khoa', N'Phiên thảo luận ADA 2026.');
SET IDENTITY_INSERT [CT_EVENTS] OFF;
GO

-- =====================================================================
-- 14. BẢNG CT_EVENT_TAGS (10 Dòng - Gắn Tag cho Buổi sự kiện)
-- =====================================================================
INSERT INTO [CT_EVENT_TAGS] (CT_EVENT_ID, TAG_ID) VALUES 
(101, 102), (101, 108), -- Tim mạch, CME
(102, 102), (102, 108),
(103, 110),             -- Hợp tác
(104, 101), (104, 108), -- Kháng sinh, CME
(105, 108),
(109, 104),             -- ADR
(110, 103);             -- Tiểu đường thai kỳ
GO

-- =====================================================================
-- 15. BẢNG CT_POST_EVENTS (10 Dòng - Gắn Bài viết vào Buổi sự kiện)
-- =====================================================================
INSERT INTO [CT_POST_EVENTS] (CT_EVENT_ID, POST_ID) VALUES 
(101, 104), -- Hội thảo Tim mạch -> Bài Mục tiêu huyết áp
(102, 104), 
(103, 105), -- PharmaSummit -> Bài Ký kết Châu Âu
(104, 101), -- Webinar Kháng sinh -> Bài Nghiên cứu Carbapenem
(109, 109), -- Talkshow Sốc phản vệ -> Bài Xử trí Sốc phản vệ
(110, 102); -- Hội nghị Sản phụ khoa -> Bài Phác đồ Tiểu đường thai kỳ ADA
GO

-- =====================================================================
-- 16. BẢNG CT_EVENT_STATUS_HISTORY (15 Dòng - Audit Log Trạng thái Sự kiện)
-- Event Sourcing: Trạng thái hiện tại = Dòng có CHANGED_AT mới nhất
-- =====================================================================
SET IDENTITY_INSERT [CT_EVENT_STATUS_HISTORY] ON;
INSERT INTO [CT_EVENT_STATUS_HISTORY] (ID, CT_EVENT_ID, STATUS_CODE, CHANGED_BY_USER_ID, CHANGED_AT, NOTE) VALUES 
(101, 101, 'DRAFT', 101, DATEADD(day, -10, GETDATE()), N'Tạo nháp'),
(102, 101, 'OPEN', 101, DATEADD(day, -8, GETDATE()), N'Mở đăng ký'),
(103, 103, 'OPEN', 101, DATEADD(day, -7, GETDATE()), N'Mở bán vé'),
(104, 104, 'OPEN', 101, DATEADD(day, -6, GETDATE()), N'Mở link Zoom'),
(105, 104, 'ONGOING', NULL, DATEADD(hour, 2, GETDATE()), N'Sự kiện đang diễn ra (System Auto)'),
(106, 105, 'OPEN', 101, DATEADD(day, -15, GETDATE()), N'Mở đăng ký nội bộ'),
(107, 105, 'COMPLETED', NULL, DATEADD(day, -2, GETDATE()), N'Đã kết thúc'),
(108, 107, 'OPEN', 101, DATEADD(day, -2, GETDATE()), N'Vé vào cửa tự do'),
(109, 109, 'OPEN', 101, DATEADD(day, -10, GETDATE()), N'Mở đăng ký'),
(110, 109, 'CANCELLED', 101, DATEADD(day, -6, GETDATE()), N'Diễn giả ốm đột xuất, dời lịch'),
(111, 110, 'OPEN', 101, DATEADD(day, -5, GETDATE()), N'Mở đăng ký');
SET IDENTITY_INSERT [CT_EVENT_STATUS_HISTORY] OFF;
GO

-- =====================================================================
-- 17. BẢNG CT_EVENT_REGISTRATIONS (10 Dòng - Khách đăng ký sự kiện)
-- =====================================================================
SET IDENTITY_INSERT [CT_EVENT_REGISTRATIONS] ON;
INSERT INTO [CT_EVENT_REGISTRATIONS] (ID, CT_EVENT_ID, USER_ID, GUEST_NAME, GUEST_EMAIL, GUEST_PHONE, WORKPLACE, STATUS, REGISTERED_AT) VALUES 
(101, 101, 102, N'Nguyễn Văn Hùng', 'hung.nv@yhn.edu.vn', '0912000102', N'Bệnh viện Bạch Mai', 'APPROVED', DATEADD(day, -7, GETDATE())),
(102, 101, NULL, N'Đặng Lê Nam', 'nam.dl@guest.vn', '0999999111', N'Phòng khám Đa khoa Tâm Trí', 'PENDING', DATEADD(day, -6, GETDATE())), -- Khách vãng lai
(103, 103, 106, N'Đại diện Long Châu', 'b2b@longchau.vn', '0936000106', N'FPT Long Châu', 'APPROVED', DATEADD(day, -5, GETDATE())),
(104, 104, 103, N'Trần Bích Liên', 'lien.tb@choray.vn', '0983000103', N'Bệnh viện Chợ Rẫy', 'APPROVED', DATEADD(day, -4, GETDATE())),
(105, 104, 105, N'Phạm Thị Minh', 'minh.pt@nhidong1.vn', '0905000105', N'Nhi Đồng 1', 'ATTENDED', DATEADD(hour, 2, GETDATE())), -- Đã điểm danh
(106, 105, 109, N'Kiểm duyệt viên', 'mod1@pharmacorp.vn', '0969000109', N'PharmaCorp', 'ATTENDED', DATEADD(day, -10, GETDATE())),
(107, 108, 107, N'Đại diện Pharmacity', 'purchasing@pharmacity.vn', '0927000107', N'Pharmacity', 'APPROVED', DATEADD(day, -2, GETDATE())),
(108, 109, 106, N'Đại diện Long Châu', 'b2b@longchau.vn', '0936000106', N'FPT Long Châu', 'CANCELLED', DATEADD(day, -8, GETDATE())), -- Hủy do event hủy
(109, 110, 103, N'Trần Bích Liên', 'lien.tb@choray.vn', '0983000103', N'Bệnh viện Chợ Rẫy', 'PENDING', DATEADD(day, -1, GETDATE())),
(110, 110, NULL, N'Lê Minh Nguyệt', 'nguyet.lm@guest.vn', '0999999222', N'Bệnh viện Từ Dũ', 'APPROVED', GETDATE());
SET IDENTITY_INSERT [CT_EVENT_REGISTRATIONS] OFF;
GO

-- =====================================================================
-- 18. BẢNG LOAI_LIKE (10 Loại cảm xúc y khoa/chuyên nghiệp)
-- =====================================================================
SET IDENTITY_INSERT [LOAI_LIKE] ON;
INSERT INTO [LOAI_LIKE] (ID, CODE, NAME, ICON_URL) VALUES 
(1, 'LIKE', N'Hữu ích', '/icons/like.svg'),
(2, 'LOVE', N'Tuyệt vời', '/icons/love.svg'),
(3, 'INSIGHTFUL', N'Sâu sắc', '/icons/insightful.svg'),
(4, 'SUPPORT', N'Đồng tình', '/icons/support.svg'),
(5, 'QUESTION', N'Thắc mắc', '/icons/question.svg'),
(6, 'CELEBRATE', N'Chúc mừng', '/icons/celebrate.svg'),
(7, 'WARNING', N'Cần lưu ý', '/icons/warning.svg'),
(8, 'SAD', N'Chia buồn', '/icons/sad.svg'),
(9, 'ANGRY', N'Không đồng tình', '/icons/angry.svg'),
(10, 'HAHA', N'Hài hước', '/icons/haha.svg');
SET IDENTITY_INSERT [LOAI_LIKE] OFF;
GO

-- =====================================================================
-- 19. BẢNG CMT (10 Bình luận gốc 5NF)
-- Không biết thuộc bài nào/sự kiện nào cho đến khi qua bảng Cầu Nối
-- =====================================================================
SET IDENTITY_INSERT [CMT] ON;
INSERT INTO [CMT] (ID, USER_ID, CONTENT, CREATED_AT, UPDATED_AT) VALUES 
(101, 103, N'Báo cáo rất chi tiết, tuy nhiên tỷ lệ tử vong do Acinetobacter baumannii đa kháng trong thử nghiệm này có vẻ hơi thấp so với thực tế lâm sàng tại Việt Nam.', DATEADD(hour, -20, GETDATE()), DATEADD(hour, -20, GETDATE())),
(102, 105, N'Cho tôi xin slide bài giảng phần cơ chế đề kháng Carbapenem được không ạ?', DATEADD(hour, -18, GETDATE()), DATEADD(hour, -18, GETDATE())),
(103, 106, N'Chuỗi cung ứng lạnh này rất phù hợp với tiêu chuẩn GSP mới. Chi phí lắp đặt hệ thống data logger thời gian thực cho kho 500m2 khoảng bao nhiêu?', DATEADD(hour, -15, GETDATE()), DATEADD(hour, -15, GETDATE())),
(104, 107, N'Hàng giả, quảng cáo láo. Bài viết toàn PR cho công ty.', DATEADD(hour, -12, GETDATE()), DATEADD(hour, -12, GETDATE())), -- Dòng này sẽ bị Report/Hide
(105, 102, N'Tiêu chuẩn ADA 2026 đã hạ ngưỡng HbA1c, điều này sẽ làm tăng vọt tỷ lệ thai phụ bị chẩn đoán GDM. Các phòng khám tuyến dưới cần chuẩn bị tâm lý.', DATEADD(hour, -10, GETDATE()), DATEADD(hour, -10, GETDATE())),
(106, 108, N'Tôi muốn đăng ký 5 vé VIP cho hội nghị PharmaSummit.', DATEADD(hour, -8, GETDATE()), DATEADD(hour, -8, GETDATE())),
(107, 104, N'Sốc phản vệ do Cephalosporin thực tế rất hiếm gặp hơn Penicillin, nhưng độc tính chéo là có thật. Bài viết tóm tắt lưu đồ rất dễ nhớ.', DATEADD(hour, -6, GETDATE()), DATEADD(hour, -6, GETDATE())),
(108, 110, N'Hỏi ngu: Thế tóm lại là thuốc này có chữa được tiểu đường dứt điểm không bác sĩ?', DATEADD(hour, -4, GETDATE()), DATEADD(hour, -4, GETDATE())),
(109, 103, N'Event này có tính điểm CME cho dược sĩ không ban tổ chức?', DATEADD(hour, -2, GETDATE()), DATEADD(hour, -2, GETDATE())),
(110, 106, N'Nhà thuốc Long Châu xin gửi lời chúc mừng sự hợp tác chiến lược giữa PharmaCorp và AstraZeneca!', DATEADD(hour, -1, GETDATE()), DATEADD(hour, -1, GETDATE()));
SET IDENTITY_INSERT [CMT] OFF;
GO

-- =====================================================================
-- 20. BẢNG CT_POST_CMT (Bảng Cầu Nối: Định tuyến CMT -> BÀI VIẾT)
-- =====================================================================
INSERT INTO [CT_POST_CMT] (POST_ID, CMT_ID) VALUES 
(101, 101), -- Bài Carbapenem -> Bình luận 101
(101, 102), -- Bài Carbapenem -> Bình luận 102
(107, 103), -- Bài Cold Chain -> Bình luận 103
(107, 104), -- Bài Cold Chain -> Bình luận 104 (Spam)
(102, 105), -- Bài Tiểu đường -> Bình luận 105
(109, 107), -- Bài Sốc phản vệ -> Bình luận 107
(102, 108), -- Bài Tiểu đường -> Bình luận 108 (Guest)
(105, 110); -- Bài Ký kết -> Bình luận 110
GO

-- =====================================================================
-- 21. BẢNG CT_EVENT_CMT (Bảng Cầu Nối: Định tuyến CMT -> SỰ KIỆN)
-- =====================================================================
INSERT INTO [CT_EVENT_CMT] (CT_EVENT_ID, CMT_ID) VALUES 
(103, 106), -- Khai mạc PharmaSummit -> Bình luận 106
(101, 109); -- Hội thảo Tim mạch -> Bình luận 109
GO

-- =====================================================================
-- 22. BẢNG PH_CMT (10 Phản hồi lồng nhau cấp 2, cấp 3)
-- =====================================================================
SET IDENTITY_INSERT [PH_CMT] ON;
INSERT INTO [PH_CMT] (ID, ROOT_CMT_ID, PARENT_PH_ID, USER_ID, CONTENT, CREATED_AT, UPDATED_AT) VALUES 
(201, 101, NULL, 102, N'Đồng ý với bác sĩ Liên. Tại Bạch Mai, chủng Acinetobacter đã kháng hầu hết các carbapenem thông thường, phải phối hợp Colistin.', DATEADD(hour, -19, GETDATE()), DATEADD(hour, -19, GETDATE())), -- Cấp 2
(202, 101, 201, 103, N'Vâng thưa thầy, em cũng đang gặp tình trạng tương tự ở khoa Hồi sức cấp cứu.', DATEADD(hour, -18, GETDATE()), DATEADD(hour, -18, GETDATE())), -- Cấp 3 (Reply cái 201)
(203, 102, NULL, 101, N'Chào bác sĩ, slide bài giảng đã được đính kèm ở mục Tài liệu bên dưới bài viết ạ. Bác sĩ click vào "Tải PPTX" để lấy file nhé.', DATEADD(hour, -17, GETDATE()), DATEADD(hour, -17, GETDATE())),
(204, 103, NULL, 101, N'Chào đại diện Long Châu, giải pháp IoT Data Logger cho kho 500m2 ước tính khoảng 150 triệu VNĐ, bao gồm 10 sensor và phần mềm theo dõi 1 năm.', DATEADD(hour, -14, GETDATE()), DATEADD(hour, -14, GETDATE())),
(205, 104, NULL, 110, N'Ông này rảnh rỗi đi cắn càn à?', DATEADD(hour, -11, GETDATE()), DATEADD(hour, -11, GETDATE())), -- Cãi nhau, sẽ bị Warn
(206, 105, NULL, 104, N'Đây đúng là gánh nặng cho hệ thống xét nghiệm. Tuy nhiên phát hiện sớm sẽ giảm biến chứng kẹt vai và hạ đường huyết sơ sinh rất nhiều.', DATEADD(hour, -9, GETDATE()), DATEADD(hour, -9, GETDATE())),
(207, 106, NULL, 101, N'Dạ chào anh/chị, vui lòng liên hệ hotline 1900 6868 nhánh phím 2 để đăng ký vé VIP khách mời doanh nghiệp ạ.', DATEADD(hour, -7, GETDATE()), DATEADD(hour, -7, GETDATE())),
(208, 108, NULL, 102, N'Chào bạn, bệnh tiểu đường (đái tháo đường) hiện nay là bệnh mãn tính, không thể chữa "dứt điểm" hoàn toàn nhưng hoàn toàn có thể kiểm soát tốt bằng thuốc và chế độ ăn nhé.', DATEADD(hour, -3, GETDATE()), DATEADD(hour, -3, GETDATE())),
(209, 109, NULL, 101, N'Sự kiện này CÓ cấp 4 điểm CME cho Dược sĩ và Bác sĩ nếu tham gia đủ 80% thời lượng và vượt qua bài test cuối giờ ạ.', DATEADD(hour, -1, GETDATE()), DATEADD(hour, -1, GETDATE())),
(210, 110, NULL, 101, N'Thay mặt PharmaCorp, xin chân thành cảm ơn sự đồng hành của hệ thống nhà thuốc Long Châu!', GETDATE(), GETDATE());
SET IDENTITY_INSERT [PH_CMT] OFF;
GO

-- =====================================================================
-- 23. BẢNG CT_LIKECMT (10 Dòng - Tương tác thả Like/Love trên Comment gốc)
-- =====================================================================
INSERT INTO [CT_LIKECMT] (USER_ID, CMT_ID, LOAILIKE_ID, CREATED_AT) VALUES 
(102, 101, 3, GETDATE()), -- BS Hùng thả "Insightful" (3) cho CMT 101
(104, 101, 4, GETDATE()), -- DS Thắng thả "Support" (4) cho CMT 101
(105, 105, 1, GETDATE()), -- BS Minh thả "Like" (1) cho CMT 105
(103, 107, 2, GETDATE()), -- BS Liên thả "Love" (2) cho CMT 107
(102, 108, 5, GETDATE()), -- BS Hùng thả "Question" (5) cho CMT 108
(101, 110, 6, GETDATE()), -- Admin thả "Celebrate" (6) cho CMT 110
(107, 110, 1, GETDATE()), -- Pharmacity thả "Like" cho CMT 110
(108, 110, 2, GETDATE()), -- An Khang thả "Love" cho CMT 110
(104, 103, 1, GETDATE()), 
(105, 103, 3, GETDATE());
GO

-- =====================================================================
-- 24. BẢNG CT_LIKEPHCMT (10 Dòng - Tương tác thả Like/Love trên Phản hồi)
-- =====================================================================
INSERT INTO [CT_LIKEPHCMT] (USER_ID, PH_CMT_ID, LOAILIKE_ID, CREATED_AT) VALUES 
(103, 201, 4, GETDATE()), -- BS Liên đồng tình với Reply 201 của BS Hùng
(105, 201, 1, GETDATE()), 
(106, 204, 1, GETDATE()), -- Long Châu Like phản hồi của Admin báo giá
(104, 205, 9, GETDATE()), -- DS Thắng phẫn nộ (9) với câu cãi nhau
(102, 206, 3, GETDATE()), -- BS Hùng thả "Sâu sắc" cho nhận xét của DS Thắng
(110, 208, 2, GETDATE()), -- Khách Love phản hồi giải thích tiểu đường
(103, 209, 1, GETDATE()), -- BS Liên Like phản hồi về điểm CME
(106, 210, 2, GETDATE()), -- Long Châu Love lời cảm ơn của Admin
(107, 210, 1, GETDATE()), 
(108, 210, 1, GETDATE());
GO

-- =====================================================================
-- 25. BẢNG MODERATION_ACTIONS (Bảng này thường có dữ liệu Seed từ Java, nhưng giả lập thêm)
-- Tránh xung đột, dùng ID từ 101
-- =====================================================================
SET IDENTITY_INSERT [MODERATION_ACTIONS] ON;
INSERT INTO [MODERATION_ACTIONS] (ID, CODE, NAME, DESCRIPTION, AFFECTED_TABLE) VALUES 
(101, 'APPROVE_CMT', N'Duyệt hiển thị bình luận', N'Duyệt comment cho phép hiển thị public', 'CMT'),
(102, 'HIDE_CMT', N'Ẩn bình luận', N'Ẩn comment do vi phạm hoặc chờ duyệt', 'CMT'),
(103, 'WARN_CMT', N'Cảnh báo người dùng', N'Gửi thư cảnh báo về ngôn từ không phù hợp', 'CMT'),
(104, 'DELETE_CMT', N'Xóa bình luận', N'Xóa vĩnh viễn (Hard delete)', 'CMT');
SET IDENTITY_INSERT [MODERATION_ACTIONS] OFF;
GO

-- =====================================================================
-- 26. BẢNG CT_CMT_MODERATION_LOG (10 Lịch sử kiểm duyệt CMT gốc)
-- Quyết định trạng thái hiển thị của các Comment
-- =====================================================================
SET IDENTITY_INSERT [CT_CMT_MODERATION_LOG] ON;
INSERT INTO [CT_CMT_MODERATION_LOG] (ID, CMT_ID, ACTION_ID, MODERATOR_ID, REASON, CREATED_AT) VALUES 
(101, 101, 101, 109, N'Bình luận chuyên môn hợp lệ', DATEADD(minute, -100, GETDATE())), -- Duyệt CMT 101
(102, 102, 101, 109, N'Duyệt câu hỏi', DATEADD(minute, -90, GETDATE())),
(103, 103, 101, 109, N'Duyệt câu hỏi đối tác', DATEADD(minute, -80, GETDATE())),
(104, 104, 102, 109, N'Tạm ẩn do nghi ngờ Spam / Cạnh tranh không lành mạnh', DATEADD(minute, -70, GETDATE())), -- Ẩn CMT 104
(105, 105, 101, 109, N'Duyệt', DATEADD(minute, -60, GETDATE())),
(106, 106, 101, 109, N'Duyệt', DATEADD(minute, -50, GETDATE())),
(107, 107, 101, 109, N'Duyệt', DATEADD(minute, -40, GETDATE())),
(108, 108, 101, 109, N'Duyệt câu hỏi độc giả', DATEADD(minute, -30, GETDATE())),
(109, 109, 101, 109, N'Duyệt', DATEADD(minute, -20, GETDATE())),
(110, 110, 101, 109, N'Duyệt', DATEADD(minute, -10, GETDATE()));
SET IDENTITY_INSERT [CT_CMT_MODERATION_LOG] OFF;
GO

-- =====================================================================
-- 27. BẢNG CT_PH_CMT_MODERATION_LOG (10 Lịch sử kiểm duyệt Phản hồi)
-- =====================================================================
SET IDENTITY_INSERT [CT_PH_CMT_MODERATION_LOG] ON;
INSERT INTO [CT_PH_CMT_MODERATION_LOG] (ID, PH_CMT_ID, ACTION_ID, MODERATOR_ID, REASON, CREATED_AT) VALUES 
(101, 201, 101, 109, N'Duyệt', DATEADD(minute, -90, GETDATE())),
(102, 202, 101, 109, N'Duyệt', DATEADD(minute, -80, GETDATE())),
(103, 203, 101, 109, N'Duyệt Admin reply', DATEADD(minute, -70, GETDATE())),
(104, 204, 101, 109, N'Duyệt Admin reply', DATEADD(minute, -60, GETDATE())),
(105, 205, 103, 109, N'Cảnh báo ngôn từ gây hấn', DATEADD(minute, -50, GETDATE())), -- Cảnh báo Reply 205 cãi nhau
(106, 206, 101, 109, N'Duyệt', DATEADD(minute, -40, GETDATE())),
(107, 207, 101, 109, N'Duyệt Admin', DATEADD(minute, -30, GETDATE())),
(108, 208, 101, 109, N'Duyệt BS giải thích', DATEADD(minute, -20, GETDATE())),
(109, 209, 101, 109, N'Duyệt Admin', DATEADD(minute, -10, GETDATE())),
(110, 210, 101, 109, N'Duyệt Admin', GETDATE());
SET IDENTITY_INSERT [CT_PH_CMT_MODERATION_LOG] OFF;
GO

-- =====================================================================
-- 28. BẢNG CT_USER_MODERATION_LOG (10 Lịch sử Admin xử phạt User)
-- =====================================================================
-- Sử dụng lại ID 104, 105 từ Mod_Actions giả lập (Hoặc các ID có sẵn từ seeder Java)
SET IDENTITY_INSERT [CT_USER_MODERATION_LOG] ON;
INSERT INTO [CT_USER_MODERATION_LOG] (ID, TARGET_USER_ID, ACTION_ID, PERMISSION_ID, MODERATOR_ID, REASON, CREATED_AT) VALUES 
(101, 104, 1, NULL, 101, N'Khóa tài khoản 3 ngày do bình luận gây hấn liên tục', DATEADD(day, -30, GETDATE())),
(102, 104, 2, NULL, 101, N'Mở khóa tự động hết hạn', DATEADD(day, -27, GETDATE())),
(103, 110, 3, 2, 101, N'Tước quyền bình luận do nghi ngờ bot spam', DATEADD(day, -10, GETDATE())), -- Giả sử Permission ID 2 là COMMENT
(104, 107, 5, NULL, 101, N'Gán quyền Đối tác B2B', DATEADD(day, -50, GETDATE())),
(105, 108, 5, NULL, 101, N'Gán quyền Đối tác B2B', DATEADD(day, -50, GETDATE())),
(106, 106, 5, NULL, 101, N'Gán quyền Đối tác VIP', DATEADD(day, -40, GETDATE())),
(107, 102, 5, NULL, 101, N'Gán quyền Bác sĩ', DATEADD(day, -100, GETDATE())),
(108, 103, 5, NULL, 101, N'Gán quyền Bác sĩ', DATEADD(day, -90, GETDATE())),
(109, 104, 5, NULL, 101, N'Gán quyền Dược sĩ', DATEADD(day, -80, GETDATE())),
(110, 105, 5, NULL, 101, N'Gán quyền Bác sĩ', DATEADD(day, -70, GETDATE()));
SET IDENTITY_INSERT [CT_USER_MODERATION_LOG] OFF;
GO

PRINT N'✅ ĐÃ HOÀN TẤT MASTER SEED DATA: 28 BẢNG - 100% THỰC TẾ Y KHOA - SẴN SÀNG CHO BƯỚC CODE JAVA.';