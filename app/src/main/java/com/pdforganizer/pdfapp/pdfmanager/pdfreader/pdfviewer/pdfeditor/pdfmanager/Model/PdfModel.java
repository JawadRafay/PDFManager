package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;

public class PdfModel implements Serializable {

    private String path;
    private int id;
    private int image;
    private String name;
    private String date;
    private long lastModified;

    Uri uri;

    public PdfModel() {
    }

    public PdfModel(String path, int id, int image, String name, String date, long lastModified) {
        this.path = path;
        this.id = id;
        this.image = image;
        this.name = name;
        this.date = date;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}

