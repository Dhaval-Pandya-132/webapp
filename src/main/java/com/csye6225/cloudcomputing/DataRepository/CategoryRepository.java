package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
public interface CategoryRepository extends JpaRepository<CategoryModel, UUID> {
    CategoryModel findFirstByCategory(String Category);
}
