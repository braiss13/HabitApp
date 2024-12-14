package org.uvigo.esei.com.dm.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleUtils {
    // Método para aplicar el idioma desde las preferencias
    public static void setLocaleFromPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String language = preferences.getString("language", "es");  // Valor por defecto "es"
        setLocale(context, language);
    }

    // Método para establecer el idioma en el sistema
    private static void setLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        // Actualizar configuración del sistema con el nuevo idioma
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
