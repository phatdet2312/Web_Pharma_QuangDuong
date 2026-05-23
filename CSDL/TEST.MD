# KHỞI ĐẦU: 
- cấm tuyệt đối tạo file .py với mục đích đối phó yêu cầu, làm kiểu lắp liếm random hoặc dùng để ghép câu từ lặp dữ liệu để làm cho xonh mà không chân thật đúng thực tế, không theo yêu càu phía dưới
- đầu tiên dây là cuộc trò chuyện mới KHÔNG  chạy code nằm ngoài phạm quản lý của fodel này (D:\J2EE\Antigravity\Web_Pharma_QuangDuong) như: tự khởi chạy csdl, tự động cập nhật dữ liệu,......... tuyệt đối không chạy lệnh nào ngoài fodel này
-  không dùng dữ liệu session phân tích cũ để dùng cho yêu cầu này, phải thực hiện yêu cầu từ đầu.
# ỨNG DỤNG ĐANG DÙNG
 1: CODE: VISUAL CODE
 2: CSDL: SQL server management studio (ssms)

 # Ngôn ngữ dùng
  1: code: java
  2: csdl: tự đọc file /CSDL/FileKhoiTaoCSDL.sql để xác định

# FILE QUAN TRỌNG
 1: /CSDL/TIN HẠN DÙNG SẢN PHẨM_20260413.xlsx -> thông tin các dòng sản phẩm thuốc thử hiện có của phía công ty.
 2: /CSDL/FileKhoiTaoCSDL.sql -> FILE KHỞI TẠO CSDL TOÀN HỆ THỐNG
 3: /CSDL/DuLieuMau.sql -> file duy nhất được phép ghi dữ liệu cũng là file chứa dữ liệu cho yêu cầu này.

