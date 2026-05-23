---
paths:
  - "**/entity/**"
  - "**/entities/**"
  - "**/model/**"
  - "**/models/**"
  - "**/repository/**"
  - "**/repositories/**"
  - "**/dao/**"
  - "**/store/**"
  - "**/stores/**"
  - "**/migration/**"
  - "**/migrations/**"
  - "**/schema/**"
  - "**/db/**"
  - "**/*.sql"
  - "**/*.xcdatamodeld/**"
  - "**/RealmModels/**"
---

> File chi tiết — claude đọc khi nested `CLAUDE.md` trỏ tới đây.

> ⚠️ **DISCLAIMER (đọc TRƯỚC khi áp dụng bất kỳ rule nào)**
> 1. Đây là **baseline TEMPLATE đa nền tảng** — KHÔNG phải convention bắt buộc của dự án
> 2. **Convention cụ thể** trong `.ai-memory/01_system_architecture.md` (section "Project Convention") **LUÔN ƯU TIÊN HƠN** rule trong file này
> 3. Mỗi section per-framework chỉ là CHECKLIST tham khảo — **KHÔNG tự suggest refactor dự án legacy** chỉ vì rule khác code hiện có
> 4. Nếu rule mâu thuẫn với code đã được team chấp nhận → giữ code, BỎ QUA rule

> 🚀 **CƠ CHẾ SKIP — tiết kiệm token (BẮT BUỘC tuân thủ)**
> 1. **BƯỚC 1**: Đọc `.ai-memory/01_system_architecture.md` section "Project Convention" → IDENTIFY tech stack thật của dự án (vd: `"React + TypeScript"`, `"Flutter"`, `"Angular + RxJS"`)
> 2. **BƯỚC 2**: Trong file này, **CHỈ load chi tiết** section khớp stack đã identify → các section còn lại CHỈ skim tiêu đề rồi **BỎ QUA hoàn toàn** (không nạp content vào context)
> 3. **BƯỚC 3**: Luôn load 3 section UNIVERSAL: `Quy tắc chung` + `Accessibility (a11y)` + `Test` (áp dụng mọi stack)
> 4. **Khi NÀO load TOÀN BỘ file** (KHÔNG skip):
>    - Memory chưa bootstrap (`Project Convention` trống hoặc chứa `PENDING_BOOTSTRAP`)
>    - Stack của dự án KHÔNG nằm trong file này (vd. Qwik, Lit, htmx) → load tất cả để fallback tìm pattern gần nhất
>    - User EXPLICIT yêu cầu "đọc toàn bộ rule frontend" hoặc tương đương
> 5. **Tự kiểm tra trước khi suggest**: "Rule tôi sắp suggest có phù hợp với stack ghi trong memory không?" — nếu KHÔNG → bỏ qua, không nêu


# Database Rules (chi tiết — đa nền tảng)

> Mỗi section per-framework có nhãn `[Áp dụng nếu dự án dùng X]` — agent dùng nhãn này để quyết định load chi tiết hay skip.

## Quy tắc chung (mọi loại data store)
- Naming convention theo "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Mỗi Entity/Model một Repository/DAO/Store riêng
- Migration BẮT BUỘC có rollback (nếu tool support)
- Index cho FK + column thường query (SQL) / field thường filter (NoSQL)
- KHÔNG truy vấn raw có user input — dùng PreparedStatement / ORM parameter binding / parameterized query
- KHÔNG store password plaintext — bcrypt/argon2 (cost factor ≥ 12)
- KHÔNG log câu query chứa data nhạy cảm (password, token, PII)
- Quyền connection user database tối thiểu (principle of least privilege)

## SQL — PostgreSQL / MySQL / MariaDB / SQLite (server) — [Áp dụng nếu dự án dùng SQL phổ biến]
- Table: snake_case, số nhiều (`users`, `order_items`)
- Column: snake_case (`created_at`, `user_id`)
- Primary key: `id` (BIGSERIAL / BIGINT AUTO_INCREMENT / UUID — chọn 1 nhất quán toàn dự án)
- Foreign key: `[bảng_tham_chiếu]_id` (vd: `user_id`, `post_id`)
- Timestamp chuẩn: `created_at`, `updated_at`, optional `deleted_at` cho soft delete
- KHÔNG dùng `SELECT *` trong production code — list column rõ
- JOIN phải có index trên column join
- Transaction cho multi-step write (BEGIN/COMMIT/ROLLBACK)
- Constraint ở DB level (NOT NULL, UNIQUE, CHECK) — không tin tưởng 100% vào validate ở app

## SQL — MSSQL / Oracle / DB2 (enterprise) — [Áp dụng nếu dự án enterprise]
- Naming theo convention dự án (PascalCase Table thường gặp ở MSSQL legacy — giữ nếu đã có)
- Stored procedure cho logic nghiệp vụ phức tạp lặp lại — KHÔNG đẩy hết logic vào app
- Schema/Owner rõ (`dbo.Users`, `HR.Employees`)
- Sequence (Oracle) hoặc IDENTITY (MSSQL) cho auto-increment
- Hint optimizer (`/*+ INDEX */`) chỉ dùng khi đo được có lợi, KHÔNG rải

