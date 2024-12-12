package org.uvigo.esei.com.dm.habitapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.NotificationHelper;
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;

public class HabitFacade {
    private NotificationHelper notificationHelper;
    private DBManager dbManager;
    private final Context context; // Almacenar el contexto para usar en el increment_progress

    public HabitFacade(HabitApplication habitApplication, Context context) {
        this.dbManager = habitApplication.getDbManager();
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
    }

    // Métodos para gestionar usuarios
    public Cursor getAllUsers() {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        return db.query(DBManager.TABLE_USUARIOS, null, null, null, null, null, null);
    }

    public String getUsername(int userId){
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query(
                DBManager.TABLE_USUARIOS,
                new String[]{DBManager.COLUMN_USERNAME},
                DBManager.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        if(cursor != null && cursor.moveToFirst()){
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_USERNAME));
            cursor.close();
            return username;
        }

        if(cursor != null) { cursor.close(); }


        return null;


    }

    public String getEmail(int userId){
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query(
                DBManager.TABLE_USUARIOS,
                new String[]{DBManager.COLUMN_EMAIL},
                DBManager.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_EMAIL));
            cursor.close();
            return email;
        }

        if (cursor != null) cursor.close();
        return null;

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

    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        String hashedPassword = PasswordSecurity.hashPassword(newPassword);

        ContentValues values = new ContentValues();//Esto no se muy bien lo que hace
        values.put(DBManager.COLUMN_PASSWORD, hashedPassword);

        int rowsUpdated = db.update(
                DBManager.TABLE_USUARIOS,
                values,
                DBManager.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsUpdated > 0;
    }

    public boolean verifyPassword(int userId, String inputPassword) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query(
                DBManager.TABLE_USUARIOS,
                new String[]{DBManager.COLUMN_PASSWORD},
                DBManager.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String storedHashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_PASSWORD));
            cursor.close();

            return PasswordSecurity.checkPassword(inputPassword, storedHashedPassword);
        }

        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    public Cursor getHabitsByCompleted(int userId){
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                DBManager.COLUMN_HABITO_ESTADO + " = ? AND user_id = ?",
                new String[]{"1", String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    public Cursor getHabitsByIncompleted( int userId){
        SQLiteDatabase db = dbManager.getReadableDatabase();

        return db.query(
                DBManager.TABLE_HABITOS,
                null,
                DBManager.COLUMN_HABITO_ESTADO + " = ? AND user_id = ?",
                new String[]{"0", String.valueOf(userId)},
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

    public boolean deleteUser(int userId){
        SQLiteDatabase db = dbManager.getWritableDatabase();

        db.beginTransaction();
        try{
            db.delete(
                    DBManager.TABLE_HABITOS,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            int rowDeleted = db.delete(
                    DBManager.TABLE_USUARIOS,
                    DBManager.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            Log.d("DeleteUser", "Filas afectadas: " + rowDeleted);

            if(rowDeleted > 0){
                db.setTransactionSuccessful();
                return true;
            }else{
                return false;
            }


        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            db.endTransaction();
        }

    }

    // Método para incrementar el progreso en HabitFacade
    public void incrementProgress(int habitId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1); // Recupera el user_id de la sesión

        Cursor cursor = db.rawQuery(
                "SELECT " + DBManager.COLUMN_HABITO_PROGRESO + ", " + DBManager.COLUMN_HABITO_FRECUENCIA +  ", " +
                        DBManager.COLUMN_HABITO_NOMBRE +
                        " FROM " + DBManager.TABLE_HABITOS +
                        " WHERE " + DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                new String[]{String.valueOf(habitId), String.valueOf(userId)}
        );

        if (cursor.moveToFirst()) {

            int currentProgress = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_PROGRESO));
            int frequency = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_FRECUENCIA));
            String habitName = ""; // Obtiene el nombre del hábito
            try {
                habitName = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_NOMBRE));
            } catch (IllegalArgumentException e) {
                Log.e("IncrementProgress", "La columna COLUMN_HABITO_NOMBRE no existe: " + e.getMessage());
            }



            if (currentProgress < frequency) {
                db.execSQL(
                        "UPDATE " + DBManager.TABLE_HABITOS +
                                " SET " + DBManager.COLUMN_HABITO_PROGRESO + " = " + DBManager.COLUMN_HABITO_PROGRESO + " + 1 " +
                                " WHERE " + DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                        new String[]{String.valueOf(habitId), String.valueOf(userId)}
                );

                if(currentProgress+1 == frequency){
                    db.execSQL("UPDATE " + DBManager.TABLE_HABITOS +
                                    " SET " + DBManager.COLUMN_HABITO_ESTADO + " = 1 " +
                                    " WHERE " + DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                            new String[]{String.valueOf(habitId), String.valueOf(userId)});
                    notificationHelper.createNotification("Enhorabuena ;)", "Ya has completado el hábito: " + habitName +".");
                }

            } else {

                Toast.makeText(context, "Ya has cumplido tu objetivo en este hábito, Enhorabuena!", Toast.LENGTH_SHORT).show();
            }
        }

        cursor.close();

    }

    public void resetAllHabitsProgress(int userId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_PROGRESO, 0);

        db.update(
                DBManager.TABLE_HABITOS,
                values,
                 "user_id = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    public void incrementAllHabitsProgress(int userId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        Cursor cursor = db.query(
                DBManager.TABLE_HABITOS,
                new String[]{DBManager.COLUMN_HABITO_ID, DBManager.COLUMN_HABITO_FRECUENCIA, DBManager.COLUMN_HABITO_PROGRESO},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                int habitId = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_ID));
                int frequency = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_FRECUENCIA));
                int progress = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_PROGRESO));

                if (progress < frequency) {
                    db.execSQL("UPDATE " + DBManager.TABLE_HABITOS +
                                    " SET " + DBManager.COLUMN_HABITO_PROGRESO + " = " + DBManager.COLUMN_HABITO_PROGRESO + " + 1 " +
                                    " WHERE " + DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                            new String[]{String.valueOf(habitId), String.valueOf(userId)});

                    if (progress + 1 == frequency) {
                        db.execSQL("UPDATE " + DBManager.TABLE_HABITOS +
                                        " SET " + DBManager.COLUMN_HABITO_ESTADO + " = 1 " +
                                        " WHERE " + DBManager.COLUMN_HABITO_ID + " = ? AND user_id = ?",
                                new String[]{String.valueOf(habitId), String.valueOf(userId)});
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    public boolean checkHabitsPending(int userId) {

        SQLiteDatabase db = dbManager.getReadableDatabase();

        // Consulta para obtener un hábito pendiente
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DBManager.TABLE_HABITOS +
                        " WHERE user_id = ? AND " + DBManager.COLUMN_HABITO_PROGRESO + " < " + DBManager.COLUMN_HABITO_FRECUENCIA,
                new String[]{String.valueOf(userId)}
        );

        boolean hasPending = cursor != null && cursor.moveToFirst();

        // Cerrar el cursor
        if (cursor != null) {
            cursor.close();
        }

        return hasPending;
    }


}
