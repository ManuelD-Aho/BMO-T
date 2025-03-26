package bahou.akandan.kassy.bmot.serveurs;

import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import bahou.akandan.kassy.bmot.utils.Configuration;
import bahou.akandan.kassy.bmot.utils.ThreadManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Serveur {
    private int port;
    private ServerSocket serverSocket;
    private boolean enExecution;

    // Structure de données pour stocker les utilisateurs et les réunions
    private Map<String, Utilisateur> utilisateurs;
    private Map<Integer, Reunion> reunions;
    private Map<String, GestionnaireClient> clientsConnectes;

    public Serveur() {
        this.port = Configuration.getPort();
        this.utilisateurs = new HashMap<>();
        this.reunions = new HashMap<>();
        this.clientsConnectes = new ConcurrentHashMap<>();

        // Création d'utilisateurs initiaux pour les tests
        creerUtilisateurTest("admin", "admin", "Admin", "Système", true);
        creerUtilisateurTest("user1", "pass1", "Jean", "Dupont", false);
        creerUtilisateurTest("user2", "pass2", "Marie", "Martin", false);
    }

    private void creerUtilisateurTest(String login, String motDePasse, String nom, String prenom, boolean estAdmin) {
        Utilisateur utilisateur = new Utilisateur(login, motDePasse, nom, prenom);
        if (estAdmin) {
            utilisateur.setEstAdministrateur(true);
        }
        utilisateurs.put(login, utilisateur);
    }

    public void demarrer() {
        try {
            serverSocket = new ServerSocket(port);
            enExecution = true;
            System.out.println("Serveur démarré sur le port " + port);

            ThreadManager.executer(() -> {
                while (enExecution) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("Nouvelle connexion acceptée: " + socket.getInetAddress());

                        // Création d'un gestionnaire de client dans un thread séparé
                        GestionnaireClient gestionnaire = new GestionnaireClient(this, socket);
                        ThreadManager.executer(gestionnaire);
                    } catch (IOException e) {
                        if (enExecution) {
                            System.err.println("Erreur lors de l'acceptation de la connexion: " + e.getMessage());
                        }
                    }
                }
            });

        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
        }
    }

    public void arreter() {
        enExecution = false;

        // Fermer toutes les connexions clients
        for (GestionnaireClient client : clientsConnectes.values()) {
            client.fermer();
        }

        // Fermer le socket serveur
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture du serveur: " + e.getMessage());
        }

        // Arrêter le gestionnaire de threads
        ThreadManager.arreter();

        System.out.println("Serveur arrêté.");
    }

    // Méthodes de gestion des utilisateurs
    public synchronized boolean authentifierUtilisateur(String login, String motDePasse) {
        Utilisateur utilisateur = utilisateurs.get(login);
        return utilisateur != null && utilisateur.verifierMotDePasse(motDePasse);
    }

    public synchronized Utilisateur getUtilisateur(String login) {
        return utilisateurs.get(login);
    }

    public synchronized boolean creerUtilisateur(Utilisateur utilisateur) {
        if (!utilisateurs.containsKey(utilisateur.getLogin())) {
            utilisateurs.put(utilisateur.getLogin(), utilisateur);
            return true;
        }
        return false;
    }

    public synchronized List<Utilisateur> getUtilisateurs() {
        return new ArrayList<>(utilisateurs.values());
    }

    // Méthodes de gestion des réunions
    public synchronized Reunion creerReunion(Reunion reunion) {
        reunions.put(reunion.getId(), reunion);
        return reunion;
    }

    public synchronized Reunion getReunion(int id) {
        return reunions.get(id);
    }

    public synchronized List<Reunion> getReunions() {
        return new ArrayList<>(reunions.values());
    }

    public synchronized void supprimerReunion(int id) {
        reunions.remove(id);
    }

    // Méthodes de gestion des clients connectés
    public void enregistrerClient(String login, GestionnaireClient gestionnaire) {
        clientsConnectes.put(login, gestionnaire);
        Utilisateur utilisateur = getUtilisateur(login);
        if (utilisateur != null) {
            utilisateur.setEstConnecte(true);
        }
    }

    public void deconnecterClient(String login) {
        clientsConnectes.remove(login);
        Utilisateur utilisateur = getUtilisateur(login);
        if (utilisateur != null) {
            utilisateur.setEstConnecte(false);
        }
    }

    public GestionnaireClient getClient(String login) {
        return clientsConnectes.get(login);
    }

    public List<GestionnaireClient> getClientsConnectes() {
        return new ArrayList<>(clientsConnectes.values());
    }

    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        serveur.demarrer();

        // Arrêt du serveur à la fermeture de l'application
        Runtime.getRuntime().addShutdownHook(new Thread(serveur::arreter));
    }
}