## NoSQL document — MongoDB / Firestore / CouchDB — [Áp dụng nếu dự án dùng document DB]
- Collection: tên số nhiều, camelCase hoặc snake_case theo platform (Firestore mặc định camelCase)
- Embed vs reference: embed cho data 1-1 hoặc 1-few, reference cho 1-many lớn (>100 con) hoặc data dùng độc lập
- Index cho field thường query/sort — không index thừa (mỗi index tốn write performance)
- TTL index cho data tạm (session, cache, OTP)
- Aggregation pipeline thay vì query rồi xử lý trong app (MongoDB)
- Firestore: cẩn thận với security rules — test rules trước deploy

## NoSQL key-value / wide-column — Redis / DynamoDB / Cassandra — [Áp dụng nếu dự án dùng KV/wide-column]
- Redis (cache/session): TTL cho mọi key trừ khi cố ý persist — tránh leak memory
- Redis key naming: `<domain>:<entity>:<id>` (vd: `user:profile:123`) — dễ debug + scan
- DynamoDB: thiết kế partition key kỹ — hot partition là nguyên nhân #1 fail scale
- DynamoDB: dùng GSI thay vì scan; nhớ giới hạn 20 GSI/table
- Cassandra: query-driven schema (thiết kế table theo query, không theo entity) — KHÔNG normalize như SQL

## Mobile local DB — CoreData (iOS) / Realm / Room (Android) / SQLite mobile — [Áp dụng nếu dự án mobile có offline storage]
- CoreData (iOS): NSManagedObjectContext thread-affinity — KHÔNG chia sẻ context giữa thread, dùng `perform`/`performAndWait`
- CoreData migration: lightweight migration cho thay đổi đơn giản, mapping model cho phức tạp — TEST migration với data thật
- Realm: object live-update theo thread — KHÔNG pass Realm object qua thread, dùng `freeze()` hoặc primary key
- Room (Android): DAO trong thread riêng (Coroutine `suspend` hoặc RxJava) — KHÔNG query main thread
- Room migration: viết Migration class + test với `MigrationTestHelper`
- SQLite mobile raw: dùng wrapper (FMDB iOS / GRDB / SQLDelight) thay vì raw cursor — tránh leak
- Encryption: SQLCipher hoặc Realm encryption cho data nhạy cảm
- KHÔNG store credential trong local DB — dùng Keychain (iOS) / EncryptedSharedPreferences (Android)

## Cache layer — Redis / Memcached / in-memory — [Áp dụng nếu dự án có cache]
- Cache-aside pattern: app đọc cache → miss thì đọc DB → set cache
- TTL phù hợp dữ liệu: short cho data động (1-5 phút), long cho data tĩnh (1-24h)
- Cache invalidation: theo event (DB update) hoặc TTL — chọn 1 chiến lược nhất quán
- KHÔNG cache data nhạy cảm chưa encrypt
- Stampede protection: lock hoặc probabilistic early expiration cho key hot

## Vector DB — Pinecone / Weaviate / Qdrant / pgvector — [Áp dụng nếu dự án có AI/semantic search]
- Embedding model nhất quán (đổi model = phải re-index toàn bộ)
- Metadata filtering: index metadata field cần filter trước query vector
- Dimension đúng với model (OpenAI ada-002 = 1536, sentence-transformers = 384/768)
- pgvector: dùng HNSW index cho dataset lớn, IVFFlat cho vừa
- Backup: vector + metadata + embedding model version

## Migration (mọi data store hỗ trợ)
- Mỗi migration file 1 thay đổi atomic
- Đặt tên: timestamp prefix + mô tả (`20260516_add_user_role.sql`, `V1.2.3__add_user_role.sql`)
- Up + Down (rollback) đầy đủ
- KHÔNG xoá column trực tiếp ở migration — deprecate trước (mark unused), xoá sau 1-2 release
- Backfill dữ liệu lớn (>100k row): chạy job riêng, KHÔNG trong migration
- Test migration trên copy production data trước khi deploy production thật
- Schema migration ≠ data migration — tách 2 loại

## Performance (mọi data store)
- N+1 query: dùng JOIN / `Include` / `populate` / batch fetch / DataLoader (GraphQL)
- Pagination cho list endpoint (page+size hoặc cursor) — KHÔNG bao giờ trả unlimited list
- Cache layer cho query đắt + ít đổi
- EXPLAIN/ANALYZE query chậm trước khi tối ưu — KHÔNG đoán
- Connection pool: cấu hình min/max theo expected load, theo dõi exhaustion
- Đo trước khi tối ưu — premature optimization là root of much evil

## Security (mọi data store)
- KHÔNG store password plaintext — bcrypt/argon2 (cost factor ≥ 12) hoặc Argon2id
- KHÔNG log câu query có data nhạy cảm
- Quyền connection user database tối thiểu (DBA cho migration tool, app user CHỈ CRUD bảng cần)
- Encrypt at rest cho data nhạy cảm (TDE / field-level encryption)
- Encrypt in transit BẮT BUỘC (TLS) — kể cả internal network
- Backup encryption + test restore định kỳ
- Audit log cho hành động write trên bảng nhạy cảm (PII, financial, medical)
