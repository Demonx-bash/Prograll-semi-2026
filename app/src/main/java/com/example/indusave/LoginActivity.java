package com.example.indusave;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUser, etPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.et_user);
        etPass = findViewById(R.id.et_pass);

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            String usuario = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese sus credenciales", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent;
            if (usuario.equals("admin") && password.equals("12345")) {
                Toast.makeText(this, "Modo Administrador de Bodega", Toast.LENGTH_SHORT).show();
                // MODIFICADO: Ahora el administrador entra a su Menú Principal de control global
                intent = new Intent(this, MenuAdminActivity.class);
                intent.putExtra("ROL", "admin");
                startActivity(intent);
                finish();
            } else if (usuario.equals("operario") && password.equals("12345")) {
                Toast.makeText(this, "Modo Operario de Planta", Toast.LENGTH_SHORT).show();
                // MODIFICADO: Ahora el operario entra a su Menú Principal de planta con sus herramientas aisladas
                intent = new Intent(this, MenuOperarioActivity.class);
                intent.putExtra("ROL", "operario");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error: Personal no registrado", Toast.LENGTH_LONG).show();
            }
        });
    }
}