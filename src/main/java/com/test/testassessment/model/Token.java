package com.test.testassessment.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Token {

    private String content;

    private ZonedDateTime expiryDate;

    public Token(String content, ZonedDateTime expiryDate) {
        this.content = content;
        this.expiryDate = expiryDate;
    }

    public Token(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(ZonedDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(content, token.content) &&
                Objects.equals(expiryDate, token.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, expiryDate);
    }

    @Override
    public String toString() {
        return "Token{" +
                "content='" + content + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }

}
