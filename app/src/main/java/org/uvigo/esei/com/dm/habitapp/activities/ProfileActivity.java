package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;
import org.uvigo.esei.com.dm.habitapp.activities.HabitsListActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView ivProfileImage;
    private Button btnChangePassword, btnDeleteUser, btnEditPhoto, btnHabitsList;
    private HabitFacade habitFacade;
    private HabitsListActivity habitsListActivity;
    private SharedPreferences sharedPreferences;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        ivProfileImage = findViewById(R.id.ivProfilePicture);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
        btnHabitsList = findViewById(R.id.btnHabitsList);
        //btnEditPhoto =findViewById(R.id.btnEditPhoto);

        sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);



        loadProfile();

        //btnEditPhoto.setOnClickListener(view -> editPhoto());
        btnDeleteUser.setOnClickListener(view -> deleteUser());
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

    }
    private void deleteUser(){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle("Confirmar borrado de Usuario")
                .setMessage("¿Está seguro de que quiere eliminar su cuenta? Esta decisión es definitiva.")
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    habitFacade.deleteUser(userId);
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void loadProfile(){

        tvUsername.setText(habitFacade.getUsername(userId));
        tvEmail.setText(habitFacade.getEmail(userId));


        //TODO -> Cargar Imagen
    }

    //public void editPhoto(){}
    //TODO-> Método para cambiar la foto de perfil


}
