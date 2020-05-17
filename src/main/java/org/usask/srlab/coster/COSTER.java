package org.usask.srlab.coster;

import org.apache.commons.cli.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.infer.ExtrinsicInference;
import org.usask.srlab.coster.infer.FileInference;
import org.usask.srlab.coster.infer.ImportStmtComplete;
import org.usask.srlab.coster.infer.IntrinsticInference;
import org.usask.srlab.coster.train.Train;
import org.usask.srlab.coster.train.RetrainOLD;

import java.util.Properties;

public class COSTER {
    private static final Logger logger = LogManager.getLogger(COSTER.class.getName()); // logger variable for loggin in the file
    private static void print(Object s){System.out.println(s.toString());}
    private static String jarRepoPath, repositoryPath,datasetPath,modelPath, contextSimilarity, nameSimilarity;
    private static int fqnThreshold, topk;
    private static boolean isExtraction;


    private static Options options;
    private static HelpFormatter formatter;

    private static void panic(int exitval) {
        formatter.printHelp(200, "java -jar COSTER.jar", "COSTER-HELP", options, "", true);
        System.exit(exitval);
    }

    public static boolean getIsExtraction() {
        return isExtraction;
    }

    public static void setIsExtraction(boolean isExtraction) {
        COSTER.isExtraction = isExtraction;
    }

    public static String getJarRepoPath() {
        return jarRepoPath;
    }

    public static void setJarRepoPath(String jarRepoPath) {
        COSTER.jarRepoPath = jarRepoPath;
    }

    public static String getRepositoryPath() {
        return repositoryPath;
    }

    public static void setRepositoryPath(String repositoryPath) {
        COSTER.repositoryPath = repositoryPath;
    }

    public static String getDatasetPath() {
        return datasetPath;
    }

    public static void setDatasetPath(String datasetPath) {
        COSTER.datasetPath = datasetPath;
    }

    public static String getModelPath() {
        return modelPath;
    }

    public static void setModelPath(String modelPath) {
        COSTER.modelPath = modelPath;
    }

    public static String getContextSimilarity() {
        return contextSimilarity;
    }

    public static void setContextSimilarity(String contextSimilarity) {
        COSTER.contextSimilarity = contextSimilarity;
    }

    public static String getNameSimilarity() {
        return nameSimilarity;
    }

    public static void setNameSimilarity(String nameSimilarity) {
        COSTER.nameSimilarity = nameSimilarity;
    }

    public static int getFqnThreshold() {
        return fqnThreshold;
    }

    public static void setFqnThreshold(int fqnThreshold) {
        COSTER.fqnThreshold = fqnThreshold;
    }

    public static int getTopk() {
        return topk;
    }

    public static void setTopk(int topk) {
        COSTER.topk = topk;
    }

    private static void init(){
        jarRepoPath = Config.GITHUB_JAR_PATH;
        repositoryPath = Config.GITHUB_SUBJECT_SYSTEM_PATH;
        datasetPath = Config.GITHUB_DATSET_PATH;
        modelPath = Config.MODEL_PATH;
        topk = Config.DEFAULT_TOP;
        fqnThreshold = Config.FQN_THRESHOLD;
        isExtraction = true;
        contextSimilarity = "cosine";
        nameSimilarity = "levenshtein";
    }



