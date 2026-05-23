#!/usr/bin/env bash
# post-edit-typecheck.sh — Auto compile/lint after file edits.
# Works with: PostToolUse (Edit|Write|MultiEdit)
# Output: JSON stdout, always exit 0. Compatible Bash 3.2+.
# Multi-language: Java, TypeScript, JavaScript, Python, Go, Rust, C#, Kotlin, Swift, Dart, Makefile.
set -euo pipefail

has_jq=0
command -v jq >/dev/null 2>&1 && has_jq=1

input=$(cat)
if [ "$has_jq" -eq 1 ]; then
  file_path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
else
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
fi

[ -z "$file_path" ] && { echo '{"continue": true}'; exit 0; }

ext="${file_path##*.}"

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
    printf '{"decision":"block","reason":"Typecheck/compile FAIL. Install jq for details.","hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":"compile/lint command failed"}}\n'
  fi
}

run_check() {
  local cmd="$1"
  local result rc
  local tmp
  tmp=$(mktemp 2>/dev/null || printf '/tmp/.claude-typecheck-%s.tmp' "$$")
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

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-.}"

case "$ext" in
  java)
    [ -f "$PROJECT_DIR/pom.xml" ]          && run_check "cd \"$PROJECT_DIR\" && mvn compile -q"
    [ -f "$PROJECT_DIR/build.gradle" ]     && run_check "cd \"$PROJECT_DIR\" && gradle compileJava -q"
    [ -f "$PROJECT_DIR/build.gradle.kts" ] && run_check "cd \"$PROJECT_DIR\" && gradle compileJava -q"
    ;;
  ts|tsx)
    [ -f "$PROJECT_DIR/tsconfig.json" ]    && run_check "cd \"$PROJECT_DIR\" && npx tsc --noEmit"
    ;;
  js|jsx)
    if [ -f "$PROJECT_DIR/package.json" ] && grep -q '"lint"' "$PROJECT_DIR/package.json" 2>/dev/null; then
      run_check "cd \"$PROJECT_DIR\" && npm run lint --silent"
    fi
    ;;
  py)
    if command -v mypy >/dev/null 2>&1; then
      if [ -f "$PROJECT_DIR/mypy.ini" ] || [ -f "$PROJECT_DIR/pyproject.toml" ]; then
        run_check "cd \"$PROJECT_DIR\" && mypy \"$file_path\""
      fi
    elif command -v ruff >/dev/null 2>&1; then
      run_check "cd \"$PROJECT_DIR\" && ruff check \"$file_path\""
    fi
    ;;
  go)
    [ -f "$PROJECT_DIR/go.mod" ]           && run_check "cd \"$PROJECT_DIR\" && go build ./..."
    ;;
  rs)
    [ -f "$PROJECT_DIR/Cargo.toml" ]       && run_check "cd \"$PROJECT_DIR\" && cargo check"
    ;;
  cs)
    if ls "$PROJECT_DIR"/*.csproj >/dev/null 2>&1 || ls "$PROJECT_DIR"/*.sln >/dev/null 2>&1; then
      run_check "cd \"$PROJECT_DIR\" && dotnet build --no-restore -q"
    fi
    ;;
  kt|kts)
    [ -f "$PROJECT_DIR/build.gradle" ]     && run_check "cd \"$PROJECT_DIR\" && gradle compileKotlin -q"
    [ -f "$PROJECT_DIR/build.gradle.kts" ] && run_check "cd \"$PROJECT_DIR\" && gradle compileKotlin -q"
    ;;
  swift)
    [ -f "$PROJECT_DIR/Package.swift" ]    && run_check "cd \"$PROJECT_DIR\" && swift build"
    ;;
  dart)
    [ -f "$PROJECT_DIR/pubspec.yaml" ]     && run_check "cd \"$PROJECT_DIR\" && dart analyze \"$file_path\""
    ;;
  *)
    if [ -f "$PROJECT_DIR/Makefile" ]; then
      if grep -q '^check:' "$PROJECT_DIR/Makefile" 2>/dev/null; then
        run_check "cd \"$PROJECT_DIR\" && make check"
      elif grep -q '^lint:' "$PROJECT_DIR/Makefile" 2>/dev/null; then
        run_check "cd \"$PROJECT_DIR\" && make lint"
      fi
    fi
    ;;
esac

echo '{"continue": true}'
exit 0
