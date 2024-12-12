package org.uvigo.esei.com.dm.habitapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

import java.util.Calendar;

public class ResetHabitsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Aquí se reinicia el progreso de los hábitos
        HabitFacade habitFacade = new HabitFacade((HabitApplication) context.getApplicationContext(), context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        habitFacade.resetAllHabitsProgress(userId);

        Log.d("ResetHabitsReceiver", "Progreso de hábitos reiniciado.");
    }
}

