package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        // Referencias al layout
        etEmail = findViewById(R.id.etEmail);
        etToken = findViewById(R.id.etToken);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Obtén el token enviado desde SendTokenActivity
        Intent intent = getIntent();
        sentToken = intent.getIntExtra("token", -1);

        btnConfirm.setOnClickListener(new View.OnClickListener() { //Manejo del Botón de confirmar
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
            Toast.makeText(this, getString(R.string.toast_empty_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (enteredToken.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_add_token), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.toast_email_not_valid), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Verifica si el token ingresado coincide con el token enviado
            int token = Integer.parseInt(enteredToken);

            if (token == sentToken) {
                // Si el token es válido, solicita la nueva contraseña
                if (etNewPassword.getVisibility() == View.GONE) {
                    etNewPassword.setVisibility(View.VISIBLE);
                    Toast.makeText(this, getString(R.string.token_checked), Toast.LENGTH_SHORT).show();
                } else if (newPassword.isEmpty()) {
                    Toast.makeText(this, getString(R.string.please_new_pass), Toast.LENGTH_SHORT).show();
                } else if (newPassword.length() < 6) {
                    Toast.makeText(this, getString(R.string.pass_6), Toast.LENGTH_SHORT).show();
                } else {
                    // La contraseña es válida, guarda los cambios (simulado aquí)
                    saveNewPassword(email, newPassword);
                    Toast.makeText(this, getString(R.string.pass_updated), Toast.LENGTH_SHORT).show();

                    // Redirige al usuario a la pantalla de inicio de sesión
                    Intent intent = new Intent(RecoverPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, getString(R.string.invalid_token), Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.token_number), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNewPassword(String email, String newPassword) {
        // Hashea la nueva contraseña
        String hashedPassword = PasswordSecurity.hashPassword(newPassword);

        // Usa HabitFacade para actualizar la contraseña
        boolean isUpdated = habitFacade.updatePasswordByEmail(email, hashedPassword);

        if (isUpdated) {
            Toast.makeText(this, getString(R.string.pass_updated), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.check_email), Toast.LENGTH_SHORT).show();
        }
    }

}

