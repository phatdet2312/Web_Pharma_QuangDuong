# Learnings — Bài học từ sai lầm
> Last updated: 2026-05-28
> Tổng entries: 15
> Entries sẵn sàng promote (Lần >= 3): 3

<!-- File này là nơi agent ghi nhận PATTERN sai lầm đã mắc -->
<!-- Mỗi entry là 1 bài học tổng quát, KHÔNG phải bug cụ thể -->
<!-- Khi Lần >= 3 → cân nhắc /promote-learning để đưa lên rules enforced -->
<!-- Agent đọc file này TRƯỚC khi bắt đầu code để tránh lặp sai lầm cũ -->

| Ngày | Loại | Pattern (sai → đúng) | Lần |
|------|------|---------------------|-----|
| 2026-05-19 | CONVENTION | Sai: biến yêu cầu dữ liệu thật/tự nhiên thành seed công thức, đánh số và copy template để đạt số lượng. Đúng: LUÔN coi yêu cầu dữ liệu mẫu chuyên nghiệp là yêu cầu nghiệp vụ, phải ưu tiên tính chân thật/ngữ cảnh/không copy-paste trước khi tối ưu số lượng. | 3 |
| 2026-05-19 | ARCHITECTURE | Sai: vá dần một artifact đã sai nền tảng thay vì dừng và thiết kế lại từ ràng buộc nghiệp vụ. Đúng: khi user xác nhận sai toàn bộ, PHẢI rollback artifact sai trước, rồi tái thiết kế chiến lược trước khi viết tiếp. | 1 |
| 2026-05-20 | CONVENTION | Sai: audit dữ liệu lớn nhưng báo cáo cuối quá ngắn, không đưa đủ bảng bằng chứng/định lượng theo yêu cầu user. Đúng: LUÔN trả hoặc tạo báo cáo chi tiết có severity, bằng chứng, số liệu, trạng thái đã sửa/chưa sửa khi user yêu cầu kiểm tra toàn diện. | 1 |
| 2026-05-21 | CONVENTION | Sai: khi user yêu cầu sửa lỗi artifact, chỉ sửa báo cáo/mapping rồi bàn giao như đã xử lý. Đúng: LUÔN sửa trực tiếp artifact chính trước, báo cáo chỉ là phụ trợ và phải nói rõ phần nào chưa sửa. | 1 |
| 2026-05-22 | LOGIC | Sai: validator dữ liệu mẫu kết luận PASS khi chỉ bắt duplicate exact/đủ số lượng, bỏ qua similarity nội dung, URL runtime truy xuất được, phân phối bảng cha-con và mốc thời gian nghiệp vụ. Đúng: LUÔN kiểm tra cả chất lượng ngữ nghĩa, khả năng render runtime và quan hệ nghiệp vụ trước khi kết luận dataset đạt. | 1 |
| 2026-05-22 | CONVENTION | Sai: sửa dataset bằng cách tối ưu validator và sinh text qua pipeline chưa kiểm tra encoding/readability, chèn cả câu meta nội bộ như nguồn internet/giữ cấu trúc vào nội dung người đọc thấy. Đúng: LUÔN kiểm tra UTF-8, preview mẫu người đọc được và dùng văn phong nghiệp vụ chuyên nghiệp, không nhồi phụ lục nhân tạo chỉ để qua validator. | 4 |
| 2026-05-22 | LOGIC | Sai: validator SQL seed báo PASS nhưng không kiểm tra đủ constraint schema thật như độ dài cột, `NOT NULL` và nullable, dẫn đến lỗi runtime khi insert. Đúng: LUÔN validate literal theo `CREATE TABLE`/DB schema trước khi bàn giao file seed. | 2 |
| 2026-05-23 | LOGIC | Sai: biến cơ chế tag/mention người được trả lời thành nhãn hiển thị bị backend/frontend ép gắn ngoài nội dung. Đúng: LUÔN phân biệt mention do người dùng chỉnh được trong ô nhập với metadata hỗ trợ hiển thị; chỉ ép nhãn khi nghiệp vụ có bảng mention riêng và user không yêu cầu quyền xóa tag. | 1 |
| 2026-05-23 | FRONTEND | Sai: cải tiến luồng UI đang hoạt động nhưng chỉ kiểm tra compile/parse, không kiểm tra đủ trạng thái render thực tế như tag, nút mở/ẩn và chuỗi `onclick`, dẫn đến regression trải nghiệm. Đúng: LUÔN kiểm tra cả cú pháp, trạng thái DOM chính và visual affordance của control trước khi bàn giao UI tương tác. | 3 |
| 2026-05-23 | LOGIC | Sai: mở rộng mô hình cây nhiều tầng nhưng không audit lại toàn bộ vòng đời create/update/delete theo ràng buộc FK tự tham chiếu. Đúng: LUÔN kiểm tra thứ tự xóa con-trước-cha, dữ liệu vệ tinh, bulk action và trạng thái UI sau success khi đổi cấu trúc cây hoặc lazy-load. | 1 |
| 2026-05-26 | CONVENTION | Sai: gọi planner cho task phức tạp nhưng bỏ qua bước persist kế hoạch điều phối trước khi implement. Đúng: LUÔN ghi `.ai-memory/04_active_plan.md` với mục tiêu, phạm vi, rủi ro và tiêu chí nghiệm thu trước mọi thay đổi code sau planner. | 1 |
| 2026-05-26 | CONVENTION | Sai: nhồi nhiều câu lệnh JS/HTML dài trên cùng một dòng để giao diện chạy tạm, làm code khó đọc và trái style hiện có. Đúng: LUÔN format template/script theo cấu trúc rõ ràng, mỗi nhánh xử lý có dòng riêng, ưu tiên readability trước khi bàn giao. | 1 |
| 2026-05-26 | CONVENTION | Sai: sửa code theo hướng chạy được nhưng chưa đối chiếu rõ với file điều kiện code riêng của dự án trước khi bàn giao. Đúng: LUÔN audit thay đổi lớn với checklist convention bắt buộc của dự án như ghi chú, cấu trúc OOP, không dùng pattern bị cấm và khả năng tái sử dụng trước khi kết luận đạt. | 2 |
| 2026-05-26 | FRONTEND | Sai: làm form quản trị media bằng cách bắt người dùng nhập thủ công đường dẫn file/ảnh thay vì thiết kế luồng chọn hoặc upload phù hợp khi public. Đúng: LUÔN kiểm tra nghiệp vụ nhập liệu thực tế cho admin UI, dùng upload/file picker/API quản lý media thay vì bắt nhập path nội bộ. | 1 |
| 2026-05-26 | LOGIC | Sai: đánh đồng hai bản ghi khác nhau có nội dung giống nhau với lỗi render lặp cùng một bản ghi, rồi đề xuất dedupe chống chế. Đúng: LUÔN phân biệt duplicate dữ liệu nghiệp vụ với duplicate render; nếu cùng record lặp nhiều lần thì truy root cause ở query/state/render, không che bằng lớp lọc phụ. | 1 |

<!-- Loại: LOGIC | CONVENTION | ARCHITECTURE | PERFORMANCE | SECURITY -->
<!-- Pattern: "Sai: [pattern sai]. Đúng: LUÔN/KHÔNG BAO GIỜ [rule]" -->
<!-- Lần >= 3 → ⚠️ candidate promote -->
