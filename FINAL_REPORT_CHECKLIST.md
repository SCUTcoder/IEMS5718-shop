# Final Report Checklist for IEMS5718 Shop

Source PDF: `AssignmentMarkingChecklistp6p7final.pdf`

Online site: https://s14.iems5718.iecuhk.cc

Admin account:
- Email: `admin@shop.local`
- Password: `Admin@12345`

Normal user account:
- Email: `user@shop.local`
- Password: `User@12345`

## PDF Summary

### Phase 6: Extensions

Phase 6 is optional bonus work. The checklist offers many possible extensions, but the bonus is capped at 7 marks even if more than 7 marks worth of features are implemented.

Recommended features to claim for this project:
- Mashup/social plugin on the main page: share buttons and Facebook social plugin.
- SEO/user-friendly URLs: category and product names are included in URLs, e.g. `/3-test/20-product`.
- AJAX infinite scroll: product cards load in batches on the main page.
- HTML5 drag-and-drop upload in admin: product images and videos can be selected by drag/drop, with image previews and file type validation.
- Product detail media gallery: multiple images and product videos are selectable from thumbnails; products with video show video first.

These exceed the Phase 6 bonus cap, so the final report can focus on the best documented ones.

### Phase 7: Peer Hacking

Phase 7 runs from 8 May 2026 6PM to 10 May 2026. It is an ethical hacking exercise against classmates' shops and a defense exercise for your own shop.

Action items:
- Backup code, configuration files, and deployment state before Phase 7.
- Optional: scan only your own website with a vulnerability scanner, then fix findings.
- Monitor availability; downtime can cause overall mark deduction.
- If reporting classmates' bugs, use the Microsoft Form with the Blackboard random ID and required format.
- If your shop receives a valid bug report, fix it before final report submission and document the fix.

### Final Report

Deadline: 12 May 2026, no extension.

The final report should document Phases 1-6, checkout/order workflow, security defenses, and vulnerabilities/bugs fixed. Be ready for final Q&A about implementation details and security protections.

## Final Report Content Plan

### 1. Phase 1: Cloud and Web Server Setup

Report evidence:
- Screenshot of the AWS/Azure/cloud VM dashboard, virtual network, and firewall/security group.
- Screenshot or browser proof that `https://s14.iems5718.iecuhk.cc` is accessible.
- Header proof that the server version/language is not exposed. Nginx returns `server: nginx` without a version.
- Directory indexing proof, e.g. `/images/` or another directory should not show a file listing.

Architecture summary:
- Nginx serves static frontend files and routes API requests.
- Spring Boot backend runs on port 8080 behind Nginx.
- SQLite database stores categories, products, users, sessions, and orders.
- Uploaded product media are stored as static image/video files and referenced by product records.

### 2. Phase 2 and Phase 4: Products, Categories, and Admin Panel

Completed features:
- Main page displays products and categories.
- Category filters and SEO category URLs are supported.
- Each product has a detail page.
- Frontend uses JavaScript/AJAX to fetch products/categories and render the page dynamically.
- Admin can create, update, soft-delete, and reorder products.
- Admin can create/update/delete categories.
- Uploaded product images are resized into thumbnails by the backend image service.
- Admin panel is protected: guest and normal user are blocked; only admin can access admin functions.

Suggested screenshots:
- Main page with products and category filter.
- A category-specific page.
- Product detail page.
- Admin product form and existing products table.
- Admin category management table.
- Authorization checks: guest, normal user, and admin access attempts.

### 3. Phase 3: Shopping Cart

Completed features:
- Add to cart without page reload.
- Cart dropdown shows added items.
- Quantity can be increased/decreased.
- Total price updates dynamically.
- Reducing quantity to zero removes the item.
- Cart state persists in browser local storage.

Suggested screenshots:
- Add a product to cart.
- Cart dropdown with quantity controls.
- Total price after changing quantity.
- Item removed after quantity reaches zero.

