package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao.FavoritePdfDao;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao.PdfDataDao;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.FavoritePdfData;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfData;

@Database(entities = {PdfData.class, FavoritePdfData.class}, version = 4)
public abstract class RoomDatabase extends androidx.room.RoomDatabase {

    public static final String DATABASE_NAME = "pdf_database";
    private static RoomDatabase instance = null;
    public static synchronized RoomDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),RoomDatabase.class,DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract PdfDataDao pdfDataDao();
    public abstract FavoritePdfDao favoritePdfDao();

}
