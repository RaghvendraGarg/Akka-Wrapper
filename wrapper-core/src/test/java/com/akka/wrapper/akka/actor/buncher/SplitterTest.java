package com.akka.wrapper.akka.actor.buncher;

import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.TestImportMessage;
import com.akka.wrapper.akka.actor.BaseActorTest;
import com.akka.wrapper.dto.ImportMessage;
import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobBuilderFactory;
import com.akka.wrapper.job.JobBuilderFactoryHelper;
import com.akka.wrapper.job.JobExecutionContext;
import com.akka.wrapper.job.JobRepository;
import com.akka.wrapper.job.listener.DefaultAfterJobExecutionListenerImpl;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;

import akka.testkit.TestProbe;

/**
 * Created by gargr on 06/03/17.
 */
public class SplitterTest extends BaseActorTest {

    public SplitterTest() {
        super(Splitter.class);
    }

    private TestProbe testActor1 = TestProbe.apply(_system);

    private TestProbe testActor2 = TestProbe.apply(_system);

    private TestProbe testActor3 = TestProbe.apply(_system);

    private JobBuilderFactoryHelper factoryHelper = new JobBuilderFactoryHelper();

    private DefaultAfterJobExecutionListenerImpl defaultAfterJobExecutionListener = new DefaultAfterJobExecutionListenerImpl();

    private Job buildJob;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Function<ImportMessage, String> function = new Function<ImportMessage, String>() {
            @Override
            public String apply(ImportMessage message) {
                return message.getMessageId();
            }
        };
        JobBuilderFactory targetObject = new JobBuilderFactory();
        JobRepository jobRepository = Mockito.spy(JobRepository.class);
        AccumulatorFactory accumulatorFactory = Mockito.mock(AccumulatorFactory.class);
        when(accumulatorFactory.getSplittingActorRef()).thenReturn(testActor3.ref());
        ReflectionTestUtils.setField(factoryHelper, "accumulatorFactory", accumulatorFactory);
        ReflectionTestUtils.setField(factoryHelper, "jobRepository", jobRepository);
        ReflectionTestUtils.setField(factoryHelper, "afterJobExecutionListener", defaultAfterJobExecutionListener);
        targetObject.setJobBuilderFactoryHelper(factoryHelper);
        buildJob = buildABuncherJob();
        when(jobExecutionContext.getJob()).thenReturn(buildJob);
    }

    @Test
    public void onReceiveWhenPreviousStepWasNotBuncher() {
        ImportMessage importMessage = new ImportMessage("22");
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getFirstStep().getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        testActor3.expectMsgClass(StepExecutionContext.class);
    }

    @Test
    public void onReceiveWhenPreviousStepWasBuncher() {
        MultiValueMap<String, PlatformMessage> map = new LinkedMultiValueMap<>();
        ImportMessage importMessage = new ImportMessage("22");
        map.add(importMessage.getMessageId(), importMessage);
        importMessage = new ImportMessage("22");
        map.add(importMessage.getMessageId(), importMessage);
        importMessage = new ImportMessage("22");
        map.add(importMessage.getMessageId(), importMessage);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getFirstStep().getStepContext(), jobExecutionContext, map);
        testActorRef.tell(stepExecutionContext, null);
        testActor3.receiveN(3);
    }

    @Test
    public void onReceiveWhenException() {
        testActorRef.tell("test", null);
        testActor3.expectNoMessage();
    }

    private Job buildABuncherJob() {
        Function<TestImportMessage, String> function = new Function<TestImportMessage, String>() {
            @Override
            public String apply(TestImportMessage testImportMessage) {
                return testImportMessage.getMessageId();
            }
        };
        Job job = JobBuilderFactory.get("buncherJob").start(testActor1.ref()).
                next(testActor3.ref()).
                accumulate(5, 1000, function, "testBunchr", 1).
                next(testActor2.ref()).
                split().next(testActor1.ref()).build();
        return job;
    }

}