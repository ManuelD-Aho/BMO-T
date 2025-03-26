package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class LoginController {
    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private MeetClient client;

    @FXML
    public void initialize() {
        client = new MeetClient("localhost", 8888);
        try {
            client.connecter();
        } catch (IOException e) {
            statusLabel.setText("Erreur de connexion au serveur");
            statusLabel.getStyleClass().add("error");
        }
    }

    @FXML
    protected void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            statusLabel.getStyleClass().add("error");
            return;
        }

        statusLabel.setText("Connexion en cours...");
        client.authentifier(login, password, message -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");

            Platform.runLater(() -> {
                if (succes) {
                    Utilisateur utilisateur = (Utilisateur) data.get("utilisateur");
                    client.setUtilisateur(utilisateur);
                    ouvrirEcranPrincipal();
                } else {
                    String messageText = (String) data.get("message");
                    statusLabel.setText(messageText);
                    statusLabel.getStyleClass().add("error");
                }
            });
        });
    }

    @FXML
    protected void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register-view.fxml"));
            Parent registerView = loader.load();
            RegisterController controller = loader.getController();
            controller.setClient(client);

            Scene currentScene = loginField.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene registerScene = new Scene(registerView, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(registerScene);
            stage.setTitle("BMO Meet - Inscription");

        } catch (IOException e) {
            statusLabel.setText("Erreur lors du chargement de la page d'inscription");
            statusLabel.getStyleClass().add("error");
        }
    }

    private void ouvrirEcranPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent homeView = loader.load();
            HomeController controller = loader.getController();
            controller.setClient(client);
            controller.initialiserDonnees();

            Scene currentScene = loginField.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene homeScene = new Scene(homeView, 1024, 768);
            stage.setScene(homeScene);
            stage.setTitle("BMO Meet - Accueil");
            stage.setMaximized(true);

        } catch (IOException e) {
            statusLabel.setText("Erreur lors du chargement de la page d'accueil");
            statusLabel.getStyleClass().add("error");
        }
    }
}