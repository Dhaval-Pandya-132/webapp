package com.csye6225.cloudcomputing;


import com.csye6225.cloudcomputing.service.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CloudcomputingApplication extends SpringBootServletInitializer {


    public static void main(String[] args) {
        SpringApplication.run(CloudcomputingApplication.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder
                                                         application) {
        return application.sources(CloudcomputingApplication.class);
    }

    @Autowired
    private Environment env;

    @Bean
    S3StorageService createStorageService() {
        S3StorageService storageService = new S3StorageService();
        storageService.init(env.getProperty("region"),env.getProperty("bucketName"));
        return storageService;
    }
}
