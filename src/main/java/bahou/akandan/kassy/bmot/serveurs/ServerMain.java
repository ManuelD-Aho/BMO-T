package bahou.akandan.kassy.bmot.serveurs;

import bahou.akandan.kassy.bmot.TestUtils;
import bahou.akandan.kassy.bmot.utils.Configuration;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("BMO Meet Server - Démarrage");
        System.out.println("Port: " + Configuration.getPort());

        Serveur serveur = new Serveur();

        // Initialiser des données de test si demandé
        if (args.length > 0 && args[0].equals("--init-test-data")) {
            try {
                Class.forName("bahou.akandan.kassy.bmot.TestUtils")
                        .getMethod("initialiserDonneesTest", Serveur.class)
                        .invoke(null, serveur);
                System.out.println("Données de test initialisées");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation des données de test: " + e.getMessage());
            }
        }

        serveur.demarrer();

        System.out.println("Serveur démarré. Appuyez sur Entrée pour arrêter...");

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Arrêt du serveur...");
        serveur.arreter();
        System.out.println("Serveur arrêté.");
    }
}