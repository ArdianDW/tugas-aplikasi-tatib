package com.kelompokMPKK.project_tatib;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import android.widget.EditText;
import android.widget.Button;
import com.google.firebase.auth.FirebaseUser;

public class Changepw extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText currentPassword, newPassword, confirmPassword;
    private Button changePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepw);

        auth = FirebaseAuth.getInstance();

        currentPassword = findViewById(R.id.password);
        newPassword = findViewById(R.id.newpw);
        confirmPassword = findViewById(R.id.confirm);
        changePasswordButton = findViewById(R.id.enter);

        changePasswordButton.setOnClickListener(v -> {
            String current = currentPassword.getText().toString();
            String newPass = newPassword.getText().toString();
            String confirm = confirmPassword.getText().toString();

            if(newPass.equals(confirm)) {
                changePassword(current, newPass);
            } else {
                Toast.makeText(Changepw.this, "Password baru dan konfirmasi password tidak sama.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changePassword(String current, String newPassword) {

        FirebaseUser user = auth.getCurrentUser();

        if(user != null) {

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {

                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(Changepw.this, "Password berhasil diubah.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Changepw.this, "Gagal merubah password.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Changepw.this, "Password saat ini salah.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}