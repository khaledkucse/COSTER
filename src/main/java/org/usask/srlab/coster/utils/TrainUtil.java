package org.usask.srlab.coster.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.model.IndexEntry;
import org.usask.srlab.coster.model.OLDEntry;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrainUtil {
    private static final TrainUtil singletonTrainUtilInst = new TrainUtil();
    private static final Logger logger = LogManager.getLogger(TrainUtil.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-nnnnnnnnn");

    private TrainUtil() {
        super();
    }
    public static TrainUtil getSingletonTrainUtilInst() {
        return singletonTrainUtilInst;
    }

    private static void print(Object s){System.out.println(s.toString());}
    @SuppressWarnings("unchecked")
    public synchronized JSONObject createOrUpdateOLDEntry(JSONObject jsonOLD, String context, String fqn, boolean isNew){
        JSONObject fqnObject,tokenObject,totalFrequency;
        JSONArray contextArray;
        long total;
        if(isNew){
            fqnObject = new JSONObject();
            contextArray = new JSONArray();
            tokenObject = new JSONObject();
            total = 0;
        }
        else{
            fqnObject = (JSONObject) jsonOLD.get(fqn);
            contextArray = (JSONArray) fqnObject.get("context_list");
            tokenObject = (JSONObject) fqnObject.get("word_list");
            total = (long) fqnObject.get("total_word");
        }
        if(jsonOLD.containsKey(":global:"))
            totalFrequency = (JSONObject) jsonOLD.get(":global:");
        else
            totalFrequency = new JSONObject();

        contextArray.add(context);
        fqnObject.put("context_list",contextArray);
        String[] context_tokens = context.split(" ");
        for(String each_token:context_tokens){
            if(tokenObject.containsKey(each_token)){
                long curvalue = (long)tokenObject.get(each_token);
                curvalue++;
                total++;
                tokenObject.put(each_token,curvalue);
                curvalue  = (long) totalFrequency.get(each_token);
                curvalue++;
                totalFrequency.put(each_token,curvalue);
            }
            else if (totalFrequency.containsKey(each_token)){
                tokenObject.put(each_token,(long)1);
                total++;
                long curvalue  = (long) totalFrequency.get(each_token);
                curvalue++;
                totalFrequency.put(each_token,curvalue);
            }
            else {
                tokenObject.put(each_token,(long)1);
                total++;
                totalFrequency.put(each_token,(long)1);
            }
        }
        fqnObject.put("word_list",tokenObject);
        fqnObject.put("total_word",total);

        jsonOLD.put(fqn,fqnObject);
        jsonOLD.put(":global:",totalFrequency);
        return jsonOLD;
    }
    @SuppressWarnings("unchecked")
    private synchronized IndexEntry calculateOccuranceLikelihood(JSONObject fqnObject, String eachFQN,JSONObject totalFrequency){
        List<OLDEntry> eachFQNEntries = new ArrayList<>();
        double maxScore = 0;
        try {
            JSONArray contextIdArray = new JSONArray();
            JSONArray contextArray = (JSONArray) fqnObject.get("context_list");
            JSONObject tokenObject = (JSONObject) fqnObject.get("word_list");
            long totalWord = (long) fqnObject.get("total_word");
            for (Object aContextArray : contextArray) {
                String context = aContextArray.toString();
                String[] tokens = context.split(" ");
                String contextId = UUID.randomUUID().toString()+"-"+dtf.format(LocalDateTime.now());
                OLDEntry eachOLDEntry = new OLDEntry(contextId,context,eachFQN);
                contextIdArray.add(contextId);
                double contextScore = 0.0;
                for (String eachToken : tokens) {
                    if (tokenObject.containsKey(eachToken)) {
                        long tokenFreq = (long) tokenObject.get(eachToken);
                        long totalFreq = (long) totalFrequency.get(eachToken);
                        double fqnScore = (tokenFreq +0.0001) / (totalFreq + 0.0001);
                        double eachScore = (tokenFreq +0.0001) / (totalWord + 0.0001);
                        contextScore += Math.log(fqnScore)+Math.log(eachScore);
                    }
                }
                if(maxScore < contextScore )
                    maxScore = contextScore;

                eachOLDEntry.setScore(contextScore);
                eachFQNEntries.add(eachOLDEntry);
            }
            fqnObject.put("context_list",contextIdArray);

        } catch (Exception e) {
            e.printStackTrace();
            print("Error Occured while calculating occuracnce likelihood for the FQN "+eachFQN+". See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }
        return new IndexEntry(eachFQNEntries,maxScore);
    }

    public synchronized JSONObject indexData(JSONObject jsonOld){
        logger.info("Calculating the occurance likelihood score and indexing the data");
        logger.info("Total number of FQNs in the train data: "+jsonOld.keySet().size());
        try {
            IndexWriter writer = createWriter(Config.MODEL_PATH);
            writer.deleteAll();
            List<Document> documents;
            int count = 0;
            for(Object eachkey: jsonOld.keySet()) {
                documents = new ArrayList<>();
                String eachfqn = eachkey.toString();
                if(eachfqn.equals(":global:"))
                    continue;

                JSONObject fqnObject =(JSONObject) jsonOld.get(eachfqn);
                JSONArray contextArray = (JSONArray) fqnObject.get("context_list");
                if(contextArray.size() < Config.FQN_THRESHOLD){
                    continue;
                }
                JSONObject totalFrequency =(JSONObject) jsonOld.get(":global:");
                IndexEntry indexEntry = TrainUtil.getSingletonTrainUtilInst().calculateOccuranceLikelihood(fqnObject,eachfqn,totalFrequency);

                List<OLDEntry> eachFQNEntries = indexEntry.getEachFQNEntries();
                double maxscore = indexEntry.getMaxScore();

                for(OLDEntry eachEntry:eachFQNEntries){
                    double finalScore = normalize(eachEntry.getScore(),maxscore);
                    Document document = TrainUtil.getSingletonTrainUtilInst().createDocument(eachEntry.getContextID(),eachEntry.getContext(),eachEntry.getFqn(),finalScore+"");
                    documents.add(document);
                }
                try {
                    writer.addDocuments(documents);
                    writer.commit();
                } catch (IOException e) {
                    print("Error Occured while creating the index file for FQN: "+eachfqn +". See the detail in the log file");
                    for(StackTraceElement eachStacktrace:e.getStackTrace())
                        logger.error(eachStacktrace.toString());
                }
                count ++;
                System.gc();

                if(count%100 == 0){
                    logger.info(count+" FQNS out of "+jsonOld.keySet().size()+" are index. Percentage of completion: "+df.format((count*100/jsonOld.keySet().size()))+"%");
                    print(count+" FQNS out of "+jsonOld.keySet().size()+" are index. Percentage of completion: "+df.format((count*100/jsonOld.keySet().size()))+"%");
                }

            }
            logger.info(count+" FQNS out of "+jsonOld.keySet().size()+" are index. Percentage of completion: "+df.format((count*100/jsonOld.keySet().size()))+"%");
            print(count+" FQNS out of "+jsonOld.keySet().size()+" are index. Percentage of completion: "+df.format((count*100/jsonOld.keySet().size()))+"%");
            logger.info("Commiting the context with id, fqn and occurance likelihood score in the OLD");
            writer.close();
        } catch (IOException e) {
            print("Error Occured while indexing the trining data. See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }
        logger.info("Occurance likelihood calculation and commiting to the index is done.");
        return jsonOld;
    }


    private synchronized IndexWriter createWriter(String indexDir) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer())
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        config.setRAMBufferSizeMB(2048);
        return new IndexWriter(dir, config);
    }
    private synchronized Document createDocument(String id, String context, String fqn, String score)
    {
        Document document = new Document();
        document.add(new StringField("id", id , Field.Store.YES));
        document.add(new TextField("context", context , Field.Store.YES));
        document.add(new TextField("fqn", fqn , Field.Store.YES));
        document.add(new StringField("score", score, Field.Store.YES));
        return document;
    }

    private synchronized double normalize(double xi, double max){
        if(max == 0)
            return 1.00;
        return xi/max;
    }
}
