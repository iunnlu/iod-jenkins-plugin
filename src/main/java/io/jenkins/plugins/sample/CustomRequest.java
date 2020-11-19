package io.jenkins.plugins.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRequest {
    private final URL url;

    public CustomRequest(String url) throws MalformedURLException {
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
        JSONArray arr = new JSONArray();
        for(Solution s: list) {
            JSONObject obj = new JSONObject();
            obj.put("id", s.getId());
            obj.put("error", s.getError());
            obj.put("solution", s.getSolution());
            arr.add(obj);
        }
        return arr.toString();
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
