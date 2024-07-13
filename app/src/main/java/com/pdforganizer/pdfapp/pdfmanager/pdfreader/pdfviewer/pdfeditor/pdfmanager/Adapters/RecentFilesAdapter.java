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
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.PdfViewActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Dao.PdfDataDao;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Database.RoomDatabase;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfData;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;

public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFilesAdapter.MyViewHolder> {

    private final ArrayList<PdfData> data;
    private final Context context;

    int count = 1;

    public RecentFilesAdapter(ArrayList<PdfData> data, Context context) {
        this.data = data;
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
        PdfData item = data.get(position);
        holder.mName.setText(item.getPdfName());

        String pdfPath = item.getPdfPath();

        if (pdfPath == null || pdfPath.isEmpty()) {
            Toast.makeText(context, "Path Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFile = new File(item.getPdfPath());
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fd);
            PdfRenderer.Page page = pdfRenderer.openPage(0); // Get the first page
            int width = 200; // Desired width for the preview
            int height = (int) (page.getHeight() * ((float) width / page.getWidth()));

            Bitmap previewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(previewBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            holder.mImage.setImageBitmap(previewBitmap);
            page.close();
            pdfRenderer.close();
        } catch (Exception e) {
            Log.e("PdfAdapter", "Error generating preview: " + e.getMessage());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(context, PdfViewActivity.class);
                intent.putExtra("pdfPath", item.getPdfPath());
                intent.putExtra("pdfName", item.getPdfName());
                context.startActivity(intent);

            }
        });

    }


    @Override
    public int getItemCount() {

        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mDate, mTime, mName;
        ImageView mImage;

        PDFView pdfView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mDate = itemView.findViewById(R.id.date);
            mTime = itemView.findViewById(R.id.time);
            mName = itemView.findViewById(R.id.name);
            mImage = itemView.findViewById(R.id.image);

        }
    }

}
