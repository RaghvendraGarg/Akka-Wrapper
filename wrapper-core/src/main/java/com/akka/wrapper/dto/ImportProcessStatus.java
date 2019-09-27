package com.akka.wrapper.dto;

public enum ImportProcessStatus {

    IN_PROGRESS ("In-progress"),
    SUCCESS ("Succeeded"),
    FAILURE_RECOVERABLE ("Failed. Retrying"),
    FAILURE_NON_RECOVERABLE ("Failed"),
    FAILURE_RECOVERABLE_WITH_DELAY ("Failed. Retrying after some time"),
    FAILED_DUE_TO_OPEN_CIRUICTBREAKER ("Failed due to open ciruictbreaker"),
    RE_PROCESSED ("Reprocessing failed message successful");

    private String status;

    ImportProcessStatus(String status) {
        this.status = status;
    }

}
