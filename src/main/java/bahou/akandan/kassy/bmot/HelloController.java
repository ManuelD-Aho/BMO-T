package bahou.akandan.kassy.bmot;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class HelloController {
    @FXML private ImageView mainVideoStream;
    @FXML private ImageView userAvatar;

    @FXML private Button micToggle;
    @FXML private Button cameraToggle;
    @FXML private Button shareScreenBtn;
    @FXML private Button endCallBtn;

    @FXML private ListView<String> participantsList;
    @FXML private ListView<String> chatMessages;
    @FXML private TextField chatInput;

    @FXML
    private void initialize() {
        // Configuration initiale
        setupParticipantsList();
        setupChatControls();
        setupMeetingControls();
    }

    private void setupParticipantsList() {
        // Ajouter des participants exemple
        participantsList.getItems().addAll(
                "Vous",
                "Alice Dupont",
                "Bernard Martin",
                "Sophie Lefebvre"
        );
    }

    private void setupChatControls() {
        // Ajouter des messages de chat exemples
        chatMessages.getItems().addAll(
                "Alice: Bonjour tout le monde !",
                "Vous: Salut, prêts pour la réunion ?"
        );

        // Gérer l'envoi de messages
        chatInput.setOnAction(event -> {
            String message = chatInput.getText();
            if (!message.isEmpty()) {
                chatMessages.getItems().add("Vous: " + message);
                chatInput.clear();
            }
        });
    }

    private void setupMeetingControls() {
        // Gérer les contrôles de la réunion
        micToggle.setOnAction(event -> toggleMicrophone());
        cameraToggle.setOnAction(event -> toggleCamera());
        shareScreenBtn.setOnAction(event -> shareScreen());
        endCallBtn.setOnAction(event -> endMeeting());
    }

    private void toggleMicrophone() {
        // Logique de activation/désactivation du microphone
        micToggle.getStyleClass().toggle("muted");
    }

    private void toggleCamera() {
        // Logique de activation/désactivation de la caméra
        cameraToggle.getStyleClass().toggle("camera-off");
    }

    private void shareScreen() {
        // Logique de partage d'écran
        System.out.println("Partage d'écran activé");
    }

    private void endMeeting() {
        // Logique de fin de réunion
        System.out.println("Fin de la réunion");
    }
}