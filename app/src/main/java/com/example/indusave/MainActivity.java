package com.example.indusave;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText et_cod, et_des, et_tipo, et_med_org, et_sob;
    TextView txt_eficiencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias de la UI
        et_cod = findViewById(R.id.et_codigo);
        et_des = findViewById(R.id.et_descripcion);
        et_tipo = findViewById(R.id.et_tipo);
        et_med_org = findViewById(R.id.et_medida_original);
        et_sob = findViewById(R.id.et_sobrante);
        txt_eficiencia = findViewById(R.id.txt_eficiencia_display);

        // Botón Registrar (Entrada y Merma)
        findViewById(R.id.btn_registrar).setOnClickListener(v -> guardar());

        // Botón Buscar individual
        findViewById(R.id.btn_buscar).setOnClickListener(v -> buscar());

        // Botón Eliminar
        findViewById(R.id.btn_eliminar).setOnClickListener(v -> eliminar());

        // Botón para ver el Catálogo de Segunda Vida
        findViewById(R.id.btn_ver_catalogo).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogoActivity.class));
        });
    }

    public void guardar() {
        String cod = et_cod.getText().toString();
        String des = et_des.getText().toString();
        String med = et_med_org.getText().toString();
        String sob = et_sob.getText().toString();

        if(cod.isEmpty() || med.isEmpty() || sob.isEmpty()){
            Toast.makeText(this, "Complete los campos para calcular merma", Toast.LENGTH_SHORT).show();
            return;
        }

        // LÓGICA DE INNOVACIÓN: Cálculo de Aprovechamiento
        double mOrg = Double.parseDouble(med);
        double mSob = Double.parseDouble(sob);
        double aprovechamiento = 100 - ((mSob / mOrg) * 100);
        String resEficiencia = String.format(Locale.US, "%.1f%%", aprovechamiento);

        // PANEL DE ALERTAS: Visualización dinámica (Punto 4 de rúbrica)
        txt_eficiencia.setText("Aprovechamiento: " + resEficiencia);
        if(aprovechamiento < 70) {
            txt_eficiencia.setTextColor(Color.RED); // ALERTA: Mucha merma
            Toast.makeText(this, "¡Alerta de desperdicio alto!", Toast.LENGTH_SHORT).show();
        } else {
            txt_eficiencia.setTextColor(Color.parseColor("#198754")); // Verde: Eficiente
        }

        // 1. Guardar en SQLite (Local)
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues reg = new ContentValues();
        reg.put("codigo", cod);
        reg.put("descripcion", des);
        reg.put("medida_original", med);
        reg.put("sobrante", sob);
        reg.put("eficiencia", resEficiencia);

        db.replace("materiales", null, reg);
        db.close();

        // 2. Sincronizar con la Nube (Punto de integración)
        sincronizarNube(cod, des, med, sob, resEficiencia);
        limpiarCampos();
    }

    public void buscar() {
        String cod = et_cod.getText().toString();
        if(cod.isEmpty()){
            Toast.makeText(this, "Ingrese código para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor fila = db.rawQuery("select descripcion, medida_original, sobrante, eficiencia from materiales where codigo='" + cod + "'", null);

        if(fila.moveToFirst()){
            et_des.setText(fila.getString(0));
            et_med_org.setText(fila.getString(1));
            et_sob.setText(fila.getString(2));
            txt_eficiencia.setText("Aprovechamiento: " + fila.getString(3));
            Toast.makeText(this, "Material encontrado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No existe el material en bodega", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void eliminar() {
        String cod = et_cod.getText().toString();
        if(cod.isEmpty()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getWritableDatabase();
        int cant = db.delete("materiales", "codigo='" + cod + "'", null);
        db.close();

        if (cant == 1) {
            Toast.makeText(this, "Material retirado de inventario", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        } else {
            Toast.makeText(this, "No se encontró el código", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos() {
        et_cod.setText("");
        et_des.setText("");
        et_med_org.setText("");
        et_sob.setText("");
        et_tipo.setText("");
    }

    private void sincronizarNube(String cod, String des, String med, String sob, String ef) {
        try {
            JSONObject json = new JSONObject();
            json.put("descripcion", des);
            json.put("medida_total", med);
            json.put("sobrante_merma", sob);
            json.put("aprovechamiento", ef);

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, Config.URL_COUCHDB + cod, json,
                    res -> Toast.makeText(this, "Sincronizado con InduSave Cloud", Toast.LENGTH_SHORT).show(),
                    err -> Toast.makeText(this, "Guardado Local (Offline)", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    String auth = "Basic " + Base64.encodeToString((Config.USUARIO + ":" + Config.PASS).getBytes(), Base64.NO_WRAP);
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            queue.add(req);
        } catch (Exception e) { e.printStackTrace(); }
    }
}