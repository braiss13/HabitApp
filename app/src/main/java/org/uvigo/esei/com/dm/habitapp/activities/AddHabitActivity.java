package org.uvigo.esei.com.dm.habitapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class AddHabitActivity extends AppCompatActivity {
    private EditText edtName, edtDescription, edtFrequency;
    private Spinner spHabitCategory;
    private String selectedCategory;
    private Button btnSave;
    private HabitFacade habitFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);
        edtName = findViewById(R.id.edtHabitName);
        edtDescription = findViewById(R.id.edtHabitDescription);
        edtFrequency = findViewById(R.id.edtHabitFrequency);
        spHabitCategory = findViewById(R.id.spHabitCategory);

        spHabitCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedCategory = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedCategory = null;
            }
        });

        btnSave = findViewById(R.id.btnSaveHabit);

        btnSave.setOnClickListener(v -> saveHabit());
    }

    public void saveHabit() {
        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String frequency = edtFrequency.getText().toString().trim();
        String category = spHabitCategory.getSelectedItem().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (name.isEmpty() || frequency.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if(frequency.equals("0")) {
            Toast.makeText(this,"La frecuencia no puede ser 0", Toast.LENGTH_SHORT).show();
            return;
        }

        habitFacade.insertHabit(name, description, frequency, category, userId);
        Toast.makeText(this, getString(R.string.habit_added_successfully), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.cancel_creation))
                .setMessage(getString(R.string.exit_without_creation))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
