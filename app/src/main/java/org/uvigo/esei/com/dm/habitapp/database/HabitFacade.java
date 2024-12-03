package org.uvigo.esei.com.dm.habitapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;

public class HabitFacade {

    private DBManager dbManager;

    public HabitFacade(HabitApplication habitApplication){

        this.dbManager = habitApplication.getDbManager();
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

    public Cursor getHabitsByCategory(String category) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,  // Nombre de la tabla
                null,
                DBManager.COLUMN_HABITO_CATEGORIA + " LIKE ?",  // Condición de filtro
                new String[]{"%" + category + "%"},
                null,
                null,
                null
        );
    }

    public Cursor getHabitsByName(String name) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                DBManager.COLUMN_HABITO_NOMBRE + " LIKE ?",  // El LIKE lo uso para búsquedas parciales
                new String[]{"%" + name + "%"},  //Esto lo saqué de stackOverflow
                null,
                null,
                null
        );
    }

    // Métodos para gestionar hábitos
    public Cursor getAllHabits() {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(DBManager.TABLE_HABITOS, null, null, null, null, null, null);
    }

    public long insertHabit(String nombre, String descripcion, String frecuencia, String categoria) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_NOMBRE, nombre);
        values.put(DBManager.COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(DBManager.COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(DBManager.COLUMN_HABITO_CATEGORIA, categoria);
        return db.insert(DBManager.TABLE_HABITOS, null, values);
    }

    public int updateHabit(int id, String nombre, String descripcion, String frecuencia, String categoria, int estado) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_NOMBRE, nombre);
        values.put(DBManager.COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(DBManager.COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(DBManager.COLUMN_HABITO_CATEGORIA, categoria);
        values.put(DBManager.COLUMN_HABITO_ESTADO, estado);
        return db.update(DBManager.TABLE_HABITOS, values, DBManager.COLUMN_HABITO_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteHabit(int id) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        return db.delete(DBManager.TABLE_HABITOS, DBManager.COLUMN_HABITO_ID + "=?", new String[]{String.valueOf(id)});
    }

}
