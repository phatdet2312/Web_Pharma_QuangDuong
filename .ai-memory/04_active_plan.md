
# Active Plan
> Last updated: 2026-06-04
> Status: DONE_WITH_VERIFY_BLOCKED

## Completed

- Refactor `ultraSecureLibrary` so core no longer owns Pharma RBAC details (`permissions`, `roleLevel`, `blacklist`, `PermissionRegistry`, `RequirePermission`).
- Add typed neutral security snapshot model: `SecurityAuthoritySnapshot`, `SecurityTokenClaim`, `SecurityTokenVersion`, `SecurityTokenConstants`.
- Move app-specific RBAC packing to `config/rbac/RbacSecuritySnapshot`.
- Preserve dynamic RBAC behavior through `UserSecurityAdapter.layAnhChupBaoMat()`, neutral JWT authorities/fingerprint and `PermissionInterceptor` request attrs.
- Preserve legacy commit-8 token compatibility through `roles` fallback and old DNA format.
- Preserve old dynamic-DNA graveyard compatibility by keeping app RBAC fingerprint core as `roles=...|permissions=...|roleLevel=...|blacklist=...`.

## Verification

- `git diff --check` passed for touched source/memory paths, with only existing LF/CRLF warnings.
- `bash mvnw -q -DskipTests compile` is blocked before Java compile because Maven cannot download parent POM from Maven Central: `Permission denied: getsockopt`.

## Next

- Re-run Maven compile/test when network access or local dependency cache is available.
