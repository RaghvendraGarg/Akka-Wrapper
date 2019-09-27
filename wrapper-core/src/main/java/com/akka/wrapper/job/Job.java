package com.akka.wrapper.job;

import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import com.akka.wrapper.job.step.Step;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gargr on 15/02/17.
 */
public final class Job {

    private List<Step> steps = new ArrayList<>();

    private AfterJobExecutionListener afterJobExecutionListener;

    private String name;

    Job(String name) {
        this.name = name;
    }

    public Step getFirstStep() {
        if (steps.isEmpty()) {
            return null;
        }
        return steps.get(0);
    }

    public Step getNextStep(Step step) {
        if (steps.isEmpty()) {
            return null;
        }
        int i = steps.indexOf(step);
        if (i < steps.size() - 1) {
            return steps.get(i + 1);
        }
        return null;
    }

    void addSteps(List<Step> steps) {
        this.steps.addAll(steps);
    }

    void setAfterJobExecutionListener(AfterJobExecutionListener afterJobExecutionListener) {
        this.afterJobExecutionListener = afterJobExecutionListener;
    }

    public AfterJobExecutionListener getAfterJobExecutionListener() {
        return afterJobExecutionListener;
    }

    public String getName() {
        return name;
    }

    public List<Step> getSteps() {
        return new ArrayList<>(steps);
    }

    @Override
    public String toString() {
        return "Job{" +
                "steps=" + steps +
                ", afterJobExecutionListener=" + afterJobExecutionListener +
                ", name='" + name + '\'' +
                '}';
    }
}
