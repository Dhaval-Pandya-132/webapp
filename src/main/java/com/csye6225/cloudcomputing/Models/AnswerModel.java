package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@JsonFilter("AnswerModelFilter")
@JsonPropertyOrder({"answer_id"
        , "question_id"
        , "created_timestamp"
        , "updated_timestamp"
        , "user_id"
        , "answer_text"
})
public class AnswerModel implements Serializable {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID answerId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", nullable = false)
    private QuestionModel questionId;
    private Date createdTimestamp;
    private Date updatedTimestamp;
    private String answerText;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private UserModel userId;


    public AnswerModel() {
    }

    @JsonGetter("user_id")
    public String getUserUUID() {
        return this.userId.getId().toString();
    }

    @JsonGetter("question_id")
    public String getQuestionIDString() {
        return this.questionId.getQuestionId().toString();
    }

    @JsonGetter("answer_id")
    public UUID getAnswerId() {
        return answerId;
    }

    public void setAnswerId(UUID answerId) {
        this.answerId = answerId;
    }

    public QuestionModel getQuestionId() {
        return questionId;
    }

    public void setQuestionId(QuestionModel questionId) {
        this.questionId = questionId;
    }

    @JsonGetter("created_timestamp")
    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @JsonGetter("updated_timestamp")
    public Date getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(Date updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public UserModel getUserId() {
        return userId;
    }

    public void setUserId(UserModel userId) {
        this.userId = userId;
    }

    @JsonGetter("answer_text")
    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }


}
