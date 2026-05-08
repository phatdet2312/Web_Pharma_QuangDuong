#!/usr/bin/env bash
# post-edit-typecheck.sh — Chạy compiler/linter sau mỗi file edit
# Exit 0 = pass, Exit khác 0 = Claude phải sửa lỗi trước khi tiếp tục
# CHỈ chạy khi file vừa sửa là file code (không compile khi sửa .md, .xml, .yml...)
 
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-.}"
 
# Lấy đường dẫn file vừa sửa từ stdin (JSON input từ hook)
input=$(cat)
file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
 
# Xác định extension của file vừa sửa
ext="${file_path##*.}"
 
# === JAVA (Maven / Gradle) ===
if [[ "$ext" == "java" ]]; then
  if [ -f "$PROJECT_DIR/pom.xml" ]; then
    cd "$PROJECT_DIR"
    result=$(mvn compile -q 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  elif [ -f "$PROJECT_DIR/build.gradle" ] || [ -f "$PROJECT_DIR/build.gradle.kts" ]; then
    cd "$PROJECT_DIR"
    result=$(gradle compileJava -q 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === TypeScript ===
elif [[ "$ext" == "ts" || "$ext" == "tsx" ]]; then
  if [ -f "$PROJECT_DIR/tsconfig.json" ]; then
    cd "$PROJECT_DIR"
    result=$(npx tsc --noEmit 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === JavaScript (lint nếu có) ===
elif [[ "$ext" == "js" || "$ext" == "jsx" ]]; then
  if [ -f "$PROJECT_DIR/package.json" ]; then
    cd "$PROJECT_DIR"
    if grep -q '"lint"' package.json 2>/dev/null; then
      result=$(npm run lint --silent 2>&1 | tail -20)
      rc=$?
      [ $rc -ne 0 ] && echo "$result" >&2
      exit $rc
    fi
  fi
 
# === Python (mypy nếu có, ruff/flake8 nếu có) ===
elif [[ "$ext" == "py" ]]; then
  cd "$PROJECT_DIR"
  if command -v mypy &>/dev/null && [ -f "mypy.ini" ] || [ -f "pyproject.toml" ]; then
    result=$(mypy "$file_path" 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  elif command -v ruff &>/dev/null; then
    result=$(ruff check "$file_path" 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === Go ===
elif [[ "$ext" == "go" ]]; then
  if [ -f "$PROJECT_DIR/go.mod" ]; then
    cd "$PROJECT_DIR"
    result=$(go build ./... 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === Rust ===
elif [[ "$ext" == "rs" ]]; then
  if [ -f "$PROJECT_DIR/Cargo.toml" ]; then
    cd "$PROJECT_DIR"
    result=$(cargo check 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === C# (.NET) ===
elif [[ "$ext" == "cs" ]]; then
  if ls "$PROJECT_DIR"/*.csproj &>/dev/null || ls "$PROJECT_DIR"/*.sln &>/dev/null; then
    cd "$PROJECT_DIR"
    result=$(dotnet build --no-restore -q 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === Kotlin ===
elif [[ "$ext" == "kt" || "$ext" == "kts" ]]; then
  if [ -f "$PROJECT_DIR/build.gradle" ] || [ -f "$PROJECT_DIR/build.gradle.kts" ]; then
    cd "$PROJECT_DIR"
    result=$(gradle compileKotlin -q 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === Swift ===
elif [[ "$ext" == "swift" ]]; then
  if [ -f "$PROJECT_DIR/Package.swift" ]; then
    cd "$PROJECT_DIR"
    result=$(swift build 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === Dart / Flutter ===
elif [[ "$ext" == "dart" ]]; then
  if [ -f "$PROJECT_DIR/pubspec.yaml" ]; then
    cd "$PROJECT_DIR"
    result=$(dart analyze "$file_path" 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
 
# === Fallback: Makefile check/lint target ===
elif [ -f "$PROJECT_DIR/Makefile" ]; then
  cd "$PROJECT_DIR"
  if grep -q '^check:' Makefile 2>/dev/null; then
    result=$(make check 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  elif grep -q '^lint:' Makefile 2>/dev/null; then
    result=$(make lint 2>&1 | tail -20)
    rc=$?
    [ $rc -ne 0 ] && echo "$result" >&2
    exit $rc
  fi
fi
 
# File không phải code (.md, .xml, .yml, .json, .sh...) → skip
exit 0
 