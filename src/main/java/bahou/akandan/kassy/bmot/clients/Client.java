package bahou.akandan.kassy.bmot.clients;

import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.communication.MessageType;
import bahou.akandan.kassy.bmot.communication.Protocole;
import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import bahou.akandan.kassy.bmot.utils.ThreadManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream sortie;
    private ObjectInputStream entree;
    private boolean connecte;
    private String login;
    private Utilisateur utilisateur;

    // Callbacks pour les différents types de messages
    private Map<MessageType, Consumer<Message>> handlers;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.connecte = false;
        this.handlers = new HashMap<>();

        // Enregistrement des handlers par défaut
        enregistrerHandler(MessageType.PING, this::handlePing);
    }

    public void connecter() throws IOException {
        socket = new Socket(host, port);
        sortie = new ObjectOutputStream(socket.getOutputStream());
        entree = new ObjectInputStream(socket.getInputStream());
        connecte = true;

        // Démarrage du thread de lecture
        ThreadManager.executer(this::boucleReception);

        System.out.println("Client connecté à " + host + ":" + port);
    }

    private void boucleReception() {
        while (connecte) {
            try {
                Message message = (Message) entree.readObject();
                traiterMessage(message);
            } catch (ClassNotFoundException | IOException e) {
                if (connecte) {
                    System.err.println("Erreur de réception: " + e.getMessage());
                    deconnecter();
                }
                break;
            }
        }
    }

    private void traiterMessage(Message message) {
        System.out.println("Message reçu: " + message.getType() + " de " + message.getExpediteur());

        // Appel du handler correspondant au type de message
        Consumer<Message> handler = handlers.get(message.getType());
        if (handler != null) {
            handler.accept(message);
        }
    }

    public void authentifier(String login, String motDePasse, Consumer<Message> callback) {
        this.login = login;
        Message message = Protocole.creerMessageConnexion(login, motDePasse);
        enregistrerHandler(MessageType.CONNEXION_REPONSE, callback);
        envoyerMessage(message);
    }

    public void deconnecter() {
        if (connecte) {
            try {
                if (login != null) {
                    envoyerMessage(new Message(MessageType.DECONNEXION, null, login));
                }

                connecte = false;
                if (sortie != null) sortie.close();
                if (entree != null) entree.close();
                if (socket != null && !socket.isClosed()) socket.close();

                System.out.println("Client déconnecté");
            } catch (IOException e) {
                System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
            }
        }
    }

    public void creerUtilisateur(String login, String motDePasse, String nom, String prenom, Consumer<Message> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put(Protocole.CLE_LOGIN, login);
        data.put(Protocole.CLE_MOT_DE_PASSE, motDePasse);
        data.put(Protocole.CLE_NOM, nom);
        data.put(Protocole.CLE_PRENOM, prenom);

        Message message = new Message(MessageType.CREER_UTILISATEUR, data, this.login != null ? this.login : "INVITE");
        enregistrerHandler(MessageType.CREER_UTILISATEUR_REPONSE, callback);
        envoyerMessage(message);
    }

    public void demanderListeUtilisateurs(Consumer<List<Utilisateur>> callback) {
        enregistrerHandler(MessageType.LISTE_UTILISATEURS_REPONSE, message -> {
            @SuppressWarnings("unchecked")
            List<Utilisateur> utilisateurs = (List<Utilisateur>) message.getContenu();
            callback.accept(utilisateurs);
        });

        envoyerMessage(new Message(MessageType.LISTE_UTILISATEURS, null, login));
    }

    public void creerReunion(Reunion reunion, Consumer<Message> callback) {
        enregistrerHandler(MessageType.CREER_REUNION_REPONSE, callback);
        envoyerMessage(Protocole.creerMessageCreerReunion(reunion));
    }

    public void demanderListeReunions(Consumer<List<Reunion>> callback) {
        enregistrerHandler(MessageType.LISTE_REUNIONS_REPONSE, message -> {
            @SuppressWarnings("unchecked")
            List<Reunion> reunions = (List<Reunion>) message.getContenu();
            callback.accept(reunions);
        });

        envoyerMessage(new Message(MessageType.LISTE_REUNIONS, null, login));
    }

    public void rejoindreReunion(int idReunion, Consumer<Message> callback) {
        enregistrerHandler(MessageType.REJOINDRE_REUNION_REPONSE, callback);
        envoyerMessage(Protocole.creerMessageRejoindreReunion(idReunion, login));
    }

    public void envoyerMessageTexte(int idReunion, String texte) {
        envoyerMessage(Protocole.creerMessageTexte(idReunion, texte, login));
    }

    public void demanderPriseParole(int idReunion, Consumer<Message> callback) {
        enregistrerHandler(MessageType.DEMANDE_PRISE_PAROLE, callback);
        envoyerMessage(Protocole.creerMessageDemandePriseParole(idReunion, login));
    }

    public void accorderPriseParole(int idReunion, String loginOrateur) {
        Map<String, Object> data = new HashMap<>();
        data.put(Protocole.CLE_ID_REUNION, idReunion);
        data.put("loginOrateur", loginOrateur);

        envoyerMessage(new Message(MessageType.ACCORDER_PRISE_PAROLE, data, login));
    }

    public void enregistrerHandlerMessageTexte(Consumer<Message> callback) {
        enregistrerHandler(MessageType.MESSAGE_TEXTE, callback);
    }

    public void enregistrerHandlerNotification(Consumer<Message> callback) {
        enregistrerHandler(MessageType.NOTIFIER_PARTICIPANTS, callback);
    }

    public void enregistrerHandlerDemandePriseParole(Consumer<Message> callback) {
        enregistrerHandler(MessageType.DEMANDE_PRISE_PAROLE, callback);
    }

    public void enregistrerHandler(MessageType type, Consumer<Message> handler) {
        handlers.put(type, handler);
    }

    private void handlePing(Message message) {
        envoyerMessage(new Message(MessageType.PONG, null, login != null ? login : "INVITE"));
    }

    public void envoyerMessage(Message message) {
        if (connecte) {
            try {
                sortie.writeObject(message);
                sortie.flush();
            } catch (IOException e) {
                System.err.println("Erreur d'envoi: " + e.getMessage());
                deconnecter();
            }
        } else {
            System.err.println("Client non connecté");
        }
    }

    public boolean estConnecte() {
        return connecte;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getLogin() {
        return login;
    }
}