package com.csye6225.cloudcomputing.Controllers;

import com.csye6225.cloudcomputing.Models.AnswerModel;
import com.csye6225.cloudcomputing.Models.CategoryModel;
import com.csye6225.cloudcomputing.Models.QuestionModel;
import com.csye6225.cloudcomputing.Models.UserModel;
import com.csye6225.cloudcomputing.Utils.Utility;
import com.csye6225.cloudcomputing.service.AnswerServices;
import com.csye6225.cloudcomputing.service.CategoryModelServices;
import com.csye6225.cloudcomputing.service.QuestionServices;
import com.csye6225.cloudcomputing.service.UserServices;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    UserServices us;

    @Autowired
    QuestionServices qs;

    @Autowired
    CategoryModelServices cs;

    @Autowired
    AnswerServices as;


    @Autowired
    Utility ut;

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

    @RequestMapping(value = "v1/question/", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> addNewQuestion(@RequestBody QuestionModel question
            , @RequestHeader(value = "Authorization") String value) {
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        List<CategoryModel> dbCategoryList = cs.getAllCategory();
        List<CategoryModel> listOfRequestCategories = question.getCategories().stream().distinct().collect(Collectors.toList());

        List<List<CategoryModel>> output =
                ut.getFinalandNewCategories(dbCategoryList, listOfRequestCategories);

        question.setCreatedDatetime(new Date());
        question.setUpdatedDatetime(new Date());
        question.setUserId(um);
        question.setCategories(output.get(0));
        cs.save(output.get(1));
        qs.save(question);

        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text"
                , "categories"
                , "answers"};

        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"QuestionModelFilter"}, question);
        return new ResponseEntity<>(mapping, HttpStatus.CREATED);
    }

    @RequestMapping(value = "v1/question/{questionId}/answer", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> saveAnswer(@PathVariable UUID questionId
            , @RequestBody AnswerModel am
            , @RequestHeader(value = "Authorization") String value) {

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        QuestionModel qmw = qs.findQuestionByQuestionId(questionId);

        if (qmw == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        String[] list = {"answer_id"
                , "question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "answer_text"};

        AnswerModel amOut = new AnswerModel();
        amOut.setAnswerText(am.getAnswerText());
        amOut.setUserId(um);
        amOut.setQuestionId(qmw);
        amOut.setCreatedTimestamp(new Date());
        amOut.setUpdatedTimestamp(new Date());
        AnswerModel answerModel = as.saveAnswer(amOut);

        MappingJacksonValue mapping = ut.getDynamicResponse(list,
                new String[]{"AnswerModelFilter"}, answerModel);
        return new ResponseEntity<>(mapping, HttpStatus.CREATED);

    }


    @RequestMapping(value = "v1/question/{questionId}/answer/{answerId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateAnswer(@PathVariable UUID questionId
            , @PathVariable UUID answerId
            , @RequestBody AnswerModel answerModel
            , @RequestHeader(value = "Authorization") String value
    ) {
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);

        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(questionId, answerId);

        if (am == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !am.getUserId().getId().toString().equalsIgnoreCase(um.getId().toString())

        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        am.setAnswerText(answerModel.getAnswerText());
        am.setUpdatedTimestamp(new Date());
        as.saveAnswer(am);

        return new ResponseEntity(HttpStatus.NO_CONTENT);

    }

    @RequestMapping(value = "v1/question/{questionId}/answer/{answerId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> deleteAnswer(@PathVariable UUID questionId
            , @PathVariable UUID answerId
            , @RequestHeader(value = "Authorization") String value
    ) {
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(questionId, answerId);

        if (am == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !am.getUserId().getId().toString().equalsIgnoreCase(um.getId().toString())

        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        as.deleteAnswer(am.getAnswerId());
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Delete Question by questionId
     * @param questionId
     * @param value
     * @return
     */
    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> deleteQuestion(@PathVariable UUID questionId
            , @RequestHeader(value = "Authorization") String value) {

        QuestionModel dbQuestion = qs.findQuestionByQuestionId(questionId);

        if (dbQuestion == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !dbQuestion.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only create can update question
        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (dbQuestion.getAnswers().size() > 0) {
            return new ResponseEntity("One or more answers exists ", HttpStatus.BAD_REQUEST);
        }


        qs.deleteQuestion(dbQuestion);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateQuestion(@PathVariable UUID questionId
            , @RequestBody QuestionModel question
            , @RequestHeader(value = "Authorization") String value) {
        QuestionModel dbQuestion = qs.findQuestionByQuestionId(questionId);

        if (dbQuestion == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !dbQuestion.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only create can update question
        ) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        List<CategoryModel> dbCategoryList = cs.getAllCategory();
        List<CategoryModel> listOfRequestCategories = question.getCategories().stream().distinct().collect(Collectors.toList());
        List<List<CategoryModel>> output =
                ut.getFinalandNewCategories(dbCategoryList, listOfRequestCategories);

        dbQuestion.setUpdatedDatetime(new Date());
        dbQuestion.setCategories(output.get(0));
        dbQuestion.setQuestionText(question.getQuestionText());
        dbQuestion.setUserId(um);
        cs.save(output.get(1));
        qs.save(dbQuestion);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }


    /**
     * Public APIs
     *
     * @return
     */

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
        user.setId(UUID.randomUUID());
        user.setCreatedDatetime(new Date());
        user.setUpdatedDatetime(new Date());
        user.setPassword(ut.BCryptPassword(user.getPassword()));
        us.save(user);
        return new ResponseEntity<>(
                ut.prepareResponse(user, "POST"),
                HttpStatus.CREATED);
    }

    @RequestMapping(value = "v1/user/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getUserByID(@PathVariable UUID id) {
        String[] list = {"id"
                , "first_name"
                , "last_name"
                , "username"
                , "account_created", "account_updated"
        };

        UserModel um = us.getById(id);
        if (um == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"UserModelFilter"}, um);
        return new ResponseEntity<>(mapping, HttpStatus.OK);

    }

    @RequestMapping(value = "v1/question/{question_id}/answer/{answer_id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getAnswerByID(@PathVariable UUID question_id, @PathVariable UUID answer_id) {
        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(question_id, answer_id);
        if (am == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text", "categories"
                , "answers"
                , "answer_text"
                , "answer_id"};

        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"AnswerModelFilter"}, am);
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }

    @RequestMapping(value = "v1/questions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getAllQuestions() {

        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text"
                , "categories"
                , "answers"
                , "answer_text"
                , "answer_id"
        };

        MappingJacksonValue mapping = ut.getDynamicResponse(list,
                new String[]{"QuestionModelFilter", "AnswerModelFilter"}, qs.getAllQuestions());
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }

    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getQuestionByQuestionID(@PathVariable UUID questionId) {
        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text", "categories"
                , "answers"};

        QuestionModel qm = qs.findQuestionByQuestionId(questionId);

        if (qm == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"QuestionModelFilter"}, qm);
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }


    // @exception handling
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class, JsonProcessingException.class, JsonParseException.class})
    void handleRuntimeException(NullPointerException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
