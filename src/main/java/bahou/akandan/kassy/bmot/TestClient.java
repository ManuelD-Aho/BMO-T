package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.clients.Client;
import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.communication.MessageType;
import bahou.akandan.kassy.bmot.communication.Protocole;
import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import bahou.akandan.kassy.bmot.utils.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Client de test en ligne de commande pour l'application BMO
 */
public class TestClient {
    private static Client client;
    private static Scanner scanner = new Scanner(System.in);
    private static Reunion reunionActive = null;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static boolean continuerBoucleChat = false;

    public static void main(String[] args) {
        System.out.println("BMO Test Client");
        System.out.println("===============");

        String host = "localhost";
        int port = Configuration.getPort();

        // Possibilité de spécifier host:port en paramètre
        if (args.length > 0) {
            String[] hostPort = args[0].split(":");
            host = hostPort[0];
            if (hostPort.length > 1) {
                port = Integer.parseInt(hostPort[1]);
            }
        }

        client = new Client(host, port);

        try {
            client.connecter();
            menuPrincipal();
        } catch (IOException e) {
            System.err.println("Erreur de connexion au serveur: " + e.getMessage());
        } finally {
            if (client.estConnecte()) {
                client.deconnecter();
            }
        }
    }

    private static void menuPrincipal() {
        boolean quitter = false;

        while (!quitter) {
            System.out.println("\nMenu Principal");
            System.out.println("1. Connexion");
            System.out.println("2. Créer un compte");
            System.out.println("3. Quitter");
            System.out.print("Choix: ");

            int choix = lireEntier();

            switch (choix) {
                case 1:
                    connexion();
                    break;
                case 2:
                    creerCompte();
                    break;
                case 3:
                    quitter = true;
                    break;
                default:
                    System.out.println("Choix invalide!");
            }
        }
    }

    private static void connexion() {
        System.out.print("Login: ");
        String login = scanner.nextLine();

        System.out.print("Mot de passe: ");
        String motDePasse = scanner.nextLine();

        System.out.println("Connexion en cours...");

        // Utilisation de CountDownLatch pour bloquer jusqu'à la réponse
        CountDownLatch authLatch = new CountDownLatch(1);
        final boolean[] authSuccess = {false};

        client.authentifier(login, motDePasse, message -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");

            if (succes) {
                Utilisateur utilisateur = (Utilisateur) data.get("utilisateur");
                client.setUtilisateur(utilisateur);
                System.out.println("Connexion réussie! Bienvenue " + utilisateur.getPrenom() + " " + utilisateur.getNom());

                // Enregistrement des handlers de messages
                client.enregistrerHandlerMessageTexte(TestClient::afficherMessageRecu);
                client.enregistrerHandlerNotification(TestClient::afficherNotification);
                client.enregistrerHandlerDemandePriseParole(TestClient::afficherDemandePriseParole);

                authSuccess[0] = true;
            } else {
                String messageText = (String) data.get("message");
                System.out.println("Erreur de connexion: " + messageText);
            }

            authLatch.countDown();
        });

        try {
            authLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (authSuccess[0]) {
            menuUtilisateur();
        }
    }

    private static void creerCompte() {
        System.out.print("Login: ");
        String login = scanner.nextLine();

        System.out.print("Mot de passe: ");
        String motDePasse = scanner.nextLine();

        System.out.print("Nom: ");
        String nom = scanner.nextLine();

        System.out.print("Prénom: ");
        String prenom = scanner.nextLine();

        System.out.println("Création du compte en cours...");

        CountDownLatch createLatch = new CountDownLatch(1);

        client.creerUtilisateur(login, motDePasse, nom, prenom, message -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");
            String messageText = (String) data.get("message");

            System.out.println(messageText);
            createLatch.countDown();
        });

