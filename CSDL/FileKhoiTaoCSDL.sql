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

-- Bảng PERMISSION_MODULES: Danh mục nhóm chức năng để phân loại quyền hạt lựu.
-- Admin quản lý tập trung: thêm/sửa/xóa module tại 1 chỗ, frontend load dropdown từ API.
-- [QUAN HỆ]: 1-N với PERMISSIONS.
CREATE TABLE [PERMISSION_MODULES](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính module
	MODULE_CODE VARCHAR(50) NOT NULL UNIQUE,     -- Mã module hệ thống (VD: 'POST', 'EVENT', 'COMMENT', 'SYSTEM')
	MODULE_NAME NVARCHAR(100) NOT NULL,          -- Tên hiển thị (VD: 'Bài viết', 'Sự kiện', 'Bình luận', 'Hệ thống')
	DESCRIPTION NVARCHAR(255),                   -- Mô tả chi tiết module
	DISPLAY_ORDER INT DEFAULT 0                  -- Thứ tự hiển thị trên giao diện (số nhỏ hiện trước)
);

-- Bảng PERMISSIONS: Danh sách các quyền hạn thao tác cực nhỏ (Hạt lựu - Granular Permissions)
-- [QUAN HỆ]: N-1 với PERMISSION_MODULES, 1-N với CT_ROLE_PERMISSIONS, 1-N với CT_USER_PERMISSION_BLACKLIST.
CREATE TABLE [PERMISSIONS](
	ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,   -- Khóa chính quyền hạn thao tác
	PERMISSION_CODE VARCHAR(50) NOT NULL UNIQUE, -- Mã thao tác (VD: 'POST_CREATE', 'EVENT_DELETE')
	DESCRIPTION NVARCHAR(255),                   -- Mô tả: 'Quyền tạo bài viết mới'
	MODULE_ID INT,                               -- Nhóm chức năng (FK tới PERMISSION_MODULES, nullable cho quyền chưa phân nhóm)
	FOREIGN KEY (MODULE_ID) REFERENCES PERMISSION_MODULES(ID)
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
	VERIFICATION_STATUS VARCHAR(50) DEFAULT 'PENDING', -- Trạng thái duyệt hồ sơ (PENDING, VERIFIED, REJECTED)
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
	IS_FEATURED BIT NOT NULL DEFAULT 0,          -- Cờ nổi bật: 1 = Hiển thị trang chủ, 0 = Bình thường
	SEO_TITLE NVARCHAR(200),                     -- Thẻ Title SEO (Ghi đè tiêu đề gốc để tối ưu từ khóa)
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
	SEO_TITLE NVARCHAR(200),                     -- Meta Title để chạy Ads/SEO Local theo tỉnh thành
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
	STATUS VARCHAR(50),                          -- Trạng thái vé (VD: 'APPROVED', 'CANCELLED', 'ATTENDED')
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



