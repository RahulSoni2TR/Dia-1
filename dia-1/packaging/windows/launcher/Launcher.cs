using System;
using System.IO;
using System.Diagnostics;
using System.Threading;
using System.Net;
using System.Windows.Forms;
using System.Drawing;
using System.Net.NetworkInformation;

namespace ProductManagerLauncher
{
    class SplashForm : Form
    {
        protected override CreateParams CreateParams
        {
            get
            {
                const int CS_DROPSHADOW = 0x20000;
                CreateParams cp = base.CreateParams;
                cp.ClassStyle |= CS_DROPSHADOW;
                return cp;
            }
        }
    }

    class Program
    {
        private static bool noUiGlobal = false;
        private static Form loadingForm;
        private static Label statusLabel;
        private static Mutex singleInstanceMutex;

        static bool IsPortInUse(int port)
        {
            try
            {
                IPGlobalProperties ipGlobalProperties = IPGlobalProperties.GetIPGlobalProperties();
                IPEndPoint[] tcpConnInfoArray = ipGlobalProperties.GetActiveTcpListeners();
                foreach (IPEndPoint tcpi in tcpConnInfoArray)
                {
                    if (tcpi.Port == port)
                    {
                        return true;
                    }
                }
            }
            catch {}
            return false;
        }

        static void StartLoadingScreen()
        {
            Thread thread = new Thread(() =>
            {
                Application.EnableVisualStyles();
                
                loadingForm = new SplashForm();
                loadingForm.Width = 450;
                loadingForm.Height = 200;
                loadingForm.FormBorderStyle = FormBorderStyle.None;
                loadingForm.StartPosition = FormStartPosition.CenterScreen;
                loadingForm.BackColor = Color.FromArgb(24, 24, 27); // Modern dark theme (zinc-900)
                
                // Add a subtle border
                loadingForm.Paint += (s, pe) => {
                    using (Pen pen = new Pen(Color.FromArgb(63, 63, 70), 1)) // zinc-700
                    {
                        pe.Graphics.DrawRectangle(pen, 0, 0, loadingForm.Width - 1, loadingForm.Height - 1);
                    }
                };

                // Title label
                Label titleLabel = new Label();
                titleLabel.Text = "PRODUCT MANAGER";
                titleLabel.Font = new Font("Segoe UI", 16, FontStyle.Bold);
                titleLabel.ForeColor = Color.White;
                titleLabel.Location = new Point(30, 40);
                titleLabel.AutoSize = true;
                loadingForm.Controls.Add(titleLabel);

                // Subtitle / Status label
                statusLabel = new Label();
                statusLabel.Text = "Starting application...";
                statusLabel.Font = new Font("Segoe UI", 10, FontStyle.Regular);
                statusLabel.ForeColor = Color.FromArgb(161, 161, 170); // zinc-400
                statusLabel.Location = new Point(32, 90);
                statusLabel.Width = 380;
                statusLabel.Height = 30;
                loadingForm.Controls.Add(statusLabel);

                // Loading bar (Visual accent)
                Panel progressBar = new Panel();
                progressBar.BackColor = Color.FromArgb(59, 130, 246); // Blue-500
                progressBar.Location = new Point(30, 140);
                progressBar.Width = 390;
                progressBar.Height = 3;
                loadingForm.Controls.Add(progressBar);

                Application.Run(loadingForm);
            });
            thread.SetApartmentState(ApartmentState.STA);
            thread.Start();
        }

        static void UpdateStatus(string text)
        {
            if (loadingForm != null && loadingForm.IsHandleCreated)
            {
                try
                {
                    loadingForm.BeginInvoke(new Action(() => {
                        if (statusLabel != null) statusLabel.Text = text;
                    }));
                }
                catch {}
            }
        }

        static void CloseLoadingScreen()
        {
            if (loadingForm != null && loadingForm.IsHandleCreated)
            {
                try
                {
                    loadingForm.BeginInvoke(new Action(() => {
                        loadingForm.Close();
                    }));
                }
                catch {}
            }
        }

