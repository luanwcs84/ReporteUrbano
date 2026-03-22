package com.example.reporteurbano;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

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

    public List<Reporte> buscarTodosReportes() {
        List<Reporte> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REPORTES, null);

        // Se houver pelo menos um resultado, vai passando linha a linha
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRICAO));
                String local = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCAL));
                String caminhoFoto = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAMINHO_FOTO));

                lista.add(new Reporte(id, titulo, descricao, local, caminhoFoto));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    public void deletarReporte(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Apaga na tabela onde a coluna ID for igual
        db.delete(TABLE_REPORTES, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
