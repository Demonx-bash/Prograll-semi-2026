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
    private EditText etCod, etDes, etTipo, etMedOrg, etSob;
    private TextView txtEficiencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCod = findViewById(R.id.et_codigo);
        etDes = findViewById(R.id.et_descripcion);
        etTipo = findViewById(R.id.et_tipo);
        etMedOrg = findViewById(R.id.et_medida_original);
        etSob = findViewById(R.id.et_sobrante);
        txtEficiencia = findViewById(R.id.txt_eficiencia_display);

        findViewById(R.id.btn_registrar).setOnClickListener(v -> guardar());
        findViewById(R.id.btn_buscar).setOnClickListener(v -> buscar());
        findViewById(R.id.btn_eliminar).setOnClickListener(v -> eliminar());

        findViewById(R.id.btn_ver_catalogo).setOnClickListener(v ->
                startActivity(new Intent(this, CatalogoActivity.class)));

        findViewById(R.id.btn_ver_bodega).setOnClickListener(v ->
                startActivity(new Intent(this, BodegaActivity.class)));
    }

    public void guardar() {
        String cod = etCod.getText().toString().trim();
        String des = etDes.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String med = etMedOrg.getText().toString().trim();
        String sob = etSob.getText().toString().trim();

        if(cod.isEmpty() || med.isEmpty() || sob.isEmpty()) {
            Toast.makeText(this, "Campos técnicos obligatorios vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        double mOrg = Double.parseDouble(med);
        double mSob = Double.parseDouble(sob);
        double aprovechamiento = 100 - ((mSob / mOrg) * 100);
        String resEficiencia = String.format(Locale.US, "%.1f%%", aprovechamiento);

        txtEficiencia.setText("Aprovechamiento: " + resEficiencia);

        // Algoritmo del Panel de Alertas Visuales
        if(aprovechamiento < 70) {
            txtEficiencia.setBackgroundColor(Color.parseColor("#F8D7DA"));
            txtEficiencia.setTextColor(Color.parseColor("#721C24"));
            Toast.makeText(this, "ALERTA: Alto índice de desperdicio", Toast.LENGTH_SHORT).show();
        } else {
            txtEficiencia.setBackgroundColor(Color.parseColor("#D1E7DD"));
            txtEficiencia.setTextColor(Color.parseColor("#0F5132"));
        }

        // PERSISTENCIA LOCAL (SQLite)
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getWritableDatabase()) {
            ContentValues reg = new ContentValues();
            reg.put("codigo", cod);
            reg.put("descripcion", des + " (" + tipo + ")");
            reg.put("sobrante", (int) mSob);
            reg.put("eficiencia", resEficiencia);

            db.replace("materiales", null, reg);
            Toast.makeText(this, "Material registrado localmente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar en SQLite", Toast.LENGTH_SHORT).show();
        }

        // ✅ EJECUCIÓN CONECTADA A LA NUBE
        sincronizarNube(cod, des, tipo, med, sob, resEficiencia);
        limpiarCampos();
    }

    public void buscar() {
        String cod = etCod.getText().toString().trim();
        if(cod.isEmpty()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        Cursor fila = db.rawQuery("select descripcion, sobrante, eficiencia from materiales where codigo='" + cod + "'", null);

        if(fila.moveToFirst()){
            etDes.setText(fila.getString(0));
            etTipo.setText("General");
            etMedOrg.setText("0");
            etSob.setText(fila.getString(1));
            txtEficiencia.setText("Aprovechamiento registrado: " + fila.getString(2));
            txtEficiencia.setBackgroundColor(Color.parseColor("#E9ECEF"));
            txtEficiencia.setTextColor(Color.BLACK);
        } else {
            Toast.makeText(this, "El código no existe en el inventario", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void eliminar() {
        String cod = etCod.getText().toString().trim();
        if(cod.isEmpty()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getWritableDatabase();
        int cant = db.delete("materiales", "codigo='" + cod + "'", null);
        db.close();

        if (cant == 1) {
            Toast.makeText(this, "Material eliminado correctamente", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        } else {
            Toast.makeText(this, "Código no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos() {
        etCod.setText(""); etDes.setText(""); etTipo.setText(""); etMedOrg.setText(""); etSob.setText("");
    }

    // ✅ MÉTODO CORREGIDO Y COMPATIBLE CON COUCHDB 2.X / 3.X
    private void sincronizarNube(String cod, String des, String tipo, String med, String sob, String ef) {
        try {
            JSONObject json = new JSONObject();
            json.put("descripcion", des);
            json.put("tipo_material", tipo);
            json.put("medida_total", med);
            json.put("sobrante_merma", sob);
            json.put("indice_aprovechamiento", ef);

            // Se concatena la URL base con el ID único del documento (el código del material)
            String urlFinal = Config.URL_COUCHDB + cod;

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, urlFinal, json,
                    res -> Toast.makeText(MainActivity.this, "☁️ ¡Sincronizado con Éxito (Cloud CouchDB)!", Toast.LENGTH_LONG).show(),
                    err -> Toast.makeText(MainActivity.this, "⚠️ Modo Offline: Guardado solo en SQLite", Toast.LENGTH_LONG).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    // Autenticación básica sin saltos de línea destructivos
                    String credenciales = Config.USUARIO + ":" + Config.PASS;
                    String auth = "Basic " + Base64.encodeToString(credenciales.getBytes(), Base64.NO_WRAP);

                    headers.put("Authorization", auth);
                    headers.put("Content-Type", "application/json"); // 👈 ESTO EVITA QUE REBOTE LA PETICIÓN
                    return headers;
                }
            };

            // Se usa el ApplicationContext global para que la petición no se cancele al limpiar la pantalla
            RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());
            queue.add(req);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}