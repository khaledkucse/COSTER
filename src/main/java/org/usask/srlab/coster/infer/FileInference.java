package org.usask.srlab.coster.infer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import org.usask.srlab.coster.extraction.NonCompilableCodeExtraction;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.OLDEntry;
import org.usask.srlab.coster.model.TestResult;
import org.usask.srlab.coster.utils.FileUtil;
import org.usask.srlab.coster.utils.InferUtil;
import org.usask.srlab.coster.utils.ParseUtil;

public class FileInference {
    private static final Logger logger = LogManager.getLogger(FileInference.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    public static void infer(String jarPath, String inputFilePath, String outPutFilePath, String modelPath, int topk, String contextSim, String nameSim) {
        print("Collecting Jar files...");
        logger.info("Collecting Jar Files...");
        String[] jarPaths = ParseUtil.collectJarFiles(new File(jarPath));
        print("Collecting source code from the  input file...");
        logger.info("Collecting source code from the input file...");
        ArrayList<String> srcList = FileUtil.getSingleTonFileUtilInst().getFileStringArray(inputFilePath);
        String src = StringUtils.join(srcList," ");

        print("Extraiting the API elements from the source code...");
        logger.info("Extraiting the API elements from the source code...");
        List<APIElement> testCases = NonCompilableCodeExtraction.extractCode(src,jarPaths,inputFilePath);


        List<TestResult> testResults = new ArrayList<>();
        if (testCases.size() == 0){
            print("No API element has been found to infer!!!!");
            logger.info("No API element has been found to infer!!!!");
            return;
        }
        int count = 0;
        print("Inferring...");
        logger.info("Inferring...");
        for (APIElement eachCase : testCases) {
            testResults.add(inferEachCase(modelPath, eachCase,contextSim,nameSim,topk));

            count++;
            if (count % 100 == 0) {
                logger.info("API elements Inferred: "+count + "/" + testCases.size() + " (" + df.format((count * 100 / testCases.size())) + "%)");
                print("API elements Inferred: "+count + "/" + testCases.size() + " (" + df.format((count * 100 / testCases.size())) + "%)");
            }
        }

        logger.info("API elements Inferred: "+count + "/" + testCases.size() + " (" + df.format((count * 100 / testCases.size())) + "%)");
        print("API elements Inferred: "+count + "/" + testCases.size() + " (" + df.format((count * 100 / testCases.size())) + "%)");

        logger.info("Writing the inferred FQNs as annotation along with the source code at "+ outPutFilePath+"...");
        print("Writting the inffered FQNs as annotation along with the source code at "+ outPutFilePath+"...");

        writitngOutputFile(outPutFilePath,srcList,testResults);

        logger.info("File Infer is Done!!! See "+outPutFilePath+" for your result!!!");
        print("File Infer is Done!!! See "+outPutFilePath+" for your result!!!");
    }


    private static TestResult inferEachCase(String modelPath, APIElement eachCase, String contextSim, String nameSim, int topk){
        String queryContext = StringUtils.join(eachCase.getContext(), " ").replaceAll(",", "");
        String queryAPIelement = eachCase.getName();
        List<OLDEntry> candidateList = InferUtil.collectCandidateList(queryContext,modelPath);
        Map<String, Double> recommendations = new HashMap<>();
        for (OLDEntry eachCandidate : candidateList) {
            String candidateContext = eachCandidate.getContext();
            String candidateFQN = eachCandidate.getFqn();
            double contextSimialrityScore = InferUtil.calculateContextSimilarity(queryContext,candidateContext,contextSim);
            double nameSimilarityScore = InferUtil.calculateNameSimilarity(queryAPIelement,candidateFQN,nameSim);

            double recommendationScore = InferUtil.calculateRecommendationScore(eachCandidate.getScore(), contextSimialrityScore, nameSimilarityScore);
            if (recommendations.containsKey(candidateFQN) && recommendations.get(candidateFQN) < recommendationScore)
                recommendations.put(candidateFQN, recommendationScore);
            else
                recommendations.put(candidateFQN, recommendationScore);
        }
        recommendations = sortByComparator(recommendations, false, topk);
        return new TestResult(eachCase, recommendations, 0);
    }


    private static void writitngOutputFile(String outPutFilePath, List<String> srcList, List<TestResult> testResults){
        Map<String, Map<String,Double>> results = new HashMap<>();
        for(TestResult eachTesResult: testResults){
            results.put(eachTesResult.getApiElement().getName(),eachTesResult.getRecommendations());
        }
        List<String> outputStrings = new ArrayList<>();
        for(String eachLine:srcList){
            String[] tokens = eachLine.trim().split(" ");
            StringBuilder annotation = new StringBuilder("[");
            for(String eachToken: tokens) {
                eachToken = eachToken.replaceAll(";","");
                String key = contains(results,eachToken);
                if (key != null) {
                    annotation.append(StringUtils.join(results.get(key).keySet(), ","));
                    annotation.append(",");
                }
            }
            if(annotation.length() > 1){
                annotation.deleteCharAt(annotation.length()-1);
                annotation.append("]");
                outputStrings.add(annotation.toString());
            }
            outputStrings.add(eachLine);
        }


        FileUtil.getSingleTonFileUtilInst().writeToFile(outPutFilePath,outputStrings);
    }

    private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order, int topK) {

        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
        list.sort((o1, o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());

            }
        });
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
            count++;
            if(count >= topK)
                break;
        }

        return sortedMap;
    }

    private static String contains(Map<String, Map<String,Double>> resutls, String eachCase) {
        Set<String> keys = resutls.keySet();
        for(String eachKey:keys)
            if (eachKey.contains(eachCase))
                return eachKey;
        return null;
    }

}
