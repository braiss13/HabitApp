package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Button btnNotifications, btnDeleteUser;
    private Spinner spinnerLanguage;
    private HabitFacade habitFacade;
    private SharedPreferences sharedPreferences;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicializar los botones
        btnNotifications = findViewById(R.id.btnNotifications);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);

        // Inicializar el Spinner
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        // Configurar las opciones del Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.language_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Obtener idioma actual
        String currentLanguage = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("language", "es");
        setSpinnerToLanguage(spinnerLanguage, currentLanguage);

        // Configurar el listener para cambiar idioma
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                String currentLanguage = getSharedPreferences("Settings", MODE_PRIVATE)
                        .getString("language", "es");

                if (!currentLanguage.equals(getLanguageCode(selectedLanguage))) {
                    setLocale(getLanguageCode(selectedLanguage));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se hace nada si no se selecciona ningún elemento
            }
        });

        // Configurar la acción para borrar usuario
        btnDeleteUser.setOnClickListener(view -> deleteUser());
    }

    private void deleteUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_confirmation))
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

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        // Actualizar configuración
        Configuration config = new Configuration();
        config.setLocale(locale);  // Usamos setLocale en lugar de config.locale (API más reciente)
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Guardar preferencia de idioma
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("language", langCode);
        editor.apply();

        // Reiniciar la actividad para aplicar cambios
        recreate();
    }


    private void setSpinnerToLanguage(Spinner spinner, String langCode) {
        String[] languageOptions = getResources().getStringArray(R.array.language_options);
        int position = 0; // Por defecto, "Español"

        switch (langCode) {
            case "en":
                position = 1; // Posición de "Inglés"
                break;
            case "gl":
                position = 2; // Posición de "Galego"
                break;
        }

        spinner.setSelection(position);
    }


    private String getLanguageCode(String language) {
        switch (language) {
            case "Inglés":
                return "en";
            case "Galego":
                return "gl";
            default:
                return "es";
        }
    }
}
