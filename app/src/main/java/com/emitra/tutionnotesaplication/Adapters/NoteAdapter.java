package com.emitra.tutionnotesaplication.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.CustomDialog;
import com.emitra.tutionnotesaplication.Models.NoteModel;
import com.emitra.tutionnotesaplication.R;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.onviewholder> {
    private Context context ;
    private List<NoteModel> model ;

    private String UniqueKey;
        FirebaseDatabase database = FirebaseDatabase.getInstance() ;
        FirebaseStorage storage =  FirebaseStorage.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();

    public NoteAdapter(Context context, List<NoteModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public onviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_layout,parent,false);
       return new onviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull onviewholder holder, int position) {

        PackageManager packageManager = context.getPackageManager();

        NoteModel noteModel = model.get(position);

        holder.filename.setText(noteModel.getFilename());
        holder.see.setVisibility(View.INVISIBLE);


        holder.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (noteModel.getFile()==null)
                    {
                        Constants.error(context,"Please select the file");
                        return;
                    }
                    if (model.size()>4)
                    {
                        Constants.error(context,"Please select the files less than 4");
                        return;
                    }
                    if (noteModel.getTitle()==null)
                    {
                        Constants.error(context,"Title is required");
                        return;
                    }
                    if (noteModel.getUserId()==null)
                    {
                        Constants.error(context,"UserId is null");
                        return;
                    }
                    if (noteModel.getUniqueKey()==null)
                    {
                        Constants.error(context,"Reference key is null");
                    }

                    uploaddata(noteModel.getFile() , getItemCount(), noteModel.getUserId(), noteModel.getUniqueKey() , holder , noteModel.getFilename());
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position,view.getContext());
            }
        });


    }

    private void uploaddata(Uri file , int size , String userid , String referenceId , onviewholder holder , String filename) {

        UniqueKey = UUID.randomUUID().toString();


        CustomDialog dialog = new CustomDialog(context);
       // dialog.title("Uploading Attachment");
        dialog.show();

        final StorageReference reference= storage.getReference().child("Attachments").child(auth.getCurrentUser().getUid()).child(filename);
        reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        HashMap<String , String> map = new HashMap<>();
                        map.put("link",uri.toString());
                        map.put("fileKey",UniqueKey);
                        map.put("filename",filename);

                        database.getReference().child("Notes").child(userid).child(referenceId).child("PDF").child(UniqueKey).setValue(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Constants.success(context,"File Uploaded Successfully");
                                        dialog.dismiss();
                                        holder.upload.setVisibility(View.INVISIBLE);
                                        holder.remove.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Constants.error(context,"Unable to upload file : "+e.getMessage());
                                        dialog.dismiss();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(context,"Unable to upload file : "+e.getMessage());
                        dialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Constants.error(context,"Unable to upload file : "+e.getMessage());
                dialog.dismiss();
            }
        });

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public void removeItem(int position , Context context) {
        if (position>=0 && position< model.size())
        {
        model.remove(position); // Remove the item from the data source
        notifyItemRemoved(position); // Notify the adapter that an item has been removed
        }
        else
        {
            Constants.error(context, "Postion Out of Bounds Please try again later");
        }
    }


    public class onviewholder extends RecyclerView.ViewHolder {

        MaterialTextView filename ;
        ImageButton see , upload , remove ;


        public onviewholder(@NonNull View itemView) {
            super(itemView);

            filename = itemView.findViewById(R.id.file_name);
            see = itemView.findViewById(R.id.see);
            upload = itemView.findViewById(R.id.upload);
            remove = itemView.findViewById(R.id.remove);
        }
    }
}
