package uz.result.zohointegration.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import uz.result.zohointegration.service.ZohoService;

@RestController
@RequestMapping("/api/integrate")
public class IntegrationController {

    @Autowired
    private ZohoService zohoService;

    @PostMapping("/sync")
    public String syncPBXWithZoho() {
        zohoService.createLeadsFromPBXResponses();
        return "OK";
    }

//    @GetMapping("/sync")
//    public String syncPBXWithZoho(@RequestParam String startStampFrom, @RequestParam String startStampTo) {
//        zohoService.createLeadsFromPBXResponses(startStampFrom, startStampTo);
//        return "OK";
//    }
}

