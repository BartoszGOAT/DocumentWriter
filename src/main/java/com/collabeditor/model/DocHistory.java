package com.collabeditor.model;

import java.util.Date;

public class DocHistory {
    private int historyId;
    private int docId;
    private String content;
    private int userId;
    private Date timestamp;

    // Constructors
    public DocHistory() {
    }

    public DocHistory(int historyId, int docId, String content, int userId, Date timestamp) {
        this.historyId = historyId;
        this.docId = docId;
        this.content = content;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DocHistory{" +
                "historyId=" + historyId +
                ", docId=" + docId +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                '}';
    }
}