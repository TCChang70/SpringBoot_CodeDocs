package demo.example.controller;

import demo.example.service.UtilService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/util")
public class UtilController {
    
    private final UtilService utilService;
    private final String appInfo;
    
    public UtilController(UtilService utilService, 
                         @Qualifier("appInfo") String appInfo) {
        this.utilService = utilService;
        this.appInfo = appInfo;
    }
    
    @GetMapping("/time")
    public String getTime() {
        return utilService.getCurrentTime();
    }
    
    @GetMapping("/uuid")
    public String getUuid() {
        return utilService.generateUuid();
    }
    
    @GetMapping("/info")
    public String getAppInfo() {
        return appInfo;
    }
}
