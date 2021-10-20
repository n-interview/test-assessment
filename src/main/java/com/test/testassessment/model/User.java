package com.test.testassessment.model;


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
    @Column(name = "username")
    private String userName;

    private String password;

    public User(String id, ZonedDateTime creationDate, ZonedDateTime lastUpdated, String fullName, String userName, String password) {
        super(id, creationDate, lastUpdated);
        this.fullName = fullName;
        this.userName = userName;
        this.password = password;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(fullName, user.fullName) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fullName, userName, password);
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
