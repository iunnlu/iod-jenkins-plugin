package io.jenkins.plugins.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.*;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.tasks.BuildStepDescriptor;
import io.jenkins.plugins.sample.dto.LoginDto;
import io.jenkins.plugins.sample.dto.PredictDto;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ErrorDetectionPostBuilder extends Recorder {
    private final String apiURL;
    private ArrayList<String> logList;
    String username;
    String password;

    @DataBoundConstructor
    public ErrorDetectionPostBuilder(String apiURL, String username, String password) {
        this.logList = new ArrayList<>();
        this.apiURL = apiURL;
        this.username = username;
        this.password = password;
    }

    public String getApiURL() {
        return apiURL;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private void distinctLogList() {
        List<String> distinctList = logList.stream().distinct().collect(Collectors.toList());
        logList = new ArrayList<>(distinctList);
    }

    private String removeRedundantField(String line) {
        if(line.contains("==")) {
            String[] splitedLine = line.split("==");
            return splitedLine[1];
        }
        return line;
    }

    private void addLogList(String line) {
        if (!line.equals(" ")) {
            String newLine = removeRedundantField(line);
            logList.add(newLine);
        }
    }

    private Run getLastBuild(AbstractBuild<?, ?> build) {
        Job<?, ?> job = build.getParent();
        return job.getLastBuild();
    }

    private void getLastSpecificLogs(AbstractBuild<?, ?> build) {
        Run lastBuild = getLastBuild(build);
        try {
            InputStream inputStream = lastBuild.getLogInputStream();
            InputStreamReader isReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isReader);
            String str = null;
            while ((str = reader.readLine()) != null) {
                addLogList(str);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String postReq(String token) throws IOException, InterruptedException {
        String req = "";
        String uri = "/predict?model_name=default";
        for(String log: logList) {
            req += log + "\n";
        }
        String url = apiURL + uri;
        CustomRequest customRequest = new CustomRequest(url);
        String response = customRequest.post(req, token);
        return response;
    }

    private PredictDto postReq() throws IOException, InterruptedException {
        Map<Object, Object> data = new HashMap<>();
        data.put("username", this.username);
        data.put("password", this.password);
        String uri = "/auth/login";
        String url = apiURL + uri;
        CustomRequest req = new CustomRequest(url);
        String response = req.post(data);
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(response, LoginDto.class);
        String token = loginDto.getAccess();

        String secondResponse = postReq(token);
        PredictDto predictDto = objectMapper.readValue(secondResponse, PredictDto.class);
        return predictDto;
    }

    private void readList() {
        for(String s: logList) {
            System.out.println(s);
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        getLastSpecificLogs(build);
        PredictDto response = postReq();
        build.addAction(new ErrorFileAction(response));
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public FormValidation doCheckApiURL(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please enter a string!");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Error Detection";
        }

    }
}
