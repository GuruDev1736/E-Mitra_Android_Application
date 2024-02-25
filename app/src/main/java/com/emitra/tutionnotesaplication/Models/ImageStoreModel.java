package com.emitra.tutionnotesaplication.Models;

import android.net.Uri;

public class ImageStoreModel {

    private byte[] imageuri ;
    private Uri uri ;
    private String imagename ,uniqueKey , userId;

    public ImageStoreModel( byte[] imageuri, String imagename, String uniqueKey, String userId) {
        this.imageuri = imageuri;
        this.imagename = imagename;
        this.uniqueKey = uniqueKey;
        this.userId = userId;

    }

    public ImageStoreModel(Uri uri, String imagename, String uniqueKey, String userId) {
        this.uri = uri;
        this.imagename = imagename;
        this.uniqueKey = uniqueKey;
        this.userId = userId;
    }

    public ImageStoreModel() {
    }

    public byte[] getImageuri() {
        return imageuri;
    }

    public void setImageuri(byte[] imageuri) {
        this.imageuri = imageuri;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
