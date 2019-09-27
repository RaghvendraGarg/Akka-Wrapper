package com.akka.wrapper.akka.actor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;

import com.akka.wrapper.TestImportMessage;
import com.akka.wrapper.akka.actor.buncher.AccumulatorFactory;
import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobBuilderFactory;
import com.akka.wrapper.job.JobBuilderFactoryHelper;
import com.akka.wrapper.job.JobExecutionContext;
import com.akka.wrapper.job.JobExecutionContextFactory;
import com.akka.wrapper.job.JobRepository;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import com.akka.wrapper.job.listener.AfterJobExecutionListenerContext;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;
import com.akka.wrapper.service.Service;

import akka.actor.ActorSystem;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;

/**
 * Created by gargr on 13/02/17.
 */
public class ImportActorTest extends TestKit {

    public static ActorSystem _system = ActorSystem.create("TestSys");

    private TestActorRef<ImportActor> testActorRef;

    @Mock
    private Service service;

    @Mock
    private JobExecutionContextFactory jobExecutionContextFactory;

    private TestProbe jobInitiatorActorRef = TestProbe.apply(_system);

    private TestProbe testActor1 = TestProbe.apply(_system);

    private TestProbe testActor2 = TestProbe.apply(_system);

    private TestProbe testActor3 = TestProbe.apply(_system);

    private Job buildJob;

    private Job buildAForkedJob;

    private Job buncherJob;

    @Mock
    private ImportActorHelper importActorHelper;

    private JobBuilderFactoryHelper factoryHelper = new JobBuilderFactoryHelper();

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private MockListener listener;

    public ImportActorTest() {
        super(_system);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(importActorHelper.getJobInitiatorActorRef()).thenReturn(jobInitiatorActorRef.ref());
        when(importActorHelper.getJobExecutionContextFactory()).thenReturn(jobExecutionContextFactory);
        JobRepository jobRepository = Mockito.spy(JobRepository.class);
        AccumulatorFactory accumulatorFactory = Mockito.mock(AccumulatorFactory.class);
        when(accumulatorFactory.getSplittingActorRef()).thenReturn(testActor3.ref());
        JobBuilderFactory targetObject = new JobBuilderFactory();
        ReflectionTestUtils.setField(factoryHelper, "accumulatorFactory", accumulatorFactory);
        ReflectionTestUtils.setField(factoryHelper, "jobRepository", jobRepository);
        ReflectionTestUtils.setField(factoryHelper, "afterJobExecutionListener", listener);
        targetObject.setJobBuilderFactoryHelper(factoryHelper);
        testActorRef = TestActorRef.create(_system, ImportActor.props(service, "testing", importActorHelper));
        buildJob = buildJob("test");
        buildAForkedJob = buildAForkedJob();
        StopWatch stopWatch  = new StopWatch("test");
        stopWatch.start();
        when(jobExecutionContext.getStopWatch()).thenReturn(stopWatch);
    }

    @Test
    public void onReceiveWhenThereAreNoBranches() throws Exception {
        when(jobExecutionContext.getJob()).thenReturn(buildJob);
        TestImportMessage importMessage = createImportMessage();
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getFirstStep().getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(importMessage);
        testActor2.expectMsgClass(StepExecutionContext.class);
        verifyZeroInteractions(listener);
    }

    @Test
    public void onReceiveWhenJobHasBranches()throws Exception {
        when(jobExecutionContext.getJob()).thenReturn(buildAForkedJob);
        TestImportMessage importMessage = createImportMessage();
        when(jobExecutionContextFactory.create(eq("test"), eq(importMessage), Mockito.any(StopWatch.class))).thenReturn(jobExecutionContext);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildAForkedJob.getFirstStep().getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(importMessage);
        jobInitiatorActorRef.expectMsg(jobExecutionContext);
        testActor3.expectNoMessage();
        verifyZeroInteractions(listener);
    }

    @Test
    public void onReceiveWhenJobHasBranchesButNoneOfThePredicatesMatches() throws Exception {
        when(jobExecutionContext.getJob()).thenReturn(buildAForkedJob);
        TestImportMessage importMessage = createImportMessage();
        importMessage.setaNumber(15);
        when(jobExecutionContextFactory.create("nextTestJob", importMessage)).thenReturn(jobExecutionContext);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildAForkedJob.getFirstStep().getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(importMessage);
        jobInitiatorActorRef.expectNoMessage();
        testActor3.expectMsgClass(StepExecutionContext.class);
        verifyZeroInteractions(listener);
    }

