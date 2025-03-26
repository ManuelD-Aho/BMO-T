module bahou.akandan.kassy.bmot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.desktop;

    opens bahou.akandan.kassy.bmot to javafx.fxml;
    opens bahou.akandan.kassy.bmot.clients to javafx.fxml;

    exports bahou.akandan.kassy.bmot;
    exports bahou.akandan.kassy.bmot.clients;
    exports bahou.akandan.kassy.bmot.modele;
    exports bahou.akandan.kassy.bmot.communication;
    exports bahou.akandan.kassy.bmot.serveurs;
    exports bahou.akandan.kassy.bmot.utils;
}