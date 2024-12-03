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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class HabitsListActivity  extends AppCompatActivity{
    private SimpleCursorAdapter adapter;
    private ListView lvHabits;
    private EditText edtHabitFilter;
    private FloatingActionButton fabAddHabit, fabLogout;
    private Spinner spHabitFilter;
    private HabitFacade habitFacade;
    private String filter = "Nombre";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits_list);

        habitFacade = new HabitFacade((HabitApplication) getApplication());

        lvHabits = findViewById(R.id.lvHabits);
        fabAddHabit = findViewById(R.id.fabAddHabit);
        fabLogout = findViewById(R.id.fabLogout);
        edtHabitFilter = findViewById(R.id.edtHabitFilter);
        spHabitFilter = findViewById(R.id.spHabitFilter);

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
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterHabits(); // Filtrar los hábitos cuando se cambie el texto
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        setupListView();
        registerForContextMenu(lvHabits);


        fabAddHabit.setOnClickListener(view -> {
            Intent intent = new Intent(HabitsListActivity.this, AddHabitActivity.class);
            startActivity(intent);
        });

        fabLogout.setOnClickListener(view -> logout());
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Inspeccionar las columnas de la tabla de hábitos
        Cursor cursor = habitFacade.getAllHabits();
        String[] columnNames = cursor.getColumnNames();
        for (String column : columnNames) {
            Log.d("DatabaseColumns", column); // Imprime los nombres de las columnas
        }
        cursor.close();

        // Cargar los hábitos en el ListView
        loadHabits();
    }
    private void setupListView() {
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_habit,
                null,
                new String[]{DBManager.COLUMN_HABITO_NOMBRE, DBManager.COLUMN_HABITO_CATEGORIA},
                new int[]{R.id.tvHabitName, R.id.tvHabitCategory},
                0
        );
        lvHabits.setAdapter(adapter);

        lvHabits.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HabitsListActivity.this, EditHabitActivity.class);
            intent.putExtra("habit_id", id);
            startActivity(intent);
        });
    }
    private void loadHabits() {
        Cursor cursor = habitFacade.getAllHabits();
        Cursor oldCursor = adapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close(); // Cierra el cursor anterior si existía
        }
    }

    private void filterHabits() {
        String filterText = edtHabitFilter.getText().toString().trim();
        Cursor cursor;

        if ("Nombre".equals(filter)) {
            cursor = habitFacade.getHabitsByName(filterText);
        } else if ("Categoría".equals(filter)) {
            cursor = habitFacade.getHabitsByCategory(filterText);
        } else {
            cursor = habitFacade.getAllHabits();
        }

        // Actualizar el ListView con los resultados filtrados
        adapter.swapCursor(cursor);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvHabits) {
            getMenuInflater().inflate(R.menu.menu_contextual_habit, menu);
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long habitId = info.id;

        if (item.getItemId() == R.id.menu_edit_habit) {
            Intent intent = new Intent(this, EditHabitActivity.class);
            intent.putExtra("habit_id", habitId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_delete_habit) {
            confirmDeletion(habitId);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    private void confirmDeletion(long habitId) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_deletion_title))
                .setMessage(getString(R.string.confirm_delete_habit))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    habitFacade.deleteHabit((int) habitId);
                    loadHabits();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MenuDebug", "SE LLAMA AL MÉTODO ONCREATE OPTIONS MENU");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_habit) {
            startActivity(new Intent(this, AddHabitActivity.class));
            return true;
        }
        /*else if (item.getItemId() == R.id.menu_filter_habits) {
            startActivity(new Intent(this, FilterActivity.class));
            return true;

         */
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void logout(){
        SharedPreferences sharedPreferences = getSharedPreferences("Session",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        Intent intent = new Intent(HabitsListActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_app_title))
                .setMessage(getString(R.string.exit_app_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

}
