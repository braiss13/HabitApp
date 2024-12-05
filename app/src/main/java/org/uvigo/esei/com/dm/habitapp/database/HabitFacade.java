package org.uvigo.esei.com.dm.habitapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.content.Context;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;

public class HabitFacade {

    private DBManager dbManager;
    private final Context context; // Almacenar el contexto para usar en el increment_progress

    public HabitFacade(HabitApplication habitApplication, Context context) {
        this.dbManager = habitApplication.getDbManager();
        this.context = context;
    }

    // Métodos para gestionar usuarios
    public Cursor getAllUsers() {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        return db.query(DBManager.TABLE_USUARIOS, null, null, null, null, null, null);
    }

    public long insertUser(String username, String password, String email) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_USERNAME, username);
        values.put(DBManager.COLUMN_PASSWORD, password);
        values.put(DBManager.COLUMN_EMAIL, email);
        return db.insert(DBManager.TABLE_USUARIOS, null, values);
    }

    public Cursor authenticateUser(String username, String password) {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBManager.TABLE_USUARIOS + " WHERE " + DBManager.COLUMN_USERNAME + "=? AND " + DBManager.COLUMN_PASSWORD + "=?",
                new String[]{username, password});
    }

    // Consultar hábitos por categoría, filtrados por usuario
    public Cursor getHabitsByCategory(String category, int userId) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                DBManager.COLUMN_HABITO_CATEGORIA + " LIKE ? AND user_id = ?",
                new String[]{"%" + category + "%", String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    // Consultar hábitos por nombre, filtrados por usuario
    public Cursor getHabitsByName(String name, int userId) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                DBManager.COLUMN_HABITO_NOMBRE + " LIKE ? AND user_id = ?",
                new String[]{"%" + name + "%", String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    // Consultar todos los hábitos de un usuario
    public Cursor getAllHabits(int userId) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    // Obtener un hábito específico por ID y usuario
    public Cursor getHabitById(int habitId, int userId) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                new String[]{String.valueOf(habitId), String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    // Insertar un nuevo hábito asociado a un usuario
    public long insertHabit(String nombre, String descripcion, String frecuencia, String categoria, int userId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_NOMBRE, nombre);
        values.put(DBManager.COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(DBManager.COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(DBManager.COLUMN_HABITO_CATEGORIA, categoria);
        values.put("user_id", userId); // Asociar el hábito al usuario
        return db.insert(DBManager.TABLE_HABITOS, null, values);
    }

    // Actualizar un hábito, asegurándose de que pertenece al usuario actual
    public int updateHabit(int habitId, String nombre, String descripcion, String frecuencia, String categoria, int estado, int userId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_NOMBRE, nombre);
        values.put(DBManager.COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(DBManager.COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(DBManager.COLUMN_HABITO_CATEGORIA, categoria);
        values.put(DBManager.COLUMN_HABITO_ESTADO, estado);

        return db.update(
                DBManager.TABLE_HABITOS,
                values,
                DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                new String[]{String.valueOf(habitId), String.valueOf(userId)}
        );
    }

    // Eliminar un hábito, asegurándose de que pertenece al usuario actual
    public int deleteHabit(int habitId, int userId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        return db.delete(
                DBManager.TABLE_HABITOS,
                DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                new String[]{String.valueOf(habitId), String.valueOf(userId)}
        );
    }

    // Método para incrementar el progreso en HabitFacade
    public void incrementProgress(int habitId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1); // Recupera el user_id de la sesión

        // Llama al método de DBManager con habitId y userId
        dbManager.incrementProgress(habitId, userId);
    }

}
