package com.example.indusave;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class CatalogoActivity extends AppCompatActivity {
    private ListView lv;
    private EditText buscador;
    private Button btnIrARegistro;
    private ArrayList<String> listaInfo;
    private ArrayList<String> listaCodigos;
    private ArrayList<String> listaDescripciones;
    private ArrayAdapter<String> adapter;
    private String rolUsuario;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imgPreviewTemporal;
    private String fotoBase64String = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        rolUsuario = getIntent().getStringExtra("ROL");

        lv = findViewById(R.id.list_materiales);
        buscador = findViewById(R.id.et_buscador);
        btnIrARegistro = findViewById(R.id.btn_ir_a_registro);

        listaInfo = new ArrayList<>();
        listaCodigos = new ArrayList<>();
        listaDescripciones = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaInfo);
        lv.setAdapter(adapter);

        cargarDatos("");

        btnIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("ROL", rolUsuario);
            startActivity(intent);
        });

        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarDatos(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaCodigos.size()) {
                mostrarDetallePedido(listaCodigos.get(position), listaDescripciones.get(position));
            }
        });
    }

    private void cargarDatos(String filtro) {
        listaInfo.clear();
        listaCodigos.clear();
        listaDescripciones.clear();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getReadableDatabase();
             Cursor fila = db.rawQuery(
                     "select codigo, descripcion, sobrante, eficiencia from materiales " +
                             "where sobrante > 0 and descripcion like ? " +
                             "and codigo not in (select codigo_mat from pedidos)", new String[]{"%" + filtro + "%"})) {

            if (fila != null && fila.moveToFirst()) {
                do {
                    listaCodigos.add(fila.getString(0));
                    listaDescripciones.add(fila.getString(1));
                    listaInfo.add("ID: " + fila.getString(0) + " | " + fila.getString(1) +
                            "\nSobrante Disponible: " + fila.getString(2) + " unidades" +
                            "\nEficiencia de Origen: " + fila.getString(3));
                } while (fila.moveToNext());
            } else {
                listaInfo.add("No se encontraron mermas libres en este momento.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void mostrarDetallePedido(String codigo, String descripcion) {
        fotoBase64String = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Solicitud de Envío / Reuso");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputDestino = new EditText(this);
        inputDestino.setHint("Ej: Planta Soyapango, Área de Maquinado B");
        layout.addView(inputDestino);

        Button btnTomarFoto = new Button(this);
        btnTomarFoto.setText("📸 TOMAR FOTO DEL ESTADO ACTUAL");

        LinearLayout.LayoutParams paramsBoton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsBoton.setMargins(0, 15, 0, 15);
        btnTomarFoto.setLayoutParams(paramsBoton);
        layout.addView(btnTomarFoto);

        imgPreviewTemporal = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
        layoutParams.gravity = android.view.Gravity.CENTER;
        imgPreviewTemporal.setLayoutParams(layoutParams);
        layout.addView(imgPreviewTemporal);

        builder.setView(layout);
        builder.setMessage("Escriba el destino y capture la evidencia visual:\n\nMaterial: " + descripcion);

        btnTomarFoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        builder.setPositiveButton("CONFIRMAR RESERVA", (dialog, which) -> {
            String destino = inputDestino.getText().toString().trim();
            if (destino.isEmpty()) {
                Toast.makeText(this, "Debe especificar el destino del traslado", Toast.LENGTH_SHORT).show();
                return;
            }

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
            try (SQLiteDatabase db = admin.getWritableDatabase()) {
                ContentValues pedido = new ContentValues();
                pedido.put("codigo_mat", codigo);
                pedido.put("descripcion", descripcion);
                pedido.put("destino", destino);
                pedido.put("foto", fotoBase64String);
                pedido.put("estado", "RESERVADO ⏳");

                long resultado = db.insert("pedidos", null, pedido);
                if (resultado != -1) {
                    Toast.makeText(this, "Pedido registrado con éxito. Retirado del catálogo.", Toast.LENGTH_LONG).show();
                    cargarDatos(buscador.getText().toString().trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("CANCELAR", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // ✅ CORRECCIÓN EXCELENTE: Ahora hereda correctamente del método de actividades
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                imgPreviewTemporal.setImageBitmap(imageBitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                fotoBase64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }
        }
    }
}