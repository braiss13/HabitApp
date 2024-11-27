package org.uvigo.esei.com.dm.habitapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class AddHabitActivity extends AppCompatActivity {
    private EditText edtName, edtDescription, edtFrequency, edtCategory;
    private Button btnSave;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        dbManager = new DBManager(this);
        edtName = findViewById(R.id.edtHabitName);
        edtDescription = findViewById(R.id.edtHabitDescription);
        edtFrequency = findViewById(R.id.edtHabitFrequency);
        edtCategory = findViewById(R.id.edtHabitCategory);
        btnSave = findViewById(R.id.btnSaveHabit);

        btnSave.setOnClickListener(v -> saveHabit());
    }

    private void saveHabit() {
        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String frequency = edtFrequency.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();

        if (name.isEmpty() || frequency.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        dbManager.insertHabit(name, description, frequency, category);
        Toast.makeText(this, "Hábito agregado con éxito", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cancelar creación")
                .setMessage("¿Deseas salir sin crear el hábito?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton("No", null)
                .show();
    }
}
