package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters.LockPdfAdapter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters.PdfAdapter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LockPdfActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LockPdfAdapter adapter;
    ArrayList<PdfModel> pdfItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lock_pdf_activty);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        recyclerView = findViewById(R.id.recycler);
        LoadPdf();


    }

    @SuppressLint("NotifyDataSetChanged")
    private void LoadPdf() {
        String[] projection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        ContentResolver contentResolver = getContentResolver();

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{"application/pdf"};

        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        Cursor cursor = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        if (cursor != null) {
            Set<String> uniqueFileIdentifiers = new HashSet<>(); // Use final for immutability

            while (cursor.moveToNext()) {
                try {
                    if (cursor.isNull(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))) {
                        Log.d(TAG, "fetchPDFs: Skipping file with null data");
                        continue; // Skip to the next iteration
                    }

                    int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    String pdfPath = cursor.getString(index);

                    // Combine file name and size for unique identifier
                    File file = new File(pdfPath);
                    String identifier = file.getName() + "_" + file.length();

                    if (!uniqueFileIdentifiers.contains(identifier)) {
                        String pdfDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED));
                        String pdfName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));

                        Log.d(TAG, "fetchPDFs: PDF Path: " + pdfPath);

                        PdfModel model = new PdfModel();
                        model.setPath(pdfPath);
//                        model.setDate(pdfDate.toString()); // Assuming you want the date
                        model.setName(pdfName);
//                        model.setUri(Uri.parse(pdfPath));

                        pdfItems.add(model);
                        uniqueFileIdentifiers.add(identifier);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "fetchPDFs: Error fetching file", e);
                }
            }
            cursor.close();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new LockPdfAdapter(pdfItems, this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Failed to fetch PDFs", Toast.LENGTH_SHORT).show();
        }
    }

}