---
name: admin-comments-rewrite
description: Gap analysis va ke hoach chuyen admin/comments.html tu demo tinh sang trang quan tri binh luan hoat dong day du
metadata:
  type: project
---

## Context
Task chuyen admin/comments.html (968 dong demo tinh) thanh trang quan tri binh luan hoat dong.
Pattern tham chieu: admin/events.html da duoc rewrite truoc do.

**Why:** User yeu cau khai thac toi da 15 bang comment/moderation/reaction/report trong CSDL.
**How to apply:** Backend gap phai bo sung truoc khi frontend bat dau; frontend chi goi API, khong biet DB.

## API Inventory (da co)
- Admin comment: stats, search, pending, moderate, bulk moderate, bulk delete, cmt delete, reply delete
- Admin report: list reports, resolve report, report history
- Audit: moderation-actions list
- Public (reuse cho admin): reply lazy-load, edit history, reaction types

## Gap xac dinh 2026-05-28
1. KHONG co endpoint admin xem moderation log cho tung CMT/PH_CMT (repo co findByCmtIdOrderByCreatedAtDesc nhung chua expose qua controller)
2. KHONG co endpoint admin xem action log cho tung CMT/PH_CMT (public endpoint co nhung can check auth)
3. Admin xem replies cua 1 comment: chua co admin endpoint, co the reuse public endpoint
4. Admin search chua ho tro filter theo REPORTED status (chi filter theo moderation status HIDE/WARN/APPROVE/PENDING)
