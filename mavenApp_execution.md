# maven-jar-plugin（打包成可執行 JAR）
## maven-jar-plugin 是 Maven 的一個插件，用於將 Java 項目打包成 JAR 文件。通過配置 maven-jar-plugin，可以指定主類（Main-Class）和依賴的 classpath，使得生成的 JAR 文件可以直接執行。
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-jar-plugin</artifactId>
      <version>3.3.0</version>
      <configuration>
        <archive>
          <manifest>
            <!-- 設定 MANIFEST.MF 的 Main-Class -->
            <mainClass>com.example.EmployeeApp</mainClass>
            <!-- 將依賴 classpath 也寫入 MANIFEST -->
            <addClasspath>true</addClasspath>
            <classpathPrefix>libs/</classpathPrefix>
          </manifest>
        </archive>
      </configuration>
    </plugin>
  </plugins>
</build>
```
#### 在上述配置中，我們指定了主類為 `com.example.EmployeeApp`，並且將依賴的 JAR 文件放在 `libs/` 目錄下。這樣生成的 JAR 文件就可以直接執行，並且會自動包含所需的依賴。

### 要生成可執行的 JAR 文件，可以使用以下命令：

#### 這將會在 target/ 目錄下生成一個包含所有依賴的可執行 JAR 文件。你可以使用以下命令來運行這個 JAR 文件：
```
java -jar target/your-app.jar
```
#### 請將 `your-app.jar` 替換為實際生成的 JAR 文件名稱。這樣就可以直接運行你的 Java 應用程序了。