package bahou.akandan.kassy.bmot.clients;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

/**
 * Gère la capture et le flux vidéo pour la vidéoconférence (version sans AWT)
 */
public class VideoStream {
    private ScheduledExecutorService timer;
    private boolean running = false;
    private boolean micActive = true;
    private boolean cameraActive = true;
    private static final int FRAME_RATE = 15;

    // Pour la webcam
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private WritableImage placeholderImage;

    // Pour l'écran
    private boolean screenSharing = false;

    public VideoStream() {
        // Créer une image placeholder pour quand la caméra est désactivée
        placeholderImage = new WritableImage(640, 480);
        fillPlaceholderImage(placeholderImage, "Caméra désactivée");
    }

    private void fillPlaceholderImage(WritableImage image, String text) {
        PixelWriter pixelWriter = image.getPixelWriter();

        // Remplir avec une couleur grise foncée
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixelWriter.setColor(x, y, Color.DARKGRAY);
            }
        }

        // Le texte serait normalement dessiné ici, mais comme nous n'utilisons pas
        // de GraphicsContext, nous ne pouvons pas dessiner du texte facilement sans AWT
    }

    public void startCamera() {
        if (running) return;

        running = true;
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::captureFrame, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
    }

    public void stopCamera() {
        running = false;
        if (timer != null) {
            timer.shutdown();
            try {
                timer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void captureFrame() {
        try {
            WritableImage capturedImage;

            if (screenSharing) {
                // Simulation du partage d'écran
                capturedImage = new WritableImage(640, 480);
                simulateScreenCapture(capturedImage);
            } else if (cameraActive) {
                // Simuler la capture webcam avec une image colorée simple
                capturedImage = new WritableImage(640, 480);
                simulateWebcamCapture(capturedImage);
            } else {
                capturedImage = placeholderImage;
            }

            // Mettre à jour l'image pour l'affichage
            final Image finalImage = capturedImage;
            Platform.runLater(() -> {
                imageProperty.set(finalImage);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void simulateWebcamCapture(WritableImage image) {
        PixelWriter pixelWriter = image.getPixelWriter();
        long time = System.currentTimeMillis();
        // Créer une couleur qui change avec le temps pour simuler un flux
        int red = (int)(time % 255);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixelWriter.setColor(x, y, Color.rgb(red, 100, 150));
            }
        }

        // Ajouter un motif simple pour montrer que c'est une simulation
        for (int y = 40; y < 80; y++) {
            for (int x = 30; x < 200; x++) {
                pixelWriter.setColor(x, y, Color.WHITE);
            }
        }
    }

    private void simulateScreenCapture(WritableImage image) {
        PixelWriter pixelWriter = image.getPixelWriter();

        // Remplir avec une couleur de base
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixelWriter.setColor(x, y, Color.LIGHTBLUE);
            }
        }

        // Simuler une interface d'écran partagé
        for (int y = 0; y < image.getHeight(); y += 50) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixelWriter.setColor(x, y, Color.GRAY);
            }
        }

        for (int x = 0; x < image.getWidth(); x += 50) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixelWriter.setColor(x, y, Color.GRAY);
            }
        }
    }

    // Cette méthode serait utilisée dans une implémentation réelle
    // Pour cette simulation, elle retourne juste un texte
    public String getBase64EncodedFrame() {
        // Dans une implémentation réelle, convertir l'image actuelle en Base64
        return "SimulatedVideoFrameInBase64==";
    }

    public ObjectProperty<Image> imageProperty() {
        return imageProperty;
    }

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

    public void toggleScreenSharing() {
        this.screenSharing = !this.screenSharing;
    }

    public void toggleCamera() {
        this.cameraActive = !this.cameraActive;
    }

    public void toggleMic() {
        this.micActive = !this.micActive;
    }
}