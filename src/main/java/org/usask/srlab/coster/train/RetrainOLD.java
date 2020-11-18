package org.usask.srlab.coster.train;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;


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


import org.usask.srlab.coster.COSTER;
import org.usask.srlab.coster.utils.InferUtil;
import org.usask.srlab.coster.utils.NotifyingBlockingThreadPoolExecutor;
import org.usask.srlab.coster.utils.ParseUtil;
import org.usask.srlab.coster.utils.TrainUtil;

public class RetrainOLD {
    private static final Logger logger = LogManager.getLogger(RetrainOLD.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    @SuppressWarnings("unchecked")
    public static void retrain(){
        if(COSTER.getIsExtraction()){
            print("Collecting Jar files...");
            String[] jarPaths = ParseUtil.collectJarFiles(new File(COSTER.getJarRepoPath()));
            String[] projectPaths = ParseUtil.collectGithubProjects(new File(COSTER.getRepositoryPath()));

            print("Extracting subject systems from the repository...");
            NotifyingBlockingThreadPoolExecutor pool = Train.collectDataset(projectPaths,jarPaths, COSTER.getDatasetPath());
            try {
                pool.await();
            } catch (InterruptedException e) {
                print("Error Occurred while extracting subject systems from the repository. See the detail in the log file");
                logger.error(e.getMessage());
                for(StackTraceElement eachStacktrace:e.getStackTrace())
                    logger.error(eachStacktrace.toString());
            }
        }

        print("Retrieving the Trained Model...");
        JSONObject jsonObject = retriveTrainedOLD(COSTER.getModelPath(), COSTER.getFqnThreshold());

        print("Populating the data from subject systems into the trained model...");
        jsonObject = Train.populateDatainOLD(new File(COSTER.getDatasetPath()), jsonObject);

        print("Calculating the occurrence likelihood score...");
        jsonObject = TrainUtil.getSingletonTrainUtilInst().indexData(jsonObject, COSTER.getModelPath(),COSTER.getFqnThreshold());

        logger.info("Storing the model at "+ COSTER.getModelPath()+"...");
        print("Storing the model at "+COSTER.getModelPath()+"...");
        try{
            FileOutputStream fos = new FileOutputStream(COSTER.getModelPath()+"OLD.json");
            if (jsonObject.toJSONString() != null) {
                fos.write(jsonObject.toJSONString().getBytes());
            }
            fos.close();
//            Files.write(Paths.get(COSTER.getModelPath()+"OLD.json"), jsonObject.toJSONString().getBytes());
        } catch (IOException e) {
            print("Error Occurred while strong the model. See the detail in the log file");
            logger.error(e.getMessage());
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }

        logger.info("Retraining is done!!!");
        print("Retraining is Done!!!");

    }

    @SuppressWarnings("unchecked")
    private static JSONObject retriveTrainedOLD(String modelPath, int fqnThreshold) {
        JSONObject jsonOLD = new JSONObject();
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(modelPath+"OLD.json")) {
            IndexSearcher searcher = InferUtil.createSearcher(modelPath);
            Object obj = jsonParser.parse(reader);
            jsonOLD = (JSONObject) obj;

            int count = 0;
            for(Object eachkey: jsonOLD.keySet()) {
                String eachfqn = eachkey.toString();
                if (eachfqn.equals(":global:"))
                    continue;
                JSONObject fqnObject = (JSONObject) jsonOLD.get(eachfqn);
                JSONArray contextIdArray = (JSONArray) fqnObject.get("context_list");
                if(contextIdArray.size() < fqnThreshold)
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
                    logger.info("FQNs Retrieved: "+count+"/"+jsonOLD.keySet().size()+" ("+df.format((count*100/jsonOLD.keySet().size()))+"%)");
                    print("FQNs Retrieved: "+count+"/"+jsonOLD.keySet().size()+" ("+df.format((count*100/jsonOLD.keySet().size()))+"%)");
                }
            }
            logger.info("FQNs Retrieved: "+count+"/"+jsonOLD.keySet().size()+" ("+df.format((count*100/jsonOLD.keySet().size()))+"%)");
            print("FQNs Retrieved: "+count+"/"+jsonOLD.keySet().size()+" ("+df.format((count*100/jsonOLD.keySet().size()))+"%)");
        } catch (IOException | ParseException e) {
            print("Error Occurred while retriving the trained model. See the detail in the log file");
            logger.error(e.getMessage());
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
            print("Error Occurred while searching index of the project file . See the detail in the log file");
            logger.error(e.getMessage());
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }

        return returnedContext;
    }

}
