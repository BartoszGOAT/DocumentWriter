package com.collabeditor.model;

public class Permission {
    private int permId;
    private int userId;
    private int docId;
    private boolean readAccess;
    private boolean writeAccess;
    private boolean adminAccess;

    // Constructors
    public Permission() {
    }

    public Permission(int permId, int userId, int docId, boolean readAccess, boolean writeAccess, boolean adminAccess) {
        this.permId = permId;
        this.userId = userId;
        this.docId = docId;
        this.readAccess = readAccess;
        this.writeAccess = writeAccess;
        this.adminAccess = adminAccess;
    }

    // Getters and Setters
    public int getPermId() {
        return permId;
    }

    public void setPermId(int permId) {
        this.permId = permId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public boolean isReadAccess() {
        return readAccess;
    }

    public void setReadAccess(boolean readAccess) {
        this.readAccess = readAccess;
    }

    public boolean isWriteAccess() {
        return writeAccess;
    }

    public void setWriteAccess(boolean writeAccess) {
        this.writeAccess = writeAccess;
    }

    public boolean isAdminAccess() {
        return adminAccess;
    }

    public void setAdminAccess(boolean adminAccess) {
        this.adminAccess = adminAccess;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "permId=" + permId +
                ", userId=" + userId +
                ", docId=" + docId +
                ", readAccess=" + readAccess +
                ", writeAccess=" + writeAccess +
                ", adminAccess=" + adminAccess +
                '}';
    }
}