package com.emitra.tutionnotesaplication.Activities.ui.CreateNote;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
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
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.emitra.tutionnotesaplication.API.ApiInterface;
import com.emitra.tutionnotesaplication.API.ApiUtilities;
import com.emitra.tutionnotesaplication.Adapters.ImageStoreAdapter;
import com.emitra.tutionnotesaplication.Adapters.NoteAdapter;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.CustomDialog;
import com.emitra.tutionnotesaplication.Models.ApiModel.Page;
import com.emitra.tutionnotesaplication.Models.ApiModel.Query;
import com.emitra.tutionnotesaplication.Models.ApiModel.WikipediaResponse;
import com.emitra.tutionnotesaplication.Models.ImageStoreModel;
import com.emitra.tutionnotesaplication.Models.NoteDataModel;
import com.emitra.tutionnotesaplication.Models.NoteModel;
import com.emitra.tutionnotesaplication.R;
import com.emitra.tutionnotesaplication.Receiver.AlaramReceiver;
import com.emitra.tutionnotesaplication.databinding.ActivityCreateNoteBinding;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TextToSpeech.OnInitListener {

    public static final String ACTION_CUSTOM_BROADCAST = "com.guruprasad.tutionnotesaplication.ACTION_CUSTOM_BROADCAST";
    ActivityCreateNoteBinding binding;
    List<NoteModel> datalist = new ArrayList<>();
    List<ImageStoreModel> imagelist = new ArrayList<>();
    NoteAdapter adapter;
    ImageStoreAdapter imageAdapter;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    private String filename;
    private String imageName;
    private Uri file;
    private Uri imageUri;
    private int count;
    private String UniqueKey;
    private String UserId;
    private String filepath;
    private AlarmManager alarmManager;
    private static final int REQUEST_CODE_VOICE_INPUT = 1736;

    private TextToSpeech textToSpeech;

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
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        UniqueKey = UUID.randomUUID().toString();
        UserId = auth.getCurrentUser().getUid();

        ApiInterface apiInterface = ApiUtilities.INSTANCE.getinstance().create(ApiInterface.class);

        textToSpeech = new TextToSpeech(this, this);

        binding.actionbar.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNoteActivity.super.onBackPressed();
            }
        });

        binding.actionbar.activityName.setText("Create Note");
        binding.actionbar.options.setVisibility(View.GONE);
        binding.actionbar.files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = binding.title.getText().toString();
                String content = binding.note.getText().toString();

                if (title.isEmpty() || content.isEmpty()) {
                    Constants.error(CreateNoteActivity.this, "Please upload your note first");
                } else {
                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select the File."), 101);
                }
            }
        });


        binding.actionbar.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(CreateNoteActivity.this).inflate(R.layout.imageoptiondialog, null);

                MaterialButton camera = dialogView.findViewById(R.id.camera);
                MaterialButton gallery = dialogView.findViewById(R.id.gallery);

                AlertDialog dialog = new AlertDialog.Builder(CreateNoteActivity.this)
                        .setView(dialogView)
                        .create();

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        String title = binding.title.getText().toString();
                        String content = binding.note.getText().toString();

                        if (title.isEmpty() || content.isEmpty()) {
                            Constants.error(CreateNoteActivity.this, "Please upload your note first");
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
                            Constants.error(CreateNoteActivity.this, "Please upload your note first");
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


        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this, datalist);
        count = adapter.getItemCount();
        binding.recyclerview.setAdapter(adapter);

        binding.ImageRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageStoreAdapter(this, imagelist);
        binding.ImageRecyclerview.setAdapter(imageAdapter);


        binding.create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomDialog dialog = new CustomDialog(CreateNoteActivity.this);

                String title = binding.title.getText().toString();
                String note = binding.note.getText().toString();
                String tag = binding.tags.getSelectedItem().toString();

                if (TextUtils.isEmpty(title)) {
                    Constants.error(CreateNoteActivity.this, "Title is necessary before creating the note");
                    return;
                }
                if (TextUtils.isEmpty(note)) {
                    Constants.error(CreateNoteActivity.this, "Note is null please enter the input");
                    return;
                }
                if (tag.isEmpty()) {
                    Constants.error(CreateNoteActivity.this, "Tag should not be empty");
                    return;
                }

                dialog.show();


                NoteDataModel model = new NoteDataModel(title, note, UniqueKey, UserId, tag);
                database.getReference().child("Notes").child(UserId).child(UniqueKey).setValue(model)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Constants.success(CreateNoteActivity.this, "Note Created Successfully");
                                    //  setAlarm();
                                    binding.create.setVisibility(View.INVISIBLE);
                                    dialog.dismiss();
                                } else {
                                    Constants.error(CreateNoteActivity.this, "Failed to create Note : " + task.getException().getMessage());
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        });


        binding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(CreateNoteActivity.this).inflate(R.layout.searchdialog, null);

                EditText editText = dialogView.findViewById(R.id.text);
                ImageButton search = dialogView.findViewById(R.id.search_btn);
                MaterialButton close = dialogView.findViewById(R.id.close);
                MaterialButton copy = dialogView.findViewById(R.id.copy);
                MaterialTextView meaning = dialogView.findViewById(R.id.response);
                ProgressBar progressBar = dialogView.findViewById(R.id.progressbar);


                AlertDialog dialog = new AlertDialog.Builder(CreateNoteActivity.this)
                        .setView(dialogView)
                        .create();

                search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (editText.getText().toString().isEmpty()) {
                            Constants.error(CreateNoteActivity.this, "Please enter the word to search");
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
                                        Query query = wikipediaResponse.getQuery();
                                        if (query != null) {
                                            Page page = query.getPages().entrySet().iterator().next().getValue();
                                            meaning.setText(page.getExtract());

                                            copy.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clipData = ClipData.newPlainText("text", page.getExtract());
                                                    clipboardManager.setPrimaryClip(clipData);
                                                    Constants.success(CreateNoteActivity.this, "Copied to clipboard");
                                                }
                                            });


                                            if (meaning.getText().toString().equals("")) {
                                                Constants.warning(CreateNoteActivity.this, "Information is not available");
                                                progressBar.setVisibility(View.GONE);
                                                search.setVisibility(View.VISIBLE);
                                                return;
                                            }
                                            progressBar.setVisibility(View.GONE);
                                            search.setVisibility(View.VISIBLE);
                                        }
                                    }
                                } else {
                                    Constants.error(CreateNoteActivity.this, "Failed to get the response");
                                    progressBar.setVisibility(View.GONE);
                                    search.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(Call<WikipediaResponse> call, Throwable t) {
                                Constants.error(CreateNoteActivity.this, "Please check the network connection");
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


        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.tags, es.dmoral.toasty.R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        binding.tags.setAdapter(arrayAdapter);


        binding.mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
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

            if (file != null) {
                filename = truncateString(getFileName(file), 10);
                filepath = file.getPath();

            }
            datalist.add(new NoteModel(filename, binding.title.getText().toString(), binding.note.getText().toString(), UniqueKey, UserId, filepath, file));
            adapter.notifyDataSetChanged();
        }

        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageData = baos.toByteArray();

            if (imageData != null) {
                imageName = truncateString(UUID.randomUUID().toString(), 10);
            }
            imagelist.add(new ImageStoreModel(imageData, imageName, UniqueKey, UserId));
            imageAdapter.notifyDataSetChanged();
        }

        if (requestCode == 112 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            if (imageUri != null) {
                imageName = truncateString(getFileName(imageUri), 10);
            }

            imagelist.add(new ImageStoreModel(imageUri, imageName, UniqueKey, UserId));
            imageAdapter.notifyDataSetChanged();
        }

        if (requestCode == REQUEST_CODE_VOICE_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result != null && !result.isEmpty() ? result.get(0) : "";
            binding.note.setText(spokenText);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language to the default locale
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Constants.error(this, "Language not supported");
            }
        } else {
           Constants.error(this, "Text-to-Speech initialization failed");
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something");

        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE_INPUT);
        } catch (ActivityNotFoundException e) {
           Constants.error(this, "Speech recognition not supported on this device");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown Text-to-Speech engine
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}















