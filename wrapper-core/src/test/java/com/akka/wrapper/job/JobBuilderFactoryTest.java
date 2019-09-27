package com.akka.wrapper.job;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.akka.wrapper.akka.actor.buncher.AccumulatorFactory;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import com.akka.wrapper.job.listener.DefaultAfterJobExecutionListenerImpl;
import com.akka.wrapper.job.step.Step;

import akka.actor.ActorRef;

public class JobBuilderFactoryTest {

    @InjectMocks
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private ActorRef actorRef;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private AccumulatorFactory accumulatorFactory;

    @Mock
    private JobBuilderFactoryHelper jobBuilderFactoryHelper;

    private DefaultAfterJobExecutionListenerImpl defaultAfterJobExecutionListener = new DefaultAfterJobExecutionListenerImpl();

    @Mock
    private AfterJobExecutionListener afterJobExecutionListener;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        jobBuilderFactory.setJobBuilderFactoryHelper(jobBuilderFactoryHelper);
        Mockito.when(jobBuilderFactoryHelper.getJobRepository()).thenReturn(jobRepository);
        Mockito.when(jobBuilderFactoryHelper.getAccumulatorFactory()).thenReturn(accumulatorFactory);
        Mockito.when(jobBuilderFactoryHelper.getAfterJobExecutionListener()).thenReturn(defaultAfterJobExecutionListener);
    }

    @Test
    public void testNextStepWithJob(){

        Job testJob = JobBuilderFactory.get("testJob").start(actorRef).
                        next(actorRef).next(actorRef).
                        afterJob(afterJobExecutionListener).
                        build();

        System.out.println(testJob.getSteps().size());

        Predicate predicate = new Predicate() {
            @Override
            public boolean test(Object o) {
                return false;
            }
        } ;

        Job job = JobBuilderFactory.get("job").start(actorRef).
                next(actorRef).
                accumulate(5, 5, "buncher", 5).
                next(actorRef).
                fork(predicate, testJob).
                split().
                next(actorRef).
                build();

        System.out.println(job.getSteps().size());

        Job sampleJob = JobBuilderFactory.get("sampleJob").start(actorRef).
                next(actorRef).
                accumulate(5, 5, "buncher", 5).
                next(actorRef).
                fork(predicate, testJob).
                split().
                next(actorRef).
                next(testJob).
                build();


        Assert.assertEquals(9, sampleJob.getSteps().size());

        for (int i = 0; i < testJob.getSteps().size(); i++) {
            Step step1 = testJob.getSteps().get(i);
            Step step2 = sampleJob.getSteps().get(i + 6);
            Assert.assertEquals(step1, step2);
        }

        System.out.println(sampleJob);
    }

}