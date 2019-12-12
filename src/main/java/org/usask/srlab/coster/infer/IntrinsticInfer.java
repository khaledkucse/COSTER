package org.usask.srlab.coster.infer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.OLDEntry;
import org.usask.srlab.coster.model.TestResult;
import org.usask.srlab.coster.utils.EvaluationUtil;
import org.usask.srlab.coster.utils.InferUtil;
import org.usask.srlab.coster.utils.ParseUtil;

public class IntrinsticInfer {
    private static final Logger logger = LogManager.getLogger(IntrinsticInfer.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}


    public static void instrinsicEvaluation(int topk) {
        print("Collecting Jar files and Test Projects");
        logger.info("Collecting Jar Files and Test Projects");
        String[] jarPaths = ParseUtil.collectGithubJars(new File(Config.GITHUB_JAR_PATH));
        String[] projectPaths = ParseUtil.collectGithubProjects(new File(Config.TEST_SUBJECT_SYSTEM_PATH));

        print("Collecting data set from the Test Dataset");
        logger.info("Collecting dataset from the Test Dataset");
        List<APIElement> testCases = InferUtil.collectDataset(projectPaths,jarPaths);
        List<TestResult> testResults = new ArrayList<>();
        int count = 0;
        long totalInferenceTime = 0;
        print("Inferring test cases");
        logger.info("Inferring test cases");
        for(APIElement eachCase:testCases){
            long starttime = System.currentTimeMillis();
            String queryContext = StringUtils.join(eachCase.getContext()," ").replaceAll(",","");
            String queryAPIelement = eachCase.getName();
            List<OLDEntry> candidateList = InferUtil.collectCandidateList(queryContext);
            Map<String, Double> recommendations = new HashMap<>();
            for(OLDEntry eachCandidate:candidateList){
                String candidateContext = eachCandidate.getContext();
                String candidateFQN = eachCandidate.getFqn();
                double contextSimialrityScore = InferUtil.calculateContextSimilarity(queryContext,candidateContext);
                double nameSimilarityScore = InferUtil.calculateNameSimilarity(queryAPIelement,candidateFQN);
                double recommendationScore = InferUtil.calculateRecommendationScore(eachCandidate.getScore(),contextSimialrityScore,nameSimilarityScore);
                if(recommendations.containsKey(candidateFQN) && recommendations.get(candidateFQN) < recommendationScore)
                    recommendations.put(candidateFQN,recommendationScore);
                else
                    recommendations.put(candidateFQN,recommendationScore);
            }
            long inferenceTime = System.currentTimeMillis()-starttime;
            totalInferenceTime += inferenceTime;
            recommendations = InferUtil.sortByComparator(recommendations,false,topk);
            TestResult eachTestResult = new TestResult(eachCase,recommendations, inferenceTime);
            testResults.add(eachTestResult);

            count++;
            if(count%100 == 0){
                logger.info(count+" test cases out of "+testCases.size()+" are inferred. Percentage of completion: "+df.format((count*100/testCases.size()))+"%");
                print(count+" test cases out of "+testCases.size()+" are inferred. Percentage of completion: "+df.format((count*100/testCases.size()))+"%");
            }
        }

        logger.info(count+" test cases out of "+testCases.size()+" are inferred. Percentage of completion: "+df.format((count*100/testCases.size()))+"%");
        print(count+" test cases out of "+testCases.size()+" are inferred. Percentage of completion: "+df.format((count*100/testCases.size()))+"%");
        logger.info("Average time for inference: "+ (double)(totalInferenceTime/testCases.size()) + "milliseconds");

        logger.info("Calculating performance mesures");
        EvaluationUtil evaluationUtil = new EvaluationUtil(testResults);
        print("Precision: "+evaluationUtil.getPrecision());
        print("Recall: "+evaluationUtil.getRecall());
        print("FScore: "+evaluationUtil.getFscore());
        logger.info("Precision: "+evaluationUtil.getPrecision());
        logger.info("Recall: "+evaluationUtil.getRecall());
        logger.info("FScore: "+evaluationUtil.getFscore());

        logger.info("Intrinsic Evaluation is done!!!");
        print("Intrinsic Evaluation is Done!!!");

    }




}
