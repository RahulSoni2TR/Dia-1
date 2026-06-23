# Product Manager (Jewellery Store Management System)

A self-contained, offline-first desktop management application designed specifically for jewellery showrooms. Built with a high-performance **React frontend** and **Spring Boot backend**, it runs as a fully portable Windows desktop app with zero pre-requisites.

---

## 🚀 Key Features

* **Multi-Category Product Inventory**: Manage complex parameters for different jewellery classes:
  * **Diamond** (Pieces, Diamond Carat, Rate, Other Stones)
  * **Open Setting** (Vilandi CT, Diamonds Weight, Beads, Pearls, SS Pearls, Other Stones)
  * **Vilandi** (Vilandi Rate, Stones, Beads, Pearls, SS Pearls, Real Stone, Fitting, Mozonite)
  * **Jadtar** (Stones, Beads, Pearls, SS Pearls, Real Stone, Fitting, Vilandi, Mozonite)
  * **Plain Gold/Chains**
* **Set Prices & Auto-Karat Calculation**: Set 24K gold price to automatically scale rates for subordinate karats (22K, 18K, 14K, and 9K/10K). Displays real-time prices in a scrolling market marquee.
* **Estimate Sheets & Logs**: Easily calculate estimates for customers, log enquiries/sales, and export print-ready snapshots.
* **Purity Verification Alarms**: Monitor dynamic product verification status and get unverified/expiry warnings based on configurable verification frequency.
* **Standalone Windows Portability**: Uses a C# wrapper to initialize a bundled, offline database (MySQL Server) and a private Java Runtime (JRE), launching the app directly in borderless Microsoft Edge App Mode.
* **Cryptographic License Locks**: Hardware-bound license verification using asymmetric RSA-2048 digital signatures, featuring an offline 10-day trial mode.
* **Automated Zip Backups**: Backup configuration tools compress database logs, product images, and QR codes into standard `.zip` files for data migration.

---

## 🛠️ Technology Stack

* **Frontend**: React.js, Vite, HTML5, Vanilla CSS, JS
* **Backend**: Spring Boot 3.x, Java 17, JPA / Hibernate, Spring Security, PDFBox, ZXing (QR Generator)
* **Database**: MySQL Server 8.0
* **Wrapper**: C# Launcher (.NET Framework 4.8)

---

## 📂 Project Structure

```text
dia-1/
├── frontend/                     # React Single Page Application (SPA)
│   ├── src/
│   │   ├── components/           # UI Components (Login, Dashboard, SetPrice, etc.)
│   │   ├── App.jsx               # Main React entrypoint
│   │   └── App.css               # Styling definitions
│   └── package.json              # Frontend Node dependencies
├── src/                          # Spring Boot Java Backend
│   ├── main/java/com/example/    # Java sources
│   │   └── webapp/
│   │       ├── controller/       # Web Controller / API Mappings
│   │       ├── models/           # Data entities (Product, Orders, Rate)
│   │       ├── security/         # Security config, License filters
│   │       └── service/          # Business logic, Backup & License services
│   └── main/resources/
│       ├── static/               # Bundled production frontend code
│       └── application.properties # Spring configuration properties
├── packaging/
│   └── windows/                  # Portable bundle configurations & C# code
│       ├── clean_seed.sql        # Fresh database schema seed
│       └── Build-WindowsBundle.ps1 # Build script to create portable installer
└── pom.xml                       # Maven build configuration
```

---

## 💻 Development Setup

### 1. Run the Frontend (Dev mode)
```bash
cd frontend
npm install
npm run dev
```
The client app will launch at `http://localhost:5173`.

### 2. Run the Backend
Ensure you have a local MySQL instance running and configure connection settings in `src/main/resources/application.properties`.
```bash
./mvnw spring-boot:run
```
The server api will start at `http://localhost:8080`.

---

## 📦 Packaging the Portable Windows Bundle

To package a clean, self-contained zip file ready to install on a client's computer:

1. Open PowerShell as Administrator in the repository root.
2. Build a clean production package:
   ```powershell
   .\Build-CleanWindowsBundle.cmd
   ```
   *(Or run the build script directly: `powershell -ExecutionPolicy Bypass -File .\packaging\windows\Build-CleanWindowsBundle.ps1 -Clean`)*
3. Once compiled, your installer package will be output to:
   ```text
   dist\ProductManager-Windows.zip
   ```

---

## ⚙️ Client Installation

1. Extract `ProductManager-Windows.zip` to a writable directory (e.g. `C:\ProductManager`).
2. Double-click `ProductManager.exe` to run. (The launcher will initialize the local MySQL server and JRE sandbox automatically).
