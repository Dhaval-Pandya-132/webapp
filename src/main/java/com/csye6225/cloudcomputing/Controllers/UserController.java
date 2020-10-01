package com.csye6225.cloudcomputing.Controllers;

import com.csye6225.cloudcomputing.Models.UserModel;
import com.csye6225.cloudcomputing.Utils.Utility;
import com.csye6225.cloudcomputing.service.UserServices;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    UserServices us;
    @Autowired
    Utility ut;

    @RequestMapping(value = "v1/user", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createNewUser(@RequestBody UserModel user) {
        UserModel um = us.getUserByEmailAddress(user.getUsername());
        if (user.getUpdatedDatetime() != null || user.getCreatedDatetime() != null) {
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Invalid request parameters"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!ut.validateEmailAddress(user.getUsername())) {
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Invalid Email address"),
                    HttpStatus.BAD_REQUEST);
        }

        if (um != null) // Check if email address is already exist or not
        {
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Email address is already exist"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!ut.checkPasswordStrength(user.getPassword())) {
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Password length should be greater than 8 character " +
                            ", and contains at least 1 special character,digit and 1 Upper case character" +
                            "and it should not contain more than 4 sequences of  2 repeating characters " +
                            "and should not contain passwords more than 4 occurrences of the same character"),
                    HttpStatus.BAD_REQUEST);
        }
        user.setCreatedDatetime(new Date());
        user.setUpdatedDatetime(new Date());
        user.setPassword(ut.BCryptPassword(user.getPassword()));
        us.save(user);
        return new ResponseEntity<>(
                ut.prepareResponse(user, "POST"),
                HttpStatus.CREATED);
    }

    @RequestMapping(value = "v1/user/self", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Map<String, String>> getUserDetail(@RequestBody UserModel user, @RequestHeader(value = "Authorization") String value) {
        UserModel um = us.getUserByEmailAddress(user.getUsername());
        if (um == null || ut.validateAuthorization(value, user.getUsername(), um.getPassword())
        ) {
            return new ResponseEntity<>(Collections.singletonMap("msg", "Unauthorized request"), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(
                ut.prepareResponse(um, "GET"),
                HttpStatus.OK);
    }

    @RequestMapping(value = "v1/user/self", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Map<String, String>> updateUserDetail(@RequestBody @Valid UserModel user, @RequestHeader(value = "Authorization") String value) {
        UserModel um = us.getUserByEmailAddress(user.getUsername());
        if (um == null || ut.validateAuthorization(value, user.getUsername(), um.getPassword())
        ) {
            return new ResponseEntity<>(Collections.singletonMap("msg", "Unauthorized request"), HttpStatus.UNAUTHORIZED);
        }
        user.setId(um.getId());
        um.setFirstName(user.getFirstName());
        um.setLastName(user.getLastName());
        um.setPassword(ut.BCryptPassword(user.getPassword()));
        um.setUpdatedDatetime(new Date());
        us.save(um);

        return new ResponseEntity<>(
                ut.prepareResponse(user, "PUT"),
                HttpStatus.OK);
    }

    // exception handling
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class, JsonProcessingException.class, JsonParseException.class})
    void handleRuntimeException(NullPointerException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
