package com.akka.wrapper.job;

import com.akka.wrapper.akka.actor.buncher.AccumulatorFactory;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Created by gargr on 03/03/17.
 */
@Component
public class JobBuilderFactoryHelper {

    @Resource
    private AccumulatorFactory accumulatorFactory;

    @Resource
    private JobRepository jobRepository;

    @Resource(name = "defaultAfterJobExecutionListenerImpl")
    private AfterJobExecutionListener afterJobExecutionListener;

    AccumulatorFactory getAccumulatorFactory() {
        return accumulatorFactory;
    }

    JobRepository getJobRepository() {
        return jobRepository;
    }

    AfterJobExecutionListener getAfterJobExecutionListener() {
        return afterJobExecutionListener;
    }
}
