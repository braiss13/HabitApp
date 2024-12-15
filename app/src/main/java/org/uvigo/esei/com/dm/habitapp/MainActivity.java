package org.uvigo.esei.com.dm.habitapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import java.util.Calendar;

import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;
import org.uvigo.esei.com.dm.habitapp.activities.LoginActivity;
import org.uvigo.esei.com.dm.habitapp.activities.RegisterActivity;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private NotificationHelper notificationHelper;
    private HabitFacade habitFacade;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario está autenticado
        if (isLogged()) {
            // Si el usuario está autenticado, redirigir directamente a HabitsListActivity
            Intent intent = new Intent(MainActivity.this, HabitsListActivity.class);
            startActivity(intent);
            finish();
            return; // Detener el método onCreate para evitar que se ejecute el resto del código
        }

        // Si el usuario no está autenticado, configurar la vista principal
        setContentView(R.layout.activity_main);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
            }
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannel();


        // Inicializar vistas
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Configurar los listeners para los botones
        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Inicializar el HabitFacade
        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        // Llamada al método que crea una notificación cada x en tiempo (en este caso cada 5 días)
        scheduleHabitReminder();

        //createNotificationChannel();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // Código de solicitud de POST_NOTIFICATIONS
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                Log.d("Permission", "POST_NOTIFICATIONS permission granted");
            } else {
                // Permiso denegado
                Log.e("Permission", "POST_NOTIFICATIONS permission denied");
            }
        }
    }


    /**
     * Verificar si el usuario ya ha iniciado sesión.
     *
     * @return True si está autenticado, False en caso contrario.
     */
    public boolean isLogged() { //Método para comprobar si el usuario ya está logueado
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLogged", false);
    }

    // Crear una solicitud periódica (actualmente de 5 días) para el Worker
    private void scheduleHabitReminder() {
        PeriodicWorkRequest reminderWorkRequest = new PeriodicWorkRequest.Builder(ReminderWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(5, TimeUnit.DAYS) // Ajustar la preferencia de días
                .build();

        Log.d("ReminderWorker", "Worker ejecutado correctamente TRAS LOS 5 DÍAS.");

        WorkManager.getInstance(this).enqueue(reminderWorkRequest);
    }

}
