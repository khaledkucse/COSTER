package org.usask.srlab.coster.model;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Objects;

public class CompileUnit {
    private String filePath;
    CompilationUnit compilationUnit;

    public CompileUnit(String filePath, CompilationUnit compilationUnit) {
        this.filePath = filePath;
        this.compilationUnit = compilationUnit;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void setCompilationUnit(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompileUnit)) return false;
        CompileUnit that = (CompileUnit) o;
        return Objects.equals(getFilePath(), that.getFilePath()) &&
                Objects.equals(getCompilationUnit(), that.getCompilationUnit());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getFilePath(), getCompilationUnit());
    }

    @Override
    public String toString() {
        return "File Path :"+filePath
                +"\nCompilation Unit: "+compilationUnit
                +"\n";
    }
}
