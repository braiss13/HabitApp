package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnResetPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Permitir operaciones de red en el hilo principal (solo para pruebas, no recomendado en producción)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Referencia a los elementos del layout
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnResetPass = findViewById(R.id.btnResetPass);

        // Manejo del botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                int userId = authenticateUser(username, password);
                if (userId != -1) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                    saveSession(username, userId); // GUARDAMOS LA SESIÓN

                    Intent intent = new Intent(LoginActivity.this, HabitsListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_invalid_credentials), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnResetPass.setOnClickListener(view->{
                Intent intent = new Intent(LoginActivity.this, SendTokenActivity.class);
                startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Llamar al método que aplica el idioma según las preferencias
        LocaleUtils.setLocaleFromPreferences(this);
    }

    /**
     * Método para autenticar al usuario consultando la base de datos.
     *
     * @param username Nombre de usuario ingresado.
     * @param password Contraseña ingresada.
     * @return El user_id si las credenciales son correctas, -1 en otro caso.
     */
    private int authenticateUser(String username, String password) {
        DBManager dbManager = ((HabitApplication) getApplication()).getDbManager();
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query(
                DBManager.TABLE_USUARIOS,
                new String[]{DBManager.COLUMN_ID, DBManager.COLUMN_PASSWORD}, // Obtener ID y hash de contraseña
                DBManager.COLUMN_USERNAME + "=?",                             // Filtro: username
                new String[]{username},                                       // Parámetro: valor del username
                null, null, null
        );

        if (cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(DBManager.COLUMN_ID);
            int passwordColumnIndex = cursor.getColumnIndex(DBManager.COLUMN_PASSWORD);

            if (idColumnIndex == -1 || passwordColumnIndex == -1) {
                throw new IllegalArgumentException("Required column not found in the database.");
            }

            int userId = cursor.getInt(idColumnIndex);
            String storedHashedPassword = cursor.getString(passwordColumnIndex);

            // Comparar la contraseña ingresada con el hash almacenado
            if (PasswordSecurity.checkPassword(password, storedHashedPassword)) {
                cursor.close();
                return userId; // Retorna el ID del usuario autenticado
            }
        }

        cursor.close();
        return -1; // Si la autenticación falla
    }

    /**
     * Guardar la sesión del usuario autenticado en SharedPreferences.
     *
     * @param username Nombre de usuario autenticado.
     * @param userId ID del usuario autenticado.
     */
    public void saveSession(String username, int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("loggeduser", username); // Guardar el nombre de usuario
        editor.putInt("user_id", userId);         // Guardar el ID del usuario
        editor.putBoolean("isLogged", true);      // Marcar como logueado
        editor.apply();
    }
}
