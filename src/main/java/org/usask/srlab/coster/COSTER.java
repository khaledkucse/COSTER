package org.usask.srlab.coster;

import org.apache.commons.cli.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.infer.ExtrinsicInference;
import org.usask.srlab.coster.infer.FileInference;
import org.usask.srlab.coster.infer.IntrinsticInference;
import org.usask.srlab.coster.train.Train;
import org.usask.srlab.coster.train.RetrainOLD;

import java.util.Properties;

public class COSTER {
    private static final Logger logger = LogManager.getLogger(COSTER.class.getName()); // logger variable for loggin in the file
    private static void print(Object s){System.out.println(s.toString());}


    private static Options options;
    private static HelpFormatter formatter;

    private static void panic(int exitval) {
        formatter.printHelp(200, "java -jar COSTER.jar", "COSTER-HELP", options, "", true);
        System.exit(exitval);
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
                .desc("Path of the Intermediate dataset created by COSTER during training, retraining and evaluation." +
                        " Default location is in /data directory.")
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
                .longOpt("functionality")
                .hasArg()
                .argName("Functionality")
                .desc("Types of functionalities the tool will run for. Option: train, retrain, infer, eval")
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
                .desc("Path of the directory where jar files are stored. Used by all functionality. Default location is in /data directory")
                .build()
        );
        options.addOption(Option.builder("m")
                .longOpt("modelPath")
                .hasArg()
                .argName("Model Path [Optional]")
                .desc("Directory path where trained Occurance Likelihood Dictonary and index files are stored after training and retraining." +
                        "This path is also required during inference and evaluation." +
                        "Default Localtion is is/model directory")
                .build()
        );
        options.addOption(Option.builder("n")
                .longOpt("namesim")
                .hasArg()
                .argName("Name Similarity Function [Optional]")
                .desc("Similairty function used for name similarity." +
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
                .desc("Threshold value to select the number of context used to filter FQNs while Creating OLD." +
                        "Default: 50")
                .build()
        );
        options.addOption(Option.builder("r")
                .longOpt("repositoryPath")
                .hasArg()
                .argName("Repository Path [Optional]")
                .desc("The path of the repository where the subject system for training, retraining and evaluation are stored." +
                        "Default location is in the /data directory.")
                .build()
        );

        options.addOption(Option.builder("t")
                .longOpt("top")
                .hasArg()
                .argName("Top-K [Optional]")
                .desc("Number of suggestion the tool generates duting inference and evaluation. default: 1")
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
            String functionality = line.getOptionValue("f");
            switch (functionality) {
                case "train":
                    String jarRepoPath = Config.GITHUB_JAR_PATH;
                    String repositoryPath = Config.GITHUB_SUBJECT_SYSTEM_PATH;
                    String datasetPath = Config.GITHUB_DATSET_PATH;
                    String modelPath = Config.MODEL_PATH;
                    int fqnThreshold = Config.FQN_THRESHOLD;
                    try{
                        if (line.hasOption("r"))
                            repositoryPath = line.getOptionValue("r");
                        else {
                            print("No path of repository for training is provided.");
                            print("Selecting the deafult path of Repository for training:" + repositoryPath);
                        }
                        if (line.hasOption("m"))
                            modelPath = line.getOptionValue("m");
                        else {
                            print("No path for stroing trained model is provided.");
                            print("Selecting the deafult path for storing trained model: " + modelPath);
                        }
                        if (line.hasOption("d"))
                            datasetPath = line.getOptionValue("d");
                        else {
                            print("No path for stroing intermidiate dataset for training is provided.");
                            print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
                        }
                        if (line.hasOption("j"))
                            jarRepoPath = line.getOptionValue("j");
                        else {
                            print("No path of jar files for training is provided.");
                            print("Selecting the deafult path of jar files for training: " + jarRepoPath);
                        }
                        if (line.hasOption("q"))
                            fqnThreshold = Integer.parseInt(line.getOptionValue("q"));
                        else {
                            print("No threshold for context size of each FQN is selected.");
                            print("Selecting the deafult threshold value: " + fqnThreshold);
                        }


                        Train.createOld(jarRepoPath, repositoryPath, datasetPath, modelPath,fqnThreshold);
                    }catch (Exception ex){
                        print("Exception occured while Taking input.\n\n");
                    }

                    break;
                case "retrain":
                    jarRepoPath = Config.GITHUB_JAR_PATH;
                    repositoryPath = Config.GITHUB_SUBJECT_SYSTEM_PATH;
                    datasetPath = Config.GITHUB_DATSET_PATH;
                    modelPath = Config.MODEL_PATH;
                    fqnThreshold = Config.FQN_THRESHOLD;
                    try{
                        if (line.hasOption("r"))
                            repositoryPath = line.getOptionValue("r");
                        else {
                            print("No path of repository for training is provided.");
                            print("Selecting the deafult path of Repository for training:" + repositoryPath);
                        }
                        if (line.hasOption("m"))
                            modelPath = line.getOptionValue("m");
                        else {
                            print("No path for stroing trained model is provided.");
                            print("Selecting the deafult path for storing trained model: " + modelPath);
                        }
                        if (line.hasOption("d"))
                            datasetPath = line.getOptionValue("d");
                        else {
                            print("No path for stroing intermidiate dataset for training is provided.");
                            print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
                        }
                        if (line.hasOption("j"))
                            jarRepoPath = line.getOptionValue("j");
                        else {
                            print("No path of jar files for training is provided.");
                            print("Selecting the deafult path of jar files for training: " + jarRepoPath);
                        }
                        if (line.hasOption("q"))
                            fqnThreshold = Integer.parseInt(line.getOptionValue("q"));
                        else {
                            print("No threshold for context size of each FQN is selected.");
                            print("Selecting the deafult threshold value: " + fqnThreshold);
                        }

                        RetrainOLD.retrain(jarRepoPath,repositoryPath,datasetPath,modelPath,fqnThreshold);
                    }catch (Exception ex){
                        print("Exception occured while taking input.\n\n");
                    }

                    break;
                case "infer":
                    if (line.hasOption("i") && line.hasOption("o")) {
                        String inputFilePath = line.getOptionValue("i");
                        String outputFilePath = line.getOptionValue("o");
                        int topk = 1;
                        String contextSimilarity = "cosine", nameSimilarity = "levenshtein";
                        jarRepoPath = Config.SO_JAR_PATH;
                        modelPath = Config.MODEL_PATH;
                        try {
                            if (line.hasOption("t"))
                                topk = Integer.parseInt(line.getOptionValue("t"));
                            else{
                                print("No value as Top-K is selected.");
                                print("Selecting the deafult number of reccomendation: " + topk);
                            }
                            if (line.hasOption("c")) {
                                String tempContext = line.getOptionValue("c");
                                if (tempContext.equals("jaccard"))
                                    contextSimilarity = tempContext;
                                else if (tempContext.equals("lcs"))
                                    contextSimilarity = tempContext;
                            } else{
                                print("No metric is slected for context similairty method.");
                                print("Selecting the deafult context similairty method Cosine");
                            }
                            if (line.hasOption("n")) {
                                String tempName = line.getOptionValue("n");
                                if (tempName.equals("hamming"))
                                    nameSimilarity = tempName;
                                else if (tempName.equals("lcs"))
                                    nameSimilarity = tempName;
                            } else{
                                print("No metric is slected for name similairty method.");
                                print("Selecting the deafult name similairty method Levenshtein distance");
                            }
                            if (line.hasOption("j"))
                                jarRepoPath = line.getOptionValue("j");
                            else {
                                print("No path of jar files for training is provided.");
                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
                            }
                            if (line.hasOption("m"))
                                modelPath = line.getOptionValue("m");
                            else {
                                print("No path for trained trained model is provided.");
                                print("Selecting the deafult path for storing trained model: " + modelPath);
                            }

                            FileInference.infer(jarRepoPath,inputFilePath, outputFilePath, modelPath, topk, contextSimilarity, nameSimilarity);
                        } catch (Exception ignored) {
                        }

                    } else {
                        print("Please choose the localtion of input and output file\n\n");
                        panic(0);
                    }
                    break;
                case "eval":
                    if (line.hasOption("e")) {
                        String evalType = line.getOptionValue("e");
                        int topk = 1;
                        String contextSimilarity = "cosine", nameSimilarity = "levenshtein";
                        modelPath = Config.MODEL_PATH;
                        try{
                            if (line.hasOption("t"))
                                topk = Integer.parseInt(line.getOptionValue("t"));
                            else{
                                print("No value as Top-K is selected.");
                                print("Selecting the deafult number of reccomendation: " + topk);
                            }
                            if (line.hasOption("m"))
                                modelPath = line.getOptionValue("m");
                            else {
                                print("No path for trained trained model is provided.");
                                print("Selecting the deafult path for storing trained model: " + modelPath);
                            }
                            if (line.hasOption("c")) {
                                String tempContext = line.getOptionValue("c");
                                if (tempContext.equals("jaccard"))
                                    contextSimilarity = tempContext;
                                else if (tempContext.equals("lcs"))
                                    contextSimilarity = tempContext;
                            } else{
                                print("No metric is slected for context similairty method.");
                                print("Selecting the deafult context similairty method Cosine");
                            }
                            if (line.hasOption("n")) {
                                String tempName = line.getOptionValue("n");
                                if (tempName.equals("hamming"))
                                    nameSimilarity = tempName;
                                else if (tempName.equals("lcs"))
                                    nameSimilarity = tempName;
                            } else{
                                print("No metric is slected for name similairty method.");
                                print("Selecting the deafult name similairty method Levenshtein distance");
                            }
                        }catch (Exception ignored){}
                        if(evalType.equals("intrinsic")){
                            jarRepoPath = Config.GITHUB_JAR_PATH;
                            repositoryPath = Config.TEST_SUBJECT_SYSTEM_PATH;
                            datasetPath = Config.TEST_DATSET_PATH;
                            if (line.hasOption("r"))
                                repositoryPath = line.getOptionValue("r");
                            else {
                                print("No path of repository for training is provided.");
                                print("Selecting the deafult path of Repository for training:" + repositoryPath);
                            }
                            if (line.hasOption("j"))
                                jarRepoPath = line.getOptionValue("j");
                            else {
                                print("No path of jar files for training is provided.");
                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
                            }
                            if (line.hasOption("d"))
                                datasetPath = line.getOptionValue("d");
                            else {
                                print("No path for intermidiate dataset for training is provided.");
                                print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
                            }

                            IntrinsticInference.evaluation(jarRepoPath,repositoryPath,datasetPath,modelPath,topk,contextSimilarity,nameSimilarity);

                        }else if(evalType.equals("extrinsic")) {
                            jarRepoPath = Config.SO_JAR_PATH;
                            repositoryPath = Config.SO_CODE_SNIPPET_PATH;
                            datasetPath = Config.SO_DATASET_PATH;
                            if (line.hasOption("r"))
                                repositoryPath = line.getOptionValue("r");
                            else {
                                print("No path of repository for training is provided.");
                                print("Selecting the deafult path of Repository for training:" + repositoryPath);
                            }
                            if (line.hasOption("j"))
                                jarRepoPath = line.getOptionValue("j");
                            else {
                                print("No path of jar files for training is provided.");
                                print("Selecting the deafult path of jar files for training: " + jarRepoPath);
                            }
                            if (line.hasOption("d"))
                                datasetPath = line.getOptionValue("d");
                            else {
                                print("No path for intermidiate dataset for training is provided.");
                                print("Selecting the deafult path for stroing intermidiate dataset for training: " + datasetPath);
                            }

                            ExtrinsicInference.evaluation(jarRepoPath,repositoryPath,datasetPath,modelPath,topk,contextSimilarity,nameSimilarity);
                        }

                    }else {
                        print("Please choose atleast one type of evaluations: intrinsic, extrinsic\n\n");
                        panic(0);
                    }
                    break;
                default:
                    print("Please choose right functionality\n\n");
                    panic(0);
                    break;
            }

        }else {
            print("You need to select atleast one of the following functionalities to run the COSTER: train, retrain, infer, eval\n\n");
            panic(0);
        }

    }

    private static void welcomeMessage(Properties properties){
        print("COSTER: Context Sensitive Type Solver");
        print("Version: "+properties.getProperty("version"));
        print("Source Code can be found at https://github.com/khaledkucse/coster");
        print("The work is accepted at 34th IEEE/ACM International Conference on Automated Software Engineering (ASE 2019)");
        print("Full paper is available at http://bit.ly/2YuuZrW\n\n\n");
        logger.info("COSTER: Context Sensitive Type Solver");
        logger.info("Version: "+properties.getProperty("version")+"\n");
    }
}
