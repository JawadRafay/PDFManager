package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfData;

import java.util.List;
@Dao
public interface PdfDataDao {

    @Insert
    void addPdf(PdfData data);

    @Query("SELECT * FROM PdfData ORDER BY pdfId DESC" )
    LiveData<List<PdfData>> getPdf();

    @Query("SELECT * FROM PdfData WHERE pdf_path = :path LIMIT 1")
    PdfData getPdfByPath(String path);


    @Delete
    void deletePdf(PdfData data);
}
