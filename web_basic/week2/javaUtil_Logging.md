# 針對 Java SE 的 java.util.logging 學習教材
下面我會以「程式開發專家 + 教材從業專家」的身分，針對 Java SE 的 java.util.logging（簡稱 JUL）做一份可直接使用於教學的學習教材，重點放在 Console 與 File 的 logging 實作（含程式範例、config 檔、執行方式、常見練習題與注意事項）。我已準備好可直接執行的範例檔案與逐步教學說明，請直接照著跑或把檔案放入你的專案中。

教學目標（Learning objectives）
- 理解 java.util.logging 的基本概念：Logger、Handler、Formatter、Level、LogManager。
- 學會在程式碼中以程式方式（programmatic）設定 ConsoleHandler 與 FileHandler。
- 學會以 logging.properties 外部設定檔設定 Console 與 File logging（含檔案輪替／大小限制）。
- 能寫自訂 Formatter 以產生所需的 log 格式。
- 知道常見問題與生產環境注意事項（檔案鎖定、併發、權限、效能）。

主要概念簡介
- Logger：取得 log 實例的入口（Logger.getLogger("name")）。log 訊息由 Logger 發出，依階層（package/class）繼承 level 設定。
- Handler：輸出目的地（ConsoleHandler、FileHandler、SocketHandler、MemoryHandler…）。一個 logger 可以有多個 handler。
- Formatter：格式化 log 訊息（SimpleFormatter、XMLFormatter 或自訂 Formatter）。
- Level：日誌等級（SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST）。Logger 與 Handler 都能設定 Level，訊息需同時通過 logger 與 handler 的 level 才會輸出。
- LogManager / logging.properties：JVM 啟動後可由檔案設定全域 logging 行為，或由程式動態設定。

範例檔案（可直接貼到專案中）

1) Console 範例（程式化設定）
```java name=examples/JulConsoleExample.java
package examples;

import java.util.logging.*;

public class JulConsoleExample {
    private static final Logger logger = Logger.getLogger(JulConsoleExample.class.getName());

    public static void main(String[] args) {
        // 移除預設 handler（若有），改用自訂 ConsoleHandler 與自訂 Formatter
        Logger root = Logger.getLogger("");
        // 清空預設 handlers（可選）
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE); // handler 等級
        consoleHandler.setFormatter(new SimpleFormatter()); // 也可以使用自訂 Formatter

        // 設定 root logger level（影響所有 logger）
        root.setLevel(Level.FINE);
        root.addHandler(consoleHandler);

        // 範例 log
        logger.severe("SEVERE - 重大錯誤");
        logger.warning("WARNING - 警告");
        logger.info("INFO - 一般資訊");
        logger.fine("FINE - 偵錯（較低等級）");
        logger.finer("FINER - 更詳細的偵錯");
    }
}
```

2) File 範例（程式化設定、大小與數量輪替）
```java name=examples/LogFileDemo.java
package examples;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.*;

public class LogFileDemo {
    private static final Logger logger = Logger.getLogger(LogFileDemo.class.getName());

    public static void main(String[] args) throws IOException {
        Logger root = Logger.getLogger("");
        // 清除預設 handler（可選）
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }

        // FileHandler constructor: pattern, limit, count, append
        // pattern 可以使用 %u (unique id) 與 %g (generation number)
        // limit = 每個文件最大 bytes ，count = 保留檔案數量
        String pattern = "logs/myapp.%u.%g.log";
        int limit = 1024 * 1024 * 5; // 5 MB per file
        int count = 3;               // 保留 3 個輪替檔案
        boolean append = true;
        // 目標目錄與 pattern（使用 Path 以避免路徑問題）
        Path logDir = Paths.get("logs");
        try {
            // 建立目錄（若已存在則不會拋例外）
        	    if(Files.notExists(logDir)) {
                Files.createDirectories(logDir);
                System.out.println("建立 log 目錄: " + logDir.toAbsolutePath());
        	    }
        } catch (IOException e) {
            System.err.println("無法建立 log 目錄: " + logDir.toAbsolutePath());            
            // 若目錄無法建立，改用 console 並退出或繼續（視需求）
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.ALL);
            ch.setFormatter(new SimpleFormatter());
            root.addHandler(ch);
            root.setLevel(Level.ALL);
            logger.severe("使用 fallback ConsoleHandler，因為無法建立 log 目錄");
            return;
        } 
        
        FileHandler fileHandler = new FileHandler(pattern, limit, count, append);
        fileHandler.setLevel(Level.INFO);
        fileHandler.setFormatter(new SimpleFormatter()); // 可改用自訂格式

        root.setLevel(Level.INFO);
        root.addHandler(fileHandler);
        logger.info("log Path:"+logDir.toAbsolutePath());
        System.out.println("log Path:"+logDir.toAbsolutePath());
        // 產生一些 log 測試輪替
        for (int i = 1; i <= 20; i++) {
            logger.info("Logging line " + i + " - this is a longish message to fill file faster.");
        }

        logger.info("Completed logging");
    }
}

```

