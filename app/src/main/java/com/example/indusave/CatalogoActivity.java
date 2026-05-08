package com.example.indusave;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class CatalogoActivity extends AppCompatActivity {
    ListView lv;
    EditText buscador;
    ArrayList<String> listaOriginal;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        lv = findViewById(R.id.list_materiales);
        buscador = findViewById(R.id.et_buscador);

        cargarDatos("");

        // EVENTO 1: Buscador en tiempo real
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarDatos(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // EVENTO 2: Tocar un item para ver detalle y "Pedir"
        lv.setOnItemClickListener((parent, view, position, id) -> {
            String itemSeleccionado = listaOriginal.get(position);
            mostrarDetallePedido(itemSeleccionado);
        });
    }

    private void cargarDatos(String filtro) {
        listaOriginal = new ArrayList<>();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        // Busqueda por nombre o código
        Cursor fila = db.rawQuery("select codigo, descripcion, sobrante, eficiencia from materiales where descripcion like '%" + filtro + "%'", null);

        if (fila.moveToFirst()) {
            do {
                listaOriginal.add("ID: " + fila.getString(0) + " | " + fila.getString(1) +
                        "\nSobrante: " + fila.getString(2) + " unidades");
            } while (fila.moveToNext());
        }
        db.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaOriginal);
        lv.setAdapter(adapter);
    }

    private void mostrarDetallePedido(String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalle del Material");
        builder.setMessage(info + "\n\n¿Desea solicitar este material para su proceso?");

        builder.setPositiveButton("SOLICITAR / PEDIR", (dialog, which) -> {
            Toast.makeText(this, "Solicitud enviada a Bodega con éxito", Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton("CANCELAR", null);
        builder.show();
    }
}