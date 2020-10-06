package com.csye6225.cloudcomputing.service;


import com.csye6225.cloudcomputing.DataRepository.QuestionRepository;
import com.csye6225.cloudcomputing.Models.AnswerModel;
import com.csye6225.cloudcomputing.Models.QuestionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionServices {

    @Autowired
    private QuestionRepository repo;

    public QuestionModel save(QuestionModel um) {
        repo.save(um);
        return um;
    }

    public QuestionModel findQuestionByQuestionId(UUID questionId) {
        return repo.findFirstByQuestionId(questionId);
    }

    public List<QuestionModel> getAllQuestions() {
        return repo.findAll();
    }

    public AnswerModel findAnswerByQuestionAndAnswerId(UUID questionId,
                                                       UUID answerId) {
        QuestionModel qm = findQuestionByQuestionId(questionId);
        if (qm != null) {
            List<AnswerModel> am = qm.getAnswers().stream().filter(answer ->
                    answer.getAnswerId().toString().equalsIgnoreCase(answerId.toString())
            ).collect(Collectors.toList());
            return am.get(0);
        }

        return null;
    }


    public void deleteQuestion(QuestionModel questionModel) {
        repo.delete(questionModel);
    }

}
