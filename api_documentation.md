# Event Management System - API Documentation

This document outlines the complete business logic of the endpoints currently implemented in the system. The platform follows a role-based architecture (`ADMIN`, `ORGANIZER`, `ATTENDEE`) and implements strict access control layers.

---

## 1. Authentication & Profile

### Logic Details
- **Registration**: Normalizes email (lowercase) and phone numbers (strips non-digits). Default role is `ATTENDEE`. Validates that the email is unique. Returns JWT access and refresh tokens.
- **Login**: Verifies credentials securely. Password verification occurs before account status checks to prevent user enumeration. Issues JWT tokens if the account is `ACTIVE`.
- **Token Management**:
    - **Refresh**: Validates the refresh token (signature and type) and issues a new pair of tokens if the user is still `ACTIVE`.
    - **Logout**: Revokes the provided access token by adding it to a revocation list (database-backed). Available to any authenticated role (not ATTENDEE-only).
- **OTP Subsystem**:
    - **Send-OTP**: Generates a 6-digit random code and persists it with an expiry (default 2 mins 30 secs). Re-sending is blocked if a valid OTP already exists for the identifier (email/phone).
    - **Verify-OTP**: Checks for expiry and exact match. Marks OTP as used upon success. Returns full auth tokens.
    - **Reset-Password**: Verifies the OTP inline (does not require a prior verify-otp call) before applying the new password.
- **Profile Management**:
    - **Get Me**: Fetches profile data for the authenticated principal.
    - **Update Me**: Allows users to update their name, phone, gender, and password. Phone numbers are normalized during updates.
    - **Soft Delete**: Marks the user status as `INACTIVE` but retains records for data integrity.

### Mismatches vs. Design
- `POST /api/auth/logout` — Design specifies ATTENDEE role only. **Actual**: any authenticated user (`isAuthenticated()`).
- `POST /api/auth/refresh` — Present in the implementation and security permit-list but absent from the design table.
- `POST /api/auth/reset-password` — Design implies OTP must be pre-verified via `/verify-otp`. **Actual**: OTP is verified inline within the reset-password call itself; a prior verify-otp step is not required.

### Status of Planned Endpoints
- [x] `POST /api/auth/register` (Implemented)
- [x] `POST /api/auth/login` (Implemented)
- [x] `POST /api/auth/logout` (Implemented — any authenticated role)
- [x] `POST /api/auth/send-otp` (Implemented)
- [x] `POST /api/auth/verify-otp` (Implemented)
- [x] `POST /api/auth/reset-password` (Implemented — OTP verified inline)
- [x] `POST /api/auth/refresh` (Implemented — not in original design table)
- [x] `GET /api/users/me` (Implemented)
- [x] `PUT /api/users/me` (Implemented)
- [x] `DELETE /api/users/me` (Implemented)

---

## 2. Users (Admin Only)

### Logic Details
- **User List**: Paginated and sortable list. Supports filtering by status (`ACTIVE`, `INACTIVE`, `SUSPENDED`), role, and search query (matches against name or email).
- **User Detail**: Provides full profile details including registration history summary.
- **Status Management**: Admins can adjust user status to `ACTIVE`, `INACTIVE`, or `SUSPENDED`.

### Status of Planned Endpoints
- [x] `GET /api/users` (Implemented)
- [x] `GET /api/users/{id}` (Implemented)
- [x] `PATCH /api/users/{id}/status` (Implemented)

---

## 3. Organizer Profiles

### Logic Details
- **Onboarding**: Users submit an organization name and bio. Profiles are created with `verified = false`. Duplicate submissions for the same user are blocked. Available to any authenticated user.
- **Management**: Organizers can update their business details (social links, website).
- **Admin Review**: Admins view profiles (filterable by `?status=PENDING` which maps to `verified = false`). Admins can `APPROVE` (sets `verified = true`), `REJECT`, or `SUSPEND` a profile.

### Mismatches vs. Design
- `POST /api/users/{id}/organizer-profile` — Design says PUBLIC. **Actual**: requires authentication (`isAuthenticated()`).
- `GET /api/users/{id}/organizer-profile/dashboard` — **Not implemented**.
- `POST /api/users/{id}/organizer-profile/images` — **Not implemented**.

### Status of Planned Endpoints
- [x] `POST /api/users/{id}/organizer-profile` (Implemented — requires authentication, not fully public)
- [x] `GET /api/users/{id}/organizer-profile` (Implemented)
- [x] `PUT /api/users/{id}/organizer-profile` (Implemented)
- [ ] `GET /api/users/{id}/organizer-profile/dashboard` (**NOT IMPLEMENTED**)
- [ ] `POST /api/users/{id}/organizer-profile/images` (**NOT IMPLEMENTED**)
- [x] `GET /api/organizer-profiles` (Implemented)
- [x] `PATCH /api/organizer-profiles/{id}/status` (Implemented)

