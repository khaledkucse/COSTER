package org.usask.srlab.coster.infer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.usask.srlab.coster.extraction.SOCodeExtraction;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.utils.FileUtil;
import org.usask.srlab.coster.utils.ParseUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportStmtComplete {
    private static final Logger logger = LogManager.getLogger(ImportStmtComplete.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits

    private static void print(Object s){System.out.println(s.toString());}

    public static void complete(String jarPath, String inputFilePath, String outputFilePath) {
        print("Collecting Jar files...");
        logger.info("Collecting Jar Files...");
        String[] jarPaths = ParseUtil.collectJarFiles(new File(jarPath));
        print("Collecting code snippet...");
        logger.info("Collecting code snippet...");
        ArrayList<String> snippetPath = ParseUtil.collectSnippets(new File(inputFilePath));
        String[] sourcefilePath = new String[snippetPath.size()];
        sourcefilePath = snippetPath.toArray(sourcefilePath);
        logger.info("Total Number of Code Snippet: " + sourcefilePath.length);

        print("Extracting data from the code snippets...");
        logger.info("Extracting data from the code snippets...");
        List<APIElement> testCases = collectSODataset(sourcefilePath, jarPaths, inputFilePath);
        List<String> returnedFQNs = new ArrayList<>();
        for(APIElement eachCase:testCases)
            if(!returnedFQNs.contains(eachCase.getActualFQN()))
                returnedFQNs.add(eachCase.getActualFQN());
        FileUtil.getSingleTonFileUtilInst().writeToFile(outputFilePath,returnedFQNs);
    }

    private static List<APIElement> collectSODataset(String[] sourcefilePaths, String[] jarPaths, String inputFilePath){
        File projectPath = new File(inputFilePath);
        List<APIElement> testcases = new ArrayList<>();
        try {
            List<APIElement> apiElements = SOCodeExtraction.extractFromSOPOST(projectPath,sourcefilePaths,jarPaths);
            if(apiElements.size()>0)
                testcases.addAll(apiElements);
        } catch (Exception e) {
            print("Error Occurred while collecting dataset. See the detail in the log file");
            logger.error(e.getMessage());
            for(StackTraceElement eachStacktrace:e.getStackTrace())
                logger.error(eachStacktrace.toString());

        }

        return testcases;

    }
}