        try {
            createLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void menuUtilisateur() {
        boolean retour = false;

        while (!retour && client.estConnecte()) {
            System.out.println("\nMenu Utilisateur");
            System.out.println("1. Liste des réunions");
            System.out.println("2. Créer une réunion");
            System.out.println("3. Rejoindre une réunion");
            System.out.println("4. Déconnexion");
            System.out.print("Choix: ");

            int choix = lireEntier();

            switch (choix) {
                case 1:
                    afficherListeReunions();
                    break;
                case 2:
                    creerReunion();
                    break;
                case 3:
                    rejoindreReunion();
                    break;
                case 4:
                    retour = true;
                    break;
                default:
                    System.out.println("Choix invalide!");
            }
        }
    }

    private static void afficherListeReunions() {
        System.out.println("\nListe des réunions disponibles:");

        CountDownLatch reunionsLatch = new CountDownLatch(1);

        client.demanderListeReunions(reunions -> {
            if (reunions.isEmpty()) {
                System.out.println("Aucune réunion n'est disponible.");
            } else {
                System.out.println("-----------------------------------------");
                System.out.printf("| %-5s | %-30s |\n", "ID", "Titre");
                System.out.println("-----------------------------------------");

                for (Reunion reunion : reunions) {
                    System.out.printf("| %-5d | %-30s |\n", reunion.getId(), reunion.getTitre());
                }
                System.out.println("-----------------------------------------");
            }
            reunionsLatch.countDown();
        });

        try {
            reunionsLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void creerReunion() {
        System.out.println("\nCréation d'une nouvelle réunion");

        System.out.print("Titre: ");
        String titre = scanner.nextLine();

        System.out.print("Description: ");
        String description = scanner.nextLine();

        LocalDateTime dateDebut = null;
        while (dateDebut == null) {
            try {
                System.out.print("Date de début (yyyy-MM-dd HH:mm): ");
                dateDebut = LocalDateTime.parse(scanner.nextLine(), formatter);
            } catch (Exception e) {
                System.out.println("Format de date invalide. Utilisez le format yyyy-MM-dd HH:mm");
            }
        }

        LocalDateTime dateFin = null;
        while (dateFin == null) {
            try {
                System.out.print("Date de fin (yyyy-MM-dd HH:mm): ");
                dateFin = LocalDateTime.parse(scanner.nextLine(), formatter);
                if (dateFin.isBefore(dateDebut)) {
                    System.out.println("La date de fin doit être après la date de début.");
                    dateFin = null;
                }
            } catch (Exception e) {
                System.out.println("Format de date invalide. Utilisez le format yyyy-MM-dd HH:mm");
            }
        }

        System.out.println("Type de réunion:");
        System.out.println("1. Standard");
        System.out.println("2. Privée");
        System.out.println("3. Démocratique");
        System.out.print("Choix: ");
        int typeChoix = lireEntier();

        Reunion.TypeReunion type;
        switch (typeChoix) {
            case 1:
                type = Reunion.TypeReunion.STANDARD;
                break;
            case 2:
                type = Reunion.TypeReunion.PRIVEE;
                break;
            case 3:
                type = Reunion.TypeReunion.DEMOCRATIQUE;
                break;
            default:
                System.out.println("Type invalide, utilisation du type standard par défaut.");
                type = Reunion.TypeReunion.STANDARD;
        }

        Reunion reunion = new Reunion(
                (int) (System.currentTimeMillis() % 10000),
                titre,
                description,
                dateDebut,
                dateFin,
                type,
                client.getUtilisateur()
        );

        System.out.println("Création de la réunion en cours...");

        CountDownLatch createLatch = new CountDownLatch(1);

        client.creerReunion(reunion, message -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");

            if (succes) {
                Reunion creee = (Reunion) data.get("reunion");
                System.out.println("Réunion créée avec succès: " + creee.getTitre() + " (ID: " + creee.getId() + ")");
            } else {
                System.out.println("Erreur lors de la création de la réunion");
            }

            createLatch.countDown();
        });

        try {
            createLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void rejoindreReunion() {
        // Demander d'abord la liste des réunions
        final List<Reunion>[] reunions = new List[1];
        CountDownLatch reunionsLatch = new CountDownLatch(1);

        client.demanderListeReunions(listeReunions -> {
            reunions[0] = listeReunions;
            reunionsLatch.countDown();
        });

        try {
            reunionsLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (reunions[0] == null || reunions[0].isEmpty()) {
            System.out.println("Aucune réunion disponible.");
            return;
        }

        System.out.println("\nRéunions disponibles:");
        for (int i = 0; i < reunions[0].size(); i++) {
            Reunion r = reunions[0].get(i);
            System.out.println((i + 1) + ". " + r.getTitre() + " (ID: " + r.getId() + ")");
        }

        System.out.print("Sélectionnez une réunion (1-" + reunions[0].size() + "): ");
        int choix = lireEntier();

        if (choix < 1 || choix > reunions[0].size()) {
            System.out.println("Choix invalide!");
            return;
        }

        Reunion reunionSelectionnee = reunions[0].get(choix - 1);
        System.out.println("Tentative de rejoindre la réunion: " + reunionSelectionnee.getTitre());

        CountDownLatch joinLatch = new CountDownLatch(1);
        final boolean[] joinSuccess = {false};

        client.rejoindreReunion(reunionSelectionnee.getId(), message -> {
            Map<String, Object> data = (Map<String, Object>) message.getContenu();
            boolean succes = (boolean) data.get("succes");
            String messageText = (String) data.get("message");

            System.out.println(messageText);

            if (succes) {
                reunionActive = (Reunion) data.get("reunion");
                joinSuccess[0] = true;
            }

            joinLatch.countDown();
        });

        try {
            joinLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (joinSuccess[0]) {
            menuReunion();
        }
    }

    private static void menuReunion() {
        System.out.println("\nRéunion: " + reunionActive.getTitre());
        System.out.println("Type: " + reunionActive.getType());
        System.out.println("Organisateur: " + reunionActive.getOrganisateur().getPrenom() + " " + reunionActive.getOrganisateur().getNom());

        continuerBoucleChat = true;

        // Démarrer un thread pour lire les messages de l'utilisateur pendant la réunion
        Thread inputThread = new Thread(() -> {
            try {
                while (continuerBoucleChat) {
                    System.out.print("\n> ");
                    String ligne = scanner.nextLine();

                    if (ligne.equalsIgnoreCase("/quitter")) {
                        continuerBoucleChat = false;
                    } else if (ligne.equalsIgnoreCase("/parole")) {
                        demanderPriseParole();
                    } else if (!ligne.trim().isEmpty()) {
                        client.envoyerMessageTexte(reunionActive.getId(), ligne);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur dans la boucle de chat: " + e.getMessage());
            }
        });

        inputThread.start();

        // Attendre que l'utilisateur décide de quitter
        try {
            inputThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        reunionActive = null;
        System.out.println("Vous avez quitté la réunion.");
    }

    private static void demanderPriseParole() {
        if (reunionActive.getType() == Reunion.TypeReunion.DEMOCRATIQUE) {
            System.out.println("Demande de prise de parole envoyée...");

            CountDownLatch parleLatch = new CountDownLatch(1);

            client.demanderPriseParole(reunionActive.getId(), message -> {
                Map<String, Object> data = (Map<String, Object>) message.getContenu();
                boolean succes = (boolean) data.get("succes");
                String messageText = (String) data.get("message");

                System.out.println(messageText);
                parleLatch.countDown();
            });

            try {
                parleLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("La prise de parole est libre dans ce type de réunion.");
        }
    }

    private static void accorderPriseParole(String loginUtilisateur) {
        if (reunionActive != null &&
                client.getUtilisateur().equals(reunionActive.getOrganisateur())) {

            System.out.println("Accord de la parole à " + loginUtilisateur);
            client.accorderPriseParole(reunionActive.getId(), loginUtilisateur);
        }
    }

    // Handlers pour les messages reçus
    private static void afficherMessageRecu(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        int idReunion = (int) data.get(Protocole.CLE_ID_REUNION);

        if (reunionActive != null && reunionActive.getId() == idReunion) {
            String texte = (String) data.get(Protocole.CLE_TEXTE);
            System.out.println("\n[" + message.getExpediteur() + "]: " + texte);
            System.out.print("> "); // Remettre le prompt après l'affichage du message
        }
    }

    private static void afficherNotification(Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getContenu();
        String messageText = (String) data.get(Protocole.CLE_MESSAGE);

        System.out.println("\n[NOTIFICATION]: " + messageText);
        System.out.print("> "); // Remettre le prompt
    }

    private static void afficherDemandePriseParole(Message message) {
        if (reunionActive == null) return;

        Map<String, Object> data = (Map<String, Object>) message.getContenu();

        if (message.getType() == MessageType.DEMANDE_PRISE_PAROLE &&
                client.getUtilisateur().equals(reunionActive.getOrganisateur())) {

            Utilisateur demandeur = (Utilisateur) data.get("utilisateur");
            int idReunion = (int) data.get(Protocole.CLE_ID_REUNION);

            if (idReunion == reunionActive.getId()) {
                System.out.println("\n[DEMANDE]: " + demandeur.getPrenom() + " " +
                        demandeur.getNom() + " demande la parole.");
                System.out.println("Pour accorder: /accorder " + demandeur.getLogin());
                System.out.print("> ");
            }
        }
    }

    private static int lireEntier() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}