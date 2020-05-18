package org.usask.srlab.coster.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileTestResult {
    APIElement apiElement;
    Map<String, Double> recommendations;
    long InfernceTime;

    public FileTestResult(APIElement apiElement, Map<String, Double> recommendations, long infernceTime) {
        this.apiElement = apiElement;
        this.recommendations = recommendations;
        InfernceTime = infernceTime;
    }

    public FileTestResult() {
        this.recommendations = new HashMap<>();
    }

    public APIElement getApiElement() {
        return apiElement;
    }

    public void setApiElement(APIElement apiElement) {
        this.apiElement = apiElement;
    }

    public Map<String, Double> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Map<String, Double> recommendations) {
        this.recommendations = recommendations;
    }

    public long getInfernceTime() {
        return InfernceTime;
    }

    public void setInfernceTime(long infernceTime) {
        InfernceTime = infernceTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileTestResult)) return false;
        FileTestResult that = (FileTestResult) o;
        return Objects.equals(getApiElement(), that.getApiElement()) &&
                Objects.equals(getRecommendations(), that.getRecommendations()) &&
                Objects.equals(getInfernceTime(), that.getInfernceTime());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getApiElement(), getRecommendations(), getInfernceTime());
    }

    @Override
    public String toString() {
        return "FileTestResult{" +
                "apiElement=" + apiElement +
                ", recommendations=" + recommendations +
                ", InfernceTime=" + InfernceTime +
                '}';
    }
}
