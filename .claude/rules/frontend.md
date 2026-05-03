---
paths:
  - "src/main/resources/templates/**"
  - "src/main/resources/static/**"
  - "frontend/**/*.{js,jsx,ts,tsx,vue,html,css}"
---
 
# Frontend Rules
 
- Component: PascalCase (UserProfile.jsx)
- Hook: camelCase prefix "use" (useAuth, useFetchData)
- CSS class: kebab-case hoặc module (user-card, styles.userCard)
- Escape output để chống XSS
 