package org.uvigo.esei.com.dm.habitapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordSecurity {

    public static String hashPassword(String normalPassword){

        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(normalPassword.getBytes()); //HASHING DE LOS BYTES

            // Convertir bytes a formato hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        }catch(NoSuchAlgorithmException alg){

            throw new RuntimeException("Error al crear hash de contraseña", alg);
        }

    }

    public static boolean checkPassword(String normalPassword, String hashedPassword){

        String generatedHash = hashPassword(normalPassword); // Generar el hash de la contraseña
        return generatedHash.equals(hashedPassword); // Comparar el hash generado con el hash almacenado

    }
}