    public static void main(String[] args) {
        PropertyConfigurator.configure(COSTER.class.getClass().getResourceAsStream("/log4j.properties"));
        final Properties properties = new Properties();
        try {
            properties.load(COSTER.class.getClass().getResourceAsStream("/coster.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        welcomeMessage(properties);
        COSTER.init();

        options = new Options();


        options.addOption(Option.builder("c")
                .longOpt("contsim")
                .hasArg()
                .argName("Context Similarity Function [Optional]")
                .desc("Similarity functions used for context similarity. " +
                        "User can choose one from: cosine (default), jaccard, lcs")
                .build()
        );
        options.addOption(Option.builder("d")
                .longOpt("dataset")
                .hasArg()
                .argName("Dataset Path [Optional]")
                .desc("Path of the Intermediate dataset created by COSTER." +
                        " Default location is in data/ directory.")
                .build()
        );
        options.addOption(Option.builder("e")
                .longOpt("evaluationType")
                .hasArg()
                .argName("Evaluation Types")
                .desc("Types of evaluation. Option: intrinsic, extrinsic")
                .build()
        );
        options.addOption(Option.builder("f")
                .longOpt("feature")
                .hasArg()
                .argName("Feature")
                .desc("Types of feature. Option: train, retrain, infer, eval")
                .build()
        );
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Prints this usage information.")
                .build()
        );
        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("Input File")
                .desc("Location of Input File")
                .build()
        );
        options.addOption(Option.builder("j")
                .longOpt("jarPath")
                .hasArg()
                .argName("Jar Path [Optional]")
                .desc("Path of the directory where jar files are stored. Default location is in data/jars/ directory")
                .build()
        );
        options.addOption(Option.builder("k")
                .longOpt("extracton")
                .hasArg()
                .argName("Is Extraction Needed [Optional]")
                .desc("A boolean value dictates whether extraction of code needed or not for training and retraining. Default 1. Option: 1 (true), 0(false)")
                .build()
        );
        options.addOption(Option.builder("m")
                .longOpt("modelPath")
                .hasArg()
                .argName("Model Path [Optional]")
                .desc("Directory path where trained models are stored." +
                        "Default Localtion is model/ directory")
                .build()
        );
        options.addOption(Option.builder("n")
                .longOpt("namesim")
                .hasArg()
                .argName("Name Similarity Function [Optional]")
                .desc("Similarity function used for name similarity." +
                        "User can choose one from: levenshtein (default), hamming, lcs")
                .build()
        );
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("Output File")
                .desc("Location of Output File")
                .build()
        );
        options.addOption(Option.builder("q")
                .longOpt("fqnThreshold")
                .hasArg()
                .argName("FQN Threshold[optional]")
                .desc("Threshold value to select the minimum number of context required for a FQN to train." +
                        "Default: 50")
                .build()
        );
        options.addOption(Option.builder("r")
                .longOpt("repositoryPath")
                .hasArg()
                .argName("Repository Path [Optional]")
                .desc("The path of the repository where the subject systems for are stored." +
                        "Default location is in the data/ directory.")
                .build()
        );

        options.addOption(Option.builder("t")
                .longOpt("top")
                .hasArg()
                .argName("Top-K [Optional]")
                .desc("Number of suggestions the tool generates. default: 1")
                .build()
        );

        formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch(Exception e) {
            panic(-1);
            return;
        }

        if(line.hasOption("h")) {
            panic(0);
        }
        else if(line.hasOption("f")){
            String feature = line.getOptionValue("f");
            switch (feature) {
                case "train":
                    try{
                        if (line.hasOption("r"))
                            COSTER.setRepositoryPath(line.getOptionValue("r"));
//                        else {
//                            print("No path of repository for training is provided.");
//                            print("Selecting the deafult path of Repository for training:" + repositoryPath);
//                        }
                        if (line.hasOption("m"))
                            COSTER.setModelPath(line.getOptionValue("m"));
//                        else {
//                            print("No path for stroing trained model is provided.");
//                            print("Selecting the deafult path for storing trained model: " + modelPath);
//                        }
                        if (line.hasOption("d"))
                            COSTER.setDatasetPath(line.getOptionValue("d"));
//                        else {
//                            print("No path for stroing intermidiate dataset for training is provided.");
//                            print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
//                        }
                        if (line.hasOption("j"))
                            COSTER.setJarRepoPath(line.getOptionValue("j"));
//                        else {
//                            print("No path of jar files for training is provided.");
//                            print("Selecting the deafult path of jar files for training: " + jarRepoPath);
//                        }
                        if (line.hasOption("q"))
                            COSTER.setFqnThreshold(Integer.parseInt(line.getOptionValue("q")));
//                        else {
//                            print("No threshold for context size of each FQN is selected.");
//                            print("Selecting the deafult threshold value: " + fqnThreshold);
//                        }
                        if (line.hasOption("k")) {
                            int temp = Integer.parseInt(line.getOptionValue("k"));
                            if(temp == 0)
                                COSTER.setIsExtraction(false);
                        }


                        Train.createOld();
                    }catch (Exception ex){
                        print("Exception occurred while taking input.\n\n");
                    }

                    break;
                case "retrain":
                    try{
                        if (line.hasOption("r"))
                            COSTER.setRepositoryPath(line.getOptionValue("r"));
//                        else {
//                            print("No path of repository for training is provided.");
//                            print("Selecting the deafult path of Repository for training:" + repositoryPath);
//                        }
                        if (line.hasOption("m"))
                            COSTER.setModelPath(line.getOptionValue("m"));
//                        else {
//                            print("No path for stroing trained model is provided.");
//                            print("Selecting the deafult path for storing trained model: " + modelPath);
//                        }
                        if (line.hasOption("d"))
                            COSTER.setDatasetPath(line.getOptionValue("d"));
//                        else {
//                            print("No path for stroing intermidiate dataset for training is provided.");
//                            print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
//                        }
                        if (line.hasOption("j"))
                            COSTER.setJarRepoPath(line.getOptionValue("j"));
//                        else {
//                            print("No path of jar files for training is provided.");
//                            print("Selecting the deafult path of jar files for training: " + jarRepoPath);
//                        }
                        if (line.hasOption("q"))
                            COSTER.setFqnThreshold(Integer.parseInt(line.getOptionValue("q")));
//                        else {
//                            print("No threshold for context size of each FQN is selected.");
//                            print("Selecting the deafult threshold value: " + fqnThreshold);
//                        }
                        if (line.hasOption("k")) {
                            int temp = Integer.parseInt(line.getOptionValue("k"));
                            if(temp == 0)
                                COSTER.setIsExtraction(false);
                        }

                        RetrainOLD.retrain();
                    }catch (Exception ex){
                        print("Exception occurred while taking input.\n\n");
                    }

                    break;
                case "infer":
                    if (line.hasOption("i") && line.hasOption("o")) {
                        String inputFilePath = line.getOptionValue("i");
                        String outputFilePath = line.getOptionValue("o");
                        COSTER.setJarRepoPath(Config.SO_JAR_PATH);
                        try {
                            if (line.hasOption("t"))
                                COSTER.setTopk(Integer.parseInt(line.getOptionValue("t")));
//                            else{
//                                print("No value as Top-K is selected.");
//                                print("Selecting the deafult number of reccomendation: " + topk);
//                            }
                            if (line.hasOption("c")) {
                                String tempContext = line.getOptionValue("c");
                                if (tempContext.equals("jaccard"))
                                    COSTER.setContextSimilarity(tempContext);
                                else if (tempContext.equals("lcs"))
                                    COSTER.setContextSimilarity(tempContext);
                            }
//                            else{
//                                print("No metric is slected for context similairty method.");
//                                print("Selecting the deafult context similairty method Cosine");
//                            }
                            if (line.hasOption("n")) {
                                String tempName = line.getOptionValue("n");
                                if (tempName.equals("hamming"))
                                    COSTER.setNameSimilarity(tempName);
                                else if (tempName.equals("lcs"))
                                    COSTER.setNameSimilarity(tempName);
                            }
//                            else{
//                                print("No metric is slected for name similairty method.");
//                                print("Selecting the deafult name similairty method Levenshtein distance");
//                            }
                            if (line.hasOption("j"))
                                COSTER.setJarRepoPath(line.getOptionValue("j"));
//                            else {
//                                print("No path of jar files for training is provided.");
//                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
//                            }
                            if (line.hasOption("m"))
                                COSTER.setModelPath(line.getOptionValue("m"));
//                            else {
//                                print("No path for trained trained model is provided.");
//                                print("Selecting the deafult path for storing trained model: " + modelPath);
//                            }

                            FileInference.infer(jarRepoPath,inputFilePath, outputFilePath, modelPath, topk, contextSimilarity, nameSimilarity);
                        } catch (Exception ignored) {
                        }

                    } else {
                        print("Please choose the location of input and output file\n\n");
                        panic(0);
                    }
                    break;
                case "eval":
                    if (line.hasOption("e")) {
                        String evalType = line.getOptionValue("e");
//                        String contextSimilarity = "cosine", nameSimilarity = "levenshtein";
//                        modelPath = Config.MODEL_PATH;
                        try{
                            if (line.hasOption("t"))
                                COSTER.setTopk(Integer.parseInt(line.getOptionValue("t")));
//                            else{
//                                print("No value as Top-K is selected.");
//                                print("Selecting the deafult number of reccomendation: " + topk);
//                            }
                            if (line.hasOption("m"))
                                COSTER.setModelPath(line.getOptionValue("m"));
//                            else {
//                                print("No path for trained trained model is provided.");
//                                print("Selecting the deafult path for storing trained model: " + modelPath);
//                            }
                            if (line.hasOption("c")) {
                                String tempContext = line.getOptionValue("c");
                                if (tempContext.equals("jaccard"))
                                    COSTER.setContextSimilarity(tempContext);
                                else if (tempContext.equals("lcs"))
                                    COSTER.setContextSimilarity(tempContext);
                            }
//                            else{
//                                print("No metric is slected for context similairty method.");
//                                print("Selecting the deafult context similairty method Cosine");
//                            }
                            if (line.hasOption("n")) {
                                String tempName = line.getOptionValue("n");
                                if (tempName.equals("hamming"))
                                    COSTER.setNameSimilarity(tempName);
                                else if (tempName.equals("lcs"))
                                    COSTER.setNameSimilarity(tempName);
                            }
//                            else{
//                                print("No metric is slected for name similairty method.");
//                                print("Selecting the deafult name similairty method Levenshtein distance");
//                            }
                        }catch (Exception ignored){}
                        if(evalType.equals("intrinsic")){
                            COSTER.setJarRepoPath(Config.GITHUB_JAR_PATH);
                            COSTER.setRepositoryPath(Config.TEST_SUBJECT_SYSTEM_PATH);
                            COSTER.setDatasetPath(Config.TEST_DATSET_PATH);
                            if (line.hasOption("r"))
                                COSTER.setRepositoryPath(line.getOptionValue("r"));
//                            else {
//                                print("No path of repository for training is provided.");
//                                print("Selecting the deafult path of Repository for training:" + repositoryPath);
//                            }
                            if (line.hasOption("j"))
                                COSTER.setJarRepoPath(line.getOptionValue("j"));
//                            else {
//                                print("No path of jar files for training is provided.");
//                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
//                            }
                            if (line.hasOption("d"))
                                COSTER.setDatasetPath(line.getOptionValue("d"));
//                            else {
//                                print("No path for intermidiate dataset for training is provided.");
//                                print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
//                            }

                            IntrinsticInference.evaluation(jarRepoPath,repositoryPath,datasetPath,modelPath,topk,contextSimilarity,nameSimilarity);

                        }else if(evalType.equals("extrinsic")) {
                            COSTER.setJarRepoPath(Config.SO_JAR_PATH);
                            COSTER.setRepositoryPath(Config.SO_CODE_SNIPPET_PATH);
                            COSTER.setDatasetPath(Config.SO_DATASET_PATH);
                            if (line.hasOption("r"))
                                COSTER.setRepositoryPath(line.getOptionValue("r"));
//                            else {
//                                print("No path of repository for training is provided.");
//                                print("Selecting the deafult path of Repository for training:" + repositoryPath);
//                            }
                            if (line.hasOption("j"))
                                COSTER.setJarRepoPath(line.getOptionValue("j"));
//                            else {
//                                print("No path of jar files for training is provided.");
//                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
//                            }
                            if (line.hasOption("d"))
                                COSTER.setDatasetPath(line.getOptionValue("d"));
//                            else {
//                                print("No path for intermidiate dataset for training is provided.");
//                                print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
//                            }

                            ExtrinsicInference.evaluation();
                        }

                    }else {
                        print("Please choose at least one type of evaluations: intrinsic, extrinsic\n\n");
                        panic(0);
                    }
                    break;
                case "complete":
                    if (line.hasOption("i") && line.hasOption("o")) {
                        String inputFilePath = line.getOptionValue("i");
                        String outputFilePath = line.getOptionValue("o");
                        COSTER.setJarRepoPath(Config.SO_JAR_PATH);
                        try {
                            if (line.hasOption("t"))
                                COSTER.setTopk(Integer.parseInt(line.getOptionValue("t")));
//                            else{
//                                print("No value as Top-K is selected.");
//                                print("Selecting the deafult number of reccomendation: " + topk);
//                            }
                            if (line.hasOption("c")) {
                                String tempContext = line.getOptionValue("c");
                                if (tempContext.equals("jaccard"))
                                    COSTER.setContextSimilarity(tempContext);
                                else if (tempContext.equals("lcs"))
                                    COSTER.setContextSimilarity(tempContext);
                            }
//                            else{
//                                print("No metric is slected for context similairty method.");
//                                print("Selecting the deafult context similairty method Cosine");
//                            }
                            if (line.hasOption("n")) {
                                String tempName = line.getOptionValue("n");
                                if (tempName.equals("hamming"))
                                    COSTER.setNameSimilarity(tempName);
                                else if (tempName.equals("lcs"))
                                    COSTER.setNameSimilarity(tempName);
                            }
//                            else{
//                                print("No metric is slected for name similairty method.");
//                                print("Selecting the deafult name similairty method Levenshtein distance");
//                            }
                            if (line.hasOption("j"))
                                COSTER.setJarRepoPath(line.getOptionValue("j"));
//                            else {
//                                print("No path of jar files for training is provided.");
//                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
//                            }
                            ImportStmtComplete.complete(jarRepoPath,inputFilePath,outputFilePath);
                        }
                        catch (Exception Ignored){}
                    } else {
                        print("Please choose the location of input and output file\n\n");
                        panic(0);
                    }
                    break;
                default:
                    print("Please choose the right feature\n\n");
                    panic(0);
                    break;
            }

        }else {
            print("You need to select at least one of the following features: train, retrain, infer, eval\n\n");
            panic(0);
        }

    }



    private static void welcomeMessage(Properties properties){
        print("COSTER: Context Sensitive Type Solver");
        print("Version: "+properties.getProperty("version"));
        print("Source Code can be found at https://github.com/khaledkucse/coster");
        print("The work is accepted at 34th IEEE/ACM International Conference on Automated Software Engineering (ASE 2019)");
        print("Full paper is available at http://bit.ly/2YuuZrW\n");
        logger.info("COSTER: Context Sensitive Type Solver");
        logger.info("Version: "+properties.getProperty("version")+"\n");
    }
}
