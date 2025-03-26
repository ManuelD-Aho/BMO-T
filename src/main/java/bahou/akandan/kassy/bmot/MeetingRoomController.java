package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import bahou.akandan.kassy.bmot.clients.VideoStream;
import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MeetingRoomController {
    @FXML
    private Label meetingTitleLabel;

    @FXML
    private Label meetingTimeLabel;

    @FXML
    private GridPane videoGrid;

    @FXML
    private VBox waitingOverlay;

    @FXML
    private VBox sidePanel;

    @FXML
    private Label participantCountLabel;

    @FXML
    private ListView<Utilisateur> participantsListView;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button micButton;

    @FXML
    private ImageView micIcon;

    @FXML
    private Button cameraButton;

    @FXML
    private ImageView cameraIcon;

    @FXML
    private Button screenShareButton;

    @FXML
    private ImageView screenShareIcon;

    private MeetClient client;
    private Reunion reunion;
    private Map<String, ImageView> participantVideos = new HashMap<>();
    private ObservableList<Utilisateur> participants = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Images pour les boutons
    private Image micOnImage;
    private Image micOffImage;
    private Image cameraOnImage;
    private Image cameraOffImage;
    private Image screenShareImage;
    private Image screenShareActiveImage;

    public void setClient(MeetClient client) {
        this.client = client;
    }

    public void setReunion(Reunion reunion) {
        this.reunion = reunion;
    }

    public void initialiserReunion() {
        // Charger les images
        loadImages();

        // Configurer les informations de la réunion
        meetingTitleLabel.setText(reunion.getTitre());
        meetingTimeLabel.setText(reunion.getDateDebut().format(formatter) + " - " + reunion.getDateFin().format(formatter));

        // Configurer la liste des participants
        participantsListView.setItems(participants);
        participantsListView.setCellFactory(lv -> new ParticipantCell());

        // Initialiser les participants
        participants.addAll(reunion.getParticipants());
        mettreAJourCompteurParticipants();

        // Configurer les handlers de messages
        client.enregistrerHandlerMessageTexte(this::handleTextMessage);
        client.enregistrerHandlerNotification(this::handleNotification);
        client.enregistrerHandlerVideoRecu(this::handleVideoFrame);

        // Démarrer la vidéo
        client.startVideoStreaming(reunion);

        // Ajouter notre propre flux vidéo
        ajouterVideoUtilisateur(client.getUtilisateur().getLogin(), client.getVideoStream());

        // Afficher l'overlay d'attente si nous sommes seuls
        if (participants.size() <= 1) {
            waitingOverlay.setVisible(true);
        } else {
            waitingOverlay.setVisible(false);
        }
    }

    private void loadImages() {
        try {
            micOnImage = new Image(getClass().getResourceAsStream("images/mic_on_icon.png"));
            micOffImage = new Image(getClass().getResourceAsStream("images/mic_off_icon.png"));
            cameraOnImage = new Image(getClass().getResourceAsStream("images/camera_on_icon.png"));
            cameraOffImage = new Image(getClass().getResourceAsStream("images/camera_off_icon.png"));
            screenShareImage = new Image(getClass().getResourceAsStream("images/screen_share_icon.png"));
            screenShareActiveImage = new Image(getClass().getResourceAsStream("images/screen_share_active_icon.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }
    }

    private void mettreAJourCompteurParticipants() {
        participantCountLabel.setText("(" + participants.size() + ")");
    }

    private void ajouterVideoUtilisateur(String login, VideoStream videoStream) {
        // Calculer la position dans la grille
        int participantCount = participantVideos.size();
        int col = participantCount % 3;
        int row = participantCount / 3;

        // Créer l'ImageView qui affichera le flux vidéo
        ImageView videoImageView = new ImageView();
        videoImageView.setFitWidth(300);
        videoImageView.setFitHeight(200);
        videoImageView.setPreserveRatio(true);

        // Lier l'ImageView au flux vidéo
        videoImageView.imageProperty().bind(videoStream.imageProperty());

        // Créer un conteneur avec le nom de l'utilisateur
        StackPane videoContainer = new StackPane();
        videoContainer.getStyleClass().add("video-cell");

        Label nameLabel = new Label(login);
        nameLabel.getStyleClass().add("video-username");
        StackPane.setAlignment(nameLabel, Pos.BOTTOM_LEFT);

        videoContainer.getChildren().addAll(videoImageView, nameLabel);

        // Ajouter à la grille
        Platform.runLater(() -> {
            videoGrid.add(videoContainer, col, row);
            participantVideos.put(login, videoImageView);
        });
    }

    private void handleTextMessage(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        int idReunion = (int) data.get("idReunion");

        if (reunion.getId() == idReunion) {
            String texte = (String) data.get("texte");
            String expediteur = message.getExpediteur();

            Platform.runLater(() -> {
                chatArea.appendText("[" + expediteur + "]: " + texte + "\n");
            });
        }
    }

    private void handleNotification(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        String messageText = (String) data.get("message");

        Platform.runLater(() -> {
            chatArea.appendText("[SYSTÈME]: " + messageText + "\n");

            // Actualiser la liste des participants si nécessaire
            if (messageText.contains("a rejoint") || messageText.contains("a quitté")) {
                participants.clear();
                participants.addAll(reunion.getParticipants());
                mettreAJourCompteurParticipants();

                // Masquer l'overlay d'attente si nous avons plus d'un participant
                waitingOverlay.setVisible(participants.size() <= 1);
            }
        });
    }

    private void handleVideoFrame(Message message) {
        String expediteur = message.getExpediteur();

        // Ne pas traiter notre propre vidéo
        if (expediteur.equals(client.getLogin())) return;

        // Ajouter le flux vidéo s'il n'existe pas encore
        if (!participantVideos.containsKey(expediteur)) {
            VideoStream participantStream = client.getParticipantStream(expediteur);
            if (participantStream != null) {
                ajouterVideoUtilisateur(expediteur, participantStream);
            }
        }

        // La mise à jour de l'image est gérée par le MeetClient
    }

    @FXML
    protected void handleToggleMic() {
        client.getVideoStream().toggleMic();

        if (client.getVideoStream().isMicActive()) {
            micIcon.setImage(micOnImage);
            micButton.getStyleClass().remove("control-button-disabled");
        } else {
            micIcon.setImage(micOffImage);
            micButton.getStyleClass().add("control-button-disabled");
        }
    }

    @FXML
    protected void handleToggleCamera() {
        client.getVideoStream().toggleCamera();

        if (client.getVideoStream().isCameraActive()) {
            cameraIcon.setImage(cameraOnImage);
            cameraButton.getStyleClass().remove("control-button-disabled");
        } else {
            cameraIcon.setImage(cameraOffImage);
            cameraButton.getStyleClass().add("control-button-disabled");
        }
    }

    @FXML
    protected void handleToggleScreenShare() {
        client.getVideoStream().toggleScreenSharing();

        if (client.getVideoStream().isScreenSharing()) {
            screenShareIcon.setImage(screenShareActiveImage);
            screenShareButton.getStyleClass().add("control-button-disabled");

            // Désactiver la caméra pendant le partage d'écran
            if (client.getVideoStream().isCameraActive()) {
                handleToggleCamera();
            }
        } else {
            screenShareIcon.setImage(screenShareImage);
            screenShareButton.getStyleClass().remove("control-button-disabled");
        }
    }

    @FXML
    protected void handleToggleChat() {
        boolean visible = sidePanel.isVisible();
        sidePanel.setVisible(!visible);

        // Ajuster la taille de la grille vidéo
        if (visible) {
            sidePanel.setMaxWidth(0);
            sidePanel.setPrefWidth(0);
        } else {
            sidePanel.setMaxWidth(300);
            sidePanel.setPrefWidth(300);
        }
    }

    @FXML
    protected void handleToggleParticipants() {
        // Implémenter si nécessaire pour basculer entre les onglets
    }

    @FXML
    protected void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.envoyerMessageTexte(reunion.getId(), message);
            messageField.clear();
        }
    }

    @FXML
    protected void handleLeaveMeeting() {
        // Arrêter la vidéo
        client.stopVideoStreaming();

        // Quitter la réunion
        Map<String, Object> data = new HashMap<>();
        data.put("idReunion", reunion.getId());

        Message message = new Message(
                bahou.akandan.kassy.bmot.communication.MessageType.QUITTER_REUNION,
                data,
                client.getLogin()
        );

        client.envoyerMessage(message);

        // Retourner à l'écran d'accueil
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent homeView = loader.load();
            HomeController controller = loader.getController();
            controller.setClient(client);
            controller.initialiserDonnees();

            Scene currentScene = meetingTitleLabel.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene homeScene = new Scene(homeView, 1024, 768);
            stage.setScene(homeScene);
            stage.setTitle("BMO Meet - Accueil");
            stage.setFullScreen(false);

        } catch (IOException e) {
            showErrorMessage("Erreur lors du retour à l'écran d'accueil: " + e.getMessage());
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Cellule personnalisée pour les participants
    private class ParticipantCell extends ListCell<Utilisateur> {
        @Override
        protected void updateItem(Utilisateur utilisateur, boolean empty) {
            super.updateItem(utilisateur, empty);

            if (empty || utilisateur == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Afficher le nom et prénom de l'utilisateur
                setText(utilisateur.getPrenom() + " " + utilisateur.getNom());

                // Ajouter un indicateur si c'est l'organisateur
                if (utilisateur.equals(reunion.getOrganisateur())) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        }
    }
}