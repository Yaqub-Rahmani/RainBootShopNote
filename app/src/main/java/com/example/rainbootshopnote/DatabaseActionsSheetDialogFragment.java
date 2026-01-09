package com.example.rainbootshopnote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DatabaseActionsSheetDialogFragment extends BottomSheetDialogFragment {

    public interface Listener {
        void onReportsPdf();
        void onExportDownloads();
        void onImportDb();     // Uri burada yok — picker SheetDialogFragment içinde
        void onSendOnline();   // dialog burada yok — SheetDialogFragment açacak
    }

    private Listener listener;
    public void setListener(Listener l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottom_sheet_db_actions, container, false);

        Button btnReports = v.findViewById(R.id.btn_db_reports);
        Button btnExport  = v.findViewById(R.id.btn_db_export);
        Button btnImport  = v.findViewById(R.id.btn_db_import);
        Button btnOnline  = v.findViewById(R.id.btn_db_online);

        btnReports.setOnClickListener(x -> {
            if (listener != null) listener.onReportsPdf();
            dismiss();
        });

        btnExport.setOnClickListener(x -> {
            if (listener != null) listener.onExportDownloads();
            dismiss();
        });

        btnImport.setOnClickListener(x -> {
            if (listener != null) listener.onImportDb();
            dismiss();
        });

        btnOnline.setOnClickListener(x -> {
            if (listener != null) listener.onSendOnline();
            dismiss();
        });

        return v;
    }
}
