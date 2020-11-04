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
    private final String specWords;
    private final String[] specificWords;
    private ArrayList<String> logList;

    @DataBoundConstructor
    public ErrorDetectionPostBuilder(String specificWords) {
        this.specWords = specificWords;
        this.specificWords = specificWords.split(",");
        this.logList = new ArrayList<>();
    }

    public String getSpecificWords() {
        return this.specWords;
    }

    private int isSpecificField(String sentence) {
        for(int i=0; i<specificWords.length; i++) {
            if(sentence.contains("[" + specificWords[i] + "]")) {
                return i;
            }
        }
        return -1;
    }

    private Run getLastBuild(AbstractBuild<?, ?> build) {
        Job<?, ?> job = build.getParent();
        return job.getLastBuild();
    }

    private void getLastSpecificLogs(Run lastBuild) throws IOException {
        InputStream inputStream = lastBuild.getLogInputStream();
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);
        String str;
        int index;
        while((str = reader.readLine())!= null){
            if((index = isSpecificField(str)) >= 0) {
                String specificWord = specificWords[index];
                String[] arr = str.split("\\[" + specificWord + "]");
                if(!arr[1].equals(" ")) {
                    if(arr[1].contains("\"")){
                        logList.add("[" + specificWord + "]" + arr[1].replace("\"", "'"));
                    } else {
                        logList.add("[" + specificWord + "]" + arr[1]);
                    }
                }
            }
        }
    }

    private void distinctLogList() {
        List<String> distinctList = logList.stream().distinct().collect(Collectors.toList());
        logList = new ArrayList<>(distinctList);
    }

    private void writeConsole(BuildListener listener) {
        //listener.getLogger().println(lastErrorLog);
    }

    private List<Solution> sendList() {
        List<Solution> list = new ArrayList<>();
        int counter = 1;
        for(String s: logList) {
            list.add(new Solution(counter, s, "solution"));
            counter++;
        }
        return list;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        getLastSpecificLogs(getLastBuild(build));
        distinctLogList();
        CustomRequest req = new CustomRequest("http://localhost:3000/errors");
        List<Solution> solList = sendList();
        List<Solution> list = req.postRequest(solList);
        build.addAction(new ErrorFileAction(list));
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public FormValidation doCheckSpecificWords(@QueryParameter String value)
                throws IOException, ServletException {
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
