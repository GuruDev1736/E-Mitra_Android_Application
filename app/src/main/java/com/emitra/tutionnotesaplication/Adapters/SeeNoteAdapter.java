package com.emitra.tutionnotesaplication.Adapters;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.Models.NoteDataModel;
import com.emitra.tutionnotesaplication.R;

public class SeeNoteAdapter extends FirebaseRecyclerAdapter<NoteDataModel, SeeNoteAdapter.onviewholder> {


    public SeeNoteAdapter(@NonNull FirebaseRecyclerOptions<NoteDataModel> options) {
        super(options);
    }

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength - 1) + "...";
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull onviewholder holder, int position, @NonNull NoteDataModel model) {

        if (model != null) {
            holder.title.setText(truncateString(model.getFilename(), 15));
            holder.upload.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);

            holder.see.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(model.getLink()), "application/pdf");
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Constants.error(view.getContext(), "No application found to display the pdf");
                    }

                }
            });
        }
    }

    @NonNull
    @Override
    public onviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_layout, parent, false);
        return new onviewholder(view);

    }

    public class onviewholder extends RecyclerView.ViewHolder {

        MaterialTextView title;
        ImageButton see, upload, delete;

        public onviewholder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.file_name);
            see = itemView.findViewById(R.id.see);
            upload = itemView.findViewById(R.id.upload);
            delete = itemView.findViewById(R.id.remove);
        }
    }
}
