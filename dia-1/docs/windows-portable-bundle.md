# Windows Portable Bundle Guide

This guide explains how to run the packaged Product Manager app on another Windows PC.

## What The Bundle Is

The Windows bundle is a portable folder/zip created at:

```text
dist\ProductManager-Windows.zip
```

After extraction, the app is started by double-clicking:

```text
ProductManager-Windows\ProductManager.cmd
```

The bundle includes the app UI, Spring Boot backend, a Java runtime, and a private MySQL Server. The user does not need to install Java, MySQL, Node, Maven, or open a browser manually.

## Requirements On The Other Windows PC

- Windows 10 or Windows 11, 64-bit.
- Microsoft Edge installed.
- Enough free disk space to extract and run the bundle. Keep at least 2 GB free.
- The folder must be writable by the current Windows user.
- Ports `18080` and `33107` should be free.

No separate Java or MySQL installation is required for running the portable bundle.

## How To Run On Another PC

1. Copy `ProductManager-Windows.zip` to the Windows PC.
2. Right-click the zip and choose **Extract All**.
3. Open the extracted `ProductManager-Windows` folder.
4. Double-click `ProductManager.cmd`.
5. Wait for the first launch. The first launch can take longer because the bundled MySQL database is initialized.

The app opens in an Edge app-style window. It may still use Edge internally, but the user does not need to manually type a URL or open a browser.

## Important: Do Not Run From Inside The Zip

Always extract the zip first. Running `ProductManager.cmd` directly from inside the compressed zip will not work correctly because the app needs writable folders for database files, uploads, backups, and logs.

## Where Data Is Stored

Inside the extracted `ProductManager-Windows` folder:

```text
data\mysql       Local MySQL database files
data\uploads     Product images and generated QR files
data\backups     Backup SQL files
logs             Startup/runtime logs
```

To move the app to another PC later, copy the whole `ProductManager-Windows` folder, not only `ProductManager.cmd`.

## Backup And Restore

Backups created from the app are stored in:

```text
ProductManager-Windows\data\backups
```

The app can import backups from that folder through **Database Backup Settings**.

## Common Problems

### Windows Shows A Security Warning

Because this is a custom local bundle, Windows may show a warning the first time it runs. Choose **More info** and then **Run anyway** only if the bundle came from your trusted source.

### App Does Not Open

Check these files:

```text
ProductManager-Windows\logs
```

Also confirm that ports `18080` and `33107` are not already used by another program.

### Edge Does Not Open The App Window

Install or update Microsoft Edge, then double-click `ProductManager.cmd` again.

### First Launch Is Slow

That is expected. The first launch initializes the private MySQL database and imports the seed data.

## How To Rebuild The Bundle

Rebuilding is only needed on the development machine after code or data changes.

Build script:

```text
packaging\windows\Build-WindowsBundle.ps1
```

Run from the repository root:

```powershell
.\packaging\windows\Build-WindowsBundle.ps1
```

Development machine requirements for rebuilding:

- JDK with `jlink` available.
- Node.js and npm.
- Existing frontend dependencies installed.
- Maven wrapper from this repo.
- MySQL Server 8.0 installed at `C:\Program Files\MySQL\MySQL Server 8.0`.
- Source database available at `localhost:3306/local`.
- Current build script expects MySQL user `root` with password `new_password`.

The build creates:

```text
dist\ProductManager-Windows
dist\ProductManager-Windows.zip
```

The zip is the file to share with another Windows PC.
