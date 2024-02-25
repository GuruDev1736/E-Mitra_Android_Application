package com.emitra.tutionnotesaplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.textview.MaterialTextView;

public class CustomDialog extends Dialog {

    private MaterialTextView textView ;

    public CustomDialog(@NonNull Context context  ) {
        super(context);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_progress_dialog);
        setCancelable(false);
        textView = findViewById(R.id.title);

    }

    public void setText(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

}
