package tw.edu.fju.miniclinic.dto;

public class PatientSummaryDTO {
    
    private String chartNo;
    private String name;

    // 必須要有建構子，老師的 new PatientSummaryDTO(...) 才不會報錯
    public PatientSummaryDTO(String chartNo, String name) {
        this.chartNo = chartNo;
        this.name = name;
    }

    // 必須要有 Getter，網頁才抓得到 JSON 資料
    public String getChartNo() {
        return chartNo;
    }

    public String getName() {
        return name;
    }
}