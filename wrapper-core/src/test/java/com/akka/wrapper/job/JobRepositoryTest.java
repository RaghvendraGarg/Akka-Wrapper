package com.akka.wrapper.job;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gargr on 01/03/17.
 */
public class JobRepositoryTest {

    private JobRepository jobRepository  = new JobRepository();

    @Test
    public void addJob(){
        Job testJobName = new Job("testJobName");
        jobRepository.addJob(testJobName);
        Assert.assertEquals(testJobName, jobRepository.getJob("testJobName"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJobWhenJobWithSameNameAlreadyExists(){
        Job testJobName = new Job("testJobName");
        jobRepository.addJob(testJobName);
        jobRepository.addJob(testJobName);
    }

    @Test
    public void getJob(){
        Assert.assertNull(jobRepository.getJob(null));
        Assert.assertNull(jobRepository.getJob("noName"));
        Job testJobName = new Job("testJobName");
        jobRepository.addJob(testJobName);
        Assert.assertEquals(testJobName, jobRepository.getJob("testJobName"));
    }

}