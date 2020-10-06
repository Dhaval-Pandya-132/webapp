package com.csye6225.cloudcomputing.service;


import com.csye6225.cloudcomputing.DataRepository.CategoryRepository;
import com.csye6225.cloudcomputing.DataRepository.QuestionRepository;
import com.csye6225.cloudcomputing.Models.CategoryModel;
import com.csye6225.cloudcomputing.Models.QuestionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CategoryModelServices {


    @Autowired
    private CategoryRepository repo;

    public void save(List<CategoryModel> um) {
        repo.saveAll(um);
//        return um;
    }

    public List<CategoryModel> getAllCategory(){
        return repo.findAll();
    }



}
