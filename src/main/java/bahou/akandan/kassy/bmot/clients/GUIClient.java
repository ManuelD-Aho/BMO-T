package bahou.akandan.kassy.bmot.clients;

import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.communication.MessageType;
import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class GUIClient extends Application {
    private Client client;
    private Stage primaryStage;

    // Composants UI
    private TextArea chatArea;
    private TextField messageField;
    private ListView<Reunion> reunionListView;
    private ListView<Utilisateur> participantsListView;
    private ComboBox<Reunion.TypeReunion> typeReunionComboBox;

    // Réunion active
    private Reunion reunionActive;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("BMO Client");

        // Initialisation du client
        client = new Client("localhost", 8888);

        // Affichage de l'écran de connexion
        afficherEcranConnexion();
    }

    private void afficherEcranConnexion() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        Label loginLabel = new Label("Login:");
        TextField loginField = new TextField();

        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Se connecter");
        Button registerButton = new Button("S'inscrire");

        grid.add(loginLabel, 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 0, 2);
        grid.add(registerButton, 1, 2);

        loginButton.setOnAction(e -> {
            try {
                client.connecter();
                client.authentifier(loginField.getText(), passwordField.getText(), this::handleAuthentification);
            } catch (IOException ex) {
                afficherErreur("Erreur de connexion", "Impossible de se connecter au serveur: " + ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> afficherEcranInscription());

        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @SuppressWarnings("unchecked")
    private void handleAuthentification(Message message) {
        Platform.runLater(() -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");
            String messageText = (String) data.get("message");

            if (succes) {
                Utilisateur utilisateur = (Utilisateur) data.get("utilisateur");
                client.setUtilisateur(utilisateur);
                afficherEcranPrincipal();
            } else {
                afficherErreur("Erreur d'authentification", messageText);
            }
        });
    }

    private void afficherEcranInscription() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        Label loginLabel = new Label("Login:");
        TextField loginField = new TextField();

        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();

        Label nomLabel = new Label("Nom:");
        TextField nomField = new TextField();

        Label prenomLabel = new Label("Prénom:");
        TextField prenomField = new TextField();

        Button registerButton = new Button("S'inscrire");
        Button backButton = new Button("Retour");

        grid.add(loginLabel, 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(nomLabel, 0, 2);
        grid.add(nomField, 1, 2);
        grid.add(prenomLabel, 0, 3);
        grid.add(prenomField, 1, 3);
        grid.add(registerButton, 0, 4);
        grid.add(backButton, 1, 4);

        registerButton.setOnAction(e -> {
            try {
                if (!client.estConnecte()) {
                    client.connecter();
                }

                client.creerUtilisateur(
                        loginField.getText(),
                        passwordField.getText(),
                        nomField.getText(),
                        prenomField.getText(),
                        this::handleInscription
                );
            } catch (IOException ex) {
                afficherErreur("Erreur de connexion", "Impossible de se connecter au serveur: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> afficherEcranConnexion());

        Scene scene = new Scene(grid, 350, 200);
        primaryStage.setScene(scene);
    }

    @SuppressWarnings("unchecked")
    private void handleInscription(Message message) {
        Platform.runLater(() -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");
            String messageText = (String) data.get("message");

            if (succes) {
                afficherInfo("Inscription réussie", "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
                afficherEcranConnexion();
            } else {
                afficherErreur("Erreur d'inscription", messageText);
            }
        });
    }

    private void afficherEcranPrincipal() {
        BorderPane mainPane = new BorderPane();

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("Fichier");
        MenuItem refreshMenuItem = new MenuItem("Actualiser");
        MenuItem logoutMenuItem = new MenuItem("Déconnexion");
        fileMenu.getItems().addAll(refreshMenuItem, new SeparatorMenuItem(), logoutMenuItem);

        Menu reunionMenu = new Menu("Réunion");
        MenuItem createReunionMenuItem = new MenuItem("Créer une réunion");
        MenuItem joinReunionMenuItem = new MenuItem("Rejoindre une réunion");
        reunionMenu.getItems().addAll(createReunionMenuItem, joinReunionMenuItem);

        menuBar.getMenus().addAll(fileMenu, reunionMenu);
        mainPane.setTop(menuBar);

        // Liste des réunions
        reunionListView = new ListView<>();
        reunionListView.setPrefWidth(200);

        // Liste des participants
        participantsListView = new ListView<>();
        participantsListView.setPrefWidth(200);

        // Zone de chat
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        messageField = new TextField();
        Button sendButton = new Button("Envoyer");
        Button requestSpeakButton = new Button("Demander la parole");

        // Rassembler les composants
        BorderPane chatPane = new BorderPane();
        chatPane.setCenter(chatArea);

        BorderPane messagePane = new BorderPane();
        messagePane.setCenter(messageField);
        messagePane.setRight(sendButton);
        chatPane.setBottom(messagePane);

        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        Label participantsLabel = new Label("Participants:");
        rightPane.getChildren().addAll(participantsLabel, participantsListView, requestSpeakButton);

        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        Label reunionsLabel = new Label("Réunions:");
        leftPane.getChildren().addAll(reunionsLabel, reunionListView);

        mainPane.setLeft(leftPane);
        mainPane.setCenter(chatPane);
        mainPane.setRight(rightPane);

        // Configuration des événements
        refreshMenuItem.setOnAction(e -> actualiserListeReunions());
        logoutMenuItem.setOnAction(e -> {
            client.deconnecter();
            afficherEcranConnexion();
        });

        createReunionMenuItem.setOnAction(e -> afficherDialogCreationReunion());
        joinReunionMenuItem.setOnAction(e -> {
            Reunion selectedReunion = reunionListView.getSelectionModel().getSelectedItem();
            if (selectedReunion != null) {
                rejoindreReunion(selectedReunion);
            } else {
                afficherErreur("Erreur", "Veuillez sélectionner une réunion à rejoindre");
            }
        });

        reunionListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        afficherDetailsReunion(newSelection);
                    }
                }
        );

        sendButton.setOnAction(e -> envoyerMessage());
        messageField.setOnAction(e -> envoyerMessage());

        requestSpeakButton.setOnAction(e -> {
            if (reunionActive != null) {
                client.demanderPriseParole(reunionActive.getId(), this::handleDemandePriseParole);
            }
        });

        // Enregistrement des handlers de messages
        client.enregistrerHandlerMessageTexte(this::afficherMessageRecu);
        client.enregistrerHandlerNotification(this::afficherNotification);
        client.enregistrerHandlerDemandePriseParole(this::handleDemandePriseParole);

        // Actualisation initiale
        actualiserListeReunions();

        Scene scene = new Scene(mainPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BMO Client - " + client.getUtilisateur().getPrenom() + " " + client.getUtilisateur().getNom());
    }

    private void actualiserListeReunions() {
        client.demanderListeReunions(reunions -> {
            Platform.runLater(() -> {
                reunionListView.getItems().clear();
                reunionListView.getItems().addAll(reunions);
            });
        });
    }

    private void afficherDialogCreationReunion() {
        Dialog<Reunion> dialog = new Dialog<>();
        dialog.setTitle("Créer une réunion");
        dialog.setHeaderText("Saisissez les détails de la réunion");

        // Boutons
        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Champs de saisie
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titreField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);

        DatePicker dateDebutPicker = new DatePicker();
        TextField heureDebutField = new TextField("14:00");

        DatePicker dateFinPicker = new DatePicker();
        TextField heureFinField = new TextField("16:00");

        typeReunionComboBox = new ComboBox<>();
        typeReunionComboBox.getItems().addAll(Reunion.TypeReunion.values());
        typeReunionComboBox.setValue(Reunion.TypeReunion.STANDARD);

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Date de début:"), 0, 2);
        grid.add(dateDebutPicker, 1, 2);
        grid.add(new Label("Heure de début:"), 0, 3);
        grid.add(heureDebutField, 1, 3);
        grid.add(new Label("Date de fin:"), 0, 4);
        grid.add(dateFinPicker, 1, 4);
        grid.add(new Label("Heure de fin:"), 0, 5);
        grid.add(heureFinField, 1, 5);
        grid.add(new Label("Type:"), 0, 6);
        grid.add(typeReunionComboBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Conversion du résultat en Reunion
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    LocalDateTime dateDebut = LocalDateTime.of(
                            dateDebutPicker.getValue(),
                            LocalDateTime.parse("2000-01-01T" + heureDebutField.getText() + ":00").toLocalTime()
                    );

                    LocalDateTime dateFin = LocalDateTime.of(
                            dateFinPicker.getValue(),
                            LocalDateTime.parse("2000-01-01T" + heureFinField.getText() + ":00").toLocalTime()
                    );

                    return new Reunion(
                            (int) (System.currentTimeMillis() % 10000),
                            titreField.getText(),
                            descriptionArea.getText(),
                            dateDebut,
                            dateFin,
                            typeReunionComboBox.getValue(),
                            client.getUtilisateur()
                    );
                } catch (Exception e) {
                    afficherErreur("Erreur de saisie", "Format de date/heure incorrect");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reunion -> {
            client.creerReunion(reunion, message -> {
                Platform.runLater(this::actualiserListeReunions);
            });
        });
    }

    private void rejoindreReunion(Reunion reunion) {
        client.rejoindreReunion(reunion.getId(), message -> {
            Platform.runLater(() -> {
                Map<String, Object> data = (Map<String, Object>) message.getContenu();
                boolean succes = (boolean) data.get("succes");
                String messageText = (String) data.get("message");

                if (succes) {
                    reunionActive = (Reunion) data.get("reunion");
                    chatArea.clear();
                    chatArea.appendText("Vous avez rejoint la réunion: " + reunionActive.getTitre() + "\n");
                    actualiserParticipants(reunionActive);
                } else {
                    afficherErreur("Erreur", messageText);
                }
            });
        });
    }

    private void afficherDetailsReunion(Reunion reunion) {
        // Affichage des détails de la réunion sélectionnée
        // Cette méthode pourrait être étendue pour afficher plus d'informations
    }

    private void actualiserParticipants(Reunion reunion) {
        if (reunion != null) {
            participantsListView.getItems().clear();
            participantsListView.getItems().addAll(reunion.getParticipants());
        }
    }

    private void envoyerMessage() {
        if (reunionActive != null && !messageField.getText().isEmpty()) {
            String message = messageField.getText();
            client.envoyerMessageTexte(reunionActive.getId(), message);
            messageField.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private void afficherMessageRecu(Message message) {
        Platform.runLater(() -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            int idReunion = (int) data.get("idReunion");

            if (reunionActive != null && reunionActive.getId() == idReunion) {
                String texte = (String) data.get("texte");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horodatage = message.getHorodatage().format(formatter);
                chatArea.appendText("[" + horodatage + "] " + message.getExpediteur() + ": " + texte + "\n");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void afficherNotification(Message message) {
        Platform.runLater(() -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            int idReunion = (int) data.get("idReunion");

            if (reunionActive != null && reunionActive.getId() == idReunion) {
                String texte = (String) data.get("message");
                chatArea.appendText("[SYSTÈME] " + texte + "\n");
                actualiserParticipants(reunionActive);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleDemandePriseParole(Message message) {
        Platform.runLater(() -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.getOrDefault("succes", true);

            if (succes) {
                if (client.getUtilisateur().equals(reunionActive.getOrganisateur())) {
                    // L'organisateur voit la demande de prise de parole
                    Utilisateur demandeur = (Utilisateur) data.get("utilisateur");
                    int idReunion = (int) data.get("idReunion");

                    if (demandeur != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Demande de prise de parole");
                        alert.setHeaderText("Demande de prise de parole");
                        alert.setContentText(demandeur.getPrenom() + " " + demandeur.getNom() +
                                " demande la parole. Accorder?");

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                client.accorderPriseParole(idReunion, demandeur.getLogin());
                            }
                        });
                    }
                } else {
                    // Le demandeur reçoit la confirmation
                    String message1 = (String) data.getOrDefault("message", "Demande de prise de parole envoyée");
                    chatArea.appendText("[SYSTÈME] " + message1 + "\n");
                }
            } else {
                String message1 = (String) data.get("message");
                afficherErreur("Erreur", message1);
            }
        });
    }

    private void afficherInfo(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void afficherErreur(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        if (client != null && client.estConnecte()) {
            client.deconnecter();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}