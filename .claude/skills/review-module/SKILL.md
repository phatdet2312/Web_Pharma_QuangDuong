---
name: review-module
description: >
  Review toàn diện 1 module/feature. Dùng mỗi khi user yêu cầu review,
  kiểm tra chất lượng, audit code, hoặc nhắc đến "review", "kiểm tra",
  "code quality", "có ổn không", "xem lại".
---
 
# Quy trình Review Module
 
## Bước 1: Xác định phạm vi
- Đọc `.ai-memory/03_deep_knowledge/` liên quan
- List tất cả file thuộc module
## Bước 2: Checklist
 
### Security (CRITICAL)
- [ ] Hardcoded credential?
- [ ] SQL injection (raw query)?
- [ ] XSS (unescaped output)?
- [ ] Missing auth check?
- [ ] Sensitive data trong log?
### Logic (HIGH)
- [ ] Null pointer risk?
- [ ] Edge case chưa handle?
- [ ] Exception handling đúng?
- [ ] Business rule đúng?
### Performance (MEDIUM)
- [ ] N+1 query?
- [ ] Missing index / pagination?
- [ ] Unnecessary loop?
### Convention (LOW)
- [ ] Naming convention?
- [ ] Method > 30 dòng?
- [ ] Dead code / duplication?
## Bước 3: Báo cáo
| # | Severity | File:Line | Issue | Suggestion |
|---|----------|-----------|-------|------------|
 