package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PDFSplitter {

    String outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Splitter/";

    private final Context context;

    public PDFSplitter(Context context) {
        this.context = context;
    }

    public void splitPDF(File inputFile, ArrayList<Integer> selectedPageNumbers, String fileName) {
        new Thread(()-> {
            try {
                splitPDFInBackground(inputFile, selectedPageNumbers, fileName);
            } catch (IOException e) {
                Log.e("PDFSplitter", "Error splitting PDF: " + e.getMessage());
            }
        }).start();
    }

    private void splitPDFInBackground(File inputFile, ArrayList<Integer> selectedPageNumbers, String fileName) throws IOException {
        // Create input file
        if (!inputFile.exists()) {
            Log.e("PDFSplitter", "Input file does not exist");
            return;
        }

        //Make directory
        File directory = new File(outputFilePath);
        if (!directory.exists()) {
            boolean mkdirsSuccess = directory.mkdirs();
            if (!mkdirsSuccess){
                Log.e("PDFSplitter", "Failed to create output directory");
                return;
            }
        }

        // Split PDF
        splitPDFPages(inputFile, selectedPageNumbers, fileName);
    }

    private int getTotalPages(File file) throws IOException {
        try {
            PdfRenderer renderer = new PdfRenderer(Objects.requireNonNull(context.getContentResolver().openFileDescriptor(Uri.fromFile(file), "r")));
            int pageCount = renderer.getPageCount();
            renderer.close();
            return pageCount;
        } catch (IOException e) {
            Log.e("PDFSplitter", "Error getting total pages: " + e.getMessage());
            throw e;
        }
    }

    private void splitPDFPages(File inputFile, ArrayList<Integer> selectedPageNumbers, String fileName) throws IOException {
        try {
            PdfRenderer renderer = new PdfRenderer(Objects.requireNonNull(context.getContentResolver().openFileDescriptor(Uri.fromFile(inputFile), "r")));

            PdfDocument pdfDoc = new PdfDocument();

            for (int pageNumber : selectedPageNumbers) {
                PdfRenderer.Page page = renderer.openPage(pageNumber);

                // Use ARGB_8888 bitmap configuration (default and commonly supported)
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                        Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(page.getWidth(), page.getHeight(), pageNumber).create();
                PdfDocument.Page pdfPage = pdfDoc.startPage(pageInfo);
                Canvas canvas = pdfPage.getCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                pdfDoc.finishPage(pdfPage);

                bitmap.recycle();
                page.close();
            }

            String splitPdfPath = outputFilePath + File.separator + fileName + ".pdf";
            FileOutputStream fos = new FileOutputStream(splitPdfPath);
            pdfDoc.writeTo(fos);
            pdfDoc.close();
            fos.close();

            renderer.close();

            // Show success message on the UI thread
            showToastOnUIThread();

        } catch (IOException e) {
            Log.e("PDFSplitter", "Error splitting PDF pages: " + e.getMessage());
            throw e;
        }
    }

    private void showToastOnUIThread() {
        if (context instanceof Activity) {
            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, "PDF split successfully", Toast.LENGTH_SHORT).show());

        } else {
            Log.e("PDFSplitter", "Context is not an instance of Activity");
        }
    }

}


