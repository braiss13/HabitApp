package org.uvigo.esei.com.dm.habitapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class EditHabitActivity extends AppCompatActivity {
    private EditText edtName, edtDescription, edtFrequency, edtCategory;
    private Button btnSave;
    private HabitFacade habitFacade;
    private long habitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_habit);

        habitFacade = new HabitFacade((HabitApplication) getApplication());
        edtName = findViewById(R.id.edtHabitName);
        edtDescription = findViewById(R.id.edtHabitDescription);
        edtFrequency = findViewById(R.id.edtHabitFrequency);
        edtCategory = findViewById(R.id.edtHabitCategory);
        btnSave = findViewById(R.id.btnSaveHabit);

        habitId = getIntent().getLongExtra("habit_id", -1);

        loadHabitDetails();

        btnSave.setOnClickListener(v -> updateHabit());
    }

    private void loadHabitDetails() {
        Cursor cursor = habitFacade.getAllHabits();
        if (cursor.moveToPosition((int) habitId -1)) {
            edtName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_NOMBRE)));
            edtDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_DESCRIPCION)));
            edtFrequency.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_FRECUENCIA)));
            edtCategory.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_CATEGORIA)));
        }
        cursor.close();
    }

    private void updateHabit() {
        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String frequency = edtFrequency.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();

        if (name.isEmpty() || frequency.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        habitFacade.updateHabit((int) habitId, name, description, frequency, category, 0);
        Toast.makeText(this, getString(R.string.habit_updated_successfully), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cancelar edición")
                .setMessage("¿Deseas salir sin guardar los cambios?")
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}