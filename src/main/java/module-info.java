module bahou.akandan.kassy.bmot {
    // Dépendances JavaFX requises
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.swing;
    requires java.desktop;

    // Ouvrir les packages pour JavaFX FXML
    opens bahou.akandan.kassy.bmot to javafx.fxml;
    opens bahou.akandan.kassy.bmot.clients to javafx.fxml;

    // Exporter les packages
    exports bahou.akandan.kassy.bmot;
    exports bahou.akandan.kassy.bmot.clients;
    exports bahou.akandan.kassy.bmot.modele;
    exports bahou.akandan.kassy.bmot.communication;
    exports bahou.akandan.kassy.bmot.serveurs;
    exports bahou.akandan.kassy.bmot.utils;
}