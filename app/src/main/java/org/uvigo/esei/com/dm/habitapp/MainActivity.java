package org.uvigo.esei.com.dm.habitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class MainActivity extends AppCompatActivity {

    private HabitFacade habitFacade;

    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if(isLogged()){
            Intent intent = new Intent(MainActivity.this, HabitsListActivity.class);
            startActivity(intent);
            finish();
        }else{
            setContentView(R.layout.activity_main);
        }


        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        habitFacade = new HabitFacade((HabitApplication) getApplication());

    }

    public boolean isLogged(){
        SharedPreferences sharedPreferences = getSharedPreferences("Session",MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLogged",false);
    }

    private DBManager getDBManager() {
        return ((HabitApplication) getApplication()).getDbManager();
    }

}