package org.usask.srlab.coster.extraction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import org.usask.srlab.coster.model.APIElement;

public class NonCompilableCodeExtraction {
    private static final Logger logger = LogManager.getLogger(NonCompilableCodeExtraction.class.getName()); // logger variable for loggin in the file
    private static final DecimalFormat df = new DecimalFormat(); // Decimal formet variable for formating decimal into 2 digits
    private static void print(Object s){System.out.println(s.toString());}


    public static List<APIElement> extractCode(String src, String[] jarPaths, String inputfilepath){
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setBindingsRecovery(true);
        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);
        parser.setUnitName("test");

        String[] sources = { };

        parser.setEnvironment(jarPaths, sources, new String[] { }, true);
        parser.setSource(src.toCharArray());
        final Block block = (Block) parser.createAST(null);


        List<APIElement> apiElements = new ArrayList<>();
        block.accept(new ASTVisitor() {
            Map<String,APIElement> identifiers = new HashMap<>();

            //Visit every field/variable decleration
            @Override
            public boolean visit(VariableDeclarationFragment varDecNode) {
                SimpleName varName = varDecNode.getName();
                String apiElement = varName.getIdentifier();
                int linenumber = varDecNode.getStartPosition();
                String actualFQN = "";
                APIElement fieldDecleration = new APIElement(apiElement,inputfilepath,linenumber,actualFQN);

                CompilableCodeExtraction.getFieldContext(fieldDecleration,block,varDecNode.toString());
                this.identifiers.put(apiElement,fieldDecleration);

//                if(fieldDecleration.getContext().size() > 5)
                apiElements.add(fieldDecleration);
                System.gc();
                return true;
            }

            //Visit every field/variable usages
            @Override
            public boolean visit(Assignment assignmentNode) {
                if(assignmentNode.getLeftHandSide() instanceof SimpleName){
                    SimpleName varName = (SimpleName) assignmentNode.getLeftHandSide();
                    String apiElement = varName.getIdentifier();
                    if (!identifiers.containsKey(apiElement)){
                        int linenumber = assignmentNode.getStartPosition();
                        String actualFQN = "";
                        APIElement fieldImplementation = new APIElement(apiElement,inputfilepath,linenumber,actualFQN);

                        CompilableCodeExtraction.getFieldContext(fieldImplementation,block,assignmentNode.toString());
                        this.identifiers.put(apiElement,fieldImplementation);

                        if(fieldImplementation.getContext().size() > 5)
                            apiElements.add(fieldImplementation);

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
                    String apiElement = invocationnode.toString();
                    String apiExpression = expression.toString()+"."+invocationnode.getName().getIdentifier();
                    int linenumber = invocationnode.getStartPosition();
                    String actualFQN = "";
                    APIElement methodInvocation = new APIElement(apiElement,inputfilepath,linenumber,actualFQN);
                    CompilableCodeExtraction.getMethodContext(methodInvocation,block,apiExpression,expression.toString());
                    if(methodInvocation.getContext().size() > 5)
                        apiElements.add(methodInvocation);
                    System.gc();
                    }

                return true;
            }

        });
        return apiElements;
    }
}
