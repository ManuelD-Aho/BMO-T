package bahou.akandan.kassy.bmot.serveurs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GestionnaireClient implements Runnable {

    private Socket clientSocket;

    public GestionnaireClient(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            // Lecture des messages du client et réponse
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Message reçu du client : " + inputLine);
                // Ici, vous pouvez appliquer une logique d'authentification ou de routage de message
                out.println("Serveur a bien reçu : " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le client : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
