package com.example.ashsaccount.safewordproject;

public class RowData {
    private String text;
    private String name;
    private String photoUrl;
    private String fileUrl;
    private boolean lockRow;
    public RowData() {
    }

    public RowData( String text, String name, String photoUrl, String fileUrl) {

            this.text = text;

        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public boolean getLocked() {
        return lockRow;
    }

    public void setLock(boolean lockRow) {
        this.lockRow = lockRow;

    }

}
