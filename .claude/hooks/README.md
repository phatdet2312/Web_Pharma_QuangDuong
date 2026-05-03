# .claude/hooks/ — Lifecycle automation (deterministic)
 
## Mục đích
Hooks là shell script chạy TỰ ĐỘNG tại các thời điểm cụ thể trong workflow của Claude.
Khác với rules/skills (dựa vào AI hiểu ý), hooks là code xác định — không hallucinate.
 
## Cấu trúc
```
.claude/hooks/
├── README.md                  ← File này
├── pre-bash-firewall.sh       ← TRƯỚC Bash: chặn lệnh nguy hiểm
├── pre-edit-protect.sh        ← TRƯỚC Edit: bảo vệ file quan trọng
├── post-edit-log.sh           ← SAU Edit: ghi log mọi file đã sửa
└── post-edit-typecheck.sh     ← SAU Edit: auto compile/lint
```
 
## Chi tiết từng hook
 
| Hook | Thời điểm | Chức năng | Exit code |
|------|----------|-----------|-----------|
| `pre-bash-firewall.sh` | TRƯỚC khi chạy Bash | Chặn rm -rf, git reset --hard, sudo, drop table, curl\|bash... | 0=cho phép, 2=chặn |
| `pre-edit-protect.sh` | TRƯỚC khi sửa file | Bảo vệ .env, Dockerfile, settings.json, core_rules.xml | 0=cho phép, 2=chặn |
| `post-edit-log.sh` | SAU khi sửa file | Ghi timestamp + tool + filepath vào `.claude/edit-history.log` | 0 (luôn pass) |
| `post-edit-typecheck.sh` | SAU khi sửa file | Tự detect loại dự án và chạy compile/lint | 0=pass, khác 0=Claude phải sửa |
 
## Cách hoạt động
 
- Hooks được cấu hình trong `.claude/settings.json` → mục `hooks`
- `PreToolUse`: chạy TRƯỚC khi tool execute → có thể CHẶN hành động
- `PostToolUse`: chạy SAU khi tool execute → kiểm tra kết quả
- Input: JSON qua stdin chứa thông tin tool (command, file_path...)
- Output: exit code quyết định hành vi
## Typecheck auto-detect
 
`post-edit-typecheck.sh` tự detect loại dự án và chạy lệnh phù hợp:
- `pom.xml` có → chạy `mvn compile`
- `build.gradle` có → chạy `gradle compileJava`
- `tsconfig.json` có → chạy `npx tsc --noEmit`
- `package.json` có script lint → chạy `npm run lint`
- Không detect được → pass (exit 0)
## Tùy chỉnh
 
- **Thêm hook mới**: tạo file `.sh` + cấu hình trong `settings.json`
- **Thêm file bảo vệ**: sửa array `protected_files` trong `pre-edit-protect.sh`
- **Thêm pattern nguy hiểm**: sửa array `deny_patterns` trong `pre-bash-firewall.sh`
- **Sửa typecheck command**: sửa `post-edit-typecheck.sh` cho khớp build tool dự án
- **Quan trọng**: sau khi copy, chạy `chmod +x .claude/hooks/*.sh`
 