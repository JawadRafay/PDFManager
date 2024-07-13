package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters.SplitterViewAdapter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Utils.PDFSplitter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Utils.PDFUtils;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.databinding.ActivitySplitterBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SplitterActivity extends AppCompatActivity {
    private static final String TAG = "SplitterActivity";
    private ActivitySplitterBinding binding;
    private PdfModel model;
    SplitterViewAdapter adapter;
    RecyclerView recyclerView;
    private static final int MAX_PAGES_PER_ITERATION = 30; // Maximum number of pages to load in each iteration
    private int currentPage = 0;
    private int pageCount = 0;
    private final int targetWidth = 200;
    private final int targetHeight = 300;
    private boolean isLoading = false;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplitterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back = findViewById(R.id.back);
        recyclerView = binding.rvPdfTotalPages;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    loadNextPages();
                }
            }
        });

        model = (PdfModel) getIntent().getSerializableExtra("PDF_MODEL");
        if (model != null) {
            loadPdfPagesInBackground();
        } else {
            Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show();
        }

        binding.btnSplit.setOnClickListener(v -> showFileNameDialog());

        back.setOnClickListener(v -> finish());
    }

    private void loadPdfPagesInBackground() {
        new Thread(() -> {
            String pdfPath = model.getPath();
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                try {
                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    pageCount = renderer.getPageCount();
                    loadNextPages();
                } catch (IOException e) {
                    Log.e(TAG, "Error loading PDF pages in background: ", e);
                }
            }
        }).start();
    }

    private void loadNextPages() {
        isLoading = true;
        // Show loading message
        runOnUiThread(() -> binding.loadingMessage.setVisibility(View.VISIBLE));
        new Thread(() -> {
            int startPage = currentPage;
            //long startTime = System.currentTimeMillis();
            ArrayList<Bitmap> pageBitmap = null;// generatePageUrls(pdfFile);

            int endPage = Math.min(currentPage + MAX_PAGES_PER_ITERATION, pageCount);

            try {
                pageBitmap = PDFUtils.getPDFPages(new File(model.getPath()), startPage, endPage, targetWidth, targetHeight);
            } catch (IOException e) {
                Log.e(TAG, "Error loading PDF pages: ", e);
            }

            ArrayList<Bitmap> finalPageBitmaps = pageBitmap;
            runOnUiThread(() -> {
                if (finalPageBitmaps != null) {
                    if (adapter == null) {
                        adapter = new SplitterViewAdapter(finalPageBitmaps, SplitterActivity.this);
                        recyclerView.setAdapter(adapter);
                        // Listen for selection changes
                        adapter.setOnSelectionChangedListener(this::updateSplitButtonVisibility);
                    } else {
                        adapter.addPages(finalPageBitmaps);
                    }
                    currentPage += MAX_PAGES_PER_ITERATION;
                }
                isLoading = false;
                // Hide loading message when loading is complete
                binding.loadingMessage.setVisibility(View.GONE);
            });
        }).start();
    }

    private void updateSplitButtonVisibility() {
        // Show the button if at least one page is selected, hide it otherwise
        if (adapter != null && adapter.getSelectedPages().size() > 0) {
            binding.btnSplit.setVisibility(View.VISIBLE);
        } else {
            binding.btnSplit.setVisibility(View.GONE);
        }
    }

    private void splitPDF(String fileName) {
        // Check if adapter is null
        if (adapter == null) {
            Toast.makeText(this, "Please load PDF pages first", Toast.LENGTH_SHORT).show();
            return;
        }

        SparseBooleanArray selectedPages = adapter.getSelectedPages();
        if (selectedPages.size() == 0) {
            Toast.makeText(this, "No pages selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert SparseBooleanArray to ArrayList of selected page numbers
        ArrayList<Integer> selectedPageNumbers = new ArrayList<>();
        for (int i = 0; i < selectedPages.size(); i++) {
            int key = selectedPages.keyAt(i);
            if (selectedPages.get(key)) {
                selectedPageNumbers.add(key);
            }
        }
        try {
            File pdfFile = new File(model.getPath());

            PDFSplitter pdfSplitter = new PDFSplitter(this);
            pdfSplitter.splitPDF(pdfFile, selectedPageNumbers, fileName);
            finish();
            adapter.clearSelections();
            updateSplitButtonVisibility();
        } catch (Exception e) {
            Log.e(TAG, "Error Split PDF pages: ", e);
            Toast.makeText(this, "Error splitting PDF", Toast.LENGTH_SHORT).show();
            adapter.clearSelections();
            updateSplitButtonVisibility();
        }
    }

    private void showFileNameDialog() {
        EditText editText = new EditText(this);
        editText.setHint("Enter file name");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Save File")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String fileName = editText.getText().toString().trim();
                    if (!fileName.isEmpty()) {
                        splitPDF(fileName);
                    } else {
                        Toast.makeText(SplitterActivity.this, "Please enter a file name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
