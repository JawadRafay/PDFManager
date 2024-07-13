package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.LockPdfActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.MergeActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.Activities.SplitActivity;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.R;


public class ToolsFragment extends Fragment {

    LinearLayout mergePdf, splitPdf, lockPdf;

    public ToolsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);

        mergePdf = view.findViewById(R.id.mergePdf);
        splitPdf = view.findViewById(R.id.splidPdf);
        lockPdf = view.findViewById(R.id.lockPdf);

        splitPdf.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SplitActivity.class);
            startActivity(intent);
        });

        mergePdf.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MergeActivity.class);
            startActivity(intent);
        });

        lockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), LockPdfActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}