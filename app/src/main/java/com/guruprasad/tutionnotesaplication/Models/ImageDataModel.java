package com.guruprasad.tutionnotesaplication.Models;

public class ImageDataModel {
    private String imagekey ;
    private String imagename;
    private String link ;

    public ImageDataModel(String imagekey, String imagename, String link) {
        this.imagekey = imagekey;
        this.imagename = imagename;
        this.link = link;
    }

    public ImageDataModel() {
    }

    public String getImagekey() {
        return imagekey;
    }

    public void setImagekey(String imagekey) {
        this.imagekey = imagekey;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
