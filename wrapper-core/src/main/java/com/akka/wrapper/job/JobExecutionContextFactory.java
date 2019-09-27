package com.akka.wrapper.job;

import com.akka.wrapper.dto.PlatformMessage;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Created by gargr on 27/02/17.
 */
@Component
public class JobExecutionContextFactory {

    @Resource
    private JobRepository jobRepository;

    public JobExecutionContext create(String jobName, PlatformMessage message, StopWatch stopWatch) {
        Job job = jobRepository.getJob(jobName);
        if (job == null) {
            throw new IllegalArgumentException("No Job with " + jobName + " exists");
        }
        JobExecutionContext context = new JobExecutionContext(job, message, stopWatch);
        return context;
    }

    public JobExecutionContext create(String jobName, PlatformMessage message) {
        Job job = jobRepository.getJob(jobName);
        if (job == null) {
            throw new IllegalArgumentException("No Job with " + jobName + " exists");
        }
        JobExecutionContext context = new JobExecutionContext(job, message, new StopWatch(jobName));
        return context;
    }

}