3) 自訂 Formatter（範例：簡潔時間+等級+logger+message）
```java name=examples/CustomFormatter.java
package examples;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.text.SimpleDateFormat;

public class CustomFormatter extends Formatter {
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date(record.getMillis())))
          .append(" ")
          .append(record.getLevel().getName())
          .append(" [")
          .append(record.getLoggerName())
          .append("] - ")
          .append(formatMessage(record))
          .append(System.lineSeparator());
        if (record.getThrown() != null) {
            try {
                Throwable t = record.getThrown();
                sb.append("Throwable: ").append(t.toString()).append(System.lineSeparator());
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append("\tat ").append(ste.toString()).append(System.lineSeparator());
                }
            } catch (Exception ex) {
                // ignore exceptions from formatter
            }
        }
        return sb.toString();
    }
}
```

4) logging.properties（以外部設定檔設定 Console 與 File，啟動 JVM 時指定）
```
name=LogPropertiesDemo

# root logger level and handlers
.level = INFO
handlers = java.util.logging.ConsoleHandler, java.util.logging.FileHandler

# ConsoleHandler 設定
java.util.logging.ConsoleHandler.level = FINE
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

# FileHandler 設定（使用 rotation）
java.util.logging.FileHandler.level = INFO
# pattern 範例：logs/app%u.%g.log
java.util.logging.FileHandler.pattern = logs/app%u.%g.log
java.util.logging.FileHandler.limit = 5000000        # 每個檔案上限，單位 bytes (5MB)
java.util.logging.FileHandler.count = 3              # 保留檔案數量
java.util.logging.FileHandler.append = true
java.util.logging.FileHandler.formatter = examples.CustomFormatter
```
執行時指定 config 檔案：
- java -Djava.util.logging.config.file=logging.properties -cp target/classes examples.LogPropertiesDemo

5) logging.properties 讀取程式

```

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.logging.*;

public class LogPropertiesDemo {
    private static final Logger logger = Logger.getLogger(LogPropertiesDemo.class.getName());

    public static void main(String[] args) {
        // 先建立 logs 目錄，避免 FileHandler 在初始化時找不到路徑而拋例外
        Path logDir = Paths.get("logs");
        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            System.err.println("無法建立 log 目錄: " + logDir.toAbsolutePath());
            e.printStackTrace();
            // fallback: 直接用 console logger 並結束
            setupConsoleFallback();
            logger.severe("使用 fallback ConsoleHandler，因為無法建立 log 目錄");
            return;
        }

        // 預設 config 檔案名稱（可用 -Djava.util.logging.config.file 指定其他路徑）
        String configFile = System.getProperty("java.util.logging.config.file", "logging.properties");
        Path configPath = Paths.get(configFile);
        if (!Files.exists(configPath)) {
            System.err.println("找不到 logging config 檔案: " + configPath.toAbsolutePath());
            System.err.println("程式會使用預設 logging 設定（Console）。");
            setupConsoleFallback();
        } else {
            // 以程式方式讀入 logging.properties，確保我們已經建立 logs 目錄後再建立 FileHandler
            try (InputStream is = Files.newInputStream(configPath, StandardOpenOption.READ)) {
                LogManager.getLogManager().readConfiguration(is);
            } catch (IOException | SecurityException ex) {
                System.err.println("讀取 logging config 檔案發生錯誤，改為 Console fallback:");
                ex.printStackTrace();
                setupConsoleFallback();
            }
        }

        // 產生不同等級的 log 測試
        logger.severe("SEVERE - 重大錯誤");
        logger.warning("WARNING - 警告");
        logger.info("INFO - 一般資訊");
        logger.config("CONFIG - 設定相關資訊");
        logger.fine("FINE - 偵錯（較低等級）");
        logger.finer("FINER - 更詳細的偵錯");
        logger.finest("FINEST - 最詳細的偵錯");

        // 示範例外輸出
        try {
            throw new RuntimeException("測試例外");
        } catch (RuntimeException rex) {
            logger.log(Level.SEVERE, "捕捉到例外", rex);
        }

        System.out.println("完成 log 測試。請檢查 logs/ 目錄或 console 輸出。");
    }

    private static void setupConsoleFallback() {
        Logger root = Logger.getLogger("");
        // 清除已有 handler，改為只輸出到 Console
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        ch.setFormatter(new SimpleFormatter());
        root.addHandler(ch);
        root.setLevel(Level.ALL);
    }
}

```

