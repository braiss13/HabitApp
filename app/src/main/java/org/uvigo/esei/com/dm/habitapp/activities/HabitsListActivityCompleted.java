package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class HabitsListActivityCompleted extends AppCompatActivity {

    private SimpleCursorAdapter adapter;
    private ListView lvHabits;
    private EditText edtHabitFilter;
    private FloatingActionButton fabAddHabit, fabLogout;
    private Spinner spHabitFilter;
    private HabitFacade habitFacade;
    private String filter = "Nombre";
    private int userId; // Usuario logueado

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits_list_completed);

        habitFacade = new HabitFacade((HabitApplication) getApplication(),this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        lvHabits = findViewById(R.id.lvHabits);
        fabAddHabit = findViewById(R.id.fabAddHabit);
        fabLogout = findViewById(R.id.fabLogout);
        edtHabitFilter = findViewById(R.id.edtHabitFilter);
        spHabitFilter = findViewById(R.id.spHabitFilter);

        setupListView();

        registerForContextMenu(lvHabits);

        spHabitFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                filter = adapterView.getItemAtPosition(position).toString();
                filterHabits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                filter = "Nombre";
            }
        });

        edtHabitFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterHabits(); // Filtrar los hábitos cuando se cambie el texto
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("FilterState",MODE_PRIVATE);
        filter = sharedPreferences.getString("filter_type", "Nombre");
        String filterText = sharedPreferences.getString("filter_text", "");
        edtHabitFilter.setText(filterText);

        // Aplicar el filtro actual
        filterHabits();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Guardar el estado del filtro actual y el texto de búsqueda
        SharedPreferences sharedPreferences = getSharedPreferences("FilterState", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("filter_type", filter);
        editor.putString("filter_text", edtHabitFilter.getText().toString());
        editor.apply();
    }

    private void setupListView() {
        // Definimos cómo se mapea cada dato de la base de datos a los elementos visuales del XML
        String[] from = {
                DBManager.COLUMN_HABITO_NOMBRE,
                DBManager.COLUMN_HABITO_CATEGORIA,
                DBManager.COLUMN_HABITO_PROGRESO,
                DBManager.COLUMN_HABITO_FRECUENCIA
        };

        int[] to = {
                R.id.tvHabitName,
                R.id.tvHabitCategory,
                R.id.tvHabitProgress,
                R.id.btnIncrementProgress
        };

        adapter = new SimpleCursorAdapter(this, R.layout.list_item_habit, null, from, to, 0);

        // Adaptador personalizado para gestionar dinámicamente el progreso (0/frecuencia)
        adapter.setViewBinder((view, cursor, columnIndex) -> {
            if (view.getId() == R.id.tvHabitProgress) {
                int progresoIndex = cursor.getColumnIndex(DBManager.COLUMN_HABITO_PROGRESO);
                int frecuenciaIndex = cursor.getColumnIndex(DBManager.COLUMN_HABITO_FRECUENCIA);

                if (progresoIndex == -1 || frecuenciaIndex == -1) {
                    Log.e("setViewBinder", "Column not found in cursor");
                    return false;
                }

                int progreso = cursor.getInt(progresoIndex);
                int frecuencia = cursor.getInt(frecuenciaIndex);
                ((TextView) view).setText(progreso + "/" + frecuencia);
                return true;
            }
            else if (view.getId() == R.id.btnIncrementProgress) {
                int habitIdIndex = cursor.getColumnIndex(DBManager.COLUMN_HABITO_ID);
                if (habitIdIndex == -1) {
                    Log.e("setViewBinder", "Column not found in cursor");
                    return false;
                }

                int habitId = cursor.getInt(habitIdIndex);
                view.setOnClickListener(v -> {
                    habitFacade.incrementProgress(habitId); // Incrementar el progreso
                    filterHabits(); // Recargar la lista
                });
                return true;
            }
            return false; // Permitir que otros valores se gestionen automáticamente
        });

        lvHabits.setAdapter(adapter);

        lvHabits.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HabitsListActivityCompleted.this, EditHabitActivity.class);
            intent.putExtra("habit_id", id);
            startActivity(intent);
        });

    }

    private void loadHabits() {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1); // Recuperar el user_id del usuario logueado

        Cursor cursor = habitFacade.getAllHabits(userId);

        // Verificar que el Cursor tiene las columnas necesarias
        if (cursor.getColumnIndex(DBManager.COLUMN_HABITO_ID) == -1) {
            throw new IllegalStateException("Cursor does not contain COLUMN_HABITO_ID");
        }

        Cursor oldCursor = adapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close(); // Cierra el cursor anterior si existía
        }
    }

    public void filterHabits() {
        String filterText = edtHabitFilter.getText().toString().trim();
        Cursor cursor;

        if ("Nombre".equals(filter)) {
            cursor = habitFacade.getHabitsByName(filterText, userId);
        } else if ("Categoría".equals(filter)) {
            cursor = habitFacade.getHabitsByCategory(filterText, userId);
        } else if ("Completado".equals(filter)) {
            cursor = habitFacade.getHabitsByCompleted(userId);
        } else if ("En progreso".equals(filter)) {
            cursor = habitFacade.getHabitsByIncompleted(userId);
        } else {
            cursor = habitFacade.getAllHabits(userId);
        }

        // Actualizar el ListView con los resultados filtrados
        adapter.swapCursor(cursor);
    }
    private void shareHabitsViaWhatsApp(int habitId) {

        Cursor cursor = habitFacade.getAllHabits(userId);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No hay hábitos para compartir.", Toast.LENGTH_SHORT).show();
            return;
        }


        StringBuilder messageBuilder = new StringBuilder("📋 *Lista de Hábitos Completados*\n\n");
        while (cursor.moveToNext()) {
            String habitName = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_NOMBRE));
            String habitCategory = cursor.getString(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_CATEGORIA));
            int habitProgress = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_PROGRESO));
            int habitFrequency = cursor.getInt(cursor.getColumnIndexOrThrow(DBManager.COLUMN_HABITO_FRECUENCIA));

            messageBuilder.append("🔹 *Hábito*: ").append(habitName).append("\n")
                    .append("📂 *Categoría*: ").append(habitCategory).append("\n")
                    .append("📊 *Progreso*: ").append(habitProgress).append("/").append(habitFrequency).append("\n\n");
        }
        cursor.close();

        String message = messageBuilder.toString();

        // Intent para compartir por WhatsApp
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");

        try {
            startActivity(sendIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "WhatsApp no está instalado en este dispositivo", Toast.LENGTH_SHORT).show();
        }
    }
}
