package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Context;
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
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
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

        // Referenciamos los elementos del Layout para trabajar con ellos
        btnNotifications = findViewById(R.id.btnNotifications);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
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
    @Override
    protected void onResume() {
        super.onResume();
        // Llamar al método que aplica el idioma según las preferencias
        LocaleUtils.setLocaleFromPreferences(this);
    }


    private void deleteUser() { //Método que borra el usuario y sale de la app
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
        config.setLocale(locale);
        // Actualizar los recursos de la aplicación con el nuevo idioma
        Context context = createConfigurationContext(config);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        // Guardar preferencia de idioma
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("language", langCode);
        editor.apply();

        // Reiniciar la actividad para aplicar cambios
        Intent intent = new Intent(this, SettingsActivity.class); // Reiniciar la actividad actual
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Esta línea es importante
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {
        // Aplicar el idioma actualizado antes de regresar
        super.onBackPressed();
        LocaleUtils.setLocaleFromPreferences(this);

        // Usar un intent con las banderas necesarias para asegurar que el idioma se aplique
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Asegurarse de que ProfileActivity se inicie correctamente

        // Iniciar la actividad
        startActivity(intent);

        // Llamar al nuevo comportamiento de "Atrás" con el dispatcher
        getOnBackPressedDispatcher().onBackPressed();
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
    public void setLocaleFromPreferences() {
        // Leer el idioma guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String languageCode = prefs.getString("language", "es");  // "es" es el valor por defecto si no hay preferencia

        // Cambiar la configuración del idioma
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        // Crear una nueva configuración con el idioma deseado
        Configuration config = new Configuration();
        config.setLocale(locale);  // Establecer el nuevo idioma

        // Crear un contexto con la nueva configuración
        Context context = createConfigurationContext(config);

        // Actualizar los recursos de la aplicación usando este nuevo contexto
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

}
