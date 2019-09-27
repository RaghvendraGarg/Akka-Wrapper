package com.akka.wrapper.job;

import akka.actor.ActorRef;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by gargr on 16/02/17.
 */
public class JobBuilderSteps {

    public interface StartStep extends AfterJobListener, BuildStep {

        AllNextStep start(ActorRef actorRef);

    }

    public interface NextStep {

        AllSteps next(ActorRef actorRef);

        BuildStep next(Job job);

    }

    public interface TerminateStep {

        AllStepsWithOutTerminate terminateIf(Predicate predicate);

    }

    public interface AccumulatingStep {

        NextStep accumulate(int flushSize, long flushInterval, Function fetchKey, String buncherName, int noOfIntances);

        NextStep accumulate(int flushSize, long flushInterval, String buncherName, int noOfIntances);

    }

    public interface SplittingStep {

        NextStep split();

    }

    public interface ForkStep {

        AllStepsWithOutTerminate fork(Predicate predicate, Job job);

    }

    public interface AllNextStep extends NextStep, ForkStep, AccumulatingStep, SplittingStep {

    }

    public interface AllStepsWithOutTerminate extends BuildStep, AllNextStep {

    }

    public interface AllSteps extends AllStepsWithOutTerminate, TerminateStep, AfterJobListener {

    }

    public interface AfterJobListener {

        BuildStep afterJob(AfterJobExecutionListener afterJobExecutionListener);

    }

    public interface BuildStep {

        Job build();

    }
}
