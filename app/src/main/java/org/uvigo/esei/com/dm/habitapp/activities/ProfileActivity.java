package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.activities.SettingsActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView ivProfileImage;
    private Button btnChangePassword, btnEditPhoto, btnHabitsList, btnSettings;
    private HabitFacade habitFacade;
    private HabitsListActivity habitsListActivity;
    private SharedPreferences sharedPreferences;
    private int userId;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PROFILE_IMAGE_FILE_NAME = "profile_image.png";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        //Referenciamos a los elementos del Layout para trabajar con ellos
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

        btnEditPhoto.setOnClickListener(new View.OnClickListener() { //Manejo del Botón de editar foto
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT); //intent para acceder a la galería y elgir la imagen
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {   //Manejo del botón de cambiar contraseña
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, PasswordChangeActivity.class);
                startActivity(intent);
            }
        });
        btnHabitsList.setOnClickListener(view->{ //Manejo del botón de Mis Hábitos
            Intent intent = new Intent(ProfileActivity.this, HabitsListActivity.class);
            startActivity(intent);
            finish();

        });

        btnSettings.setOnClickListener(view -> { //Manejo del Botón de ajustes
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

    // Método que se ejecuta al pulsar el botón de ir hacia atrás
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

    //Método para cargar los datos del perfil
    private void loadProfile(){

        tvUsername.setText(habitFacade.getUsername(userId));
        tvEmail.setText(habitFacade.getEmail(userId));

        File file = new File(getFilesDir(), PROFILE_IMAGE_FILE_NAME);   //Cargamos la imagen desde el almacenamiento de la App porque no tenemos permisos para recuperar la Uri
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ivProfileImage.setImageBitmap(bitmap);
        }

    }

    // Respuesta al intent de elegir foto
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);                                 //Esto se hace así porque solo con la Uri no nos deja

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Cargamos el InputStream de la imagen seleccionada
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Guardamos la imagen en el almacenamiento interno
                saveImageToInternalStorage(bitmap);


                ivProfileImage.setImageBitmap(bitmap);

                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.image_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Método para guardar la imagen escogida en el almacenamiento interno
    private void saveImageToInternalStorage(Bitmap bitmap) {
        File file = new File(getFilesDir(), PROFILE_IMAGE_FILE_NAME);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.image_error_intern), Toast.LENGTH_SHORT).show();
        }

    }

}
