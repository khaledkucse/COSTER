package org.usask.srlab.coster.test;

public class GarbageCode {
    /*//Visit all class variable
                public boolean visit(final FieldDeclaration fieldNode) {
                    ITypeBinding fieldBinding = fieldNode.getType().resolveBinding();
                    if (fieldBinding != null && !fieldBinding.isFromSource()) {
                        fieldNode.accept(new ASTVisitor() {
                            public boolean visit(Modifier modifierNode) {
                                globalContexts = addToContext(globalContexts,cu.getLineNumber(modifierNode.getStartPosition()),modifierNode.toString());
                                return super.visit(modifierNode);
                            }
                        });
                        globalContexts = addToContext(globalContexts,cu.getLineNumber(fieldNode.getStartPosition()),fieldBinding.getName());
                        List fragement = fieldNode.fragments();
                        if(fragement.size() == 1) {
                            String fieldname = fragement.get(0).toString();
                            if (fragement.get(0).toString().contains("=")){
                                fieldname = fragement.get(0).toString().substring(0,fragement.get(0).toString().indexOf('='));
                            }

                            APIElement apiElement = new APIElement(fieldname,compileUnit.getFilePath(),cu.getLineNumber(fieldNode.getStartPosition()),fieldBinding.getQualifiedName());
                            apiElements.add(apiElement);
                            if (fragement.get(0).toString().contains("="))
                                globalContexts = addToContext(globalContexts,cu.getLineNumber(fieldNode.getStartPosition()),"=");
                            if(fragement.get(0).toString().contains("new"))
                                globalContexts = addToContext(globalContexts,cu.getLineNumber(fieldNode.getStartPosition()),"new");
                        }
                        else {
                            for(Object eachfield:fragement)
                            {
                                String fieldname = eachfield.toString();
                                if (eachfield.toString().contains("="))
                                    fieldname = eachfield.toString().substring(0,eachfield.toString().indexOf('='));
                                APIElement apiElement = new APIElement(fieldname,compileUnit.getFilePath(),cu.getLineNumber(fieldNode.getStartPosition()),fieldBinding.getQualifiedName());
                                apiElements.add(apiElement);
                            }
                        }


                        fieldNode.accept(new ASTVisitor() {
                            @Override
                            public boolean visit(ClassInstanceCreation node) {
                                IMethodBinding constructorBinding = node.resolveConstructorBinding();
                                if(constructorBinding != null){
                                    globalContexts = addToContext(globalContexts,cu.getLineNumber(fieldNode.getStartPosition()),constructorBinding.getName());
                                }
                                return super.visit(node);
                            }
                        });
                    }
                    return super.visit(fieldNode);
                }*/




    //                                        List<String> reccList = getReccomendation(actualFQN, typeBinding.getPackage().getName(), allfqns);
//
//                                        StringBuilder eachTestCase = new StringBuilder("Rank: " + rank + "\n");
//                                        eachTestCase.append("File Name: ").append(compileUnit.getFilePath()).append("\n");
//                                        eachTestCase.append("Position: ").append(linenumber).append("\n");
//                                        eachTestCase.append("Source Code: \n").append(block.statements().toString());
//                                        eachTestCase.append("Context: \n").append(context.toString()).append("\n");
//                                        eachTestCase.append("API Element: ").append(invocationnode.toString()).append("\n");
//                                        eachTestCase.append("Actual FQNs: ").append(actualFQN).append("\nReccomedation:\n");
//                                        for (String eachrecc : reccList)
//                                            eachTestCase.append(eachrecc).append("\n");
//                                        results.add(eachTestCase.toString());
}
