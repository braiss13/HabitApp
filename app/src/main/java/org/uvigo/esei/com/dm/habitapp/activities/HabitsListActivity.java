package org.uvigo.esei.com.dm.habitapp.activities;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import org.uvigo.esei.com.dm.habitapp.HabitApplication;
import org.uvigo.esei.com.dm.habitapp.MainActivity;
import org.uvigo.esei.com.dm.habitapp.R;
import org.uvigo.esei.com.dm.habitapp.database.DBManager;
import org.uvigo.esei.com.dm.habitapp.database.HabitFacade;

public class HabitsListActivity extends AppCompatActivity {

    private SimpleCursorAdapter adapter;
    private ListView lvHabits;
    private EditText edtHabitFilter;
    private FloatingActionButton fabAddHabit, fabLogout;
    private Spinner spHabitFilter;
    private HabitFacade habitFacade;
    private String filter = "Nombre";
    private int userId; // Usuario logueado

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits_list);

        habitFacade = new HabitFacade((HabitApplication) getApplication(), this);

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

        fabAddHabit.setOnClickListener(view -> {
            Intent intent = new Intent(HabitsListActivity.this, AddHabitActivity.class);
            startActivity(intent);
        });

        fabLogout.setOnClickListener(view -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargar los hábitos en el ListView
        loadHabits();
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
                    loadHabits(); // Recargar la lista
                });
                return true;
            }
            return false; // Permitir que otros valores se gestionen automáticamente
        });

        lvHabits.setAdapter(adapter);

        lvHabits.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HabitsListActivity.this, EditHabitActivity.class);
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

    private void filterHabits() {
        String filterText = edtHabitFilter.getText().toString().trim();
        Cursor cursor;

        if ("Nombre".equals(filter)) {
            cursor = habitFacade.getHabitsByName(filterText, userId);
        } else if ("Categoría".equals(filter)) {
            cursor = habitFacade.getHabitsByCategory(filterText, userId);
        } else {
            cursor = habitFacade.getAllHabits(userId);
        }

        // Actualizar el ListView con los resultados filtrados
        adapter.swapCursor(cursor);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d("ContextMenu", "SE ESTÁ LLAMANDO A ON CREATE CONTEXTMENU :)");
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvHabits) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("ContextMenu", "ESTOY LLAMANDO AL CONTEXT ITEM SELECTED CON: " + item.getTitle());
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
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);

        int userId = sharedPreferences.getInt("user_id", -1); // Asegúrate de usar la clave correcta

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_deletion_title))
                .setMessage(getString(R.string.confirm_delete_habit))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    habitFacade.deleteHabit((int) habitId, userId);
                    loadHabits(); // Recargar los hábitos tras eliminar uno
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d("MenuDebug", "SE LLAMA AL MÉTODO ONCREATE OPTIONS MENU");
        this.getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_habit) {
            startActivity(new Intent(this, AddHabitActivity.class));
            // TODO -> Hay que cambiar esta opción a un método que sume +1 a todos los hábitos
            return true;
        } else if (item.getItemId() == R.id.menu_filter_habits) {
            filterHabits();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        new AlertDialog.Builder(this)
                .setTitle("Cierre de Sesión")
                .setMessage("¿Está seguro de que quiere cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(HabitsListActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(HabitsListActivity.this, "La sesión ha sido cerrada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();

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