    @Test
    public void onReceiveWhenCurrentStepHasATerminatingCondition() throws Exception {
        when(jobExecutionContext.getJob()).thenReturn(buildJob);
        TestImportMessage importMessage = createImportMessage();
        importMessage.setaNumber(70);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(importMessage);
        testActor3.expectNoMessage();
        jobInitiatorActorRef.expectNoMsg();
        verify(listener).onSuccess(any(AfterJobExecutionListenerContext.class));
    }

    @Test
    public void onReceiveWhenCurrentStepHasATerminatingConditionButPredicateDoesNotMatch() throws Exception {
        when(jobExecutionContext.getJob()).thenReturn(buildJob);
        TestImportMessage importMessage = createImportMessage();
        importMessage.setaNumber(15);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(importMessage);
        testActor3.expectMsgClass(StepExecutionContext.class);
        jobInitiatorActorRef.expectNoMsg();
        verifyZeroInteractions(listener);
    }

    @Test
    public void onReceiveWhenThereAreNoBranchesNoTerminatingConditionAndNoNextStep() throws Exception {
        when(jobExecutionContext.getJob()).thenReturn(buildJob);
        TestImportMessage importMessage = createImportMessage();
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getSteps().get(2).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(importMessage);
        verify(listener).onSuccess(any(AfterJobExecutionListenerContext.class));
    }

    @Test
    public void onReceiveWhenAnyPreviousStepWasBuncherStep() throws Exception {
        buncherJob = buildABuncherJob();
        when(jobExecutionContext.getJob()).thenReturn(buncherJob);
        MultiValueMap<String, PlatformMessage> multiValueMap = new LinkedMultiValueMap<>();
        IntStream.range(0, 10).forEach(i -> {
            TestImportMessage testImportMessage = new TestImportMessage("test");
            testImportMessage.setaNumber(i);
            multiValueMap.add(testImportMessage.getMessageId(), testImportMessage);
        });
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buncherJob.getNextStep(buncherJob.getNextStep(buncherJob.getFirstStep())).getStepContext(), jobExecutionContext, multiValueMap);
        testActorRef.tell(stepExecutionContext, null);
        verify(service).apply(multiValueMap);
        testActor3.expectMsgClass(StepExecutionContext.class);
        verifyZeroInteractions(listener);
    }

    @Test
    public void onReceiveWhenServiceClassIsNotProvided(){
        when(jobExecutionContext.getJob()).thenReturn(buildJob);
        testActorRef = TestActorRef.create(_system, ImportActor.props(null, "testing", importActorHelper));
        TestImportMessage importMessage = createImportMessage();
        importMessage.setaNumber(70);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        verify(listener).onFailure(any(AfterJobExecutionListenerContext.class));
    }

    private Job buildJob(String jobName) {
        Predicate<TestImportMessage> importMessagePredicate = new Predicate<TestImportMessage>() {
            @Override
            public boolean test(TestImportMessage message) {
                return message.getaNumber() == 70;
            }
        };

        Job job = JobBuilderFactory.
                get(jobName).
                start(testActor1.ref()).
                next(testActor2.ref()).
                terminateIf(importMessagePredicate).
                next(testActor3.ref()).
                build();
        return job;
    }

    private Job buildAForkedJob() {
        Predicate<TestImportMessage> predicate = new Predicate<TestImportMessage>() {
            @Override
            public boolean test(TestImportMessage testImportMessage) {
                return testImportMessage.getaNumber() == 10;
            }
        };
        Job job = JobBuilderFactory.get("forkedJob").start(testActor2.ref()).
                fork(predicate, buildJob).
                next(testActor3.ref()).build();
        return job;
    }

    private Job buildABuncherJob() {
        Function<TestImportMessage, String> function = new Function<TestImportMessage, String>() {
            @Override
            public String apply(TestImportMessage testImportMessage) {
                return testImportMessage.getMessageId();
            }
        };
        Job job = JobBuilderFactory.get("buncherJob").start(testActor1.ref()).
                accumulate(5, 1000, function, "testBunchr", 1).
                next(testActor2.ref()).
                split().next(testActor1.ref()).build();
        return job;
    }

    private TestImportMessage createImportMessage() {
        TestImportMessage testImportMessage = new TestImportMessage("test");
        testImportMessage.setaNumber(10);
        return testImportMessage;
    }

    class MockListener implements AfterJobExecutionListener {

        @Override
        public void onFailure(AfterJobExecutionListenerContext afterJobExecutionListenerContext) {

        }

        @Override
        public void onSuccess(AfterJobExecutionListenerContext afterJobExecutionListenerContext) {

        }
    }

}