---

## 4. Categories

### Logic Details
- **Visibility**: Public can list only `ACTIVE` categories. `GET /categories/{id}` is public and returns any category regardless of status.
- **Creation**: Validates category name uniqueness (case-insensitive).
- **Lifecycle**: Toggling a category to `INACTIVE` hides it from the public event search but preserves historical links.

### Status of Planned Endpoints
- [x] `GET /api/categories` (Implemented)
- [x] `GET /api/categories/{id}` (Implemented)
- [x] `POST /api/categories` (Implemented)
- [x] `PUT /api/categories/{id}` (Implemented)
- [x] `PATCH /api/categories/{id}/status` (Implemented)

---

## 5. Venues

### Logic Details
- **Public Search**: Filterable by `city`. Lists only `ACTIVE` venues.
- **Detail**: `GET /venues/{id}` is public and returns any venue regardless of status.
- **Creation/Update**: Admin-driven. Includes capacity tracking used to validate event ticket totals.
- **Status**: Toggling to `INACTIVE` prevents new events from being scheduled at the venue and hides it from public event listings.

### Mismatches vs. Design
- `POST /api/venues/{id}/images` — **Not implemented**.
- `DELETE /api/venues/{id}/images/{mediaId}` — **Not implemented**.

### Status of Planned Endpoints
- [x] `GET /api/venues` (Implemented)
- [x] `GET /api/venues/{id}` (Implemented)
- [x] `POST /api/venues` (Implemented)
- [x] `PUT /api/venues/{id}` (Implemented)
- [x] `PATCH /api/venues/{id}/status` (Implemented)
- [ ] `POST /api/venues/{id}/images` (**NOT IMPLEMENTED**)
- [ ] `DELETE /api/venues/{id}/images/{mediaId}` (**NOT IMPLEMENTED**)

---

## 6. Events

### Logic Details
- **Lifecycle & State Transitions**:
    - **Draft**: Initial state. Editable by organizer.
    - **Pending Approval**: Submitted by organizer for admin review.
    - **Approved**: Verified by admin. Editable by organizer.
    - **Published**: Made visible to the public. Only approved events can be published.
    - **Cancelled**: Terminated by organizer or admin. Organizer cannot cancel a published event; admin can cancel any state.
- **Validation**:
    - Total ticket counts across all tiers must match the event's declared total capacity.
    - Organizers cannot update published events; only `DRAFT` or `APPROVED` state allows edits.
- **Access Control on `GET /events`**:
    - PUBLIC: Only sees `PUBLISHED` events with a future `startTime`, from `ACTIVE` categories and `ACTIVE` venues.
    - ORGANIZER: Sees only their own events across all statuses. The `?status=` filter is respected.
    - ADMIN: Sees all events across all statuses. The `?status=` filter is respected.
- **Access Control on `GET /events/{id}`**:
    - PUBLIC: Returns 404 for any non-PUBLISHED event, any past event, or events with inactive category/venue.
    - ORGANIZER: Can only access their own events; receives 403 for others.
    - ADMIN: Can access any event.
- **Filters**: `?search=`, `?categoryId=`, `?city=`, `?venueId=`, `?date=` (single date, `YYYY-MM-DD`), `?minPrice=`, `?maxPrice=`, `?status=` (ORGANIZER/ADMIN only), `?sort=`, `?page=`, `?size=`.

### Mismatches vs. Design
- `?date=` parameter — Design listed `?date=` (single date). Previous implementation used `?fromDate=` / `?toDate=`. Now corrected to `?date=`.
- `?minPrice=` / `?maxPrice=` — Were missing from the previous implementation. Now implemented.
- `POST /api/events/{id}/images` — **Not implemented**.
- `DELETE /api/events/{id}/images/{mediaId}` — **Not implemented**.
- `PUT /api/events/{id}/images/{mediaId}/primary` — **Not implemented**.

### Status of Planned Endpoints
- [x] `GET /api/events` (Implemented — role-based filtering enforced)
- [x] `POST /api/events` (Implemented)
- [x] `GET /api/events/{id}` (Implemented — role-based access control enforced)
- [x] `PUT /api/events/{id}` (Implemented)
- [x] `DELETE /api/events/{id}` (Implemented)
- [x] `PATCH /api/events/{id}/status` (Implemented)
- [ ] `POST /api/events/{id}/images` (**NOT IMPLEMENTED**)
- [ ] `DELETE /api/events/{id}/images/{mediaId}` (**NOT IMPLEMENTED**)
- [ ] `PUT /api/events/{id}/images/{mediaId}/primary` (**NOT IMPLEMENTED**)

