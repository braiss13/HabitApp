package org.uvigo.esei.com.dm.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.models.Habito;

import java.util.List;

public class ReminderWorker extends Worker {

    private Context context;  // Contexto de la aplicación
    private HabitFacade habitFacade;  // Instancia de HabitFacade

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

        // Inicializar HabitFacade con los parámetros requeridos
        HabitApplication app = (HabitApplication) context.getApplicationContext();
        this.habitFacade = new HabitFacade(app, context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d("ReminderWorker", "Inicio del Worker");

            int userId = getUserIdFromSession();

            if (userId == -1) {
                Log.e("ReminderWorker", "Error: UserID no encontrado");
                return Result.failure(); // Usuario no registrado
            }

            boolean habitsPending = habitFacade.checkHabitsPending(userId);

            if (habitsPending) {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.createNotification(
                        "¡Tienes hábitos pendientes!",
                        "Recuerda completar tus hábitos antes de que termine la semana."
                );
                Log.d("ReminderWorker", "Notificación creada con éxito");
            } else {
                Log.d("ReminderWorker", "No hay hábitos pendientes. Worker terminado.");
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("ReminderWorker", "Error en el Worker", e);
            return Result.failure();
        }
    }

    private int getUserIdFromSession() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1); // Devuelve -1 si no se encuentra el ID del usuario
    }

}


