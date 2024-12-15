package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;


public class RegisterActivity extends AppCompatActivity {
    private HabitFacade habitFacade;

    private Button btnRegister;

    private EditText edtUsername,edtPassword,edtEmail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!LocaleUtils.isValidPassword(password)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.register_password_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!LocaleUtils.isValidEmail(email)){
                    Toast.makeText(RegisterActivity.this, "Email no valido", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si el email ya está registrado
                if (habitFacade.isEmailRegistered(email)) {
                    Toast.makeText(RegisterActivity.this, "Este email ya existe, inicia sesión o regístrate con otro", Toast.LENGTH_SHORT).show();
                    return;
                }

                // En caso de que pase todas las validaciones, se registra el usuario
                if (registerUser(username, password, email)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                    Intent intent =new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.register_error), Toast.LENGTH_SHORT).show();
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

    //TODO ESTA CLASE TMB INTERACTUA A PALO SOBRE LA BD
    private boolean registerUser(String username, String password, String email) {
        DBManager dbManager = ((HabitApplication) getApplication()).getDbManager();
        SQLiteDatabase db = dbManager.getWritableDatabase();

        String hashedPassword = PasswordSecurity.hashPassword(password);

        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_USERNAME, username);
        values.put(DBManager.COLUMN_PASSWORD, hashedPassword);
        values.put(DBManager.COLUMN_EMAIL, email);

        long result = db.insert(DBManager.TABLE_USUARIOS, null, values);
        return result != -1;
    }
}
