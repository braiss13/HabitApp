package org.uvigo.esei.com.dm.habitapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class HabitsListActivity extends AppCompatActivity {
    private DBManager dbManager;
    private SimpleCursorAdapter adapter;
    private ListView lvHabits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits_list);

        dbManager = new DBManager(this);
        lvHabits = findViewById(R.id.lvHabits);

        setupListView();
        registerForContextMenu(lvHabits);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Inspeccionar las columnas de la tabla de hábitos
        Cursor cursor = dbManager.getAllHabits();
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
        Cursor cursor = dbManager.getAllHabits();
        adapter.changeCursor(cursor);
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
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este hábito?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    dbManager.deleteHabit((int) habitId);
                    loadHabits();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
}
