package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Users")
@DynamicUpdate
@JsonFilter("UserModelFilter")
@JsonPropertyOrder({"id"
        , "first_name"
        , "last_name"
        , "username"
        , "account_created"
        , "account_updated"
})
public class UserModel implements Serializable {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;//= UUID.randomUUID();
    private String username;
    private String password;
    private Date createdDatetime;
    private Date updatedDatetime;
    private String firstName;
    private String lastName;


    @OneToMany(
            mappedBy = "userId",
            cascade = CascadeType.PERSIST
    )
    @JsonIgnore
    private Set<QuestionModel> questions;


    public UserModel() {
    }

    public UserModel(String username, String password, String firstName, String lastName, Date createdDatetime, Date updatedDatetime) {
        this.username = username;
        this.password = password;
        this.createdDatetime = createdDatetime;
        this.updatedDatetime = updatedDatetime;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @JsonGetter("first_name")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonGetter("last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonGetter("account_updated")
    public Date getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(Date updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }


    @JsonGetter("id")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JsonGetter("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String emailAddress) {
        this.username = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @JsonGetter("account_created")
    public Date getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime;
    }


//    @Override
//    public String toString() {
//        return this.getId().toString();
//    }
}