        static void ShowError(string message)
        {
            CloseLoadingScreen();
            Console.WriteLine(message);
            if (!noUiGlobal)
            {
                MessageBox.Show(
                    message + "\n\nPlease check the logs in C:\\Users\\<Username>\\.productmanager\\logs\\ for details or contact your administrator.",
                    "Application Launch Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
        }

        [STAThread]
        static void Main(string[] args)
        {
            string root = AppDomain.CurrentDomain.BaseDirectory;

            bool createdNew;
            singleInstanceMutex = new Mutex(true, "Local\\ProductManagerSingleInstanceMutex", out createdNew);
            if (!createdNew)
            {
                // Parse arguments to check for -NoUi
                bool isNoUi = false;
                foreach (string arg in args)
                {
                    if (arg.Equals("-NoUi", StringComparison.OrdinalIgnoreCase))
                    {
                        isNoUi = true;
                    }
                }

                if (!isNoUi)
                {
                    int redirectPort = 18080;
                    string redirectPortFile = Path.Combine(root, "port.txt");
                    if (File.Exists(redirectPortFile))
                    {
                        try
                        {
                            string content = File.ReadAllText(redirectPortFile).Trim();
                            int parsedPort;
                            if (int.TryParse(content, out parsedPort) && parsedPort > 0 && parsedPort < 65536)
                            {
                                redirectPort = parsedPort;
                            }
                        }
                        catch {}
                    }

                    try
                    {
                        string edge = FindEdge();
                        if (edge != null)
                        {
                            string edgeProfile = Path.Combine(root, @"data\edge-profile");
                            Process.Start(edge, string.Format("--app={0} --user-data-dir=\"{1}\"", "http://127.0.0.1:" + redirectPort, edgeProfile));
                        }
                        else
                        {
                            Process.Start("http://127.0.0.1:" + redirectPort);
                        }
                    }
                    catch {}
                }
                return;
            }
            
            // Check if NoUi switch or smoke test duration is passed
            bool noUi = false;
            int smokeTestSeconds = 0;
            foreach (string arg in args)
            {
                if (arg.Equals("-NoUi", StringComparison.OrdinalIgnoreCase))
                {
                    noUi = true;
                }
                if (arg.StartsWith("-SmokeTestSeconds=", StringComparison.OrdinalIgnoreCase))
                {
                    int.TryParse(arg.Substring(18), out smokeTestSeconds);
                }
            }

            noUiGlobal = noUi;

            if (!noUiGlobal)
            {
                StartLoadingScreen();
            }

            string runtimeJava = Path.Combine(root, @"runtime\bin\java.exe");
            string javaExe = File.Exists(runtimeJava) ? runtimeJava : "java";
            string mysqlBin = Path.Combine(root, @"mysql\bin");
            string mysqld = Path.Combine(mysqlBin, "mysqld.exe");
            string mysql = Path.Combine(mysqlBin, "mysql.exe");
            string mysqlAdmin = Path.Combine(mysqlBin, "mysqladmin.exe");
            string jar = Path.Combine(root, @"app\dia-1.jar");
            string seedSql = Path.Combine(root, @"app\seed.sql");
            
            string userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            string persistentRoot = Path.Combine(userProfile, ".productmanager");
            string dataDir = Path.Combine(persistentRoot, @"data\mysql");
            string uploadsDir = Path.Combine(persistentRoot, @"data\uploads");
            string qrDir = Path.Combine(uploadsDir, "qr_codes").Replace("\\", "/") + "/";
            string backupDir = Path.Combine(persistentRoot, @"data\backups");
            string logsDir = Path.Combine(persistentRoot, "logs");
            
            int mysqlPort = 33107;
            int appPort = 18080;
            string portFile = Path.Combine(root, "port.txt");
            if (File.Exists(portFile))
            {
                try
                {
                    string content = File.ReadAllText(portFile).Trim();
                    int parsedPort;
                    if (int.TryParse(content, out parsedPort) && parsedPort > 0 && parsedPort < 65536)
                    {
                        appPort = parsedPort;
                    }
                }
                catch {}
            }
            else
            {
                try
                {
                    File.WriteAllText(portFile, appPort.ToString());
                }
                catch {}
            }
            string appUrl = "http://127.0.0.1:" + appPort;

            try
            {
                // Wait for ports to clear if a previous instance is shutting down
                if (IsPortInUse(appPort) || IsPortInUse(mysqlPort))
                {
                    UpdateStatus("Waiting for previous instance to close...");
                    for (int i = 0; i < 15; i++)
                    {
                        if (!IsPortInUse(appPort) && !IsPortInUse(mysqlPort))
                        {
                            break;
                        }
                        Thread.Sleep(1000);
                    }
                }

                UpdateStatus("Checking configuration and folder structures...");
                EnsureDirectory(dataDir);
                EnsureDirectory(uploadsDir);
                EnsureDirectory(Path.Combine(uploadsDir, "qr_codes"));
                EnsureDirectory(backupDir);
                EnsureDirectory(logsDir);

                string mysqlLog = Path.Combine(logsDir, "mysql.log");
                string mysqlErrLog = Path.Combine(logsDir, "mysql-error.log");
                string appLog = Path.Combine(logsDir, "app.log");
                string appErrLog = Path.Combine(logsDir, "app-error.log");

                if (!File.Exists(mysqld))
                {
                    ShowError("Error: Bundled MySQL database binary was not found.");
                    return;
                }
                if (!File.Exists(jar))
                {
                    ShowError("Error: Backend application file was not found.");
                    return;
                }

                // Migrate data from old application folder if it exists
                UpdateStatus("Migrating existing databases and settings...");
                MigrateOldData(root, persistentRoot);

                // Initialize MySQL database if not already initialized
                string initializedMarker = Path.Combine(dataDir, ".initialized");
                if (!File.Exists(initializedMarker))
                {
                    UpdateStatus("Initializing database schemas... (first boot)");
                    ProcessStartInfo initInfo = new ProcessStartInfo();
                    initInfo.FileName = mysqld;
                    initInfo.Arguments = string.Format("--initialize-insecure --basedir=\"{0}\" --datadir=\"{1}\" --console", Path.Combine(root, "mysql"), dataDir);
                    initInfo.UseShellExecute = false;
                    initInfo.CreateNoWindow = true;
                    using (Process initProc = Process.Start(initInfo))
                    {
                        initProc.WaitForExit();
                        if (initProc.ExitCode != 0)
                        {
                            ShowError("Error: Could not initialize database storage.");
                            return;
                        }
                    }
                    File.WriteAllText(initializedMarker, "");
                }

                // Start MySQL Server Process
                UpdateStatus("Starting local database server...");
                ProcessStartInfo mysqlInfo = new ProcessStartInfo();
                mysqlInfo.FileName = mysqld;
                mysqlInfo.Arguments = string.Format("--no-defaults --standalone --console --basedir=\"{0}\" --datadir=\"{1}\" --port={2} --bind-address=127.0.0.1 --skip-log-bin --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci", Path.Combine(root, "mysql"), dataDir, mysqlPort);
                mysqlInfo.UseShellExecute = false;
                mysqlInfo.CreateNoWindow = true;
                mysqlInfo.RedirectStandardOutput = true;
                mysqlInfo.RedirectStandardError = true;

                Process mysqlProcess = new Process();
                mysqlProcess.StartInfo = mysqlInfo;
                mysqlProcess.OutputDataReceived += (s, eArgs) => { if (eArgs.Data != null) AppendLog(mysqlLog, eArgs.Data); };
                mysqlProcess.ErrorDataReceived += (s, eArgs) => { if (eArgs.Data != null) AppendLog(mysqlErrLog, eArgs.Data); };
                
                mysqlProcess.Start();
                mysqlProcess.BeginOutputReadLine();
                mysqlProcess.BeginErrorReadLine();

                try
                {
                    // Wait for MySQL to start
                    if (!WaitForMysql(mysqlAdmin, mysqlPort))
                    {
                        ShowError("Error: Database server failed to respond.");
                        return;
                    }

                    // Create database schema
                    UpdateStatus("Verifying database configuration...");
                    ProcessStartInfo createDbInfo = new ProcessStartInfo();
                    createDbInfo.FileName = mysql;
                    createDbInfo.Arguments = string.Format("--host=127.0.0.1 --port={0} --user=root --execute=\"CREATE DATABASE IF NOT EXISTS local CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;\"", mysqlPort);
                    createDbInfo.UseShellExecute = false;
                    createDbInfo.CreateNoWindow = true;
                    using (Process dbProc = Process.Start(createDbInfo))
                    {
                        dbProc.WaitForExit();
                        if (dbProc.ExitCode != 0)
                        {
                            ShowError("Error: Could not verify local database schema.");
                            return;
                        }
                    }

                    // Import seed SQL if not done
                    string seedMarker = Path.Combine(dataDir, ".seed-imported");
                    if (File.Exists(seedSql) && !File.Exists(seedMarker))
                    {
                        UpdateStatus("Importing default application data...");
                        ProcessStartInfo importInfo = new ProcessStartInfo();
                        importInfo.FileName = mysql;
                        importInfo.Arguments = string.Format("--host=127.0.0.1 --port={0} --user=root local", mysqlPort);
                        importInfo.UseShellExecute = false;
                        importInfo.RedirectStandardInput = true;
                        importInfo.CreateNoWindow = true;
                        using (Process importProc = Process.Start(importInfo))
                        {
                            using (StreamWriter sw = importProc.StandardInput)
                            {
                                sw.Write(File.ReadAllText(seedSql));
                            }
                            importProc.WaitForExit();
                            if (importProc.ExitCode == 0)
                            {
                                File.WriteAllText(seedMarker, "");
                            }
                            else
                            {
                                Console.WriteLine("Warning: Initial seed data import exited with code " + importProc.ExitCode);
                            }
                        }
                    }

                    // Start Backend Java App
                    UpdateStatus("Starting backend application server...");
                    string uploadsUri = uploadsDir.Replace("\\", "/");
                    if (!uploadsUri.EndsWith("/")) uploadsUri += "/";
                    
                    string javaArgs = string.Format("-jar \"{0}\" " +
                        "--server.address=0.0.0.0 " +
                        "--server.port={1} " +
                        "--spring.datasource.url=\"jdbc:mariadb://127.0.0.1:{2}/local?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Kolkata\" " +
                        "--spring.datasource.username=root " +
                        "--spring.datasource.password= " +
                        "--spring.jpa.hibernate.ddl-auto=none " +
                        "--spring.web.resources.static-locations=\"classpath:/static/,file:/{3}\" " +
                        "--app.server.host=192.168.1.50 " +
                        "--app.server.port={1} " +
                        "--app.base-url=\"http://192.168.1.50:{1}\" " +
                        "--app.qr-dir=\"{4}\" " +
                        "--app.qr-public-path=/uploads/qr_codes/ " +
                        "--app.uploads.path=\"file:/{3}\" " +
                        "--save.uploads.path=\"{5}\" " +
                        "--delete.baseUploadDir.path=\"{5}\" " +
                        "--app.backup.dir=\"{6}\" " +
                        "--spring.cache.type=simple",
                        jar, appPort, mysqlPort, uploadsUri, qrDir, uploadsDir, backupDir);

                    ProcessStartInfo javaInfo = new ProcessStartInfo();
                    javaInfo.FileName = javaExe;
                    javaInfo.Arguments = javaArgs;
                    javaInfo.UseShellExecute = false;
                    javaInfo.CreateNoWindow = true;
                    javaInfo.RedirectStandardOutput = true;
                    javaInfo.RedirectStandardError = true;

                    Process javaProcess = new Process();
                    javaProcess.StartInfo = javaInfo;
                    javaProcess.OutputDataReceived += (s, eArgs) => { if (eArgs.Data != null) AppendLog(appLog, eArgs.Data); };
                    javaProcess.ErrorDataReceived += (s, eArgs) => { if (eArgs.Data != null) AppendLog(appErrLog, eArgs.Data); };

                    javaProcess.Start();
                    javaProcess.BeginOutputReadLine();
                    javaProcess.BeginErrorReadLine();

                    try
                    {
                        // Wait for Backend to start
                        UpdateStatus("Launching user interface...");
                        if (!WaitForApp(appUrl))
                        {
                            ShowError("Error: Backend application server failed to respond.");
                            return;
                        }

                        // Close loading screen right before launching UI window
                        CloseLoadingScreen();

                        if (noUi)
                        {
                            Console.WriteLine("Product Manager started at " + appUrl);
                            if (smokeTestSeconds > 0)
                            {
                                Console.WriteLine("Running smoke test for " + smokeTestSeconds + " seconds...");
                                Thread.Sleep(smokeTestSeconds * 1000);
                            }
                            return;
                        }

                        // Locate and clear Edge favicon cache
                        string edgeProfile = Path.Combine(root, @"data\edge-profile");
                        EnsureDirectory(edgeProfile);
                        string favCache = Path.Combine(edgeProfile, @"Default\Favicons");
                        string favJournal = Path.Combine(edgeProfile, @"Default\Favicons-journal");
                        try { if (File.Exists(favCache)) File.Delete(favCache); } catch {}
                        try { if (File.Exists(favJournal)) File.Delete(favJournal); } catch {}

                        string edge = FindEdge();
                        if (edge != null)
                        {
                            Console.WriteLine("Launching Product Manager in app window...");
                            ProcessStartInfo edgeInfo = new ProcessStartInfo();
                            edgeInfo.FileName = edge;
                            edgeInfo.Arguments = string.Format("--app={0} --user-data-dir=\"{1}\"", appUrl, edgeProfile);
                            edgeInfo.UseShellExecute = false;
                            
                            using (Process edgeProc = Process.Start(edgeInfo))
                            {
                                Thread.Sleep(3000);
                                if (edgeProc.HasExited)
                                {
                                    // Edge delegated to an existing background instance, wait on simple hidden loop
                                    javaProcess.WaitForExit();
                                }
                                else
                                {
                                    // Edge is running as a dedicated process, wait for it to close
                                    edgeProc.WaitForExit();
                                }
                            }
                        }
                        else
                        {
                            Console.WriteLine("Microsoft Edge not found. Opening default browser...");
                            Process.Start(appUrl);
                            javaProcess.WaitForExit();
                        }
                    }
                    finally
                    {
                        Console.WriteLine("Stopping backend application server...");
                        if (!javaProcess.HasExited)
                        {
                            try { javaProcess.Kill(); } catch {}
                        }
                    }
                }
                finally
                {
                    Console.WriteLine("Stopping database server...");
                    ProcessStartInfo shutInfo = new ProcessStartInfo();
                    shutInfo.FileName = mysqlAdmin;
                    shutInfo.Arguments = string.Format("--host=127.0.0.1 --port={0} --user=root shutdown", mysqlPort);
                    shutInfo.UseShellExecute = false;
                    shutInfo.CreateNoWindow = true;
                    try
                    {
                        using (Process shutProc = Process.Start(shutInfo))
                        {
                            shutProc.WaitForExit(5000);
                        }
                    }
                    catch {}

                    if (!mysqlProcess.HasExited)
                    {
                        try { mysqlProcess.Kill(); } catch {}
                    }
                }
            }
            catch (Exception ex)
            {
                ShowError("Fatal launch error: " + ex.Message);
            }
        }

        static void EnsureDirectory(string path)
        {
            if (!Directory.Exists(path))
            {
                Directory.CreateDirectory(path);
            }
        }

        static void AppendLog(string path, string text)
        {
            try
            {
                File.AppendAllText(path, text + Environment.NewLine);
            }
            catch {}
        }

        static bool WaitForMysql(string mysqlAdmin, int port)
        {
            string args = string.Format("--host=127.0.0.1 --port={0} --user=root ping", port);
            for (int i = 0; i < 60; i++)
            {
                ProcessStartInfo psi = new ProcessStartInfo(mysqlAdmin, args);
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
                psi.RedirectStandardOutput = true;
                psi.RedirectStandardError = true;
                try
                {
                    using (Process p = Process.Start(psi))
                    {
                        p.WaitForExit();
                        if (p.ExitCode == 0) return true;
                    }
                }
                catch {}
                Thread.Sleep(1000);
            }
            return false;
        }

        static bool WaitForApp(string appUrl)
        {
            using (WebClient client = new WebClient())
            {
                for (int i = 0; i < 90; i++)
                {
                    try
                    {
                        client.DownloadString(appUrl);
                        return true;
                    }
                    catch
                    {
                        Thread.Sleep(1000);
                    }
                }
            }
            return false;
        }

        static string FindEdge()
        {
            string[] candidates = new string[]
            {
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), @"Microsoft\Edge\Application\msedge.exe")
            };
            foreach (string candidate in candidates)
            {
                if (File.Exists(candidate)) return candidate;
            }
            return null;
        }

