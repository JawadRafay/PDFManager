package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PdfData {
    @PrimaryKey(autoGenerate = true)
    public int pdfId;

    @ColumnInfo(name = "pdf_path")
    public String pdfPath;

    @ColumnInfo(name = "pdf_name")
    public String pdfName;

    @ColumnInfo(name = "favorite")
    public boolean favorite;

    public PdfData(int pdfId, String pdfPath, String pdfName, boolean favorite) {
        this.pdfId = pdfId;
        this.pdfPath = pdfPath;
        this.pdfName = pdfName;
        this.favorite = favorite;
    }

    public PdfData() {
    }

    public int getPdfId() {
        return pdfId;
    }

    public void setPdfId(int pdfId) {
        this.pdfId = pdfId;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
