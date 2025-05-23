module lk.ijse.gdse.project.mychatapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.ijse.gdse.project.mychatapp to javafx.fxml;
    exports lk.ijse.gdse.project.mychatapp;
}