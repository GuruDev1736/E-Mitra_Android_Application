package com.emitra.tutionnotesaplication.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.Models.ImageDataModel;
import com.emitra.tutionnotesaplication.R;

public class EditImageAdapter extends FirebaseRecyclerAdapter<ImageDataModel,EditImageAdapter.onviewholder> {

    private Context context ;
    private String noteId ;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public EditImageAdapter(@NonNull FirebaseRecyclerOptions<ImageDataModel> options , Context context , String noteId) {
        super(options);
        this.context = context;
        this.noteId = noteId;
    }

    @Override
    protected void onBindViewHolder(@NonNull onviewholder holder, int position, @NonNull ImageDataModel model) {

        if (model!=null)
        {
        holder.filename.setText(model.getImagename());
        holder.upload.setVisibility(View.GONE);
        holder.see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(v.getContext()).inflate(R.layout.image_show_dialog,null);

                MaterialTextView filename = view.findViewById(R.id.file_name);
                ImageView image = view.findViewById(R.id.image);

                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setView(view)
                        .create();

                filename.setText(model.getImagename());
                Glide.with(v.getContext()).load(model.getLink()).into(image);

                dialog.show();
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(model);
            }
        });

        }

    }

    private void showDeleteConfirmationDialog(ImageDataModel model) {
        MaterialAlertDialogBuilder dialogBuilder = Constants.dialog(context, "Note", "Are you sure you want to delete this Image")
                .setPositiveButton("YES", (dialogInterface, i) -> {
                    deleteNote(model);
                })
                .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void deleteNote(ImageDataModel model) {

        if (noteId!=null && model.getImagekey()!=null)
        {
            ProgressDialog pd = Constants.progress_dialog(context,"Please Wait","Deleting your Image...");
            pd.show();


            database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("Images").child(model.getImagekey()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                Constants.success(context, "Image has successfully deleted");
                            }
                            else
                            {
                                Constants.error(context,"Failed to delete Image : "+ task.getException().getMessage());
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
    public onviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagestorelayout,parent,false);
        return new onviewholder(view);
    }

    class onviewholder extends RecyclerView.ViewHolder {

        MaterialTextView filename ;
        ImageButton see , upload , delete ;
        public onviewholder(@NonNull View itemView) {
            super(itemView);
            filename = itemView.findViewById(R.id.file_name);
            see = itemView.findViewById(R.id.see);
            upload = itemView.findViewById(R.id.upload);
            delete = itemView.findViewById(R.id.remove);
        }
    }
}
