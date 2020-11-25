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
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ErrorDetectionPostBuilder extends Recorder {
    private final String[] specificWordsArr;
    private final String specificWords;
    private final String apiURL;
    private ArrayList<String> logList;
    private Map<String, String> diffLogList;

    @DataBoundConstructor
    public ErrorDetectionPostBuilder(String specificWords, String apiURL) {
        this.specificWords = specificWords;
        this.logList = new ArrayList<>();
        this.specificWordsArr = createSpecArr();
        this.diffLogList = new HashMap<>();
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
        for (int i = 0; i < arr.length; i++) {
            //arr[i] = arr[i].trim().toUpperCase();
            arr[i] = arr[i].toLowerCase();
        }
        return arr;
    }

    private void distinctLogList() {
        List<String> distinctList = logList.stream().distinct().collect(Collectors.toList());
        logList = new ArrayList<>(distinctList);
    }

    private int isSpecificField(String sentence) throws Exception {
        if (sentence.equals(null)) {
            throw new Exception("Sentence is null!");
        }
        for (int i = 0; i < specificWordsArr.length; i++) {
            if (sentence.contains("[" + specificWordsArr[i] + "]")) {
                return i;
            }
        }
        return -1;
    }

    private void addLogList(String err, String specificWord) {
        if (!err.equals(" ")) {
            if (err.contains("\"")) {
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

    private void extractErrorsFromLog(BufferedReader reader) {
        String str = null;
        int index = 0;
        try {
            while ((str = reader.readLine()) != null) {
                if ((index = isSpecificField(str)) >= 0) {
                    String specificWord = specificWordsArr[index];
                    String[] arr = str.split("\\[" + specificWord + "]");
                    addLogList(arr[1], specificWord);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private int isSpecificFieldDiffVersion(String sentence) throws Exception {
        for (int i = 0; i < specificWordsArr.length; i++) {
            if (sentence.contains(specificWordsArr[i])) {
                return i;
            }
        }
        return -1;
    }

    //[8mha - [0m

    private void extractErrorsFromLogDiffVersion(BufferedReader reader) throws FileNotFoundException {
        String str = null;
        String newStr = null;
        int index = 0;
        int counter = 0;
        int lineCounter = 1;

        List<String> colorTexts;
        colorTexts = new ArrayList<>();
        colorTexts.add("[8mha");
        colorTexts.add("4K8+MEeuyHJhLasZBij7cZROXTmZ111uOsXx8k9031x7AAAAYB+LCAAAAAAAAP9b85aBtbiIQSmjNKU4P0+vJLE4u1gvPjexLDVPzxdEGvvmZ+X75ZekLlOVfvTjc8FPJgaGiiIGKaiG5Py84vycVD1nCA1SyAABjCCFBQCV27OjYAAAAA");
        colorTexts.add("4B2qLqJ8jP38q3bgYOEfyhudkTpsedqKcqkLDxklYB7DAAAAYx+LCAAAAAAAAP9b85aBtbiIQSWjNKU4P0+vJLE4u1gvPjexLDVPzxdEhicW5WXmpfvll6S2fNly5fzGzauYGBgqihikoFqS8/OK83NS9ZwhNEghAwQwghQWAACwxA+XYgAAAA");
        colorTexts.add("4Db30S3CEbT25RWG6kmP/RjK+a9z5IChL/ZhxYyQosLJAAAAYB+LCAAAAAAAAP9b85aBtbiIQSmjNKU4P0+vJLE4u1gvPjexLDVPzxdEuhYV5Rf55ZekOlc7RKnPKH7IxMBQUcQgBdWQnJ9XnJ+TqucMoUEKGSCAEaSwAACsNFCqYAAAAA");
        colorTexts.add(":");
        colorTexts.add("=");
        colorTexts.add("/");
        colorTexts.add("\u001B");
        colorTexts.add("[0m");

        File obj = new File("numeratedLog.txt");

        try {
            if (obj.createNewFile()) {
                System.out.println("File created: " + obj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(obj);
            while ((str = reader.readLine()) != null) {
                for(String colorText: colorTexts) {
                    if(str.contains(colorText)) {
                        str = str.replace(colorText, "");
                    }
                }
                myWriter.write(lineCounter + " # " + str + "\n");
                lineCounter++;
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        Scanner fileReader = new Scanner(obj);

        try {
            while (fileReader.hasNextLine()) {
                str = fileReader.nextLine().toLowerCase();
                if (!str.contains("warning")) {
                    if (str.contains("error:")) {
                        String[] arr = str.split("#");
                        String err = arr[1].trim();
                        if (diffLogList.size() == 0) {
                            diffLogList.put(err, arr[0]);
                        } else {
                            if (diffLogList.keySet().contains(err)) {
                                for (String s : diffLogList.keySet()) {
                                    if (s.equals(err)) {
                                        diffLogList.put(s, diffLogList.get(s) + ", " + arr[0]);
                                    }
                                }
                            } else {
                                diffLogList.put(err, arr[0]);
                            }
                        }
                        counter++;
                    } else {
                        if (isSpecificFieldDiffVersion(str) >= 0) {
                            String[] arr = str.split("#");
                            String err = arr[1].trim();
                            if (diffLogList.size() == 0) {
                                diffLogList.put(err, arr[0]);
                            } else {
                                if (diffLogList.keySet().contains(err)) {
                                    for (String k : diffLogList.keySet()) {
                                        if (k.equals(err)) {
                                            diffLogList.put(k, diffLogList.get(k) + ", " + arr[0]);
                                        }
                                    }
                                } else {
                                    diffLogList.put(err, arr[0]);
                                }
                            }
                            counter++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void getLastSpecificLogs(AbstractBuild<?, ?> build) {
        Run lastBuild = getLastBuild(build);
        try {
            InputStream inputStream = lastBuild.getLogInputStream();
            InputStreamReader isReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isReader);
            extractErrorsFromLogDiffVersion(reader);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        distinctLogList();
    }

    private List<Solution> createSolutionList() {
        List<Solution> solutionsList = new ArrayList<>();
        int counter = 1;
        for (String s : logList) {
            solutionsList.add(new Solution(counter, s, "solution"));
            counter++;
        }
        return solutionsList;
    }

    private List<Solution> postReq() {
        List<Solution> responseList = null;
        try {
            CustomRequest req = new CustomRequest(apiURL);
            List<Solution> solutionList = createSolutionList();
            responseList = req.postRequest(solutionList);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return responseList;
    }

    private void readMap() {
        for(String s: diffLogList.keySet()) {
            System.out.println(s + "->" + diffLogList.get(s));
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        getLastSpecificLogs(build);
        readMap();
        //List<Solution> responseList = postReq();
        //build.addAction(new ErrorFileAction(responseList));
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public FormValidation doCheckSpecificWords(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please enter a string!");
            }
            return FormValidation.ok();
        }

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
