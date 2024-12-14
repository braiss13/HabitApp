package org.uvigo.esei.com.dm.habitapp.activities;

// SendTokenActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.MailSender;
import org.uvigo.esei.com.dm.habitapp.R;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class SendTokenActivity extends AppCompatActivity {
    private String emailFrom = "alicianoal1@gmail.com";
    private String passFrom = "mdrt ysck fnxg dkrl";
    private Session session;
    private MimeMessage message;
    int token = (int) (Math.random() * 900000) + 100000; // Genera un número aleatorio de 6 dígitos



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_token);

        EditText emailTo = findViewById(R.id.emailEditText);
        Button sendTokenButton = findViewById(R.id.sendTokenButton);

        sendTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTo.getText().toString();
                    MailSender mailSender = new MailSender();
                    String subject = "Token de recuperacion";
                    String body = "El token es: " +token;
                    mailSender.sendEmail(email, subject, body);
                    Intent intent = new Intent(SendTokenActivity.this, RecoverPasswordActivity.class);
                    intent.putExtra("token", token);
                    startActivity(intent);

            }
        });
    }


}

