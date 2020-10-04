package com.csye6225.cloudcomputing.Models;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class QuestionModelWrapper extends QuestionModel {
    List<CategoryModel> categories;
    List<AnswerModel> answers;

    public QuestionModelWrapper() {
    }

    public QuestionModelWrapper(QuestionModel qm) {
        super(qm.getQuestionId(),qm.getCreatedDatetime(), qm.getUpdatedDatetime(), qm.getQuestionText(), qm.getUserId(),qm.getCategoryId());
        this.categories = new ArrayList<>();
        this.answers= new ArrayList<>();
    }

    public List<AnswerModel> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerModel> answers) {
        this.answers = answers;
    }

    public List<CategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryModel> categories) {
        this.categories = categories;
    }
}
