package com.example.rainbootshopnote;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PdfOptionsDialogFragment extends DialogFragment {

    public interface OnPdfConfirmedListener {
        void onConfirmed(boolean exportFiltered);
    }

    private OnPdfConfirmedListener listener;

    public void setOnPdfConfirmedListener(OnPdfConfirmedListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pdf_options, null);

        RadioButton rbFiltered = v.findViewById(R.id.rbPdfFiltered);
        RadioButton rbAll = v.findViewById(R.id.rbPdfAll);

        if (rbFiltered != null) rbFiltered.setChecked(true);

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setTitle(getString(R.string.pdf_title))
                .setNegativeButton(getString(R.string.cancel), (d, w) -> dismiss())
                .setPositiveButton(getString(R.string.submitCode), (d, w) -> {
                    boolean exportFiltered = rbFiltered != null && rbFiltered.isChecked();
                    if (listener != null) listener.onConfirmed(exportFiltered);
                    dismiss();
                })
                .create();
    }
}
