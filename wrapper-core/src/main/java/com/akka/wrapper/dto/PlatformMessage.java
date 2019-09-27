package com.akka.wrapper.dto;

import java.util.UUID;

public abstract class PlatformMessage {

    private String messageId;

    private ImportProcessStatus importProcessStatus = ImportProcessStatus.IN_PROGRESS;

    public PlatformMessage() {
        messageId = UUID.randomUUID().toString();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public ImportProcessStatus getImportProcessStatus() {
        return importProcessStatus;
    }

    public void setImportProcessStatus(ImportProcessStatus importProcessStatus) {
        this.importProcessStatus = importProcessStatus;
    }
}
