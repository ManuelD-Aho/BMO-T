package bahou.akandan.kassy.bmot.serveurs;

import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.communication.MessageType;
import bahou.akandan.kassy.bmot.communication.Protocole;
import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

public class GestionnaireClient implements Runnable {
    private Serveur serveur;
    private Socket socket;
    private ObjectInputStream entree;
    private ObjectOutputStream sortie;
    private boolean connecte;
    private String loginUtilisateur;

    public GestionnaireClient(Serveur serveur, Socket socket) {
        this.serveur = serveur;
        this.socket = socket;
        this.connecte = true;
    }

    @Override
    public void run() {
        try {
            // Initialisation des flux
            sortie = new ObjectOutputStream(socket.getOutputStream());
            entree = new ObjectInputStream(socket.getInputStream());

            // Boucle principale de traitement des messages
            while (connecte) {
                try {
                    // Lecture du message
                    Message message = (Message) entree.readObject();
                    traiterMessage(message);
                } catch (SocketTimeoutException e) {
                    // Timeout - envoi d'un ping pour vérifier si le client est toujours connecté
                    envoyerPing();
                } catch (ClassNotFoundException | IOException e) {
                    System.err.println("Erreur de communication avec le client: " + e.getMessage());
                    connecte = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur d'initialisation des flux: " + e.getMessage());
        } finally {
            fermer();
        }
    }

    private void traiterMessage(Message message) {
        if (message == null) return;

        System.out.println("Message reçu: " + message.getType() + " de " + message.getExpediteur());

        switch (message.getType()) {
            case CONNEXION:
                traiterConnexion(message);
                break;

            case DECONNEXION:
                traiterDeconnexion();
                break;

            case CREER_UTILISATEUR:
                traiterCreerUtilisateur(message);
                break;

            case LISTE_UTILISATEURS:
                traiterListeUtilisateurs();
                break;

            case CREER_REUNION:
                traiterCreerReunion(message);
                break;

            case LISTE_REUNIONS:
                traiterListeReunions();
                break;

            case REJOINDRE_REUNION:
                traiterRejoindreReunion(message);
                break;

            case MESSAGE_TEXTE:
                traiterMessageTexte(message);
                break;

            case DEMANDE_PRISE_PAROLE:
                traiterDemandePriseParole(message);
                break;

            case ACCORDER_PRISE_PAROLE:
                traiterAccorderPriseParole(message);
                break;

            case PING:
                envoyerMessage(new Message(MessageType.PONG, null, "SERVEUR"));
                break;

            default:
                envoyerMessage(Protocole.creerMessageErreur("Type de message non pris en charge"));
        }
    }

    @SuppressWarnings("unchecked")
    private void traiterConnexion(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        String login = (String) data.get(Protocole.CLE_LOGIN);
        String motDePasse = (String) data.get(Protocole.CLE_MOT_DE_PASSE);

        boolean authentifie = serveur.authentifierUtilisateur(login, motDePasse);

        if (authentifie) {
            this.loginUtilisateur = login;
            serveur.enregistrerClient(login, this);
            Utilisateur utilisateur = serveur.getUtilisateur(login);
            envoyerMessage(Protocole.creerMessageConnexionReponse(true, utilisateur, "Connexion réussie"));
        } else {
            envoyerMessage(Protocole.creerMessageConnexionReponse(false, null, "Login ou mot de passe incorrect"));
        }
    }

    private void traiterDeconnexion() {
        if (loginUtilisateur != null) {
            serveur.deconnecterClient(loginUtilisateur);
        }
        connecte = false;
    }

    @SuppressWarnings("unchecked")
    private void traiterCreerUtilisateur(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        String login = (String) data.get(Protocole.CLE_LOGIN);
        String motDePasse = (String) data.get(Protocole.CLE_MOT_DE_PASSE);
        String nom = (String) data.get(Protocole.CLE_NOM);
        String prenom = (String) data.get(Protocole.CLE_PRENOM);

        Utilisateur nouvelUtilisateur = new Utilisateur(login, motDePasse, nom, prenom);
        boolean succes = serveur.creerUtilisateur(nouvelUtilisateur);

        Map<String, Object> reponse = Map.of(
                Protocole.CLE_SUCCES, succes,
                Protocole.CLE_MESSAGE, succes ? "Utilisateur créé avec succès" : "Login déjà utilisé"
        );

        envoyerMessage(new Message(MessageType.CREER_UTILISATEUR_REPONSE, reponse, "SERVEUR"));
    }

    private void traiterListeUtilisateurs() {
        envoyerMessage(new Message(MessageType.LISTE_UTILISATEURS_REPONSE, serveur.getUtilisateurs(), "SERVEUR"));
    }

    private void traiterCreerReunion(Message message) {
        Reunion reunion = (Reunion) message.getContenu();
        Reunion creee = serveur.creerReunion(reunion);

        Map<String, Object> reponse = Map.of(
                Protocole.CLE_SUCCES, true,
                "reunion", creee
        );

        envoyerMessage(new Message(MessageType.CREER_REUNION_REPONSE, reponse, "SERVEUR"));
    }

    private void traiterListeReunions() {
        envoyerMessage(new Message(MessageType.LISTE_REUNIONS_REPONSE, serveur.getReunions(), "SERVEUR"));
    }

    @SuppressWarnings("unchecked")
    private void traiterRejoindreReunion(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        int idReunion = (int) data.get(Protocole.CLE_ID_REUNION);

        Reunion reunion = serveur.getReunion(idReunion);
        boolean succes = false;
        String messageReponse = "Réunion introuvable";

        if (reunion != null) {
            Utilisateur utilisateur = serveur.getUtilisateur(loginUtilisateur);
            if (utilisateur != null) {
                reunion.ajouterParticipant(utilisateur);
                succes = true;
                messageReponse = "Vous avez rejoint la réunion";

                // Notifier les autres participants
                for (Utilisateur participant : reunion.getParticipants()) {
                    if (!participant.getLogin().equals(loginUtilisateur)) {
                        GestionnaireClient client = serveur.getClient(participant.getLogin());
                        if (client != null) {
                            Map<String, Object> notification = Map.of(
                                    "message", utilisateur.getPrenom() + " " + utilisateur.getNom() + " a rejoint la réunion",
                                    Protocole.CLE_ID_REUNION, idReunion
                            );
                            client.envoyerMessage(new Message(MessageType.NOTIFIER_PARTICIPANTS, notification, "SERVEUR"));
                        }
                    }
                }
            } else {
                messageReponse = "Utilisateur non trouvé";
            }
        }

        Map<String, Object> reponse = Map.of(
                Protocole.CLE_SUCCES, succes,
                Protocole.CLE_MESSAGE, messageReponse,
                "reunion", succes ? reunion : null
        );

        envoyerMessage(new Message(MessageType.REJOINDRE_REUNION_REPONSE, reponse, "SERVEUR"));
    }

    @SuppressWarnings("unchecked")
    private void traiterMessageTexte(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        int idReunion = (int) data.get(Protocole.CLE_ID_REUNION);
        String texte = (String) data.get(Protocole.CLE_TEXTE);

        Reunion reunion = serveur.getReunion(idReunion);
        if (reunion != null) {
            // Vérifier si l'utilisateur peut parler (orateur ou standard)
            Utilisateur utilisateur = serveur.getUtilisateur(loginUtilisateur);
            boolean peutParler = reunion.getOrateur() == null ||
                    reunion.getOrateur().equals(utilisateur) ||
                    reunion.getType() != Reunion.TypeReunion.DEMOCRATIQUE;

            if (peutParler) {
                // Distribuer le message à tous les participants
                for (Utilisateur participant : reunion.getParticipants()) {
                    GestionnaireClient client = serveur.getClient(participant.getLogin());
                    if (client != null) {
                        client.envoyerMessage(message);
                    }
                }
            } else {
                envoyerMessage(Protocole.creerMessageErreur("Vous n'avez pas la parole dans cette réunion"));
            }
        } else {
            envoyerMessage(Protocole.creerMessageErreur("Réunion introuvable"));
        }
    }

    @SuppressWarnings("unchecked")
    private void traiterDemandePriseParole(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        int idReunion = (int) data.get(Protocole.CLE_ID_REUNION);

        Reunion reunion = serveur.getReunion(idReunion);
        boolean succes = false;

        if (reunion != null && reunion.getType() == Reunion.TypeReunion.DEMOCRATIQUE) {
            Utilisateur utilisateur = serveur.getUtilisateur(loginUtilisateur);
            succes = reunion.demanderPriseParole(utilisateur);

            // Notifier l'organisateur
            GestionnaireClient organisateur = serveur.getClient(reunion.getOrganisateur().getLogin());
            if (organisateur != null) {
                Map<String, Object> notification = Map.of(
                        "utilisateur", utilisateur,
                        Protocole.CLE_ID_REUNION, idReunion
                );
                organisateur.envoyerMessage(new Message(MessageType.DEMANDE_PRISE_PAROLE, notification, loginUtilisateur));
            }
        }

        Map<String, Object> reponse = Map.of(
                Protocole.CLE_SUCCES, succes,
                Protocole.CLE_MESSAGE, succes ? "Demande de prise de parole envoyée" : "Impossible de demander la prise de parole"
        );

        envoyerMessage(new Message(MessageType.DEMANDE_PRISE_PAROLE, reponse, "SERVEUR"));
    }

    @SuppressWarnings("unchecked")
    private void traiterAccorderPriseParole(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        int idReunion = (int) data.get(Protocole.CLE_ID_REUNION);
        String loginOrateur = (String) data.get("loginOrateur");

        Reunion reunion = serveur.getReunion(idReunion);
        boolean succes = false;

        if (reunion != null) {
            // Vérifier si l'utilisateur actuel est l'organisateur
            if (loginUtilisateur.equals(reunion.getOrganisateur().getLogin())) {
                Utilisateur orateur = serveur.getUtilisateur(loginOrateur);
                if (orateur != null) {
                    reunion.accorderPriseParole(orateur);
                    succes = true;

                    // Notifier tous les participants
                    for (Utilisateur participant : reunion.getParticipants()) {
                        GestionnaireClient client = serveur.getClient(participant.getLogin());
                        if (client != null) {
                            Map<String, Object> notification = Map.of(
                                    "orateur", orateur,
                                    Protocole.CLE_ID_REUNION, idReunion,
                                    Protocole.CLE_MESSAGE, orateur.getPrenom() + " " + orateur.getNom() + " a obtenu la parole"
                            );
                            client.envoyerMessage(new Message(MessageType.NOTIFIER_PARTICIPANTS, notification, "SERVEUR"));
                        }
                    }
                }
            }
        }

        if (!succes) {
            envoyerMessage(Protocole.creerMessageErreur("Impossible d'accorder la prise de parole"));
        }
    }

    public void envoyerMessage(Message message) {
        try {
            sortie.writeObject(message);
            sortie.flush();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
            connecte = false;
        }
    }

    private void envoyerPing() {
        envoyerMessage(new Message(MessageType.PING, null, "SERVEUR"));
    }

    public void fermer() {
        if (loginUtilisateur != null) {
            serveur.deconnecterClient(loginUtilisateur);
        }

        try {
            connecte = false;
            if (sortie != null) sortie.close();
            if (entree != null) entree.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}