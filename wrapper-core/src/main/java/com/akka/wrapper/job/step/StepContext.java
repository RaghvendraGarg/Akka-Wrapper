package com.akka.wrapper.job.step;

import akka.actor.ActorRef;
import com.akka.wrapper.job.Job;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.util.CollectionUtils;

/**
 * Created by gargr on 16/02/17.
 */
public class StepContext {

    private ActorRef actorRef;

    private Predicate terminatingCondition;

    private List<ForkedStep> jobs;

    private String uniqueStepIdentifier;

    private boolean previousStepWasBunchingStep;

    public StepContext(ActorRef actorRef) {
        this.actorRef = actorRef;
        this.uniqueStepIdentifier = UUID.randomUUID().toString();
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public Predicate getTerminatingCondition() {
        return terminatingCondition;
    }

    public void setTerminatingCondition(Predicate terminatingCondition) {
        this.terminatingCondition = terminatingCondition;
    }

    public void addForkJob(Predicate predicate, Job job) {
        if (CollectionUtils.isEmpty(jobs)) {
            jobs = new ArrayList<>();
        }
        jobs.add(new ForkedStep(predicate, job));
    }

    public List<ForkedStep> getJobs() {
        return jobs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepContext that = (StepContext) o;

        return uniqueStepIdentifier != null ? uniqueStepIdentifier.equals(that.uniqueStepIdentifier) : that.uniqueStepIdentifier == null;
    }

    @Override
    public int hashCode() {
        return uniqueStepIdentifier != null ? uniqueStepIdentifier.hashCode() : 0;
    }

    public boolean isPreviousStepWasBunchingStep() {
        return previousStepWasBunchingStep;
    }

    public void setPreviousStepWasBunchingStep(boolean previousStepWasBunchingStep) {
        this.previousStepWasBunchingStep = previousStepWasBunchingStep;
    }

    @Override
    public String toString() {
        return "StepContext{" +
                "actorRef=" + actorRef +
                ", terminatingCondition=" + terminatingCondition +
                ", jobs=" + jobs +
                ", uniqueStepIdentifier='" + uniqueStepIdentifier + '\'' +
                ", previousStepWasBunchingStep=" + previousStepWasBunchingStep +
                '}';
    }
}
