package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFUtils {
    public static ArrayList<Bitmap> getPDFPages(File file, int startPage, int endPage, int targetWidth, int targetHeight) throws IOException {
        ArrayList<Bitmap> pageBitmaps = new ArrayList<>();

        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            for (int i = startPage; i < endPage; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888; // Reduced quality
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = bitmapConfig;
                options.inSampleSize = calculateInSampleSize(page.getWidth(), page.getHeight(), targetWidth, targetHeight);

                Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, bitmapConfig);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                Log.d("bitmap", "getPDFPages: bitmap" + bitmap.getWidth() + " " + bitmap.getHeight());

                pageBitmaps.add(bitmap);
                page.close();
            }

            renderer.close();
        } catch (IOException e) {
            Log.e("PDFUtils", "Error getting PDF pages: " + e.getMessage());
            throw e;
        }

        return pageBitmaps;
    }
    private static int calculateInSampleSize(int originalWidth, int originalHeight, int desiredWidth, int desiredHeight) {
        int inSampleSize = 1;

        if (originalWidth > desiredWidth || originalHeight > desiredHeight) {
            int halfWidth = originalWidth / 2;
            int halfHeight = originalHeight / 2;

            while ((halfWidth / inSampleSize) >= desiredWidth && (halfHeight / inSampleSize) >= desiredHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static ArrayList<Bitmap> getPDFPagesForMerge(List<File> file) throws IOException {
        ArrayList<Bitmap> pageBitmaps = new ArrayList<>();

        try {
            for (File file1 : file) {
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file1, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                int pageCount = renderer.getPageCount();

                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


                    pageBitmaps.add(bitmap);
                    page.close();
                }

                renderer.close();
            }
        } catch (IOException e) {
            Log.e("PDFUtils", "Error getting PDF pages: " + e.getMessage());
            throw e;
        }

        return pageBitmaps;
    }
    public static void mergePDFs(List<Bitmap> bitmaps, String mergedFileName, Context context) throws IOException {
        new Thread(() -> {
            PdfDocument document = new PdfDocument();

            for (Bitmap bitmap : bitmaps) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                document.finishPage(page);
            }

            savePDF(context, document, mergedFileName);

            document.close();
        }).start();
    }
    private static void savePDF(Context context, PdfDocument document, String filename) {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Merger");
        if (!directory.exists()) {
            boolean mkdirsSuccess = directory.mkdirs();
            if (!mkdirsSuccess) {
                Log.e("PDFMerger", "Failed to create output directory");
                return;
            }
        }


        String mergePDFPath = directory + File.separator + filename + ".pdf";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mergePDFPath);
            document.writeTo(fileOutputStream);
            document.close();

            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "PDF merged successfully", Toast.LENGTH_SHORT).show() );
        } catch (IOException e) {
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Error splitting PDF pages: " + e.getMessage(), Toast.LENGTH_SHORT).show() );
            throw new RuntimeException(e);
        }
    }

}



