package com.example.indusave;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class InventarioGlobalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_global);

        ListView lv = findViewById(R.id.lv_inventario_global);
        ArrayList<String> lista = new ArrayList<>();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        try (SQLiteDatabase db = admin.getReadableDatabase();
             Cursor fila = db.rawQuery("select codigo, descripcion, sobrante, eficiencia from materiales", null)) {

            if (fila != null && fila.moveToFirst()) {
                do {
                    lista.add("COD: " + fila.getString(0) + "\n" + fila.getString(1) +
                            "\nSobrante: " + fila.getString(2) + " unidades | Eficiencia: " + fila.getString(3));
                } while (fila.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista));
    }
}