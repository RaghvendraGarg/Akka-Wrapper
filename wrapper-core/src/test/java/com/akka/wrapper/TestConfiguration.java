package com.akka.wrapper;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.akka.wrapper.akka.actor.ActorFactoryImpl;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobBuilderFactory;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import com.akka.wrapper.job.listener.AfterJobExecutionListenerContext;

import akka.actor.ActorRef;

/**
 * Created by gargr on 13/02/17.
 */
@Configuration
public class TestConfiguration {

    @Resource
    private TestAdditionService testAdditionService;

    @Resource
    private TestSubtractionService testSubtractionService;

    @Resource
    private TestMultiplicationService testMultiplicationService;

    @Resource
    private ActorFactoryImpl actorFactory;

    @Resource
    private TestDivisionService testDivisionService;

    @Resource
    private TestBunchService testBunchService;

    @Bean
    public ActorRef additionRef() {
        ActorRef actorRef = actorFactory.create("addition", testAdditionService, "addition", 10);
        return actorRef;
    }

    @Bean
    public ActorRef subtractionRef() {
        ActorRef actorRef = actorFactory.create("subtraction", testSubtractionService, "subtraction", 10);
        return actorRef;
    }

    @Bean
    public ActorRef multiplicationRef() {
        ActorRef actorRef = actorFactory.create("multiplication", testMultiplicationService, "multiplication", 10);
        return actorRef;
    }

    @Bean
    public ActorRef divisionRef() {
        ActorRef actorRef = actorFactory.create("division", testDivisionService, "division", 10);
        return actorRef;
    }

    @Bean
    public ActorRef afterBuncherActor() {
        ActorRef actorRef = actorFactory.create("afterBuncher", testBunchService, "afterBuncher", 10);
        return actorRef;
    }

    @Bean
    public ActorRef actorRefFromConf() {
        ActorRef actorRef = actorFactory.create("actorRefFromConf", testAdditionService, "actorRefFromConf", 10);
        return actorRef;
    }

    @Bean
    public Job actorChain(@Qualifier("actorRefFromConf") ActorRef additionRef, @Qualifier("subtractionRef") ActorRef subtractionRef,
                          @Qualifier("multiplicationRef") ActorRef multiplicationRef, @Qualifier("divisionRef") ActorRef divisionRef) {

        Predicate<TestImportMessage> evenNumber = new Predicate<TestImportMessage>() {
            @Override
            public boolean test(TestImportMessage integer) {
                return integer.getaNumber() % 2 == 0;
            }
        };

        Predicate<TestImportMessage> isIsDivisibleByFive = new Predicate<TestImportMessage>() {
            @Override
            public boolean test(TestImportMessage integer) {
                return integer.getaNumber() % 5 == 0;
            }
        };

        Predicate<TestImportMessage> isIsDivisibleBySeven = new Predicate<TestImportMessage>() {
            @Override
            public boolean test(TestImportMessage integer) {
                return integer.getaNumber() % 7 == 0;
            }
        };

        Job addJob = JobBuilderFactory.get("addJob").start(additionRef).next(multiplicationRef).build();

        Job subtractionJob = JobBuilderFactory.get("subtractionJob").start(subtractionRef).next(multiplicationRef).build();

        Job multiplicationJob = JobBuilderFactory.get("multiplicationJob").start(multiplicationRef).next(multiplicationRef).build();

        Job divisionJob = JobBuilderFactory.get("divisionJob").start(divisionRef).next(additionRef).build();

        Job build = JobBuilderFactory.get("remainingJob").
                start(multiplicationRef).  // will terminate if the number after executing subtraction is a even number else next step will be executed
                fork(evenNumber, addJob). // if the number after executing the above step is a prime number then Add job will be called.
                // and will terminate after executing the last actor in Add job
                        fork(isIsDivisibleByFive, subtractionJob). // if the number after executing the above step is divisible by 5 then Subtraction job will be called.
                // and will terminate after executing the last actor in Subtraction job
                        next(divisionRef). // if either of the above two conditions does not match then this call will be executed
                fork(isIsDivisibleBySeven, divisionJob).// if the above fork conditions were not met only then the call will reach here,
                // and if the number is a prime number then division job will be initiated
                        build(); // end

        return build;
    }

    Predicate<TestImportMessage> primeNumber = new Predicate<TestImportMessage>() {
        @Override
        public boolean test(TestImportMessage number) {
            for (int i = 2; i <= number.getaNumber() / 2; i++) {
                if (number.getaNumber() % i == 0) {
                    return false;
                }
            }
            return true;
        }
    };

    @Bean
    public Job remainingJob(@Qualifier("actorChain") Job job, @Qualifier("actorRefFromConf") ActorRef additionRef, @Qualifier("subtractionRef") ActorRef subtractionRef) {

        Job build = JobBuilderFactory.get("build").
                start(additionRef).
                next(subtractionRef).
                terminateIf(primeNumber).
                next(job).
                build();
        return build;
    }

    @Bean
    public Job buncherWithSplitterJob(@Qualifier("additionRef") ActorRef additionRef, @Qualifier("afterBuncherActor") ActorRef afterBuncherActorRef, @Qualifier("multiplicationRef") ActorRef multiplicationRef) {

        Function<TestImportMessage, String> function = new Function<TestImportMessage, String>() {
            @Override
            public String apply(TestImportMessage testImportMessage) {
                return testImportMessage.getMessageId();
            }
        };
        Job job = JobBuilderFactory.get("buncherWithSplitter").start(additionRef).
                accumulate(15, 1500l, function, "additionBuncher", 2).
                next(afterBuncherActorRef).
                split().
                next(multiplicationRef).
                afterJob(new TestAfterJobListener()).
                build();
        return job;
    }

    class TestAfterJobListener implements AfterJobExecutionListener {

        @Override
        public void onFailure(AfterJobExecutionListenerContext afterJobExecutionListenerContext) {
            System.out.println(getMessageIds(afterJobExecutionListenerContext.getObject()));
        }

        @Override
        public void onSuccess(AfterJobExecutionListenerContext afterJobExecutionListenerContext) {
            System.out.println(getMessageIds(afterJobExecutionListenerContext.getObject()));
        }
    }

}
