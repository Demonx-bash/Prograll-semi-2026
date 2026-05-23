package com.example.indusave;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MenuAdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        // 1. Ir a Despacho de Pedidos (BodegaActivity)
        findViewById(R.id.btn_admin_bodega).setOnClickListener(v -> {
            Intent intent = new Intent(this, BodegaActivity.class);
            intent.putExtra("ROL", "admin");
            startActivity(intent);
        });

        // 2. Ir a Ver todo el Inventario de la Planta (InventarioGlobalActivity)
        findViewById(R.id.btn_admin_inventario).setOnClickListener(v -> {
            startActivity(new Intent(this, InventarioGlobalActivity.class));
        });

        // 3. Ir a Registrar nuevo material (MainActivity)
        findViewById(R.id.btn_admin_registro).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("ROL", "admin");
            startActivity(intent);
        });
    }
}