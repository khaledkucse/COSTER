package org.usask.srlab.coster.train;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.usask.srlab.coster.config.Config;
import org.usask.srlab.coster.utils.FileUtil;
import org.usask.srlab.coster.utils.NotifyingBlockingThreadPoolExecutor;
import org.usask.srlab.coster.utils.ParseUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import static org.usask.srlab.coster.utils.ParseUtil.getFilteredRecursiveFiles;

public class CalculateAPIUsages {
    private static final int THREAD_POOL_SIZE = 8;

    private static final Callable<Boolean> blockingTimeoutCallback = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return true; // keep waiting
        }
    };
    private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);

    private static final AtomicInteger numofCompilationUnit = new AtomicInteger(0), numOfProjects = new AtomicInteger(0), numOfSourceFiles = new AtomicInteger(0), numOfClasses = new AtomicInteger(0), numOfMethods = new AtomicInteger(0), numOfFields = new AtomicInteger(0);

    private final static HashSet<String> fqns = new HashSet<>();

    private static String logFilePath = Config.LOG_PATH+"coster_apicount.log";

    private static Map<String, Long> fqnCount = new HashMap<>();
    private static  String[] jarPath;

    private static void print(Object s){System.out.println(s.toString());}

    /**
     * The only public method of the class that creates dictonary.
     * @param repoPath: path of the repository/subject systems
     * @param jarRepoPath: path of the jar repository
     * @param repoDictPath: Path to write the dictonary of the subject systems files
    //* @param repositoryPath: path of the repository where all the subject systems are reside.
     */
    //public static void createDictonary(String libDirPath, String dictonaryOutpath, String repositoryPath) {
    private static void createDictonary(String repoPath, String jarRepoPath, String repoDictPath) {

        jarPath = collectJarFiles(new File(jarRepoPath));
        extractFromSource(new File(repoPath),repoDictPath);

        print("Total Projects: " + numOfProjects);
        print("Total Source Files: " + numOfSourceFiles);
        print("Total Compilation Unit: " + numofCompilationUnit);
        print("Total Classes/Types: " + numOfClasses);
        print("Total Methods: " + numOfMethods);
        print("Total Fields: " + numOfFields);
    }
    /**
     * method that extract source code from the project.
     * @param repository: Path of the repository
     * @param dictonaryOutpath: Path where dictonary will be written
     */
    private static void extractFromSource(final File repository, String dictonaryOutpath) {
        if (repository.isDirectory()) {
            if (new File(repository, ".git").exists() || new File(repository, ".gitignore").exists()|| new File(repository, "pom.xml").exists()|| new File(repository, "README.md").exists()) {
                final AtomicInteger curProjectNum = numOfProjects;
                numOfProjects.incrementAndGet();
                final File out = new File(dictonaryOutpath);
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CalculateAPIUsages calculateAPIUsages = new CalculateAPIUsages();
                            calculateAPIUsages.extractFromSourceProject(repository);

                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"+++++++++++++++++++++++++++++++++++++++++++++");
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Project Number: " + curProjectNum);
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Project Name:" + repository.getAbsolutePath());
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Source Files: " + numOfSourceFiles);
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Compilation Units: " + numofCompilationUnit);
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Classes: " + numOfClasses);
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Methods: " + numOfMethods);
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Fields: " + numOfFields);

                            sortandToString();
                            FileUtil.getSingleTonFileUtilInst().writeToFile(new File(out, repository.getParentFile().getName() + "___" + repository.getName() + "-FQN").getAbsolutePath(), calculateAPIUsages.fqns);
                        } catch (Throwable t) {
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"+++++++++++++++++++++++++++++++++++++++++++++");
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Project Number: " + curProjectNum);
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Project Name:" + repository.getAbsolutePath());
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,"Error in parsing project " + repository.getAbsolutePath());
                            FileUtil.getSingleTonFileUtilInst().appendLineToFile(logFilePath,t.getMessage());
                        }
                    }
                });
            }
            else
                for (File sub : repository.listFiles())
                    extractFromSource(sub,dictonaryOutpath);
        }
    }

    /**
     * Method that bind from a single project file
     * @param projectFile: Path of the project file
     */
    private void extractFromSourceProject(File projectFile) {

        String[] sources = { projectFile.getAbsolutePath() };


        ArrayList<File> files = ParseUtil.collectJavaFiles(projectFile);
        String[] sourceCodePaths = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            sourceCodePaths[i] = files.get(i).getAbsolutePath();
        }

        numOfSourceFiles.addAndGet(sourceCodePaths.length);

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
        parser.setEnvironment(jarPath == null ? new String[0] : jarPath, sources, new String[]{"UTF-8"}, true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        parser.createASTs(sourceCodePaths, null, new String[0], r, null);

        numofCompilationUnit.addAndGet(cus.size());

        for (CompilationUnit cu : cus)
            for (int i = 0 ; i < cu.types().size(); i++){
                System.gc();
                extractTypeDecleration((AbstractTypeDeclaration) cu.types().get(i), cu.getPackage() == null ? "" : cu.getPackage().getName().getFullyQualifiedName() + ".");
            }

    }

    /**
     * Method the extract the type decleration from a .java file
     * @param type: The class name
     * @param prefix: String having previous value while recursing
     */
    private void extractTypeDecleration(AbstractTypeDeclaration type, String prefix) {

        ITypeBinding tb = type.resolveBinding();
        if (tb == null){
            return;
        }

        tb = tb.getTypeDeclaration();
        if (tb == null)
            return;
        if (tb.isAnonymous() || tb.isLocal())
            return;
        numOfClasses.incrementAndGet();
        for (int i = 0; i < type.bodyDeclarations().size(); i++) {
            BodyDeclaration bd = (BodyDeclaration) type.bodyDeclarations().get(i);
            if (bd instanceof FieldDeclaration)
                extractFieldDecleration((FieldDeclaration) bd);
            else if (bd instanceof MethodDeclaration)
                extractMethodDecleration((MethodDeclaration) bd);
            else if (bd instanceof AbstractTypeDeclaration)
                extractTypeDecleration((AbstractTypeDeclaration) bd, prefix);
        }
    }

    /**
     * Method the extract the field decleration from a .java file
     * @param fieldDeclerration: Field Decleration Node
     */
    private void extractFieldDecleration(FieldDeclaration fieldDeclerration) {
        for (int j = 0; j < fieldDeclerration.fragments().size(); j++) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) fieldDeclerration.fragments().get(j);
            IVariableBinding vb = vdf.resolveBinding();
            if (vb == null || vb.getType().isFromSource())
                continue;
            vb = vb.getVariableDeclaration();
            String fqn = getQualifiedName(vb.getType().getTypeDeclaration());
            if(fqn.contains(".")){
                addFQNCount(fqn);
                numOfFields.incrementAndGet();
            }
        }
    }


    /**
     * Method the extract the method decleration from a .java file
     * @param method: Method Decleration node
     */
    private void extractMethodDecleration(MethodDeclaration method) {
        Block block = method.getBody();

        block.accept(new ASTVisitor() {

            //Visit every field/variable decleration
            @Override
            public boolean visit(VariableDeclarationFragment varDecNode) {
                IVariableBinding variableBinding = varDecNode.resolveBinding();
                if (variableBinding != null && !variableBinding.getType().isFromSource()) {
                    String fqn = getQualifiedName(variableBinding.getType().getTypeDeclaration());
                    if(fqn.contains(".")){
                        addFQNCount(fqn);
                        numOfFields.incrementAndGet();
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
                        String fqn = getQualifiedName(typeBinding.getTypeDeclaration());
                        if(fqn.contains(".")){
                            addFQNCount(fqn);
                            numOfMethods.incrementAndGet();
                        }
                        System.gc();
                    }
                }
                return true;
            }

        });
    }
    private static String getQualifiedName(ITypeBinding tb) {
        if (tb.isArray()) {
            return tb.getElementType().getTypeDeclaration().getQualifiedName();
        } else
            return tb.getQualifiedName();
    }

    private static String[] collectJarFiles(File jarRepository){
        String[] extensions = { ".jar" };
        HashMap<String, List<File>> allJarFiles = getFilteredRecursiveFiles(jarRepository, extensions);

        List<File> arrJars = allJarFiles.get(".jar");
        if (arrJars == null) {
            arrJars = new ArrayList<>();
        }
        String[] jarPath = new String[arrJars.size() + 1];
        jarPath[0] = System.getProperty("java.home") +File.separator+ "lib"+File.separator+"rt.jar";

        for (int i = 0; i < arrJars.size(); i++) {
            jarPath[i + 1] = arrJars.get(i).getAbsolutePath();
        }

        return jarPath;
    }

    private static void addFQNCount(String FQN) {
        if(fqnCount.containsKey(FQN)) {
            long curValue = fqnCount.get(FQN);
            curValue +=1;
            fqnCount.put(FQN,curValue);
        }
        else
            fqnCount.put(FQN, (long) 1);
    }

    private static void sortandToString() {
        for(Map.Entry eachEntry:fqnCount.entrySet()) {
            String key = (String) eachEntry.getKey();
            long value = (long) eachEntry.getValue();
            fqns.add(key+"\t"+value);

        }
    }

    public static void main(String[] args) {
        CalculateAPIUsages.createDictonary(Config.GITHUB_SUBJECT_SYSTEM_PATH,Config.FULL_JAR_PATH,Config.SUBJECT_SYSTEM_DICTONARY_PATH);
    }
}
