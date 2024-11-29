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

    // Métodos para gestionar usuarios
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USUARIOS, null, null, null, null, null, null);
    }

    public long insertUser(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_EMAIL, email);
        return db.insert(TABLE_USUARIOS, null, values);
    }

    public Cursor authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password});
    }

    // Métodos para gestionar hábitos
    public Cursor getAllHabits() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_HABITOS, null, null, null, null, null, null);
    }

    public long insertHabit(String nombre, String descripcion, String frecuencia, String categoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABITO_NOMBRE, nombre);
        values.put(COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(COLUMN_HABITO_CATEGORIA, categoria);
        return db.insert(TABLE_HABITOS, null, values);
    }

    public int updateHabit(int id, String nombre, String descripcion, String frecuencia, String categoria, int estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABITO_NOMBRE, nombre);
        values.put(COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(COLUMN_HABITO_CATEGORIA, categoria);
        values.put(COLUMN_HABITO_ESTADO, estado);
        return db.update(TABLE_HABITOS, values, COLUMN_HABITO_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteHabit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_HABITOS, COLUMN_HABITO_ID + "=?", new String[]{String.valueOf(id)});
    }

}
