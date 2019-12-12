package org.usask.srlab.coster.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Output {
    private APIElement apiElement;
    private List<String> reccomendation;
    private int rank;

    public Output() {
    }

    public Output(APIElement apiElement) {
        this.apiElement = apiElement;
        reccomendation = new ArrayList<>();
    }

    public Output(APIElement apiElement, List<String> reccomendation, int rank) {
        this.apiElement = apiElement;
        this.reccomendation = reccomendation;
        this.rank = rank;
    }

    public APIElement getApiElement() {
        return apiElement;
    }

    public void setApiElement(APIElement apiElement) {
        this.apiElement = apiElement;
    }

    public List<String> getReccomendation() {
        return reccomendation;
    }

    public void setReccomendation(List<String> reccomendation) {
        this.reccomendation = reccomendation;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Output)) return false;
        Output output = (Output) o;
        return getRank() == output.getRank() &&
                Objects.equals(getApiElement(), output.getApiElement()) &&
                Objects.equals(getReccomendation(), output.getReccomendation());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getApiElement(), getReccomendation(), getRank());
    }

    @Override
    public String toString() {
        return "\nRank :"+this.rank
                +"\n"+apiElement.toString()
                +"\nReccomendation: "+reccomendation.toString()
                +"\n";
    }
}
