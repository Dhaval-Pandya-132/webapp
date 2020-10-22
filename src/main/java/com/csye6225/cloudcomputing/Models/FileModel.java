package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


@Entity
@JsonFilter("FileModelFilter")
@JsonPropertyOrder({"file_name"
        , "s3_object_name"
        , "file_id"
        , "created_date"
        , "bucket_name"
        , "sse_algorithm"

})
public class FileModel implements Serializable {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID fileId = UUID.randomUUID();
    private String fileName;
    private String s3ObjectKey;
    private Date createdTimestamp;
    private String s3BucketName;
    private String sseAlgorithm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", nullable = false)
    @JsonIgnore
    private QuestionModel questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "answerId", nullable = true)
    private AnswerModel answerId;

    @JsonGetter("file_id")
    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    @JsonGetter("file_name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonGetter("s3_object_name")
    public String getS3ObjectKey() {
        return s3ObjectKey;
    }

    public void setS3ObjectKey(String s3ObjectKey) {
        this.s3ObjectKey = s3ObjectKey;
    }

    @JsonGetter("created_date")
    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }


    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @JsonGetter("bucket_name")
    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public QuestionModel getQuestionId() {
        return questionId;
    }

    public void setQuestionId(QuestionModel questionId) {
        this.questionId = questionId;
    }

    public AnswerModel getAnswerId() {
        return answerId;
    }

    public void setAnswerId(AnswerModel answerId) {
        this.answerId = answerId;
    }

    @JsonGetter("sse_algorithm")
    public String getSseAlgorithm() {
        return sseAlgorithm;
    }

    public void setSseAlgorithm(String sseAlgorithm) {
        this.sseAlgorithm = sseAlgorithm;
    }
}
