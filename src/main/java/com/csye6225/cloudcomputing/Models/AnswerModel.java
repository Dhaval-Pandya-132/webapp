package com.csye6225.cloudcomputing.Models;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class AnswerModel {

    @Id
    @GeneratedValue( generator = "uuid2" )
    @GenericGenerator( name = "uuid2", strategy = "uuid2" )
    @Column(columnDefinition = "BINARY(16)" )
    private UUID answerId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionModel questionId;

    private Date createdTimestamp;
    private Date updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel userId;
    private String answerText;


    public AnswerModel() {
    }
}
