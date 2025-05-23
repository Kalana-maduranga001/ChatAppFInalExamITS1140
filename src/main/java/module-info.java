module lk.ijse.gdse.project.chatappfinalexam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens lk.ijse.gdse.project.mychatapp to javafx.fxml;
    opens lk.ijse.gdse.project.mychatapp.cotroller to javafx.fxml;

    exports lk.ijse.gdse.project.mychatapp;
    exports lk.ijse.gdse.project.mychatapp.cotroller;

}