package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.io.File;

public class PdfViewActivity extends AppCompatActivity {

    PDFView pdfView;
    ImageView back, orientation, menu;
    TextView title;
    String path, name;
    Toolbar toolbar;
    LinearLayout bottomBar, sharePdf, viewMode;
    boolean isFullScreen = false;
    BottomSheetDialog bottomSheetDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pdf_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        listeners();

    }

    public void loadPdf(){
        path = getIntent().getStringExtra("pdfPath");
        name = getIntent().getStringExtra("pdfName");

        pdfView.fromFile(new File(path)).load();
        title.setText(name);

        setDataInBottomSheet();

    }
    private void init() {
        pdfView = findViewById(R.id.pdfView);
        back = findViewById(R.id.back);
        title = findViewById(R.id.fileName);
        toolbar = findViewById(R.id.toolbar);
        bottomBar = findViewById(R.id.bottomBar);
        orientation = findViewById(R.id.orientation);
        menu = findViewById(R.id.menu);
        sharePdf = findViewById(R.id.sharePdf);
        viewMode = findViewById(R.id.viewMode);

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet);

        loadPdf();
    }
    private void setDataInBottomSheet(){

        ImageView pdfImage = bottomSheetDialog.findViewById(R.id.image);
        TextView pdfName = bottomSheetDialog.findViewById(R.id.name);

        // text set
        assert pdfName != null;
        pdfName.setText(name);


        // image set
        try {
            File pdfFile = new File(path);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fd);
            PdfRenderer.Page page = pdfRenderer.openPage(0); // Get the first page
            int width = 200; // Desired width for the preview
            int height = (int) (page.getHeight() * ((float) width / page.getWidth()));

            Bitmap previewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(previewBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            assert pdfImage != null;
            pdfImage.setImageBitmap(previewBitmap);

            // Close the resources
            page.close();
            pdfRenderer.close();
        } catch (Exception e) {
            Log.e("PdfViewActivity", "Error generating preview: " + e.getMessage());
        }
    }
    private void toggleToolbarVisibility() {
        isFullScreen = !isFullScreen;
        if (isFullScreen) {
            hideToolbarWithAnimation();
            hideBottomBarWithAnimation();
        } else {
            showToolbarWithAnimation();
            showBottomBarWithAnimation();
        }
    }
    private void hideToolbarWithAnimation() {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -toolbar.getHeight());
        animate.setDuration(100);
        animate.setFillAfter(true);
        toolbar.startAnimation(animate);
        toolbar.setVisibility(View.GONE);
    }
    private void showToolbarWithAnimation() {
        TranslateAnimation animate = new TranslateAnimation(0, 0, -toolbar.getHeight(), 0);
        animate.setDuration(100);
        animate.setFillAfter(true);
        toolbar.startAnimation(animate);
        toolbar.setVisibility(View.VISIBLE);
    }
    private void hideBottomBarWithAnimation() {
        int translationY = getWindow().getDecorView().getHeight() - bottomBar.getTop();
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, translationY);
        animate.setDuration(100);
        animate.setFillAfter(true);
        bottomBar.startAnimation(animate);
        bottomBar.setVisibility(View.GONE);
    }
    private void showBottomBarWithAnimation() {
        int translationY = getWindow().getDecorView().getHeight() - bottomBar.getTop();
        TranslateAnimation animate = new TranslateAnimation(0, 0, translationY, 0);
        animate.setDuration(100);
        animate.setFillAfter(true);
        bottomBar.startAnimation(animate);
        bottomBar.setVisibility(View.VISIBLE);
    }
    private void rotatePage() {
        int currentOrientation = getResources().getConfiguration().orientation;
        int newOrientation = currentOrientation == Configuration.ORIENTATION_PORTRAIT ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        setRequestedOrientation(newOrientation);
    }
    private void sharePdf() {
        Uri fileUri = FileProvider.getUriForFile(PdfViewActivity.this,
                PdfViewActivity.this.getPackageName() + ".provider", new File(path));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share PDF File");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share PDF File Using"));
    }
    public void listeners(){

        pdfView.setOnClickListener(v -> toggleToolbarVisibility());
        back.setOnClickListener(v -> onBackPressed());
        pdfView.setOnFocusChangeListener((v, hasFocus) -> {
            if (toolbar.getVisibility() == View.VISIBLE) {
                hideToolbarWithAnimation();
            }          });
        orientation.setOnClickListener(v -> rotatePage());
        menu.setOnClickListener(v -> bottomSheetDialog.show());
        sharePdf.setOnClickListener(v -> sharePdf());
        viewMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrientationDialog();
            }
        });

    }
    private void showOrientationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Orientation");
        String[] orientations = {"Horizontal", "Vertical"};
        builder.setItems(orientations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        break;
                    case 1:
                       break;
                }
            }
        });
        builder.show();
    }



}