package com.csye6225.cloudcomputing.service;


import com.csye6225.cloudcomputing.DataRepository.CategoryRepository;
import com.csye6225.cloudcomputing.DataRepository.QuestionRepository;
import com.csye6225.cloudcomputing.Models.CategoryModel;
import com.csye6225.cloudcomputing.Models.QuestionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryModelServices {


    @Autowired
    private CategoryRepository repo;

    public CategoryModel save(CategoryModel um) {
        repo.save(um);
        return um;
    }

    public CategoryModel getCategoryByName(String categoryName)
    {
       return repo.findFirstByCategory(categoryName);
    }


}
