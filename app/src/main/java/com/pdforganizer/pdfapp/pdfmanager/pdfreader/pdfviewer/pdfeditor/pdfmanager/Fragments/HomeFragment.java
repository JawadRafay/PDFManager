package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Adapters.RecentFilesAdapter;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Database.RoomDatabase;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Model.PdfData;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {


    RecyclerView recyclerView;
    RecentFilesAdapter adapter;
    ArrayList<PdfData> data;
    RoomDatabase database;

    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler);

        init();
        fetchData();

        return view;

    }

    public void init(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        data = new ArrayList<>();
        adapter = new RecentFilesAdapter(data, getContext());
        recyclerView.setAdapter(adapter);
        database = RoomDatabase.getInstance(getContext());
    }
    public void fetchData(){
        database.pdfDataDao().getPdf().observe(getViewLifecycleOwner(), new Observer<List<PdfData>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<PdfData> pdfData) {
                data.clear();
                data.addAll(pdfData);
                adapter.notifyDataSetChanged();
            }
        });
    }
}