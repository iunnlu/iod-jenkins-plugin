package io.jenkins.plugins.sample;

public class Solution {
    private int id;
    private String error;
    private String solution;

    public Solution(int id, String error, String solution) {
        this.id = id;
        this.error = error;
        this.solution = solution;
    }

    public Solution() { }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return '{' +
                "\"id\": " + id +
                ", \"error\": \"" + error + "\"" +
                ", \"solution\": \"" + solution + "\"" +
                '}';
    }
}
