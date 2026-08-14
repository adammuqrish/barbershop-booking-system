# Frontend Enhancement Plan — Hugi Barbershop

This document catalogues frontend issues, inconsistencies, and improvement opportunities identified across the project's HTML templates, CSS, and JavaScript. Each item includes the affected file(s), a description of the problem, and a recommended enhancement. Items are grouped by category and ordered roughly by priority/impact.

---

## 1. Global / Shared Fragments

### 1.1 Stale "Java Getting Started on Heroku" Layout Fragment ✅ [COMPLETED]
- **File:** `src/main/resources/templates/fragments/layout.html`
- **Issue:** This is a leftover from Heroku's default "Java getting-started" template. It references Bootstrap 3.3.7, jQuery UI, links to Heroku Dev Center guides, uses outdated `glyphicon` icons, and is **not actually used** by any page in the app. It is dead code that clutters the codebase and could confuse future developers.
- **Enhancement:** Delete `layout.html` and its reference in `target/classes/templates/fragments/layout.html` (build artifact, will regenerate). It serves no purpose in the current app.
- **Change made:** Deleted `src/main/resources/templates/fragments/layout.html`. Verified the file was not referenced (`th:replace`) in any template or Java file before removal. ✅

### 1.2 Duplicate / Unused JS File Paths ✅ [COMPLETED]
- **Files:**
  - `src/main/resources/static/resources/js/script.js` (loaded in `fragments/header.html`)
  - `src/main/resources/static/js/scripts.js` (the full NioBoard admin script)
  - `src/main/resources/static/resources/jsAdmin/scripts.js`
- **Issue:** There were duplicated JavaScript files in three separate locations (`static/js/`, `static/resources/jsAdmin/`, and `static/resources/assetsAdmin/js/`). The admin pages reference `assetsAdmin/css/style.css` but don't explicitly load `scripts.js`, so the admin sidebar/navbar toggles and other interactive features were not wired up. `profile.html` referenced nonexistent `bundle.js` / `demo-init.js`, and the critical `nioapp.js` (which defines the global `NioApp` that `scripts.js` depends on) was never loaded anywhere.
- **Enhancement:** Consolidate JS to one canonical path (`assetsAdmin/js/`) and explicitly load the required admin JS in every admin page template. Remove the `resources/jsAdmin` and `static/js` duplicates.
- **Change made:**
  - Created `static/resources/assetsAdmin/js/vendors/` and copied `nioapp.js` + `pureknob/pureknob.js` into it (these were only present in the deleted `jsAdmin/vendors/` and are required before `scripts.js`).
  - Deleted `static/resources/jsAdmin/` (full duplicate of admin JS, never referenced by any template).
  - Deleted `static/js/` (duplicate NioBoard JS, never referenced by any template — only `assetsAdmin/js/` is referenced).
  - Created reusable fragment `fragments/adminScripts.html` that loads jQuery 3.6.0, Bootstrap 5.3.3 bundle, `assetsAdmin/js/vendors/nioapp.js`, and `assetsAdmin/js/scripts.js`.
  - Added `<div th:replace="~{fragments/adminScripts :: adminJs}"></div>` to all 10 admin templates (`adminLogin`, `adminIndex`, `registerStaff`, `listCustomer`, `profile`, `listBarber`, `listTransactions`, `listFeedback`, `listAppointment`, `edit-staff`), replacing ad-hoc/incomplete script includes. This wires up the NioBoard sidebar/navbar and fixes the broken `profile.html` references.
  - Verified `mvn -o compile` → BUILD SUCCESS. ✅

### 1.3 Header Fragment Misses Common Meta Tags & Favicon ✅ [COMPLETED]
- **File:** `src/main/resources/templates/fragments/header.html`
- **Issue:** The `<head th:fragment="head">` block did not include:
  - A favicon reference (`favicon.png` exists in uploads but was never linked)
  - `charset` meta is present but there were no `theme-color`, author, description, or social/Open Graph meta tags
  - It loads `@{ /resources/js/script.js }` unconditionally even on pages that don't need it (e.g., admin pages)
- **Enhancement:** Add `<link rel="icon" th:href="@{/resources/uploads/favicon.png}">` and a theme-color meta tag. Make the `script.js` include conditional or move it to pages that need it.
- **Change made:**
  - Added `<link rel="icon" th:href="@{/resources/uploads/favicon.png}" type="image/png">` (favicon confirmed present at `static/resources/uploads/favicon.png`).
  - Added `<meta name="theme-color" content="#2dc58c">` (matches the brand green used in `style.css`).
  - Added `<meta name="description">` (defaults to a Hugi Barbershop tagline, overridable via a `description` model attribute) and `<meta name="author" content="Hugi Barbershop">`.
  - Added Open Graph + Twitter Card tags (`og:type`, `og:title`, `og:description`, `og:image`, `twitter:card`) for social sharing, using the favicon as the share image.
  - Kept the `script.js` include in the shared head: every customer page that uses this fragment also renders `fragments/nav.html`, whose dropdown/toggle logic depends on `script.js`, so it is needed on all of them. (Admin pages do not use this fragment — they load their own `assetsAdmin` head — so the "admin pages" concern does not apply here.)
  - Default `<title>` changed from "Barbershop Landing Page" to "Hugi Barbershop" for consistent branding. ✅

