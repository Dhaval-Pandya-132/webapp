package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.AnswerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerModel, UUID> {

    @Override
    void deleteById(UUID uuid);

    @Override
    void delete(AnswerModel answerModel);
}
