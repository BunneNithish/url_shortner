# Architecture Tradeoffs

This document details key architectural compromises made during design.

## 1. Local Mock Geolocation vs. Live API Integration
- **Tradeoff**: Live Geolocation API vs. Local Maps lookup.
- **Decision**: Implemented a map/hash-based local `GeoIPResolver` fallback.
- **Rationale**: Connecting to external Geo-IP lookup endpoints during redirects introduces variable network latency ranging from **150ms to 1s+**, directly violating the **under 100ms** redirect constraint. A local helper operates in **<1ms**, maintaining response time under **5ms**.

## 2. In-Memory Grouping vs. Database SQL Grouping for Analytics
- **Tradeoff**: SQL aggregations (`GROUP BY`) vs. In-Memory Stream mapping.
- **Decision**: Performed aggregations inside the Java `UrlService` using Java Streams.
- **Rationale**: SQL syntax for date grouping (e.g. `DATE()`, `FORMAT()`) varies widely between MySQL (production) and H2 (testing). Performing grouping in Java ensures **100% database-agnostic code**, allowing unit tests to run on H2 in-memory databases with zero configuration divergence from production.

## 3. Temporary Placeholder on Sequential Short-code Creation
- **Tradeoff**: Multiple INSERT/UPDATE statements vs. pre-computed UUIDs.
- **Decision**: Saved the URL record first with a short placeholder UUID, encoded its auto-increment ID to Base62, and ran an UPDATE.
- **Rationale**: Base62 seq encoding depends on the database assigning an ID. By creating a temporary placeholder, we leverage the native db auto-increment sequence to maintain short, readable sequences (like `b`, `c`, `d`) instead of lengthy 36-character UUID keys, maximizing user clickability.

## 4. SPA State Tabs vs. React Router DOM
- **Tradeoff**: Virtual DOM State routing vs. Browser Path routing.
- **Decision**: Maintained active tab views via React state variable (`activeView`).
- **Rationale**: Keeps the bundle light and prevents page refresh requirements, ensuring the entire control experience feels instantaneous.
