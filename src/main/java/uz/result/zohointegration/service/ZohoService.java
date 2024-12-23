package uz.result.zohointegration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uz.result.zohointegration.model.dto.PbxResponse;
import uz.result.zohointegration.model.dto.ZohoLead;

import java.io.File;
import java.io.FileOutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
                // Create a ZohoLead object
                ZohoLead lead = new ZohoLead();
                lead.setLastName(pbxResponse.getCallerIdName() != null ? pbxResponse.getCallerIdName() : "Unknown Caller");
                lead.setFirstName("PBX Contact"); // Default First Name
                lead.setPhone(pbxResponse.getDestinationNumber());
                lead.setMobile(pbxResponse.getGateway());
                lead.setEmail("none@example.com"); // Dummy Email
                lead.setDescription(showDescription(pbxResponse, authenticationKey));
                lead.setLeadSource("PBX System");
                lead.setCity("Unknown");
                lead.setCountry("Unknown");
                lead.setIndustry("Telecom");
                lead.setLeadStatus("Attempted to Contact");

                ZohoLead.Owner owner = new ZohoLead.Owner();
                owner.setId(ownerId); // Zoho Default Owner ID
                lead.setOwner(owner);

                // Create lead in Zoho CRM
                Map<String, Object> leadData = Map.of("data", List.of(lead));
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(leadData, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                // Extract the lead ID from the response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                String leadId = responseNode.path("data").get(0).path("details").path("id").asText();

                System.out.println("Lead created successfully with ID: " + leadId);

                // Upload the audio file associated with this lead
                uploadAudioFile(leadId, pbxResponse.getUuid(), authenticationKey, token);

            } catch (Exception e) {
                System.err.println("Error creating lead or uploading audio: " + e.getMessage());
            }
        }
    }

    public void uploadAudioFile(String leadId, String uuid, String authenticationKey, String token) {
        String audioUrl = pbxService.getCallAudioFileUrl(uuid, authenticationKey); // Get audio file URL
        System.out.println(audioUrl);
        // Download the audio file from PBX (temporary storage on server or memory)
        File audioFile = downloadAudioFile(audioUrl);

        if (audioFile != null) {
            String uploadUrl = "https://www.zohoapis.com/crm/v2/Leads/" + leadId + "/Attachments";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Zoho-oauthtoken " + token);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audioFile));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, entity, String.class);
                System.out.println("Audio file uploaded successfully for Lead ID: " + leadId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // Clean up temporary audio file
                audioFile.delete();
            }
        }
    }

    public File downloadAudioFile(String fileUrl) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(fileUrl, byte[].class);

            // Javobning holatini va content-lengthni tekshirish
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Fayl yo'lini belgilash
                File directory = new File("src/main/resources/audio");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Faylni yozish
                String fileName = "audio-" + UUID.randomUUID() + ".mp3";
                File audioFile = new File(directory, fileName);

                // Faylni yozish
                try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                    fos.write(response.getBody());
                }

                System.out.println("Fayl muvaffaqiyatli yuklandi: " + fileName);
                return audioFile;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

//    public void createLeadsFromPBXResponses() {
//        String authenticationKey = pbxService.getAuthenticationKey(authKey);
//        System.out.println(authenticationKey);
//        List<PbxResponse> pbxResponses = pbxService.getCalls(authenticationKey);
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
//                lead.setDescription(showDescription(pbxResponse,authenticationKey));
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

