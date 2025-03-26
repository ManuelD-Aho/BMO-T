package bahou.akandan.kassy.bmot;

import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;
import bahou.akandan.kassy.bmot.serveurs.Serveur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe utilitaire pour tester et initialiser le serveur avec des données
 */
public class TestUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Initialise le serveur avec des données de test
     * @param serveur Le serveur à initialiser
     */
    public static void initialiserDonneesTest(Serveur serveur) {
        System.out.println("Initialisation des données de test...");

        // Création d'utilisateurs
        Utilisateur admin = new Utilisateur("admin", "admin", "Admin", "Système");
        admin.setEstAdministrateur(true);

        Utilisateur user1 = new Utilisateur("user1", "pass1", "Jean", "Dupont");
        Utilisateur user2 = new Utilisateur("user2", "pass2", "Marie", "Martin");
        Utilisateur user3 = new Utilisateur("user3", "pass3", "Paul", "Bernard");

        serveur.creerUtilisateur(admin);
        serveur.creerUtilisateur(user1);
        serveur.creerUtilisateur(user2);
        serveur.creerUtilisateur(user3);

        // Création de réunions
        LocalDateTime now = LocalDateTime.now();

        Reunion reunion1 = new Reunion(
                1001,
                "Réunion de développement",
                "Discussion sur l'avancement des fonctionnalités",
                now.plusDays(1).withHour(10).withMinute(0),
                now.plusDays(1).withHour(11).withMinute(30),
                Reunion.TypeReunion.STANDARD,
                user1
        );

        Reunion reunion2 = new Reunion(
                1002,
                "Réunion de planification",
                "Planification des tâches pour le prochain sprint",
                now.plusDays(2).withHour(14).withMinute(0),
                now.plusDays(2).withHour(16).withMinute(0),
                Reunion.TypeReunion.DEMOCRATIQUE,
                admin
        );

        Reunion reunion3 = new Reunion(
                1003,
                "Réunion confidentielle",
                "Discussion sur les nouveaux projets",
                now.plusDays(3).withHour(9).withMinute(0),
                now.plusDays(3).withHour(10).withMinute(30),
                Reunion.TypeReunion.PRIVEE,
                user2
        );

        // Ajout des participants
        reunion1.ajouterParticipant(user2);
        reunion1.ajouterParticipant(user3);

        reunion2.ajouterParticipant(user1);
        reunion2.ajouterParticipant(user2);
        reunion2.ajouterParticipant(user3);

        reunion3.ajouterParticipant(admin);

        // Enregistrement des réunions
        serveur.creerReunion(reunion1);
        serveur.creerReunion(reunion2);
        serveur.creerReunion(reunion3);

        System.out.println("Données de test initialisées avec succès!");
        System.out.println("Utilisateurs créés: " + serveur.getUtilisateurs().size());
        System.out.println("Réunions créées: " + serveur.getReunions().size());

        afficherDetails(serveur);
    }

    /**
     * Affiche les détails des utilisateurs et réunions dans le serveur
     * @param serveur Le serveur dont afficher les détails
     */
    public static void afficherDetails(Serveur serveur) {
        List<Utilisateur> utilisateurs = serveur.getUtilisateurs();
        List<Reunion> reunions = serveur.getReunions();

        System.out.println("\n=== Utilisateurs ===");
        for (Utilisateur u : utilisateurs) {
            System.out.println("- " + u.getLogin() + " (" + u.getPrenom() + " " + u.getNom() + ")");
        }

        System.out.println("\n=== Réunions ===");
        for (Reunion r : reunions) {
            System.out.println("- ID: " + r.getId() + " | " + r.getTitre() + " | Type: " + r.getType() +
                    " | Organisateur: " + r.getOrganisateur().getLogin());

            System.out.println("  Participants:");
            for (Utilisateur u : r.getParticipants()) {
                System.out.println("  * " + u.getLogin());
            }
            System.out.println();
        }
    }

    /**
     * Méthode principale pour tester l'initialisation des données
     */
    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        initialiserDonneesTest(serveur);
        serveur.demarrer();

        System.out.println("Serveur démarré avec les données de test.");
        System.out.println("Appuyez sur Entrée pour arrêter le serveur...");

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        serveur.arreter();
        System.out.println("Serveur arrêté.");
    }
}