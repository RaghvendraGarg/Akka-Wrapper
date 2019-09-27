package com.akka.wrapper.job.step;

import com.akka.wrapper.job.Job;
import java.util.function.Predicate;

/**
 * Created by gargr on 16/02/17.
 */
public class Step {

    private StepContext stepContext;

    private boolean isBuncherStep;

    public Step(StepContext stepContext) {
        this.stepContext = stepContext;
    }

    public StepContext getStepContext() {
        return stepContext;
    }

    public void addBranchStep(Predicate predicate, Job job) {
        this.stepContext.addForkJob(predicate, job);
    }

    public boolean isBuncherStep() {
        return isBuncherStep;
    }

    public void setBuncherStep(boolean buncherStep) {
        isBuncherStep = buncherStep;
    }

    @Override
    public String toString() {
        return "Step{" +
                "stepContext=" + stepContext +
                ", isBuncherStep=" + isBuncherStep +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Step step = (Step) o;

        return stepContext != null ? stepContext.equals(step.stepContext) : step.stepContext == null;
    }

    @Override
    public int hashCode() {
        return stepContext != null ? stepContext.hashCode() : 0;
    }
}
