package com.guruprasad.tutionnotesaplication.Activities.Authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.guruprasad.tutionnotesaplication.Activities.NavigationActivity;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    public static final String EMAIL = "email";
    public static final String SHAREDPREF = "sharedpref";
    public static final String PASSWORD = "password";
    ActivityLoginBinding binding;
    FirebaseAuth auth;
    private String mail;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        loadData();
        binding.etEmail.setText(mail);
        binding.etPassword.setText(pass);

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = binding.etEmail.getText().toString();
                String password = binding.etPassword.getText().toString();

                statusCheck(email, password);
            }
        });

        binding.tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder dialogBuilder = Constants.dialog(LoginActivity.this, "Reset Password", "Enter Your Registered Email To reset the Password");
                EditText email = new EditText(LoginActivity.this);
                dialogBuilder.setView(email);
                dialogBuilder.setPositiveButton("YES", (dialogInterface, i) -> {
                            String mail = email.getText().toString();
                            if (mail.isEmpty()) {
                                Constants.error(LoginActivity.this, "Please enter the registered email");
                            } else {
                                resetpassword(mail);
                            }
                        })
                        .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });
    }

    private void resetpassword(String mail) {

        CustomDialog dialog = new CustomDialog(LoginActivity.this);
        dialog.show();

        if (mail != null) {
            auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Constants.success(LoginActivity.this, "Reset link has send successfully on your registered email address");
                        dialog.dismiss();
                    } else {
                        Constants.error(LoginActivity.this, "Failed to send the reset link" + task.getException().getMessage());
                    }
                    dialog.dismiss();
                }
            });
        }


    }


    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
        mail = sharedPreferences.getString(EMAIL, "");
        pass = sharedPreferences.getString(PASSWORD, "");
    }

    private void statusCheck(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email Required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password Required");
            return;
        }

        savedata(email, password);

        CustomDialog dialog = new CustomDialog(LoginActivity.this);
        dialog.setText("Logging User");
        dialog.show();

        login(email, password, dialog);
    }

    private void savedata(String email, String password) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL, email);
        editor.putString(PASSWORD, password);
        editor.apply();

    }

    private void login(String email, String password, CustomDialog dialog) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        Constants.success(LoginActivity.this, "Login Successful");
                        startActivity(new Intent(LoginActivity.this, NavigationActivity.class));
                        finish();
                        dialog.dismiss();
                    } else {
                        Constants.error(LoginActivity.this, "Please verify the email and login again");
                        dialog.dismiss();
                    }
                } else {
                    Constants.error(LoginActivity.this, "Failed to login user : " + task.getException().getMessage());
                    dialog.dismiss();
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}