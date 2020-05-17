package org.usask.srlab.coster.utils;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DictonaryUtil {
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
    public static void writeToFile(String path, Collection<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String l : lines)
            sb.append(l + "\n");
        writeToFile(path, sb.toString());
    }
    public static void writeToFile(String path, String content) {
        BufferedWriter bf = null;
        try {
            bf = new BufferedWriter(new FileWriter(new File(path), false));
            bf.write(content);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bf != null)
                    bf.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static ArrayList<File> getPaths(File file) {
        ArrayList<File> files = new ArrayList<>();
        if (file.isDirectory())
            for (File sub : file.listFiles())
                files.addAll(getPaths(sub));
        else if (file.getName().endsWith(".java")) {
            Map options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setCompilerOptions(options);
            parser.setIgnoreMethodBodies(true);
            parser.setSource(DictonaryUtil.getFileContent(file.getAbsolutePath()).toCharArray());
            try {
                CompilationUnit ast = (CompilationUnit) parser.createAST(null);
                if (ast.getPackage() != null && !ast.types().isEmpty()) {
                    files.add(file);
                }
            } catch (Throwable t) {
                // Skip file
            }
        }
        return files;
    }
    public static String getFileContent(String fp) {
        String strResult = "";
        try {
            strResult = new String(Files.readAllBytes(Paths.get(fp)));
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        return strResult;

    }
}
