package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.GUIClient;
import javafx.application.Application;

public class HelloApplication {
    public static void main(String[] args) {
        // Lancement de l'application client GUI
        Application.launch(GUIClient.class, args);
    }
}