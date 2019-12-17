package org.usask.srlab.coster.train;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.json.simple.JSONObject;


import org.usask.srlab.coster.extraction.CompilableCodeExtraction;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.utils.*;


public class Train {
    private static final Logger logger = LogManager.getLogger(Train.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static final int THREAD_POOL_SIZE = 8;

    private static final Callable<Boolean> blockingTimeoutCallback = () -> {
        return true; // keep waiting
    };
    private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);

    private static void print(Object s){System.out.println(s.toString());}

    public static void createOld(String jarRepoPath, String repositoryPath,String datasetPath, String modelPath, int fqnThreshold, boolean isExtraction) {

        if(isExtraction){
            print("Collecting Jar files and Github projects");
            String[] jarPaths = ParseUtil.collectGithubJars(new File(jarRepoPath));
            String[] projectPaths = ParseUtil.collectGithubProjects(new File(repositoryPath));

            print("Collecting data set from the Github Dataset");
            collectDataset(projectPaths,jarPaths,datasetPath);
            try {
                pool.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        print("Populating the dataset in the OLD");
        JSONObject jsonObject = new JSONObject();
        jsonObject = populateDatainOLD(new File(datasetPath), jsonObject);

        print("Calculating the occurance likelihood score and storing the context along with FQN and occurance likelihood score in the index file");
        jsonObject = TrainUtil.getSingletonTrainUtilInst().indexData(jsonObject, modelPath, fqnThreshold);

        logger.info("Writting the OLD at "+ modelPath+"OLD.json");
        print("Writting the OLD at "+modelPath+"OLD.json");
        try {
            Files.write(Paths.get(modelPath+"OLD.json"), jsonObject.toJSONString().getBytes());
        } catch (IOException e) {
            print("Error Occured while wrtting the OLD. See the detail in the log file");
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());
        }

        logger.info("Training is done!!!");
        print("Training is Done!!!");

    }


    static NotifyingBlockingThreadPoolExecutor collectDataset(String[] projectPaths, String[] jarPaths, String datasetPath){
        int count  = 0;

        for(String eachProjectPath:projectPaths){
            pool.execute(() -> {
                try {
                    logger.info("Working on the project "+eachProjectPath);
                    File eachProject = new File(eachProjectPath.replace(".zip",""));
                    UnzipUtil.unzip(eachProjectPath,eachProject.getAbsolutePath());
                    List<APIElement> apiElements = CompilableCodeExtraction.extractfromSource(eachProject,jarPaths);
                    if(apiElements.size()>0){
                        FileUtil.getSingleTonFileUtilInst().writeCOSTERProjectData(datasetPath+eachProject.getName()+".csv",apiElements);
                    }

                    FileUtils.deleteDirectory(eachProjectPath.replace(".zip",""));
                } catch (Exception e) {
                    print("Error Occured while unzipping the project file "+eachProjectPath+". See the detail in the log file");
                    for(StackTraceElement eachStacktrace:e.getStackTrace())
                        logger.error(eachStacktrace.toString());

                }
            });

            count ++;

            if(count%500 == 0){
                logger.info(count+" subject systems out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
                print(count+" subject systems out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
            }
        }

        logger.info(count+" subject systems out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
        print(count+" subject systems out of "+projectPaths.length+" are parsed. Percentage of completion: "+df.format((count*100/projectPaths.length))+"%");
        return pool;
    }

    static JSONObject populateDatainOLD(File datasetDir, JSONObject jsonOLD){
        logger.info("Populating the data from the CSV file to the OLD");
        File[] projectFiles = datasetDir.listFiles();
        if (projectFiles != null) {
            logger.info("Total number of projects: " + projectFiles.length);
            int count = 0;
            for (File eachProjectFile : projectFiles) {
                ArrayList<Pair<String, String>> filecontent = FileUtil.getSingleTonFileUtilInst().readCSVFiles(eachProjectFile);
                for (Pair<String, String> eachData : filecontent) {
                    String context = eachData.getKey();
                    String fqn = eachData.getValue();
                    if (jsonOLD.containsKey(fqn)) {
                        jsonOLD = TrainUtil.getSingletonTrainUtilInst().createOrUpdateOLDEntry(jsonOLD, context, fqn, false);
                    } else {
                        jsonOLD = TrainUtil.getSingletonTrainUtilInst().createOrUpdateOLDEntry(jsonOLD, context, fqn, true);
                    }
                }

                count++;

                if (count % 500 == 0) {
                    logger.info(count + " subject systems out of " + projectFiles.length + " are populated in the OLD. Percentage of completion: " + df.format((count * 100 / projectFiles.length)) + "%");
                    print(count + " subject systems out of " + projectFiles.length + " are populated in the OLD. Percentage of completion: " + df.format((count * 100 / projectFiles.length)) + "%");
                }
            }
            logger.info(count + " subject systems out of " + projectFiles.length + " are populated in the OLD. Percentage of completion: " + df.format((count * 100 / projectFiles.length)) + "%");
            print(count + " subject systems out of " + projectFiles.length + " are populated in the OLD. Percentage of completion: " + df.format((count * 100 / projectFiles.length)) + "%");
        }
        return jsonOLD;
    }
}
