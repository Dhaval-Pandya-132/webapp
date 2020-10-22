package com.csye6225.cloudcomputing.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;


public class S3StorageService {

    AmazonS3 s3;
    String bucketName ;

    public void init(String region , String bucketName) {
        this.bucketName=bucketName;
        s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1").build();

    }

    public List<String> listFiles() {
        List<S3ObjectSummary> summaries = null;


        try {
            ObjectListing objectListing = s3.listObjects(bucketName);
            summaries = objectListing.getObjectSummaries();
        } catch (Exception e) {
            System.out.println("Exception occurred " + e);
        }

        return summaries.stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }


    public PutObjectResult uploadFile(String fileName , String file){
        return s3.putObject(new PutObjectRequest(bucketName, fileName,new File(file)));
    }
    public PutObjectResult uploadFile2(String fileName , File file){
        return s3.putObject(new PutObjectRequest(bucketName, fileName,file));
    }

    public void deleteFile(String filename){
        s3.deleteObject(new DeleteObjectRequest(bucketName,filename));
    }

}
