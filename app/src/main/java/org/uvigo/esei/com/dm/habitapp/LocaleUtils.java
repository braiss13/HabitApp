package org.uvigo.esei.com.dm.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;
import java.util.regex.Pattern;

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

    // Método para comprobar el formato de un correo electrónico
    public static boolean isValidEmail(String email) {
        // Expresión regular para validar el formato del correo electrónico
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    // Método para comprobar la validez de una contraseña
    public static boolean isValidPassword(String password) {
        // Al menos 8 caracteres, con al menos una letra y un número
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        return password != null && Pattern.compile(passwordRegex).matcher(password).matches();
    }
}
