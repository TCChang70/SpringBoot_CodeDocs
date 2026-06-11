# Apache Tomcat 安裝與設定指南（最新 Tomcat 為主）

此文件內容以 Apache Tomcat 最新穩定版（Tomcat 11 / 10 / 9 系列）為主軸，提供在 Windows 環境下的安裝、設定與驗證步驟。教學採用繁體中文撰寫，並註明 Java EE / Jakarta EE 相容性差異與遷移建議。

> 重要：Tomcat 10 及以後的版本已從 Java EE 的 javax.* 套件切換到 Jakarta EE 的 jakarta.* 套件。若您使用的應用程式基於 Java EE (javax.*)，請使用 Tomcat 9 或使用 Tomcat 提供之遷移工具將應用轉換為 Jakarta。

目錄
- 版本與相容性速覽
- 在 Windows 上安裝 Tomcat（以 Tomcat 10 為示範）
- 設定 Java 環境 (JDK)
- 下載與安裝 Tomcat
- 基本設定（用戶、連接器、環境變數、服務安裝）
- 部署範例 Web 應用 (.war)
- 變更日志與常見問題
- 附錄：遷移與回退建議

## 版本與相容性速覽

- Tomcat 11.x：實作 Jakarta EE 11（API 由 jakarta.* 命名空間提供）。建議新專案或已完成 Jakarta 移轉的專案採用。
- Tomcat 10.1.x：實作 Jakarta EE 10。也屬於 Jakarta 系列，Java 版本需求與特性略有差異。
- Tomcat 9.0.x：實作 Java EE（javax.*）。若您的現有應用仍使用 javax.*，選擇 Tomcat 9 可最少改動。

選擇建議：
- 新開發、或已採用 Jakarta EE 的專案：優先選擇 Tomcat 11.x（最新功能、安全性與長期維護）。
- 尚未遷移的 Java EE 應用：使用 Tomcat 9.x；或使用 Tomcat 10/11 並先以官方遷移工具轉換應用程式。

## 在 Windows 上安裝 Tomcat（以 Tomcat 10 為示範）

本節示範在 Windows 10 / 11 上安裝 Tomcat，採用 zip 發行包（免安裝）與 Service 安裝兩種常見方法。

先決條件
- 已安裝合適版本的 JDK：Tomcat 10 需要 Java 17 或更新版本（視官方說明而定）。請安裝 Oracle JDK、OpenJDK 或其他相容發行版。
- 有管理員權限（若要安裝為 Windows 服務）。

1) 檢查 Java 版本

在 PowerShell 執行：

```powershell
java -version
javac -version
```

確保輸出顯示 Java 17 或更高（或根據 Tomcat 官方需求）。若無，請至 https://adoptium.net 或 JDK 官方頁下載並安裝。

2) 下載 Tomcat

- 官方網站：https://tomcat.apache.org/。
- 下載對應版的二進位 zip（Core -> zip）或 Windows Service 安裝程式（若有提供）。

示例：下載 Tomcat 10.0.x zip，解壓縮到您想安裝的目錄（例如 C:\apache-tomcat-10）

3) 設定環境變數（建議）

- CATALINA_HOME：指向 Tomcat 解壓目錄（例如 C:\apache-tomcat-10）。
- JAVA_HOME：指向 JDK 安裝目錄（例如 C:\Program Files\Java\jdk-17）。

在系統環境變數中新增或修改：

1. 開啟「系統屬性」->「進階」->「環境變數」。
2. 在系統變數中新增 `JAVA_HOME` 和 `CATALINA_HOME`。
3. 編輯 `Path`，加入 `%JAVA_HOME%\bin`（若尚未加入）。

4) 啟動 Tomcat

進入 Tomcat 解壓目錄的 `bin`，執行：

```powershell
cd C:\apache-tomcat-10\bin
.\startup.bat
```

或要安裝為服務（需管理員權限）：

```powershell
.\service.bat install
```

啟動後，開啟瀏覽器至 http://localhost:8080/ 檢查 Tomcat 歡迎頁是否顯示。

5) 設定管理介面（可選）

Tomcat 預設不啟用 web 管理應用以避免安全風險。若需要使用 Manager 或 Host Manager，請：

1. 在 `conf/tomcat-users.xml` 中新增使用者及角色（只在受信任網段或開發環境使用）：

```xml
<role rolename="manager-gui"/>
<role rolename="admin-gui"/>
<user username="admin" password="yourStrongPassword" roles="manager-gui,admin-gui"/>
```

2. 重新啟動 Tomcat。

注意：生產環境請使用更安全的認證與網路控管（反向代理、IP 限制、HTTPS）。

## 部署範例 Web 應用 (.war)

1. 將 `yourapp.war` 複製到 `webapps` 目錄。Tomcat 會自動展開與部署。
2. 或使用 Manager UI 上傳（若已啟用）。

驗證：在瀏覽器開啟 http://localhost:8080/yourapp/。

## 常見設定與調校

- 變更連接埠：編輯 `conf/server.xml` 中的 Connector（預設 8080）。
- 設定 HTTPS：建議使用反向代理（Nginx/Apache HTTPD）或在 Tomcat 上配置 TLS（需 keystore）。
- 記憶體與 GC：透過 `setenv.bat` 設定 `CATALINA_OPTS` 或 `JAVA_OPTS`，例如：

```powershell
set CATALINA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
```

把上述命令加到 `bin/setenv.bat`（若不存在則建立）。

## Jakarta / Java EE 遷移提示

- 若應用使用 javax.* 套件（Java EE），要升級到 Tomcat 10/11，必須將程式碼與依賴改為 jakarta.*。Apache 提供「Apache Tomcat migration tool for Jakarta EE」來協助轉換 WAR 檔。
- 轉換步驟（離線工具示例）：
  1. 下載 Migration Tool（官方網站）。
  2. 使用工具轉換原始 WAR，產出可在 Tomcat 10/11 上執行的 WAR。

## 測試與驗證

1. 瀏覽器開啟 http://localhost:8080/。
2. 部署 sample.war 並檢查應用可用。
3. 檢查 Tomcat 日誌（`logs/catalina.out`, `logs/localhost.*`）以排錯。

## 常見問題 (FAQ)

- 問：啟動時出現 Java 版本錯誤？
  答：確認 `JAVA_HOME` 指向正確 JDK，且 `java -version` 回傳符合 Tomcat 要求的版本。

- 問：應用使用 javax.*，但部署在 Tomcat 10 失敗？
  答：Tomcat 10 使用 jakarta.*，必須將應用轉換或使用 Tomcat 9。


---

完成：已更新為以 Tomcat 最新系列（Tomcat 10）為主軸，並提供遷移注意事項與 Windows 實作步驟。
