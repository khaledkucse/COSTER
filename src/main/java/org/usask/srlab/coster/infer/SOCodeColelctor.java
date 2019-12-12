package org.usask.srlab.coster.infer;

import java.io.File;
import java.io.IOException;;
import java.util.*;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;


import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.utils.FileUtil;

public class SOCodeColelctor
{
    private static Map<String,List<String>> results;
    public static void main(String[] args) throws IOException {

        System.out.println("Extracting Code Snippet");
        results = new HashMap<String, List<String>>();
        extractfromSource(new File(Config.TEMP_PATH+"projects"));
        System.out.println("Number of Code Snippet found: "+ results.size());

        System.out.println("Writting results");
        writetoFile();



    }

    public static void extractfromSource(File projectFile)
    {
        System.out.println("Colelcting Java and Jar Files");
        String[] arrAllExtension = { ".java", ".jar" };
        String[] sources = { projectFile.getAbsolutePath() };
        HashMap<String, List<File>> arrAllSources = getFilteredRecursiveFiles(projectFile, arrAllExtension);

        System.out.println("Colelcting Java Files");
        List<File> files = arrAllSources.get(".java");
        if (files == null) {
            files = new ArrayList<File>();
        }
        System.out.println("Total Number of Java Files: "+ files.size());

        System.out.println("Colelcting Jar Files");
        List<File> arrJars = arrAllSources.get(".jar");
        arrJars.addAll(Arrays.asList(new File(Config.TEMP_PATH+"jars/").listFiles()));
        if (arrJars == null) {
            arrJars = new ArrayList<File>();
        }
        System.out.println("Total Number of JAR Files: "+ arrJars.size());

        String[] classpath = new String[arrJars.size() + 1];
        classpath[0] = System.getProperty("java.home") +File.separator+ "lib"+File.separator+"rt.jar";
        for (int i = 0; i < arrJars.size(); i++) {
            classpath[i + 1] = arrJars.get(i).getAbsolutePath();
        }

        String[] paths = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            paths[i] = files.get(i).getAbsolutePath();
        }

        System.out.println("Creating Compilation Unit for each java file");
        final HashSet<CompilationUnit> cus = new HashSet<>();
        FileASTRequestor r = new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit cu) {
                cus.add(cu);
            }
        };

        @SuppressWarnings("rawtypes")
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setCompilerOptions(options);
        parser.setEnvironment(classpath == null ? new String[0] : classpath, sources, new String[]{"UTF-8"}, true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.createASTs(paths, null, new String[0], r, null);


        System.out.println("Total Number of Compilation Units: " + cus.size());
        System.out.println("Extracting types/methods/fields from each Compilation Unit");
        for (final CompilationUnit cu : cus)
        {
            if (results.size() > Config.MAX_NO_OF_SO_CODE_SNIPPET)
                break;
            System.gc();
            cu.accept(new ASTVisitor() {
                //visit every method body.....
                @Override
                public boolean visit(final MethodDeclaration node) {
                    final Block block = node.getBody();
                    if (block != null && block.statements().size()>= Config.MIN_SO_CODE_LINE && block.statements().size()<=Config.MAX_SO_CODE_LINE) {
                        block.accept(new ASTVisitor() {
                            //visit every method invoked in a method body.
                            public boolean visit(MethodInvocation invocationnode) {
                                Expression expression = invocationnode.getExpression();
                                if (expression != null) {
                                    //resolve the type of the method invocation
                                    ITypeBinding typeBinding = expression.resolveTypeBinding();
                                    if (typeBinding != null) {
                                        //if the packge name is on the list of the allowed package
                                        if(typeBinding.getPackage() !=null &&
                                                Config.isAllowedPackage(typeBinding.getPackage().getName())  &&
                                                results.size() < Config.MAX_NO_OF_SO_CODE_SNIPPET)
                                        {
                                            //System.out.println(typeBinding.getPackage().getName()+"."+invocationnode.getName()+" "+invocationnode.arguments());
                                            //Creating method body
                                            List<String> prev_code = new ArrayList<>();
                                            StringBuilder prev_code_stmt = new StringBuilder();
                                            for(Object eachStatement: block.statements())
                                            {
                                                prev_code.add(eachStatement.toString());
                                                prev_code_stmt.append(eachStatement.toString());
                                            }
                                            if(!results.containsKey(prev_code_stmt.toString()))
                                                results.put(prev_code_stmt.toString(),prev_code);

                                            System.gc();
                                        }

                                    }
                                }
                                return true;
                            }
                        });
                    }
                    return true;
                }
            });
        }
    }

    private static void writetoFile()
    {
        int count =1;
        int folder_count = 1;
        for(Map.Entry<String, List<String>> each_code_snippet : results.entrySet())
        {
            List<String> each_test_case = each_code_snippet.getValue();
            if(new File(Config.SO_CODE_SNIPPET_PATH +"fold"+folder_count).mkdir()){}
            String path = Config.SO_CODE_SNIPPET_PATH +"fold"+folder_count+"/"+"SO_"+count+".java";
            FileUtil.getSingleTonFileUtilInst().writeToFile(path,"package fold"+folder_count+";\n");
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(path,Config.SO_IMPORT_STATEMENT);
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(path,"public class "+"SO_"+count+" {");
            FileUtil.getSingleTonFileUtilInst().appendLineToFile(path,"public void soCodeSnippet(){");
            for(String each_line:each_test_case)
                FileUtil.getSingleTonFileUtilInst().appendLineToFile(path,each_line);

            FileUtil.getSingleTonFileUtilInst().appendLineToFile(path,"}\n}");
            if(count%50==0)
                folder_count++;
            count += 1;
        }
    }

    public static HashMap<String,List<File>> getFilteredRecursiveFiles(File parentDir, String [] sourceFileExt)
    {
        HashMap<String,List<File>> recursiveFiles = new HashMap<String,List<File>>();

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
                    //	System.out.println(ext);
                    if(!recursiveFiles.containsKey(ext)){
                        List<File> lstFiles=new ArrayList<File>();
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

    public static String isPassFileReturn(File file, String [] sourceFileExt)
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

}
