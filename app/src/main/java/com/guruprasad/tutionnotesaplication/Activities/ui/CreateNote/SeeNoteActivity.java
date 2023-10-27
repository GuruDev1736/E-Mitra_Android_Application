package com.guruprasad.tutionnotesaplication.Activities.ui.CreateNote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.guruprasad.tutionnotesaplication.Adapters.SeeImageAdapter;
import com.guruprasad.tutionnotesaplication.Adapters.SeeNoteAdapter;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.Models.ImageDataModel;
import com.guruprasad.tutionnotesaplication.Models.NoteDataModel;
import com.guruprasad.tutionnotesaplication.R;
import com.guruprasad.tutionnotesaplication.databinding.ActivitySeeNoteBinding;

public class SeeNoteActivity extends AppCompatActivity {

    ActivitySeeNoteBinding binding ;
    FirebaseDatabase database ;
    FirebaseAuth auth ;
    private SeeNoteAdapter adapter ;
    private SeeImageAdapter image_Adapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();


        CustomDialog dialog = new CustomDialog(SeeNoteActivity.this);
       // dialog.title("Loading Data");
        dialog.show();

        binding.actionbar.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SeeNoteActivity.super.onBackPressed();
            }
        });

        binding.actionbar.activityName.setText("See Note");
        binding.actionbar.files.setVisibility(View.INVISIBLE);
        binding.actionbar.options.setVisibility(View.INVISIBLE);
        binding.actionbar.camera.setVisibility(View.INVISIBLE);
        binding.notetext.setText("Your note is here");


        Intent intent = getIntent();
        String id  = intent.getStringExtra("uniqueId");

        if (id!=null)
        {
            database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    NoteDataModel model = snapshot.getValue(NoteDataModel.class);

                    if (binding!=null && model!=null)
                    {
                        dialog.dismiss();

                    binding.title.setText(model.getTitle());
                    binding.note.setText(model.getNote());
                    binding.tag.setText(model.getTag());
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Constants.error(SeeNoteActivity.this,"Error : "+error.getMessage());
                    dialog.dismiss();
                }
            });

        }


        binding.progressbar.setVisibility(View.VISIBLE);

        binding.imageRecview.setLayoutManager(new WrapContentLinearLayoutManager(SeeNoteActivity.this,LinearLayoutManager.VERTICAL,false));
        Query imageQuery = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(id).child("Images");
        FirebaseRecyclerOptions<ImageDataModel> imageOption = new FirebaseRecyclerOptions.Builder<ImageDataModel>().setQuery(imageQuery, ImageDataModel.class).build();
        image_Adapter = new SeeImageAdapter(imageOption)
        {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(SeeNoteActivity.this,"Image Error : "+error.getMessage());
                binding.progressbar.setVisibility(View.GONE);
            }
        };

        binding.imageRecview.setAdapter(image_Adapter);


        binding.recyclerview.setLayoutManager(new WrapContentLinearLayoutManager(SeeNoteActivity.this,LinearLayoutManager.VERTICAL,false));
        Query query = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(id).child("PDF");
        FirebaseRecyclerOptions<NoteDataModel> options = new FirebaseRecyclerOptions.Builder<NoteDataModel>().setQuery(query,NoteDataModel.class).build();
        adapter = new SeeNoteAdapter(options)
        {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(SeeNoteActivity.this,"Error : "+error.getMessage());
                binding.progressbar.setVisibility(View.GONE);
            }
        };
        binding.recyclerview.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        image_Adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        image_Adapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}