---

## 7. Tickets

### Logic Details
- **Management**: Tied to parent event. Pricing and quantity can only be updated before sales start.
- **Availability**: Tracks `available_quantity` in real-time. Public view is filtered to `ACTIVE` tickets only. Organizer sees all ticket statuses including `INACTIVE`.

### Status of Planned Endpoints
- [x] `GET /api/events/{eventId}/tickets` (Implemented)
- [x] `POST /api/events/{eventId}/tickets` (Implemented)
- [x] `PUT /api/tickets/{id}` (Implemented)
- [x] `PATCH /api/tickets/{id}/status` (Implemented)

---

## 8. Offers & Discounts

### Logic Details
- **Validation**: Checks for validity date range and usage limits. Code is case-insensitive.
- **Calculation**: Applies percentage discount capped at a `max_discount_amount`.
- **Enforcement**: Organizers can only manage offers for their own events. Admins can manage offers for any event.

### Status of Planned Endpoints
- [x] `GET /api/events/{eventId}/offers` (Implemented)
- [x] `POST /api/events/{eventId}/offers` (Implemented)
- [x] `PUT /api/offers/{id}` (Implemented)
- [x] `PATCH /api/offers/{id}/status` (Implemented)
- [x] `POST /api/offers/validate` (Implemented)

---

## 9. Bookings

### Logic Details
- **Preview**: Dry-run calculation. Applies ticket prices and offers without persisting a transaction.
- **Checkout Flow**:
    - Creates a `PENDING` registration.
    - Decrements ticket inventory.
    - Generates a Razorpay order.
    - Persists a `PENDING` payment record.
- **Cancellation**: Only allowed for `CONFIRMED` bookings on cancellable events and before the event's `cancellation_deadline`. Restores ticket inventory upon cancellation.

### Status of Planned Endpoints
- [x] `POST /api/bookings/preview` (Implemented)
- [x] `POST /api/bookings` (Implemented)
- [x] `GET /api/bookings` (Implemented)
- [x] `GET /api/bookings/{id}` (Implemented)
- [x] `PATCH /api/bookings/{id}/status` (Implemented)
- [x] `GET /api/bookings/{bookingId}/refunds` (Implemented)

---

## 10. Participants

### Logic Details
- **Creation**: Triggered automatically upon payment verification.
- **Status Flow**:
    - **Active**: Default state.
    - **Checked In**: Gate management by Organizer via `PATCH /participants/{id}/status`.
    - **Cancelled**: Attendee-initiated via `PATCH /participants/{id}/status`; triggers partial refund logic.
- **Restrictions**: Name and phone edits only allowed before event start.
- **Filters on list**: `?status=` and pagination. The design-specified filters `?search=` and `?ticketId=` are **not implemented** in the current controller.

### Mismatches vs. Design
- `GET /api/events/{eventId}/participants` — Design specifies `?search=` (name/email/phone/ticketCode) and `?ticketId=` filters. **Actual**: only `?status=` is supported.

### Status of Planned Endpoints
- [x] `GET /api/events/{eventId}/participants` (Implemented — `?search=` and `?ticketId=` filters missing)
- [x] `GET /api/events/{eventId}/participants/{id}` (Implemented)
- [x] `PATCH /api/participants/{id}/status` (Implemented)
- [x] `PUT /api/participants/{id}` (Implemented)

---

## 11. Event Registrations (Organizer / Admin View)

### Logic Details
- Provides organizers and admins with a scoped view of all bookings for a specific event. Useful for reconciling financial data and ticket sales.

### Status of Planned Endpoints
- [x] `GET /api/events/{eventId}/registrations` (Implemented)
- [x] `GET /api/events/{eventId}/registrations/{id}` (Implemented)

---

## 12. Payments

### Logic Details
- **Verification**: Performs HMAC-SHA256 signature verification to ensure payment data integrity. On success, updates registration to `CONFIRMED` and spawns participant records.
- **Retries**: Allows retrying `FAILED` payments by creating a fresh gateway order session.
- **Webhook**: Service-layer logic (`handleWebhook`) exists but the HTTP endpoint mapping (`POST /payments/webhook`) is not declared in the controller. The path is permit-listed in `SecurityConfig` but unreachable.
- **Audit**: Admin access to global payment logs. Current `GET /payments` filter supports `?status=` only; design also specifies `?eventId=`, `?from=`, `?to=`.

