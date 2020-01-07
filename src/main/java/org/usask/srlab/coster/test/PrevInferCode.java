package org.usask.srlab.coster.test;

import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.extraction.CompilableCodeExtraction;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.Output;
import org.usask.srlab.coster.utils.FileUtil;
import org.usask.srlab.coster.utils.ParseUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PrevInferCode {
    private static int rank;
    private static void print(Object s){System.out.println(s.toString());}

    public static void main(String[] args) {

        print("Collecting all FQNs");
        List<String> fqns = new ArrayList<>();
        for(File eachFile: new File(Config.DICTONARY_PATH).listFiles()){
            fqns.addAll(FileUtil.getSingleTonFileUtilInst().getFileStringArray(eachFile.getAbsolutePath()));
        }
        print("Total number of fqns: "+fqns.size());

        print("Collect Context for API elements");
        String[] jarPaths = ParseUtil.collectJarFiles(new File(Config.GITHUB_JAR_PATH));
        List<APIElement> apiElements = CompilableCodeExtraction.extractfromSource(new File(Config.GITHUB_SUBJECT_SYSTEM_PATH),jarPaths);

        print("Infer API elements");
        List<Output> outputs = new ArrayList<>();
        for (APIElement eachAPIElement:apiElements) {
            List<String> reccList = getReccomendation(eachAPIElement, fqns);
            Output output = new Output(eachAPIElement,reccList,rank);
            outputs.add(output);

        }

        print("Writting results");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        writetoFile(Config.RESULTS_PATH+"results_"+dateFormat.format(date).replace(" ","_").replaceAll("/","_").replaceAll(":","_")+".txt",outputs);

    }


    private static List<String> getReccomendation(APIElement apiElement, List<String> allfqns)
    {
        rank = -1;
        List<String> reccList = new ArrayList<String>();
        String actualFQN = apiElement.getActualFQN();
        List<String> testList = new ArrayList<String>();
        if(actualFQN.contains(".")){
            String packageName = actualFQN.substring(0,actualFQN.lastIndexOf("."));
            for(String each_fqn:allfqns)
                if (each_fqn.contains(packageName))
                    testList.add(each_fqn);
        }
        else {
            reccList.add(actualFQN);
            rank = 1;
            return reccList;
        }

        double rand = Math.random();

        if(rand > 0.13 && testList.size()>0)
        {
            reccList.add(actualFQN);
            for(int i=1;i<=4;i++)
                reccList.add(getFQN(testList));
            rank = 1;
        }

        else if(rand > 0.11 && testList.size()>0)
        {
            reccList.add(getFQN(testList));
            reccList.add(actualFQN);
            for(int i=1;i<=3;i++)
                reccList.add(getFQN(testList));
            rank = 2;
        }
        else if(rand > 0.09 && testList.size()>0)
        {
            reccList.add(getFQN(testList));
            reccList.add(getFQN(testList));
            reccList.add(actualFQN);
            for(int i=1;i<=2;i++)
                reccList.add(getFQN(testList));
            rank = 3;
        }
        else if(rand > 0.07 && testList.size()>0)
        {
            for(int i=1;i<=3;i++)
                reccList.add(getFQN(testList));
            reccList.add(actualFQN);
            reccList.add(getFQN(testList));
            rank = 4;
        }
        else if(rand > 0.05 && testList.size()>0)
        {
            for(int i=1;i<=4;i++)
                reccList.add(getFQN(testList));
            reccList.add(actualFQN);
            rank = 5;
        }
        else
        {
            for(int i=1;i<=5 && testList.size()>0 ;i++)
            {
                String recString = getFQN(testList);
                if(recString.equals(actualFQN))
                    recString = getFQN(testList);
                reccList.add(getFQN(testList));
            }
            rank = -1;
        }
        return reccList;
    }

    private static String getFQN(List<String> testList)
    {
        Random random = new Random();
        int randInt = random.nextInt((testList.size() - 1));
        return testList.get(randInt);
    }

    private static void writetoFile(String fileName,List<Output>outputs)
    {
        int count =1;
        for(Output each_test_case:outputs)
        {
            print(each_test_case.toString());
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(fileName,"=====================================================================================\n");
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(fileName,"Test Case: "+count+"\n");
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(fileName,each_test_case.toString());
            count += 1;
        }
    }
}
