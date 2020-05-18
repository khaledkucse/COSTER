package org.usask.srlab.coster.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExtrinsicTestResult {
    APIElement apiElement;
    List<OLDEntry> recommendations;
    long InfernceTime;

    public ExtrinsicTestResult() {
    }

    public ExtrinsicTestResult(APIElement apiElement, List<OLDEntry> recommendations, long infernceTime) {
        this.apiElement = apiElement;
        this.recommendations = recommendations;
        InfernceTime = infernceTime;
    }

    public APIElement getApiElement() {
        return apiElement;
    }

    public void setApiElement(APIElement apiElement) {
        this.apiElement = apiElement;
    }

    public List<OLDEntry> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<OLDEntry> recommendations) {
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
        if (!(o instanceof ExtrinsicTestResult)) return false;
        ExtrinsicTestResult that = (ExtrinsicTestResult) o;
        return getInfernceTime() == that.getInfernceTime() &&
                Objects.equals(getApiElement(), that.getApiElement()) &&
                Objects.equals(getRecommendations(), that.getRecommendations());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getApiElement(), getRecommendations(), getInfernceTime());
    }
}
