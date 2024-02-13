package com.guruprasad.tutionnotesaplication.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.Models.ImageStoreModel;
import com.guruprasad.tutionnotesaplication.R;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ImageStoreAdapter extends RecyclerView.Adapter<ImageStoreAdapter.onviewholder> {

    private final Context context;
    private final List<ImageStoreModel> model;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private String uniqueId;

    public ImageStoreAdapter(Context context, List<ImageStoreModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public onviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagestorelayout, parent, false);
        return new onviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull onviewholder holder, int position) {

        ImageStoreModel imageStoreModel = model.get(position);

        holder.filename.setText(imageStoreModel.getImagename());
        if (imageStoreModel.getImageuri() != null) {
            Glide.with(context).load(imageStoreModel.getImageuri()).into(holder.image);
        } else {
            Glide.with(context).load(imageStoreModel.getUri()).into(holder.image);
        }
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position, view.getContext());
            }
        });
        holder.see.setVisibility(View.INVISIBLE);
        holder.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (model.size() > 4) {
                    Constants.error(context, "Please select the images less than 4");
                    return;
                }
                if (imageStoreModel.getUniqueKey() == null) {
                    Constants.error(context, "Reference id is null");
                    return;
                }
                if (imageStoreModel.getImagename() == null) {
                    Constants.error(context, "Image name is null");
                    return;
                }
                if (imageStoreModel.getUserId() == null) {
                    Constants.error(context, "User id is null");
                    return;
                }

                if (imageStoreModel.getUri() != null) {
                    uriUpload(imageStoreModel.getUri(), getItemCount(), imageStoreModel.getUserId(), imageStoreModel.getUniqueKey(), holder, imageStoreModel.getImagename());
                } else if (imageStoreModel.getImageuri() != null) {
                    uploaddata(imageStoreModel.getImageuri(), getItemCount(), imageStoreModel.getUserId(), imageStoreModel.getUniqueKey(), holder, imageStoreModel.getImagename());
                }


            }
        });

    }

    private void uploaddata(byte[] file, int size, String userid, String referenceId, onviewholder holder, String filename) {

        uniqueId = UUID.randomUUID().toString();


        CustomDialog dialog = new CustomDialog(context);
        // dialog.title("Uploading Attachment");
        dialog.show();

        final StorageReference reference = storage.getReference().child("Attachments").child(auth.getCurrentUser().getUid()).child("Images").child(filename);
        reference.putBytes(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        HashMap<String, String> map = new HashMap<>();
                        map.put("link", uri.toString());
                        map.put("imagekey", uniqueId);
                        map.put("imagename", filename);

                        database.getReference().child("Notes").child(userid).child(referenceId).child("Images").child(uniqueId).setValue(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Constants.success(context, "Image Uploaded Successfully");
                                        dialog.dismiss();
                                        holder.upload.setVisibility(View.INVISIBLE);
                                        holder.remove.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Constants.error(context, "Unable to upload Image : " + e.getMessage());
                                        dialog.dismiss();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(context, "Unable to upload Image : " + e.getMessage());
                        dialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Constants.error(context, "Unable to upload Image" +
                        " : " + e.getMessage());
                dialog.dismiss();
            }
        });

    }

    private void uriUpload(Uri file, int size, String userid, String referenceId, onviewholder holder, String filename) {

        uniqueId = UUID.randomUUID().toString();


        CustomDialog dialog = new CustomDialog(context);
        // dialog.title("Uploading Attachment");
        dialog.show();

        final StorageReference reference = storage.getReference().child("Attachments").child(auth.getCurrentUser().getUid()).child("Images").child(filename);
        reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        HashMap<String, String> map = new HashMap<>();
                        map.put("link", uri.toString());
                        map.put("imagekey", uniqueId);
                        map.put("imagename", filename);

                        database.getReference().child("Notes").child(userid).child(referenceId).child("Images").child(uniqueId).setValue(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Constants.success(context, "Image Uploaded Successfully");
                                        dialog.dismiss();
                                        holder.upload.setVisibility(View.INVISIBLE);
                                        holder.remove.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Constants.error(context, "Unable to upload Image : " + e.getMessage());
                                        dialog.dismiss();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(context, "Unable to upload Image : " + e.getMessage());
                        dialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Constants.error(context, "Unable to upload Image" +
                        " : " + e.getMessage());
                dialog.dismiss();
            }
        });

    }


    @Override
    public int getItemCount() {
        return model.size();
    }

    public void removeItem(int position, Context context) {
        if (position >= 0 && position < model.size()) {
            model.remove(position); // Remove the item from the data source
            notifyItemRemoved(position); // Notify the adapter that an item has been removed
        } else {
            Constants.error(context, "Postion Out of Bounds Please try again later");
        }
    }

    class onviewholder extends RecyclerView.ViewHolder {

        MaterialTextView filename;
        ImageButton see, upload, remove;
        ImageView image;

        public onviewholder(@NonNull View itemView) {
            super(itemView);

            filename = itemView.findViewById(R.id.file_name);
            see = itemView.findViewById(R.id.see);
            upload = itemView.findViewById(R.id.upload);
            remove = itemView.findViewById(R.id.remove);
            image = itemView.findViewById(R.id.image);
        }
    }


}