# YÊU CẦU: 
Web_Pharma_QuangDuong LÀ WEB ĐANG PHÁT TIỂN, TUY NHIÊN CHƯA CÓ DỮ LIỆU ĐỂ TEST CHỨC NĂNG TÔI CẦN BẠN GIÚP TÔI TẠO 1 BỘ DỮ LIỆU ĐỂ TEST CHỨC NĂNG VÀ PHÁT TRIỂN VỀ SAU (VIẾT CODE KHỞI TẠO DỮ LIỆU VÀO /CSDL/DuLieuMau.sql Tôi sẽ tự mở file bằng SQL server management studio (ssms) để chạy, đảm bảo không sinh lỗi nào khi chạy cho tôi dù 1 lỗi cũng ko được sinh ra, nhớ lấy ) NHƯ SAU:
 1: ĐỌC HIỂU: CHUYÊN SÂU BACKEND + frontend +FileKhoiTaoCSDL.sql chuyên sâu để hiểu mói liên kết của 3 tầng. 
 2: dữ liệu viết ra: 
  - không đạo văn, chân thật về dữ liệu như thao tác bình tường ủa 1 user
  - không bịa code và không bịa số liệu
  - mọi số liệu (nếu có) đều phải làn guồn chính thống
 3: phần cmt quy trình tạo dữ liệu mẫu: 
  - 3 bảng cha bao gồm: User, event, post. tất cả các bảng khác  thông tin mẫu  đều phụ thuộc hoặc là con của 3 bảng này. User là bảng đại diện cho người dùng, event là bảng đại diện cho sự kiện, post là bảng đại diện cho bài viết. 
  - user: ít nhất 500 user: tạo thông tin user phải chân thật user đại diện cho người hành động hức năng trên hệ thống.
  - post: 300 post, mỗi post ít nhất 1000 từ, sử dụng html để phát huy tối đa sức mạnh của giao diện đã thiét kế. mỗi bài viết là độc nhất không được copy nội dung của các bài viết khác (sử dụng AI để tạo nội dung, đảm bảo không bịa đặt và không sao chép trái phép, nội dung phải tham khảo các nguồn uy tín -> rồi mới được tạo, nhưng nội dung phải là do AI tạo ra để không đạo văn.)
  - event: 100 event, mỗi event phải thực tế để khớp với 300 bài viết.
  - thời gian: thời gian phải từ năm 2023 - 20/05/2026 riêng event thì phải có thêm event của tương lai đến 30/07/2026, các thời gian khác trong từng cột của mỗi bảng phải đảm bảo đúng thực tế đến tugừ mili giây không coppy pass thời gian.
  - Các bảng con: cần tạo dữ liệu cho các bảng con để đảm bảo tính chân thật khi hiển thị. ví dụ: user_event: khi user thực hiện hành động tham gia event thì bảng user_event sẽ được cập nhật, hoặc bài viết và event đều cần có thông tin về lượt xem và lượt thích, v.v...  phải đảm bảo tính thực tế và chân thật. (đọc kỹ sơ đồ CSDL và bảng để hiểu mói liên kết)
  - các bảng con phải đảm bảo phù hợp 3 bảng chính để thể hiện đươc tính chân thật khi hiển thị và không vô lý với thực tế, nó phải thể hiện được từng bước của 1 user đang thao tác chức năng trên hệ thống.
  - các bảng con phải có thông tin đầy đủ không sót thông tin của bảng con nào nếu thiếu sẽ sót chức năng.
  - bộ dữ liệu ảo phải đảm bảo mô phỏng được hành động của từng user: như cmt, đăng kí tham gia sự kiện, like bài viết, chia sẻ bài viết, tải bài viết, vi hạm, báo cáo v.v... để khi xem trong db sẽ thấy dữ liệu rất chân thật.
  - tyệt đối không coppy pass dù là 1 dòng dữ liệu, mỗi dữ liệu phải là đọc nhất dù là khác hay cùng bản.
  - ở databaseseend.java có 1 số bảng có dữ liệu khởi tạo tự động khi server chạy, các dữ liệu này liên quan mật thiết đến sự vận hành cho toàn bộ trang web. vì thế các bảng nào liên quan với databaseseend.java tuyệt đối không được đụng vào không tạo mới dữ liệu, không thêm xoá sửa dữ liệu, đặt biệt là nhóm bảng phân quyền và quyền hạt lựu không được thêm dữ liệu vào vì đây là quyền admin tôi sẽ tự làm sau, chỉ được tạo user và ghi log và các profile ngoài ra không làm gì các bảng này nửa. (đọc kĩ file databaseseend.java để hiểu).
  - các bảng con khi tạo lưu ý cho tôi các trạng thái để phù hợp với backend và thực tế.

  4: MỘT SỐ LƯU Ý: 
  - dữ liệu không dưới 250.000 dòng dữ liệu cho tát cả các bảng cộng lại, và dữ liệu phải theo mô hình chuông mô hình phân phối dữ liệu theo chuông, (đọc kĩ về mô hình chuông để hiểu) đảm bảo tính chân thật khi hiển thị và không vô lý với thực tế.
  - sử dụng các từ ngữ chân thật, đời thường để viết mã SQL, cmt trong SQL phải có ý nghĩa và mô tả được hành động đang được thực hiện. và cmt phải bằng tiếng Việt có dấu.
  - file /CSDL/DuLieuMau.sql phải chứa toàn bộ mã SQL để tạo dữ liệu mẫu, đảm bảo không sinh lỗi khi chạy cho tôi dù 1 lỗi cũng ko được sinh ra.
  - trong sql không viết hàm để random mà phải là từng dòng dữ liệu được viết tay nhưng tuân theo quy luật.
  - mỗi bộ dữ liệu của mỗi bảng chỉ cần 1 dòng INSERT INTO không cần lặp lại insert nhiều lần, sau đó hàng trăm hàng ngàn dữ liệu mẫu sẽ ở phí sau, mỗi đầu trước hi insert into thì có print mô tả để dễ qản lý.
  5: quy tắt bàn gia0:
   -> nếu có bất kì điều  kiện nào không thoả thì làm lại từ đầu, xoay vòng cho đến khi thoả.
  
  