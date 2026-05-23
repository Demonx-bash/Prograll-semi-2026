package com.example.indusave;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BodegaActivity extends AppCompatActivity {
    private ListView lvPedidos;
    private ArrayList<String> listaInfo;
    private ArrayList<Integer> listaIds;
    private ArrayList<String> listaCodsMat;
    private ArrayList<String> listaEstados;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bodega);

        lvPedidos = findViewById(R.id.lv_pedidos_bodega);

        listaInfo = new ArrayList<>();
        listaIds = new ArrayList<>();
        listaCodsMat = new ArrayList<>();
        listaEstados = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaInfo);
        lvPedidos.setAdapter(adapter);

        cargarPedidos();

        lvPedidos.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaIds.size()) {
                obtenerDetallesYMostrar(listaIds.get(position), listaCodsMat.get(position));
            }
        });
    }

    private void cargarPedidos() {
        listaInfo.clear();
        listaIds.clear();
        listaCodsMat.clear();
        listaEstados.clear();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getReadableDatabase();
             Cursor fila = db.rawQuery("select id_pedido, descripcion, estado, codigo_mat from pedidos", null)) {

            if (fila != null && fila.moveToFirst()) {
                do {
                    int idPed = fila.getInt(0);
                    String desc = fila.getString(1);
                    String est = fila.getString(2);
                    String codM = fila.getString(3);

                    listaIds.add(idPed);
                    listaCodsMat.add(codM);
                    listaEstados.add(est);
                    listaInfo.add("Pedido #" + idPed + " | " + desc + "\nEstado actual: " + est);
                } while (fila.moveToNext());
            } else {
                listaInfo.add("No hay solicitudes de despacho pendientes.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void obtenerDetallesYMostrar(int idPedido, String codigoMat) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getReadableDatabase();
             Cursor cursor = db.rawQuery("select descripcion, destino, estado, foto, foto_recibido from pedidos where id_pedido=" + idPedido, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                String desc = cursor.getString(0);
                String dest = cursor.getString(1);
                String est = cursor.getString(2);
                String fotoReserva = cursor.getString(3);
                String fotoRecibido = cursor.getString(4);

                mostrarDetalleDespacho(idPedido, codigoMat, desc, dest, est, fotoReserva, fotoRecibido);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al recuperar evidencias", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDetalleDespacho(int idPedido, String codigoMat, String desc, String dest, String est, String fotoReserva, String fotoRecibido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Auditoría de Pedido #" + idPedido);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView txtDetalle = new TextView(this);
        txtDetalle.setText("Material: " + desc + "\nDestino: " + dest + "\nEstado: " + est + "\n");
        txtDetalle.setTextSize(16);
        layout.addView(txtDetalle);

        if (fotoReserva != null && !fotoReserva.isEmpty()) {
            TextView txtO = new TextView(this);
            txtO.setText("📸 Foto de Salida (Reserva en Catálogo):");
            txtO.setTextColor(Color.BLACK);
            layout.addView(txtO);

            ImageView imgOrigen = new ImageView(this);
            imgOrigen.setImageBitmap(convertirBase64ToBitmap(fotoReserva));
            LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(300, 300);
            paramsImg.gravity = Gravity.CENTER;
            paramsImg.setMargins(0, 10, 0, 20);
            imgOrigen.setLayoutParams(paramsImg);
            layout.addView(imgOrigen);
        }

        if (fotoRecibido != null && !fotoRecibido.isEmpty()) {
            TextView txtD = new TextView(this);
            txtD.setText("✅ Foto de Llegada (Constancia del Operario):");
            txtD.setTextColor(Color.parseColor("#0F5132"));
            layout.addView(txtD);

            ImageView imgDestino = new ImageView(this);
            imgDestino.setImageBitmap(convertirBase64ToBitmap(fotoRecibido));
            LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(300, 300);
            paramsImg.gravity = Gravity.CENTER;
            paramsImg.setMargins(0, 10, 0, 20);
            imgDestino.setLayoutParams(paramsImg);
            layout.addView(imgDestino);
        }

        builder.setView(layout);

        if (est.toUpperCase().contains("ENTREGADO")) {
            builder.setPositiveButton("🗑️ ARCHIVAR ORDEN FINALIZADA", (dialog, which) -> {
                eliminarPedidoCompletamente(idPedido, codigoMat, true);
            });
        } else {
            builder.setPositiveButton("DESPACHAR / EN TRÁNSITO 📦", (dialog, which) -> {
                actualizarEstado(idPedido, "EN TRÁNSITO 📦");
            });
        }

        builder.setNeutralButton("💥 ELIMINAR PEDIDO", (dialog, which) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Panel de Eliminación Rápida")
                    .setMessage("¿Qué acción deseas tomar con este pedido?")
                    .setPositiveButton("REINTEGRAR AL CATÁLOGO", (d, w) -> {
                        eliminarPedidoCompletamente(idPedido, codigoMat, false);
                    })
                    .setNegativeButton("ELIMINAR TODO DE RAÍZ", (d, w) -> {
                        eliminarPedidoCompletamente(idPedido, codigoMat, true);
                    })
                    .setNeutralButton("CANCELAR", null)
                    .show();
        });

        builder.setNegativeButton("CERRAR PANEL", null);
        builder.show();
    }

    private void actualizarEstado(int idPedido, String nuevoEstado) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getWritableDatabase()) {
            ContentValues valores = new ContentValues();
            valores.put("estado", nuevoEstado);
            db.update("pedidos", valores, "id_pedido=" + idPedido, null);
            Toast.makeText(this, "Pedido actualizado a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cargarPedidos();
    }

    private void eliminarPedidoCompletamente(int idPed, String codMat, boolean borrarMaterialGlobal) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getWritableDatabase()) {

            if (borrarMaterialGlobal) {
                db.delete("materiales", "codigo='" + codMat + "'", null);
                // ✅ LLAMADA DE SINCRONIZACIÓN: Si se borra de raíz localmente, también saca el JSON de CouchDB
                eliminarDeCouchDB(codMat);
                Toast.makeText(this, "Pedido y material eliminados del sistema", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Orden eliminada. Material reincorporado al catálogo.", Toast.LENGTH_SHORT).show();
            }

            db.delete("pedidos", "id_pedido=" + idPed, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        cargarPedidos();
    }

    // ✅ NUEVO MÉTODO: Lógica REST para eliminar el documento directamente de CouchDB
    private void eliminarDeCouchDB(String codMat) {
        String urlDoc = Config.URL_COUCHDB + codMat;

        // Paso 1: Primero hacemos un GET rápido al documento para averiguar su revisión actual ("_rev")
        JsonObjectRequest getReq = new JsonObjectRequest(Request.Method.GET, urlDoc, null,
                response -> {
                    try {
                        String rev = response.getString("_rev");
                        // Paso 2: Con la revisión obtenida, estructuramos la URL de borrado con el parámetro ?rev=
                        String urlDelete = urlDoc + "?rev=" + rev;

                        JsonObjectRequest deleteReq = new JsonObjectRequest(Request.Method.DELETE, urlDelete, null,
                                res -> android.util.Log.d("COUCH_DELETE", "Documento eliminado con éxito de la nube"),
                                err -> android.util.Log.e("COUCH_DELETE", "Error al ejecutar el DELETE en CouchDB")
                        ) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                String credenciales = Config.USUARIO + ":" + Config.PASS;
                                String auth = "Basic " + Base64.encodeToString(credenciales.getBytes(), Base64.NO_WRAP);
                                headers.put("Authorization", auth);
                                return headers;
                            }
                        };
                        Volley.newRequestQueue(getApplicationContext()).add(deleteReq);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> android.util.Log.e("COUCH_DELETE", "No se pudo obtener la revisión del documento (Tal vez no existe en la nube)")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String credenciales = Config.USUARIO + ":" + Config.PASS;
                String auth = "Basic " + Base64.encodeToString(credenciales.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(getReq);
    }

    private Bitmap convertirBase64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}