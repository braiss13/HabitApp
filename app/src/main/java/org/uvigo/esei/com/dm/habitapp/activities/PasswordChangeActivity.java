package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.HabitApplication;

public class PasswordChangeActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword;
    private Button btnChangePassword;

    private long userId;
    private HabitFacade habitFacade;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        //Referenciamos a los elementos del Layout para trabajar con ellos
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {   //Manejo del Botón de cambiar contraseña
            @Override
            public void onClick(View view) {
                String oldPassword = edtOldPassword.getText().toString().trim();
                String newPassword = edtNewPassword.getText().toString().trim();

                if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(PasswordChangeActivity.this, getString(R.string.toast_fill_both), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Validar la nueva contraseña

            if (!LocaleUtils.isValidPassword(oldPassword)) {
                Toast.makeText(PasswordChangeActivity.this, getString(R.string.register_password_invalid), Toast.LENGTH_SHORT).show();
                return;
            }


            // Obtener el ID del usuario de la sesión actual
                SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
                int userId = sharedPreferences.getInt("user_id", -1);

                if (userId == -1) {
                    Toast.makeText(PasswordChangeActivity.this, getString(R.string.error_session), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si la contraseña actual es correcta
                boolean isOldPasswordCorrect = habitFacade.verifyPassword(userId, oldPassword);

                if (!isOldPasswordCorrect) {
                    Toast.makeText(PasswordChangeActivity.this, getString(R.string.login_invalid_credentials), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cambiar la contraseña
                boolean isPasswordChanged = habitFacade.updatePassword(userId, newPassword);

                if (isPasswordChanged) {
                    Toast.makeText(PasswordChangeActivity.this, getString(R.string.updated_pass), Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad
                } else {
                    Toast.makeText(PasswordChangeActivity.this, getString(R.string.error_updating_pass), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Llamar al método que aplica el idioma según las preferencias
        LocaleUtils.setLocaleFromPreferences(this);
    }
}
