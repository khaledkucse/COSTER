package org.usask.srlab.coster.extraction;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.model.APIElement;
import org.usask.srlab.coster.model.CompileUnit;
import org.usask.srlab.coster.utils.ParseUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class CompilableCodeExtraction {
    private static final Logger logger = LogManager.getLogger(CompilableCodeExtraction.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits
    private static void print(Object s){System.out.println(s.toString());}

    public static List<APIElement> extractfromSource(File projectFile, String[] jarPaths)
    {
        String[] sources = { projectFile.getAbsolutePath() };

        df.setMaximumFractionDigits(2);

//        print("Colelcting Source code files....");
        String[] sourcefilepaths = collectJavaFiles(projectFile);

//        print("Configuring Eclise JDT Parser....");
        ASTParser astParser = configEclipseJDTParser(sources,jarPaths);

//        print("Collecting Compilation Units....");
        final List<CompileUnit> cus = collectCompilationUnits(astParser,sourcefilepaths);

        logger.info("Extracting types/methods/fields from each Compilation Unit");
        List<APIElement> apiElements = new ArrayList<>();
        int count = 0;
        for (final CompileUnit compileUnit : cus) {
            System.gc();
            apiElements.addAll(parseSourceCode(compileUnit));
            count ++;

            if(count%100 == 0){
                logger.info(count+" compilation units out of "+cus.size()+" are parsed. Percentage of completion: "+df.format((count*100/cus.size()))+"%");
            }
        }

        logger.info(count+" compilation units out of "+cus.size()+" are parsed. Percentage of completion: "+df.format((count*100/cus.size()))+"%");

        return apiElements;

    }

    private static String[] collectJavaFiles(File projectFile){
        logger.info("Colelcting Java Files");
        String[] extensions = { ".java"};
        HashMap<String, List<File>> allJarJavaFiles = ParseUtil.getFilteredRecursiveFiles(projectFile, extensions);

        List<File> sourceCodeFiles = allJarJavaFiles.get(".java");
        if (sourceCodeFiles == null) {
            sourceCodeFiles = new ArrayList<>();
        }
        String[] sourceFilePath = new String[sourceCodeFiles.size()];
        for (int i = 0; i < sourceCodeFiles.size(); i++) {
            sourceFilePath[i] = sourceCodeFiles.get(i).getAbsolutePath();
        }
        logger.info("Total Number of Java Files: "+ sourceFilePath.length);
        return sourceFilePath;
    }

    static ASTParser configEclipseJDTParser(String[] sources, String[] jarPaths){
        logger.info("Configure Eclipse JDT");

        @SuppressWarnings("rawtypes")
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setCompilerOptions(options);
        parser.setEnvironment(jarPaths == null ? new String[0] : jarPaths, sources, new String[]{"UTF-8"}, true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);

        return parser;
    }

    static List<CompileUnit> collectCompilationUnits(ASTParser parser, String[] sourceFilePath){
        logger.info("Creating Compilation Unit for each java file");
        final List<CompileUnit> cus = new ArrayList<>();
        FileASTRequestor r = new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit cu) {
                CompileUnit compileUnit = new CompileUnit(sourceFilePath,cu);
                cus.add(compileUnit);
            }
        };
        parser.createASTs(sourceFilePath, null, new String[0], r, null);

        logger.info("Total Number of Compilation Units: " + cus.size());

        return cus;
    }


    static List<APIElement> parseSourceCode(CompileUnit compileUnit)
    {
        CompilationUnit cu = compileUnit.getCompilationUnit();
        List<APIElement> apiElements = new ArrayList<>();

        cu.accept(new ASTVisitor() {

            //Visit all method body
            @Override
            public boolean visit(final MethodDeclaration methodDecNode) {
                final Block block = methodDecNode.getBody();
                if (block != null) {

                    block.accept(new ASTVisitor() {
                        Map<String,APIElement> identifiers = new HashMap<>();

                        //Visit every field/variable decleration
                        @Override
                        public boolean visit(VariableDeclarationFragment varDecNode) {
                            IVariableBinding variableBinding = varDecNode.resolveBinding();
                            if (variableBinding != null && !variableBinding.getType().isFromSource()) {
                                SimpleName varName = varDecNode.getName();
                                String apiElement = varName.getIdentifier();
                                int linenumber = cu.getLineNumber(varName.getStartPosition());
                                String actualFQN = ParseUtil.reformatFQN(variableBinding.getType().getQualifiedName());
                                APIElement fieldDecleration = new APIElement(apiElement,compileUnit.getFilePath(),linenumber,actualFQN);

                                getFieldContext(fieldDecleration,block,varDecNode.toString());
                                this.identifiers.put(apiElement,fieldDecleration);

                                if(fieldDecleration.getContext().size() > 5)
                                    apiElements.add(fieldDecleration);

                            }
                            System.gc();
                            return true;
                        }

                        //Visit every field/variable usages
                        @Override
                        public boolean visit(Assignment assignmentNode) {
                            if(assignmentNode.getLeftHandSide() instanceof SimpleName){
                                ITypeBinding variableBinding = assignmentNode.getLeftHandSide().resolveTypeBinding();
                                if (variableBinding != null && !variableBinding.isFromSource()) {
                                    SimpleName varName = (SimpleName) assignmentNode.getLeftHandSide();
                                    String apiElement = varName.getIdentifier();
                                    if (!identifiers.containsKey(apiElement)){
                                        int linenumber = cu.getLineNumber(varName.getStartPosition());
                                        String actualFQN = ParseUtil.reformatFQN(variableBinding.getQualifiedName());
                                        APIElement fieldImplementation = new APIElement(apiElement,compileUnit.getFilePath(),linenumber,actualFQN);

                                        getFieldContext(fieldImplementation,block,assignmentNode.toString());
                                        this.identifiers.put(apiElement,fieldImplementation);

                                        if(fieldImplementation.getContext().size() > 5)
                                            apiElements.add(fieldImplementation);

                                    }
                                }
                            }
                            System.gc();
                            return true;
                        }


                        //visit every method invocation
                        @Override
                        public boolean visit(MethodInvocation invocationnode) {
                            Expression expression = invocationnode.getExpression();
                            if (expression != null) {
                                ITypeBinding typeBinding = expression.resolveTypeBinding();
                                if (typeBinding != null && !typeBinding.isFromSource() && typeBinding.getPackage() !=null) {
                                    String apiElement = invocationnode.toString();
                                    String apiExpression = expression.toString()+"."+invocationnode.getName().getIdentifier();
                                    int linenumber = cu.getLineNumber(invocationnode.getStartPosition());
                                    String actualFQN = ParseUtil.reformatFQN(typeBinding.getQualifiedName());
                                    APIElement methodInvocation = new APIElement(apiElement,compileUnit.getFilePath(),linenumber,actualFQN);
                                    getMethodContext(methodInvocation,block,apiExpression,expression.toString());
                                    if(methodInvocation.getContext().size() > 5)
                                        apiElements.add(methodInvocation);
                                    System.gc();
                                }
                            }
                            return true;
                        }

                    });
                }
                return true;
            }
        });
        return apiElements;
    }


    private static void getMethodContext(APIElement apiElement,Block block,String apiExpression,String recievervariable)
    {

        List<String>[] prevCode = splitPrevPostCode(block,apiExpression,apiElement.getName());

        StringBuilder[] codes = splitLocalGlobalCode(prevCode[0],prevCode[1],prevCode[2]);

        List<String> returnedcontext = getGlobalContext(apiElement,codes[1],recievervariable);

        returnedcontext.addAll(getLocalContext(apiElement,codes[0]));

        returnedcontext.addAll(getGlobalContext(apiElement,codes[2],recievervariable));

        apiElement.setContext(returnedcontext);
    }





    private static void getFieldContext(APIElement apiElement,Block block,String apiStatement)
    {

        List<String>[] prevCode = splitPrevPostCode(block,apiElement.getName(),apiStatement);

        StringBuilder[] codes = splitLocalGlobalCode(prevCode[0],prevCode[1],prevCode[2]);

        List<String> returnedcontext = getGlobalContext(apiElement,codes[1],apiElement.getName());

        returnedcontext.addAll(getLocalContext(apiElement,codes[0]));

        returnedcontext.addAll(getGlobalContext(apiElement,codes[2],apiElement.getName()));

        apiElement.setContext(returnedcontext);

    }

    private static List<String>[] splitPrevPostCode(Block block, String apiElement,String apiStatement){
        List<String>[] code = new ArrayList[3];
        code[0] = new ArrayList<>();
        code[1] = new ArrayList<>();
        code[2] = new ArrayList<>();
        int prevFlag = 0;

        for (int i = 0; i< block.statements().size();i++) {
            String eachStmt = block.statements().get(i).toString();
            String[] token = eachStmt.split("\n");
            for(String eachLine:token)
            {
                if (eachLine.contains(apiStatement) && prevFlag == 0) {
                    code[1].add(eachLine.replace(apiElement,""));
                    prevFlag = 1;
                }
                else if(prevFlag == 1)
                    code[2].add(eachLine);
                else
                    code[0].add(eachLine);
            }
        }
        return code;
    }
    /*

     */
    private static StringBuilder[] splitLocalGlobalCode(List<String> prevCode, List<String>inlineCode, List<String> postCode)
    {
        StringBuilder[] codes = new StringBuilder[3];
        codes[0] = new StringBuilder();
        codes[1] = new StringBuilder();
        codes[2] = new StringBuilder();
        if (prevCode.size() > 5) {
            for (int l = prevCode.size() - 5; l < prevCode.size(); l++)
                codes[0].append(prevCode.get(l));
            for(int l = 0; l<prevCode.size() - 5;l++)
                codes[1].append(prevCode.get(l));
        }
        else {
            for (String eachPrevCode : prevCode)
                codes[0].append(eachPrevCode);
        }
        codes[0].append(inlineCode.toString());
        if (postCode.size() > 5) {
            for (int l = 0; l < 4; l++)
                codes[0].append(postCode.get(l));
            for (int l = 4; l < postCode.size(); l++)
                codes[2].append(postCode.get(l));
        }
        else {
            for (String eachPostCode : postCode)
                codes[0].append(eachPostCode);
        }

        return codes;
    }


    private static List<String> getLocalContext(APIElement apiElement, StringBuilder codes){
        List<String> localContext = new ArrayList<>();
        String code = codes.toString().replaceAll("[;(){}]"," ").replaceAll(System.getProperty("line.separator")," ");
        String[] tokens = code.trim().split(" ");

        for(String each_token:tokens)
        {
            try{
                if(each_token.trim().equalsIgnoreCase(""))
                    continue;
                if (each_token.contains("\""))
                    continue;
                if(Config.isJavaKeyword(each_token))
                    localContext.add(each_token);
                else if (each_token.startsWith(".")&& Character.isLowerCase(each_token.charAt(1)))
                    localContext.add(each_token.substring(1));
                else if (each_token.contains("="))
                    localContext.add("=");
                else if(Character.isUpperCase(each_token.charAt(0)))
                    localContext.add(each_token);
                else if (each_token.contains(".") && Character.isLowerCase(each_token.charAt(0)))
                    localContext.add(each_token.substring(each_token.indexOf(".")+1));
            }catch (Exception ignored){}
        }
        apiElement.setLocalContext(localContext);
        return localContext;
    }

    private static List<String> getGlobalContext(APIElement apiElement, StringBuilder codes, String expression) {
        List<String> globalcontext = getMethodsInvockedOnAPIElement(apiElement,codes,expression);
        globalcontext.addAll(getMethodsInWhichAPIElementParam(apiElement,codes,expression));
        return globalcontext;
    }

    private static List<String> getMethodsInvockedOnAPIElement(APIElement apiElement, StringBuilder codes, String expression)
    {
        List<String> returnedmethods = new ArrayList<>();
        String code = codes.toString().replaceAll("[;(){}]"," ").replaceAll(System.getProperty("line.separator")," ");
        String[] tokens = code.trim().split(" ");

        for(String each_token:tokens) {
            try{
                if (each_token.trim().equalsIgnoreCase(""))
                    continue;
                if (each_token.trim().contains(expression + "."))
                    returnedmethods.add(each_token.trim().substring(each_token.indexOf(".") + 1));
            }catch(Exception ignored){}
        }
        apiElement.appendGlobalContext(returnedmethods);
        return returnedmethods;
    }


    private static List<String> getMethodsInWhichAPIElementParam(APIElement apiElement, StringBuilder codes, String expression)
    {
        List<String> returnedmethods = new ArrayList<>();
        String code = codes.toString().replaceAll("[;{}]"," ").replaceAll(System.getProperty("line.separator")," ");
        String[] tokens = code.trim().split(" ");
        for(String each_token:tokens) {
            try {
                if (each_token.trim().equalsIgnoreCase("") || !each_token.trim().contains("(") || !each_token.trim().contains(")")) {}
                else if (each_token.trim().indexOf("(") == 0 && each_token.trim().contains(".")) {
                    each_token = each_token.substring(1);
                    String arguments = each_token.substring(each_token.indexOf("(") + 1, each_token.indexOf(")"));
                    List<String> argumentTokens = Arrays.asList(arguments.trim().split(","));
                    if (argumentTokens.contains(expression))
                        returnedmethods.add(each_token.substring(each_token.indexOf(".") + 1, each_token.indexOf("(")));
                }
                else if (each_token.trim().contains(".")) {
                    String arguments = each_token.substring(each_token.indexOf("(") + 1, each_token.indexOf(")"));
                    List<String> argumentTokens = Arrays.asList(arguments.trim().split(","));
                    if (argumentTokens.contains(expression))
                        returnedmethods.add(each_token.substring(each_token.indexOf(".") + 1, each_token.indexOf("(")));
                }
            } catch (Exception ignored) {
            }
        }
        apiElement.appendGlobalContext(returnedmethods);
        return returnedmethods;
    }

}
