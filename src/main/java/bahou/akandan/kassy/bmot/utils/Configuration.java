package bahou.akandan.kassy.bmot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private static Properties props = new Properties();

    static {
        try (InputStream in = Configuration.class.getResourceAsStream("/bahou/akandan/kassy/bmot/config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("Fichier config.properties non trouvé !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
