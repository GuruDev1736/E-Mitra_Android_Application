package com.guruprasad.tutionnotesaplication.Activities.ui.CreateNote;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.guruprasad.tutionnotesaplication.API.ApiInterface;
import com.guruprasad.tutionnotesaplication.API.ApiUtilities;
import com.guruprasad.tutionnotesaplication.Adapters.EditImageAdapter;
import com.guruprasad.tutionnotesaplication.Adapters.EditNoteRecyclerAdapter;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.Models.ApiModel.Page;
import com.guruprasad.tutionnotesaplication.Models.ApiModel.WikipediaResponse;
import com.guruprasad.tutionnotesaplication.Models.ImageDataModel;
import com.guruprasad.tutionnotesaplication.Models.NoteDataModel;
import com.guruprasad.tutionnotesaplication.R;
import com.guruprasad.tutionnotesaplication.databinding.ActivityEditNoteBinding;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    EditNoteRecyclerAdapter adapter;
    EditImageAdapter imageAdapter;
    private ActivityEditNoteBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private Uri file;
    private Uri gallery_Image;
    private String filename;
    private String filepath;
    private String uniqueKey;

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength - 1) + "...";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        ApiInterface apiInterface = ApiUtilities.INSTANCE.getinstance().create(ApiInterface.class);

        Intent intent = getIntent();
        String noteId = intent.getStringExtra("noteId");

        assert noteId != null;

        CustomDialog dialog = new CustomDialog(EditNoteActivity.this);
        //  dialog.title("Loading Data");
        dialog.show();

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.tags, es.dmoral.toasty.R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        binding.tags.setAdapter(arrayAdapter);


        binding.progressbar.setVisibility(View.VISIBLE);
        database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                NoteDataModel model = snapshot.getValue(NoteDataModel.class);
                if (binding != null && model != null) {
                    binding.title.setText(model.getTitle());
                    binding.note.setText(model.getNote());

                    if (model.getTag().equals("Study")) {
                        binding.tags.setSelection(0);
                    }
                    if (model.getTag().equals("Work")) {

                        binding.tags.setSelection(1);
                    }
                    if (model.getTag().equals("Personal")) {

                        binding.tags.setSelection(2);
                    }
                    if (model.getTag().equals("To-Do")) {
                        binding.tags.setSelection(3);
                    }
                    if (model.getTag().equals("Ideas")) {
                        binding.tags.setSelection(4);
                    }
                    if (model.getTag().equals("Meetings")) {
                        binding.tags.setSelection(5);

                    }
                    if (model.getTag().equals("Shopping")) {

                        binding.tags.setSelection(6);
                    }
                    if (model.getTag().equals("Recipes")) {
                        binding.tags.setSelection(7);
                    }
                    if (model.getTag().equals("Travel")) {
                        binding.tags.setSelection(8);

                    }
                    if (model.getTag().equals("Health")) {

                        binding.tags.setSelection(9);
                    }
                    if (model.getTag().equals("Finance")) {

                        binding.tags.setSelection(10);
                    }
                    if (model.getTag().equals("Books")) {

                        binding.tags.setSelection(11);
                    }
                    if (model.getTag().equals("Movies/TV Shows")) {
                        binding.tags.setSelection(12);

                    }
                    if (model.getTag().equals("Hobbies")) {
                        binding.tags.setSelection(13);

                    }
                    if (model.getTag().equals("Goals")) {
                        binding.tags.setSelection(14);

                    }
                    if (model.getTag().equals("Quotes")) {

                        binding.tags.setSelection(15);
                    }
                    if (model.getTag().equals("Miscellaneous")) {
                        binding.tags.setSelection(16);
                    }

                    dialog.dismiss();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Constants.error(EditNoteActivity.this, "Failed to fetch the details : " + error.getMessage());
                dialog.dismiss();
            }
        });


        binding.actionbar.activityName.setText("Edit Note");
        binding.actionbar.options.setVisibility(View.INVISIBLE);

        binding.actionbar.files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String title = binding.title.getText().toString();
                String content = binding.note.getText().toString();

                if (title.isEmpty() || content.isEmpty()) {
                    Constants.error(EditNoteActivity.this, "Cannot Update file on empty note");
                } else {
                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select the File."), 101);
                }
            }
        });

        binding.actionbar.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNoteActivity.super.onBackPressed();
            }
        });


        binding.actionbar.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                View dialogView = LayoutInflater.from(EditNoteActivity.this).inflate(R.layout.imageoptiondialog, null);

                MaterialButton camera = dialogView.findViewById(R.id.camera);
                MaterialButton gallery = dialogView.findViewById(R.id.gallery);

                AlertDialog dialog = new AlertDialog.Builder(EditNoteActivity.this)
                        .setView(dialogView)
                        .create();

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String title = binding.title.getText().toString();
                        String content = binding.note.getText().toString();

                        if (title.isEmpty() || content.isEmpty()) {
                            Constants.error(EditNoteActivity.this, "Cannot Update file on empty note");
                        } else {
                            takePicture(dialog);
                        }
                    }
                });

                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String title = binding.title.getText().toString();
                        String content = binding.note.getText().toString();

                        if (title.isEmpty() || content.isEmpty()) {
                            Constants.error(EditNoteActivity.this, "Cannot Update file on empty note");
                        } else {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, 112);
                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();
            }
        });


        binding.recyclerview.setLayoutManager(new WrapContentLinearLayoutManager(EditNoteActivity.this, LinearLayoutManager.VERTICAL, false));
        Query query = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("PDF");
        FirebaseRecyclerOptions<NoteDataModel> options = new FirebaseRecyclerOptions.Builder<NoteDataModel>().setQuery(query, NoteDataModel.class).build();
        adapter = new EditNoteRecyclerAdapter(options, EditNoteActivity.this, noteId) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(EditNoteActivity.this, "Error : " + error.getMessage());
                binding.progressbar.setVisibility(View.GONE);
            }
        };
        binding.recyclerview.setAdapter(adapter);

        binding.imageRecview.setLayoutManager(new WrapContentLinearLayoutManager(EditNoteActivity.this, LinearLayoutManager.VERTICAL, false));
        Query imagequery = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("Images");
        FirebaseRecyclerOptions<ImageDataModel> imageOptions = new FirebaseRecyclerOptions.Builder<ImageDataModel>().setQuery(imagequery, ImageDataModel.class).build();
        imageAdapter = new EditImageAdapter(imageOptions, EditNoteActivity.this, noteId) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(EditNoteActivity.this, "Error : " + error.getMessage());
                binding.progressbar.setVisibility(View.GONE);
            }
        };
        binding.imageRecview.setAdapter(imageAdapter);


        binding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(EditNoteActivity.this).inflate(R.layout.searchdialog, null);

                EditText editText = dialogView.findViewById(R.id.text);
                ImageButton search = dialogView.findViewById(R.id.search_btn);
                MaterialButton close = dialogView.findViewById(R.id.close);
                MaterialButton copy = dialogView.findViewById(R.id.copy);
                MaterialTextView meaning = dialogView.findViewById(R.id.response);
                ProgressBar progressBar = dialogView.findViewById(R.id.progressbar);


                AlertDialog dialog = new AlertDialog.Builder(EditNoteActivity.this)
                        .setView(dialogView)
                        .create();

                search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (editText.getText().toString().isEmpty()) {
                            Constants.error(EditNoteActivity.this, "Please enter the word to search");
                            return;
                        }

                        progressBar.setVisibility(View.VISIBLE);
                        search.setVisibility(View.GONE);

                        String data = editText.getText().toString();

                        Call<WikipediaResponse> call = apiInterface.getPageSummary("query", "json", "extracts", 1, 1, data);
                        call.enqueue(new Callback<WikipediaResponse>() {
                            @Override
                            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                                if (response.isSuccessful()) {
                                    WikipediaResponse wikipediaResponse = response.body();

                                    if (wikipediaResponse != null) {
                                        com.guruprasad.tutionnotesaplication.Models.ApiModel.Query query = wikipediaResponse.getQuery();
                                        if (query != null) {
                                            Page page = query.getPages().entrySet().iterator().next().getValue();
                                            meaning.setText(page.getExtract());

                                            copy.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clipData = ClipData.newPlainText("text", page.getExtract());
                                                    clipboardManager.setPrimaryClip(clipData);
                                                    Constants.success(EditNoteActivity.this, "Copied to clipboard");
                                                }
                                            });


                                            if (meaning.getText().toString().equals("")) {
                                                Constants.warning(EditNoteActivity.this, "Information is not available");
                                                progressBar.setVisibility(View.GONE);
                                                search.setVisibility(View.VISIBLE);
                                                return;
                                            }
                                            progressBar.setVisibility(View.GONE);
                                            search.setVisibility(View.VISIBLE);
                                        }
                                    }
                                } else {
                                    Constants.error(EditNoteActivity.this, "Failed to get the response");
                                    progressBar.setVisibility(View.GONE);
                                    search.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(Call<WikipediaResponse> call, Throwable t) {
                                Constants.error(EditNoteActivity.this, "Please check the network connection");
                                progressBar.setVisibility(View.GONE);
                                search.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


        binding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = binding.title.getText().toString();
                String note = binding.note.getText().toString();
                String tag = binding.tags.getSelectedItem().toString();

                if (TextUtils.isEmpty(title)) {
                    binding.title.setError("Title should not be empty");
                    return;
                }
                if (TextUtils.isEmpty(note)) {
                    binding.note.setError("Note should not be empty");
                    return;
                }
                if (tag.isEmpty()) {
                    Constants.error(EditNoteActivity.this, "Tag should not be empty");
                    return;
                }

                CustomDialog dialog1 = new CustomDialog(EditNoteActivity.this);
                // dialog1.title("Updating Notes");
                dialog1.show();

                HashMap<String, Object> map = new HashMap<>();
                map.put("title", title);
                map.put("note", note);
                map.put("tag", tag);


                database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Constants.success(EditNoteActivity.this, "Note updated successfully");
                                    dialog1.dismiss();
                                } else {
                                    Constants.error(EditNoteActivity.this, "Failed to update note : " + task.getException().getMessage());
                                    dialog1.dismiss();
                                }
                            }
                        });
            }
        });


    }

    private void takePicture(AlertDialog dialog) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 111);
            dialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            file = data.getData();
            Intent intent = getIntent();
            String noteId = intent.getStringExtra("noteId");

            if (file != null && noteId != null) {

                filename = getFileName(file);
                uniqueKey = UUID.randomUUID().toString();

                CustomDialog dialog = new CustomDialog(EditNoteActivity.this);
                //dialog.title("Updating PDF");
                dialog.show();

                final StorageReference reference = storage.getReference().child("Attachments").child(auth.getCurrentUser().getUid()).child(filename);
                reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, String> map = new HashMap<>();
                                map.put("link", uri.toString());
                                map.put("fileKey", uniqueKey);
                                map.put("filename", filename);

                                assert noteId != null;
                                database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("PDF").child(uniqueKey).setValue(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Constants.success(EditNoteActivity.this, "File Uploaded Successfully");
                                                dialog.dismiss();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Constants.error(EditNoteActivity.this, "Unable to upload file : " + e.getMessage());
                                                dialog.dismiss();
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Constants.error(EditNoteActivity.this, "Unable to upload file : " + e.getMessage());
                                dialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(EditNoteActivity.this, "Unable to upload file : " + e.getMessage());
                        dialog.dismiss();
                    }
                });

            } else {
                Constants.error(EditNoteActivity.this, "File is null or note id is null");
            }

        }


        if (requestCode == 112 && resultCode == RESULT_OK && data != null) {
            gallery_Image = data.getData();
            Intent intent = getIntent();
            String noteId = intent.getStringExtra("noteId");

            if (gallery_Image != null && noteId != null) {

                filename = truncateString(getFileName(gallery_Image), 10);
                uniqueKey = UUID.randomUUID().toString();

                CustomDialog dialog = new CustomDialog(EditNoteActivity.this);
                //dialog.title("Updating PDF");
                dialog.show();

                final StorageReference reference = storage.getReference().child("Attachments").child(auth.getCurrentUser().getUid()).child("Images").child(filename);
                reference.putFile(gallery_Image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, String> map = new HashMap<>();
                                map.put("link", uri.toString());
                                map.put("imagekey", uniqueKey);
                                map.put("imagename", filename);

                                assert noteId != null;
                                database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("Images").child(uniqueKey).setValue(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Constants.success(EditNoteActivity.this, "Image Uploaded Successfully");
                                                dialog.dismiss();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Constants.error(EditNoteActivity.this, "Unable to upload Image : " + e.getMessage());
                                                dialog.dismiss();
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Constants.error(EditNoteActivity.this, "Unable to upload Image : " + e.getMessage());
                                dialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(EditNoteActivity.this, "Unable to upload Image : " + e.getMessage());
                        dialog.dismiss();
                    }
                });

            } else {
                Constants.error(EditNoteActivity.this, "Image is null or note id is null");
            }

        }

        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageData = baos.toByteArray();

            Intent intent = getIntent();
            String noteId = intent.getStringExtra("noteId");

            if (imageData != null && noteId != null) {

                filename = truncateString(UUID.randomUUID().toString(), 10);
                uniqueKey = UUID.randomUUID().toString();

                CustomDialog dialog = new CustomDialog(EditNoteActivity.this);
                //dialog.title("Updating PDF");
                dialog.show();

                final StorageReference reference = storage.getReference().child("Attachments").child(auth.getCurrentUser().getUid()).child("Images").child(filename);
                reference.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, String> map = new HashMap<>();
                                map.put("link", uri.toString());
                                map.put("imagekey", uniqueKey);
                                map.put("imagename", filename);

                                assert noteId != null;
                                database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).child(noteId).child("Images").child(uniqueKey).setValue(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Constants.success(EditNoteActivity.this, "Image Uploaded Successfully");
                                                dialog.dismiss();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Constants.error(EditNoteActivity.this, "Unable to upload Image : " + e.getMessage());
                                                dialog.dismiss();
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Constants.error(EditNoteActivity.this, "Unable to upload Image : " + e.getMessage());
                                dialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Constants.error(EditNoteActivity.this, "Unable to upload Image : " + e.getMessage());
                        dialog.dismiss();
                    }
                });

            } else {
                Constants.error(EditNoteActivity.this, "Image is null or note id is null");
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}