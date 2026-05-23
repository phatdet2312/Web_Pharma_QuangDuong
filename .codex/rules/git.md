# Git Conventions

> File chi tiết — Codex đọc khi user yêu cầu commit hoặc khi nested AGENTS.md trỏ tới.

## Commit message
Format: `[type]: mô tả ngắn`

| Type     | Khi dùng                                 | Ví dụ                                |
|----------|-------------------------------------------|--------------------------------------|
| feat     | Tính năng mới                            | `feat: thêm API đăng ký user`        |
| fix      | Sửa bug                                  | `fix: sửa null pointer khi login`    |
| refactor | Đổi cấu trúc code, không đổi behavior    | `refactor: tách UserService`         |
| docs     | Tài liệu                                 | `docs: cập nhật README`              |
| test     | Thêm/sửa test                            | `test: thêm test cho AuthService`    |
| chore    | Việc lặt vặt (build, config, dependency) | `chore: nâng Spring lên 3.3.5`       |
| perf     | Tối ưu performance                       | `perf: cache user lookup`            |
| style    | Format code, không đổi logic             | `style: format theo prettier`        |

## Quy tắc commit
- Mô tả ngắn ≤ 72 ký tự
- Tiếng Việt OK, viết rõ ràng "vì sao" hơn là "làm gì"
- 1 commit = 1 logical change (không gom nhiều việc khác nhau)
- Nếu có breaking change → thêm `BREAKING CHANGE: ...` ở body

## Branch
- Feature branch: `feat/<tên-ngắn>` (vd: `feat/user-registration`)
- Bug fix: `fix/<tên-ngắn>` (vd: `fix/login-npe`)
- Release: `release/x.y.z`

## Không bao giờ
- `git push --force` lên `main`/`master`/`develop`
- `git reset --hard` khi chưa commit work-in-progress
- Commit `.env`, `node_modules/`, file build, secret
- Skip pre-commit hook (`--no-verify`) trừ khi có lý do rõ ràng và được user đồng ý
