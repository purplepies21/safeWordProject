//RowData holds the information needed for  a single file. used in the items arrayList contained in the GridAdapter and CusomAdapter.

package com.example.ashsaccount.safewordproject;

public class RowData {
    private String text;
    private String name;
    private String photoUrl;
    private String fileUrl;
    private boolean lockRow;
    private String fileID;
    public RowData() {
    }

    public RowData( String text, String name, String photoUrl, boolean locked) {

            this.text = text;

        this.name = name;
        this.photoUrl = photoUrl;
        lockRow=locked;
    }
//simple getter for
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

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }
    public boolean getLock() {
        return lockRow;
    }

    public void setLock(boolean lockRow) {
        this.lockRow = lockRow;

    }

}
