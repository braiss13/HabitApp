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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        SharedPreferences sharedPreferences = getSharedPreferences("FilterState",MODE_PRIVATE);
        filter = sharedPreferences.getString("filter_type", "Nombre");
        String filterText = sharedPreferences.getString("filter_text", "");
        edtHabitFilter.setText(filterText);
        long lastPauseTime = sharedPreferences.getLong("lastPauseTime", -1);

        // Si es la primera vez que se abre la actividad, no hay necesidad de resetear
        if (lastPauseTime == -1) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long elapsedTimeMillis = currentTimeMillis - lastPauseTime;
        long oneWeekMillis = 7L * 24 * 60 * 60 * 1000;

        // Verificar si ha pasado una semana
        if (elapsedTimeMillis >= oneWeekMillis) {
            resetAllHabitsProgress();
        }

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


        // Guardar la marca de tiempo actual en milisegundos
        long currentTimeMillis = System.currentTimeMillis();
        editor.putLong("lastPauseTime", currentTimeMillis);

        editor.apply();
    }

    public void resetAllHabitsProgress(){
        habitFacade.resetAllHabitsProgress(userId);
    }

    private void setupListView() {
        // Definimos cómo se mapea cada dato de la base de datos a los elementos visuales del XML
        String[] from = {
                DBManager.COLUMN_HABITO_NOMBRE,
                DBManager.COLUMN_HABITO_CATEGORIA,
                DBManager.COLUMN_HABITO_PROGRESO,
                DBManager.COLUMN_HABITO_FECHA_CREACION,
                DBManager.COLUMN_HABITO_FRECUENCIA,
                DBManager.COLUMN_HABITO_FRECUENCIA
        };

        int[] to = {
                R.id.tvHabitName,
                R.id.tvHabitCategory,
                R.id.tvHabitProgress,
                R.id.tvCreationDate,
                R.id.btnIncrementProgress,
                R.id.btnDecrementProgress
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

            } else if (view.getId() == R.id.btnIncrementProgress) {
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
            } else if (view.getId() == R.id.btnDecrementProgress) {
                int habitIdIndex = cursor.getColumnIndex(DBManager.COLUMN_HABITO_ID);
                int progressIndex = cursor.getColumnIndex(DBManager.COLUMN_HABITO_PROGRESO); // Obtenemos el índice del progreso

                if (habitIdIndex == -1 || progressIndex == -1) {
                    Log.e("setViewBinder", "Column not found in cursor");
                    return false;
                }

                int habitId = cursor.getInt(habitIdIndex);
                int progress = cursor.getInt(progressIndex); // Obtenemos el valor del progreso

                view.setOnClickListener(v -> {
                    // Crear el diálogo de confirmación
                    new AlertDialog.Builder(HabitsListActivity.this, R.style.AppTheme_Dialog)
                            .setTitle("Eliminar progreso") // Título del diálogo
                            .setMessage("¿Seguro que quiere restar uno al progreso?") // Mensaje del diálogo
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                // Verificar si el progreso es 1
                                if (progress == 1) {
                                    habitFacade.updateProgressToZero(habitId); // Si es 1, lo ponemos a 0
                                } else {
                                    habitFacade.decrementProgress(habitId); // Sino, restamos 1 al progreso
                                }
                                filterHabits(); // Recargar la lista después de hacer el cambio
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();
                });
                return true;

            }else if(view.getId() == R.id.tvCreationDate){

                int creationDateIndex = cursor.getColumnIndex(DBManager.COLUMN_HABITO_FECHA_CREACION);
                if (creationDateIndex == -1) {
                    Log.e("setViewBinder", "Column not found in cursor");
                    return false;
                }

                // Obtener la fecha en milisegundos desde la columna
                long creationDateMillis = cursor.getLong(creationDateIndex);

                // Formatear la fecha en un formato legible
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = sdf.format(new Date(creationDateMillis));

                // Establecer el texto en el TextView
                ((TextView) view).setText(formattedDate);
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

    private void incrementAllHabits(){
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        habitFacade.incrementAllHabitsProgress(userId);

        // Refrescar la lista
        filterHabits();
        Toast.makeText(this, getString(R.string.toast_progress_incremented_all), Toast.LENGTH_SHORT).show();

    }

    private void shareHabitsViaWhatsApp(int habitId) {

        Cursor cursor = habitFacade.getAllHabits(userId);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No hay hábitos para compartir.", Toast.LENGTH_SHORT).show();
            return;
        }


        StringBuilder messageBuilder = new StringBuilder("📋 *Lista de Hábitos*\n\n");
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvHabits) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
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
        }else{
            return super.onContextItemSelected(item);
        }
    }

    private void confirmDeletion(long habitId) {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);

        int userId = sharedPreferences.getInt("user_id", -1);

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
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
        this.getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_increment_all_habits) {
            incrementAllHabits();
            return true;

        } else if (item.getItemId() == R.id.menu_profile) {
            Intent intent = new Intent(HabitsListActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;

        } else if(item.getItemId() == R.id.menu_share) {
            shareHabitsViaWhatsApp(userId);
            return true;

        }else if(item.getItemId() == R.id.menu_completed_habits){
            Intent intent = new Intent(HabitsListActivity.this, HabitsListActivityCompleted.class);
            startActivity(intent);
            return true;

        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle( getString(R.string.session_close))
                .setMessage(getString(R.string.session_close_confirmation))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(HabitsListActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(HabitsListActivity.this, getString(R.string.session_closed), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.exit_app_title))
                .setMessage(getString(R.string.exit_app_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    super.onBackPressed(); // Llamar al comportamiento predeterminado para cerrar la actividad
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
