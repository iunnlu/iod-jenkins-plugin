package io.jenkins.plugins.sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metric {
    @JsonProperty("INFO")
    private int info;

    @JsonProperty("DEVELOPMENT_ERROR")
    private int developmentError;

    @JsonProperty("ERROR_INFO")
    private int errorInfo;

    @JsonProperty("UNDEFINED")
    private int undefined;

    public Metric() {
    }

    public Metric(int info, int developmentError, int errorInfo, int undefined) {
        this.info = info;
        this.developmentError = developmentError;
        this.errorInfo = errorInfo;
        this.undefined = undefined;
    }

    public int getInfo() {
        return info;
    }

    public int getDevelopmentError() {
        return developmentError;
    }

    public int getErrorInfo() {
        return errorInfo;
    }

    public int getUndefined() {
        return undefined;
    }

    @Override
    public String toString() {
        return "{" +
                "info=" + info +
                ", developmentError=" + developmentError +
                ", errorInfo=" + errorInfo +
                ", undefined=" + undefined +
                '}';
    }
}
