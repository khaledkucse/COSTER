package org.usask.srlab.coster.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IndexEntry {
    List<OLDEntry> eachFQNEntries;
    double maxScore;

    public IndexEntry(List<OLDEntry> eachFQNEntries, double maxScore) {
        this.eachFQNEntries = eachFQNEntries;
        this.maxScore = maxScore;
    }

    public IndexEntry() {
        eachFQNEntries = new ArrayList<>();
    }

    public List<OLDEntry> getEachFQNEntries() {
        return eachFQNEntries;
    }

    public void setEachFQNEntries(List<OLDEntry> eachFQNEntries) {
        this.eachFQNEntries = eachFQNEntries;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexEntry)) return false;
        IndexEntry that = (IndexEntry) o;
        return Double.compare(that.getMaxScore(), getMaxScore()) == 0 &&
                Objects.equals(getEachFQNEntries(), that.getEachFQNEntries());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getEachFQNEntries(), getMaxScore());
    }

    @Override
    public String toString() {
        return "IndexEntry{" +
                "eachFQNEntries=" + eachFQNEntries +
                ", maxScore=" + maxScore +
                '}';
    }
}
