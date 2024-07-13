package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters.PdfAdapter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FilesFragment extends Fragment {


    private static final String TAG = "FilesFragment";
    private PdfAdapter adapter;
    private static final int MANAGE_PERMISSION_REQUEST_CODE = 1001;
    private RecyclerView recyclerView;
    private String[] PERMISSIONS;
    private static final int REQUEST_PERMISSIONS = 102;
    ArrayList<PdfModel> pdfItems = new ArrayList<>();

    public FilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        recyclerView = view.findViewById(R.id.recylerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Log.d(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.R");
            // Check if MANAGE_APP_ALL_FILES_ACCESS_PERMISSION is granted
            if (Environment.isExternalStorageManager()){
                Log.d(TAG, "Environment.isExternalStorageManager()");
                LoadPdf();
            } else {
                Log.d(TAG, "requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, MANAGE_PERMISSION_REQUEST_CODE);");
                requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, MANAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "Else:   Build.VERSION.SDK_INT >= Build.VERSION_CODES.R");
            // For devices below Android 11, check for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions
            if (arePermissionDenied()) {
                Log.d(TAG, "arePermissionDenied()");
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            } else {
                Log.d(TAG, "else { fetchPDFs();");
                LoadPdf();
            }

        }


        return view;
    }

    private boolean arePermissionDenied() {
        for (String permissions : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MANAGE_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                startActivity(intent);
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void LoadPdf() {
        String[] projection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        ContentResolver contentResolver = getContext().getContentResolver();

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
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new PdfAdapter(pdfItems, getContext());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Failed to fetch PDFs", Toast.LENGTH_SHORT).show();
        }
    }


}
