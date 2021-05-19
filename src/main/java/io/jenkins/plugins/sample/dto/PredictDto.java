package io.jenkins.plugins.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.sample.model.Log;
import io.jenkins.plugins.sample.model.Metric;

import java.util.ArrayList;

public class PredictDto {
    @JsonProperty("Model name")
    private String modelName;

    @JsonProperty("Unknown class")
    private String unknownClass;

    @JsonProperty("Probability threshold")
    private float probabilityThreshold;

    @JsonProperty("Metrics")
    private Metric metrics;

    @JsonProperty("Data")
    private ArrayList<Log> data;

    public PredictDto() {
    }

    public PredictDto(String modelName, String unknownClass, float probabilityThreshold, Metric metrics, ArrayList<Log> data) {
        this.modelName = modelName;
        this.unknownClass = unknownClass;
        this.probabilityThreshold = probabilityThreshold;
        this.metrics = metrics;
        this.data = data;
    }

    public String getModelName() {
        return modelName;
    }

    public String getUnknownClass() {
        return unknownClass;
    }

    public float getProbabilityThreshold() {
        return probabilityThreshold;
    }

    public Metric getMetrics() {
        return metrics;
    }

    public ArrayList<Log> getData() {
        return data;
    }

    @Override
    public String toString() {
        String dataString = "";
        for(Log log: data) {
            dataString += log.toString();
        }
        return "{" +
                " modelName='" + modelName + '\n' +
                ", unknownClass='" + unknownClass + '\n' +
                ", probabilityThreshold=" + probabilityThreshold + '\n' +
                ", metrics=" + metrics + '\n' +
                ", data=" + dataString +
                '}';
    }
}
