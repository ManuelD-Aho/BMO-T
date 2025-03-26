package bahou.akandan.kassy.bmot;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

// Contrôleur pour l'interface FXML - pas utilisé dans cette implémentation
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Bienvenue dans l'application BMO!");
    }
}