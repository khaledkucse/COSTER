package org.usask.srlab.coster.train;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.utils.InferUtil;
import org.usask.srlab.coster.utils.NotifyingBlockingThreadPoolExecutor;
import org.usask.srlab.coster.utils.ParseUtil;
import org.usask.srlab.coster.utils.TrainUtil;

public class RetrainOLD {
    private static final Logger logger = LogManager.getLogger(RetrainOLD.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    @SuppressWarnings("unchecked")
    public static void retrainOLD() {

        print("Collecting Jar files and Github projects");
        String[] jarPaths = ParseUtil.collectGithubJars(new File(Config.GITHUB_JAR_PATH));
        String[] projectPaths = ParseUtil.collectGithubProjects(new File(Config.GITHUB_SUBJECT_SYSTEM_PATH));

        print("Collecting data set from the Github Dataset");
        NotifyingBlockingThreadPoolExecutor pool = CreateOLD.collectDataset(projectPaths,jarPaths);
        try {
            pool.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        print("Reriving the Trained OLD");
        JSONObject jsonObject =retrivedTrainedOLD();

        print("Populating the dataset in the OLD");
        jsonObject = CreateOLD.populateDatainOLD(new File(Config.GITHUB_DATSET_PATH), jsonObject);

        print("Calculating the occurance likelihood score and storing the context along with FQN and occurance likelihood score in the index file");
        jsonObject = TrainUtil.getSingletonTrainUtilInst().indexData(jsonObject);

        logger.info("Writting the OLD at "+ Config.MODEL_PATH+"OLD.json");
        print("Writting the OLD at "+Config.MODEL_PATH+"OLD.json");
        try {
            Files.write(Paths.get(Config.MODEL_PATH+"OLD.json"), jsonObject.toJSONString().getBytes());
        } catch (IOException e) {
            print("Error Occured while wrtting the OLD. See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }

        logger.info("Creating OLD is done!!!");
        print("Creating OLD is Done!!!");

    }

    @SuppressWarnings("unchecked")
    private static JSONObject retrivedTrainedOLD() {
        JSONObject jsonOLD = new JSONObject();
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(Config.MODEL_PATH+"OLD.json")) {
            IndexSearcher searcher = InferUtil.createSearcher(Config.MODEL_PATH);
            Object obj = jsonParser.parse(reader);
            jsonOLD = (JSONObject) obj;

            int count = 0;
            for(Object eachkey: jsonOLD.keySet()) {
                String eachfqn = eachkey.toString();
                if (eachfqn.equals(":global:"))
                    continue;
                JSONObject fqnObject = (JSONObject) jsonOLD.get(eachfqn);
                JSONArray contextIdArray = (JSONArray) fqnObject.get("context_list");
                if(contextIdArray.size() < Config.FQN_THRESHOLD)
                    continue;

                JSONArray contextArray = new JSONArray();
                //Iterate over each context of the FQN
                for(Object eachObject:contextIdArray){
                    String contextUUID = (String) eachObject;
                    String context = retriveContext(contextUUID,searcher);
                    if(context.trim().equals(""))
                        continue;
                    contextArray.add(context);
                }
                fqnObject.put("context_list",contextArray);
                jsonOLD.put(eachfqn,fqnObject);

                count ++;

                if(count%100 == 0){
                    logger.info(count+" FQNS out of "+jsonOLD.keySet().size()+" are retrived from the trained OLD. Percentage of completion: "+df.format((count*100/jsonOLD.keySet().size()))+"%");
                    print(count+" FQNS out of "+jsonOLD.keySet().size()+" are retrived from the trained OLD. Percentage of completion: "+df.format((count*100/jsonOLD.keySet().size()))+"%");
                }
            }
            logger.info(count+" FQNS out of "+jsonOLD.keySet().size()+" are retrived from the trained OLD. Percentage of completion: "+df.format((count*100/jsonOLD.keySet().size()))+"%");
            print(count+" FQNS out of "+jsonOLD.keySet().size()+" are retrived from the trained OLD. Percentage of completion: "+df.format((count*100/jsonOLD.keySet().size()))+"%");
        } catch (IOException | ParseException e) {
            print("Error Occured while retriving the trained OLD. See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }
        return jsonOLD;
    }

    private static String retriveContext(String uuid,IndexSearcher searcher){
        String returnedContext= "";
        try {
            TopDocs candidate = InferUtil.searchById(uuid,searcher,1);
            for (ScoreDoc eachCandidate : candidate.scoreDocs) {
                Document eachCandDoc = searcher.doc(eachCandidate.doc);
                returnedContext = eachCandDoc.get("context");
            }

        }catch (Exception e) {
            print("Error Occured while searching index of the project file . See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }

        return returnedContext;
    }

}
