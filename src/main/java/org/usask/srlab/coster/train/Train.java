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
            print("Collecting Jar files...");
            String[] jarPaths = ParseUtil.collectJarFiles(new File(jarRepoPath));
            String[] projectPaths = ParseUtil.collectGithubProjects(new File(repositoryPath));

            print("Extracting subject systems from the repository...");
            collectDataset(projectPaths,jarPaths,datasetPath);
            try {
                pool.await();
            } catch (InterruptedException e) {
                print("Error Occurred while extracting subject systems from the repository. See the detail in the log file");
                logger.error(e.getMessage());
                for(StackTraceElement eachStacktrace:e.getStackTrace())
                    logger.error(eachStacktrace.toString());
            }
        }

        print("Populating the data from the subject systems in the model...");
        JSONObject jsonObject = new JSONObject();
        jsonObject = populateDatainOLD(new File(datasetPath), jsonObject);

        print("Calculating the occurrence likelihood score...");
        jsonObject = TrainUtil.getSingletonTrainUtilInst().indexData(jsonObject, modelPath, fqnThreshold);

        logger.info("Storing the model at "+ modelPath+"...");
        print("Storing the model at "+modelPath+"...");
        try {
            Files.write(Paths.get(modelPath+"OLD.json"), jsonObject.toJSONString().getBytes());
        } catch (IOException e) {
            print("Error Occurred while storing the model. See the detail in the log file");
            logger.error(e.getMessage());
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
                    logger.info("Working on the project "+eachProjectPath+"...");
                    File eachProject = new File(eachProjectPath.replace(".zip",""));
                    UnzipUtil.unzip(eachProjectPath,eachProject.getAbsolutePath());
                    List<APIElement> apiElements = CompilableCodeExtraction.extractfromSource(eachProject,jarPaths);
                    if(apiElements.size()>0){
                        FileUtil.getSingleTonFileUtilInst().writeCOSTERProjectData(datasetPath+eachProject.getName()+".csv",apiElements);
                    }

                    FileUtils.deleteDirectory(eachProjectPath.replace(".zip",""));
                } catch (Exception e) {
                    print("Error Occurred while unzipping the project file "+eachProjectPath+". See the detail in the log file");
                    logger.error(e.getMessage());
                    for(StackTraceElement eachStacktrace:e.getStackTrace())
                        logger.error(eachStacktrace.toString());

                }
            });

            count ++;

            if(count%500 == 0){
                logger.info("Subject Systems Extracted: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
                print("Subject Systems Extracted: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
            }
        }

        logger.info("Subject Systems Extracted: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
        print("Subject Systems Extracted: "+count+"/"+projectPaths.length+" ("+df.format((count*100/projectPaths.length))+"%)");
        return pool;
    }

    static JSONObject populateDatainOLD(File datasetDir, JSONObject jsonOLD){
        logger.info("Populating the data to the model...");
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
                    logger.info("Data Populated in the model: "+count + "/" + projectFiles.length + " (" + df.format((count * 100 / projectFiles.length)) + "%)");
                    print("Data Populated in the model: "+count + "/" + projectFiles.length + " (" + df.format((count * 100 / projectFiles.length)) + "%)");
                }
            }
            logger.info("Data Populated in the model: "+count + "/" + projectFiles.length + " (" + df.format((count * 100 / projectFiles.length)) + "%)");
            print("Data Populated in the model: "+count + "/" + projectFiles.length + " (" + df.format((count * 100 / projectFiles.length)) + "%)");
        }
        return jsonOLD;
    }
}
