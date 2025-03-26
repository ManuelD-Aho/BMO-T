package bahou.akandan.kassy.bmot.serveurs;

import bahou.akandan.kassy.bmot.utils.Configuration;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    private int port;

    public Serveur() {
        // Lit le port depuis config.properties, 5002 par défaut
        this.port = Configuration.getInt("server.port", 5002);
    }

    public void start() {
        System.out.println("Démarrage du serveur sur le port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré, en attente de connexions...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté: " + clientSocket.getRemoteSocketAddress());
                // Vous pouvez lancer un nouveau thread pour gérer la connexion du client ici
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Serveur().start();
    }
}
