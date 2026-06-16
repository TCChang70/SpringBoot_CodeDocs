package tw.edu.fju.miniclinic.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

// autoApply = true 代表系統會自動幫所有的 LocalDate 套用這個轉換器
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    // 1. Java 存入資料庫時的轉換：LocalDate -> String
    @Override
    public String convertToDatabaseColumn(LocalDate locDate) {
        if (locDate == null) {
            return null;
        }
        return locDate.toString(); // 轉換成 "YYYY-MM-DD" 格式的字串
    }

    // 2. 從資料庫讀出回 Java 時的轉換：String -> LocalDate
    @Override
    public LocalDate convertToEntityAttribute(String sqlDate) {
        if (sqlDate == null || sqlDate.isEmpty()) {
            return null;
        }
        return LocalDate.parse(sqlDate); // 將字串解析回 LocalDate 物件
    }
}