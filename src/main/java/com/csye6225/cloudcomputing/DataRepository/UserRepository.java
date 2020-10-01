package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

    UserModel findByUsername (String username);

}
