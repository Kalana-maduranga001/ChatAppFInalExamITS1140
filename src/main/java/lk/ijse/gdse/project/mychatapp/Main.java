package lk.ijse.gdse.project.chatappfinalexam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.gdse.project.chatappfinalexam.cotroller.ServerController;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load and show server window
        FXMLLoader serverLoader = new FXMLLoader(Main.class.getResource("/view/Sever.fxml"));
        Scene serverScene = new Scene(serverLoader.load());

        ServerController serverController = serverLoader.getController();

        // Set up the server window
        Stage serverStage = new Stage();
        serverStage.setTitle("Chat Server");
        serverStage.setScene(serverScene);
        serverStage.setOnCloseRequest(event -> {
            if (serverController != null) {
                serverController.shutdown();
            }
        });
        serverStage.show();

        // Note: We don't automatically open a client window here
        // The server UI has a button to open client windows as needed
    }

    public static void main(String[] args) {
        launch();
    }
}