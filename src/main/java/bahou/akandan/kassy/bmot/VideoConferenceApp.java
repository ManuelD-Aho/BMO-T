package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.serveurs.ServerMain;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

/**
 * Application principale pour la plateforme de vidéoconférence BMO Meet
 */
public class VideoConferenceApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Charger la feuille de style globale
        String css = getClass().getResource("styles.css").toExternalForm();

        // Charger la vue de connexion
        FXMLLoader fxmlLoader = new FXMLLoader(VideoConferenceApp.class.getResource("login-view.fxml"));
        Parent loginView = fxmlLoader.load();

        // Configurer la scène
        Scene scene = new Scene(loginView, 800, 600);
        scene.getStylesheets().add(css);

        // Configurer la fenêtre principale
        stage.setTitle("BMO Meet - Vidéoconférence");
        try {
            Image appIcon = new Image(VideoConferenceApp.class.getResourceAsStream("images/meet_icon.png"));
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Impossible de charger l'icône de l'application: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Vérifier si l'application doit être lancée en mode serveur
        if (args.length > 0 && (args[0].equals("--server") || args[0].equals("-s"))) {
            // Lancer le serveur
            ServerMain.main(Arrays.copyOfRange(args, 1, args.length));
        } else {
            // Lancer l'interface utilisateur JavaFX
            launch(args);
        }
    }
}