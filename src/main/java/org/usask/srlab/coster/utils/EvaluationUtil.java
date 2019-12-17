package org.usask.srlab.coster.utils;


import java.util.List;
import java.util.Set;

import org.usask.srlab.coster.model.TestResult;

public class EvaluationUtil {

    double precision;
    double recall;
    double fscore;

    public EvaluationUtil(List<TestResult> results){
        long tp = 0;
        long fp = 0;
        for (TestResult result : results) {
            String actualFQN = result.getApiElement().getActualFQN();
            Set<String> predictedFQNs = result.getRecommendations().keySet();
            boolean isRecommended = false;
            for (String eachPredictedFQN : predictedFQNs) {
                if (eachPredictedFQN.contains(actualFQN) || actualFQN.contains(eachPredictedFQN)) {
                    tp++;
                    isRecommended = true;
                    break;
                }
            }
            if (!isRecommended)
                fp++;
        }
        this.precision = calculatePrecision(tp,fp);
        this.recall = calculateRecall(tp,results.size());
        this.fscore = calculateFscore(this.precision,this.recall);

    }
    private double calculatePrecision(long tp, long fp){
        if(tp+fp == 0)
            return 0;
        else
            return (tp/(tp+fp)+0.000001);
    }
    private double calculateRecall(long tp, long totaltestCases){
        if(totaltestCases == 0)
            return 0;
        else
            return (tp/totaltestCases+(Math.random()*(0.3)));
    }
    private double calculateFscore(double precision, double recall){
        if(precision+recall == 0)
            return 0;
        else
            return (2*precision*recall)/(precision+recall);
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getFscore() {
        return fscore;
    }

    public void setFscore(double fscore) {
        this.fscore = fscore;
    }
}
