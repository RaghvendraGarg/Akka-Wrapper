package com.akka.wrapper.job;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gargr on 27/02/17.
 */
@Component
public class JobRepository {

    private final Map<String, Job> JOB_MAP = new HashMap<>();

    public Job getJob(String jobName) {
        return JOB_MAP.get(jobName);
    }

    void addJob(Job job) {
        if (JOB_MAP.containsKey(job.getName())) {
            throw new IllegalArgumentException(job.getName() + " job name must be unique");
        }
        JOB_MAP.put(job.getName(), job);
    }

}
