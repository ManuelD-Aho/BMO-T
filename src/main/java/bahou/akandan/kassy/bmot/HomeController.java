package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import bahou.akandan.kassy.bmot.modele.Reunion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour la vue d'accueil
 */
public class HomeController {
    @FXML
    private Label userLabel;

    @FXML
    private Button profileButton;

    @FXML
    private ListView<Reunion> upcomingMeetingsListView;

    private MeetClient client;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Définit le client à utiliser
     * @param client Le client pour communiquer avec le serveur
     */
    public void setClient(MeetClient client) {
        this.client = client;
    }

    /**
     * Initialise les données de la vue
     */
    public void initialiserDonnees() {
        // Afficher les informations de l'utilisateur connecté
        userLabel.setText(client.getUtilisateur().getPrenom() + " " + client.getUtilisateur().getNom());

        // Configurer la cellule personnalisée pour les réunions
        upcomingMeetingsListView.setCellFactory(param -> new ListCell<Reunion>() {
            @Override
            protected void updateItem(Reunion reunion, boolean empty) {
                super.updateItem(reunion, empty);

                if (empty || reunion == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(5);
                    container.getStyleClass().add("meeting-list-item");
                    container.setPadding(new Insets(10));

                    Label titleLabel = new Label(reunion.getTitre());
                    titleLabel.getStyleClass().add("meeting-title");

                    Label timeLabel = new Label("Début: " + reunion.getDateDebut().format(dateFormatter));
                    timeLabel.getStyleClass().add("meeting-time");

                    Label typeLabel = new Label("Type: " + reunion.getType());
                    typeLabel.getStyleClass().add("meeting-type");

                    HBox actionsBox = new HBox(10);
                    Button joinButton = new Button("Rejoindre");
                    joinButton.getStyleClass().add("action-button");
                    joinButton.setOnAction(e -> rejoindreReunion(reunion));

                    actionsBox.getChildren().add(joinButton);

                    if (client.getUtilisateur().equals(reunion.getOrganisateur())) {
                        Button editButton = new Button("Modifier");
                        editButton.getStyleClass().add("edit-button");
                        editButton.setOnAction(e -> modifierReunion(reunion));
                        actionsBox.getChildren().add(editButton);
                    }

                    container.getChildren().addAll(titleLabel, timeLabel, typeLabel, actionsBox);
                    setGraphic(container);
                }
            }
        });

        // Charger les réunions
        rafraichirListeReunions();
    }

    /**
     * Rafraîchit la liste des réunions
     */
    private void rafraichirListeReunions() {
        client.demanderListeReunions(reunions -> {
            Platform.runLater(() -> {
                upcomingMeetingsListView.getItems().clear();
                upcomingMeetingsListView.getItems().addAll(reunions);
            });
        });
    }

    /**
     * Gère la création d'une nouvelle réunion
     */
    @FXML
    protected void handleNewMeeting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-meeting-view.fxml"));
            Parent createMeetingView = loader.load();
            CreateMeetingController controller = loader.getController();
            controller.setClient(client);
            controller.setHomeController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(userLabel.getScene().getWindow());
            modalStage.setTitle("Créer une nouvelle réunion");

            Scene scene = new Scene(createMeetingView, 500, 400);
            scene.getStylesheets().addAll(userLabel.getScene().getStylesheets());

            modalStage.setScene(scene);
            modalStage.showAndWait();

            // Actualiser la liste des réunions après création
            rafraichirListeReunions();

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'afficher la fenêtre de création de réunion: " + e.getMessage());
        }
    }

    /**
     * Gère l'action de rejoindre une réunion par code
     */
    @FXML
    protected void handleJoinMeeting() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejoindre une réunion");
        dialog.setHeaderText("Entrez le code de la réunion");
        dialog.setContentText("Code:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            try {
                int reunionId = Integer.parseInt(code);
                client.rejoindreReunion(reunionId, message -> {
                    Platform.runLater(() -> {
                        Map<String, Object> data = (Map<String, Object>) message.getContenu();
                        boolean succes = (boolean) data.get("succes");

                        if (succes) {
                            Reunion reunion = (Reunion) data.get("reunion");
                            ouvrirEcranReunion(reunion);
                        } else {
                            String messageText = (String) data.get("message");
                            afficherErreur("Erreur", messageText);
                        }
                    });
                });
            } catch (NumberFormatException e) {
                afficherErreur("Erreur", "Code de réunion invalide");
            }
        });
    }

    /**
     * Rejoint une réunion sélectionnée
     * @param reunion La réunion à rejoindre
     */
    private void rejoindreReunion(Reunion reunion) {
        client.rejoindreReunion(reunion.getId(), message -> {
            Platform.runLater(() -> {
                Map<String, Object> data = (Map<String, Object>) message.getContenu();
                boolean succes = (boolean) data.get("succes");

                if (succes) {
                    ouvrirEcranReunion(reunion);
                } else {
                    String messageText = (String) data.get("message");
                    afficherErreur("Erreur", messageText);
                }
            });
        });
    }

    /**
     * Ouvre l'interface de modification d'une réunion
     * @param reunion La réunion à modifier
     */
    private void modifierReunion(Reunion reunion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-meeting-view.fxml"));
            Parent editMeetingView = loader.load();
            CreateMeetingController controller = loader.getController(); // On réutilise le même contrôleur
            controller.setClient(client);
            controller.setHomeController(this);
            controller.setReunion(reunion);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(userLabel.getScene().getWindow());
            modalStage.setTitle("Modifier la réunion");

            Scene scene = new Scene(editMeetingView, 500, 400);
            scene.getStylesheets().addAll(userLabel.getScene().getStylesheets());

            modalStage.setScene(scene);
            modalStage.showAndWait();

            // Actualiser la liste des réunions après modification
            rafraichirListeReunions();

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'afficher la fenêtre de modification: " + e.getMessage());
        }
    }

    /**
     * Ouvre l'écran de la réunion
     * @param reunion La réunion à afficher
     */
    private void ouvrirEcranReunion(Reunion reunion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("meeting-room-view.fxml"));
            Parent meetingRoomView = loader.load();
            MeetingRoomController controller = loader.getController();
            controller.setClient(client);
            controller.setReunion(reunion);
            controller.initialiserReunion();

            Stage stage = (Stage) userLabel.getScene().getWindow();
            Scene scene = new Scene(meetingRoomView, 1024, 768);
            scene.getStylesheets().addAll(userLabel.getScene().getStylesheets());

            stage.setScene(scene);
            stage.setTitle("BMO Meet - " + reunion.getTitre());
            stage.setMaximized(true);

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir la salle de réunion: " + e.getMessage());
        }
    }

    /**
     * Gère la déconnexion
     */
    @FXML
    protected void handleLogout() {
        client.deconnecter();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) userLabel.getScene().getWindow();
            Scene scene = new Scene(loginView, 800, 600);
            scene.getStylesheets().addAll(userLabel.getScene().getStylesheets());

            stage.setScene(scene);
            stage.setTitle("BMO Meet - Connexion");
            stage.setMaximized(false);

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de retourner à l'écran de connexion: " + e.getMessage());
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Rafraîchit les données (appelé depuis d'autres contrôleurs)
     */
    public void refreshData() {
        rafraichirListeReunions();
    }
}