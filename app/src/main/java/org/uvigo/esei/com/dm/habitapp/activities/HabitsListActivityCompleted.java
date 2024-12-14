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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitsListActivityCompleted extends AppCompatActivity {

    private SimpleCursorAdapter adapter;
    private ListView lvHabitsCompleted;
    private EditText edtHabitFilter;
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

        lvHabitsCompleted = findViewById(R.id.lvHabitsCompleted);
        edtHabitFilter = findViewById(R.id.edtHabitFilter);
        spHabitFilter = findViewById(R.id.spHabitFilter);

        //setupCompletedListView();

        registerForContextMenu(lvHabitsCompleted);

        spHabitFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                filter = adapterView.getItemAtPosition(position).toString();
               // filterHabits();
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
                //filterHabits(); // Filtrar los hábitos cuando se cambie el texto
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
        //filterHabits();
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

   /* private void setupCompletedListView() {
        String[] from = {
                DBManager.COLUMN_HABITO_NOMBRE,
                DBManager.COLUMN_HABITO_CATEGORIA,
                DBManager.COLUMN_HABITO_COMPLETADO_FECHA_COMPLETADO
        };

        int[] to = {
                R.id.tvHabitName,
                R.id.tvHabitCategory,
                R.id.tvCompletionDate
        };

        adapter = new SimpleCursorAdapter(this, R.layout.list_item_completed_habit, null, from, to, 0);

        // Formatear la fecha de completado
        adapter.setViewBinder((view, cursor, columnIndex) -> {
            if (view.getId() == R.id.tvCompletionDate) {

                long completedDateMillis = cursor.getLong(columnIndex);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                String formattedDate = sdf.format(new Date(completedDateMillis));
                ((TextView) view).setText(formattedDate);
                return true;
            }
            return false;
        });

        lvHabitsCompleted.setAdapter(adapter);
    }

    private void loadHabits() {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1); // Recuperar el user_id del usuario logueado

        Cursor cursor = habitFacade.getCompletedHabits();

        // Verificar que el Cursor tiene las columnas necesarias
        if (cursor.getColumnIndex(DBManager.COLUMN_HABITO_ID) == -1) {
            throw new IllegalStateException("Cursor does not contain COLUMN_HABITO_ID");
        }

        Cursor oldCursor = adapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close(); // Cierra el cursor anterior si existía
        }
    }*/

  /*  public void filterHabits() {
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
    }*/
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