教學步驟（適用於課堂或 workshop）
1. 範例 1（Console）
   - 先跑 JulConsoleExample，讓學生看到預設 Console 輸出與 level 的差異（INFO vs FINE）。
   - 示範移除預設 handler、加入自訂 ConsoleHandler 與改 Formatter。
   - 練習：把 handler level 設為 INFO，讓學生觀察哪些訊息會被過濾掉。

2. 範例 2（File）
   - 跑 JulFileExample，觀察 logs 目錄下產生的檔案與輪替行為（pattern、%u、%g）。
   - 示範修改 limit 與 count，讓檔案快速輪替（用較小的 limit 產生輪替）。
   - 練習：把 append 改成 false，觀察每次啟動是否覆蓋檔案。

3. 範例 3（logging.properties）
   - 用外部設定檔啟動應用程式（-Djava.util.logging.config.file=...）。
   - 示範如何只用 config 檔改變 handler 與 formatter，無需改程式碼重編。
   - 練習：把 ConsoleHandler 改為使用自訂 formatter（指定 fully-qualified class name）。

練習題（含提示）
- 練習 A：把一個使用 System.out.println 的小程式改為使用 Logger，並把 Logger 設為類別名稱（Logger.getLogger(YourClass.class.getName())）。
  提示：使用 logger.info / warning / severe 等方法取代 System.out。

- 練習 B：使用 FileHandler 實作「每日輪替 + 檔案上限」的機制（JUL 原生只支援以 limit+count 或 pattern%g 的輪替；若要 time-based rotation 必須自行實作或外掛）。
  提示：可使用 FileHandler 的 pattern 與 count 做簡單輪替；進階可在應用程式啟動時以日期產生檔名。

- 練習 C：實作自訂 Formatter（像 CustomFormatter），並確保在 formatter 中能輸出例外堆疊。
  提示：覆寫 format(LogRecord) 並使用 record.getThrown()。

- 練習 D（進階）：將 logging.properties 放到資源目錄，示範如何在容器或服務啟動腳本中更換檔案以切換 production / staging 設定。
  提示：在啟動 script 中使用 -Djava.util.logging.config.file 指向不同檔案。

常見問題與注意事項（教學要點）
- Handler 與 Logger 的 Level 是雙重過濾：訊息需通過 logger 的 level 與 handler 的 level 才會輸出。
- FileHandler 在 Windows 上可能因檔案鎖定造成問題；FileHandler 預設會在寫入時鎖檔。生產環境若有多個進程寫同一個 log 檔，需用其他方案（比如集中 log 傳送、或每個 process 一個檔名）。
- FileHandler 的 pattern 中：
  - %u：用於避免檔名衝突的唯一識別數值
  - %g：產生輪替檔案的 generation number（0..count-1）
- FileHandler 的 rotation 是以 byte limit 為基礎（time-based rotation 不是原生支援）。
- 性能：JUL 的 FileHandler 寫入是同步的（會有 I/O blocking）。若對效能有要求，考慮：
  - 使用 MemoryHandler 作 buffer（但風險是訊息在記憶體中可能丟失）
  - 或採用其他 logging 實作（Log4j2、Logback）並使用 AsyncAppender
  - 或把 log 寫到本地 queue，再由獨立 thread/程式批次送到中央系統
- 格式化與敏感資料：不要把敏感 PII、密碼或金鑰寫入 log。建議在 Formatter 或寫入前做 redaction。

教學評量範例（短測 / 作業）
- 小測（10 分）：
  1. 說明 Logger、Handler、Formatter 三者的角色。 (3 分)
  2. FileHandler 的 pattern 中 %u 與 %g 的用途為何？ (2 分)
  3. 若要在不改程式碼情況下改變 logging 設定，應怎麼做？ (2 分)
  4. 列出兩個生產環境的 logging 注意事項。 (3 分)

- 作業（100 分）：
  - 實作一個簡單 CLI 程式，會輸出多種 level 的 log（INFO, FINE, WARNING）。（30）
  - 使用 logging.properties 將 log 同時輸出到 console（human readable）與檔案（自訂 formatter），檔案大小限制為 1MB，保留 2 個輪替檔。 （40）
  - 提交一個 README 說明如何啟動（含 -Djava.util.logging.config.file 的範例），並提供測試證明輪替行為（log 檔名與檔案大小）。（30）

示範輸出（預期）
- Console 範例會顯示類似：
  2025-10-17 10:00:00 INFO [examples.JulConsoleExample] - INFO - 一般資訊
  2025-10-17 10:00:00 FINE [examples.JulConsoleExample] - FINE - 偵錯（較低等級）
- File 範例會在 logs/ 目錄產生檔案 myapp.0.0.log、myapp.0.1.log 等（依 limit 與 count）。

