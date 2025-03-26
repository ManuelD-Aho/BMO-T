package bahou.akandan.kassy.bmot.serveurs;

import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.communication.MessageType;
import bahou.akandan.kassy.bmot.modele.Reunion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VideoManager {
    // Mapper les utilisateurs aux réunions auxquelles ils sont connectés en vidéo
    private Map<String, Integer> utilisateurReunionMap = new ConcurrentHashMap<>();

    // Mapper les ID de réunions aux utilisateurs et leur statut vidéo
    private Map<Integer, Map<String, VideoStatus>> reunionVideoMap = new ConcurrentHashMap<>();

    public VideoManager() {
    }

    public void enregistrerUtilisateur(String login, int idReunion) {
        utilisateurReunionMap.put(login, idReunion);

        Map<String, VideoStatus> videoStatusMap = reunionVideoMap.computeIfAbsent(
                idReunion,
                k -> new ConcurrentHashMap<>()
        );

        videoStatusMap.put(login, new VideoStatus());
    }

    public void deconnecterUtilisateur(String login) {
        Integer idReunion = utilisateurReunionMap.remove(login);

        if (idReunion != null) {
            Map<String, VideoStatus> videoStatusMap = reunionVideoMap.get(idReunion);
            if (videoStatusMap != null) {
                videoStatusMap.remove(login);

                // Si plus personne dans la réunion, on nettoie
                if (videoStatusMap.isEmpty()) {
                    reunionVideoMap.remove(idReunion);
                }
            }
        }
    }

    public void diffuserVideo(Serveur serveur, Message message) {
        String expediteur = message.getExpediteur();
        Map<String, Object> videoData = (Map<String, Object>) message.getContenu();
        int idReunion = (int) videoData.get("idReunion");

        // Mettre à jour le statut vidéo de l'utilisateur
        Map<String, VideoStatus> videoStatusMap = reunionVideoMap.get(idReunion);
        if (videoStatusMap != null) {
            VideoStatus status = videoStatusMap.get(expediteur);
            if (status != null) {
                boolean micActive = (boolean) videoData.get("micActive");
                boolean screenShare = (boolean) videoData.get("screenShare");
                status.setMicActive(micActive);
                status.setScreenSharing(screenShare);
            }
        }

        // Obtenir la réunion
        Reunion reunion = serveur.getReunion(idReunion);
        if (reunion == null) return;

        // Diffuser aux autres participants
        for (String login : videoStatusMap.keySet()) {
            // Ne pas renvoyer à l'expéditeur
            if (!login.equals(expediteur)) {
                GestionnaireClient client = serveur.getClient(login);
                if (client != null) {
                    client.envoyerMessage(message);
                }
            }
        }
    }

    public Map<String, VideoStatus> getStatusUtilisateursReunion(int idReunion) {
        return reunionVideoMap.getOrDefault(idReunion, new HashMap<>());
    }

    // Classe pour suivre le statut vidéo d'un utilisateur
    public static class VideoStatus {
        private boolean micActive = true;
        private boolean cameraActive = true;
        private boolean screenSharing = false;

        public boolean isMicActive() {
            return micActive;
        }

        public void setMicActive(boolean micActive) {
            this.micActive = micActive;
        }

        public boolean isCameraActive() {
            return cameraActive;
        }

        public void setCameraActive(boolean cameraActive) {
            this.cameraActive = cameraActive;
        }

        public boolean isScreenSharing() {
            return screenSharing;
        }

        public void setScreenSharing(boolean screenSharing) {
            this.screenSharing = screenSharing;
        }
    }
}