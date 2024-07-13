package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FavoritePdfData {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "pdf_name")
    private String pdfName;

    @ColumnInfo(name = "pdf_path")
    private String pdfPath;


    public FavoritePdfData(int id, String pdfName, String pdfPath) {
        this.id = id;
        this.pdfName = pdfName;
        this.pdfPath = pdfPath;
    }

    public FavoritePdfData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
}
