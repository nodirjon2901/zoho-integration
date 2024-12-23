package uz.result.zohointegration.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PbxResponse {

    private String uuid;

    private String callerIdName;

    private String callerIdNumber;

    private String destinationNumber;

    private String fromHost;

    private String toHost;

    private long startStamp;

    private long endStamp;

    private int duration;

    private int userTalkTime;

    private String hangupCause;

    private String accountCode;

    private String gateway;

    private int qualityScore;

    @JsonProperty("contacted")
    private boolean contacted;

    // Getter va Setter
    public boolean isContacted() {
        return contacted;
    }

    public void setContacted(boolean contacted) {
        this.contacted = contacted;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonProperty("caller_id_name")
    public String getCallerIdName() {
        return callerIdName;
    }

    public void setCallerIdName(String callerIdName) {
        this.callerIdName = callerIdName;
    }

    @JsonProperty("caller_id_number")
    public String getCallerIdNumber() {
        return callerIdNumber;
    }

    public void setCallerIdNumber(String callerIdNumber) {
        this.callerIdNumber = callerIdNumber;
    }

    @JsonProperty("destination_number")
    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }

    @JsonProperty("from_host")
    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    @JsonProperty("to_host")
    public String getToHost() {
        return toHost;
    }

    public void setToHost(String toHost) {
        this.toHost = toHost;
    }

    @JsonProperty("start_stamp")
    public long getStartStamp() {
        return startStamp;
    }

    public void setStartStamp(long startStamp) {
        this.startStamp = startStamp;
    }

    @JsonProperty("end_stamp")
    public long getEndStamp() {
        return endStamp;
    }

    public void setEndStamp(long endStamp) {
        this.endStamp = endStamp;
    }

    @JsonProperty("duration")
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @JsonProperty("user_talk_time")
    public int getUserTalkTime() {
        return userTalkTime;
    }

    public void setUserTalkTime(int userTalkTime) {
        this.userTalkTime = userTalkTime;
    }

    @JsonProperty("hangup_cause")
    public String getHangupCause() {
        return hangupCause;
    }

    public void setHangupCause(String hangupCause) {
        this.hangupCause = hangupCause;
    }

    @JsonProperty("accountcode")
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    @JsonProperty("gateway")
    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @JsonProperty("quality_score")
    public int getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(int qualityScore) {
        this.qualityScore = qualityScore;
    }
}