        static void MigrateOldData(string root, string persistentRoot)
        {
            string oldData = Path.Combine(root, "data");
            string oldLogs = Path.Combine(root, "logs");

            if (Directory.Exists(oldData))
            {
                EnsureDirectory(persistentRoot);
                string newData = Path.Combine(persistentRoot, "data");
                if (!Directory.Exists(newData))
                {
                    try
                    {
                        Console.WriteLine("Migrating existing database and upload files to persistent user profile location...");
                        CopyDirectory(oldData, newData);
                        Console.WriteLine("Migration complete.");
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Warning: Could not copy data to persistent location: " + ex.Message);
                    }
                }
                if (Directory.Exists(newData))
                {
                    try
                    {
                        Directory.Move(oldData, Path.Combine(root, "data_migrated_backup"));
                        Console.WriteLine("Renamed old data folder to data_migrated_backup.");
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Warning: Could not rename old data folder: " + ex.Message);
                    }
                }
            }
            
            if (Directory.Exists(oldLogs))
            {
                EnsureDirectory(persistentRoot);
                string newLogs = Path.Combine(persistentRoot, "logs");
                if (!Directory.Exists(newLogs))
                {
                    try
                    {
                        Console.WriteLine("Migrating logs...");
                        CopyDirectory(oldLogs, newLogs);
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Warning: Could not copy logs: " + ex.Message);
                    }
                }
                if (Directory.Exists(newLogs))
                {
                    try
                    {
                        Directory.Move(oldLogs, Path.Combine(root, "logs_migrated_backup"));
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Warning: Could not rename old logs folder: " + ex.Message);
                    }
                }
            }
        }

        static void CopyDirectory(string sourceDir, string destinationDir)
        {
            Directory.CreateDirectory(destinationDir);
            foreach (string file in Directory.GetFiles(sourceDir))
            {
                string dest = Path.Combine(destinationDir, Path.GetFileName(file));
                File.Copy(file, dest, true);
            }
            foreach (string folder in Directory.GetDirectories(sourceDir))
            {
                string dest = Path.Combine(destinationDir, Path.GetFileName(folder));
                CopyDirectory(folder, dest);
            }
        }
    }
}
