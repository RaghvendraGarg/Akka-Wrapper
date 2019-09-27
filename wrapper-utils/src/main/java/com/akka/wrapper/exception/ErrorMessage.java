package com.akka.wrapper.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Error message to return REST client
 * @author changedm
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName(value = "error")
public class ErrorMessage {

    private String message;

    private String uuid;

    private int httpCode;

    /**
     * Get error message
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set error message
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get generated UUID
     * @return uuid
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Set generated UUID
     * @param uuid
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get error HTTP code
     * @return
     */
    @JsonProperty(value = "HTTPCode")
    public int getHTTPCode() {
        return httpCode;
    }

    /**
     * Set error HTTP code
     * @param httpCode
     */
    public void setHTTPCode(int httpCode) {
        this.httpCode = httpCode;
    }

}
