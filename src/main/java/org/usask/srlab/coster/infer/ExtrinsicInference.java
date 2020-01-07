package org.usask.srlab.coster.infer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.OLDEntry;
import org.usask.srlab.coster.model.TestResult;
import org.usask.srlab.coster.utils.EvaluationUtil;
import org.usask.srlab.coster.utils.InferUtil;
import org.usask.srlab.coster.utils.ParseUtil;


public class ExtrinsicInference {
    private static final Logger logger = LogManager.getLogger(ExtrinsicInference.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    public static void evaluation(String jarPath, String repositoryPath, String datasetPath, String modelPath, int topk, String contextSim, String nameSim){
        print("Collecting Jar files...");
        logger.info("Collecting Jar Files...");
        String[] jarPaths = ParseUtil.collectJarFiles(new File(jarPath));
        print("Collecting StackOverflow code snippets...");
        logger.info("Collecting StackOverflow code snippets...");
        ArrayList<String> snippetPaths = ParseUtil.collectSOSnippets(new File(repositoryPath));
        String[] sourcefilePaths = new String[snippetPaths.size()];
        sourcefilePaths = snippetPaths.toArray(sourcefilePaths);
        logger.info("Total Number of StackOverflow Code Snippet: "+ sourcefilePaths.length);

        print("Extracting test data from the StackOverflow code snippets...");
        logger.info("Extracting test data from the StackOverflow code snippets...");
        List<APIElement> testCases = InferUtil.collectSODataset(sourcefilePaths,jarPaths,repositoryPath,datasetPath);

        List<TestResult> testResults = new ArrayList<>();
        int count = 0;
        long totalInferenceTime = 0;
        print("Inferring test data...");
        logger.info("Inferring test data...");
        for(APIElement eachCase:testCases){
            long starttime = System.currentTimeMillis();
            String queryContext = StringUtils.join(eachCase.getContext()," ").replaceAll(",","");
            String queryAPIelement = eachCase.getName();
            List<OLDEntry> candidateList = InferUtil.collectCandidateList(queryContext, modelPath);
            Map<String, Double> recommendations = new HashMap<>();
            for(OLDEntry eachCandidate:candidateList){
                String candidateContext = eachCandidate.getContext();
                String candidateFQN = eachCandidate.getFqn();
                double contextSimialrityScore = InferUtil.calculateContextSimilarity(queryContext,candidateContext, contextSim);
                double nameSimilarityScore = InferUtil.calculateNameSimilarity(queryAPIelement,candidateFQN, nameSim);
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
                logger.info("Test Data Inferred: "+count+"/"+testCases.size()+" ("+df.format((count*100/testCases.size()))+"%)");
                print("Test Data Inferred: "+count+"/"+testCases.size()+" ("+df.format((count*100/testCases.size()))+"%)");
            }
        }

        logger.info("Test Data Inferred: "+count+"/"+testCases.size()+" ("+df.format((count*100/testCases.size()))+"%)");
        print("Test Data Inferred: "+count+"/"+testCases.size()+" ("+df.format((count*100/testCases.size()))+"%)");

        print("Average time for inference: "+ (double)(totalInferenceTime/testCases.size()) + "milliseconds");
        logger.info("Average time for inference: "+ (double)(totalInferenceTime/testCases.size()) + "milliseconds");

        logger.info("Calculating performance mesures...");
//        int totalCases = CompilableCodeExtraction.getTotalCase().get();
        EvaluationUtil evaluationUtil = new EvaluationUtil(testResults);
        print("Precision: "+String.format("%.2f",evaluationUtil.getPrecision()));
        print("Recall: "+String.format("%.2f",evaluationUtil.getRecall()));
        print("FScore: "+String.format("%.2f",evaluationUtil.getFscore()));
        logger.info("Precision: "+String.format("%.2f",evaluationUtil.getPrecision()));
        logger.info("Recall: "+String.format("%.2f",evaluationUtil.getRecall()));
        logger.info("FScore: "+String.format("%.2f",evaluationUtil.getFscore()));

        logger.info("Extrinsic Evaluation is done!!!");
        print("Extrinsic Evaluation is Done!!!");
    }
}
