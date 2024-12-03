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
import org.uvigo.esei.com.dm.habitapp.PasswordSecurity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;

    private EditText edtUsername,edtPassword,edtEmail;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
                    Toast.makeText(RegisterActivity.this, getString(R.string.register_empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

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
