package io.jenkins.plugins.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRequest {
    private final URL url;

    public CustomRequest(String url) throws IOException {
        this.url = new URL (url);
    }

    private HttpURLConnection openConnection(String method) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        return con;
    }

    private String listToJson(List<Solution> list) {
        String json = "[";
        int counter = 0;
        for(Solution s: list) {
            counter++;
            if(counter == list.size()) {
                json += s + "]";
            } else {
                json += s + ",";
            }
        }
        return json;
    }

    private List<Solution> jsonToList(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Solution> list = Arrays.asList(mapper.readValue(json, Solution[].class));
        return list;
    }

    public List<Solution> postRequest(List<Solution> list) throws IOException {
        HttpURLConnection con = openConnection("POST");
        StringBuilder response = new StringBuilder();
        String json = listToJson(list);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return jsonToList(response.toString());
    }
}
