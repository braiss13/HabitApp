package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class SettingsActivity extends AppCompatActivity {

    private Button btnNotifications,btnLanguage,btnDeleteUser;
    private HabitFacade habitFacade;
    private SharedPreferences sharedPreferences;
    private int userId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLanguage = findViewById(R.id.btnLanguage);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);

        sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        btnDeleteUser.setOnClickListener(view -> deleteUser());
    }

    private void deleteUser(){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle("Confirmar borrado de Usuario")
                .setMessage("¿Está seguro de que quiere eliminar su cuenta? Esta decisión es definitiva.")
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    habitFacade.deleteUser(userId);
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
