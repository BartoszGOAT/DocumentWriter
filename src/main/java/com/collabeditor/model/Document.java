package com.collabeditor.model;

import java.util.Date;

public class Document {
    private int docId;
    private String title;
    private String content;
    private Date creationDate;
    private Date lastModified;
    private int ownerId;

    // Constructors
    public Document() {
    }

    public Document(int docId, String title, String content, Date creationDate, Date lastModified, int ownerId) {
        this.docId = docId;
        this.title = title;
        this.content = content;
        this.creationDate = creationDate;
        this.lastModified = lastModified;
        this.ownerId = ownerId;
    }

    // Getters and Setters
    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "Document{" +
                "docId=" + docId +
                ", title='" + title + '\'' +
                ", creationDate=" + creationDate +
                ", lastModified=" + lastModified +
                ", ownerId=" + ownerId +
                '}';
    }
}