package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, UUID> {
    CategoryModel findFirstByCategory(String Category);
    List<CategoryModel> findAll();
}