### Mismatches vs. Design
- `POST /api/payments/webhook` — Service logic exists but **controller endpoint mapping is missing**.
- `GET /api/payments` — `?eventId=`, `?from=`, `?to=` filters specified in design are **not implemented**.

### Status of Planned Endpoints
- [x] `POST /api/payments/verify` (Implemented)
- [x] `POST /api/payments/fail` (Implemented)
- [x] `POST /api/payments/retry` (Implemented)
- [ ] `POST /api/payments/webhook` (**NOT IMPLEMENTED** — service logic exists, controller mapping missing)
- [x] `GET /api/payments/{bookingId}` (Implemented)
- [x] `GET /api/payments` (Implemented — `?eventId=`, `?from=`, `?to=` filters missing)

---

## 13. Refunds

### Logic Details
- Primarily tracks status (`PENDING`, `SUCCESS`, `FAILED`). Retries allowed only for `FAILED` records. Scoped by event for Organizers and global for Admins.
- Current `GET /refunds` filter supports `?status=` only; design also specifies `?eventId=`, `?from=`, `?to=`.

### Mismatches vs. Design
- `GET /api/refunds` — `?eventId=`, `?from=`, `?to=` filters specified in design are **not implemented**.

### Status of Planned Endpoints
- [x] `GET /api/events/{eventId}/refunds` (Implemented)
- [x] `GET /api/refunds` (Implemented — `?eventId=`, `?from=`, `?to=` filters missing)
- [x] `POST /api/refunds/{id}/retry` (Implemented)

---

## 14. Feedback

### Logic Details
- **Eligibility**: Only users who have attended `COMPLETED` events can submit feedback. Duplicate submissions for the same event are blocked.
- **Editing**: Strictly allowed only within 7 days of initial submission.
- **Moderation**: Admins can flag (hide) inappropriate feedback. Public view restricted to `VISIBLE` status. Organizer view includes `FLAGGED`. Admin sees all.

### Status of Planned Endpoints
- [x] `POST /api/events/{eventId}/feedbacks` (Implemented)
- [x] `GET /api/events/{eventId}/feedbacks` (Implemented)
- [x] `PUT /api/feedbacks/{id}` (Implemented)
- [x] `DELETE /api/feedbacks/{id}` (Implemented)
- [x] `PATCH /api/feedbacks/{id}/status` (Implemented)

---

## 15. Notifications

### Logic Details
- **Types**:
    - **Event Announcement**: Targeted broadcast to all confirmed attendees of a specific event (Organizer only).
    - **Broadcast**: System-wide message to all users or a specific role (Admin only).
- **Engagement Tracking**: Tracks read/unread status with bulk "Mark All Read" capability.
- **Broadcast History**: `GET /api/notifications/broadcast-history` is **not implemented**.

### Mismatches vs. Design
- `PATCH /notifications/{id}/status` — Design path is missing the `/api` prefix and is listed inconsistently. **Actual path**: `PATCH /notifications/{id}/status` (matches the controller mapping).
- `GET /api/notifications/broadcast-history` — **Not implemented**.

### Status of Planned Endpoints
- [x] `GET /api/notifications` (Implemented)
- [x] `GET /api/notifications/unread-count` (Implemented)
- [x] `PATCH /notifications/{id}/status` (Implemented)
- [x] `POST /api/notifications/read-all` (Implemented)
- [x] `POST /api/events/{eventId}/notifications` (Implemented)
- [x] `POST /api/notifications/broadcast` (Implemented)
- [ ] `GET /api/notifications/broadcast-history` (**NOT IMPLEMENTED**)

---

## 16. Reports

### Status of Planned Endpoints
- [ ] `GET /api/reports/summary` (**NOT IMPLEMENTED**)
- [ ] `GET /api/reports/revenue` (**NOT IMPLEMENTED**)
- [ ] `GET /api/reports/events` (**NOT IMPLEMENTED**)
- [ ] `GET /api/reports/events/{eventId}` (**NOT IMPLEMENTED**)
- [ ] `GET /api/reports/events/{eventId}/tickets` (**NOT IMPLEMENTED**)
- [ ] `GET /api/reports/events/{eventId}/revenue` (**NOT IMPLEMENTED**)
- [ ] `GET /api/reports/organizers` (**NOT IMPLEMENTED**)

---

## 17. Media Management

### Status of Planned Endpoints
- [ ] `POST /api/media` (**NOT IMPLEMENTED**)
- [ ] `DELETE /api/media/{id}` (**NOT IMPLEMENTED**)