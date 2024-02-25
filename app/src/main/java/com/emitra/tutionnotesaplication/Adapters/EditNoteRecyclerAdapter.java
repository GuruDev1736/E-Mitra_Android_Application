package com.emitra.tutionnotesaplication.Adapters;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.Models.NoteDataModel;
import com.emitra.tutionnotesaplication.R;

public class EditNoteRecyclerAdapter extends FirebaseRecyclerAdapter<NoteDataModel,EditNoteRecyclerAdapter.Onviewholder> {
    private Context context ;
    private String noteId ;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public EditNoteRecyclerAdapter(@NonNull FirebaseRecyclerOptions<NoteDataModel> options) {
        super(options);
    }

    public EditNoteRecyclerAdapter(@NonNull FirebaseRecyclerOptions<NoteDataModel> options, Context context, String noteId) {
        super(options);
        this.context = context;
        this.noteId = noteId;
    }

    @Override
    protected void onBindViewHolder(@NonNull Onviewholder holder, int position, @NonNull NoteDataModel model) {

        if (model!=null)
        {
            holder.filename.setText(truncateString(model.getFilename(),15));
            holder.see.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(model.getLink()),"application/pdf");
                    try{
                        view.getContext().startActivity(intent);
                    }
                    catch (ActivityNotFoundException e )
                    {
                        Constants.error(view.getContext(),"No application found to display the pdf");
                    }
                }
            });

            holder.upload.setVisibility(View.INVISIBLE);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog(model);
                }
            });
        }


    }

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength - 1) + "...";
        }
    }


    private void showDeleteConfirmationDialog(NoteDataModel model) {
        MaterialAlertDialogBuilder dialogBuilder = Constants.dialog(context, "Note", "Are you sure you want to delete this PDF")
                .setPositiveButton("YES", (dialogInterface, i) -> {
                    deleteNote(model);
                })
                .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void deleteNote(NoteDataModel model) {

        if (noteId!=null && model.getFileKey()!=null)
        {
            ProgressDialog pd = Constants.progress_dialog(context,"Please Wait","Deleting your PDF...");
            pd.show();


            database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("PDF").child(model.getFileKey()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                Constants.success(context, "PDF has successfully deleted");
                            }
                            else
                            {
                                Constants.error(context,"Failed to delete PDF : "+ task.getException().getMessage());
                            }
                            pd.dismiss();
                        }
                    });
        }
        else {
            Constants.error(context,"Unique id is null or file key is null");
        }
    }


    @NonNull
    @Override
    public Onviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_layout,parent,false);
        return new Onviewholder(view);
    }

    public class Onviewholder extends RecyclerView.ViewHolder {

        MaterialTextView filename ;
        ImageButton see , upload , delete ;


        public Onviewholder(@NonNull View itemView) {
            super(itemView);

            filename = itemView.findViewById(R.id.file_name);
            see = itemView.findViewById(R.id.see);
            upload = itemView.findViewById(R.id.upload);
            delete = itemView.findViewById(R.id.remove);

        }
    }
}
