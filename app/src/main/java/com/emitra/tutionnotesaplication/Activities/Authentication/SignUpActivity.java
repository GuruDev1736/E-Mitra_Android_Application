package com.emitra.tutionnotesaplication.Activities.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.CustomDialog;
import com.emitra.tutionnotesaplication.Models.UserModel;
import com.emitra.tutionnotesaplication.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = binding.etFullname.getText().toString();
                String phone = binding.etPhoneno.getText().toString();
                String email = binding.etEmail.getText().toString();
                String password = binding.etPassword.getText().toString();

                statusCheck(name, phone, email, password);
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

    }

    private void statusCheck(String name, String phone, String email, String password) {

        if (TextUtils.isEmpty(name)) {
            binding.etFullname.setError("Name Required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etPhoneno.setError("Phone No Required");
            return;
        }
        if (phone.length() < 10) {
            Constants.error(SignUpActivity.this, "Phone No is Not Valid");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email Required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password Required");
            return;
        }

        signup(name, phone, email, password);

    }

    private void signup(String name, String phone, String email, String password) {

        CustomDialog dialog = new CustomDialog(SignUpActivity.this);
        // dialog.title("Creating new Account");
        dialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            UserModel model = new UserModel(name, email, password, phone);
                            database.getReference().child("Users").child(auth.getCurrentUser().getUid()).setValue(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            FirebaseUser user = auth.getCurrentUser();
                                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        Constants.success(SignUpActivity.this, "User Account Successfully Created Please Verify your email before login");

                                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                        finish();
                                                        dialog.dismiss();
                                                    } else {
                                                        Constants.error(SignUpActivity.this, "Failed to send the email verification link");
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Constants.error(SignUpActivity.this, "Failed to Create Account : " + e.getMessage());
                                            dialog.dismiss();
                                        }
                                    });
                        } else {
                            Constants.error(SignUpActivity.this, "Failed to create account : " + task.getException().getMessage());
                            dialog.dismiss();
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}