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

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = edtOldPassword.getText().toString().trim();
                String newPassword = edtNewPassword.getText().toString().trim();

                if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(PasswordChangeActivity.this, "Por favor, complete ambos campos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Validar la nueva contraseña

                if (newPassword.length() < 8 || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {

                    Toast.makeText(PasswordChangeActivity.this, "La nueva contraseña debe tener al menos 8 caracteres, incluir una letra y un número.", Toast.LENGTH_SHORT).show();
                    return;

                }

                // Obtener el ID del usuario de la sesión actual
                SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
                int userId = sharedPreferences.getInt("user_id", -1);

                if (userId == -1) {
                    Toast.makeText(PasswordChangeActivity.this, "Error al obtener la sesión. Por favor, inicie sesión de nuevo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si la contraseña actual es correcta
                boolean isOldPasswordCorrect = habitFacade.verifyPassword(userId, oldPassword);

                if (!isOldPasswordCorrect) {
                    Toast.makeText(PasswordChangeActivity.this, "La contraseña actual es incorrecta.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cambiar la contraseña
                boolean isPasswordChanged = habitFacade.updatePassword(userId, newPassword);

                if (isPasswordChanged) {
                    Toast.makeText(PasswordChangeActivity.this, "Contraseña actualizada con éxito.", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad
                } else {
                    Toast.makeText(PasswordChangeActivity.this, "Error al actualizar la contraseña. Inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
}
