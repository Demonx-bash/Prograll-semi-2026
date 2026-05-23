package com.example.indusave;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MisPedidosActivity extends AppCompatActivity {
    private ListView lvMisPedidos;
    private ArrayList<String> info;
    private ArrayList<Integer> ids;
    private ArrayList<String> codsMat;
    private ArrayAdapter<String> adapter;

    private static final int REQUEST_IMAGE_CAPTURE_RECIBIDO = 2;
    private ImageView imgPreviewRecepcion;
    private String fotoRecepcionBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pedidos);

        lvMisPedidos = findViewById(R.id.lv_mis_pedidos);

        info = new ArrayList<>();
        ids = new ArrayList<>();
        codsMat = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, info);
        lvMisPedidos.setAdapter(adapter);

        cargar();

        lvMisPedidos.setOnItemClickListener((p, v, pos, id) -> {
            if (pos >= 0 && pos < ids.size()) {
                mostrarDialogoConfirmacion(ids.get(pos), codsMat.get(pos));
            }
        });
    }

    private void cargar() {
        info.clear();
        ids.clear();
        codsMat.clear();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getReadableDatabase();
             Cursor f = db.rawQuery("select id_pedido, descripcion, estado, codigo_mat from pedidos", null)) {
            if (f != null && f.moveToFirst()) {
                do {
                    ids.add(f.getInt(0));
                    codsMat.add(f.getString(3));
                    info.add("Orden: " + f.getInt(0) + " | " + f.getString(1) + "\nEstado: " + f.getString(2));
                } while (f.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogoConfirmacion(int idPed, String codMat) {
        fotoRecepcionBase64 = "";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Recepción");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // 1. Campo para capturar nuevo residuo/sobrante sobrante si aplica
        TextView lblSobrante = new TextView(this);
        lblSobrante.setText("¿Sobró material tras su uso? Escriba la cantidad (Dejar 0 si se consumió todo):");
        layout.addView(lblSobrante);

        final EditText inputNuevoSobrante = new EditText(this);
        inputNuevoSobrante.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputNuevoSobrante.setText("0");
        layout.addView(inputNuevoSobrante);

        // 2. Botón de cámara interno
        Button btnTomarFotoRecibido = new Button(this);
        btnTomarFotoRecibido.setText("📸 TOMAR FOTO DE CONSTANCIA");
        LinearLayout.LayoutParams paramsBoton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsBoton.setMargins(0, 15, 0, 15);
        btnTomarFotoRecibido.setLayoutParams(paramsBoton);
        layout.addView(btnTomarFotoRecibido);

        // 3. Miniatura
        imgPreviewRecepcion = new ImageView(this);
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(250, 250);
        paramsImg.gravity = Gravity.CENTER;
        imgPreviewRecepcion.setLayoutParams(paramsImg);
        layout.addView(imgPreviewRecepcion);

        builder.setView(layout);

        btnTomarFotoRecibido.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_RECIBIDO);
            }
        });

        builder.setPositiveButton("SÍ, RECIBIDO", (dialog, which) -> {
            if (fotoRecepcionBase64.isEmpty()) {
                Toast.makeText(this, "⚠️ ERROR: Debe tomar la foto de evidencia antes de confirmar", Toast.LENGTH_LONG).show();
                return;
            }

            String cantStr = inputNuevoSobrante.getText().toString().trim();
            int cantidadRestante = cantStr.isEmpty() ? 0 : Integer.parseInt(cantStr);

            procesarRecepcion(idPed, codMat, cantidadRestante);
        });

        builder.setNegativeButton("AÚN NO", null);
        builder.show();
    }

    // Procesa el estado del pedido y recalcula de forma inteligente las mermas remanentes
    private void procesarRecepcion(int idPed, String codMat, int nuevaCantidad) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getWritableDatabase()) {

            // 1. Actualizamos el pedido a estado ENTREGADO y le inyectamos la foto de constancia
            ContentValues valoresPedido = new ContentValues();
            valoresPedido.put("estado", "ENTREGADO 🏁");
            valoresPedido.put("foto_recibido", fotoRecepcionBase64);
            db.update("pedidos", valoresPedido, "id_pedido=" + idPed, null);

            // 2. Lógica Inteligente de Inventario:
            if (nuevaCantidad > 0) {
                ContentValues valoresMat = new ContentValues();
                valoresMat.put("sobrante", nuevaCantidad);
                db.update("materiales", valoresMat, "codigo='" + codMat + "'", null);
                Toast.makeText(this, "Recepción registrada. Se regresaron " + nuevaCantidad + " unidades al stock global.", Toast.LENGTH_LONG).show();
            } else {
                // Si es 0, el administrador limpiará este material de la tabla global cuando archive el pedido
                ContentValues valoresMat = new ContentValues();
                valoresMat.put("sobrante", 0);
                db.update("materiales", valoresMat, "codigo='" + codMat + "'", null);
                Toast.makeText(this, "Material consumido por completo. Pendiente de archivo.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        cargar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE_RECIBIDO && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                imgPreviewRecepcion.setImageBitmap(imageBitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                fotoRecepcionBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }
        }
    }
}