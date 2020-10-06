package com.csye6225.cloudcomputing.service;

import com.csye6225.cloudcomputing.DataRepository.UserRepository;
import com.csye6225.cloudcomputing.Models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserServices {

    @Autowired
    private UserRepository repo;

    public List<UserModel> listAll() {
        return repo.findAll();
    }

    public UserModel save(UserModel um) {
        repo.save(um);
        return um;
    }

    public UserModel getById(UUID id) {
        return repo.getFirstById(id);
    }

    public UserModel getUserByEmailAddress(String username) {
        return repo.findByUsername(username);
    }

}
