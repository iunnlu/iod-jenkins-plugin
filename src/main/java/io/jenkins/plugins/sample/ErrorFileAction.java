package io.jenkins.plugins.sample;

import hudson.model.Action;

import java.util.ArrayList;
import java.util.List;

public class ErrorFileAction implements Action {
    private List content;

    public ErrorFileAction(List<Solution> content) {
        this.content = content;
    }

    public List getContent() {
        return content;
    }

    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @Override
    public String getDisplayName() {
        return "Response File";
    }

    @Override
    public String getUrlName() {
        return "response-file";
    }
}
