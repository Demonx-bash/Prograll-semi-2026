package com.example.indusave;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context) {
        super(context, "InduSave.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table materiales(" +
                "codigo text primary key, " +
                "descripcion text, " +
                "tipo text, " +
                "medida_original real, " +
                "sobrante real, " +
                "eficiencia text, " +
                "foto text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists materiales");
        onCreate(db);
    }
}