package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.util.ArrayList;

public class SplitterViewAdapter extends RecyclerView.Adapter<SplitterViewAdapter.MyViewHolder> {

    private final ArrayList<Bitmap> pageBitmaps;
    //private Set<Integer> selectedPages = new HashSet<>();
    private final SparseBooleanArray selectedPages = new SparseBooleanArray(); // Store checkbox state
    private final Context context;
    private Runnable onSelectionChangedListener;

    public SplitterViewAdapter(ArrayList<Bitmap> pageBitmaps, Context context) {
        this.pageBitmaps = pageBitmaps;
        this.context = context;
    }

    public void setOnSelectionChangedListener(Runnable listener){
        this.onSelectionChangedListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelections() {
        selectedPages.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.splitter_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Bitmap pageBitmap = pageBitmaps.get(position);
        holder.ivPages.setImageBitmap(pageBitmap);
        holder.checkBox.setChecked(selectedPages.get(position, false)); // Retrieve checkbox state, default is false



        holder.checkBox.setOnClickListener(view -> {
            if (holder.checkBox.isChecked()) {
                selectedPages.put(position, true);
                holder.cardView.setStrokeColor(context.getResources().getColor(R.color.blue));
            } else {
                selectedPages.delete(position); // Remove checkbox state if unchecked
                holder.cardView.setStrokeColor(context.getResources().getColor(R.color.gray));

            }

            // Notify the listener (activity) of the selection change
            if (onSelectionChangedListener != null){
                onSelectionChangedListener.run();
            }
        });

    }

    @Override
    public int getItemCount() {
        return pageBitmaps.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPages;
        CheckBox checkBox;
        MaterialCardView cardView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPages = itemView.findViewById(R.id.ivPages);
            checkBox = itemView.findViewById(R.id.checkbox);
            cardView = itemView.findViewById(R.id.imageCard);
        }
    }

    public void addPages(ArrayList<Bitmap> newPageUrls) {
        int startPosition = pageBitmaps.size();
        pageBitmaps.addAll(newPageUrls);
        notifyItemRangeInserted(startPosition, newPageUrls.size());
    }

    public SparseBooleanArray getSelectedPages() {
        return selectedPages;
    }

}
