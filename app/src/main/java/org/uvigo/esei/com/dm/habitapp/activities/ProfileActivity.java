package org.uvigo.esei.com.dm.habitapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView ivProfileImage;
    private Button btnChangePassword, btnDeleteUser, btnEditPhoto;
    private HabitFacade habitFacade;
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
        //btnEditPhoto =findViewById(R.id.btnEditPhoto);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
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
    }

    private void loadProfile(){

        tvUsername.setText(habitFacade.getUsername(userId));
        tvEmail.setText(habitFacade.getEmail(userId));


        //TODO -> Cargar Imagen
    }

    //public void editPhoto(){}
    //TODO-> Método para cambiar la foto de perfil

    public boolean deleteUser(){

         return habitFacade.deleteUser(userId);

        //TODO -> Esto no funciona, pero tampoco me da error, simplemente no pasa nada

    }


}
