package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.FavoritePdfData;

import java.util.List;

@Dao
public interface FavoritePdfDao {
    @Insert
    void addFavoritePdf(FavoritePdfData data);

    @Query("SELECT * FROM FavoritePdfData ORDER BY id DESC")
    LiveData<List<FavoritePdfData>> getFavoritePdfs();

    @Query(value = "SELECT * FROM FavoritePdfData WHERE id = :id")
    FavoritePdfData isPdfFavorite(long id);

    @Delete
    void removeFavoritePdf(FavoritePdfData data);
}
