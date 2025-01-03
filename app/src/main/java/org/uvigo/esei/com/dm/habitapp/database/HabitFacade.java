package org.uvigo.esei.com.dm.habitapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.widget.Toast;
import android.util.Log;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.NotificationHelper;
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;

import java.util.Date;
import java.util.Locale;

public class HabitFacade {
    private NotificationHelper notificationHelper;
    private DBManager dbManager;
    private final Context context; // Almacenar el contexto para usar en el increment_progress

    public HabitFacade(HabitApplication habitApplication, Context context) {
        this.dbManager = habitApplication.getDbManager();
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
    }

    // Métodos para obtener todos los usuarios
    public Cursor getAllUsers() {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        return db.query(DBManager.TABLE_USUARIOS, null, null, null, null, null, null);
    }

    // Método para obtener el nombre de usuario
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

    // Método para obtener el email
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

    public boolean registerNewUser(String username, String hashedPassword, String email) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_USERNAME, username);
        values.put(DBManager.COLUMN_PASSWORD, hashedPassword);
        values.put(DBManager.COLUMN_EMAIL, email);

        long result = db.insert(DBManager.TABLE_USUARIOS, null, values);

        return result != -1; // Devuelve true si la inserción fue exitosa
    }

    // Método para comprobar que un usuario existe
    public Cursor authenticateUser(String username, String password) {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBManager.TABLE_USUARIOS + " WHERE " + DBManager.COLUMN_USERNAME + "=? AND " + DBManager.COLUMN_PASSWORD + "=?",
                new String[]{username, password});
    }

    // Obtener los hábitos filtrados por categoría
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

    // Obtener los hábitos filtrados por nombre
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

    // Obtener los hábitos completados
    public Cursor getCompletedHabits(int userId) {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        String query = "SELECT hc." + DBManager.COLUMN_HABITO_COMPLETADO_ID + ", " +
                "h." + DBManager.COLUMN_HABITO_NOMBRE + ", " +
                "h." + DBManager.COLUMN_HABITO_CATEGORIA + ", " +
                "hc." + DBManager.COLUMN_HABITO_COMPLETADO_FECHA_COMPLETADO +
                " FROM " + DBManager.TABLE_HABITOS_COMPLETADOS + " hc " +
                " JOIN " + DBManager.TABLE_HABITOS + " h " +
                " ON hc.habito_id = h." + DBManager.COLUMN_HABITO_ID +
                " WHERE h.user_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Método para actualizar la contraseña
    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        String hashedPassword = PasswordSecurity.hashPassword(newPassword);

        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_PASSWORD, hashedPassword);

        int rowsUpdated = db.update(
                DBManager.TABLE_USUARIOS,
                values,
                DBManager.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsUpdated > 0;
    }

    //Método para actualizar la contraseña
    public boolean updatePasswordByEmail(String email, String hashedPassword) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_PASSWORD, hashedPassword);

        int rowsUpdated = db.update(DBManager.TABLE_USUARIOS, values, DBManager.COLUMN_EMAIL + " = ?", new String[]{email});

        return rowsUpdated > 0; // Devuelve true si se actualizó al menos una fila
    }

    // Método para comprobar si es correcta la contraseña
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

    // Obtener habitos filtrados por estado completado
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

    // Obtener habitos filtrados por estado en progreso
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

    // Método para obtener todos los hábitos de un usuario
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
    public long insertHabit(String nombre, String descripcion, String frecuencia, String categoria,long time, int userId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_NOMBRE, nombre);
        values.put(DBManager.COLUMN_HABITO_DESCRIPCION, descripcion);
        values.put(DBManager.COLUMN_HABITO_FRECUENCIA, frecuencia);
        values.put(DBManager.COLUMN_HABITO_CATEGORIA, categoria);
        values.put(DBManager.COLUMN_HABITO_FECHA_CREACION, time);
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

    // Eliminar un usuario junto con sus hábitos
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
                    ContentValues values = new ContentValues();
                    values.put(DBManager.COLUMN_HABITO_COMPLETADO_FECHA_COMPLETADO, System.currentTimeMillis());
                    values.put("habito_id", habitId);
                    long result = db.insert(DBManager.TABLE_HABITOS_COMPLETADOS, null, values);
                    notificationHelper.createNotification("Enhorabuena ;)", "Ya has completado el hábito: " + habitName +".");
                }

            } else {

                Toast.makeText(context, "Ya has cumplido tu objetivo en este hábito, Enhorabuena!", Toast.LENGTH_SHORT).show();
            }
        }

        cursor.close();

    }

    // Método para resetear el progreso de los hábitos
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

    // Método para incrementar el progreso de todos los hábitos
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

    // Método para verificar si quedan hábitos por hacer (para luego enviar notificación)
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

    // Método para resetear el progreso a 0
    public void updateProgressToZero(int habitId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();  // Usamos dbManager para obtener la instancia de la base de datos.
        String updateQuery = "UPDATE " + DBManager.TABLE_HABITOS +  // Usamos el nombre de la tabla de hábitos desde DBManager
                " SET " + DBManager.COLUMN_HABITO_PROGRESO + " = 0 WHERE " + DBManager.COLUMN_HABITO_ID + " = ?";
        db.execSQL(updateQuery, new Object[]{habitId});
    }

    // Método que permite decrementar en 1 el progreso actual (en caso de equivocación al pulsar el +)
    public void decrementProgress(int habitId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();  // Usamos dbManager para obtener la instancia de la base de datos.
        String updateQuery = "UPDATE " + DBManager.TABLE_HABITOS +  // Usamos el nombre de la tabla de hábitos desde DBManager
                " SET " + DBManager.COLUMN_HABITO_PROGRESO + " = " + DBManager.COLUMN_HABITO_PROGRESO + " - 1 WHERE " + DBManager.COLUMN_HABITO_ID + " = ?";
        db.execSQL(updateQuery, new Object[]{habitId});
    }

    // Método para marcar un hábito como completado
    public boolean markHabitAsCompleted(int habitId) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        // Obtener la fecha actual
        long completionDate = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_HABITO_COMPLETADO_FECHA_COMPLETADO, completionDate);
        values.put("habito_id", habitId); // FK al hábito completado

        long result = db.insert(DBManager.TABLE_HABITOS_COMPLETADOS, null, values);

        return result != -1; // Retorna true si se insertó correctamente
    }


    // Método auxiliar para obtener la fecha actual en formato adecuado
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());  // Devuelve la fecha y hora actual
    }

    // Método para verificar si un email ya existe en la BD
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DBManager.TABLE_USUARIOS + " WHERE " + DBManager.COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0; // Si COUNT(*) > 0, el email existe
        }
        cursor.close();
        return exists;
    }


}
