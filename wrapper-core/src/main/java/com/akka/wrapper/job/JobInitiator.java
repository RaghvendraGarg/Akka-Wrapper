package com.akka.wrapper.job;

import com.akka.wrapper.dto.PlatformMessage;

/**
 * Created by gargr on 11/04/17.
 */
public interface JobInitiator {

    void initiate(String jobName, PlatformMessage message);

}
