package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters.MergeSplitPdfAdapter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.databinding.ActivitySplitBinding;

import java.util.ArrayList;

public class SplitActivity extends AppCompatActivity {
    private static final String TAG = "SplitActivity";
    private ActivitySplitBinding binding;
    private static final int REQUEST_PERMISSIONS = 102;
    private static final int MANAGE_PERMISSION_REQUEST_CODE = 1001;
    private String[] PERMISSIONS;
    ArrayList<PdfModel> modelArrayList;
    MergeSplitPdfAdapter adapter;
    RecyclerView recyclerView;
    ImageView back;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MANAGE_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        PERMISSIONS = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.R");
            // Check if MANAGE_APP_ALL_FILES_ACCESS_PERMISSION is granted
            if (Environment.isExternalStorageManager()) {
                Log.d(TAG, "Environment.isExternalStorageManager()");
                fetchPDFs();
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
                fetchPDFs();
            }
        }

    }

    private boolean arePermissionDenied() {
        for (String permissions : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permissions) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void fetchPDFs() {
        String[] projection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        ContentResolver contentResolver = this.getContentResolver();

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
            while (cursor.moveToNext()) {
                try {
                    if (cursor.isNull(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))) {
                        Log.d(TAG, "fetchPDFs: Skipping file with null data");
                        continue; // Skip to the next iteration
                    }

                    // Check permission before accessing file path
                   /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "fetchPDFs: Permission denied for file");
                        continue;
                    }*/

                    int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);

                    String pdfPath = cursor.getString(index);
                    String pdfDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED));
                    String pdfName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));

                    Log.d("pdfPath", "fetchPDFs: " + pdfPath);

                    PdfModel model = new PdfModel();
                    model.setPath(pdfPath);
                    model.setDate(pdfDate);
                    model.setName(pdfName);
                    modelArrayList.add(model);
                } catch (Exception e) {
                    Log.e(TAG, "fetchPDFs: Error fetching file", e);
                }
            }
            cursor.close();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MergeSplitPdfAdapter(modelArrayList, this, true);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Failed to fetch PDFs", Toast.LENGTH_SHORT).show();
        }
    }
    public void init() {
        recyclerView = binding.rViewPDF;
        modelArrayList = new ArrayList<>();
        back = binding.back;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private boolean arePermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, check if MANAGE_EXTERNAL_STORAGE permission is granted
            return Environment.isExternalStorageManager();
        } else {
            // For devices below Android 11, check for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions
            for (String permission : PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (arePermissionsGranted()) {
            fetchPDFs();
        }
    }
}