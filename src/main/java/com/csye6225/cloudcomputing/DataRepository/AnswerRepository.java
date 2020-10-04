package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.AnswerModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerRepository extends JpaRepository<AnswerModel, UUID> {
}
