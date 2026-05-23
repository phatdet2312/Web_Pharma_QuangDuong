#!/usr/bin/env bash
# Hook: PostToolUse khớp ^(apply_patch|Edit|Write|MultiEdit)$
# Auto compile/lint sau khi file code bị sửa.
#
# Multi-language: Java (Maven/Gradle), TypeScript, JavaScript, Python (mypy/ruff),
# Go, Rust, C# (.NET), Kotlin, Swift, Dart, Makefile fallback.
#
# Trả PostToolUse output để Codex biết kết quả:
#   - Pass: { "continue": true }
#   - Fail: { "decision": "block", "reason": "<error>",
#             "hookSpecificOutput": {"hookEventName":"PostToolUse",
#                                    "additionalContext":"<error tail>"} }
set -euo pipefail

has_jq=0
if command -v jq >/dev/null 2>&1; then
  has_jq=1
fi

input=$(cat)
if [ "$has_jq" -eq 1 ]; then
  file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
  patch_text=$(echo "$input" | jq -r '.tool_input.input // .tool_input.patch // .tool_input.command // ""')
else
  # Fallback parser for simple Codex hook JSON when jq is unavailable.
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
  patch_text="$input"
fi

extract_patch_paths() {
  printf '%s\n' "$patch_text" \
    | grep -oE '\*\*\* (Update|Add|Delete) File: [^\\"]+' 2>/dev/null \
    | sed -E 's/^\*\*\* (Update|Add|Delete) File: //'
}

paths=()
if [ -n "$file_path" ]; then
  paths+=("$file_path")
else
  while IFS= read -r p; do
    [ -n "$p" ] && paths+=("$p")
  done < <(extract_patch_paths)
fi
[ "${#paths[@]}" -eq 0 ] && { echo '{"continue": true}'; exit 0; }

emit_fail() {
  local reason="$1"
  local ctx="$2"
  if [ "$has_jq" -eq 1 ]; then
    jq -nc --arg reason "$reason" --arg ctx "$ctx" '{
      decision: "block",
      reason: $reason,
      hookSpecificOutput: {
        hookEventName: "PostToolUse",
        additionalContext: $ctx
      }
    }'
  else
    printf '%s\n' '{"decision":"block","reason":"Typecheck/compile FAIL. Install jq for detailed escaped output.","hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":"Typecheck/compile command failed; jq unavailable so details were not embedded."}}'
  fi
}

run_check() {
  local cmd="$1"
  local result rc tmp
  tmp=$(mktemp 2>/dev/null || printf '.codex-typecheck-%s.tmp' "$$")
  set +e
  eval "$cmd" >"$tmp" 2>&1
  rc=$?
  set -e
  result=$(tail -30 "$tmp" 2>/dev/null || true)
  rm -f "$tmp" 2>/dev/null || true
  if [ "$rc" -ne 0 ]; then
    emit_fail "Typecheck/compile FAIL ($ext)" "$result"
    exit 0
  fi
}

make_checked=0
for file_path in "${paths[@]}"; do
  ext="${file_path##*.}"
  case "$ext" in
    java)
      [ -f "pom.xml" ]         && run_check "mvn compile -q"
      [ -f "build.gradle" ]    && run_check "gradle compileJava -q"
      [ -f "build.gradle.kts" ]&& run_check "gradle compileJava -q"
      ;;
    ts|tsx)
      [ -f "tsconfig.json" ]   && run_check "npx tsc --noEmit"
      ;;
    js|jsx)
      if [ -f "package.json" ] && grep -q '"lint"' package.json 2>/dev/null; then
        run_check "npm run lint --silent"
      fi
      ;;
    py)
      if command -v mypy >/dev/null 2>&1 && { [ -f "mypy.ini" ] || [ -f "pyproject.toml" ]; }; then
        run_check "mypy \"$file_path\""
      elif command -v ruff >/dev/null 2>&1; then
        run_check "ruff check \"$file_path\""
      fi
      ;;
    go)
      [ -f "go.mod" ]          && run_check "go build ./..."
      ;;
    rs)
      [ -f "Cargo.toml" ]      && run_check "cargo check"
      ;;
    cs)
      if ls *.csproj >/dev/null 2>&1 || ls *.sln >/dev/null 2>&1; then
        run_check "dotnet build --no-restore -q"
      fi
      ;;
    kt|kts)
      [ -f "build.gradle" ]    && run_check "gradle compileKotlin -q"
      [ -f "build.gradle.kts" ]&& run_check "gradle compileKotlin -q"
      ;;
    swift)
      [ -f "Package.swift" ]   && run_check "swift build"
      ;;
    dart)
      [ -f "pubspec.yaml" ]    && run_check "dart analyze \"$file_path\""
      ;;
    *)
      # === Fallback: Makefile check/lint target ===
      # Dùng khi extension không khớp case ở trên hoặc dự án chỉ có Makefile (vd C/C++, Elixir, mixed-stack).
      # Port từ Claude post-edit-typecheck.sh dòng 128-141.
      if [ "$make_checked" -eq 0 ] && [ -f "Makefile" ]; then
        make_checked=1
        if grep -q '^check:' Makefile 2>/dev/null; then
          run_check "make check"
        elif grep -q '^lint:' Makefile 2>/dev/null; then
          run_check "make lint"
        fi
      fi
      ;;
  esac
done

echo '{"continue": true}'
exit 0
