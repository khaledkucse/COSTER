package org.usask.srlab.coster.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.util.FileUtils;


import org.usask.srlab.coster.COSTER;
import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.extraction.CompilableCodeExtraction;
import org.usask.srlab.coster.extraction.SOCodeExtraction;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.OLDEntry;
import org.usask.srlab.coster.model.OLDEntryComparer;

public class InferUtil {
    private static final Logger logger = LogManager.getLogger(InferUtil.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    public static List<APIElement> collectDataset(String[] projectPaths, String[] jarPaths, String datasetPath){
        int count  = 0;
        List<APIElement> testcases = new ArrayList<>();
        for(String eachProjectPath:projectPaths){
            try {
                logger.info("Working on the project "+eachProjectPath);
                File eachProject = new File(eachProjectPath.replace(".zip",""));
                UnzipUtil.unzip(eachProjectPath,eachProject.getAbsolutePath());
                List<APIElement> apiElements = CompilableCodeExtraction.extractfromSource(eachProject,jarPaths);
                if(apiElements.size()>0){
                    FileUtil.getSingleTonFileUtilInst().writeCOSTERProjectData(datasetPath+eachProject.getName()+".csv",apiElements);
                    testcases.addAll(apiElements);
                }

                FileUtils.deleteDirectory(eachProjectPath.replace(".zip",""));
            } catch (Exception e) {
                print("Error Occurred while unzipping the project file "+eachProjectPath+". See the detail in the log file");
                logger.error(e.getMessage());
                for(StackTraceElement eachStacktrace:e.getStackTrace())
                    logger.error(eachStacktrace.toString());

            }
            count ++;
            if(count%500 == 0){
                logger.info("Subject Systems Parsed: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
                print("Subject Systems Parsed: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
            }
        }

        logger.info("Subject Systems Parsed: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
        print("Subject Systems Parsed: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");

        return testcases;
    }

    public static List<APIElement> collectSODataset(String[] sourcefilePaths, String[] jarPaths, String repositoryPath, String datasetPath){
        File projectPath = new File(repositoryPath);
        List<APIElement> testcases = new ArrayList<>();
        try {
            List<APIElement> apiElements = SOCodeExtraction.extractFromSOPOST(projectPath,sourcefilePaths,jarPaths);
            if(apiElements.size()>0){
                FileUtil.getSingleTonFileUtilInst().writeCOSTERProjectData(datasetPath+projectPath.getName()+".csv",apiElements);
                testcases.addAll(apiElements);
            }
        } catch (Exception e) {
            print("Error Occurred while collecting dataset for Extrinsic Evaluation. See the detail in the log file");
            logger.error(e.getMessage());
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());

        }

        return testcases;

    }

    public static List<APIElement> collectTestAPISFromDATASET(String datasetPath){
        return FileUtil.getSingleTonFileUtilInst().readTestCase(new File(datasetPath));
    }

    public static List<OLDEntry> generateList(List<OLDEntry> candidates){
        candidates.sort(new OLDEntryComparer());
        Collections.reverse(candidates);
        return candidates.subList(0,COSTER.getReccs());
    }

    public static List<OLDEntry> collectCandidateList(String context, String modelPath){
        List<OLDEntry> candidateList = new ArrayList<>();
        try {
            IndexSearcher searcher = InferUtil.createSearcher(modelPath);
            TopDocs candidates = InferUtil.searchByContext(context,searcher);
            double topScore = 0;
            for (ScoreDoc eachCandidate : candidates.scoreDocs) {
                if(topScore == 0)
                    topScore = eachCandidate.score;
                Document eachCandDoc = searcher.doc(eachCandidate.doc);
                OLDEntry eachCandidateInfo = new OLDEntry(eachCandDoc.get("id"),eachCandDoc.get("context"),eachCandDoc.get("fqn"),(eachCandidate.score/topScore));
                candidateList.add(eachCandidateInfo);
            }

        }catch (Exception e) {
            print("Error Occurred while searching index the project file . See the detail in the log file");
            logger.error(e.getMessage());
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }
        return candidateList;
    }

    public static IndexSearcher createSearcher(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        return new IndexSearcher(reader);
    }

    public static TopDocs searchById(String id, IndexSearcher searcher, int topn) throws Exception {
        return searcher.search(new TermQuery(new Term("id", id)), topn);
    }
    private static TopDocs searchByContext(String context, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("context", new WhitespaceAnalyzer());
        Query firstNameQuery = qp.parse(QueryParser.escape(context));
        return searcher.search(firstNameQuery, 1000);
    }

    public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order, int topK) {

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



    public static double calculateContextSimilarity(String queryContext, String candidateContext, String contextSim){
        double contextSimialrityScore = 0;
        switch (contextSim) {
            case "cosine":
                contextSimialrityScore = InferUtil.cosineSimilarity(queryContext, candidateContext);
                break;
            case "jaccard":
                contextSimialrityScore = InferUtil.jaccardSimialrity(queryContext, candidateContext);
                break;
            case "lcs":
                contextSimialrityScore = InferUtil.lcsSimilarity(queryContext, candidateContext);
                break;
        }
        return contextSimialrityScore;
    }
    public static double calculateNameSimilarity(String queryAPIElement, String candidateFQN, String nameSim){
        double nameSimilarityScore = 0;
        switch (nameSim) {
            case "levenshtein":
                nameSimilarityScore = InferUtil.levenshteinSimilarity(queryAPIElement, candidateFQN);
                break;
            case "hamming":
                nameSimilarityScore = InferUtil.hammingDistance(queryAPIElement, candidateFQN);
                break;
            case "lcs":
                nameSimilarityScore = InferUtil.lcsSimilarity(queryAPIElement, candidateFQN);
                break;
        }
        return nameSimilarityScore;
    }
    public static double calculateRecommendationScore (double likelihoodScore, double contSimScore, double nameSimScore){
        return (Config.alpha*likelihoodScore)+(Config.beta*contSimScore)+(Config.gamma*nameSimScore);
    }

    private static double cosineSimilarity(String queryContext, String candidateContext){
        CosineSimilarity contextSimilarity = new CosineSimilarity();
        Map<CharSequence, Integer> queryVector = Arrays.stream(queryContext.split(""))
                .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
        Map<CharSequence, Integer> candVector = Arrays.stream(candidateContext.split(""))
                .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));

        return (contextSimilarity.cosineSimilarity(queryVector,candVector));

    }

    private static double jaccardSimialrity(String queryContext, String candidateContext){
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        return jaccardSimilarity.apply(queryContext,candidateContext);
    }

    private static double lcsSimilarity(String query, String candidate){
        LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
        int lcs = longestCommonSubsequence.apply(query,candidate);
        if(candidate.length() == 0)
            return 0;
        else if(lcs <= candidate.length())
            return 0;
        else
            return (1-(lcs/candidate.length()));
    }

    private static double levenshteinSimilarity(String queryAPIElement, String candidateFQN){
        LevenshteinDistance distance = new LevenshteinDistance();
        int lev =  distance.apply(queryAPIElement,candidateFQN);
        if(candidateFQN.length() == 0)
            return 0;
        else if(lev <= candidateFQN.length())
            return 0;
        else
            return (1-(lev/candidateFQN.length()));

    }

    private static double hammingDistance(String queryAPIElement, String candidateFQN){
        HammingDistance hammingDistance = new HammingDistance();
        int hamDis = hammingDistance.apply(queryAPIElement,candidateFQN);
        if(candidateFQN.length() == 0)
            return 0;
        else if(hamDis <= candidateFQN.length())
            return 0;
        else
            return (1-(hamDis/candidateFQN.length()));
    }
}
