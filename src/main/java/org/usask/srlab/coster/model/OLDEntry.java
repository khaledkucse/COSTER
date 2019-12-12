package org.usask.srlab.coster.model;

import java.util.Objects;

public class OLDEntry {
    String contextID;
    String context;
    String fqn;
    double score;

    public OLDEntry(String contextID, String context, String fqn, double score) {
        this.contextID = contextID;
        this.context = context;
        this.fqn = fqn;
        this.score = score;
    }

    public OLDEntry(String contextID, String context, String fqn) {
        this.contextID = contextID;
        this.context = context;
        this.fqn = fqn;
        this.score = 0.0;
    }

    public OLDEntry(String context, String fqn, double score) {
        this.context = context;
        this.fqn = fqn;
        this.score = score;
    }

    public OLDEntry() {
    }

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OLDEntry)) return false;
        OLDEntry oldEntry = (OLDEntry) o;
        return Double.compare(oldEntry.getScore(), getScore()) == 0 &&
                Objects.equals(getContextID(), oldEntry.getContextID()) &&
                Objects.equals(getContext(), oldEntry.getContext()) &&
                Objects.equals(getFqn(), oldEntry.getFqn());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getContextID(), getContext(), getFqn(), getScore());
    }

    @Override
    public String toString() {
        return "OLDEntry{" +
                "contextID='" + contextID + '\'' +
                ", context='" + context + '\'' +
                ", fqn='" + fqn + '\'' +
                ", score=" + score +
                '}';
    }
}
