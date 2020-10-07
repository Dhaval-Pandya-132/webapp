package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.QuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionModel, UUID> {
    QuestionModel findFirstByQuestionId(UUID QuestionId);
    List<QuestionModel>  findAll();

    @Override
    void delete(QuestionModel questionModel);
}
