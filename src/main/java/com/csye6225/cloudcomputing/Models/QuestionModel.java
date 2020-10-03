package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class QuestionModel implements Serializable {

    @Id
    @GeneratedValue( generator = "uuid2" )
    @GenericGenerator( name = "uuid2", strategy = "uuid2" )
    @Column(columnDefinition = "BINARY(16)" )
    private UUID questionId = UUID.randomUUID();
    private Date createdDatetime;
    private Date updatedDatetime;
    private String questionText;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private UserModel userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoryId", nullable = false)
    @JsonIgnore
    private CategoryModel categoryId;


    public QuestionModel(){

    }


    public QuestionModel(Date createdDatetime, Date updatedDatetime, String questionText, UserModel userId, CategoryModel categoryId) {
        this.createdDatetime = createdDatetime;
        this.updatedDatetime = updatedDatetime;
        this.questionText = questionText;
        this.userId = userId;
        this.categoryId = categoryId;
    }

    public CategoryModel getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(CategoryModel categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public Date getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public Date getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(Date updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

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

}
