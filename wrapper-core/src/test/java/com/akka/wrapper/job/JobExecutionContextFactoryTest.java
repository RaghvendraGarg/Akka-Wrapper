package com.akka.wrapper.job;

import com.akka.wrapper.dto.ImportMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Created by gargr on 01/03/17.
 */
public class JobExecutionContextFactoryTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobExecutionContextFactory jobExecutionContextFactory;

    private Job job = new Job("test");

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWhenJobDoesNotExist(){
        jobExecutionContextFactory.create("new", new ImportMessage("test"));
    }

    @Test
    public void create(){
        Mockito.when(jobRepository.getJob("test")).thenReturn(job);
        ImportMessage test = new ImportMessage("test");
        JobExecutionContext context = jobExecutionContextFactory.create("test", test);
        Assert.assertEquals(job, context.getJob());
        Assert.assertEquals(test, context.getMessage());

    }

}