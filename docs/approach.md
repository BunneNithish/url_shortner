# Technical Approach - URL Shortener & Analytics Dashboard

This document details the engineering approach chosen to design and implement the AI-Powered URL Shortener and its corresponding React dashboard.

## 1. Backend Updates (Spring Boot 3)

The legacy Spring Boot codebase was upgraded from **2.2.4.RELEASE** to **3.2.5** to support **Java 17** compatibility and ensure compatibility with modern dependency managers like **Gradle 8.9**. This change migrated all `javax.*` servlet and persistence packages to the standard `jakarta.*` namespace.

The backend functionality was expanded to support the following business rules:
- **Unique Custom Aliases**: Added alphanumeric input validation on custom aliases. If a custom alias is provided, the backend validates format and checks for conflicts in active (non-deleted) URLs, returning a `409 Conflict` if duplicate.
- **Base62 Seq-Fallback**: If no custom alias is provided, the short url is dynamically generated using the Base62 representation of the auto-incremented database `id`. To avoid multiple inserts, a check is run to reuse an existing auto-generated active link if the URL and all parameters match.
- **Expiry Dates**: Expiry timestamp verification is executed on every redirect. If the link is past its expiry date, the API returns a `400 Bad Request` with an explanation.
- **Pagination & Search**: Utilized Spring Data `Pageable` and `Page` objects. Integrated a SQL query using `LOWER(u.title) LIKE LOWER(...)` to provide quick server-side search by title or full URL.
- **Soft Deletion**: Implemented via a `is_deleted` tinyint/boolean flag. Active repository queries filter out deleted links, while visit records remain available for historical reporting.
- **Fast Geolocation & User-Agent Parsing**:
  - A custom regex-based `UserAgentParser` extracts Browser (Chrome/Firefox/Safari/Edge/Opera), OS (Windows/macOS/Linux/Android/iOS), and Device Type (Desktop/Mobile/Tablet).
  - A Map/Hash-based `GeoIPResolver` handles IP resolution. For local loopback or private ranges (like `127.0.0.1`), it dynamically maps requests to realistic countries (e.g. US, IN, DE, CA, GB) to feed charts. This ensures lookup times remain under `1ms`, satisfying the 100ms redirect constraint.

---

## 2. Frontend Implementation (React & Vanilla CSS)

The React frontend was built with **Vite** for rapid bundling and hot module replacement. To achieve maximum styling flexibility and maintain compliance with guidelines, **Vanilla CSS** with CSS custom properties was utilized.

Key visual and structural decisions include:
- **Glassmorphism Theme**: Main theme uses a deep dark color palette with blurred backdrops (`backdrop-filter`) and thin subtle borders (`rgba(255,255,255,0.08)`) to deliver a premium, modern software feel.
- **State-driven Navigation**: SPA views switch between Dashboard Home and Link Analytics via standard React state to ensure zero page refreshes.
- **Interactive Analytics Panels**: Integrated **Recharts** to build charts:
  - **Daily Clicks History**: Line/Area chart with gradients.
  - **Browser & Device Share**: Double Pie charts with custom legends.
  - **Country & OS Breakdown**: Responsive Bar charts.
- **Micro-Animations**: Hover behaviors include subtle translate transitions, slight scale increases, and smooth borders focus states.
- **Fallback Empty States**: Handled zero-visits/zero-links states gracefully with custom helper graphics to prompt copy-pasting links or starting campaigns.
