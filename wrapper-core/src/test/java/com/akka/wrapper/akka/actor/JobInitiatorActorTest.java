package com.akka.wrapper.akka.actor;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import com.akka.wrapper.TestImportMessage;
import com.akka.wrapper.akka.actor.buncher.AccumulatorFactory;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobBuilderFactory;
import com.akka.wrapper.job.JobBuilderFactoryHelper;
import com.akka.wrapper.job.JobExecutionContext;
import com.akka.wrapper.job.JobExecutionContextFactory;
import com.akka.wrapper.job.JobRepository;
import com.akka.wrapper.job.step.StepExecutionContext;

import akka.testkit.TestProbe;

/**
 * Created by gargr on 27/02/17.
 */
public class JobInitiatorActorTest extends BaseActorTest {

    public JobInitiatorActorTest() {
        super(JobInitiatorActor.class);
    }

    private JobBuilderFactoryHelper factoryHelper = new JobBuilderFactoryHelper();

    private TestProbe testActor1 = TestProbe.apply(_system);

    private TestProbe testActor2 = TestProbe.apply(_system);

    @Mock
    private JobExecutionContextFactory jobExecutionContextFactory;

    @Mock
    private JobExecutionContext jobExecutionContext;

    private TestImportMessage testImportMessage;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        JobRepository jobRepository = Mockito.spy(JobRepository.class);
        AccumulatorFactory accumulatorFactory = Mockito.mock(AccumulatorFactory.class);
        JobBuilderFactory targetObject = new JobBuilderFactory();
        ReflectionTestUtils.setField(factoryHelper, "accumulatorFactory", accumulatorFactory);
        ReflectionTestUtils.setField(factoryHelper, "jobRepository", jobRepository);
        targetObject.setJobBuilderFactoryHelper(factoryHelper);
        StopWatch stopWatch = new StopWatch("test");
        stopWatch.start();
        when(jobExecutionContext.getStopWatch()).thenReturn(stopWatch);
    }

    @Test
    public void onReceive() {
        testImportMessage = createImportMessage();
        when(jobExecutionContextFactory.create("build", createImportMessage())).thenReturn(jobExecutionContext);
        Job build = buildJob("build");
        when(jobExecutionContext.getJob()).thenReturn(build);
        when(jobExecutionContext.getMessage()).thenReturn(testImportMessage);
        testActorRef.tell(jobExecutionContext, getTestActorRef());
        testActor1.expectMsgClass(StepExecutionContext.class);
    }

    @Test
    public void onReceiveWhenMessageIsNotOfJobExecutionContext() {
        testActorRef.tell("hello", getTestActorRef());
        verifyZeroInteractions(jobExecutionContext);
        testActor1.expectNoMessage();
    }

    private Job buildJob(String jobName) {
        Job job = JobBuilderFactory.
                get(jobName).
                start(testActor1.ref()).
                next(testActor2.ref()).
                build();
        return job;
    }

    private TestImportMessage createImportMessage() {
        TestImportMessage testImportMessage = new TestImportMessage("test");
        testImportMessage.setaNumber(10);
        return testImportMessage;
    }

}