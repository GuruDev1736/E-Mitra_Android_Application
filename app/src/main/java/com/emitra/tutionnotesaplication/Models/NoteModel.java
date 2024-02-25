package com.emitra.tutionnotesaplication.Models;

import android.net.Uri;

public class NoteModel {

    private String filename , title , content, uniqueKey , userId , filepath;
    private Uri file;

    public NoteModel(String filename, Uri file) {
        this.filename = filename;
        this.file = file;
    }

    public NoteModel(String filename, String title, String content, Uri file) {
        this.filename = filename;
        this.title = title;
        this.content = content;
        this.file = file;
    }

    public NoteModel(String filename, String title, String content, String uniqueKey, String userId, Uri file) {
        this.filename = filename;
        this.title = title;
        this.content = content;
        this.uniqueKey = uniqueKey;
        this.userId = userId;
        this.file = file;
    }

    public NoteModel(String filename, String title, String content, String uniqueKey, String userId, String filepath, Uri file) {
        this.filename = filename;
        this.title = title;
        this.content = content;
        this.uniqueKey = uniqueKey;
        this.userId = userId;
        this.filepath = filepath;
        this.file = file;
    }



    public NoteModel() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Uri getFile() {
        return file;
    }

    public void setFile(Uri file) {
        this.file = file;
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

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}
