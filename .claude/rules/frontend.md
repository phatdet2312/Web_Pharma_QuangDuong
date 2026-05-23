---
paths:
  - "frontend/**"
  - "client/**"
  - "web/**"
  - "app/**/*.{jsx,tsx,vue,svelte}"
  - "pages/**"
  - "components/**"
  - "templates/**"
  - "static/**"
  - "public/**"
  - "**/*.html"
  - "**/*.css"
  - "**/*.scss"
  - "ios/**"
  - "android/**"
  - "lib/**/*.dart"
  - "**/*.swift"
  - "**/*.kt"
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

# Frontend Rules (chi tiết — đa nền tảng)
> Mỗi section per-framework có nhãn `[Áp dụng nếu dự án dùng X]` — agent dùng nhãn này để quyết định load chi tiết hay skip.


## Quy tắc chung (mọi framework)
- Component/Widget/View: PascalCase (`UserProfile`, `UserCard`)
- Hook/composable/service: camelCase prefix theo convention framework (`useAuth`, `fetchData`)
- CSS class web: kebab-case hoặc CSS module (`user-card`, `styles.userCard`)
- Escape output để chống XSS — KHÔNG render raw HTML/string từ user input chưa sanitize
- KHÔNG hardcode endpoint/key — dùng biến môi trường qua build-time inject hoặc config file
- Tách logic UI (presentational) khỏi logic data fetching / state — testable hơn
- Naming theo `01_system_architecture.md` nếu Project Convention quy định khác

## React (web) — [Áp dụng nếu dự án dùng React/Next.js/Remix]
- Function component + hooks (KHÔNG class component trừ khi rất đặc biệt)
- Custom hook tách logic dùng lại
- Tránh prop drilling sâu → dùng Context API hoặc state management library theo dự án
- Memoization (`useMemo`, `useCallback`) chỉ khi đo được có lợi
- Key prop trong list phải stable (KHÔNG dùng index trừ list bất biến)
- KHÔNG `dangerouslySetInnerHTML` trừ khi đã sanitize (DOMPurify hoặc tương đương)

## Vue (web) — [Áp dụng nếu dự án dùng Vue 2/3/Nuxt]
- Composition API (Vue 3) cho code mới
- `<script setup>` ưu tiên
- Props validation đầy đủ (type, required, default)
- `emits` khai báo rõ
- `v-html` chỉ với content đã sanitize

## Angular (web) — [Áp dụng nếu dự án dùng Angular 14+]
- Standalone component (Angular 14+) ưu tiên — giảm NgModule boilerplate
- Reactive Form > Template-driven Form cho form phức tạp
- RxJS: unsubscribe đầy đủ (`takeUntilDestroyed` hoặc async pipe trong template)
- Service injectable scope đúng (`providedIn: 'root'` cho singleton)
- ChangeDetectionStrategy.OnPush cho component đọc-nhiều, sửa-ít → tối ưu render
- KHÔNG bypass `DomSanitizer.bypassSecurityTrustHtml` trừ khi đã sanitize trước

## Svelte / SolidJS / Qwik / Astro / khác — [Theo framework đang dùng]
- Theo convention community + check `.ai-memory/01_system_architecture.md`
- Reactivity rules đặc thù từng framework — đừng áp pattern React/Vue

## Server-side rendering (Thymeleaf/Razor/Blade/EJS/Pug/JSP) — [Áp dụng nếu dự án J2EE/.NET/PHP/Rails/Express SSR]
- Auto-escape mặc định BẬT — chỉ disable khi BẮT BUỘC và đã sanitize trước
- Tách layout (master/_Layout/base.html) — KHÔNG copy header/footer mỗi page
- Partial / fragment cho component tái sử dụng (Thymeleaf `th:fragment`, Razor `_Partial`, Blade `@include`)
- CSRF token trong mọi form POST (framework thường tự inject)
- Tránh logic phức tạp trong template — đẩy về controller/service
- i18n key thay vì hardcode chuỗi tiếng Việt/Anh trong template (nếu dự án đa ngôn ngữ)

