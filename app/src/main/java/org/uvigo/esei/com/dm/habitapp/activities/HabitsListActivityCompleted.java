package org.uvigo.esei.com.dm.habitapp.activities;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.LocaleUtils;
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

        //Referenciamos todos los elementos del Layout para trabajar con ellos

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        lvHabitsCompleted = findViewById(R.id.lvHabitsCompleted);

        setupCompletedListView();
        registerForContextMenu(lvHabitsCompleted);

        loadCompletedHabits(); // Llama después de inicializar lo necesario
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompletedHabits(); // Refresca los datos al reanudar la actividad
        LocaleUtils.setLocaleFromPreferences(this);
    }

    //Método para cargar los hábitos completados
    private void loadCompletedHabits() {
        if (userId == -1) {
            Toast.makeText(this, getString(R.string.user_not_loged), Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = habitFacade.getCompletedHabits(userId);

        if (cursor == null) {
            Toast.makeText(this, getString(R.string.habits_completed_not_loadede), Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualiza el adaptador con el nuevo cursor
        Cursor oldCursor = adapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close(); // Cierra el cursor anterior si existía
        }
    }

    // Configura la lista de datos usando un Cursor para recuperarlos de la BD y luego mostrarlos como ListView
    private void setupCompletedListView() {
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

        adapter.setViewBinder((view, cursor, columnIndex) -> {
            if (view.getId() == R.id.tvCreationDate || view.getId() == R.id.tvCompletionDate) {
                try {
                    long dateMillis = cursor.getLong(columnIndex); // Obtén la fecha en milisegundos
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Formato amigable
                    String formattedDate = sdf.format(new Date(dateMillis)); // Formatea la fecha
                    ((TextView) view).setText(formattedDate); // Asigna la fecha formateada al TextView
                    return true; // Indica que manejaste el bind
                } catch (Exception e) {
                    ((TextView) view).setText(""); // Muestra vacío en caso de error
                    return true; // Previene la asignación predeterminada
                }
            }
            return false;
        });

        lvHabitsCompleted.setAdapter(adapter);
    }
}
