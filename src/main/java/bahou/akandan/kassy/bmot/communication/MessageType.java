package bahou.akandan.kassy.bmot.communication;

public enum MessageType {
    // Messages d'authentification
    CONNEXION,
    CONNEXION_REPONSE,
    DECONNEXION,

    // Messages de gestion des utilisateurs
    CREER_UTILISATEUR,
    CREER_UTILISATEUR_REPONSE,
    LISTE_UTILISATEURS,
    LISTE_UTILISATEURS_REPONSE,

    // Messages de gestion des réunions
    CREER_REUNION,
    CREER_REUNION_REPONSE,
    LISTE_REUNIONS,
    LISTE_REUNIONS_REPONSE,
    REJOINDRE_REUNION,
    REJOINDRE_REUNION_REPONSE,
    QUITTER_REUNION,
    MODIFIER_REUNION,
    SUPPRIMER_REUNION,

    // Messages de communication dans les réunions
    MESSAGE_TEXTE,
    DEMANDE_PRISE_PAROLE,
    ACCORDER_PRISE_PAROLE,
    NOTIFIER_PARTICIPANTS,

    // Nouveaux messages pour la vidéo et l'audio
    VIDEO_FRAME,       // Pour envoyer un frame vidéo
    AUDIO_FRAME,       // Pour envoyer un chunk audio
    VIDEO_STATUS,      // Pour indiquer si la caméra/micro est activé/désactivé
    SCREEN_SHARE,      // Pour indiquer qu'un partage d'écran commence/s'arrête
    SCREEN_SHARE_FRAME,// Pour envoyer un frame de partage d'écran

    // Messages d'erreurs et système
    ERREUR,
    PING,
    PONG
}