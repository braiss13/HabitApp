package org.uvigo.esei.com.dm.habitapp.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class EditHabitActivity extends AppCompatActivity {
    private EditText edtName, edtDescription, edtFrequency;
    private Button btnSave;
    private Spinner spHabitCategory;
    private String selectedCategory;
    private HabitFacade habitFacade;
    private long habitId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_habit);

        //Referenciamos todos los elementos del Layout para trabajar con ellos

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        edtName = findViewById(R.id.edtHabitName);
        edtDescription = findViewById(R.id.edtHabitDescription);
        edtFrequency = findViewById(R.id.edtHabitFrequency);
        spHabitCategory = findViewById(R.id.spHabitCategory);

        //Manejo del Spinner de categoría
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
        btnSave = findViewById(R.id.btnSaveHabit); //Manejo del Botón de guardar hábito

        habitId = getIntent().getLongExtra("habit_id", -1);

        loadHabitDetails();

        btnSave.setOnClickListener(v -> updateHabit());
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Llamar al método que aplica el idioma según las preferencias
        LocaleUtils.setLocaleFromPreferences(this);
    }

    //Método para cargar los atributos del Hábito que quieres editar
    private void loadHabitDetails() {

        int userId = getSharedPreferences("Session", MODE_PRIVATE).getInt("user_id", -1); // Obtener usuario actual
        Cursor cursor = habitFacade.getHabitById((int) habitId, userId); // Modificar HabitFacade para admitir userId

        if (cursor != null && cursor.moveToFirst()) {
            edtName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_NOMBRE)));
            edtDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_DESCRIPCION)));
            edtFrequency.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_FRECUENCIA)));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_CATEGORIA));

            // Configurar el Spinner con la categoría guardada
            SpinnerAdapter adapter = spHabitCategory.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(category)) {
                    spHabitCategory.setSelection(i);
                    break;
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_habit_not_found), Toast.LENGTH_SHORT).show();
            finish(); // Salir de la actividad si no se encuentra el hábito
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    //Método para actualizar el hábito en la BD.
    private void updateHabit() {

        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String frequency = edtFrequency.getText().toString().trim();
        String category = spHabitCategory.getSelectedItem().toString();

        if (name.isEmpty() || frequency.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if(frequency.equals("0")){
            Toast.makeText(this,getString(R.string.no_0_frequency), Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = getSharedPreferences("Session", MODE_PRIVATE).getInt("user_id", -1); // Obtener usuario actual
        int rowsUpdated = habitFacade.updateHabit((int) habitId, name, description, frequency, category, 0, userId); // Modificar HabitFacade

        if (rowsUpdated > 0) {
            Toast.makeText(this, getString(R.string.habit_updated_successfully), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.toast_cant_update_habit), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    // Método que se ejecuta al pulsar el botón de ir hacia atrás
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.cancel_edit))
                .setMessage(getString(R.string.leave_wo_save))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
