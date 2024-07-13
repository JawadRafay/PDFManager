package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.PdfViewActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao.FavoritePdfDao;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao.PdfDataDao;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Database.RoomDatabase;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.FavoritePdfData;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfData;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.MyViewHolder> {

    private final ArrayList<PdfModel> pdfFiles;
    private final Context context;
    private final RoomDatabase roomDatabase;
    private final PdfDataDao pdfDataDao;
//    private final FavoritePdfDao favoritePdfDao;
    int count = 1;

    public PdfAdapter(ArrayList<PdfModel> pdfFiles, Context context) {
        this.pdfFiles = pdfFiles;
        this.context = context;
        this.roomDatabase = RoomDatabase.getInstance(context);
        this.pdfDataDao = roomDatabase.pdfDataDao();
//        this.favoritePdfDao = roomDatabase.favoritePdfDao();

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
                RoomDatabase roomDatabase = RoomDatabase.getInstance(context);
                PdfDataDao pdfDataDao = roomDatabase.pdfDataDao();

                Intent intent = new Intent(context, PdfViewActivity.class);
                intent.putExtra("pdfPath", item.getPath());
                intent.putExtra("pdfName", item.getName());
                context.startActivity(intent);

                // Check if the PDF data already exists in the database
                PdfData existingPdfData = pdfDataDao.getPdfByPath(item.getPath());
                if (existingPdfData != null) {
                    // Data already exists, show a toast and return
                    Toast.makeText(context, "Data Already Inserted", Toast.LENGTH_SHORT).show();

                    return;
                } else {

                    //if not exists then this will work
                    PdfData pdfData = new PdfData();
                    pdfData.setPdfName(item.getName());
                    pdfData.setPdfPath(item.getPath());
                    pdfDataDao.addPdf(pdfData);

                    Toast.makeText(context, "Data Added", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        setFavoriteIcon(holder.favorite, item);


        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite(item, holder.favorite);
            }
        });

    }

    private void toggleFavorite(PdfModel pdfModel, ImageView imageView) {
    }


    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mDate, mTime, mName;
        ImageView mImage, favorite;
        PDFView pdfView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mDate = itemView.findViewById(R.id.date);
            mTime = itemView.findViewById(R.id.time);
            mName = itemView.findViewById(R.id.name);
            mImage = itemView.findViewById(R.id.image);
//          pdfView = itemView.findViewById(R.id.pdfView);
            favorite = itemView.findViewById(R.id.favorite);


        }
    }



}