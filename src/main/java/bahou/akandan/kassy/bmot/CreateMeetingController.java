package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.MeetClient;
import bahou.akandan.kassy.bmot.modele.Reunion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public class CreateMeetingController {
    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField endTimeField;

    @FXML
    private ComboBox<Reunion.TypeReunion> typeComboBox;

    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    private MeetClient client;
    private HomeController homeController;

    @FXML
    public void initialize() {
        // Initialiser le ComboBox avec les types de réunion
        typeComboBox.getItems().addAll(Reunion.TypeReunion.values());
        typeComboBox.setValue(Reunion.TypeReunion.STANDARD);

        // Initialiser les dates par défaut
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        // Heure de début par défaut (heure actuelle arrondie)
        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.of(now.getHour(), 0);
        startTimeField.setText(formatTime(startTime));

        // Heure de fin par défaut (1 heure après)
        LocalTime endTime = startTime.plusHours(1);
        endTimeField.setText(formatTime(endTime));

        // Configurer les boutons
        createButton.setOnAction(e -> handleCreateMeeting());
        cancelButton.setOnAction(e -> closeDialog());
    }

    public void setClient(MeetClient client) {
        this.client = client;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void handleCreateMeeting() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Créer les date/heure de début et de fin
            LocalDateTime startDateTime = createDateTime(startDatePicker.getValue(), startTimeField.getText());
            LocalDateTime endDateTime = createDateTime(endDatePicker.getValue(), endTimeField.getText());

            // Créer un identifiant unique pour la réunion
            int reunionId = (int) (System.currentTimeMillis() % 100000);

            // Créer l'objet réunion
            Reunion reunion = new Reunion(
                    reunionId,
                    titleField.getText(),
                    descriptionArea.getText(),
                    startDateTime,
                    endDateTime,
                    typeComboBox.getValue(),
                    client.getUtilisateur()
            );

            // Envoyer la demande au serveur
            client.creerReunion(reunion, message -> {
                Map<String, Object> data = (Map<String, Object>) message.getContenu();
                boolean succes = (boolean) data.get("succes");

                if (succes) {
                    javafx.application.Platform.runLater(() -> {
                        showSuccessMessage("La réunion a été créée avec succès!");
                        closeDialog();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showErrorMessage("Erreur lors de la création de la réunion");
                    });
                }
            });

        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // Vérifier le titre
        if (titleField.getText().trim().isEmpty()) {
            errorMessage.append("- Veuillez saisir un titre pour la réunion\n");
        }

        // Vérifier les dates
        if (startDatePicker.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une date de début\n");
        }

        if (endDatePicker.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une date de fin\n");
        }

        // Vérifier les heures
        if (!isValidTimeFormat(startTimeField.getText())) {
            errorMessage.append("- Format d'heure de début invalide (utilisez HH:MM)\n");
        }

        if (!isValidTimeFormat(endTimeField.getText())) {
            errorMessage.append("- Format d'heure de fin invalide (utilisez HH:MM)\n");
        }

        // Vérifier la cohérence des dates/heures
        try {
            LocalDateTime startDateTime = createDateTime(startDatePicker.getValue(), startTimeField.getText());
            LocalDateTime endDateTime = createDateTime(endDatePicker.getValue(), endTimeField.getText());

            if (endDateTime.isBefore(startDateTime)) {
                errorMessage.append("- La date/heure de fin doit être après la date/heure de début\n");
            }

            LocalDateTime now = LocalDateTime.now();
            if (startDateTime.isBefore(now)) {
                errorMessage.append("- La date/heure de début doit être dans le futur\n");
            }
        } catch (Exception e) {
            // Les erreurs de format seront déjà capturées par les vérifications précédentes
        }

        // Si des erreurs ont été trouvées, les afficher
        if (errorMessage.length() > 0) {
            showErrorMessage("Veuillez corriger les erreurs suivantes :\n" + errorMessage.toString());
            return false;
        }

        return true;
    }

    private boolean isValidTimeFormat(String time) {
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private LocalDateTime createDateTime(LocalDate date, String timeStr) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    private String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }
}