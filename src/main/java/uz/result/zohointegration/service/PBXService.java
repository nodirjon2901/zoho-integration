package uz.result.zohointegration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import uz.result.zohointegration.model.dto.PbxResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class PBXService {

    private final RestTemplate restTemplate = new RestTemplate();

//    public List<PbxResponse> getCalls(String authenticationKey) {
//        String url = "https://api2.onlinepbx.ru/pbx28186.onpbx.ru/mongo_history/search.json";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("x-pbx-authentication", authenticationKey);
//        headers.set("Content-Type", "application/x-www-form-urlencoded");
//
//        // Parametrlarsiz request body
//        HttpEntity<String> entity = new HttpEntity<>("", headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode rootNode = objectMapper.readTree(response.getBody());
//            JsonNode dataNode = rootNode.path("data");
//
//            List<PbxResponse> pbxResponses = new ArrayList<>();
//            if (dataNode.isArray()) {
//                for (JsonNode dataItem : dataNode) {
//                    PbxResponse pbxResponse = objectMapper.treeToValue(dataItem, PbxResponse.class);
//                    pbxResponses.add(pbxResponse);
//                }
//            }
//            return pbxResponses;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error while parsing PBX response: " + e.getMessage(), e);
//        }
//    }

    public String getCallAudioFileUrl(String uuid, String authenticationKey) {
        String url = "https://api2.onlinepbx.ru/pbx28186.onpbx.ru/mongo_history/search.json";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-pbx-authentication", authenticationKey);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String requestBody = "uuid=" + uuid + "&download=download";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (rootNode.has("data")) {
                return rootNode.get("data").asText(); // Audio faylni yuklab olish uchun URL qaytariladi
            }

            throw new RuntimeException("Audio file URL not found for UUID: " + uuid);

        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving call audio file URL: " + e.getMessage(), e);
        }
    }

    public String getAuthenticationKey(String authKey) {
        String url = "https://api2.onlinepbx.ru/pbx28186.onpbx.ru/auth.json";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        headers.set("Accept", "application/json");

        // Request Body
        String requestBody = "auth_key=" + authKey + "&new=true";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode dataNode = rootNode.path("data");

                if (dataNode.has("key") && dataNode.has("key_id")) {
                    String key = dataNode.get("key").asText();
                    String keyId = dataNode.get("key_id").asText();
                    return keyId + ":" + key;
                } else {
                    throw new RuntimeException("Invalid response structure: key or key_id not found.");
                }
            } else {
                throw new RuntimeException("Failed to authenticate: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving authentication key: " + e.getMessage(), e);
        }
    }

        public List<PbxResponse> getCalls(String authenticationKey) {
        String startStampFrom="1733011200";
        String startStampTo="1733615999";
        String url = "https://api2.onlinepbx.ru/pbx28186.onpbx.ru/mongo_history/search.json";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-pbx-authentication", authenticationKey);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String requestBody = "start_stamp_from=" + startStampFrom + "&start_stamp_to=" + startStampTo;

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataNode = rootNode.path("data");

            List<PbxResponse> pbxResponses = new ArrayList<>();
            if (dataNode.isArray()) {
                for (JsonNode dataItem : dataNode) {
                    PbxResponse pbxResponse = objectMapper.treeToValue(dataItem, PbxResponse.class);
                    pbxResponses.add(pbxResponse);
                }
            }
            return pbxResponses;
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing PBX response: " + e.getMessage(), e);
        }
    }

}
