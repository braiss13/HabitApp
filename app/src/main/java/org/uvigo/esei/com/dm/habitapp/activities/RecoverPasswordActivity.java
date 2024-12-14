package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;



public class RecoverPasswordActivity extends AppCompatActivity {
    private HabitFacade habitFacade;

    private int sentToken; // Token generado y enviado
    private EditText etEmail, etToken, etNewPassword;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        // Referencias al layout
        etEmail = findViewById(R.id.etEmail);
        etToken = findViewById(R.id.etToken);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Obtén el token enviado desde SendTokenActivity
        Intent intent = getIntent();
        sentToken = intent.getIntExtra("token", -1);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleConfirmation();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Llamar al método que aplica el idioma según las preferencias
        LocaleUtils.setLocaleFromPreferences(this);
    }

    private void handleConfirmation() {
        // Obtén valores ingresados
        String email = etEmail.getText().toString().trim();
        String enteredToken = etToken.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Validación inicial de los campos
        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (enteredToken.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el token de recuperación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Verifica si el token ingresado coincide con el token enviado
            int token = Integer.parseInt(enteredToken);

            if (token == sentToken) {
                // Si el token es válido, solicita la nueva contraseña
                if (etNewPassword.getVisibility() == View.GONE) {
                    etNewPassword.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Token verificado, ahora ingresa tu nueva contraseña", Toast.LENGTH_SHORT).show();
                } else if (newPassword.isEmpty()) {
                    Toast.makeText(this, "Por favor, ingresa una nueva contraseña", Toast.LENGTH_SHORT).show();
                } else if (newPassword.length() < 6) {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                } else {
                    // La contraseña es válida, guarda los cambios (simulado aquí)
                    saveNewPassword(email, newPassword);
                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();

                    // Redirige al usuario a la pantalla de inicio de sesión
                    Intent intent = new Intent(RecoverPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Token incorrecto, inténtelo nuevamente", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El token debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }

    // TODO ESTE METODO ESTA INTERACTUANDO CON LA BD A PALO SECO
    private void saveNewPassword(String email, String newPassword) {
        DBManager dbManager = ((HabitApplication) getApplication()).getDbManager();
        SQLiteDatabase db = dbManager.getWritableDatabase();

        // Hashea la nueva contraseña
        String hashedPassword = PasswordSecurity.hashPassword(newPassword);

        // Crea los valores para la actualización
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_PASSWORD, hashedPassword);

        // Actualiza la contraseña para el correo dado
        int rowsUpdated = db.update(DBManager.TABLE_USUARIOS, values, DBManager.COLUMN_EMAIL + " = ?", new String[]{email});

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al actualizar la contraseña. Verifica el correo.", Toast.LENGTH_SHORT).show();
        }
    }

}

