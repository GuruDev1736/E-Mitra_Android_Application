package com.guruprasad.tutionnotesaplication.Models;

import java.util.List;

public class NoteDataModel {

    private String title , note , UniqueID , userid , link ,fileKey , filename , tag ;

    public NoteDataModel(String title, String note, String uniqueID, String userid, String tag) {
        this.title = title;
        this.note = note;
        UniqueID = uniqueID;
        this.userid = userid;
        this.tag = tag ;
    }

    public NoteDataModel(String title, String note, String uniqueID, String userid, String link, String fileKey, String filename) {
        this.title = title;
        this.note = note;
        UniqueID = uniqueID;
        this.userid = userid;
        this.link = link;
        this.fileKey = fileKey;
        this.filename = filename;
    }





    public String getTitle() {
        return title;
    }

    public NoteDataModel() {
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUniqueID() {
        return UniqueID;
    }

    public void setUniqueID(String uniqueID) {
        UniqueID = uniqueID;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
