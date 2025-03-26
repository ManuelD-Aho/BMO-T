package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class RegisterController {
    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label statusLabel;

    private MeetClient client;

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> handleRegister());
        backButton.setOnAction(e -> handleBack());
    }

    public void setClient(MeetClient client) {
        this.client = client;
    }

    @FXML
    private void handleRegister() {
        // Vérifier que tous les champs sont remplis
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                confirmPasswordField.getText().isEmpty() || nomField.getText().isEmpty() ||
                prenomField.getText().isEmpty()) {

            setErrorStatus("Veuillez remplir tous les champs");
            return;
        }

        // Vérifier que les mots de passe correspondent
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            setErrorStatus("Les mots de passe ne correspondent pas");
            return;
        }

        // Envoyer la demande d'inscription
        statusLabel.setText("Création du compte en cours...");

        client.creerUtilisateur(
                loginField.getText(),
                passwordField.getText(),
                nomField.getText(),
                prenomField.getText(),
                message -> {
                    Map<String, Object> data = (Map<String, Object>) message.getContenu();
                    boolean succes = (boolean) data.get("succes");
                    String messageText = (String) data.get("message");

                    javafx.application.Platform.runLater(() -> {
                        if (succes) {
                            showSuccessMessage("Compte créé avec succès");
                            navigateToLogin();
                        } else {
                            setErrorStatus(messageText);
                        }
                    });
                }
        );
    }

    @FXML
    private void handleBack() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent loginView = loader.load();

            Scene currentScene = loginField.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene loginScene = new Scene(loginView, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(loginScene);
            stage.setTitle("BMO Meet - Connexion");

        } catch (IOException e) {
            setErrorStatus("Erreur lors du chargement de la page de connexion");
        }
    }

    private void setErrorStatus(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().add("error");
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}