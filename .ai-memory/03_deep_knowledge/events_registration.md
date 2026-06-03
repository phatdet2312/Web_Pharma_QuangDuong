# Events & Registration
> Last updated: 2026-06-03
> Source files: `controller/view/EventViewController.java`, `controller/api/ApiEventController.java`, `controller/api/ApiPublicSpeakerAgendaController.java`, `controller/api/ApiAdminEventController.java`, `controller/api/ApiAdminSpeakerAgendaController.java`, `service/itf/IEventService.java`, `service/itf/IAdminEventService.java`, `service/impl/EventServiceImpl.java`, `service/impl/AdminEventServiceImpl.java`, `service/impl/SpeakerAgendaServiceImpl.java`, `service/support/EventStatusDisplayPolicy.java`, `service/support/NguCanhNguoiDung.java`, `service/support/NguCanhNguoiDungFactory.java`, `utils/ImagePathUtil.java`, `utils/PagingUtil.java`, event entities, `templates/events/*.html`, `templates/admin/events.html`
> Confidence: HIGH

## Summary

Events manage campaigns (`EVENTS`), sessions (`CT_EVENTS`), registration, status history, type/location, speaker/agenda, tags, linked posts and role-gated access. Public pages use `/api/events`; admin page `admin/events.html` uses `/api/admin/events` plus admin speaker/agenda endpoints.

## Current Size / Inventory

- `admin/events.html`: 3663 lines.
- `AdminEventServiceImpl`: 1318 lines.
- Admin media endpoints are current code: `/api/admin/events/media/campaign-thumbnail` and `/api/admin/events/media/speaker-avatar`.

## Public Business Rules

- Current session status comes from latest `CT_EVENT_STATUS_HISTORY`.
- Registrable statuses: `OPEN` or `UPCOMING`.
- `totalSlots = 0` means unlimited. If `totalSlots > 0`, registration status updates must avoid overbooking.
- Registration is append-friendly: latest `CANCELLED` lets user register again.
- Cancel registration checks owner (`reg.user.id == userId`).
- Public attendee summary masks names/phone numbers.
- Role-gated sessions are filtered/masked by backend before DTO response.
- Locked private sessions expose safe display status/content only. Speaker/agenda/related posts/comments/status history/attendee summary must not leak if user lacks access.
- `EventAccessDecision` in `EventServiceImpl` is the access decision point.
- `EventStatusDisplayPolicy` translates status codes to public/admin labels; frontend should use API labels.
- `taoMoTaMarketing` strips script/style/HTML tags and URLs for locked teaser text.

## NguCanhNguoiDung Refactor

- Public event service methods receive `NguCanhNguoiDung` instead of raw `Long userId` for access-aware flows.
- `NguCanhNguoiDungFactory` builds context once from current user and role level.
- Methods that still use `Long userId`: my registrations, my ticket, cancellation/ownership checks.

## Public Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/events/stats` | Public event stats | No |
| GET | `/api/events/locations` | Locations | No |
| GET | `/api/events/calendar` | Sessions by month | Optional |
| GET | `/api/events/types` | Event types | No |
| GET | `/api/events` | Search/filter/page campaigns | Optional |
| GET | `/api/events/{slug}` | Campaign detail | Optional |
| GET | `/api/events/upcoming` | Upcoming sessions | Optional |
| GET | `/api/events/my-registrations` | My registrations | Yes |
| GET | `/api/events/sessions/{ctEventId}` | Session detail | Optional |
| GET | `/api/events/sessions/{ctEventId}/my-ticket` | My ticket for session | Optional |
| POST | `/api/events/register` | Register for session | `USER_REGISTER` |
| DELETE | `/api/events/registrations/{id}` | Cancel registration | Yes |
| GET | `/api/events/sessions/{ctEventId}/comments` | Session comments via event API | No/locked returns empty |
| POST | `/api/events/sessions/{ctEventId}/comments` | Create session comment | `USER_COMMENT` |
| GET | `/api/events/sessions/{ctEventId}/status-history` | Public status timeline | No/locked returns empty |
| GET | `/api/events/sessions/{ctEventId}/attendees-summary` | Masked attendees | No/locked returns empty |
| GET | `/api/events/sessions/{ctEventId}/speakers` | Public speakers | Access-gated |
| GET | `/api/events/sessions/{ctEventId}/agenda` | Public agenda | Access-gated |

