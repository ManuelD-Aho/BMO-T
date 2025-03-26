package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import bahou.akandan.kassy.bmot.communication.Protocole;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import bahou.akandan.kassy.bmot.utils.Configuration;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

/**
 * Contrôleur pour la vue de connexion
 */
public class LoginController {
    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private MeetClient client;

    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        try {
            // Initialiser le client avec les paramètres de configuration
            String host = Configuration.getServerHost();
            int port = Configuration.getPort();
            client = new MeetClient(host, port);
            client.connecter();

            statusLabel.setText("Connecté au serveur");
            statusLabel.getStyleClass().remove("error");
            statusLabel.getStyleClass().add("success");
        } catch (IOException e) {
            statusLabel.setText("Erreur de connexion au serveur: " + e.getMessage());
            statusLabel.getStyleClass().add("error");

            // Désactiver les boutons si le serveur n'est pas accessible
            loginButton.setDisable(true);
            registerButton.setDisable(true);
        }

        // Ajouter un listener pour permettre la connexion en appuyant sur Entrée
        passwordField.setOnAction(event -> handleLogin());
    }

    /**
     * Gère le clic sur le bouton de connexion
     */
    @FXML
    protected void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            statusLabel.getStyleClass().add("error");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Connexion en cours...");
        statusLabel.getStyleClass().remove("error");

        // Tenter de se connecter
        client.authentifier(login, password, message -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get(Protocole.CLE_SUCCES);

            Platform.runLater(() -> {
                if (succes) {
                    Utilisateur utilisateur = (Utilisateur) data.get("utilisateur");
                    client.setUtilisateur(utilisateur);
                    ouvrirEcranPrincipal();
                } else {
                    String messageText = (String) data.get(Protocole.CLE_MESSAGE);
                    statusLabel.setText(messageText);
                    statusLabel.getStyleClass().add("error");
                    loginButton.setDisable(false);
                }
            });
        });
    }

    /**
     * Gère le clic sur le bouton d'inscription
     */
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
            registerScene.getStylesheets().addAll(currentScene.getStylesheets());

            stage.setScene(registerScene);
            stage.setTitle("BMO Meet - Inscription");

        } catch (IOException e) {
            statusLabel.setText("Erreur lors du chargement de la page d'inscription");
            statusLabel.getStyleClass().add("error");
        }
    }

    /**
     * Ouvre l'écran principal après une connexion réussie
     */
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
            homeScene.getStylesheets().addAll(currentScene.getStylesheets());

            stage.setScene(homeScene);
            stage.setTitle("BMO Meet - Accueil");
            stage.setMaximized(true);

        } catch (IOException e) {
            statusLabel.setText("Erreur lors du chargement de la page d'accueil");
            statusLabel.getStyleClass().add("error");
            loginButton.setDisable(false);
        }
    }
}