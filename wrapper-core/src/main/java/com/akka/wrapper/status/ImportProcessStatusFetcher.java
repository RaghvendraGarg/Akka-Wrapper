package com.akka.wrapper.status;

import com.akka.wrapper.dto.ImportProcessStatus;
import com.akka.wrapper.exception.ExceptionToImportProcessStatusMapper;

public class ImportProcessStatusFetcher {

    public static ImportProcessStatus getImportProcessStatus(final Class<?> exceptionClass) {
        ImportProcessStatus processStatus = ExceptionToImportProcessStatusMapper.getMappedImportProcessStatus(exceptionClass);
        if (processStatus == null) {
            processStatus = ImportProcessStatus.FAILURE_NON_RECOVERABLE;
        }
        return processStatus;
    }
}
