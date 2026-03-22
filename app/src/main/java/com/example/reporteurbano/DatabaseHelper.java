package com.example.reporteurbano;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ReporteUrbanoDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_REPORTES = "reportes";
    private static final String COL_ID = "id";
    private static final String COL_TITULO = "titulo";
    private static final String COL_DESCRICAO = "descricao";
    private static final String COL_LOCAL = "local";
    private static final String COL_CAMINHO_FOTO = "caminho_foto";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_REPORTES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITULO + " TEXT, " +
                COL_DESCRICAO + " TEXT, " +
                COL_LOCAL + " TEXT, " +
                COL_CAMINHO_FOTO + " TEXT)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTES);
        onCreate(db);
    }

    public long inserirReporte(String titulo, String descricao, String local, String caminhoFoto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITULO, titulo);
        values.put(COL_DESCRICAO, descricao);
        values.put(COL_LOCAL, local);
        values.put(COL_CAMINHO_FOTO, caminhoFoto);

        long idGerado = db.insert(TABLE_REPORTES, null, values);
        db.close();
        return idGerado;
    }
}