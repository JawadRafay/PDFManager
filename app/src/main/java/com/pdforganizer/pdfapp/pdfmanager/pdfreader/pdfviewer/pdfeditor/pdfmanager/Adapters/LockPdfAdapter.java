package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.PdfViewActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LockPdfAdapter extends RecyclerView.Adapter<LockPdfAdapter.MyViewHolder> {

    private final ArrayList<PdfModel> pdfFiles;
    private final Context context;

    public LockPdfAdapter(ArrayList<PdfModel> pdfFiles, Context context) {
        this.pdfFiles = pdfFiles;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_design, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PdfModel item = pdfFiles.get(position);
        holder.mName.setText(item.getName());
        String pdfPath = item.getPath();
        Log.d("TAG", "onBindViewHolder: "+pdfPath);


        if (pdfPath == null || pdfPath.isEmpty()) {
            Toast.makeText(context, "Path Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFile = new File(item.getPath());
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fd);
            PdfRenderer.Page page = pdfRenderer.openPage(0); // Get the first page
            int width = 200; // Desired width for the preview
            int height = (int) (page.getHeight() * ((float) width / page.getWidth()));

            Bitmap previewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(previewBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            holder.mImage.setImageBitmap(previewBitmap);

            // Close the resources
            page.close();
            pdfRenderer.close();
        } catch (Exception e) {
            Log.e("PdfAdapter", "Error generating preview: " + e.getMessage());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    PDDocument document = PDDocument.load(new File(pdfPath));

                    if (document.isEncrypted()) {
                        Toast.makeText(context, "PDF is already encrypted", Toast.LENGTH_SHORT).show();
                        document.close();
                        return;
                    }

                    showSetPasswordDialog(pdfPath);

                } catch (IOException e) {
                    Toast.makeText(context, "PDF is already encrypted", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDate, mTime, mName;
        ImageView mImage, favorite;
        public MyViewHolder(View itemView) {
            super(itemView);

            mDate = itemView.findViewById(R.id.date);
            mTime = itemView.findViewById(R.id.time);
            mName = itemView.findViewById(R.id.name);
            mImage = itemView.findViewById(R.id.image);
            favorite = itemView.findViewById(R.id.favorite);

        }
    }
    private void showSetPasswordDialog(String pdfPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Password");

        // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            encryptPdf(pdfPath, password);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void encryptPdf(String pdfPath, String password) {
        try {

            PDDocument document = PDDocument.load(new File(pdfPath));

            AccessPermission ap = new AccessPermission();
            ap.setCanPrint(true);
            ap.setCanExtractContent(true);

            StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, ap);
            spp.setEncryptionKeyLength(128);

            document.protect(spp);
            document.save(pdfPath);
            document.close();

            Toast.makeText(context, "PDF encrypted successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "PDF is already encrypted", Toast.LENGTH_SHORT).show();
        }
    }

}
