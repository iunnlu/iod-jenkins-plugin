package io.jenkins.plugins.sample;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.*;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorDetectionPostBuilder extends Recorder {
    private final String[] specificWordsArr;
    private final String specificWords;
    private final String apiURL;
    private ArrayList<String> logList;

    @DataBoundConstructor
    public ErrorDetectionPostBuilder(String specificWords, String apiURL) {
        this.specificWords = specificWords;
        this.logList = new ArrayList<>();
        this.specificWordsArr = createSpecArr();
        this.apiURL = apiURL;
    }

    public String getSpecificWords() {
        return this.specificWords;
    }

    public String getApiURL() {
        return apiURL;
    }

    private String[] createSpecArr() {
        String[] arr = this.specificWords.split(",");
        for(int i=0; i<arr.length; i++) {
            arr[i] = arr[i].trim().toUpperCase();
        }
        return arr;
    }

    private void distinctLogList() {
        List<String> distinctList = logList.stream().distinct().collect(Collectors.toList());
        logList = new ArrayList<>(distinctList);
    }

    private int isSpecificField(String sentence) throws Exception {
        if(sentence.equals(null)) {
            throw new Exception("Sentence is null!");
        }
        for(int i=0; i<specificWordsArr.length; i++) {
            if(sentence.contains("[" + specificWordsArr[i] + "]")) {
                return i;
            }
        }
        return -1;
    }

    private void addLogList(String err, String specificWord) {
        if(!err.equals(" ")) {
            if(err.contains("\"")){
                logList.add("[" + specificWord + "]" + err.replace("\"", "'"));
            } else {
                logList.add("[" + specificWord + "]" + err);
            }
        }
    }

    private Run getLastBuild(AbstractBuild<?, ?> build) {
        Job<?, ?> job = build.getParent();
        return job.getLastBuild();
    }

    private void getLastSpecificLogs(AbstractBuild<?, ?> build) {
        Run lastBuild = getLastBuild(build);
        try{
            InputStream inputStream = lastBuild.getLogInputStream();
            InputStreamReader isReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isReader);
            String str;
            int index;
            while((str = reader.readLine())!= null){
                if((index = isSpecificField(str)) >= 0) {
                    String specificWord = specificWordsArr[index];
                    String[] arr = str.split("\\[" + specificWord + "]");
                    addLogList(arr[1], specificWord);
                }
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private List<Solution> createSolutionList() {
        List<Solution> solutionsList = new ArrayList<>();
        int counter = 1;
        for(String s: logList) {
            solutionsList.add(new Solution(counter, s, "solution"));
            counter++;
        }
        return solutionsList;
    }

    private List<Solution> postReq() {
        List<Solution> responseList = null;
        try{
            CustomRequest req = new CustomRequest(apiURL);
            List<Solution> solutionList = createSolutionList();
            responseList = req.postRequest(solutionList);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return responseList;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        getLastSpecificLogs(build);
        List<Solution> responseList = postReq();
        build.addAction(new ErrorFileAction(responseList));
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public FormValidation doCheckSpecificWords(@QueryParameter String value)
                throws IOException, ServletException {
            if(value.length() == 0) {
                return FormValidation.error("Please enter a string!");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApiURL(@QueryParameter String value)
                throws IOException, ServletException {
            if(value.length() == 0) {
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
