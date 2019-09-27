package com.akka.wrapper.akka.actor.buncher;

import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.akka.wrapper.TestImportMessage;
import com.akka.wrapper.dto.ImportMessage;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobBuilderFactory;
import com.akka.wrapper.job.JobBuilderFactoryHelper;
import com.akka.wrapper.job.JobExecutionContext;
import com.akka.wrapper.job.JobRepository;
import com.akka.wrapper.job.listener.DefaultAfterJobExecutionListenerImpl;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;

import akka.actor.ActorSystem;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;

/**
 * Created by gargr on 06/03/17.
 */
public class AccumulatorTest extends TestKit {

    public static ActorSystem _system = ActorSystem.create("TestSys");

    private TestActorRef<Accumulator> testActorRef;

    public AccumulatorTest() {
        super(_system);
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
    public void setup(){
        MockitoAnnotations.initMocks(this);
        Function<ImportMessage, String> function = new Function<ImportMessage, String>() {
            @Override
            public String apply(ImportMessage message) {
                return message.getMessageId();
            }
        };
        testActorRef = TestActorRef.create(_system, Accumulator.props(2, 300, function));
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
    public void buncherWhenFlushSizeHasReached(){
        TestImportMessage importMessage = createImportMessage(10);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        importMessage = createImportMessage(14);
        stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        importMessage = createImportMessage(17);
        stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        testActor2.expectMsgClass(StepExecutionContext.class);
    }

    @Test
    public void buncherWhenFlushIntervalHasPassed() throws InterruptedException {
        TestImportMessage importMessage = createImportMessage(10);
        StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(buildJob.getNextStep(buildJob.getFirstStep()).getStepContext(), jobExecutionContext, importMessage);
        testActorRef.tell(stepExecutionContext, null);
        Thread.sleep(500);
        testActor2.expectMsgClass(StepExecutionContext.class);
    }

    @Test
    public void buncherWhenThrowsException() throws InterruptedException {
        testActorRef.tell("test", null);
        Thread.sleep(400);
        testActor2.expectNoMessage();
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

    private TestImportMessage createImportMessage(int no) {
        TestImportMessage testImportMessage = new TestImportMessage("test");
        testImportMessage.setaNumber(no);
        return testImportMessage;
    }


}