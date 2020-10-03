package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.QuestionModel;
import com.csye6225.cloudcomputing.Models.QuestionModelWrapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionModel, UUID> {
    QuestionModel findFirstByQuestionId(UUID QuestionId);
    List<QuestionModel>  findAll();

}
