package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
public class CategoryModel  implements Serializable  {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID categoryId;//= UUID.randomUUID();

    @Column(unique = true)
    private String category;
    @ManyToMany(
            mappedBy = "categories"
    )
    @JsonIgnore
    private List<QuestionModel> questions = new ArrayList<>();

    public List<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final CategoryModel other = (CategoryModel) obj;
        if ((this.category == null) ? (other.category != null)
                : !this.category.equalsIgnoreCase(other.category)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.category.toLowerCase().hashCode();
    }
}
