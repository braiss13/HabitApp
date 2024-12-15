package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.MailSender;
import org.uvigo.esei.com.dm.habitapp.R;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class SendTokenActivity extends AppCompatActivity {
    private HabitFacade habitFacade;
    private String emailFrom = "alicianoal1@gmail.com";
    private String passFrom = "mdrt ysck fnxg dkrl";
    private Session session;
    private MimeMessage message;
    int token = (int) (Math.random() * 900000) + 100000; // Genera un número aleatorio de 6 dígitos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_token);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        EditText emailTo = findViewById(R.id.emailEditText);
        Button sendTokenButton = findViewById(R.id.sendTokenButton);

        sendTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTo.getText().toString();

                // Verificar si el email ya está registrado
                if (!habitFacade.isEmailRegistered(email)) {
                    Toast.makeText(SendTokenActivity.this, "Este email no existe, revísalo o prueba con otro", Toast.LENGTH_SHORT).show();
                    return;
                }

                MailSender mailSender = new MailSender();
                String subject = "Token de recuperacion";
                String body = "El token es: " + token;
                mailSender.sendEmail(email, subject, body);
                Intent intent = new Intent(SendTokenActivity.this, RecoverPasswordActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);

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

