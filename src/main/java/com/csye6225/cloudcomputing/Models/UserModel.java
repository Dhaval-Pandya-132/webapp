package com.csye6225.cloudcomputing.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="Users")
@DynamicUpdate
public class UserModel {

    @Id
    @GeneratedValue( generator = "uuid2" )
    @GenericGenerator( name = "uuid2", strategy = "uuid2" )
    @Column(columnDefinition = "BINARY(16)" )
    private UUID id = UUID.randomUUID();
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Date createdDatetime;
    private Date updatedDatetime;

    @OneToMany(
            mappedBy = "userId",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<QuestionModel> questions;


    public UserModel(){
    }

    public UserModel(String emailAddress, String password, String firstName, String lastName, Date createdDatetime, Date updatedDatetime) {
        this.id = id;
        this.username = emailAddress;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdDatetime = createdDatetime;
        this.updatedDatetime = updatedDatetime;
    }


    public Date getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(Date updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        UserModel userModel = (UserModel) o;
//        return Objects.equals(emailAddress, userModel.emailAddress);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(emailAddress);
//    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime;
    }
}
