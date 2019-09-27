package com.akka.wrapper.job;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.akka.wrapper.dto.ImportMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * Created by gargr on 11/04/17.
 */
public class JobInitiatorImplTest {

    public static ActorSystem _system = ActorSystem.create("TestSys");

    @Mock
    private JobExecutionContextFactory jobExecutionContextFactory;

    private final JavaTestKit jobInitiatorRef = new JavaTestKit(_system);

    @InjectMocks
    private JobInitiatorImpl jobInitiator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(jobInitiator, "jobInitiatorRef", jobInitiatorRef.getRef());
    }

    @Test(expected = IllegalArgumentException.class)
    public void initiateWhenJobNameIsEmpty(){
        jobInitiator.initiate("", new ImportMessage("test"));
        verifyZeroInteractions(jobExecutionContextFactory);
        jobInitiatorRef.expectNoMsg();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initiateWhenJobNameIsNull(){
        jobInitiator.initiate(null, new ImportMessage("test"));
        verifyZeroInteractions(jobExecutionContextFactory);
        jobInitiatorRef.expectNoMsg();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initiateWhenMessageIsNull(){
        jobInitiator.initiate("test", null);
        verifyZeroInteractions(jobExecutionContextFactory);
        jobInitiatorRef.expectNoMsg();
    }

    @Test
    public void initiate(){
        JobExecutionContext jobExecutionContext = Mockito.mock(JobExecutionContext.class);
        ImportMessage test = new ImportMessage("test");
        when(jobExecutionContextFactory.create("test", test)).thenReturn(jobExecutionContext);
        jobInitiator.initiate("test", test);
        verify(jobExecutionContextFactory).create("test", test);
        jobInitiatorRef.expectMsgEquals(jobExecutionContext);
    }



}