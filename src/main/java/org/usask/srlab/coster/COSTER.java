package org.usask.srlab.coster;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.util.StringUtils;
import org.usask.srlab.coster.infer.ExtrinsicInfer;
import org.usask.srlab.coster.infer.IntrinsticInfer;
import org.usask.srlab.coster.train.CreateOLD;
import org.usask.srlab.coster.train.RetrainOLD;

import java.util.Properties;
import java.util.Scanner;

public class COSTER {
    private static final Logger logger = LogManager.getLogger(COSTER.class.getName()); // logger variable for loggin in the file
    private static void print(Object s){System.out.println(s.toString());}

    public static void main(String[] args) {
        PropertyConfigurator.configure(COSTER.class.getClass().getResourceAsStream("/log4j.properties"));
        final Properties properties = new Properties();
        try {
            properties.load(COSTER.class.getClass().getResourceAsStream("/coster.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("COSTER: Context Sensitive Type Solver");
        print("Version: "+properties.getProperty("version"));
        print("Source Code can be found at https://github.com/khaledkucse/coster");
        print("The work is accepted at 34th IEEE/ACM International Conference on Automated Software Engineering (ASE 2019)");
        print("Full paper is available at http://bit.ly/2YuuZrW");
        logger.info("COSTER: Context Sensitive Type Solver");
        logger.info("Version: "+properties.getProperty("version")+"\n");

        run();

    }


    private static void run(){
        print("Chose one from the following:\n" +
                "1\tCreate OLD\n" +
                "2\tRetrain OLD\n" +
                "3\tIntrisitc Evaluation\n" +
                "4 \tExtrinsic Evaluation\n");
        print("Option Selected: ");
        Scanner sc = new Scanner(System.in);
        String choice = sc.nextLine();


        if(choice.equals("1")){
            print("All the trained data will be erased!!!!. Are you sure?[y/n]");
            String isSure = sc.next();
            if(isSure.equals("y") || isSure.equals("Y"))
                CreateOLD.createOld();
            else
                run();
        }
        else if(choice.equals("2")){
            RetrainOLD.retrainOLD();
        }
        else if(choice.equals("3")){
            print("Fow which Top-K reccomendation you like to see the evaluation?");
            String topk = sc.next();
            if (!StringUtils.isNumeric(topk)) {
                print("The input cannot be parsed into a numerical value. Please provide a numeric value such as 5");
                run();
            }
            else{
                IntrinsticInfer.instrinsicEvaluation(Integer.parseInt(topk));
            }
        }
        else if(choice.equals("4")){
            print("Fow which Top-K reccomendation you like to see the evaluation?");
            String topk = sc.next();
            if (!StringUtils.isNumeric(topk)) {
                print("The input cannot be parsed into a numerical value. Please provide a numeric value such as 5");
                run();
            }
            else{
                ExtrinsicInfer.extrinsicInference(Integer.parseInt(topk));
            }
        }
    }
}
