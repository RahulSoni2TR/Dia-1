# Jewellery Store Product Manager (Showroom Management System)

Welcome to the **Jewellery Store Product Manager** repository. This is an offline-first showroom management system designed to streamline jewellery inventory tracking, pricing calculation, estimate generation, and customer enquiry logging.

Depending on the UI architecture and feature set you require, the codebase is split into specific branches. Please refer to the guide below to select the appropriate branch for your needs.

---

## 🔀 Branch Selection Guide

Please checkout or switch to the corresponding branch depending on the technology stack and features you need:

| Branch Name | Frontend UI Stack | Backend Stack | Key Features Included | Best For |
| :--- | :--- | :--- | :--- | :--- |
| [**`react_updates`**](https://github.com/RahulSoni2TR/Dia-1/tree/react_updates) *(Recommended)* | **React.js SPA** (Vite, HTML5, Vanilla CSS, JS) | **Spring Boot 3.x** (Java 17, JPA, Hibernate) | Modern Single Page App UI, automatic karat scaling, scrolling price marquee, interactive estimate generator, purity alerts, offline asymmetric licensing, automated zip backups, Edge App mode wrapper. | Active production deployments, modern interactive UX. |
| [**`updated`**](https://github.com/RahulSoni2TR/Dia-1/tree/updated) | **Classic HTML** (Thymeleaf, AJAX, CSS, jQuery) | **Spring Boot 3.x** (Java 17, JPA, Hibernate) | Server-side rendered HTML, batch updating, custom tags, enquiry/sales logs, PDF reports, estimate snapshots, price history, verification alarms. | Classic server-side rendering architecture. |
| **`master`** | **Basic HTML** (Thymeleaf, CSS, JS) | **Spring Boot 3.x** (Java 17) | Core inventory tracking, basic templates. | Initial baseline version. |

---

## 🚀 Getting Started

### 1. Checkout your desired branch
```bash
# To get the modern React-based version:
git checkout react_updates

# To get the classic Spring Boot HTML version:
git checkout updated
```

### 2. Follow Branch Documentation
Each branch contains a comprehensive `README.md` at its root detailing specific installation requirements, development setup, and packaging scripts for that branch's architecture.
