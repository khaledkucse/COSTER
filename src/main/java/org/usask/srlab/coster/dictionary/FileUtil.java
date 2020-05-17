package org.usask.srlab.coster.dictionary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class FileUtil {
    public static ArrayList<String> getFileStringArray(String fp) {
        ArrayList<String> lstResults = new ArrayList<String>();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(fp))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // process the line.
                    // strResult+=line+"\n";
                    if (!line.trim().isEmpty()) {
                        lstResults.add(line.trim());
                    }
                }
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        return lstResults;

    }
}
