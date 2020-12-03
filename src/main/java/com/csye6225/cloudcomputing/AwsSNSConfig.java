package com.csye6225.cloudcomputing;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AwsSNSConfig {
    // Value is populated with the aws region code
//    @Value("${cloud.aws.region.static}")
//    private String region;

    // @Primary annotation gives a higher preference to a bean (when there are multiple beans of the same type).
    @Primary
    // @Bean annotation tells that a method produces a bean that is to be managed by the spring container.
    @Bean
    public AmazonSNSClient amazonSNSClient() {
        return (AmazonSNSClient) AmazonSNSClientBuilder
                .standard()
                .withRegion("us-east-1")
                .build();
    }
}
