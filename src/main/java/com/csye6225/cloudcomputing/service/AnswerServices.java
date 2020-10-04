package com.csye6225.cloudcomputing.service;


import com.csye6225.cloudcomputing.DataRepository.AnswerRepository;
import com.csye6225.cloudcomputing.Models.AnswerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnswerServices {

    @Autowired
    AnswerRepository ar;


    public AnswerModel saveAnswer(AnswerModel am)
    {
        ar.save(am);
        return  am ;
    }


}
