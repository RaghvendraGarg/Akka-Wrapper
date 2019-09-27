Project aims at creating a wrapper on top of Akka which abstracts out the actor creation, Also provides a much simpler way to configure akka actors in chained fashion to perform a number of pipelined tasks. Similar to a spring batch where you could just configure a Steps to form a Job, but with Akka Wrapper user gets the benefit of parrallelising each step unlike spring batch's complex way of running parallel steps.
Actors can be chained together forming a Job which can be executed using JobIntiatorActor. Also this Api works best when a single message is flowing through the chain. But for some use cases if a bunch of messages has to processed for e.g. for a service call you want to combine multiple request into one, for that Accumulator can be used and later once the job is done again the whole bunch can be converted into individual messages using a Splitter.

**Important Components:**

**Actor:**  
An actor is a container for State, Behavior, a Mailbox, Child Actors and a Supervisor Strategy. All of this is encapsulated behind an Actor Reference.(Read more here)

**Service:** 
Each Actor will be injected with implementation of this interface which will have the actual logic that the underlying actor will invoke.

**ImportMessage:** 
Any message that has to be processed within a Job that has to extend ImportMessage.

Actors can be created using ActorFactory bean

**Creating ActorRef**

 1. By calling create(String actorName, Service service, String processingStep, int noOfInstances) where actorName should be the name of the Actor you want to create, service should be the bean instance of implementation of Service interface, processingStep is the name of the step, and noOfInstances is the no of Actor instance you want. This method returns an Instance of ActorRef, this method by default uses SmallestMailBox router and Akka default dispatcher while creating Actor. if you want to create Actor with different dispatcher and router then use method no.2.

```java
        @Bean
        public ActorRef additionRef() {
            ActorRef actorRef = actorFactory.create("addition", testAdditionService, "addition", 10);
            return actorRef;
        }
```
   2. create(String actorName, Service service, String processingStep) where actorName should be the name of the Actor you want to create, service should be the bean instance of implementation of Service interface,             processingStep is the name of the step, Also it expects the configuration defined in application.conf for e.g. if the actorName is addition and service is additionService with step name addition. application.conf should look   something like this.
```java
        akka.actor.deployment {
                /addition {
                    router = random-pool
                    nr-of-instances = 5
                }
            }
         And bean Configuration will be.
         @Bean
         public ActorRef actorFromApplicationConf(){
              ActorRef actorRef = actorFactory.create("addition", testAdditionService, "actorFromApplicationConf");
              return actorRef;
         }
```
 
**Note: Class containing these @Bean annotations should be marked @Configuration in order to load these beans.**

 
**Job:** 
Once set of actors are created we want to chain them together, so chain of actors is called a Job.
```java
    @Bean
    public Job actorChain(@Qualifier("additionRef") ActorRef additionRef, @Qualifier("subtractionRef") ActorRef subtractionRef) {
                Job build = JobBuilderFactory.get("build").start(additionRef).
                            next(subtractionRef).
                            build(); // end
                            return build;
    }
```
Above code will create a Job with name "build" where the first step in the job will be to call additionRef and then subtractionRef actor. AfterJobExecutionListener can be overriden and added to a Job using afterJob method. Also by default DefaultAfterJobExecutionListenerImpl is injected.

For use cases where a Job has to be terminated based on certain condition while processing the message, that can be configured by injecting a Predicate in the Job configuration Also, a different job can be forked out based on some condition. For e.g.
Creating Job With Branches

```java
    @Bean
    public Job actorChain(@Qualifier("actorRefFromConf") ActorRef additionRef, @Qualifier("subtractionRef") ActorRef subtractionRef,
                          @Qualifier("multiplicationRef") ActorRef multiplicationRef, @Qualifier("divisionRef") ActorRef divisionRef) {
     
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
     
     
        Job build = JobBuilderFactory.get("build").start(additionRef).
                next(subtractionRef).terminateIf(primeNumber).
                next(multiplicationRef).  // will terminate if the number after executing multiplication is a even number else next step will be executed
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
```
 

If certain messages needed to be accumulated and then given to the subsequent actor then accumulate can be called. Once accumulate is called then all subsequent actors will receive a MultiValueMap till split is called.

accumulate method takes 5 parameters 

    1.flushSize : maximum no. of messages each accumulator will hold once this limit is reached accumulator will forward the messages to next actor.
    2.flushInterval : interval till accumulator will keep collecting messages once the configured time has passed accumulator will flush messages to next actor.
     Note : flushSize or flushInterval when either of them crosses the configured value messages are flushed.
    3.fetchKey: since Accumulator holds data in a Map it needs a key. fetchKey is function that will return a key. Also this is an optional parameter if not passed then messageId from ImportMessage will be used as a key.
    4.name: name of the accumulator Actor.
    5.noOfInstances: no of instances of Accumulator actor needed.

```java
    @Bean
    public Job accumulatorWithSplitterJob(@Qualifier("additionRef") ActorRef additionRef, @Qualifier("afterBuncherActor") ActorRef afterBuncherActorRef, @Qualifier("multiplicationRef") ActorRef multiplicationRef) {
     
        Function<TestImportMessage, String> function = new Function<TestImportMessage, String>() {
            @Override
            public String apply(TestImportMessage testImportMessage) {
                return testImportMessage.getMessageId();
            }
        };
        Job job = JobBuilderFactory.get("accumulatorWithSplitter").start(additionRef).
                accumulate(15, 1500l, function, "additionBuncher", 2). // will wait for 1500 ms or till 15 messages are accumulated and then flush to next actor
                next(afterBuncherActorRef). // will receive an instance of MultiValueMap
                split(). // splits the received MultivalueMap into single messages again.
                next(multiplicationRef).
                afterJob(new TestAfterJobListener()).
                build();
        return job;
    }
```
Once a Job is created it can be invoked using JobInitiator.initiate(String jobName, ImportMessage message) where jobName is the name of the job that you want to be invoked and message it object that we want to process in the job.
```java
    @Resource
    private JobInitiator jobInitiator;
      
    public void invokeJob(){
        TestImportMessage testImportMessage = new TestImportMessage("test");
        testImportMessage.setaNumber(number);
        jobInitiator.initiate("build",testImportMessage); // invoking job build with testImportMessage
    }
```

Once the job is completed the implementation of AfterJobExecutionListener bean is invoked if any custom implementation is configured then that will be called else instance of DefaultAfterJobExecutionListenerImpl will be used this interface exposes 2 methods onSuccess and onFailure which can be used for any audit or monitoring purposes.
```java
@Component
public final class DefaultAfterJobExecutionListenerImpl implements AfterJobExecutionListener {
 
    private static final Logger logger = LoggerFactory.getLogger(DefaultAfterJobExecutionListenerImpl.class);
 
    @Override
    public void onFailure(AfterJobExecutionListenerContext j) {
        logger.error("Job failed at step {}, status {}, due to {}, for messageId {}, time taken in ms {}", j.getProcessingStep(), j.getStatus(), j.getMessage(), getMessageIds(j.getObject()), j.getTimeTaken());
    }
 
    @Override
    public void onSuccess(AfterJobExecutionListenerContext j) {
        logger.info("Message processed successfully messageId {}, time taken in ms {}", getMessageIds(j.getObject()), j.getTimeTaken());
    }
 
}
```