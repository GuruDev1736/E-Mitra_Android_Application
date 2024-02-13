package com.guruprasad.tutionnotesaplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.guruprasad.tutionnotesaplication.Activities.Authentication.LoginActivity;
import com.guruprasad.tutionnotesaplication.databinding.ActivityMainBinding;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void run() {
                PermissionX.init(MainActivity.this)
                        .permissions(Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.CALL_PHONE)
                        .request(new RequestCallback() {
                            @Override
                            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                                if (allGranted) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        startActivity(new Intent(MainActivity.this, NavigationActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                        finish();
                                    }

                                } else {
                                    Toast.makeText(MainActivity.this, "These permissions are denied: " + deniedList, Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        });
            }
        }, 1000);
    }
}

//
// Manifest.permission.CAMERA,
//         Manifest.permission.INTERNET,
//         Manifest.permission.READ_EXTERNAL_STORAGE,
//         Manifest.permission.WRITE_EXTERNAL_STORAGE,
//         Manifest.permission.POST_NOTIFICATIONS,
//         Manifest.permission.CALL_PHONE