### 1.4 Tailwind CSS CDN (JIT) Loaded at Runtime ✅ [COMPLETED]
- **File:** `fragments/header.html:10`
- **Issue:** Tailwind was loaded via `<script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>`. The `@tailwindcss/browser` build is intended for **development only**. It is slow (JIT compilation happens in-browser on every load) and not production-ready.
- **Enhancement:** Replace with a **compiled Tailwind CSS build**: generate `main.css` via `tailwindcss-cli` as part of the Maven build and reference a static CSS file.
- **Change made:**
  - Added `src/main/resources/static/css/tailwind-input.css` (`@import "tailwindcss";` + `@source` directives scanning `templates/**/*.html` and `static/**/*.js`).
  - Added `package.json` with `build:css` (`tailwindcss -i … -o …/main.css --minify`) and `watch:css` scripts; dev dependency `@tailwindcss/cli@^4.1.0`.
  - Removed the `@tailwindcss/browser@4` `<script>` from `fragments/header.html` and replaced it with `<link rel="stylesheet" th:href="@{/css/main.css}">`.
  - Added `frontend-maven-plugin` (1.15.1) to `pom.xml`, bound to `generate-resources`: it installs Node 20, runs `npm install`, then `npm run build:css`, regenerating `main.css` on every `mvn` build (including Heroku slug compile). `main.css` is committed as a baseline so the app works even if the build step is skipped.
  - Added `node/` and `node_modules/` to `.gitignore`.
  - Verified: `npm run build:css` produces a ~29.5 KB minified `main.css` (includes JS-referenced classes like `text-yellow-300` and `hidden`); `mvn generate-resources` → **BUILD SUCCESS** with the plugin executing `npm run build:css`. ✅

### 1.5 Nav & Footer Comments Contain Placeholder Text ✅ [COMPLETED]
- **Files:** `fragments/nav.html:11`, `fragments/footer.html:10`
- **Issue:** Both contained literal comment text like `<!-- ... (omitted SVGs for brevity, but I will include them) ... -->` and `<!-- SVG Icons from original JSP (restoring them exactly) -->`. This was leftover from a migration and indicated SVGs that were supposed to be restored but never were.
- **Enhancement:** Remove the stale comments. Add actual SVG icons for social/contact info in the footer (which were missing).
- **Change made:**
  - `fragments/nav.html`: removed the two stale comments from the empty top-bar `<div>` and replaced it with real Facebook + Instagram SVG icons (inline, `fill="currentColor"`, `aria-label`s, hover state).
  - `fragments/footer.html`: replaced the stale comment in the brand column with a Facebook + Instagram social SVG row; added location-pin, phone (`tel:` link), and envelope (`mailto:` link) SVG icons to the "Barbershop Information" column so the contact details now have icons.
  - Regenerated `main.css` (`npm run build:css`, ~30 KB) so the newly-used utilities (`flex-shrink-0`, `mt-0.5`, `gap-2`, `items-start`, `space-y-2`, `mt-3`) are included. ✅

