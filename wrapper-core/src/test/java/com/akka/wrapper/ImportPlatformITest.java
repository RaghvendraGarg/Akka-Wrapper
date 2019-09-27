package com.akka.wrapper;

import com.akka.wrapper.job.JobInitiator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = { "server.port=8080" })
public class ImportPlatformITest {

    @Resource
    private JobInitiator jobInitiator;

    @Resource
    private TestBunchService testBunchService;

    @Test
    public void testJobWhenNumberIsPrimeNumberAtStep2() throws InterruptedException {
        int i = assertValue(5);
        Assert.assertEquals(5, i);
    }

    @Test
    public void jobWhenNumberIsEvenAtFork1() throws Exception {
        int i = assertValue(4);
        Assert.assertEquals(45, i);
    }

    @Test
    public void jobWhenNumberIsDivisibleBy5AtFork2() throws Exception {
        int i = assertValue(15);
        Assert.assertEquals(126, i);
    }

    @Test
    public void jobWhenNumberIsNotEvenAndNotDivisibleBy5() throws Exception {
        int i = assertValue(9);
        Assert.assertEquals(9, i);
    }

    @Test
    public void jobWhenNumberIsNotEvenAndDivisibleBy7() throws Exception {
        int i = assertValue(21);
        Assert.assertEquals(10, i);
    }

    @Test
    public void buncherJob() throws InterruptedException {
        List<TestImportMessage> testImportMessages = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            TestImportMessage testImportMessage = new TestImportMessage("Accumulator");
            testImportMessage.setaNumber(5 * i);
            testImportMessages.add(testImportMessage);
        }

        testImportMessages.stream().forEach(t -> {
            jobInitiator.initiate("buncherWithSplitter", t);
        });
        Thread.sleep(5000);
        Assert.assertEquals(10, testBunchService.getStringTestImportMessageMultiValueMap().size());
        testImportMessages.stream().forEach(v -> {
            System.out.println(v.getaNumber());
            Assert.assertEquals(0, v.getaNumber() % 3); // checking if multiplcation actor was called for each message
        });
    }

    private int assertValue(int number) throws InterruptedException {
        TestImportMessage testImportMessage = new TestImportMessage("test");
        testImportMessage.setaNumber(number);
        jobInitiator.initiate("build", testImportMessage);
        Thread.sleep(2000);
        return testImportMessage.getaNumber();
    }

}
