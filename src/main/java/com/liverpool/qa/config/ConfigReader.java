package main.java.com.liverpool.qa.config;

import java.util.Properties;
import java.io.InputStream;
import java.util.Properties;


/**
 * Lector centralizado de configuración.
 * Prioriza variables enviadas por línea de comandos (-Dclave=valor)
 * sobre los valores por defecto definidos en config.properties.
 */

public final class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = new Properties();


    static{
        try {
            InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (input == null){
                throw new IllegalStateException("No se encontró el archivo '" + CONFIG_FILE + "' en el classpath (src/test/resources).");
            }
            PROPERTIES.load(inputStream);

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el archivo de configuración: " + CONFIG_FILE, e);
            }
    }

    public static String get(String key) {
        // 1. Si se pasa por terminal (ej. mvn test -Dbrowser.headless=true), usa ese valor.
        // 2. Si no se pasa por terminal, toma el valor de config.properties.
        return System.getProperty(key, properties.getProperty(key));
    }

    /**
     * método destinado a recibir propiedades int desde consola, 
     * ideal para cuando se requiere cambiar o editar el timeout
    */

    public static int getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La propiedad '" + key + "' no tiene un formato numérico válido.", e);
        }
    }

    /**
     * método destinado a recibir propiedades boolean desde consola, 
     * ideal para cuando se requiere realizar ejecuciones headless
    */

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

}