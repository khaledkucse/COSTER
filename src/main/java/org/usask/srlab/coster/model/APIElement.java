package org.usask.srlab.coster.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class APIElement {
    private String name;
    private String fileName;
    private int lineNumber;
    private List<String> context;
    private List<String> localContext;
    private List<String> globalContext;
    private String actualFQN;

    public APIElement() {
        context = new ArrayList<>();
        localContext = new ArrayList<>();
        globalContext = new ArrayList<>();
    }

    public APIElement(String name,String fileName, int lineNumber, String actualFQN) {
        this.name = name;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.actualFQN = actualFQN;
        context = new ArrayList<>();
        localContext = new ArrayList<>();
        globalContext = new ArrayList<>();
    }

    public APIElement(String name, String fileName, int lineNumber, List<String> context, List<String> localContext, List<String> globalContext, String actualFQN) {
        this.name = name;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.context = context;
        this.localContext = localContext;
        this.globalContext = globalContext;
        this.actualFQN = actualFQN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<String> getContext() {
        return context;
    }

    public void setContext(List<String> context) {
        this.context = context;
    }

    public List<String> getLocalContext() {
        return localContext;
    }

    public void setLocalContext(List<String> localContext) {
        this.localContext = localContext;
    }

    public List<String> getGlobalContext() {
        return globalContext;
    }

    public void setGlobalContext(List<String> globalContext) {
        this.globalContext = globalContext;
    }
    public void appendGlobalContext(List<String> globalContext) {
        this.globalContext.addAll(globalContext);
    }

    public String getActualFQN() {
        return actualFQN;
    }

    public void setActualFQN(String actualFQN) {
        this.actualFQN = actualFQN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APIElement)) return false;
        APIElement that = (APIElement) o;
        return getLineNumber() == that.getLineNumber() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFileName(), that.getFileName()) &&
                Objects.equals(getContext(), that.getContext()) &&
                Objects.equals(getLocalContext(), that.getLocalContext()) &&
                Objects.equals(getGlobalContext(), that.getGlobalContext()) &&
                Objects.equals(getActualFQN(), that.getActualFQN());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName(), getFileName(), getLineNumber(), getContext(), getLocalContext(), getGlobalContext(), getActualFQN());
    }

    public String toString(){
        return "Name :"+name
                +"\nFileName: "+fileName
                +"\nLineNumber: "+lineNumber
                +"\nLocalContext: "+ localContext.toString()
                +"\nGlobalContext: "+ globalContext.toString()
                +"\nActualFQN: "+actualFQN
                +"\n";
    }
}
