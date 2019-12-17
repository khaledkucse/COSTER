package org.usask.srlab.coster.extraction;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.*;

import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.CompileUnit;

public class SOCodeExtraction {
    private static final Logger logger = LogManager.getLogger(SOCodeExtraction.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits
//    private static void print(Object s){System.out.println(s.toString());}

    public static List<APIElement> extractFromSOPOST(File projectFile, String[] sourcefilepaths, String[] jarPaths) {
        String[] sources = { projectFile.getAbsolutePath() };

        df.setMaximumFractionDigits(2);

//        print("Configuring Eclise JDT Parser....");
        ASTParser astParser = CompilableCodeExtraction.configEclipseJDTParser(sources,jarPaths);

//        print("Collecting Compilation Units....");
        final List<CompileUnit> cus = CompilableCodeExtraction.collectCompilationUnits(astParser,sourcefilepaths);

        logger.info("Extracting types/methods/fields from each Compilation Unit");
        List<APIElement> apiElements = new ArrayList<>();
        int count = 0;
        for (final CompileUnit compileUnit : cus) {
            System.gc();
            apiElements.addAll(CompilableCodeExtraction.parseSourceCode(compileUnit));
            count ++;

            if(count%100 == 0){
                logger.info(count+" compilation units out of "+cus.size()+" are parsed. Percentage of completion: "+df.format((count*100/cus.size()))+"%");
            }
        }

        logger.info(count+" compilation units out of "+cus.size()+" are parsed. Percentage of completion: "+df.format((count*100/cus.size()))+"%");

        return apiElements;

    }
}
