package org.usask.srlab.coster.config;

public class Config {
    //Global Path variables
    private static String ROOT_PATH = "/home/khaledkucse/Project/backup/coster/";
//    private static String ROOT_PATH = "./";
    private static final String DATA_PATH = ROOT_PATH+"data/";
    private static final String JAR_PATH = DATA_PATH+"jars/";
    public static final String LOG_PATH = ROOT_PATH+"logs/";
    public static final String MODEL_PATH = ROOT_PATH+"model/";
    public static final String RESULTS_PATH = ROOT_PATH+"results/";
    public static final String TEMP_PATH = ROOT_PATH+"temp/";

    //Dictonary Related Path variable
    public static final String FULL_JAR_PATH = JAR_PATH+"full/";
    public static final String DICTONARY_PATH = DATA_PATH+"dictonary/";
    public static final String SO_DICTONARY_PATH = DICTONARY_PATH+"so/";
    public static final String JAR_DICTONARY_PATH = DICTONARY_PATH+"jar/";
    public static final String SUBJECT_SYSTEM_DICTONARY_PATH = DICTONARY_PATH+"subjectSystem/";

    //Training Related Path Variable
    public static final String GITHUB_JAR_PATH = JAR_PATH+"github/";
    public static final String SO_JAR_PATH = JAR_PATH+"so/";
    public static final String GITHUB_SUBJECT_SYSTEM_PATH = DATA_PATH+"GitHubDataset/subjectSystem/";
    public static final String GITHUB_DATSET_PATH = DATA_PATH+"GitHubDataset/dataset/";
    public static final String SO_CODE_SNIPPET_PATH = DATA_PATH+"SODataset/codeSnippet/";
    public static final String SO_DATASET_PATH = DATA_PATH+"SODataset/dataset/";

    //Inference related Path variable
    public static final String TEST_SUBJECT_SYSTEM_PATH = DATA_PATH+"TestDataset/subjectSystem/";
    public static final String TEST_DATSET_PATH = DATA_PATH+"TestDataset/dataset/";


    //SO Code related variable
    public static final int MIN_SO_CODE_LINE = 5;
    public static final int MAX_SO_CODE_LINE = 20;
    public static final int MAX_NO_OF_SO_CODE_SNIPPET = 500;

    //Train test Related varaible
    public static final int FQN_THRESHOLD = 50;
    public static final double alpha  = 0.5;
    public static final double beta  = 0.8;
    public static final double gamma  = 0.75;
    public static final int DEFAULT_TOP_K  = 1;


    private static final String [] JAVA_KEYWORDS = {"abstract","boolean","break","byte","case","catch","char","class","const","continue",
                                                    "default","do","double","else","extends","final","finally","float","for","goto","if",
                                                    "implements","import","instanceof","int","interface","long","native","new","null","package",
                                                    "private","protected","public","return","short","static","super","switch","synchronized",
                                                    "this","throw","throws","transient","try","void","volatile","while","assert","enum",
                                                    "strictfp","null","true","false","=","<","<=",">",">=","=="};


    private static final String[] SO_PACKAGE_NAME ={"org.apache.commons",
                                                "com.google.guava",
                                                "com.google.code.gson",
                                                "org.springframework.boot",
                                                "org.apache.httpcomponent",
                                                "java"};
    private static final String[] SO_PACKAGE_COMPONENT = {"log4j","junit"};
    public static final String SO_IMPORT_STATEMENT = "import org.apache.commons.*;\n" +
                                                    "import org.apache.http.client.*;\n" +
                                                    "import com.google.*;\n" +
                                                    "import org.springframework.boot.*;\n" +
                                                    "import org.apache.log4j.*;\n" +
                                                    "import junit.*;\n";




    public static boolean isJavaKeyword(String token)
    {
        for(String each_keyword:JAVA_KEYWORDS)
            if (token.trim().equals(each_keyword))
                return true;

        return false;
    }


    public static boolean isAllowedPackage(String packagename)
    {
        for(String each_package: SO_PACKAGE_NAME)
            if (packagename.trim().startsWith(each_package))
                return true;
        for(String each_package: SO_PACKAGE_COMPONENT)
            if(packagename.trim().contains(each_package))
                return true;

        return false;
    }
}
