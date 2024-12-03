package org.uvigo.esei.com.dm.habitapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class AddHabitActivity extends AppCompatActivity {
    private EditText edtName, edtDescription, edtFrequency, edtCategory;
    private Button btnSave;
    private HabitFacade habitFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        habitFacade = new HabitFacade((HabitApplication) getApplication());
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
            Toast.makeText(this,  getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        habitFacade.insertHabit(name, description, frequency, category);
        Toast.makeText(this, getString(R.string.habit_added_successfully), Toast.LENGTH_SHORT).show();
        finish();
    }
    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.cancel_creation))
                .setMessage(getString(R.string.exit_without_creation))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
