package com.guruprasad.tutionnotesaplication.Activities.ui.CreateNote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.guruprasad.tutionnotesaplication.API.ApiInterface;
import com.guruprasad.tutionnotesaplication.API.ApiUtilities;
import com.guruprasad.tutionnotesaplication.Adapters.ImageStoreAdapter;
import com.guruprasad.tutionnotesaplication.Adapters.NoteAdapter;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.Models.ApiModel.Page;
import com.guruprasad.tutionnotesaplication.Models.ApiModel.Query;
import com.guruprasad.tutionnotesaplication.Models.ApiModel.WikipediaResponse;
import com.guruprasad.tutionnotesaplication.Models.ImageStoreModel;
import com.guruprasad.tutionnotesaplication.Models.NoteDataModel;
import com.guruprasad.tutionnotesaplication.Models.NoteModel;
import com.guruprasad.tutionnotesaplication.R;
import com.guruprasad.tutionnotesaplication.Receiver.AlaramReceiver;
import com.guruprasad.tutionnotesaplication.databinding.ActivityCreateNoteBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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
    private Calendar calendar;

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

        notificationChannel();


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
                Dexter.withContext(CreateNoteActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

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

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
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

                        Dexter.withContext(CreateNoteActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                String title = binding.title.getText().toString();
                                String content = binding.note.getText().toString();

                                if (title.isEmpty() || content.isEmpty()) {
                                    Constants.error(CreateNoteActivity.this, "Please upload your note first");
                                } else {
                                    takePicture(dialog);
                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Constants.error(CreateNoteActivity.this, "Camera permission is necessary");
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
                    }
                });

                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dexter.withContext(CreateNoteActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
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

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Constants.error(CreateNoteActivity.this, "Permission is necessary");
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
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
                                    setAlarm();
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


        binding.remainder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });


    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select the time")
                .build();

        picker.show(getSupportFragmentManager(), "Admin");
        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {


                if (picker.getHour() > 12) {

                    int hour = picker.getHour() - 12;
                    int minute = picker.getMinute();
                    String time = String.format("%02d:%02d", hour, minute); // Format the time
                    binding.time.setText(time);
                } else {
                    binding.time.setText(picker.getHour() + " : " + picker.getMinute());
                }

                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
                calendar.set(Calendar.MINUTE, picker.getMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

            }
        });

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlaram();
            }
        });
    }

    private void cancelAlaram() {
        Intent intent = new Intent(this, AlaramReceiver.class); // Correcting the misspelled class name
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE); // Change FLAG_IMMUTABLE to FLAG_UPDATE_CURRENT if you need to update the PendingIntent

        if (alarmManager == null) {

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        }

        alarmManager.cancel(pendingIntent);
        Constants.error(CreateNoteActivity.this, "Remainder Cancel");

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

    private void notificationChannel() {
        CharSequence name = "Admin";
        String description = "Channel for notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("Admin", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlaramReceiver.class); // Correcting the misspelled class name
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE); // Change FLAG_IMMUTABLE to FLAG_UPDATE_CURRENT if you need to update the PendingIntent

        // Schedule the alarm using the provided time in milliseconds from the calendar
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


}















