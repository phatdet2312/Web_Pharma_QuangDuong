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
---
 
# Frontend Rules
 
- Component: PascalCase (UserProfile.jsx, UserCard.vue)
- Hook/composable: camelCase prefix "use" (useAuth, useFetchData)
- CSS class: kebab-case hoặc module (user-card, styles.userCard)
- Escape output để chống XSS
 
# ví dụ: Java Frontend Rules
 
- Component: PascalCase (UserProfile.jsx)
- Hook: camelCase prefix "use" (useAuth, useFetchData)
- CSS class: kebab-case hoặc module (user-card, styles.userCard)
- Escape output để chống XSS
 