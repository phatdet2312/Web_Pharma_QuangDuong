#!/usr/bin/env bash
# post-edit-typecheck.sh — Chạy compiler/linter sau mỗi file edit
# Exit 0 = pass, Exit khác 0 = Claude phải sửa lỗi trước khi tiếp tục
set -euo pipefail
 
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-.}"
 
# Detect project type và chạy check phù hợp
if [ -f "$PROJECT_DIR/pom.xml" ]; then
  # Java Maven project — compile check
  cd "$PROJECT_DIR"
  mvn compile -q 2>&1 | tail -20
  exit ${PIPESTATUS[0]}
 
elif [ -f "$PROJECT_DIR/build.gradle" ] || [ -f "$PROJECT_DIR/build.gradle.kts" ]; then
  # Java Gradle project
  cd "$PROJECT_DIR"
  gradle compileJava -q 2>&1 | tail -20
  exit ${PIPESTATUS[0]}
 
elif [ -f "$PROJECT_DIR/tsconfig.json" ]; then
  # TypeScript project — type check
  cd "$PROJECT_DIR"
  npx tsc --noEmit 2>&1 | tail -20
  exit ${PIPESTATUS[0]}
 
elif [ -f "$PROJECT_DIR/package.json" ]; then
  # Node.js project — lint nếu có script lint
  cd "$PROJECT_DIR"
  if grep -q '"lint"' package.json 2>/dev/null; then
    npm run lint --silent 2>&1 | tail -20
    exit ${PIPESTATUS[0]}
  fi
fi
 
# Không detect được project type → pass
exit 0
 