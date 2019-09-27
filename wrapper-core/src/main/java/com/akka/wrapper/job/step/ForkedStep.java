package com.akka.wrapper.job.step;

import com.akka.wrapper.job.Job;
import java.util.function.Predicate;

public class ForkedStep {

    private Predicate predicate;

    private Job job;

    ForkedStep(Predicate predicate, Job job) {
        this.predicate = predicate;
        this.job = job;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Job getJob() {
        return job;
    }

    @Override
    public String toString() {
        return "ForkedStep{" +
                "predicate=" + predicate +
                ", job=" + job +
                '}';
    }
}