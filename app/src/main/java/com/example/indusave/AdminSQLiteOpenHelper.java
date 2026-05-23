package com.example.indusave;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "indusave.db";
    private static final int DATABASE_VERSION = 1;

    public AdminSQLiteOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla de inventario global de materiales
        db.execSQL("create table materiales(" +
                "codigo text primary key, " +
                "descripcion text, " +
                "sobrante integer, " +
                "eficiencia text)");

        // Tabla de control de pedidos y mermas en tránsito
        db.execSQL("create table pedidos(" +
                "id_pedido integer primary key autoincrement, " +
                "codigo_mat text, " +
                "descripcion text, " +
                "destino text, " +
                "foto text, " +          // Foto de salida de bodega
                "foto_recibido text, " + // Foto de llegada tomada por operario
                "estado text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists materiales");
        db.execSQL("drop table if exists pedidos");
        onCreate(db);
    }
}