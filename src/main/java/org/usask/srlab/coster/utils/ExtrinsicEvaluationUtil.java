package org.usask.srlab.coster.utils;

import org.usask.srlab.coster.model.ExtrinsicTestResult;
import org.usask.srlab.coster.model.OLDEntry;
import org.usask.srlab.coster.model.TestResult;

import java.util.List;
import java.util.Set;

public class ExtrinsicEvaluationUtil {
    double precision;
    double recall;
    double fscore;

    public ExtrinsicEvaluationUtil(List<ExtrinsicTestResult> results){
        long tp = 0;
        long fp = 0;
        for (ExtrinsicTestResult result : results) {
            String actualFQN = result.getApiElement().getActualFQN();

//            Set<String> predictedFQNs = result.getRecommendations().keySet();

            if(isHave(result.getRecommendations(),actualFQN))
                tp++;
            else
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
            return ((tp+0.000001)/(tp+fp+0.000001));
    }
    private double calculateRecall(long tp, long totaltestCases){
        if(totaltestCases == 0)
            return 0;
        else {
            double recall = ((tp + 0.00001) / (totaltestCases + 0.00001));
            return recall > 1 ? 1 - (1 - recall) : recall;
        }
    }
    private double calculateFscore(double precision, double recall){
        if(precision+recall == 0)
            return 0;
        else
            return (2*precision*recall)/(precision+recall);
    }

    private static boolean contains(Set<String> resutls, String eachCase) {
        for(String eachResult:resutls)
            if (eachResult.contains(eachCase) || eachCase.contains(eachResult))
                return true;
        return false;
    }
    private static boolean isHave(List<OLDEntry> resutls, String eachCase) {
        for(OLDEntry eachResult:resutls)
            if (eachResult.getFqn().contains(eachCase) || eachCase.contains(eachResult.getFqn()))
                return true;
        return false;
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
