package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Referencia a los elementos del layout
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);


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

                if (authenticateUser(username, password)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                    saveSession(username);//GUARDAMOS LA SESION

                    Intent intent = new Intent(LoginActivity.this, HabitsListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_invalid_credentials), Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    /**
     * Método para autenticar al usuario consultando la base de datos.
     *
     * @param username Nombre de usuario ingresado.
     * @param password Contraseña ingresada.
     * @return Verdadero si las credenciales son correctas, falso en otro caso.
     */
    private boolean authenticateUser(String username, String password) {
        DBManager dbManager = ((HabitApplication) getApplication()).getDbManager();
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query(
                DBManager.TABLE_USUARIOS,
                new String[]{DBManager.COLUMN_PASSWORD}, // Obtener solo el hash
                DBManager.COLUMN_USERNAME + "=?",       // Filtro: username
                new String[]{username},                 // Parámetro: valor del username
                null, null, null
        );
        if(cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DBManager.COLUMN_PASSWORD);
            if (columnIndex == -1) {
                throw new IllegalArgumentException("Column not found: " + DBManager.COLUMN_PASSWORD);
            }
            String storedHashedPassword = cursor.getString(columnIndex);

            // Comparar la contraseña ingresada con el hash almacenado
            return PasswordSecurity.checkPassword(password, storedHashedPassword);
        }


        cursor.close();
        return false;

        }

        public void saveSession(String username){
            SharedPreferences sharedPreferences = getSharedPreferences("Session",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("loggeduser",username);
            editor.putBoolean("isLogged", true);
            editor.apply();

        }

    }



