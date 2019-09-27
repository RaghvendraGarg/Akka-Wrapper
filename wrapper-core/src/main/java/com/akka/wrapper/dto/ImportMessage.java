package com.akka.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by gargr on 13/02/17.
 */
public class ImportMessage extends PlatformMessage {

    private String source;

    private String batchId;

    @JsonIgnore
    private Acknowledgement acknowledgement;

    private String loggingDetails;

    public ImportMessage() {}

    public ImportMessage(String source) {
        this.source = source;
    }

    public String getLoggingDetails() {
        return loggingDetails;
    }

    public void setLoggingDetails(String loggingDetails) {
        this.loggingDetails = loggingDetails;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Acknowledgement getAcknowledgement() {
        return acknowledgement;
    }

    public void setAcknowledgement(Acknowledgement acknowledgement) {
        this.acknowledgement = acknowledgement;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

}
