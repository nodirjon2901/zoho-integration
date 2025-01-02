package uz.result.zohointegration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uz.result.zohointegration.model.dto.RingStarResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class RingStarService {

    public List<RingStarResponse> getCalls() {
        List<RingStarResponse> callList = null;
        try {
            // HttpClient yaratish
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            // Query parametrlarini qo'shgan URL
            String url = "https://mrjt.ringstar.io/crmapi/v1/history/json" +
                    "?type=all" +
                    "&limit=100" +
                    "&start=20240801T000000Z";

            // HttpRequest sozlash (GET metodi bilan)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-API-KEY", "342b5dbe-ee3c-4453-9b7d-b6a6a31c4117")
                    .GET()
                    .build();

            // Javobni olish
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Code: " + response.statusCode());

            if (response.statusCode() == 200) {
                // Javobni JSON dan Java obyektiga o‘tkazish
                ObjectMapper objectMapper = new ObjectMapper();
                callList = objectMapper.readValue(response.body(), new TypeReference<List<RingStarResponse>>() {});
            } else {
                System.out.println("Failed to fetch calls. Status code: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return callList;
    }

}


