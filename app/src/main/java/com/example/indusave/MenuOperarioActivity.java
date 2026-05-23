package com.example.indusave;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MenuOperarioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_operario);

        // 1. Ver catálogo para apartar material
        findViewById(R.id.btn_op_catalogo).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogoActivity.class).putExtra("ROL", "operario"));
        });

        // 2. Ver sus propios pedidos (Su bodega privada)
        findViewById(R.id.btn_op_mis_pedidos).setOnClickListener(v -> {
            startActivity(new Intent(this, MisPedidosActivity.class));
        });

        // 3. Registrar mermas de su área
        findViewById(R.id.btn_op_reportar).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class).putExtra("ROL", "operario"));
        });
    }
}