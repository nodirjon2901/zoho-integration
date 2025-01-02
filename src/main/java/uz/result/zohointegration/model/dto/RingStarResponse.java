package uz.result.zohointegration.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RingStarResponse {

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("status")
    private String status;

    @JsonProperty("client")
    private String client;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("user")
    private String user;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("diversion")
    private String diversion;

    @JsonProperty("start")
    private String start;

    @JsonProperty("wait")
    private int wait;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("record")
    private String record;

}