### 4. Phase 4: Authentication and Security

Completed defenses:
- Passwords are hashed with BCrypt.
- Authentication tokens are random and only token hashes are stored in the database.
- Auth cookie is HttpOnly, SameSite=Lax, and Secure over HTTPS.
- CSRF token is required for state-changing authenticated requests.
- Admin product/category writes require both admin role and CSRF token.
- Inputs are normalized and validated before saving.
- JPA repositories avoid manual SQL string concatenation for normal CRUD operations.
- Output is escaped in frontend rendering paths to reduce XSS risk.
- Security headers include CSP, X-Content-Type-Options, X-Frame-Options, Referrer-Policy, Permissions-Policy, COOP, and CORP.
- Session fixation is mitigated by issuing a new random auth token at login.

Suggested screenshots:
- Register page.
- Login page.
- Change password page.
- Logout behavior.
- Failed admin API request as guest/normal user.
- Failed no-CSRF admin mutation request returning 403.

### 5. Phase 5: Checkout Workflow

Completed features:
- Checkout creates an order from cart items for authenticated users.
- Stripe checkout session is created when Stripe configuration is present.
- Stripe webhook validates signature when webhook secret is configured.
- Order digest is recalculated before marking payment complete.
- User can view recent orders from profile page.
- Admin can view all orders in admin panel.

Suggested screenshots:
- Cart before checkout.
- Checkout/create response or Stripe redirect.
- Admin order list showing created order.
- User profile/recent orders page.
- Completed/verified payment status if a paid Stripe test flow is performed.

### 6. Phase 6: Extensions to Claim

Recommended claim set:

1. Mashup/social plugin
   - Main page includes Facebook social plugin plus X and WhatsApp share links.
   - Social metadata uses the public site URL and public image URL.

2. SEO/user-friendly URLs
   - Category URLs include category id and name.
   - Product URLs include category id/name and product id/name.
   - Newly inserted products automatically generate SEO links from their database fields.

3. AJAX infinite scroll
   - Main page uses `IntersectionObserver` to load product batches without page reload.

4. HTML5 drag-and-drop admin upload
   - Admin image/video upload zones support drag/drop and paste.
   - Image files are validated and preview thumbnails are rendered.
   - Invalid file types are rejected.

5. Multiple image/video product gallery
   - Product detail page builds a media gallery from product images and video.
   - Users can switch media via thumbnails.
   - If a product has a video, the video is rendered first.

### 7. Security Vulnerabilities or Bugs Fixed

Document these fixes:
- Fixed SEO product path loading so extension URLs render the full product page instead of unstyled HTML.
- Fixed cart media fallback so products with video do not show broken image icons.
- Fixed admin product/category write APIs so logged-in admins still need a valid CSRF token.
- Fixed product media ordering so videos display before images when a product has video.
- Fixed social share URLs from placeholder domain to the deployed shop URL.

## Phase 7 Reporting Template

Use this format when reporting a classmate's vulnerability:

- Shop name with vulnerability: `sXX`
- Your Blackboard random ID: `[fill in from Blackboard]`
- Type of vulnerability: `[OWASP type]`, e.g. `[XSS]`, `[Broken Access Control]`, `[CSRF]`
- Content: answer the Microsoft Form questions clearly.
- Proof: attach at least one screenshot.

For non-security bugs:
- Prefix with assignment requirement number, e.g. `[P4-1]`.
- Confirm it reproduces in both Firefox and Chrome.
- Report at most 3 non-security bugs.

## Items Requiring Manual Evidence

These cannot be completed from source code alone and should be added to the report manually:
- SID.
- Blackboard random ID for Phase 7.
- Cloud provider dashboard screenshots.
- Firewall/security group screenshots.
- Optional scanner output if you choose to run a vulnerability scanner.
- Payment completion screenshot from a real Stripe test payment flow, if available.

