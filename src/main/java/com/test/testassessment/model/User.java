package com.test.testassessment.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "fullname")
    private String fullName;
    @Column(name = "username", unique = true, nullable = false)
    private String userName;
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonIgnore
    private String salt;

    public User(String id, ZonedDateTime creationDate, ZonedDateTime lastUpdated, String fullName, String userName, String password, String salt) {
        super(id, creationDate, lastUpdated);
        this.fullName = fullName;
        this.userName = userName;
        this.password = password;
        this.salt = salt;
    }

    public User() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(fullName, user.fullName) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(password, user.password) &&
                Objects.equals(salt, user.salt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fullName, userName, password, salt);
    }

}
