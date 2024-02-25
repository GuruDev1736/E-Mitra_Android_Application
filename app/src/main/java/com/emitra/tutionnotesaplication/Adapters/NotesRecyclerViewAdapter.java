package com.emitra.tutionnotesaplication.Adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.emitra.tutionnotesaplication.Activities.ui.CreateNote.EditNoteActivity;
import com.emitra.tutionnotesaplication.Activities.ui.CreateNote.SeeNoteActivity;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.Models.NoteDataModel;
import com.emitra.tutionnotesaplication.R;

import java.util.List;

public class NotesRecyclerViewAdapter extends FirebaseRecyclerAdapter<NoteDataModel, NotesRecyclerViewAdapter.onViewHolder> {

    private final Context context;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public NotesRecyclerViewAdapter(@NonNull FirebaseRecyclerOptions<NoteDataModel> options, Context context) {
        super(options);
        this.context = context;
    }

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength - 1) + "...";
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull onViewHolder holder, int position, @NonNull NoteDataModel model) {

        String title = truncateString(model.getTitle(), 15);
        holder.title.setText("Title : " + title);
        holder.description.setText("Description : " + truncateString(model.getNote(), 15));
        holder.tag.setText("Tag : " + model.getTag());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent seeNoteIntent = new Intent(context, SeeNoteActivity.class);
                seeNoteIntent.putExtra("uniqueId", model.getUniqueID());
                context.startActivity(seeNoteIntent);
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = "Title : " + model.getTitle() + "\n" + "Description : " + model
                        .getNote();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, message);

                PackageManager packageManager = holder.itemView.getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                boolean isWhatsAppInstalled = false;
                boolean isEmailInstalled = false;

                for (ResolveInfo info : activities) {
                    if (info.activityInfo.packageName.equals("com.whatsapp")) {
                        isWhatsAppInstalled = true;
                        intent.setPackage("com.whatsapp");
                        holder.itemView.getContext().startActivity(intent);
                        break;
                    } else if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                        isEmailInstalled = true;
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "This My Note from E-Mitra Application");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
                        holder.itemView.getContext().startActivity(Intent.createChooser(emailIntent, "Send email"));
                    }
                }

                if (!isWhatsAppInstalled && !isEmailInstalled) {
                    holder.itemView.getContext().startActivity(Intent.createChooser(intent, "Share via"));
                }
            }
        });

        holder.see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent seeNoteIntent = new Intent(context, SeeNoteActivity.class);
                seeNoteIntent.putExtra("uniqueId", model.getUniqueID());
                context.startActivity(seeNoteIntent);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editNoteIntent = new Intent(context, EditNoteActivity.class);
                editNoteIntent.putExtra("noteId", model.getUniqueID());
                context.startActivity(editNoteIntent);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog(model);
            }
        });

    }


    private void showDeleteConfirmationDialog(NoteDataModel model) {
        MaterialAlertDialogBuilder dialogBuilder = Constants.dialog(context, "Note", "Are you sure you want to delete this note")
                .setPositiveButton("YES", (dialogInterface, i) -> {
                    deleteNote(model);
                })
                .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void deleteNote(NoteDataModel model) {

        if (model.getUniqueID() != null) {
            ProgressDialog pd = Constants.progress_dialog(context, "Please Wait", "Deleting your note...");
            pd.show();


            database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(model.getUniqueID()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Constants.success(context, "Notes has successfully deleted");
                            } else {
                                Constants.error(context, "Failed to delete note : " + task.getException().getMessage());
                            }
                            pd.dismiss();
                        }
                    });
        } else {
            Constants.error(context, "Unique id is null");
        }


    }

    @Override
    public int getItemCount() {
        return getSnapshots().size();
    }

    @NonNull
    @Override
    public onViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notelayout, parent, false);
        return new onViewHolder(view);
    }

    public class onViewHolder extends RecyclerView.ViewHolder {

        ImageView logo;
        MaterialTextView title, description, tag;
        ImageButton see, edit, delete, share;

        MaterialCardView layout;

        public onViewHolder(@NonNull View itemView) {
            super(itemView);

            logo = itemView.findViewById(R.id.logo);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            tag = itemView.findViewById(R.id.tag);
            see = itemView.findViewById(R.id.see);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            share = itemView.findViewById(R.id.share);
            layout = itemView.findViewById(R.id.layout);


        }
    }
}
