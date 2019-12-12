package org.usask.srlab.coster.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.util.FileUtils;

import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.extraction.CompilableCodeExtraction;
import org.usask.srlab.coster.extraction.SOCodeExtraction;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.OLDEntry;

public class InferUtil {
    private static final Logger logger = LogManager.getLogger(InferUtil.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    public static List<APIElement> collectDataset(String[] projectPaths, String[] jarPaths){
        int count  = 0;
        List<APIElement> testcases = new ArrayList<>();
        for(String eachProjectPath:projectPaths){
            try {
                logger.info("Working on the project "+eachProjectPath);
                File eachProject = new File(eachProjectPath.replace(".zip",""));
                UnzipUtil.unzip(eachProjectPath,eachProject.getAbsolutePath());
                List<APIElement> apiElements = CompilableCodeExtraction.extractfromSource(eachProject,jarPaths);
                if(apiElements.size()>0){
                    FileUtil.getSingleTonFileUtilInst().writeCOSTERProjectData(Config.TEST_DATSET_PATH+eachProject.getName()+".csv",apiElements);
                    testcases.addAll(apiElements);
                }

                FileUtils.deleteDirectory(eachProjectPath.replace(".zip",""));
            } catch (Exception e) {
                print("Error Occured while unzipping the project file "+eachProjectPath+". See the detail in the log file");
                for(StackTraceElement eachStacktrace:e.getStackTrace())
                    logger.error(eachStacktrace.toString());

            }
            count ++;
            if(count%500 == 0){
                logger.info(count+" subject systems out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
                print(count+" subject systems out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
            }
        }

        logger.info(count+" compilation units out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
        print(count+" compilation units out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");

        return testcases;
    }

    public static List<APIElement> collectSODataset(String[] sourcefilePaths, String[] jarPaths){
        File projectPath = new File(Config.SO_CODE_SNIPPET_PATH);
        List<APIElement> testcases = new ArrayList<>();
        try {
            List<APIElement> apiElements = SOCodeExtraction.extractFromSOPOST(projectPath,sourcefilePaths,jarPaths);
            if(apiElements.size()>0){
                FileUtil.getSingleTonFileUtilInst().writeCOSTERProjectData(Config.SO_DATASET_PATH+projectPath.getName()+".csv",apiElements);
                testcases.addAll(apiElements);
            }
        } catch (Exception e) {
            print("Error Occured while collecting dataset for Extrinsic Evaluation. See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());

        }

        return testcases;

    }


    public static List<OLDEntry> collectCandidateList(String context){
        List<OLDEntry> candidateList = new ArrayList<>();
        try {
            IndexSearcher searcher = InferUtil.createSearcher(Config.MODEL_PATH);
            TopDocs candidates = InferUtil.searchByContext(context,searcher,1000);

            for (ScoreDoc eachCandidate : candidates.scoreDocs) {
                Document eachCandDoc = searcher.doc(eachCandidate.doc);
                OLDEntry eachCandidateInfo = new OLDEntry(eachCandDoc.get("id"),eachCandDoc.get("context"),eachCandDoc.get("fqn"),Double.parseDouble(eachCandDoc.get("score")));
                candidateList.add(eachCandidateInfo);
            }

        }catch (Exception e) {
            e.printStackTrace();
            print("Error Occured while searching index the project file . See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }
        return candidateList;
    }


    public static double calculateContextSimilarity(String queryContext, String candidateContext){
        CosineSimilarity contextSimilarity = new CosineSimilarity();
        Map<CharSequence, Integer> queryVector = Arrays.stream(queryContext.split(""))
                .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
        Map<CharSequence, Integer> candVector = Arrays.stream(candidateContext.split(""))
                .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));

        return (contextSimilarity.cosineSimilarity(queryVector,candVector));
    }
    public static double calculateNameSimilarity(String queryAPIElement, String candidateFQN){
        LevenshteinDistance distance = new LevenshteinDistance();
        int lev =  distance.apply(queryAPIElement,candidateFQN);
        if(candidateFQN.length() == 0)
            return 0;
        else if(lev <= candidateFQN.length())
            return 0;
        else
            return (1-(lev/candidateFQN.length()));

    }
    public static double calculateRecommendationScore (double likelihoodScore, double contSimScore, double nameSimScore){
        return (Config.alpha*likelihoodScore)+(Config.beta*contSimScore)+(Config.gamma*nameSimScore);
    }

    public static IndexSearcher createSearcher(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }

    public static TopDocs searchById(String id, IndexSearcher searcher, int topn) throws Exception {
//        QueryParser qp = new QueryParser("id", new StandardAnalyzer());
//        Query idQuery = qp.parse(id);
//        return searcher.search(idQuery, topn);
        return searcher.search(new TermQuery(new Term("id", id)), topn);
    }
    private static TopDocs searchByContext(String context, IndexSearcher searcher, int topn) throws Exception {
        QueryParser qp = new QueryParser("context", new WhitespaceAnalyzer());
        Query firstNameQuery = qp.parse(QueryParser.escape(context));
        return searcher.search(firstNameQuery, topn);
    }

    public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order, int topK) {

        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
        list.sort(new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        int count = 0;
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
            count++;
            if(count >= topK*100)
                break;
        }

        return sortedMap;
    }
}
