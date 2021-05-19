package io.jenkins.plugins.sample;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class CustomRequest {
    private final String url;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public CustomRequest(String url) throws MalformedURLException {
        this.url = url;
    }

    private HttpRequest openConnection(Map<Object, Object> data) throws IOException {
        return HttpRequest.newBuilder()
                .POST(ofFormData(data))
                .uri(URI.create(this.url))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }

    private HttpRequest openConnection(String data, String token) throws IOException {
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .uri(URI.create(this.url))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Authorization", "Bearer " + token)
                .header("Content-Type", "text/plain")
                .build();
    }

    public HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    public String post(Map<Object, Object> data) throws InterruptedException, IOException {
        HttpResponse<String> response = httpClient.send(openConnection(data), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String post(String data, String token) throws InterruptedException, IOException {
        HttpResponse<String> response = httpClient.send(openConnection(data, token), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
