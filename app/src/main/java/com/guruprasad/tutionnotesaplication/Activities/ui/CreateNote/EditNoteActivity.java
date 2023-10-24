package com.guruprasad.tutionnotesaplication.Activities.ui.CreateNote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.guruprasad.tutionnotesaplication.Adapters.EditImageAdapter;
import com.guruprasad.tutionnotesaplication.Adapters.EditNoteRecyclerAdapter;
import com.guruprasad.tutionnotesaplication.Adapters.SeeNoteAdapter;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.Models.ImageDataModel;
import com.guruprasad.tutionnotesaplication.Models.NoteDataModel;
import com.guruprasad.tutionnotesaplication.Models.NoteModel;
import com.guruprasad.tutionnotesaplication.R;
import com.guruprasad.tutionnotesaplication.databinding.ActivityEditNoteBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EditNoteActivity extends AppCompatActivity {


    private ActivityEditNoteBinding binding ;

    private FirebaseDatabase database ;
    private FirebaseAuth auth ;
    private FirebaseStorage storage ;
    private Uri file ;
    private String filename ;
    private String filepath;

    private String uniqueKey;

    EditNoteRecyclerAdapter adapter ;
    EditImageAdapter imageAdapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        Intent intent = getIntent();
        String noteId = intent.getStringExtra("noteId");

        assert noteId != null;

        CustomDialog dialog = new CustomDialog(EditNoteActivity.this);
      //  dialog.title("Loading Data");
        dialog.show();

        binding.progressbar.setVisibility(View.VISIBLE);
        database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                NoteDataModel model = snapshot.getValue(NoteDataModel.class);
                if ( binding!=null && model!=null)
                {
                    binding.title.setText(model.getTitle());
                    binding.note.setText(model.getNote());
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Constants.error(EditNoteActivity.this,"Failed to fetch the details : "+error.getMessage());
                dialog.dismiss();
            }
        });


        binding.actionbar.activityName.setText("Edit Note");
        binding.actionbar.options.setVisibility(View.INVISIBLE);

        binding.actionbar.files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dexter.withContext(EditNoteActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                                String title = binding.title.getText().toString();
                                String content = binding.note.getText().toString();

                                if (title.isEmpty() || content.isEmpty())
                                {
                                    Constants.error(EditNoteActivity.this,"Please upload your note first");
                                }
                                else {
                                    Intent intent = new Intent();
                                    intent.setType("application/pdf");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent,"Select the File."),101);
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        binding.actionbar.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNoteActivity.super.onBackPressed();
            }
        });

        binding.recyclerview.setLayoutManager(new WrapContentLinearLayoutManager(EditNoteActivity.this,LinearLayoutManager.VERTICAL,false));
        Query query = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("PDF");
        FirebaseRecyclerOptions<NoteDataModel> options = new FirebaseRecyclerOptions.Builder<NoteDataModel>().setQuery(query,NoteDataModel.class).build();
        adapter = new EditNoteRecyclerAdapter(options,EditNoteActivity.this,noteId)
        {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(EditNoteActivity.this,"Error : "+error.getMessage());
                binding.progressbar.setVisibility(View.GONE);
            }
        };
        binding.recyclerview.setAdapter(adapter);

        binding.imageRecview.setLayoutManager(new WrapContentLinearLayoutManager(EditNoteActivity.this,LinearLayoutManager.VERTICAL,false));
        Query imagequery = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("Images");
        FirebaseRecyclerOptions<ImageDataModel> imageOptions = new FirebaseRecyclerOptions.Builder<ImageDataModel>().setQuery(imagequery,ImageDataModel.class).build();
        imageAdapter = new EditImageAdapter(imageOptions,EditNoteActivity.this,noteId)
        {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(EditNoteActivity.this,"Error : "+error.getMessage());
                binding.progressbar.setVisibility(View.GONE);
            }
        };
        binding.imageRecview.setAdapter(imageAdapter);


        binding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = binding.title.getText().toString();
                String note = binding.note.getText().toString();

                if (TextUtils.isEmpty(title))
                {
                    binding.title.setError("Title should not be empty");
                    return;
                }
                if (TextUtils.isEmpty(note))
                {
                    binding.note.setError("Note should not be empty");
                    return;
                }

                CustomDialog dialog1 = new CustomDialog(EditNoteActivity.this);
               // dialog1.title("Updating Notes");
                dialog1.show();

                HashMap<String,Object> map = new HashMap<>();
                map.put("title",title);
                map.put("note",note);


                database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    Constants.success(EditNoteActivity.this,"Note updated successfully");
                                    dialog1.dismiss();
                                }
                                else
                                {
                                    Constants.error(EditNoteActivity.this,"Failed to update note : "+task.getException().getMessage());
                                    dialog1.dismiss();
                                }
                            }
                        });
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101  && resultCode==RESULT_OK && data!=null)
        {
            file = data.getData();
            Intent intent = getIntent();
            String noteId = intent.getStringExtra("noteId");

            if (file!=null && noteId!=null)
            {

                filename = getFileName(file);
                uniqueKey = UUID.randomUUID().toString();

                CustomDialog dialog = new CustomDialog(EditNoteActivity.this);
                //dialog.title("Updating PDF");
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
                                map.put("fileKey",uniqueKey);
                                map.put("filename",filename);

                                assert noteId != null;
                                database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("PDF").child(uniqueKey).setValue(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Constants.success(EditNoteActivity.this,"File Uploaded Successfully");
                                                dialog.dismiss();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Constants.error(EditNoteActivity.this,"Unable to upload file : "+e.getMessage());
                                                dialog.dismiss();
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Constants.error(EditNoteActivity.this,"Unable to upload file : "+e.getMessage());
                                dialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(EditNoteActivity.this,"Unable to upload file : "+e.getMessage());
                        dialog.dismiss();
                    }
                });

            }
            else
            {
                Constants.error(EditNoteActivity.this,"File is null or note id is null");
            }

        }
    }


    private String getFileName(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        fileName = cursor.getString(index);
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        return fileName;
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        imageAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        imageAdapter.stopListening();
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