package lk.ijse.gdse.project.mychatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.gdse.project.mychatapp.cotroller.ServerController;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader serverLoader = new FXMLLoader(Main.class.getResource("/view/Sever.fxml"));
        Scene serverScene = new Scene(serverLoader.load());

        ServerController serverController = serverLoader.getController();

        Stage serverStage = new Stage();
        serverStage.setTitle("Chat Server");
        serverStage.setScene(serverScene);
        serverStage.setOnCloseRequest(event -> {
            if (serverController != null) {
                serverController.shutdown();
            }
        });
        serverStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}