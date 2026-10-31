package com.college.events.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A student's answer to one {@link RegistrationQuestion}, stored alongside the
 * {@link EventRegistration} it belongs to.
 */
@Entity
@Table(name = "registration_answers")
public class RegistrationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", insertable = false, updatable = false)
    private Long registrationId;
    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String answer;

    public RegistrationAnswer() {
    }

    public RegistrationAnswer(Long id, Long registrationId, Long questionId,
                              String questionText, String answer) {
        this.id = id;
        this.registrationId = registrationId;
        this.questionId = questionId;
        this.questionText = questionText;
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getDisplayAnswer() {
        return (answer == null || answer.isBlank()) ? "-" : answer;
    }
}
