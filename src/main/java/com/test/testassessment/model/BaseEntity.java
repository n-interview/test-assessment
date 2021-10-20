package com.test.testassessment.model;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Base class for all entities that are to be persisted in the application's database
 * Kept as abstract because it bears no information other than supply metadata to the
 * database. It is your duty to ensure that the id field is a valid UUID before
 * persisting. creationDate and lastUpdated will be automatically set as needed
 */

@MappedSuperclass
public abstract class BaseEntity {

    @Column(insertable = false, nullable = false, unique = true, updatable = false)
    @Id
    private String id;
    @Column(columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP", name = "creationdate")
    private ZonedDateTime creationDate;
    @Column(columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP", name = "lastupdated")
    private ZonedDateTime lastUpdated;

    public BaseEntity(String id, ZonedDateTime creationDate, ZonedDateTime lastUpdated) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdated = lastUpdated;
    }

    @PrePersist
    protected void onCreate() {
        lastUpdated = creationDate = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = ZonedDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id='" + id + '\'' +
                ", creationDate=" + creationDate +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdated);
    }
}
