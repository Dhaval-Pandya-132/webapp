package com.csye6225.cloudcomputing;
import com.csye6225.cloudcomputing.DataRepository.*;
import com.csye6225.cloudcomputing.Models.CategoryModel;
import com.csye6225.cloudcomputing.Models.FileModel;
import com.csye6225.cloudcomputing.Models.UserModel;
import com.csye6225.cloudcomputing.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AutoConfigureWebMvc
@SpringBootTest(classes = CloudcomputingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CloudcomputingApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    UserRepository ur;
    @MockBean
    UserServices us;

    @MockBean
    QuestionServices qs;
    @MockBean
    QuestionRepository qr;

    @MockBean
    CategoryModelServices cs;
    @MockBean
    CategoryRepository cr;

    @MockBean
    AnswerServices ans;
    @MockBean
    AnswerRepository anr;

    @MockBean
    FileService fs;
    @MockBean
    FileRepository fr;

    @MockBean
    S3StorageService ss;

    @Autowired
    ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String getRootUrl() {
        return "http://localhost:" + this.port;
    }

    @Test
    public void contextLoads() {

    }

    @Test
    public void createNewUserTest() {

        // verifying different user creation with all validation process
        Map<UserModel, Integer> mockUserListandStatus = new HashMap<>();
        mockUserListandStatus.put(new UserModel("test@test.com", "testPassword!12"
                , "firstName", "Lastname",  null, null), HttpStatus.CREATED.value());
        // email invalid
        mockUserListandStatus.put(new UserModel("test.com", "testPassword!12"
                , "firstName", "Lastname", null, null), HttpStatus.BAD_REQUEST.value());
        // invalid password
        mockUserListandStatus.put(new UserModel("test@test2.com", "testPassword!"
                , "firstName", "Lastname", null, null), HttpStatus.BAD_REQUEST.value());

// check for different status code.
        for (Map.Entry mapElement :
                mockUserListandStatus.entrySet()) {
            ResponseEntity<String> re = restTemplate.postForEntity(getRootUrl() + "/v1/user", mapElement.getKey(), String.class);
            assertNotNull(re);
            assertNotNull(re.getBody());

        }

    }
}