## Mobile — React Native — [Áp dụng nếu dự án dùng React Native/Expo]
- Function component + hooks (cùng convention React web)
- Platform-specific code: tách file `.ios.tsx` / `.android.tsx` thay vì `if (Platform.OS)` rải rác
- Image: dùng `react-native-fast-image` cho image lưới lớn (memory tốt hơn)
- Navigation: React Navigation v6+ — tách stack/tab/drawer rõ
- Async storage: KHÔNG store secret raw — dùng `react-native-keychain` cho credential
- KHÔNG dùng `dangerouslySetInnerHTML` tương đương trong WebView

## Mobile — Flutter — [Áp dụng nếu dự án dùng Flutter/Dart]
- Widget: PascalCase, tách stateful/stateless rõ ràng
- `const` constructor mọi khi có thể → giảm rebuild
- State management theo dự án (Provider / Riverpod / Bloc / GetX) — đừng trộn 2-3 loại
- Tách business logic khỏi Widget → testable
- KHÔNG block UI thread — `async/await` cho I/O
- Secret: dùng `flutter_secure_storage`, KHÔNG `SharedPreferences`

## Mobile native iOS — SwiftUI / UIKit — [Áp dụng nếu dự án iOS Swift]
- SwiftUI cho code mới (iOS 14+) — UIKit chỉ khi cần API chưa có trong SwiftUI
- View là struct (value type) — KHÔNG mutate trực tiếp
- `@State` cho state cục bộ, `@StateObject` cho ViewModel, `@ObservedObject` cho injected
- Async work: Swift Concurrency (`async/await`) > Combine > GCD
- Secret: Keychain Services, KHÔNG `UserDefaults`
- ATS (App Transport Security): giữ default (HTTPS only), exception phải có lý do rõ

## Mobile native Android — Jetpack Compose / XML — [Áp dụng nếu dự án Android Kotlin/Java]
- Jetpack Compose cho code mới — View XML chỉ legacy
- Composable: PascalCase, hoisting state lên (stateless composable testable hơn)
- ViewModel survive configuration change — đừng giữ Context trong ViewModel
- Coroutine cho async (`viewModelScope.launch`) — KHÔNG `AsyncTask` (deprecated)
- Secret: EncryptedSharedPreferences hoặc Android Keystore, KHÔNG SharedPreferences raw
- Proguard/R8 rules cho release build — kiểm tra obfuscation không break

## Desktop — Electron / Tauri / Qt — [Áp dụng nếu dự án desktop]
- Electron: context isolation BẬT, nodeIntegration TẮT, preload script làm bridge
- Tauri: ưu tiên hơn Electron cho dự án mới (Rust backend, binary nhỏ, secure default)
- IPC payload validate cả 2 chiều (renderer ↔ main)
- Auto-update: dùng official updater, ký code (code signing) BẮT BUỘC release

## Style (web) — [Áp dụng cho frontend web]
- CSS module / Tailwind / styled-component / vanilla-extract theo dự án (xem memory)
- Mobile-first responsive (`min-width` media query)
- Color/spacing dùng design token, KHÔNG hardcode hex/px rải rác
- Dark mode: dùng CSS variable hoặc Tailwind `dark:` thay vì if-else trong JS

## Accessibility (a11y) — [Áp dụng cho UI có người dùng]
- Web: `<button>` cho action, `<a>` cho navigation; `alt` cho `<img>`, `aria-label` cho icon-only button
- Mobile: accessibility label cho mọi tap target; minimum tap target 44x44pt (iOS) / 48dp (Android)
- Keyboard navigation (web): tab order hợp lý, focus visible
- Contrast ratio: WCAG AA tối thiểu (4.5:1 cho text thường)
- Screen reader test: VoiceOver (iOS), TalkBack (Android), NVDA/JAWS (web)

## Test
- Web component: React Testing Library / Vue Test Utils / Testing Library tương ứng / Angular TestBed
- Mobile: Jest + React Native Testing Library / `flutter_test` / XCTest / Espresso + Compose Test
- Test behavior, KHÔNG test implementation detail
- E2E web: Playwright / Cypress
- E2E mobile: Detox (RN) / `integration_test` (Flutter) / XCUITest / Espresso
- Snapshot test thận trọng — dễ bị "approve mù" khi UI thay đổi hợp lệ