### 1.6 External CDN Assets Self-Hosted (Icons Missing on Production) ✅ [COMPLETED]
- **Files:** All admin templates + `fragments/adminScripts.html`, `fragments/header.html`, `customer/view-customer-details.html`, `admin/adminIndex.html`
- **Issue:** Templates loaded key assets from third-party CDNs. Locally these load fine, but on production the browser must fetch them from `cdnjs.cloudflare.com` (Font Awesome), `code.jquery.com` (jQuery), and `cdn.jsdelivr.net` (Bootstrap, Flowbite, Chart.js). When those CDNs are blocked/slow/unreachable, the icons silently disappear — e.g. the `fa-eye` / `fa-eye-slash` password-toggle icons on admin pages (while the customer page's inline-SVG eye still shows, since it has no external dependency).
- **Enhancement:** Self-host all external assets in `static/` and reference them via local `th:href` / `th:src` URLs, so production has zero third-party runtime dependencies.
- **Change made:**
  - Downloaded **Font Awesome 6.5.0** (`css/all.min.css` + `webfonts/*`) into `static/resources/assetsAdmin/css/fontawesome/` and pointed all 11 templates (all `admin/*` + `customer/view-customer-details.html`) at `@{/resources/assetsAdmin/css/fontawesome/css/all.min.css}`.
  - Downloaded **jQuery 3.6.0**, **Bootstrap 5.3.3** (`bootstrap.bundle.min.js`), **Flowbite 3.1.2**, and **Chart.js 4.4.1** (`chart.umd.min.js`) into `static/resources/assetsAdmin/js/vendors/`.
  - `fragments/adminScripts.html` now loads jQuery + Bootstrap from the vendored paths.
  - `fragments/header.html` now loads Flowbite from the vendored path.
  - `admin/adminIndex.html` now loads Chart.js from the vendored path.
  - Verified: `grep https:// templates` → none; `mvnw -o compile` → **BUILD SUCCESS**; `target/classes` synced and verified. ✅

---

## 2. Navigation Bar (`fragments/nav.html`)

### 2.1 Contact Info Hardcoded in Template ✅ [COMPLETED]
- **File:** `fragments/nav.html:5-7` (and `fragments/footer.html` contact block)
- **Issue:** "CONTACT US: 0127865132" and "OPENING HOUR: TUESDAY - SUNDAY (10 a.m - 10 p.m)" in `nav.html` were hardcoded, as were the footer's address / phone / email. They should come from a config/properties file so the barbershop owner can change them without modifying templates.
- **Enhancement:** Move to `application.properties` (e.g. `barbershop.phone`, `barbershop.opening-hours`) and inject via Thymeleaf `th:text`.
- **Change made:**
  - Added a `Barbershop Public Contact Info` block to `application.properties` with `barbershop.phone`, `barbershop.opening-hours`, `barbershop.email`, and `barbershop.address`.
  - Created `com.heroku.java.config.GlobalModelAttributes` (`@ControllerAdvice`) that reads those properties (with safe defaults) and exposes them as a `barbershop` map to every Thymeleaf template.
  - `fragments/nav.html`: `CONTACT US` and `OPENING HOUR` now use `th:text="${barbershop.phone}"` / `th:text="${barbershop.openingHours}"` (static fallback text preserved for graceful degradation).
  - `fragments/footer.html`: address, phone (`tel:` link), and email (`mailto:` link) now use `barbershop.address`, `barbershop.phone`, `barbershop.email`. **Note:** the footer previously hardcoded a different phone number (`012-7678776`) than the nav (`0127865132`); both now read the single `barbershop.phone` property, so they are unified — set `barbershop.phone` to the correct value if one of the originals was a typo.
  - `mvn -o compile` → **BUILD SUCCESS**. ✅

### 2.2 Appointment Dropdown — Conflicting Desktop & Mobile Logic ✅ [COMPLETED]
- **File:** `fragments/nav.html:35-65` + `static/resources/js/script.js:84-130`
- **Issue:** The nav had **two separate triggers** for the Appointment dropdown: `appointment-desktop-trigger` (a `<span>` shown on desktop via `hidden md:flex`) and `dropdown-toggle` (a `<button>` shown on mobile). The JS handled both with separate listeners. The mobile script targeted the button via a fragile selector: `.dropdown-toggle.md\\:hidden, .dropdown-toggle.md\\:block.md\\:hidden`. This was fragile and could break if class names changed.
- **Enhancement:** Simplify to a single dropdown trigger pattern. Use a consistent `data-dropdown` attribute instead of custom per-trigger JS.
- **Change made:**
  - `fragments/nav.html`: replaced the two Appointment triggers with a **single** `<button data-dropdown="appointment-dropdown-menu">` (works on desktop and mobile, no `hidden md:flex` / `md:hidden` split). Also added `data-dropdown="loginDropdown"` to the Login button. Removed the now-unused `appointment-desktop-trigger` / `loginToggleBtn` IDs and the `dropdown-toggle` class from nav markup. Changed the dropdown `<li>` wrappers from `relative group` to `relative` (the dropdown menus are `absolute`, so they anchor to the `<li>`).
  - `static/resources/js/script.js`: deleted the three separate dropdown handlers (desktop appt, mobile appt with the fragile selector, login) and replaced them with one delegated handler bound to `[data-dropdown]`. It toggles the referenced menu, ensures only one dropdown is open at a time, syncs `aria-expanded`, and closes all dropdowns on outside click. No fragile class selectors remain.
  - `mvn -o compile` → **BUILD SUCCESS**. (Flowbite is unaffected — it only reacts to `data-dropdown-toggle`, not our `data-dropdown`.) ✅

### 2.3 Nav Active-Link Highlighting is Broken ✅ [COMPLETED]
- **File:** `static/resources/js/script.js:56-82`
- **Issue:** The script selected links with class `.lg\:hover\:bg-transparent` to add a yellow text highlight when the link href matched the current page. It only covered top-level nav links (Home, About Us). Dropdown items (Book Appointment, Current Appointment, Appointment History) and the Login/Logout links were not covered. Worse, `linkHref === currentPage` compared the full href (e.g. `/booking`) to the last path segment (e.g. `booking`), which **never matched**, so highlighting was effectively broken. It also failed for links like `/index#aboutUs`.
- **Enhancement:** Use a more robust active-link detection (compare `new URL(link.href).pathname` to `window.location.pathname`). Add active states to dropdown sub-items too.
- **Change made:** Rewrote the handler in `static/resources/js/script.js`:
  - Selects all nav links via `#navbar-default a[href]` (covers top-level links **and** the dropdown sub-items for Appointment and Login, plus Logout/Profile).
  - Compares `new URL(link.href).pathname` (hash/query ignored) to `window.location.pathname`, normalized to strip trailing slashes — so `/index#aboutUs` now resolves to `/index` and highlights correctly.
  - Keeps the existing `text-yellow-300` highlight (verified it appears after `text-white` in the compiled `main.css`, so it overrides the white nav text).
  - Verified with `node --check` (JS syntax OK). ✅

### 2.4 User Avatar Container Size Mismatch ✅ [COMPLETED]
- **File:** `fragments/nav.html:75-88`
- **Issue:** The avatar wrapper was `relative w-10 h-10 overflow-hidden` but the image inside was `absolute w-12 h-12` — the image was **larger** than its container, causing overflow/overlap with adjacent nav items (the fallback SVG also used `w-12 h-12` plus a `-left-1` offset).
- **Enhancement:** Make the image fit the container (`w-10 h-10`) and use `object-cover object-center`.
- **Change made:** In `fragments/nav.html`, resized the profile `<img>` from `w-12 h-12` to `w-10 h-10` and added `object-cover object-center` (fills the circular `10x10` container and centers/crops the image). Resized the fallback user-icon `<svg>` from `w-12 h-12 -left-1` to `w-10 h-10` and removed the `-left-1` offset so it no longer overflows either. Regenerated `main.css` to include `object-cover` / `object-center`. ✅

### 2.5 Login Dropdown Doesn't Close on Navigation ✅ [COMPLETED]
- **File:** `static/resources/js/script.js:132-154`
- **Issue:** The login dropdown has a "close on outside click" handler, but if a user clicks a dropdown item (Login as Customer / Login as Staff), the dropdown didn't immediately close before navigation — the click bubbled and the menu stayed visible momentarily during navigation.
- **Enhancement:** Add `click` handlers on dropdown items that close the menu before following the link.
- **Change made:** In `static/resources/js/script.js`, extended the shared `[data-dropdown]` handler (added in 2.2) with a document-level click handler that, when a link inside any open dropdown menu (Appointment **and** Login) is clicked, hides that menu and resets `aria-expanded` *before* the browser navigates. Verified with `node --check` (JS syntax OK). ✅

---

## 3. Customer Pages

### 3.1 Sliding Login/Register Form Has No Password Visibility Toggle ✅ [COMPLETED]
- **File:** `customer/register.html`
- **Issue:** The password fields had no "show/hide password" toggle, even though the admin `scripts.js` has a `showHidePassword` utility. The register page has its own custom CSS for a sliding panel modal but didn't integrate password visibility.
- **Enhancement:** Add eye-icon toggle for both password fields. The custom modal CSS is already well-written; just add the toggle.
- **Change made:** In `customer/register.html`:
  - Wrapped all three password inputs (Login, Register, Confirm Password) in `relative` containers and added a reusable `.password-toggle` eye-button (`data-target` → input id) positioned inside the input (`inset-y-0 right-0`, `pr-10` on the input). Each button holds an open-eye SVG (`eye-icon`) and a slashed-eye SVG (`eye-off-icon`).
  - Added a small, generic script that binds every `.password-toggle`: toggles the input `type` between `password`/`text`, swaps the eye/eye-off icons, and updates the `aria-label`.
  - Regenerated `main.css` to include the new utilities (`inset-y-0`, `right-0`, `pr-10`, `px-3`, `items-center`). `mvn -o compile` → **BUILD SUCCESS**. ✅

  **Follow-up:** The customer `register.html` sliding form already had the toggle, but three admin/customer pages had password fields with **no** visibility toggle:
  - `admin/registerStaff.html` — `Password` and `Confirm Password` fields
  - `admin/adminLogin.html` — `Password` field
  - `admin/listBarber.html` — `Password` and `Confirm Password` fields (in the "Add Barber" modal)
  - `customer/editProfile.html` — `New Password` field (note: `script.js` has no toggle binding, so each page gets its own inline binding)

  Added eye/eye-slash toggles to all of them. Admin pages (NioBoard theme) use Font Awesome icons with inline styles for positioning. The customer `editProfile.html` uses inline SVG icons (Tailwind sizing classes) + an inline `.password-toggle` script (since the shared `script.js` has no toggle binding). Used pure inline styles on admin pages to avoid NioBoard theme compatibility issues. ✅

  **Important deployment note:** With `spring.thymeleaf.cache=false` the server re-reads templates from `target/classes` on each request, so after editing `src/` you must rebuild/copy resources into `target/classes` and confirm the app process is running the latest build (restart the server). The customer inline-SVG toggle renders without external CSS, so it is a reliable signal of whether fresh templates are being served.

### 3.2 Registration Form Lacks Client-Side Validation
- **File:** `customer/register.html:166-214`
- **Issue:** The form has `required` attributes and server-side validation, but no client-side validation feedback. The phone field strips non-numeric chars (good), but there's no email format validation, no password strength check, and no match indicator for confirm password.
- **Enhancement:** Add real-time validation feedback (error messages inline, password strength meter, confirm-password match indicator).
- **Status:** ✅ COMPLETED — Added `id`s to all register inputs (`registerName`, `registerEmail`, `registerPhone`, `registerPassword`, `registerConfirmPassword`), inline error `<span>`s (`data-error-for`), a 4-bar password strength meter (`#passwordStrength`/`#strengthText`), and a confirm-password match indicator (`#passwordMatch`, green "Passwords match." / red "Passwords do not match."). New `<script>` (DOMContentLoaded) validates name/email(regex `^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$`)/phone(7-15 digits)/password(min 8)/confirm on `input`+`blur` and blocks submit when invalid. `form-container` height raised 600→680px and register panel given `overflow-y-auto` to fit the meter. Rebuilt `main.css` via `mvnw -o compile`; verified `/register` renders HTTP 200 with all new elements.

### 3.3 Booking Page — Slot Grid Uses `grid-cols-6` Without Mobile Responsiveness
- **File:** `customer/booking.html:27-36`
- **Issue:** Time slots are rendered in a fixed `grid grid-cols-6 gap-2` which doesn't adapt on small screens. On mobile, 6 columns is too cramped — labels overlap.
- **Enhancement:** Use responsive grid (`grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6`) so slots wrap nicely on all screen sizes.

### 3.4 Booking Page — "Booking For" Field Has No Input Validation / Labeling
- **File:** `customer/booking.html:12-17`
- **Issue:** The "Booking For" text input allows any value with no pattern or suggestion. There's no clear label association error (the `for` matches `id`, which is correct, but the field name `booking-for` with a hyphen is fine). The main issue is no guidance on expected input format.
- **Enhancement:** Add a placeholder like "e.g., John Doe" and consider making it a select with "Myself" / "Someone else" for better UX.

### 3.5 Edit Appointment — Duplicate Slot Radio JS Logic (No Availability Check)
- **File:** `customer/edit-appointment.html`
- **Issue:** Unlike the new `booking.html`, the `edit-appointment.html` page renders **all** time slots as enabled radio buttons (no `disabled` attribute) and the JS only updates the selected-time display field. It fetches availability from `/booking/unavailable` but then **never disables any slots** based on the result in `updateSlotStatus()`. The availability logic from `booking.html` is not replicated here.
- **Enhancement:** Port the availability + time-past disabling logic from `booking.html` into `edit-appointment.html`'s `updateSlotStatus` function.

### 3.6 Payment Page — Online Banking Is a Mock with No Real Checkout Flow
- **File:** `customer/payment.html`
- **Issue:** The "Online Banking" option shows a fake "Select Bank" modal with hardcoded bank options (Maybank, CIMB, Public Bank). Clicking "Confirm Payment" just submits the form with a hidden bank name — no real payment gateway integration. The bank modal is a placeholder.
- **Enhancement:** Either integrate a real payment gateway (e.g., Stripe, SenangPay, FPX) **or** clearly label this as a simulation with a disclaimer and auto-complete step. At minimum, show a spinner/loading state on "Confirm Payment" to indicate processing.

### 3.7 Payment Page — Button Text/State Inconsistency on Price = 0
- **File:** `customer/payment.html:95-116`
- **Issue:** When `price === 0` (free appointment via loyalty points), the button text changes to "Proceed" but selecting "Cash" or "Online Banking" still triggers the bank modal check. The `togglePaymentDetails()` function doesn't account for the free case properly — if price is 0 and user selects online, the bank modal still appears.
- **Enhancement:** When price is 0, hide the payment method selection entirely and only show the "Proceed" button, or auto-set payment method to "cash" with completed status.

### 3.8 Profile Page — No Change Password Confirmation Field
- **File:** `customer/editProfile.html:55-59`
- **Issue:** The "New Password" field has no "Confirm Password" field, unlike the register form. Users could mistype their new password.
- **Enhancement:** Add a "Confirm New Password" field with match validation.

### 3.9 Profile Page — Image Upload Preview Is Missing
- **File:** `customer/editProfile.html:41-53`
- **Issue:** The file upload shows the filename but no image preview. The existing `NioApp.Custom.uploadImage` function in `scripts.js` supports preview but isn't used here.
- **Enhancement:** Add a live image preview thumbnail that appears when a file is selected.

### 3.10 Appointment History — Cards Have Inconsistent Action Button Styling
- **File:** `customer/appointment-history.html:36-49`
- **Issue:** The "View Receipt" button uses a custom Tailwind class (`bg-blue-200 text-blue-800`) while "Give Feedback" uses (`bg-blue-500 text-white`). The styling is inconsistent.
- **Enhancement:** Standardize action button styles across all customer pages (use a consistent color palette for primary/secondary actions).

### 3.11 Receipt Page — No Print Button
- **File:** `customer/receipt.html`
- **Issue:** The receipt page has a "Back" button but no "Print Receipt" button. Users would want to print or save the receipt as PDF.
- **Enhancement:** Add a print button that calls `window.print()` and applies receipt-specific print styles.

---

## 4. Admin / Staff Dashboard & Management Pages

### 4.1 Admin Dashboard — Chart.js Canvas Has No Aspect Ratio Container
- **File:** `admin/adminIndex.html:86-93`
- **Issue:** The `<canvas id="salesChart">` is placed directly inside a `.card` with no height constraint. On some screen sizes the chart can render with zero height or stretched proportions.
- **Enhancement:** Wrap the canvas in a container with a fixed aspect ratio (e.g., `style="position: relative; height: 300px;"`) or use Chart.js's `maintainAspectRatio: false` with an explicit height.

### 4.2 Admin Dashboard — Sales Chart Labels Don't Match Data Order
- **File:** `admin/adminIndex.html:100-117`
- **Issue:** The `dayLabels` array is defined but **never used**. The chart labels array is `['Sun', 'Mon', 'Tue', ...]` while the data is pushed from the `days` array which is in the same order, so it works but the unused variable is dead code.
- **Enhancement:** Remove the unused `dayLabels` variable. Consider labeling full day names for accessibility.

### 4.3 Admin Sidebar — Logo Link Uses Inline Width/Height Styles
- **File:** `fragments/adminFragments.html:9-13`
- **Issue:** The sidebar logo image has `style="width: 150px; height: 100px"` which will distort the image (not maintain aspect ratio). The logo wrapper also has fixed dimensions.
- **Enhancement:** Use `object-contain` or `max-width: 100%` with auto height on the image, and remove forced height on the wrapper.

### 4.4 Admin Sidebar — Missing "Active" Class Highlighting for Current Page
- **File:** `fragments/adminFragments.html:1-78`
- **Issue:** The sidebar menu items don't have dynamic "active" class highlighting based on the current URL. The `scripts.js` has `CurrentLink` logic but the sidebar uses `nk-menu-link` class which may or may not be targeted.
- **Enhancement:** Add `th:classappend` to highlight the current section based on `window.location.pathname` or a server-provided active menu variable.

### 4.5 Staff List Page — "Admin" Column Shows Admin Name for Non-Admin Staff Only
- **File:** `admin/listBarber.html:54`
- **Issue:** The "Admin" column shows `${adminNameMap[barber.staffId]}`, which only includes entries where `staff.adminId != null`. Admins themselves (who have `adminId == null`) show `—`. This is actually correct behavior, but the column header says "Admin" which is ambiguous — it really means "Created By".
- **Enhancement:** Rename the column header to "Created By" for clarity.

### 4.6 Staff List Page — Register Modal Phone Field Lacks Input Masking
- **File:** `admin/listBarber.html:178-181`
- **Issue:** The phone number input in the register staff modal has no input masking (unlike the customer register page which strips non-numeric chars). Users could enter invalid characters.
- **Enhancement:** Add `inputmode="numeric"` and the same non-numeric stripping script as the customer register page.

### 4.7 Appointment List — Edit Panel Date Picker Min Attribute Logic is Client-Side Only
- **File:** `admin/listAppointment.html:198-203`
- **Issue:** The date picker has `th:value="${appointment.appointmentDate}"` but no `min` attribute set server-side. The `min` is only set via JS in `DOMContentLoaded`. This means the native date picker could allow selecting past dates before JS runs.
- **Enhancement:** Set `th:min="${#dates.format(#dates.createNow(), 'yyyy-MM-dd')}"` server-side as a fallback.

### 4.8 Appointment List — Time Select Starts Empty, No "Loading" State
- **File:** `admin/listAppointment.html:206-214` + JS `:292-348`
- **Issue:** When editing an appointment, the time `<select>` starts with only a "Select Time" placeholder option. The `updateAvailableTimes()` function fetches options via AJAX but there's no loading indicator or error handling. If the fetch fails, the user sees an empty dropdown with no feedback.
- **Enhancement:** Add a loading spinner placeholder option and a `.catch()` handler that shows an error message in the dropdown.

### 4.9 Appointment List / Edit Appointment — Barber Hidden When All Slots Full
- **File:** `admin/listAppointment.html:219-224`
- **Issue:** When no barber is selected, the available-times API returns all slots booked (checks all appointments for that date). The time dropdown will be nearly empty. There's no guidance telling the admin to select a barber first.
- **Enhancement:** Default the barber select to the appointment's current barber so times load immediately. Add a "Select a barber first" placeholder message.

### 4.10 Admin Login Page — Layout Is Not Centered on Large Screens
- **File:** `admin/adminLogin.html:11-16`
- **Issue:** The login container has `d-flex` (Bootstrap) but the wrapper uses `display: flex` with `align-items: center` inline style. On very large screens the login form can appear left-aligned rather than perfectly centered.
- **Enhancement:** Add `justify-content-center` to the flex container or use `mx-auto` centering.

### 4.11 All Admin Pages — Missing CSRF Token Handling
- **File:** `config/SecurityConfig.java:48-51`
- **Issue:** The CSRF configuration ignores `/staffAuth`, `/auth`, `/payment/**`, `/booking/**`, `/feedback/**`, and `/admin/**`. While this allows forms to submit without tokens, it also disables CSRF protection on admin actions (delete staff, update appointment, register staff, etc.). This is a security concern, though the CSRF exemption was likely intentional to avoid adding token fields to every form.
- **Enhancement (Frontend):** Add `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />` to all admin POST forms. Remove the `/admin/**` CSRF exemption from `SecurityConfig`. *This requires a backend change but the frontend template updates are documented here.*

---

## 5. Booking Flow / Appointment Management

### 5.1 Booking Page — No Visual Feedback After Form Submission
- **File:** `customer/booking.html`
- **Issue:** When the user clicks "Book Appointment", the form submits and redirects to `/payment`. There's no loading state or confirmation. If the server is slow, the user may double-submit.
- **Enhancement:** Add a loading spinner to the submit button on click (`disabled` + spinner text).

### 5.2 Booking Page — Barber Dropdown Hides Options Based on First Slot Only
- **File:** `customer/booking.html:51-55`
- **Issue:** The `th:style` on barber `<option>` elements checks only `unavailableBarbersBySlot[slots[0]]` (the first slot). This hides barbers who are unavailable at the **first time slot** regardless of which slot the user will actually select. This is incorrect — it should not pre-hide barbers based on the first slot.
- **Enhancement:** Remove the `th:style` on options. Let the JS (slot change listener) dynamically hide/show barbers based on the actually selected slot.

### 5.3 Booking Page — Radio Button Disabling Doesn't Update Barber Dropdown on Date Change
- **File:** `customer/booking.html:68-85` (JS)
- **Issue:** When the date changes, `updateSlotStatus()` disables time slots and resets the barber dropdown. But if a time was previously selected and becomes disabled, the `selected-time` input keeps the old (now invalid) value. There's no visual cue that the previously selected time is no longer available.
- **Enhancement:** When date changes and no time is selected, or when a previously selected time becomes disabled, show a notice like "Please select a new time slot."

---

## 6. CSS / Styling Consistency

### 6.1 Inconsistent Background Colors Across Pages
- **Files:** All customer pages use `bg-yellow-100` on `<body>`. Admin pages use a white/light-gray bg with `#101820` color scheme. There is no unified theme.
- **Enhancement:** Define a consistent color palette in CSS custom properties (`:root`) and use them across all pages. Consider a shared theme token file.

### 6.2 Admin Pages Don't Load `script.js` or NioBoard Scripts
- **Files:** `admin/adminIndex.html`, `admin/listBarber.html`, `admin/listAppointment.html`, etc.
- **Issue:** These pages load `assetsAdmin/css/style.css` (the NioBoard theme) but don't load `assetsAdmin/js/scripts.js` or any JS. This means sidebar toggle, dropdown menus, and other NioBoard interactions **don't work** on admin pages.
- **Enhancement:** Add `<script>` includes for jQuery, Bootstrap 5, and `scripts.js` (NioBoard) to a shared admin layout/head fragment so all admin pages get interactivity.

### 6.3 Customer Pages Load `script.js` Twice (via header + body inline script)
- **File:** `fragments/header.html:9` loads `@{ /resources/js/script.js }` — this is the correct path for customer pages. But the file at `static/resources/js/script.js` (with the nav toggle logic) is a **different, simpler** script than `static/js/scripts.js` (full NioBoard). The naming is confusing.
- **Enhancement:** Rename `static/resources/js/script.js` to `nav-scripts.js` to avoid confusion with the full `scripts.js`. Document which pages use which.

### 6.4 No Dark Mode Support
- **Issue:** The entire app uses light-themed UIs for both customer and admin. The barbershop branding (`#101820` dark color) is used for nav headers, but there's no toggle.
- **Enhancement:** Add a dark/light mode toggle (Persists in `localStorage`). Tailwind CSS v4 supports this via the `dark:` variant.

---

## 7. Accessibility (a11y)

### 7.1 Missing ARIA Attributes on Form Controls
- **Files:** All customer pages (booking, register, payment, edit-profile)
- **Issue:** Custom radio button groups (time slots), dropdowns, and the login/register sliding form have no ARIA roles, labels, or keyboard navigation support beyond native HTML.
- **Enhancement:** Add `role="radiogroup"`, `aria-labelledby`, `aria-describedby` on custom control groups. Ensure all interactive elements are keyboard-accessible.

### 7.2 Images Missing Alt Text in Several Places
- **Files:** `index.html:34,36`, `customer/profile.html`, `admin/listBarber.html`
- **Issue:** Barber images on the homepage and profile pictures lack descriptive `alt` attributes (some use empty alt, some have none). 
- **Enhancement:** Add meaningful `alt` text (e.g., "Barber at work" or the barber's name) or `alt=""` for decorative images.

### 7.3 Color-Only Indicators for Status / Errors
- **Files:** `admin/listAppointment.html` (status badges), `customer/feedback.html` (error alerts)
- **Issue:** Status indicators use color badges (green=success, yellow=warning, red=danger) with text labels, which is mostly OK, but error/success alerts rely solely on color for the icon.
- **Enhancement:** Ensure all status indicators have text labels or `aria-label` attributes for screen readers.

---

## 8. Performance & Asset Optimization

### 8.1 Images Not Optimized / No Lazy Loading
- **Files:** All pages with `<img>` tags, especially `index.html` (barber background images), `customer/profile.html`, `admin/profile.html`
- **Issue:** Images are loaded eagerly with no `loading="lazy"` attribute and no width/height attributes for layout stability. Background images in `index.html` are large.
- **Enhancement:** Add `loading="lazy"`, `width`/`height` attributes, and `object-cover`. Consider responsive `srcset` for different screen sizes. Optimize background images (they appear to be full-size JPEGs in hero sections).

### 8.2 Unused Static Assets
- **Files:** `static/js/editors/`, `static/js/kanban/`, `static/js/data-tables/`, `static/js/fullcalendar/`, `static/js/apps/`, `static/js/sweetalert/`
- **Issue:** Many JS library files (TinyMCE, Quill, Kanban, FullCalendar, SweetAlert, Data Tables) are bundled but **not referenced** in any template. They bloat the deployment.
- **Enhancement:** Audit and remove unused JS/CSS assets. Only the NioBoard theme, Chart.js, Tailwind, and Flowbite are actually needed.

---

## 9. Summary Table

| # | Category | File(s) | Priority |
|---|----------|---------|----------|
| 1.1 | Dead Code | `fragments/layout.html` | Low | ✅ Done |
| 1.2 | JS Consolidation | Multiple | Medium | ✅ Done
| 1.3 | SEO / UX | `fragments/header.html` | Medium | ✅ Done
| 1.4 | Performance | `fragments/header.html` | High | ✅ Done
| 1.5 | Cleanup | `nav.html`, `footer.html` | Low | ✅ Done
| 2.1 | Config | `nav.html` | Low | ✅ Done
| 2.2 | Responsiveness | `nav.html`, `script.js` | Medium | ✅ Done
| 2.3 | UX | `script.js` | Medium | ✅ Done
| 2.4 | CSS Bug | `nav.html` | Low | ✅ Done
| 2.5 | UX | `script.js` | Low | ✅ Done
| 3.1 | UX | `register.html` | Low | ✅ Done
| 3.2 | UX | `register.html` | Medium | ✅ Done
| 3.3 | Responsive | `booking.html` | Low |
| 3.4 | UX | `booking.html` | Low |
| 3.5 | Bug | `edit-appointment.html` | High |
| 3.6 | Functionality | `payment.html` | High |
| 3.7 | Logic Bug | `payment.html` | Medium |
| 3.8 | UX | `editProfile.html` | Low |
| 3.9 | UX | `editProfile.html` | Low |
| 3.10 | Consistency | `appointment-history.html` | Low |
| 3.11 | Feature | `receipt.html` | Low |
| 4.1 | Layout | `adminIndex.html` | Low |
| 4.2 | Code Quality | `adminIndex.html` | Low |
| 4.3 | CSS Bug | `adminFragments.html` | Low |
| 4.4 | UX | `adminFragments.html` | Medium |
| 4.5 | Clarity | `listBarber.html` | Low |
| 4.6 | UX | `listBarber.html` | Low |
| 4.7 | Bug | `listAppointment.html` | Medium |
| 4.8 | UX | `listAppointment.html` | Medium |
| 4.9 | UX | `listAppointment.html` | Medium |
| 4.10 | Layout | `adminLogin.html` | Low |
| 4.11 | Security | Admin POST forms | High |
| 5.1 | UX | `booking.html` | Low |
| 5.2 | Bug | `booking.html` | Medium |
| 5.3 | UX | `booking.html` | Low |
| 6.1 | Consistency | All pages | Medium |
| 6.2 | Bug | All admin pages | High |
| 6.3 | Code Quality | `header.html` | Low |
| 6.4 | Feature | All pages | Low |
| 7.1 | a11y | All forms | Medium |
| 7.2 | a11y | Image tags | Medium |
| 7.3 | a11y | Badges | Low |
| 8.1 | Performance | All pages | Medium |
| 8.2 | Performance | Static JS | Medium |
