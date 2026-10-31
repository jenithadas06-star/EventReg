package com.college.events.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for the dedicated "Registration questions" editor, where an
 * organizer adds, edits, removes and reorders the custom questions of an event.
 *
 * <p>Like {@link EventForm} the dynamic rows arrive as parallel lists.</p>
 */
public class QuestionForm {

    private List<String> questionText = new ArrayList<>();
    private List<String> questionType = new ArrayList<>();
    private List<String> questionRequired = new ArrayList<>();
    private List<String> questionOptions = new ArrayList<>();

    public List<String> getQuestionText() {
        return questionText;
    }

    public void setQuestionText(List<String> questionText) {
        this.questionText = questionText == null ? new ArrayList<>() : questionText;
    }

    public List<String> getQuestionType() {
        return questionType;
    }

    public void setQuestionType(List<String> questionType) {
        this.questionType = questionType == null ? new ArrayList<>() : questionType;
    }

    public List<String> getQuestionRequired() {
        return questionRequired;
    }

    public void setQuestionRequired(List<String> questionRequired) {
        this.questionRequired = questionRequired == null ? new ArrayList<>() : questionRequired;
    }

    public List<String> getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(List<String> questionOptions) {
        this.questionOptions = questionOptions == null ? new ArrayList<>() : questionOptions;
    }
}
