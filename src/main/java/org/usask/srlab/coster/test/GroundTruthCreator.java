package org.usask.srlab.coster.test;

import org.usask.srlab.coster.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

public class GroundTruthCreator {
    public static void main(String[] args) {
        String importstatement = "import org.apache.commons.io.*;\n" +
                "import org.apache.commons.lang3.*;\n" +
                "import org.apache.commons.collections.*;\n" +
                "import org.apache.http.client.*;\n" +
                "import com.google.*;\n" +
                "import org.springframework.boot.*;\n" +
                "import org.apache.log4j.*;\n" +
                "import junit.*;\n";


        ArrayList<File> files = FileUtil.getSingleTonFileUtilInst().getFiles(new File("/home/khaledkucse/Project/backup/coster/Final_SO_DATASET/"));
        for(File each_file:files)
        {
            ArrayList<String> content = FileUtil.getSingleTonFileUtilInst().getFileStringArray(each_file.getAbsolutePath());
            String[] pathTokens= each_file.getAbsolutePath().trim().split("/");
            String dir = pathTokens[pathTokens.length-2];

            String newPath = "/home/khaledkucse/Project/backup/coster/DRAFT_DATASET/"+dir+"/"+each_file.getName();
            FileUtil.getSingleTonFileUtilInst().writeToFile(newPath,importstatement);
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(newPath,"public class "+each_file.getName().replaceAll(".java","")+" {");
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(newPath,"public void test(){");
            for(String each_line:content)
                FileUtil.getSingleTonFileUtilInst().appendLineToFile(newPath,each_line);

            FileUtil.getSingleTonFileUtilInst().appendLineToFile(newPath,"}\n}");
        }
    }
}