## Admin Endpoints

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/api/admin/events/stats` | Admin stats | `EVENT_VIEW` |
| GET | `/api/admin/events/dictionaries/statuses` | Event and registration statuses | `EVENT_VIEW` |
| POST | `/api/admin/events/media/campaign-thumbnail` | Upload campaign image to `/uploads/events/campaigns/` | `EVENT_EDIT` |
| POST | `/api/admin/events/media/speaker-avatar` | Upload speaker avatar to `/uploads/events/speakers/` | `EVENT_EDIT` |
| GET | `/api/admin/events` | Campaign list with keyword/type/location/role/date/page | `EVENT_VIEW` |
| POST/PUT/DELETE | `/api/admin/events`, `/{eventId}` | Campaign CRUD | `EVENT_CREATE` / `EVENT_EDIT` / `EVENT_DELETE` |
| POST | `/api/admin/events/bulk/status` | Bulk session status change by campaigns | `EVENT_EDIT` |
| POST/PUT/DELETE | `/api/admin/events/sessions/**` | Session CRUD | `EVENT_EDIT` |
| GET/POST | `/api/admin/events/sessions/{ctEventId}/status-history`, `/sessions/status` | Status history/read-write | `EVENT_EDIT` |
| GET/PATCH | `/api/admin/events/sessions/{ctEventId}/registrations`, `/registrations/{id}/status` | Registration list/status update | `EVENT_EDIT` |
| GET/POST/PUT/DELETE | `/api/admin/events/types/**` | Event type CRUD and FK checks | `EVENT_MANAGE_TYPE` |
| GET/POST/PUT/DELETE | `/api/admin/events/locations/**` | Location CRUD and FK checks | `EVENT_MANAGE_LOCATION` |
| GET/POST/PUT/DELETE | `/api/admin/events/sessions/{ctEventId}/speakers`, `/events/speakers/{speakerId}` | Speaker admin | `EVENT_MANAGE_SPEAKER` |
| GET/POST/PUT/DELETE | `/api/admin/events/sessions/{ctEventId}/agenda`, `/events/agenda/{agendaId}` | Agenda admin | `EVENT_MANAGE_AGENDA` |

## Admin Business Rules

- Admin status dictionaries are backend-provided. Do not reintroduce JS hardcoded status lists.
- Allowed session statuses: `DRAFT`, `OPEN`, `UPCOMING`, `ONGOING`, `FULL`, `CANCELLED`, `FINISHED`, `ENDED`.
- Allowed registration statuses: `PENDING`, `CONFIRMED`, `APPROVED`, `ATTENDED`, `CANCELLED`.
- Admin image uploads validate max 5MB and server namespace.
- `EventRequest`/`CtEventRequest` validate required FK/length fields.
- Role/tag/related post IDs are validated atomically before replacing join rows.
- Session time must have `endTime > startTime`; update cannot silently change to a mismatched campaign.
- Campaign/session delete cleans dependent comment links, agenda/speaker bridge, agenda, speakers, registrations, status history, post links, tags and roles.
- `ImagePathUtil` validates/chuan hoa server image URLs. `PagingUtil` clamps page/size.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Admin event media uses upload endpoints | `/media/campaign-thumbnail`, `/media/speaker-avatar` | Avoid manual internal path input and keep server namespace | 2026-06-03 | 2026-09-03 |
| Public locked session privacy enforced in backend | DTO masking/filtering before frontend | Frontend must not receive private speaker/agenda/attendee data | 2026-05-26 | 2026-08-26 |
