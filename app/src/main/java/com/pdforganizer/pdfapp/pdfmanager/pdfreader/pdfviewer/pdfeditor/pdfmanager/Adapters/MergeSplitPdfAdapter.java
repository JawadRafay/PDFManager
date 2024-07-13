package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.SplitterActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfModel;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MergeSplitPdfAdapter extends RecyclerView.Adapter<MergeSplitPdfAdapter.MyViewHolder> {

    private final ArrayList<PdfModel> modelArrayList;
    private final Context context;
    private final boolean isSplitActivity;
    private final SharedPreferences sharedPreferences;
    private final Set<String> selectedPdfPaths;
    private Runnable onSelectionChangedListener;

    public MergeSplitPdfAdapter(ArrayList<PdfModel> items, Context context, boolean isSplitActivity) {
        this.modelArrayList = items;
        this.context = context;
        this.isSplitActivity = isSplitActivity;

        sharedPreferences = context.getSharedPreferences("pdf_adapter_prefs", Context.MODE_PRIVATE);
        // Load previously selected PDF paths from SharedPreferences
        selectedPdfPaths = sharedPreferences.getStringSet("selected_pdfs", new HashSet<>());
    }

    public void setOnSelectionChangedListener(Runnable listener){
        this.onSelectionChangedListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_design1, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PdfModel model = modelArrayList.get(position);

        holder.mName.setText(model.getName());
        holder.mDate.setText(String.valueOf(model.getDate()));

        String pdfPath = model.getPath();

        if (pdfPath == null || pdfPath.isEmpty()) {
            Toast.makeText(context, "Path Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFile = new File(model.getPath());
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


        if (isSplitActivity) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, SplitterActivity.class);
                intent.putExtra("PDF_MODEL", model);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
            //hide checkbox in SplitActivity
            holder.checkBox.setVisibility(View.GONE);
        } else {
            // set checkbox state based on selectedItems
            holder.checkBox.setChecked(selectedPdfPaths.contains(model.getPath()));
        }

        // Handle checkbox selection
        holder.checkBox.setOnClickListener(v -> {
            boolean isChecked = holder.checkBox.isChecked();

            if (isChecked){
                selectedPdfPaths.add(model.getPath()); // Store checkbox state as checked
            } else {
                selectedPdfPaths.remove(model.getPath()); // Remove checkbox state if unchecked

            }
            // Notify the listener (activity) of the selection change
            if (onSelectionChangedListener != null){
                onSelectionChangedListener.run();
            }

        });
    }

    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDate, mTime, mName;
        ImageView mImage;
        CheckBox checkBox;
        MaterialCardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);

            mDate = itemView.findViewById(R.id.date);
            mTime = itemView.findViewById(R.id.time);
            mName = itemView.findViewById(R.id.name);
            mImage = itemView.findViewById(R.id.image);
            checkBox = itemView.findViewById(R.id.checkbox);
            cardView = itemView.findViewById(R.id.card);
        }
    }
    public List<String> getSelectedPdfs() {
        return new ArrayList<>(selectedPdfPaths);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelections(){
        selectedPdfPaths.clear();
        notifyDataSetChanged();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("selected_pdfs", selectedPdfPaths);
        editor.apply();
    }
}

