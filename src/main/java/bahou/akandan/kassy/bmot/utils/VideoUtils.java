package bahou.akandan.kassy.bmot.utils;

import bahou.akandan.kassy.bmot.clients.VideoStream;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class VideoUtils {

    /**
     * Met à jour le flux vidéo d'un participant avec une image encodée en Base64
     */
    public static void updateParticipantVideoStream(VideoStream stream, String base64EncodedFrame) {
        if (base64EncodedFrame == null || base64EncodedFrame.isEmpty()) {
            return;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64EncodedFrame);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            Image image = new Image(bis);

            Platform.runLater(() -> {
                stream.imageProperty().set(image);
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du flux vidéo: " + e.getMessage());
        }
    }

    /**
     * Redimensionne une image pour optimiser la bande passante
     */
    public static Image resizeImage(Image source, int targetWidth) {
        if (source == null) return null;

        double ratio = source.getWidth() / source.getHeight();
        int targetHeight = (int) (targetWidth / ratio);

        return new Image(source.getUrl(), targetWidth, targetHeight, true, true);
    }
}