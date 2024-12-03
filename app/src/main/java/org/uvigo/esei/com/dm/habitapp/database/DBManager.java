package org.uvigo.esei.com.dm.habitapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "HabitAppDB";
    private static final int DB_VERSION = 1;

    // Tabla de usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";

    // Tabla de hábitos
    public static final String TABLE_HABITOS = "habitos";
    public static final String COLUMN_HABITO_ID = "_id";
    public static final String COLUMN_HABITO_NOMBRE = "nombre";
    public static final String COLUMN_HABITO_DESCRIPCION = "descripcion";
    public static final String COLUMN_HABITO_FRECUENCIA = "frecuencia";
    public static final String COLUMN_HABITO_CATEGORIA = "categoria";
    public static final String COLUMN_HABITO_ESTADO = "estado";

    public DBManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Crear la base de datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DBManager", "Creando base de datos: " + DB_NAME + " versión " + DB_VERSION);

        try {
            db.beginTransaction();

            String CREATE_TABLE_USUARIOS = "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE)";
            db.execSQL(CREATE_TABLE_USUARIOS);

            String CREATE_TABLE_HABITOS = "CREATE TABLE " + TABLE_HABITOS + " (" +
                    COLUMN_HABITO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HABITO_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_HABITO_DESCRIPCION + " TEXT, " +
                    COLUMN_HABITO_FRECUENCIA + " TEXT, " +
                    COLUMN_HABITO_CATEGORIA + " TEXT, " +
                    COLUMN_HABITO_ESTADO + " INTEGER DEFAULT 0)";

            db.execSQL(CREATE_TABLE_HABITOS);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("DBManager", "Error creando tablas: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    // Actualizar la base de datos si la versión cambia
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DBManager", "Actualizando base de datos de versión " + oldVersion + " a " + newVersion);
        try {
            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITOS);
            onCreate(db);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("DBManager.onUpgrade", "Error actualizando la base de datos: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

}
