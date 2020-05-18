package org.usask.srlab.coster.utils;

import java.io.File;
import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;



public class ParseUtil {

    private static final Logger logger = LogManager.getLogger(ParseUtil.class.getName()); // logger variable for loggin in the file
    public static HashMap<String,List<File>> getFilteredRecursiveFiles(File parentDir, String [] sourceFileExt)
    {
        HashMap<String,List<File>> recursiveFiles = new HashMap<>();

        File[] childFiles = parentDir.listFiles();
        if (childFiles==null)
            return recursiveFiles;
        for (File file:childFiles)
        {
            if (file.isFile())
            {
                String ext=isPassFileReturn(file, sourceFileExt);
                if (!ext.isEmpty())
                {
                    //	print(ext);
                    if(!recursiveFiles.containsKey(ext)){
                        List<File> lstFiles=new ArrayList<>();
                        lstFiles.add(file);
                        recursiveFiles.put(ext,lstFiles);
                    }else{
                        recursiveFiles.get(ext).add(file);
                    }

                }
            }
            else
            {
                HashMap<String,List<File>> subList = getFilteredRecursiveFiles(file, sourceFileExt);
                for(String strKey:subList.keySet()){
                    List<File> lstSubListFile=subList.get(strKey);
                    if(lstSubListFile.size()>0){
                        if(!recursiveFiles.containsKey(strKey)){
//							List<File> lstFiles=new ArrayList<File>();
//							lstFiles.addAll(lstSubListFile);
                            recursiveFiles.put(strKey, lstSubListFile);
                        } else{
                            recursiveFiles.get(strKey).addAll(lstSubListFile);
                        }
                    }

                }

            }
        }
        return recursiveFiles;
    }

    private static String isPassFileReturn(File file, String [] sourceFileExt)
    {
        String name = file.getName();
        for (String fileExt:sourceFileExt)
        {
            if (name.endsWith(fileExt))
            {
                return fileExt;
            }
        }
        return "";
    }


    public static String reformatFQN(String actualFQN){
        if (actualFQN.contains("<"))
            actualFQN = actualFQN.substring(0, actualFQN.indexOf("<"));
        return actualFQN;
    }


    public static String[] collectJarFiles(File jarFiles){
        logger.info("Collecting Jar Files");
        String[] extensions = { ".jar"};
        HashMap<String, List<File>> allJarFiles = getFilteredRecursiveFiles(jarFiles, extensions);
        List<File> arrJars = allJarFiles.get(".jar");
        if (arrJars == null) {
            arrJars = new ArrayList<>();
        }
        String[] jarPath = new String[arrJars.size() + 1];
        jarPath[0] = System.getProperty("java.home") +File.separator+ "lib"+File.separator+"rt.jar";

        for (int i = 0; i < arrJars.size(); i++) {
            jarPath[i + 1] = arrJars.get(i).getAbsolutePath();
        }
        logger.info("Total Number of JAR Files: "+ jarPath.length);
        return jarPath;
    }


    public static String[] collectGithubProjects(File subjectSytems){
        logger.info("Collecting GitHub Subject Systems/Projects");
        String[] extensions = { ".zip"};
        HashMap<String, List<File>> allProject = getFilteredRecursiveFiles(subjectSytems, extensions);
        List<File> arrProjetcs = allProject.get(".zip");
        if (arrProjetcs == null) {
            arrProjetcs = new ArrayList<>();
        }
        String[] projectPaths = new String[arrProjetcs.size()];
        for (int i = 0; i < arrProjetcs.size(); i++) {
            projectPaths[i] = arrProjetcs.get(i).getAbsolutePath();
        }
        logger.info("Total Number of Subject Systems: "+ projectPaths.length);
        return projectPaths;
    }


    public static ArrayList<File> collectJavaFiles(File file) {
        ArrayList<File> files = new ArrayList<>();
        if (file.isDirectory()){
            for (File subDirecotry : Objects.requireNonNull(file.listFiles()))
                files.addAll(collectJavaFiles(subDirecotry));
        }
        else if (file.getName().endsWith(".java")) {
            Map options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setCompilerOptions(options);
            parser.setIgnoreMethodBodies(true);
            parser.setSource(FileUtil.getSingleTonFileUtilInst().getFileContent(file.getAbsolutePath()).toCharArray());
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


    public static ArrayList<String> collectSOSnippets(File file) {
//        logger.info("Collecting Stack Overflow Code Snippets");
        ArrayList<String> files = new ArrayList<>();
        if (file.isDirectory()){
            for (File sub : Objects.requireNonNull(file.listFiles()))
                files.addAll(collectSOSnippets(sub));
        }
        else if (file.getName().endsWith(".java")) {
            Map options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setCompilerOptions(options);
            parser.setIgnoreMethodBodies(true);
            parser.setSource(FileUtil.getSingleTonFileUtilInst().getFileContent(file.getAbsolutePath()).toCharArray());
            try {
                CompilationUnit ast = (CompilationUnit) parser.createAST(null);
                if (ast.getPackage() != null && !ast.types().isEmpty()) {
                    files.add(file.getAbsolutePath());
                }
            } catch (Throwable t) {
                // Skip file
            }
        }
        return files;
    }

    public static ArrayList<String> collectSnippets(File file) {
        logger.info("Collecting Code Snippets");
        ArrayList<String> files = new ArrayList<>();
        if (file.isDirectory())
            for (File sub : Objects.requireNonNull(file.listFiles()))
                files.addAll(collectSnippets(sub));
        else if (file.getName().endsWith(".java"))
            files.add(file.getAbsolutePath());
        return files;
    }
}
