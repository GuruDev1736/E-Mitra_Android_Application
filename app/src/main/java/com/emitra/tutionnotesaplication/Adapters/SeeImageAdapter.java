package com.emitra.tutionnotesaplication.Adapters;

import android.app.AlertDialog;
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
import com.google.android.material.textview.MaterialTextView;
import com.emitra.tutionnotesaplication.Models.ImageDataModel;
import com.emitra.tutionnotesaplication.R;

public class SeeImageAdapter extends FirebaseRecyclerAdapter<ImageDataModel,SeeImageAdapter.onviewholder> {

    public SeeImageAdapter(@NonNull FirebaseRecyclerOptions<ImageDataModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull onviewholder holder, int position, @NonNull ImageDataModel model) {

        if (model!=null)
        {
            holder.filename.setText(model.getImagename());
            holder.upload.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
            holder.see.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.image_show_dialog,null);

                    ImageView image = dialogView.findViewById(R.id.image);
                    MaterialTextView filename = dialogView.findViewById(R.id.file_name);

                    AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                            .setView(dialogView)
                            .create();


                    Glide.with(v.getContext()).load(model.getLink()).into(image);
                    filename.setText(model.getImagename());

                    dialog.show();
                }
            });
        }

    }

    @NonNull
    @Override
    public onviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagestorelayout , parent,false);
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
