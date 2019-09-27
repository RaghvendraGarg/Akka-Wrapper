package com.akka.wrapper.cache.rabbitmq;

import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueueNameUtil {

    private static final Logger logger = LoggerFactory.getLogger(QueueNameUtil.class);

    @Value("${info.app.name:imports-cache}")
    private String applicationName;

    private static String hostName;

    public String getQueueName() {
        return applicationName + "." + getHostname();
    }

    private static String getHostname() {
        try {
            if (StringUtils.isBlank(hostName) || StringUtils.equalsIgnoreCase(hostName, "defaultHost")) {
                hostName = InetAddress.getLocalHost().getHostName();
            }
        } catch (Exception var1) {
            logger.error("Unable to get HostName.");
            hostName = "defaultHost";
        }
        return hostName;
    }

}
