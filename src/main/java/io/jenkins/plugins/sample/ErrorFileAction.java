package io.jenkins.plugins.sample;

import hudson.model.Action;
import io.jenkins.plugins.sample.dto.PredictDto;

public class ErrorFileAction implements Action {
    private PredictDto content;

    public ErrorFileAction(PredictDto content) {
        this.content = content;
    }

    public PredictDto getContent() {
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
