module bahou.akandan.kassy.bmot {
    requires javafx.controls;
    requires javafx.fxml;


    opens bahou.akandan.kassy.bmot to javafx.fxml;
    exports bahou.akandan.kassy.bmot;
}