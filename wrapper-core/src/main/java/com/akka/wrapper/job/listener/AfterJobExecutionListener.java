package com.akka.wrapper.job.listener;

import com.akka.wrapper.akka.actor.JobInitiatorActor;
import com.akka.wrapper.dto.PlatformMessage;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.MultiValueMap;

/**
 * Listener to perform any after job operations in case of failure or success.
 * User can override onFailure and onSuccess methods and call a chain of events by creating a {@link com.akka.wrapper.job.Job}
 * which can be invoked using {@link JobInitiatorActor}
 */
public interface AfterJobExecutionListener {

    void onFailure(AfterJobExecutionListenerContext afterJobExecutionListenerContext);

    void onSuccess(AfterJobExecutionListenerContext afterJobExecutionListenerContext);

    default String getMessageIds(Object object) {
        if (object != null) {
            if (object instanceof PlatformMessage) {
                return ((PlatformMessage) object).getMessageId();
            } else if (object instanceof MultiValueMap) {
                MultiValueMap<String, PlatformMessage> messages = (MultiValueMap) object;
                List<String> messageIds = messages.entrySet().stream().
                        map(v -> v.getValue()).
                        flatMap(v -> v.stream()).
                        map(v -> v.getMessageId()).
                        collect(Collectors.toList());
                if (messageIds != null && !messageIds.isEmpty()) {
                    return StringUtils.join(messageIds, ",");
                }
            }

        }
        return null;
    }

}
