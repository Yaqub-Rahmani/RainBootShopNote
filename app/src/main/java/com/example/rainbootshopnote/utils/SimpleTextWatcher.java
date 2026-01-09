package com.example.rainbootshopnote.utils;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;

public class SimpleTextWatcher implements TextWatcher {

    public interface OnTextChanged {
        void onChanged(@Nullable CharSequence s);
    }

    private final OnTextChanged cb;

    public SimpleTextWatcher(OnTextChanged cb) {
        this.cb = cb;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (cb != null) cb.onChanged(s);
    }
    @Override public void afterTextChanged(Editable s) {}
}
