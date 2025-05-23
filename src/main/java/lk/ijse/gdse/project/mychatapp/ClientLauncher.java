package lk.ijse.gdse.project.mychatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.gdse.project.mychatapp.cotroller.ClaintController;

import java.io.IOException;

/**
 * Separate launcher for client application if you want to run it independently
 */
public class ClientLauncher extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load client window
        FXMLLoader clientLoader = new FXMLLoader(ClientLauncher.class.getResource("/view/Client.fxml"));
        Scene clientScene = new Scene(clientLoader.load());

        ClaintController clientController = clientLoader.getController();

        // Set up the client window
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(clientScene);
        primaryStage.setOnCloseRequest(event -> {
            if (clientController != null) {
                clientController.shutdown();
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }


}