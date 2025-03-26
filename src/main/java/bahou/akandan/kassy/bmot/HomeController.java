package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import bahou.akandan.kassy.bmot.modele.Reunion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HomeController {
    @FXML
    private Label userLabel;

    @FXML
    private Button profileButton;

    @FXML
    private ListView<Reunion> upcomingMeetingsListView;

    private MeetClient client;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setClient(MeetClient client) {
        this.client = client;
    }

    public void initialiserDonnees() {
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

                    Label titleLabel = new Label(reunion.getTitre());
                    titleLabel.getStyleClass().add("meeting-title");

                    Label timeLabel = new Label("Début: " + reunion.getDateDebut().format(dateFormatter));
                    timeLabel.getStyleClass().add("meeting-time");

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

                    container.getChildren().addAll(titleLabel, timeLabel, actionsBox);
                    setGraphic(container);
                }
            }
        });

        // Charger les réunions
        rafraichirListeReunions();
    }

    private void rafraichirListeReunions() {
        client.demanderListeReunions(reunions -> {
            Platform.runLater(() -> {
                upcomingMeetingsListView.getItems().clear();
                upcomingMeetingsListView.getItems().addAll(reunions);
            });
        });
    }

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
            modalStage.setScene(scene);
            modalStage.showAndWait();

            // Actualiser la liste des réunions après création
            rafraichirListeReunions();

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'afficher la fenêtre de création de réunion: " + e.getMessage());
        }
    }

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

    private void modifierReunion(Reunion reunion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-meeting-view.fxml"));
            Parent editMeetingView = loader.load();
            EditMeetingController controller = loader.getController();
            controller.setClient(client);
            controller.setReunion(reunion);
            controller.setHomeController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(userLabel.getScene().getWindow());
            modalStage.setTitle("Modifier la réunion");

            Scene scene = new Scene(editMeetingView, 500, 400);
            modalStage.setScene(scene);
            modalStage.showAndWait();

            // Actualiser la liste des réunions après modification
            rafraichirListeReunions();

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'afficher la fenêtre de modification: " + e.getMessage());
        }
    }

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
            stage.setScene(scene);
            stage.setTitle("BMO Meet - " + reunion.getTitre());
            stage.setFullScreen(true);

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir la salle de réunion: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout() {
        client.deconnecter();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) userLabel.getScene().getWindow();
            Scene scene = new Scene(loginView, 800, 600);
            stage.setScene(scene);
            stage.setTitle("BMO Meet - Connexion");
            stage.setMaximized(false);

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de retourner à l'écran de connexion: " + e.getMessage());
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}