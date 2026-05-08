# System Architecture
> Last updated: YYYY-MM-DD
> Status: PENDING_BOOTSTRAP (Agent sẽ tự điền khi chạy lần đầu)
 
## Tech Stack
<!-- Agent điền khi bootstrap, đọc từ build config (pom.xml, package.json, go.mod, Cargo.toml, requirements.txt, *.csproj...) -->
 
| Layer      | Technology     | Version |
|------------|---------------|---------|
| Language   |               |         |
| Framework  |               |         |
| Database   |               |         |
| Cache      |               |         |
| Auth       |               |         |
| Frontend   |               |         |
| Build Tool |               |         |
 
## Project Type
<!-- Agent điền khi bootstrap. Xác định từ cấu trúc dự án + dependencies -->
<!-- Giá trị: web-backend, web-fullstack, mobile, desktop, cli, game, data-science, library, monorepo, embedded, other -->
<!-- Agent đọc field này để biết agent/skill nào phù hợp, agent nào bỏ qua -->
- **Type:** 
- **Đặc điểm:** <!-- VD: "có REST API + PostgreSQL", "iOS app + CoreData", "ML pipeline + FastAPI serving" -->

## Kiến trúc tổng quan
<!-- Monolith / Microservice / Modular Monolith -->
<!-- Mô tả ngắn về luồng request chính -->
 
## Ports & Endpoints
<!-- Agent điền từ app config (.env, application.yml, appsettings.json, config.toml...) / docker-compose.yml -->
 
| Service        | Port  | Base URL       |
|----------------|-------|----------------|
| Backend API    |       |                |
| Frontend       |       |                |
| Database       |       |                |
 
## External Dependencies
<!-- Các service bên ngoài: payment gateway, email service, storage... -->
 
## Cách chạy dự án
<!-- Agent điền khi bootstrap, kiểm tra Makefile / scripts / README gốc -->
```bash
# Lệnh build
# Lệnh chạy dev
# Lệnh chạy test
# Lệnh lint/typecheck
```
 
## Project Convention
<!-- QUAN TRỌNG: Agent điền khi bootstrap. Đây là nguồn sự thật cho TẤT CẢ agent -->
<!-- Mọi agent đọc section này thay vì đoán convention -->
 
### Kiến trúc phân lớp
<!-- VD Java: Controller → Service → Repository -->
<!-- VD Python: Router → Service → Repository -->
<!-- VD Go: Handler → Service → Store -->
<!-- VD Node: Controller → Service → Model -->
 
### Naming Convention
<!-- VD Java: camelCase method, PascalCase class -->
<!-- VD Python: snake_case all, PascalCase class -->
<!-- VD Go: exported PascalCase, unexported camelCase -->
 
### Error Handling
<!-- VD: Custom Exception + GlobalExceptionHandler -->
<!-- VD: Result/Either pattern -->
<!-- VD: error wrapping + middleware -->
 
### Test Framework & Pattern
<!-- VD: JUnit 5 + Mockito, AAA pattern, should_X_when_Y naming -->
<!-- VD: pytest + fixtures, test_X_when_Y naming -->
<!-- VD: go test + testify, TestX_when_Y naming -->
 
### Validation
<!-- VD: @Valid + DTO annotation -->
<!-- VD: pydantic model -->
<!-- VD: struct tags + validator -->
 
### Code Doc Style
<!-- VD: Javadoc (@param, @return, @throws) -->
<!-- VD: docstring (Google style / NumPy style) -->
<!-- VD: godoc (comment trên function) -->
 
## Architecture Decisions
<!-- Quyết định CROSS-CUTTING ảnh hưởng toàn dự án -->
<!-- VD: auth strategy, response format, error handling approach -->
<!-- CHỈ ghi khi có 2+ phương án được cân nhắc, KHÔNG ghi convention -->
 
| Quyết định | Phương án (✅ chọn / ❌ bỏ) | Lý do |
|-----------|---------------------------|-------|
|           |                           |       |
 
## Ghi chú quan trọng
<!-- Những điều đặc biệt agent cần biết về dự án này -->
<!-- VD: "Cần chạy Docker trước khi start", "Port 8080 đã bị chiếm bởi X" -->