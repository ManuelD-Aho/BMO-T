package bahou.akandan.kassy.bmot.communication;

import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.modele.Utilisateur;

import java.util.HashMap;
import java.util.Map;

public class Protocole {
    // Constantes pour les clés de données communes
    public static final String CLE_LOGIN = "login";
    public static final String CLE_MOT_DE_PASSE = "motDePasse";
    public static final String CLE_NOM = "nom";
    public static final String CLE_PRENOM = "prenom";
    public static final String CLE_SUCCES = "succes";
    public static final String CLE_MESSAGE = "message";
    public static final String CLE_ERREUR = "erreur";
    public static final String CLE_ID_REUNION = "idReunion";
    public static final String CLE_TEXTE = "texte";

    // Méthodes pour créer des messages standard

    public static Message creerMessageConnexion(String login, String motDePasse) {
        Map<String, Object> data = new HashMap<>();
        data.put(CLE_LOGIN, login);
        data.put(CLE_MOT_DE_PASSE, motDePasse);
        return new Message(MessageType.CONNEXION, data, login);
    }

    public static Message creerMessageConnexionReponse(boolean succes, Utilisateur utilisateur, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put(CLE_SUCCES, succes);
        data.put(CLE_MESSAGE, message);
        if (succes && utilisateur != null) {
            data.put("utilisateur", utilisateur);
        }
        return new Message(MessageType.CONNEXION_REPONSE, data, "SERVEUR");
    }

    public static Message creerMessageCreerReunion(Reunion reunion) {
        return new Message(MessageType.CREER_REUNION, reunion, reunion.getOrganisateur().getLogin());
    }

    public static Message creerMessageRejoindreReunion(int idReunion, String loginUtilisateur) {
        Map<String, Object> data = new HashMap<>();
        data.put(CLE_ID_REUNION, idReunion);
        return new Message(MessageType.REJOINDRE_REUNION, data, loginUtilisateur);
    }

    public static Message creerMessageTexte(int idReunion, String texte, String loginExpediteur) {
        Map<String, Object> data = new HashMap<>();
        data.put(CLE_ID_REUNION, idReunion);
        data.put(CLE_TEXTE, texte);
        return new Message(MessageType.MESSAGE_TEXTE, data, loginExpediteur);
    }

    public static Message creerMessageDemandePriseParole(int idReunion, String loginDemandeur) {
        Map<String, Object> data = new HashMap<>();
        data.put(CLE_ID_REUNION, idReunion);
        return new Message(MessageType.DEMANDE_PRISE_PAROLE, data, loginDemandeur);
    }

    public static Message creerMessageErreur(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put(CLE_ERREUR, message);
        return new Message(MessageType.ERREUR, data, "SERVEUR");
    }
}