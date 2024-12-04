package org.uvigo.esei.com.dm.habitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;
import org.uvigo.esei.com.dm.habitapp.activities.LoginActivity;
import org.uvigo.esei.com.dm.habitapp.activities.RegisterActivity;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class MainActivity extends AppCompatActivity {

    private HabitFacade habitFacade;

    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario está autenticado
        if (isLogged()) {
            // Si el usuario está autenticado, redirigir directamente a HabitsListActivity
            Intent intent = new Intent(MainActivity.this, HabitsListActivity.class);
            startActivity(intent);
            finish();
            return; // Detener el método onCreate para evitar que se ejecute el resto del código
        }

        // Si el usuario no está autenticado, configurar la vista principal
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Configurar los listeners para los botones
        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Inicializar el HabitFacade
        habitFacade = new HabitFacade((HabitApplication) getApplication());
    }

    /**
     * Verificar si el usuario ya ha iniciado sesión.
     *
     * @return True si está autenticado, False en caso contrario.
     */
    public boolean isLogged() {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLogged", false);
    }
}



















/*
package org.uvigo.esei.com.dm.habitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;
import org.uvigo.esei.com.dm.habitapp.activities.LoginActivity;
import org.uvigo.esei.com.dm.habitapp.activities.RegisterActivity;
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

        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
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

}*/