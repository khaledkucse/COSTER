package org.usask.srlab.coster.infer;

import java.io.File;
import java.util.*;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import org.usask.srlab.coster.config.Config;

public class RecieverVariableAnalysis
{
    private static Map<String,Integer> expression_types;
    public static void main(String[] args) {

        expression_types = new HashMap<String, Integer>();
        extractfromSource(new File(Config.TEMP_PATH+"projects/"));

        for (Map.Entry each_expressionType : expression_types.entrySet()) {
            System.out.println(each_expressionType.getKey().toString()+": "+each_expressionType.getValue().toString());
        }


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
            System.gc();
            cu.accept(new ASTVisitor() {
                //visit every method body.....
                @Override
                public boolean visit(final MethodDeclaration node) {
                    final Block block = node.getBody();
                    if (block != null) {
                        block.accept(new ASTVisitor() {
                            //visit every method invoked in a method body.
                            public boolean visit(MethodInvocation invocationnode) {
                                Expression expression = invocationnode.getExpression();
                                if (expression != null) {
                                    //resolve the type of the method invocation
                                    ITypeBinding typeBinding = expression.resolveTypeBinding();
                                    if (typeBinding != null) {
                                        //if the packge name is on the list of the allowed package
                                        if(typeBinding.getPackage() !=null && Config.isAllowedPackage(typeBinding.getPackage().getName()))
                                        {
                                            calculateExpressionType(expression);
                                            System.gc();
                                        }

                                    }
                                }
                                return false;
                            }
                        });
                    }
                    return true;
                }
            });
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

    public static void calculateExpressionType(Expression expression)
    {
        if(expression instanceof SimpleName){
            if(expression_types.containsKey("SimpleName"))
                expression_types.put("SimpleName",expression_types.get("SimpleName")+1);
            else
                expression_types.put("SimpleName",1);
        }
        else if(expression instanceof QualifiedName){
            if(expression_types.containsKey("QualifiedName"))
                expression_types.put("QualifiedName",expression_types.get("QualifiedName")+1);
            else
                expression_types.put("QualifiedName",1);
        }
        else if(expression instanceof MethodInvocation){
            System.out.println(expression);
            if(expression_types.containsKey("MethodInvocation"))
                expression_types.put("MethodInvocation",expression_types.get("MethodInvocation")+1);
            else
                expression_types.put("MethodInvocation",1);
        }
        else if(expression instanceof CastExpression){
            if(expression_types.containsKey("CastExpression"))
                expression_types.put("CastExpression",expression_types.get("CastExpression")+1);
            else
                expression_types.put("CastExpression",1);
        }
        else if(expression instanceof ThisExpression){
            if(expression_types.containsKey("ThisExpression"))
                expression_types.put("ThisExpression",expression_types.get("ThisExpression")+1);
            else
                expression_types.put("ThisExpression",1);
        }
        else if(expression instanceof VariableDeclarationExpression ){
            if(expression_types.containsKey("VariableDeclarationExpression"))
                expression_types.put("VariableDeclarationExpression",expression_types.get("VariableDeclarationExpression")+1);
            else
                expression_types.put("VariableDeclarationExpression",1);
        }
        else if(expression instanceof TypeLiteral){
            if(expression_types.containsKey("TypeLiteral"))
                expression_types.put("TypeLiteral",expression_types.get("TypeLiteral")+1);
            else
                expression_types.put("TypeLiteral",1);
        }
        else if(expression instanceof SuperMethodInvocation){
            if(expression_types.containsKey("SuperMethodInvocation"))
                expression_types.put("SuperMethodInvocation",expression_types.get("SuperMethodInvocation")+1);
            else
                expression_types.put("SuperMethodInvocation",1);
        }
        else if(expression instanceof SuperFieldAccess){
            if(expression_types.containsKey("SuperFieldAccess"))
                expression_types.put("SuperFieldAccess",expression_types.get("SuperFieldAccess")+1);
            else
                expression_types.put("SuperFieldAccess",1);
        }
        else if(expression instanceof StringLiteral){
            if(expression_types.containsKey("StringLiteral"))
                expression_types.put("StringLiteral",expression_types.get("StringLiteral")+1);
            else
                expression_types.put("StringLiteral",1);
        }
        else if(expression instanceof PrefixExpression){
            if(expression_types.containsKey("PrefixExpression"))
                expression_types.put("PrefixExpression",expression_types.get("PrefixExpression")+1);
            else
                expression_types.put("PrefixExpression",1);
        }
        else if(expression instanceof PostfixExpression){
            if(expression_types.containsKey("PostfixExpression"))
                expression_types.put("PostfixExpression",expression_types.get("PostfixExpression")+1);
            else
                expression_types.put("PostfixExpression",1);
        }
        else if(expression instanceof ParenthesizedExpression){
            if(expression_types.containsKey("ParenthesizedExpression"))
                expression_types.put("ParenthesizedExpression",expression_types.get("ParenthesizedExpression")+1);
            else
                expression_types.put("ParenthesizedExpression",1);
        }
        else if(expression instanceof NumberLiteral){
            if(expression_types.containsKey("NumberLiteral"))
                expression_types.put("NumberLiteral",expression_types.get("NumberLiteral")+1);
            else
                expression_types.put("NumberLiteral",1);
        }
        else if(expression instanceof NullLiteral){
            if(expression_types.containsKey("NullLiteral"))
                expression_types.put("NullLiteral",expression_types.get("NullLiteral")+1);
            else
                expression_types.put("NullLiteral",1);
        }
        else if(expression instanceof MethodReference){
            if(expression_types.containsKey("MethodReference"))
                expression_types.put("MethodReference",expression_types.get("MethodReference")+1);
            else
                expression_types.put("MethodReference",1);
        }
        else if(expression instanceof LambdaExpression){
            if(expression_types.containsKey("LambdaExpression"))
                expression_types.put("LambdaExpression",expression_types.get("LambdaExpression")+1);
            else
                expression_types.put("LambdaExpression",1);
        }
        else if(expression instanceof InstanceofExpression){
            if(expression_types.containsKey("InstanceofExpression"))
                expression_types.put("InstanceofExpression",expression_types.get("InstanceofExpression")+1);
            else
                expression_types.put("InstanceofExpression",1);
        }
        else if(expression instanceof InfixExpression){
            if(expression_types.containsKey("InfixExpression"))
                expression_types.put("InfixExpression",expression_types.get("InfixExpression")+1);
            else
                expression_types.put("InfixExpression",1);
        }
        else if(expression instanceof FieldAccess){
            if(expression_types.containsKey("FieldAccess"))
                expression_types.put("FieldAccess",expression_types.get("FieldAccess")+1);
            else
                expression_types.put("FieldAccess",1);
        }
        else if(expression instanceof ConditionalExpression){
            if(expression_types.containsKey("ConditionalExpression"))
                expression_types.put("ConditionalExpression",expression_types.get("ConditionalExpression")+1);
            else
                expression_types.put("ConditionalExpression",1);
        }
        else if(expression instanceof ClassInstanceCreation){
            if(expression_types.containsKey("ClassInstanceCreation"))
                expression_types.put("ClassInstanceCreation",expression_types.get("ClassInstanceCreation")+1);
            else
                expression_types.put("ClassInstanceCreation",1);
        }
        else if(expression instanceof CharacterLiteral){
            if(expression_types.containsKey("CharacterLiteral"))
                expression_types.put("CharacterLiteral",expression_types.get("CharacterLiteral")+1);
            else
                expression_types.put("CharacterLiteral",1);
        }
        else if(expression instanceof BooleanLiteral){
            if(expression_types.containsKey("BooleanLiteral"))
                expression_types.put("BooleanLiteral",expression_types.get("BooleanLiteral")+1);
            else
                expression_types.put("BooleanLiteral",1);
        }
        else if(expression instanceof Assignment){
            if(expression_types.containsKey("Assignment"))
                expression_types.put("Assignment",expression_types.get("Assignment")+1);
            else
                expression_types.put("Assignment",1);
        }
        else if(expression instanceof ArrayInitializer){
            if(expression_types.containsKey("ArrayInitializer"))
                expression_types.put("ArrayInitializer",expression_types.get("ArrayInitializer")+1);
            else
                expression_types.put("ArrayInitializer",1);
        }
        else if(expression instanceof ArrayCreation){
            if(expression_types.containsKey("ArrayCreation"))
                expression_types.put("ArrayCreation",expression_types.get("ArrayCreation")+1);
            else
                expression_types.put("ArrayCreation",1);
        }
        else if(expression instanceof ArrayAccess){
            if(expression_types.containsKey("ArrayAccess"))
                expression_types.put("ArrayAccess",expression_types.get("ArrayAccess")+1);
            else
                expression_types.put("ArrayAccess",1);
        }
    }

}
