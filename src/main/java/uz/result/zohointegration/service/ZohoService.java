package uz.result.zohointegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.result.zohointegration.model.dto.PbxResponse;
import uz.result.zohointegration.model.dto.ZohoLead;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ZohoService {

    @Value("${zoho.owner.id}")
    private String ownerId;

    @Value("${zoho.client.id}")
    private String clientId;

    @Value("${zoho.client.secret}")
    private String clientSecret;

    @Value("${zoho.refresh.token}")
    private String refreshToken;

    @Value("${online.pbx.api.key}")
    private String authKey;

    @Autowired
    private PBXService pbxService;

    private final RestTemplate restTemplate = new RestTemplate();

    public void createLeadsFromPBXResponses() {
        String authenticationKey = pbxService.getAuthenticationKey(authKey);
        System.out.println(authenticationKey);
        List<PbxResponse> pbxResponses = pbxService.getCalls(authenticationKey);
        String url = "https://www.zohoapis.com/crm/v2/Leads";
        String token = refreshAccessToken();
        System.out.println(token);

        // API uchun umumiy sozlamalar
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();

        for (PbxResponse pbxResponse : pbxResponses) {
            try {
                ZohoLead lead = new ZohoLead();

                lead.setLastName(pbxResponse.getCallerIdName() != null ? pbxResponse.getCallerIdName() : "Unknown Caller");
                lead.setFirstName("PBX Contact"); // First Name standart qiymat
                lead.setPhone(pbxResponse.getDestinationNumber());
                lead.setMobile(pbxResponse.getGateway());
                lead.setEmail("none@example.com"); // Emailni dummy tarzda o'zgartirish mumkin
                lead.setDescription(showDescription(pbxResponse,authenticationKey));
                lead.setLeadSource("PBX System");
                lead.setCity("Unknown");
                lead.setCountry("Unknown");
                lead.setIndustry("Telecom");
                lead.setLeadStatus("Attempted to Contact");

                ZohoLead.Owner owner = new ZohoLead.Owner();
                owner.setId(ownerId); // Zoho'dagi default owner ID
                lead.setOwner(owner);

                Map<String, Object> leadData = Map.of("data", List.of(lead));
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(leadData, headers);
                restTemplate.postForEntity(url, entity, String.class);

            } catch (Exception e) {
                System.err.println("Error creating lead: " + e.getMessage());
            }
        }
    }

//    public void createLeadsFromPBXResponses(String startStampFrom, String startStampTo) {
//        String authenticationKey = pbxService.getAuthenticationKey(authKey);
//        System.out.println(authenticationKey);
//        List<PbxResponse> pbxResponses = pbxService.getCalls(startStampFrom, startStampTo, authenticationKey);
//        String url = "https://www.zohoapis.com/crm/v2/Leads";
//        String token = refreshAccessToken();
//        System.out.println(token);
//
//        // API uchun umumiy sozlamalar
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//        headers.set("Content-Type", "application/json");
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        for (PbxResponse pbxResponse : pbxResponses) {
//            try {
//                ZohoLead lead = new ZohoLead();
//
//                lead.setLastName(pbxResponse.getCallerIdName() != null ? pbxResponse.getCallerIdName() : "Unknown Caller");
//                lead.setFirstName("PBX Contact"); // First Name standart qiymat
//                lead.setPhone(pbxResponse.getDestinationNumber());
//                lead.setMobile(pbxResponse.getGateway());
//                lead.setEmail("none@example.com"); // Emailni dummy tarzda o'zgartirish mumkin
//                lead.setDescription(showDescription(pbxResponse, authenticationKey));
//                lead.setLeadSource("PBX System");
//                lead.setCity("Unknown");
//                lead.setCountry("Unknown");
//                lead.setIndustry("Telecom");
//                lead.setLeadStatus("Attempted to Contact");
//
//                ZohoLead.Owner owner = new ZohoLead.Owner();
//                owner.setId(ownerId); // Zoho'dagi default owner ID
//                lead.setOwner(owner);
//
//                Map<String, Object> leadData = Map.of("data", List.of(lead));
//                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(leadData, headers);
//                restTemplate.postForEntity(url, entity, String.class);
//
//            } catch (Exception e) {
//                System.err.println("Error creating lead: " + e.getMessage());
//            }
//        }
//    }

    private String showDescription(PbxResponse pbxResponse, String authenticationKey) {
        return "Call from PBX system. UUID:  " + pbxResponse.getUuid() + "\n" +
                "Call from host:  " + pbxResponse.getFromHost() + "\n" +
                "Call to host:  " + pbxResponse.getToHost() + "\n" +
                "Call start stamp:  " + pbxResponse.getStartStamp() + "\n" +
                "Call end stamp:  " + pbxResponse.getEndStamp() + "\n" +
                "Call duration:  " + pbxResponse.getDuration() + "\n" +
                "Call user talk time:  " + pbxResponse.getUserTalkTime() + "\n" +
                "Call hang up cause:  " + pbxResponse.getHangupCause() + "\n" +
                "Call account code:  " + pbxResponse.getAccountCode() + "\n" +
                "Call contacted:  " + pbxResponse.isContacted() + "\n" +
                "Call quality score:  " + pbxResponse.getQualityScore() + "\n" +
                "Call audio link for download:  " + pbxService.getCallAudioFileUrl(pbxResponse.getUuid(), authenticationKey) + "\n";
    }

    public String refreshAccessToken() {
        String REFRESH_TOKEN_URL = "https://accounts.zoho.com/oauth/v2/token";
        String requestBody = String.format(
                "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                clientId, clientSecret, refreshToken
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    REFRESH_TOKEN_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            } else {
                throw new RuntimeException("Failed to refresh access token. Response: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while refreshing access token: " + e.getMessage(), e);
        }
    }

//        @Scheduled(cron = "01 59 23 * * *", zone = "Asia/Tashkent")
////    @Scheduled(cron = "0 * * * * *", zone = "Asia/Tashkent")
//    public void autoRun() {
//        // Hozirgi vaqtni olish (GMT+5 - Tashkent zonasi)
//        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Tashkent"));
//        String startStampTo = String.valueOf(now.toEpochSecond());
//        String startStampFrom = String.valueOf(now.minusDays(1).toEpochSecond());
//        createLeadsFromPBXResponses(startStampFrom, startStampTo);
//    }

}

