package bahou.akandan.kassy.bmot.clients;

import bahou.akandan.kassy.bmot.communication.Message;
import bahou.akandan.kassy.bmot.communication.MessageType;
import bahou.akandan.kassy.bmot.modele.Reunion;
import bahou.akandan.kassy.bmot.utils.VideoUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MeetClient extends Client {
    private VideoStream videoStream;
    private ScheduledExecutorService videoSender;
    private boolean streamingActive = false;
    private Map<String, VideoStream> participantStreams = new HashMap<>();

    public MeetClient(String host, int port) {
        super(host, port);
        this.videoStream = new VideoStream();
    }

    public VideoStream getVideoStream() {
        return videoStream;
    }

    public void startVideoStreaming(Reunion reunion) {
        if (streamingActive) return;

        videoStream.startCamera();
        streamingActive = true;

        // Envoyer la vidéo périodiquement au serveur
        videoSender = Executors.newSingleThreadScheduledExecutor();
        videoSender.scheduleAtFixedRate(() -> {
            if (streamingActive && estConnecte()) {
                String encodedFrame = videoStream.getBase64EncodedFrame();
                if (encodedFrame != null && !encodedFrame.isEmpty()) {
                    Map<String, Object> videoData = new HashMap<>();
                    videoData.put("idReunion", reunion.getId());
                    videoData.put("frame", encodedFrame);
                    videoData.put("micActive", videoStream.isMicActive());
                    videoData.put("screenShare", videoStream.isScreenSharing());

                    Message videoMessage = new Message(
                            MessageType.VIDEO_FRAME,
                            videoData,
                            getLogin()
                    );

                    envoyerMessage(videoMessage);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS); // 10 FPS pour la vidéo

        // Enregistrer le handler pour recevoir les vidéos des autres participants
        enregistrerHandler(MessageType.VIDEO_FRAME, this::handleVideoFrame);
    }

    private void handleVideoFrame(Message message) {
        if (!streamingActive) return;

        Map<String, Object> videoData = (Map<String, Object>) message.getContenu();
        int idReunion = (int) videoData.get("idReunion");
        String frame = (String) videoData.get("frame");
        String expediteur = message.getExpediteur();

        // Ne pas traiter ses propres frames
        if (expediteur.equals(getLogin())) return;

        // Obtenir ou créer un objet VideoStream pour ce participant
        VideoStream participantStream = participantStreams.computeIfAbsent(
                expediteur,
                k -> new VideoStream()
        );

        // Mettre à jour l'image du participant
        VideoUtils.updateParticipantVideoStream(participantStream, frame);
    }

    public void stopVideoStreaming() {
        streamingActive = false;

        if (videoSender != null) {
            videoSender.shutdown();
            try {
                videoSender.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        videoStream.stopCamera();

        // Nettoyer les streams des participants
        for (VideoStream stream : participantStreams.values()) {
            stream.stopCamera();
        }
        participantStreams.clear();
    }

    public VideoStream getParticipantStream(String login) {
        return participantStreams.get(login);
    }

    @Override
    public void deconnecter() {
        stopVideoStreaming();
        super.deconnecter();
    }

    // Méthode pour enregistrer les handlers de vidéo spécifiquement
    public void enregistrerHandlerVideoRecu(Consumer<Message> callback) {
        enregistrerHandler(MessageType.VIDEO_FRAME, callback);
    }

    // Méthode pour enregistrer les handlers d'audio spécifiquement
    public void enregistrerHandlerAudioRecu(Consumer<Message> callback) {
        enregistrerHandler(MessageType.AUDIO_FRAME, callback);
    }
}