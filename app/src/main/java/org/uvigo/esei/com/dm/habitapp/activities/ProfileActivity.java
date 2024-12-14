package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.activities.SettingsActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView ivProfileImage;
    private Button btnChangePassword, btnEditPhoto, btnHabitsList, btnSettings;
    private HabitFacade habitFacade;
    private HabitsListActivity habitsListActivity;
    private SharedPreferences sharedPreferences;
    private int userId;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        ivProfileImage = findViewById(R.id.ivProfilePicture);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnHabitsList = findViewById(R.id.btnHabitsList);
        btnSettings = findViewById(R.id.btnSettings);
        btnEditPhoto =findViewById(R.id.btnEditPhoto);

        sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);



        loadProfile();

        btnEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, PasswordChangeActivity.class);
                startActivity(intent);
            }
        });
        btnHabitsList.setOnClickListener(view->{
            Intent intent = new Intent(ProfileActivity.this, HabitsListActivity.class);
            startActivity(intent);
            finish();

        });

        btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Llama al método de la clase utilitaria para aplicar el idioma
        LocaleUtils.setLocaleFromPreferences(this);

    }

    public void onBackPressed() {
        // Aplicar el idioma actualizado antes de regresar
        super.onBackPressed();
        LocaleUtils.setLocaleFromPreferences(this);

        // Usar un intent con las banderas necesarias para asegurar que el idioma se aplique
        Intent intent = new Intent(this, HabitsListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Asegurarse de que ProfileActivity se inicie correctamente

        // Iniciar la actividad
        startActivity(intent);

        // Llamar al nuevo comportamiento de "Atrás" con el dispatcher
        getOnBackPressedDispatcher().onBackPressed();
    }



    private void loadProfile(){

        tvUsername.setText(habitFacade.getUsername(userId));
        tvEmail.setText(habitFacade.getEmail(userId));
        String imageUri = sharedPreferences.getString("profile_image_uri", null);
        if (imageUri != null) {
            ivProfileImage.setImageURI(Uri.parse(imageUri));
        }

    }



    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);


            sharedPreferences.edit().putString("profile_image_uri", imageUri.toString()).apply();
        }
    }

}
