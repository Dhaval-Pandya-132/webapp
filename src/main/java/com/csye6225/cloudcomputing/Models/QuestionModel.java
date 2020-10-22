package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@JsonFilter("QuestionModelFilter")
@JsonPropertyOrder({"question_id"
        , "created_timestamp"
        , "updated_timestamp"
        , "user_id"
        , "question_text"
        , "categories"
        , "answers"
        , "attachments"
})
public class QuestionModel implements Serializable {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID questionId = UUID.randomUUID();
    private Date createdDatetime;
    private Date updatedDatetime;
    private String questionText;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private UserModel userId;

    @ManyToMany
    private List<CategoryModel> categories = new ArrayList<>();


    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = "questionId")
    private List<AnswerModel> answers = new ArrayList<>();


    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = "questionId")
    private List<FileModel> attachments = new ArrayList<>();


    public QuestionModel() {
    }

    public QuestionModel(UUID questionId, Date createdDatetime, Date updatedDatetime, String questionText, UserModel userId) {
        this.createdDatetime = createdDatetime;
        this.updatedDatetime = updatedDatetime;
        this.questionText = questionText;
        this.userId = userId;
        //  this.categoryId = categoryId;
        this.questionId = questionId;
    }


    /*--- Getter and Setters ----*/

    @JsonGetter("categories")
    public List<CategoryModel> getCategories() {
        return categories;
    }


    public void setCategories(List<CategoryModel> categories) {
        this.categories = categories;
    }

    public List<AnswerModel> getAnswers() {
        return answers;
    }

    @JsonGetter("answers")
    public void setAnswers(List<AnswerModel> answers) {
        this.answers = answers;
    }


    @JsonGetter("user_id")
    public String getUserID() {
        return this.userId.getId().toString();
    }

    @JsonGetter("question_id")
    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    @JsonGetter("created_timestamp")
    public Date getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    @JsonGetter("updated_timestamp")
    public Date getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(Date updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

    @JsonGetter("question_text")
    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public UserModel getUserId() {
        return userId;
    }

    public void setUserId(UserModel userId) {
        this.userId = userId;
    }

    @JsonGetter("attachments")
    public List<FileModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileModel> attachments) {
        this.attachments = attachments;
    }

}
