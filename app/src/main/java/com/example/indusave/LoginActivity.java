package com.example.indusave;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText user, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = findViewById(R.id.et_user);
        pass = findViewById(R.id.et_pass);

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            if(user.getText().toString().equals("admin") && pass.getText().toString().equals("12345")){
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Credenciales no autorizadas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}