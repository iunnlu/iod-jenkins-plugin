package io.jenkins.plugins.sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Log {

    @JsonProperty("Log")
    private String log;

    @JsonProperty("Prediction")
    private String prediction;

    @JsonProperty("Probability")
    private float probability;

    public Log() {
    }

    public Log(String log, String prediction, Float probability) {
        this.log = log;
        this.prediction = prediction;
        this.probability = probability;
    }

    public String getLog() {
        return log;
    }

    public String getPrediction() {
        return prediction;
    }

    public Float getProbability() {
        return probability;
    }

    @Override
    public String toString() {
        return "{" +
                " log='" + log + '\'' +
                ", prediction='" + prediction + '\'' +
                ", probability=" + probability +
                " }";
    }
}
