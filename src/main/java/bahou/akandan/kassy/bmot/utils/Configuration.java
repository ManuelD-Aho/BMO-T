package bahou.akandan.kassy.bmot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static Properties properties = new Properties();
    private static final String CONFIG_FILE = "src/main/resources/bahou/akandan/kassy/bmot/config.properties";

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier de configuration: " + e.getMessage());
            // Valeurs par défaut
            properties.setProperty("serveur.port", "8888");
            properties.setProperty("serveur.timeout", "60000");
            properties.setProperty("serveur.maxConnexions", "100");
        }
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("serveur.port", "8888"));
    }

    public static int getTimeout() {
        return Integer.parseInt(properties.getProperty("serveur.timeout", "60000"));
    }

    public static int getMaxConnexions() {
        return Integer.parseInt(properties.getProperty("serveur.maxConnexions", "100"));
    }
}