package com.csye6225.cloudcomputing.Controllers;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.util.AWSRequestMetrics;
import com.csye6225.cloudcomputing.AwsSNSConfig;
import com.csye6225.cloudcomputing.Models.*;
import com.csye6225.cloudcomputing.Utils.Utility;
import com.csye6225.cloudcomputing.service.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.timgroup.statsd.NonBlockingStatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.timgroup.statsd.StatsDClient;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final static Logger lg = LoggerFactory.getLogger(UserController.class);
    private final static StatsDClient stdclient = new NonBlockingStatsDClient("","localhost",8125);
    @Autowired
    UserServices us;

    @Autowired
    QuestionServices qs;

    @Autowired
    CategoryModelServices cs;

    @Autowired
    AnswerServices as;

    @Autowired
    FileService fs;
    @Autowired
    FileUploadService fileService;

    @Autowired
    Utility ut;


    // Topic arn. Developers are free to choose their topic arn.
    @Value("${app.snstopic}")
    private static String TOPIC_ARN;

    @Autowired
    private Environment env;

    @Autowired
    private AmazonSNSClient amazonSNSClient;

    private S3StorageService storageService;

    @Autowired
    public UserController(S3StorageService storageService) {
        this.storageService = storageService;
    }


    @RequestMapping(value = "v1/user/self", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Map<String, String>> getUserDetail(@RequestHeader(value = "Authorization") String value) {
        lg.info("get :v1/user/self is called");
        long startTime = System.currentTimeMillis();
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("get.v1.user.self.count");
        stdclient.incrementCounter("GET");


        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        statsDClient.recordExecutionTime("get.v1.user.self.db.response.time", System.currentTimeMillis() - startTime);

        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity<>(Collections.singletonMap("msg", "Unauthorized request"), HttpStatus.UNAUTHORIZED);
        }
        lg.info("get :v1/user/self execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("get.v1.user.self.response.time", System.currentTimeMillis() - startTime);

        return new ResponseEntity<>(
                ut.prepareResponse(um, "GET"),
                HttpStatus.OK);
    }

    @RequestMapping(value = "v1/user/self", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Map<String, String>> updateUserDetail(@RequestBody @Valid UserModel user, @RequestHeader(value = "Authorization") String value) {
        lg.info("put :v1/user/self is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("PUT");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("put.v1.user.self.count");

        UserModel um = us.getUserByEmailAddress(user.getUsername());
        if (um == null || ut.validateAuthorization(value, user.getUsername(), um.getPassword())
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity<>(Collections.singletonMap("msg", "Unauthorized request"), HttpStatus.UNAUTHORIZED);
        }
        if (!ut.checkPasswordStrength(user.getPassword())) {
            stdclient.incrementCounter("Badrequest");
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
        statsDClient.recordExecutionTime("put.v1.user.self.db.response.time", System.currentTimeMillis() - startTime);


        lg.info("put :v1/user/self execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("put.v1.user.self.response.time", System.currentTimeMillis() - startTime);

        return new ResponseEntity<>(
                ut.prepareResponse(user, "PUT"),
                HttpStatus.OK);
    }

    @RequestMapping(value = "v1/question/", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> addNewQuestion(@RequestBody QuestionModel question
            , @RequestHeader(value = "Authorization") String value) {

        lg.info("post :v1/question/ is called");
        long startTime = System.currentTimeMillis();
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("post.v1.question.count");
        stdclient.incrementCounter("POST");

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (question.getQuestionText() == null
                || question.getQuestionText().equalsIgnoreCase("")) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
        statsDClient.recordExecutionTime("post.v1.question.db.response.time", System.currentTimeMillis() - startTime);

        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text"
                , "categories"
                , "answers"
                , "attachments"
                , "file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"
        };

        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"QuestionModelFilter", "FileModelFilter"}
                , question);
        lg.info("post :v1/question/ execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("post.v1.question.response.time", System.currentTimeMillis() - startTime);

        return new ResponseEntity<>(mapping, HttpStatus.CREATED);
    }

    @RequestMapping(value = "v1/question/{questionId}/answer", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> saveAnswer(@PathVariable UUID questionId
            , @RequestBody AnswerModel am
            , @RequestHeader(value = "Authorization") String value) {

        lg.info("post :v1/question/{questionId}/answer is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("POST");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("post.v1.question.questionId.answer.count");

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        QuestionModel qmw = qs.findQuestionByQuestionId(questionId);

        if (qmw == null) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        if (am == null ||
                am.getAnswerText().equalsIgnoreCase("")) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String[] list = {"answer_id"
                , "question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "answer_text"
                , "attachments"
                , "file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"

        };

        AnswerModel amOut = new AnswerModel();
        amOut.setAnswerText(am.getAnswerText());
        amOut.setUserId(um);
        amOut.setQuestionId(qmw);
        amOut.setCreatedTimestamp(new Date());
        amOut.setUpdatedTimestamp(new Date());
        AnswerModel answerModel = as.saveAnswer(amOut);
        statsDClient.recordExecutionTime("post.v1.question.questionId.answer.db.response.time", System.currentTimeMillis() - startTime);

        MappingJacksonValue mapping = ut.getDynamicResponse(list,
                new String[]{"AnswerModelFilter", "FileModelFilter"}, answerModel);
        lg.info("post :v1/question/{questionId}/answer execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("post.v1.question.questionId.answer.response.time", System.currentTimeMillis() - startTime);

        String message = "addAnswer"+"|"+amOut.getAnswerId()
                + "|" +amOut.getQuestionId().getQuestionId()+"|"+ amOut.getQuestionId().getQuestionText()
                +"|"+am.getAnswerText() +"|"+ amOut.getQuestionId().getUserId().getUsername();

        final PublishRequest publishRequest = new PublishRequest(env.getProperty("app.snstopic"),message);
        amazonSNSClient.publish(publishRequest);
        return new ResponseEntity<>(mapping, HttpStatus.CREATED);

    }


    @RequestMapping(value = "v1/question/{questionId}/answer/{answerId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateAnswer(@PathVariable UUID questionId
            , @PathVariable UUID answerId
            , @RequestBody AnswerModel answerModel
            , @RequestHeader(value = "Authorization") String value
    ) {
        lg.info("put :v1/question/{questionId}/answer/{answerId} is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("PUT");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("put.v1.question.questionId.answer.answerId.count");


        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);

        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(questionId, answerId);

        if (am == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !am.getUserId().getId().toString().equalsIgnoreCase(um.getId().toString())

        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (answerModel == null ||
                answerModel.getAnswerText().equalsIgnoreCase("")) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        am.setAnswerText(answerModel.getAnswerText());
        am.setUpdatedTimestamp(new Date());
        as.saveAnswer(am);
        statsDClient.recordExecutionTime("put.v1.question.questionId.answer.answerId.db.response.time", System.currentTimeMillis() - startTime);


        lg.info("put :v1/question/{questionId}/answer/{answerId} execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("put.v1.question.questionId.answer.answerId.response.time", System.currentTimeMillis() - startTime);

        String message = "updateAnswer"+"|"+am.getAnswerId()
                + "|" +am.getQuestionId().getQuestionId()+"|"+ am.getQuestionId().getQuestionText()
                +"|"+am.getAnswerText() +"|"+ am.getQuestionId().getUserId().getUsername();

        final PublishRequest publishRequest = new PublishRequest(env.getProperty("app.snstopic"),message);
        amazonSNSClient.publish(publishRequest);

        return new ResponseEntity(HttpStatus.NO_CONTENT);

    }

    @RequestMapping(value = "v1/question/{questionId}/answer/{answerId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> deleteAnswer(@PathVariable UUID questionId
            , @PathVariable UUID answerId
            , @RequestHeader(value = "Authorization") String value
    ) {
        lg.info("delete :v1/question/{questionId}/answer/{answerId} is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("DELETE");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("delete.v1.question.questionId.answer.answerId.count");

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(questionId, answerId);

        if (am == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !am.getUserId().getId().toString().equalsIgnoreCase(um.getId().toString())

        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        List<FileModel> fm = fs.getFilesByQuestionIDAndAnswerId(questionId ,answerId);
        for (FileModel file:
                fm) {
            storageService.deleteFile(file.getS3ObjectKey());
        }

        as.deleteAnswer(am.getAnswerId());
        statsDClient.recordExecutionTime("delete.v1.question.questionId.answer.answerId.db.response.time", System.currentTimeMillis() - startTime);


        lg.info("delete :v1/question/{questionId}/answer/{answerId} execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("delete.v1.question.questionId.answer.answerId.response.time", System.currentTimeMillis() - startTime);


        String message = "deleteAnswer"+"|"+am.getAnswerId()
                + "|" +am.getQuestionId().getQuestionId()+"|"+ am.getQuestionId().getQuestionText()
                +"|"+am.getAnswerText() +"|"+ am.getQuestionId().getUserId().getUsername();

        final PublishRequest publishRequest = new PublishRequest(env.getProperty("app.snstopic"),message);
        amazonSNSClient.publish(publishRequest);


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Delete Question by questionId
     *
     * @param questionId
     * @param value
     * @return
     */
    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> deleteQuestion(@PathVariable UUID questionId
            , @RequestHeader(value = "Authorization") String value) {
        lg.info("delete :v1/question/{questionId} is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("DELETE");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("delete.v1.question.questionId.count");

        QuestionModel dbQuestion = qs.findQuestionByQuestionId(questionId);

        if (dbQuestion == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !dbQuestion.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only create can update question
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (dbQuestion.getAnswers().size() > 0) {
            return new ResponseEntity("One or more answers exists ", HttpStatus.BAD_REQUEST);
        }

        List<FileModel> fm = fs.getFilesByQuestionID(dbQuestion.getQuestionId());
        for (FileModel file:
             fm) {
            storageService.deleteFile(file.getS3ObjectKey());
        }



        qs.deleteQuestion(dbQuestion);
        statsDClient.recordExecutionTime("delete.v1.question.questionId.db.response.time", System.currentTimeMillis() - startTime);

        lg.info("delete :v1/question/{questionId} execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("delete.v1.question.questionId.response.time", System.currentTimeMillis() - startTime);

//        String message = "deleteQuestion"+"|" + dbQuestion.getQuestionId()+"|" + dbQuestion.getQuestionText() +"|"+ um.getUsername();
//        final PublishRequest publishRequest = new PublishRequest(TOPIC_ARN,message);
//        amazonSNSClient.publish(publishRequest);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateQuestion(@PathVariable UUID questionId
            , @RequestBody QuestionModel question
            , @RequestHeader(value = "Authorization") String value) {


        lg.info("put :v1/question/{questionId} is called");
        long startTime = System.currentTimeMillis();
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("put.v1.question.questionId.count");
        stdclient.incrementCounter("PUT");
        QuestionModel dbQuestion = qs.findQuestionByQuestionId(questionId);

        if (dbQuestion == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !dbQuestion.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only create can update question
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (question.getQuestionText() == null
                || question.getQuestionText().equalsIgnoreCase("")) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
        statsDClient.recordExecutionTime("put.v1.question.questionId.db.response.time", System.currentTimeMillis() - startTime);

        lg.info("put :v1/question/{questionId} execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("put.v1.question.questionId.response.time", System.currentTimeMillis() - startTime);

//        String message = "updateQuestion"+"|" + dbQuestion.getQuestionId()+"|" +"|"+ dbQuestion.getQuestionText() +"|"+ um.getUsername();
//        final PublishRequest publishRequest = new PublishRequest(TOPIC_ARN,message);
//        amazonSNSClient.publish(publishRequest);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }


    @RequestMapping(value = "v1/question/{question_id}/file", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> uploadQuestionFile(@PathVariable UUID question_id
            , @RequestPart(value = "file") MultipartFile multiPartFile
            , @RequestHeader(value = "Authorization") String value
    ) throws IOException {

        lg.info("post :v1/question/{question_id}/file is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("POST");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("post.v1.question.question_id.file.count");

        QuestionModel dbQuestion = qs.findQuestionByQuestionId(question_id);

        if (dbQuestion == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !dbQuestion.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only create can update question
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

//        new File()
        Path p = fileService.uploadFile(multiPartFile);
        File file = ut.convertMultiPartToFile(multiPartFile);

        FileModel fm = new FileModel();
        String fileName = fm.getFileId() + "_" + multiPartFile.getOriginalFilename();
        PutObjectResult por = storageService.uploadFile2(fileName, new File("/opt/tomcat/temp/" + multiPartFile.getOriginalFilename()));
       // PutObjectResult por = storageService.uploadFile2(fileName, file.getName());
        fm.setQuestionId(dbQuestion);
        fm.setFileName(multiPartFile.getOriginalFilename());
        fm.setS3BucketName("webapp.dhaval.pandya");
        fm.setS3ObjectKey(fileName);
        fm.setSseAlgorithm(por.getSSEAlgorithm());
        fm.setCreatedTimestamp(new Date());
        fs.saveFile(fm);
        statsDClient.recordExecutionTime("post.v1.question.question_id.file.s3bucket.response.time", System.currentTimeMillis() - startTime);

        String[] list = {"file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"
        };

        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"FileModelFilter"}, fm);

        lg.info("post :v1/question/{question_id}/file execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("post.v1.question.question_id.file.response.time", System.currentTimeMillis() - startTime);


        return new ResponseEntity<>(mapping, HttpStatus.CREATED);
    }


    @RequestMapping(value = "v1/question/{question_id}/answer/{answer_id}/file", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> uploadAnswerFile(
            @PathVariable UUID question_id
            , @RequestPart(value = "file") MultipartFile multiPartFile
            , @RequestHeader(value = "Authorization") String value
            , @PathVariable UUID answer_id) throws IOException {

        lg.info("post :v1/question/{question_id}/answer/{answer_id}/file is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("POST");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("post.v1.question.question_id.answer.answer_id.file.count");


        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(question_id, answer_id);
        if (am == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !am.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only creator can attach the file
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        Path p = fileService.uploadFile(multiPartFile);

        File file = ut.convertMultiPartToFile(multiPartFile);
        FileModel fm = new FileModel();
        String fileName = fm.getFileId() + "_" + multiPartFile.getOriginalFilename();
        PutObjectResult por = storageService.uploadFile2(fileName, new File("/opt/tomcat/temp/"+multiPartFile.getOriginalFilename()));

        fm.setQuestionId(am.getQuestionId());
        fm.setFileName(multiPartFile.getOriginalFilename());
        fm.setS3BucketName("webapp.dhaval.pandya");
        fm.setS3ObjectKey(fileName);
        fm.setSseAlgorithm(por.getSSEAlgorithm());
        fm.setCreatedTimestamp(new Date());
        fm.setAnswerId(am);
        fs.saveFile(fm);
        statsDClient.recordExecutionTime("post.v1.question.question_id.answer.answer_id.file.s3bucket.response.time", System.currentTimeMillis() - startTime);

        String[] list = {"file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"
        };

        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"FileModelFilter"}, fm);

        lg.info("post :v1/question/{question_id}/file execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("post.v1.question.question_id.answer.answer_id.file.response.time", System.currentTimeMillis() - startTime);

        String message = "uploadFileAnswer"+"|"+am.getAnswerId()
                + "|" +am.getQuestionId().getQuestionId()+"|"+ am.getQuestionId().getQuestionText()
                +"|"+am.getAnswerText() +"|"+ am.getQuestionId().getUserId().getUsername();

        final PublishRequest publishRequest = new PublishRequest(env.getProperty("app.snstopic"),message);
        amazonSNSClient.publish(publishRequest);

        return new ResponseEntity<>(mapping, HttpStatus.CREATED);
    }


    @RequestMapping(value = "v1/question/{question_id}/file/{file_id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> deleteQuestionFile(
            @PathVariable UUID question_id
            , @RequestHeader(value = "Authorization") String value
            , @PathVariable UUID file_id) throws IOException {
        lg.info("delete :v1/question/{question_id}/file/{file_id} is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("DELETE");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("delete.v1.question.question_id.file.file_id.count");


        QuestionModel dbQuestion = qs.findQuestionByQuestionId(question_id);
        FileModel fm = fs.getFileByID(file_id);

        if (dbQuestion == null || fm == null ||
                dbQuestion.getQuestionId() != fm.getQuestionId().getQuestionId()) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !dbQuestion.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only create can update question
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        storageService.deleteFile(fm.getS3ObjectKey());
        fs.deleteFile(fm);
        statsDClient.recordExecutionTime("delete.v1.question.question_id.file.file_id.s3bucket.response.time", System.currentTimeMillis() - startTime);

        lg.info("delete :v1/question/{question_id}/file/{file_id}  execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("delete.v1.question.question_id.file.file_id.response.time", System.currentTimeMillis() - startTime);

        return new ResponseEntity(HttpStatus.NO_CONTENT);

    }
    @RequestMapping(value = "v1/question/{question_id}/answer/{answer_id}/file/{file_id}",
            method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> deleteQuestionFile(
            @PathVariable UUID question_id
            , @RequestHeader(value = "Authorization") String value
            , @PathVariable UUID file_id,
            @PathVariable UUID answer_id) throws IOException {

        lg.info("delete :v1/question/{question_id}/answer/{answer_id}/file/{file_id} is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("DELETE");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("delete.v1.question.question_id.answer.answer_id.file.file_id.count");


        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(question_id, answer_id);
        FileModel fm = fs.getFileByID(file_id);

        if (am == null || fm == null ||
                (fm.getQuestionId().getQuestionId() != am.getQuestionId().getQuestionId()
                || fm.getAnswerId().getAnswerId() != am.getAnswerId()
                )
        ) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String[] parseToken = ut.parseAuthorizationToken(value);
        UserModel um = us.getUserByEmailAddress(parseToken[0]);
        // authenticate the users
        if (um == null || ut.validateAuthorization(value, parseToken[0], um.getPassword())
                || !am.getUserId().getUsername().equalsIgnoreCase(parseToken[0])  // Only creator can attach the file
        ) {
            stdclient.incrementCounter("Unauthorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        storageService.deleteFile(fm.getS3ObjectKey());
        fs.deleteFile(fm);
        statsDClient.recordExecutionTime("delete.v1.question.question_id.answer.answer_id.file.file_id.s3bucket.response.time", System.currentTimeMillis() - startTime);


        lg.info("delete :v1/question/{question_id}/file/{file_id}  execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("delete.v1.question.question_id.answer.answer_id.file.file_id.response.time", System.currentTimeMillis() - startTime);
        return new ResponseEntity(HttpStatus.NO_CONTENT);

    }


    /**
     * Public APIs
     *
     * @return
     */

    @RequestMapping(value = "v1/user", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createNewUser(@RequestBody UserModel user) {
        lg.info("post :v1/user is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("POST");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("post.v1.user.count");

        UserModel um = us.getUserByEmailAddress(user.getUsername());
        if (user.getUpdatedDatetime() != null || user.getCreatedDatetime() != null) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Invalid request parameters"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!ut.validateEmailAddress(user.getUsername())) {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Invalid Email address"),
                    HttpStatus.BAD_REQUEST);
        }

        if (um != null) // Check if email address is already exist or not
        {
            stdclient.incrementCounter("Badrequest");
            return new ResponseEntity<>(
                    Collections.singletonMap("msg", "Email address is already exist"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!ut.checkPasswordStrength(user.getPassword())) {
            stdclient.incrementCounter("Badrequest");
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

        statsDClient.recordExecutionTime("post.v1.user.response.time", System.currentTimeMillis() - startTime);

        lg.info("post :v1/user is called  execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
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
        lg.info("get :v1/user/{id} is called");
        long startTime = System.currentTimeMillis();
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("get.v1.user.id.count");
        stdclient.incrementCounter("GET");

        UserModel um = us.getById(id);
        statsDClient.recordExecutionTime("get.v1.user.id.db.response.time", System.currentTimeMillis() - startTime);

        if (um == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        MappingJacksonValue mapping = ut.getDynamicResponse(list, new String[]{"UserModelFilter"}, um);

        lg.info("get :v1/user/{id} is called  execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("get.v1.user.id.response.time", System.currentTimeMillis() - startTime);
        return new ResponseEntity<>(mapping, HttpStatus.OK);

    }

    @RequestMapping(value = "v1/question/{question_id}/answer/{answer_id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getAnswerByID(@PathVariable UUID question_id, @PathVariable UUID answer_id) {

        lg.info("get :v1/question/{question_id}/answer/{answer_id} is called");
        long startTime = System.currentTimeMillis();
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("get.v1.question.question_id.answer.answer_id.count");
        stdclient.incrementCounter("GET");

        AnswerModel am = qs.findAnswerByQuestionAndAnswerId(question_id, answer_id);
        statsDClient.recordExecutionTime("get.v1.question.question_id.answer.answer_id.db.response.time", System.currentTimeMillis() - startTime);

        if (am == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text", "categories"
                , "answers"
                , "answer_text"
                , "answer_id"
                , "attachments"
                , "file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"
        };

        MappingJacksonValue mapping = ut.getDynamicResponse(list,
                new String[]{"AnswerModelFilter", "FileModelFilter"}, am);

        lg.info("get :v1/question/{question_id}/answer/{answer_id} is called  execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("get.v1.question.question_id.answer.answer_id.response.time", System.currentTimeMillis() - startTime);
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }

    @RequestMapping(value = "v1/questions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getAllQuestions() {


        System.out.println("topic arn :"+TOPIC_ARN  + "----"+ env.getProperty("app.snstopic"));
        lg.info("get :v1/questions is called");
        long startTime = System.currentTimeMillis();
        stdclient.incrementCounter("GET");
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("get.v1.questions.count");

        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                , "user_id"
                , "question_text"
                , "categories"
                , "answers"
                , "answer_text"
                , "answer_id"
                , "attachments"
                , "file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"
        };

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lg.info("get :v1/questions fetching all questions");

        List<QuestionModel> output = ut.removeDuplicateAttachments(qs.getAllQuestions());

        statsDClient.recordExecutionTime("get.v1.questions.db.response.time", System.currentTimeMillis() - startTime);

        MappingJacksonValue mapping = ut.getDynamicResponse(list,
                new String[]{"QuestionModelFilter", "AnswerModelFilter", "FileModelFilter"}, output);

        lg.info("get :v1/questions execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("get.v1.questions.response.time", System.currentTimeMillis() - startTime);
        return new ResponseEntity<>(mapping, HttpStatus.OK);

    }

    @RequestMapping(value = "v1/question/{questionId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> getQuestionByQuestionID(@PathVariable UUID questionId) {
        String[] list = {"question_id"
                , "created_timestamp"
                , "updated_timestamp"
                ,"answer_id"
                , "user_id"
                , "question_text", "categories"
                , "answers"
                , "attachments"
                , "file_name"
                , "s3_object_name"
                , "file_id"
                , "created_date"
                , "bucket_name"
                , "sse_algorithm"
        };

        lg.info("get :v1/question/{questionId} is called");
        long startTime = System.currentTimeMillis();
        StatsDClient statsDClient = new NonBlockingStatsDClient("","localhost",8125);
        statsDClient.incrementCounter("get.v1.question.questionId.count");
        stdclient.incrementCounter("GET");

        QuestionModel qm = qs.findQuestionByQuestionId(questionId);

        statsDClient.recordExecutionTime("get.v1.question.questionId.db.response.time", System.currentTimeMillis() - startTime);
        lg.info("get :v1/question/{questionId} debug 1");
        if (qm == null) {
            stdclient.incrementCounter("Notfound");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        lg.info("get :v1/question/{questionId} debug 2");
        List<QuestionModel> ql = new ArrayList<>();
        ql.add(qm);
        List<QuestionModel> output = ut.removeDuplicateAttachments(ql);
        lg.info("get :v1/question/{questionId} debug 3" + output);
        MappingJacksonValue mapping = ut.getDynamicResponse(list,
                new String[]{"QuestionModelFilter","AnswerModelFilter", "FileModelFilter"}
                , output.get(0));
        lg.info("get :v1/question/{questionId} debug 4" + mapping);
        lg.info("get :v1/question/{questionId} execution time : "+ (System.currentTimeMillis() - startTime) +"ms");
        statsDClient.recordExecutionTime("get.v1.question.questionId.response.time", System.currentTimeMillis() - startTime);
      //  return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }





    @RequestMapping(value = "v1/file", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<MappingJacksonValue> uploadAnswerFile(

            @RequestPart(value = "file") MultipartFile multiPartFile
      ) throws IOException {
        fileService.uploadFile(multiPartFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }









    // @exception handling
    @ExceptionHandler({NullPointerException.class
            , IllegalArgumentException.class, JsonProcessingException.class
            , JsonParseException.class
            , IndexOutOfBoundsException.class
    })
    void handleRuntimeException(NullPointerException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
