package uz.result.zohointegration.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import uz.result.zohointegration.service.ZohoService;

import java.util.Map;

@RestController
@RequestMapping("/api/integrate")
public class IntegrationController {

    @Autowired
    private ZohoService zohoService;

    @PostMapping(value = "/sync", consumes = "application/x-www-form-urlencoded")
    public String syncPBXWithZoho(@RequestParam Map<String, String> params) {
        System.out.println("Webhook received with params: " + params);
        zohoService.createLeadsFromPBXResponses();
        return "OK";
    }

}

