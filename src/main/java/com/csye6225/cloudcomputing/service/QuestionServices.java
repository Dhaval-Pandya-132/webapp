package com.csye6225.cloudcomputing.service;


import com.csye6225.cloudcomputing.DataRepository.QuestionRepository;
import com.csye6225.cloudcomputing.DataRepository.UserRepository;
import com.csye6225.cloudcomputing.Models.QuestionModel;
import com.csye6225.cloudcomputing.Models.QuestionModelWrapper;
import com.csye6225.cloudcomputing.Models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuestionServices {

    @Autowired
    private QuestionRepository repo;

    public QuestionModel save(QuestionModel um) {
        QuestionModelWrapper qwm = new QuestionModelWrapper(um);
//        qwm.getCategories().add(um.getCategoryId());
//        qwm.setQuestionId(um.getQuestionId());
     //  repo.findAll();
        repo.save(um);
        return um;
    }

    public QuestionModelWrapper findQuestionByQuestionId(UUID questionId) {
      //  QuestionModelWrapper qwm = null;
//        List<QuestionModel> qmList = repo.findAll();
//        for (QuestionModel qm:
//             qmList) {
//            if(qm.getQuestionId().equals(questionId))
//            {
//                System.out.println(qm.getQuestionId());
//             qwm = new QuestionModelWrapper(qm);
//             return qwm;
//            }
//
//        }

        return new QuestionModelWrapper(repo.findFirstByQuestionId(questionId));
    }


}
