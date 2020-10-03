package com.csye6225.cloudcomputing.Models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
public class CategoryModel {

    @Id
    @GeneratedValue( generator = "uuid2" )
    @GenericGenerator( name = "uuid2", strategy = "uuid2" )
    @Column(columnDefinition = "BINARY(16)" )
    private UUID categoryId = UUID.randomUUID();

    @Column(unique = true)
    private String category ;
    @OneToMany(
            mappedBy = "categoryId",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    private Set<QuestionModel> questions;


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
}
