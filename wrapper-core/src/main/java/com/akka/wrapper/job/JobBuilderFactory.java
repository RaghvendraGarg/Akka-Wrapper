package com.akka.wrapper.job;

import akka.actor.ActorRef;
import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.JobBuilderSteps.*;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import com.akka.wrapper.job.step.Step;
import com.akka.wrapper.job.step.StepContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Created by gargr on 16/02/17.
 */
@Component
public class JobBuilderFactory {

    private static JobBuilderFactoryHelper jobBuilderFactoryHelper;

    public static StartStep get(String jobName) {
        Steps steps = new Steps(jobName, jobBuilderFactoryHelper);
        return steps;
    }

    @Autowired
    public void setJobBuilderFactoryHelper(JobBuilderFactoryHelper jobBuilderFactoryHelper) {
        JobBuilderFactory.jobBuilderFactoryHelper = jobBuilderFactoryHelper;
    }

    private static class Steps implements StartStep, AllSteps, SplittingStep {

        private List<Step> steps = new ArrayList<>();

        private AfterJobExecutionListener afterJobExecutionListener;

        private StepContext lastStepContext;

        private String name;

        private JobBuilderFactoryHelper factoryHelper;

        private Steps(String name, JobBuilderFactoryHelper factoryHelper) {
            this.factoryHelper = factoryHelper;
            this.name = name;
            this.afterJobExecutionListener = factoryHelper.getAfterJobExecutionListener();
        }

        @Override
        public AllNextStep start(ActorRef actorRef) {
            Validate.notNull(actorRef, "Actor Ref cannot be null");
            addStep(actorRef, false);
            return this;
        }

        @Override
        public AllSteps next(ActorRef actorRef) {
            Validate.notNull(actorRef, "Actor Ref cannot be null");
            addStep(actorRef, false);
            return this;
        }

        /**
         * This will always be the last @{@link Step} in the @{@link Job} config. as Once the execution moves to this @{@link Job} there is no coming
         * back to the old job. Also after this step @{@link AfterJobExecutionListener} cannot be added for the @{@link Job} getting configured, but @{@link AfterJobExecutionListener}
         * of the injected @{@link Job} will be excuted at the end.
         * @param job
         * @return
         */
        @Override
        public BuildStep next(Job job) {
            Validate.notNull(job, "Job instance cannot be null");
            this.afterJobExecutionListener = job.getAfterJobExecutionListener();
            List<Step> steps = job.getSteps();
            this.steps.addAll(steps);
            return this;
        }

        @Override
        public AllStepsWithOutTerminate terminateIf(Predicate predicate) {
            lastStepContext.setTerminatingCondition(predicate);
            return this;
        }

        @Override
        public AllStepsWithOutTerminate fork(Predicate predicate, Job job) {
            Validate.notNull(predicate, "predicate cannot be null");
            Validate.notNull(job, "Job cannot be null");
            Step lastStep = steps.get(steps.size() - 1);
            lastStep.addBranchStep(predicate, job);
            return this;
        }

        @Override
        public NextStep accumulate(int flushSize, long flushInterval, Function fetchKey, String name, int noOfInstances) {
            ActorRef buncher = factoryHelper.getAccumulatorFactory().createAccumulator(name, flushSize, flushInterval, fetchKey, noOfInstances);
            addStep(buncher, true);
            return this;
        }

        @Override
        public NextStep accumulate(int flushSize, long flushInterval, String name, int noOfInstances) {
            Function<PlatformMessage, String> fetchKey = new Function<PlatformMessage, String>() {
                @Override
                public String apply(PlatformMessage testImportMessage) {
                    return testImportMessage.getMessageId();
                }
            };

            ActorRef buncher = factoryHelper.getAccumulatorFactory().createAccumulator(name, flushSize, flushInterval, fetchKey, noOfInstances);
            addStep(buncher, true);
            return this;
        }

        @Override
        public NextStep split() {
            addStep(factoryHelper.getAccumulatorFactory().getSplittingActorRef(), false);
            return this;
        }

        @Override
        public Job build() {
            Job job = new Job(name);
            job.addSteps(steps);
            job.setAfterJobExecutionListener(afterJobExecutionListener);
            factoryHelper.getJobRepository().addJob(job);
            return job;
        }

        @Override
        public BuildStep afterJob(AfterJobExecutionListener afterJobExecutionListener) {
            Validate.notNull(afterJobExecutionListener, "listener cannot be null");
            this.afterJobExecutionListener = afterJobExecutionListener;
            return this;
        }

        private void addStep(ActorRef actorRef, boolean isBunchStep) {
            boolean isPreviousStepWasBunching = isPreviousStepWasBunching(isBunchStep);
            StepContext stepContext = getStepContext(actorRef);
            Step e = new Step(stepContext);
            stepContext.setPreviousStepWasBunchingStep(isPreviousStepWasBunching);
            e.setBuncherStep(isBunchStep);
            steps.add(e);
        }

        private boolean isPreviousStepWasBunching(boolean isBunchStep) {
            if (!isBunchStep) {
                if (!CollectionUtils.isEmpty(steps)) {
                    Step lastStep = steps.get(steps.size() - 1);
                    return lastStep.isBuncherStep();
                }
            }
            return false;
        }

        private StepContext getStepContext(ActorRef actorRef) {
            StepContext context = new StepContext(actorRef);
            lastStepContext = context;
            return context;
        }

    }
}
