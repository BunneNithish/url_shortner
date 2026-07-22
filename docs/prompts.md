# AI Assistance & Prompt Methodology

This document summarizes the prompts and assistant development workflows used to build the URL Shortener dashboard.

## 1. Upgrades and Migrations Prompts
- **Objective**: Modernize build configurations and migrate dependencies from Javax to Jakarta.
- **Workflow**: Checked local Java versions (found OpenJDK 17) and Gradle versions (found 8.9). Adjusted properties to ensure compatibility with Spring Boot 3.2.5.

## 2. Database Schema Expansion Prompts
- **Objective**: Add analytics capabilities and features to support metadata (expiration, custom alias, click tracking) while preserving legacy integrity.
- **Workflow**: Updated schema constraints to map sequential base62 logic alongside user-supplied custom aliases.

## 3. UI Aesthetics Prompts
- **Objective**: Design a dark-mode glassmorphism interface using Vanilla CSS to satisfy premium styling guidelines.
- **Workflow**: Created CSS custom properties for gradients and glowing states, combined with Recharts to build responsive charts mapping browser, OS, device, and referrer statistics.
