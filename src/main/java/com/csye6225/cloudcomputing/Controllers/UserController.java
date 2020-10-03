package com.csye6225.cloudcomputing.Controllers;

import com.csye6225.cloudcomputing.Models.QuestionModel;
import com.csye6225.cloudcomputing.Models.QuestionModelWrapper;
import com.csye6225.cloudcomputing.Models.UserModel;
import com.csye6225.cloudcomputing.Utils.Utility;
import com.csye6225.cloudcomputing.service.CategoryModelServices;
import com.csye6225.cloudcomputing.service.QuestionServices;
import com.csye6225.cloudcomputing.service.UserServices;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
public class UserController {

    @Autowired
    UserServices us;

    @Autowired
    QuestionServices qs;

    @Autowired
    CategoryModelServices cs;

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
    public ResponseEntity<Map<String, String>> getUserDetail(@RequestHeader(value = "Authorization") String value) {
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
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
        if (!ut.checkPasswordStrength(user.getPassword())) {
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Password length should be greater than 8 character " +
                            ", and contains at least 1 special character,digit and 1 Upper case character" +
                            "and it should not contain more than 4 sequences of  2 repeating characters " +
                            "and should not contain passwords more than 4 occurrences of the same character"),
                    HttpStatus.BAD_REQUEST);
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


    @RequestMapping(value = "v1/question", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<QuestionModelWrapper> createQuestion(@RequestBody QuestionModelWrapper Question, @RequestHeader(value = "Authorization") String value) {
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        QuestionModel qm = new QuestionModel(new Date(), new Date(), Question.getQuestionText(), um, Question.getCategories().get(0));
        if (cs.getCategoryByName(Question.getCategories().get(0).getCategory()) == null) // check category is already exist or not
        {
            cs.save(Question.getCategories().get(0));
        }
        QuestionModelWrapper qmw = qs.save(qm);

        return new ResponseEntity<>(
                qmw,
                HttpStatus.CREATED);
    }


    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public ResponseEntity<QuestionModelWrapper> updateQuestion(@PathVariable UUID questionId
            , @RequestBody QuestionModelWrapper Question
            , @RequestHeader(value = "Authorization") String value) {
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        System.out.println("param "+ questionId );
        QuestionModelWrapper qmw = qs.findQuestionByQuestionId(questionId);

        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !qmw.getUserId().getId().equals(um.getId())
        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        qmw.setUpdatedDatetime(new Date());
        qmw.setQuestionText(Question.getQuestionText());
        qmw.setCategories(Question.getCategories());

//        QuestionModel qm = new QuestionModel(new Date(),new Date(), Question.getQuestionText(),um, Question.getCategories().get(0));
//        if (cs.getCategoryByName(Question.getCategories().get(0).getCategory()) == null) // check category is already exist or not
//        {
//            cs.save(Question.getCategories().get(0));
//        }
//        QuestionModelWrapper qmw= qs.save(qm);

        return new ResponseEntity<>(
                qmw,
                HttpStatus.CREATED);
    }


    //    @exception handling
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class, JsonProcessingException.class, JsonParseException.class})
    void handleRuntimeException(NullPointerException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
