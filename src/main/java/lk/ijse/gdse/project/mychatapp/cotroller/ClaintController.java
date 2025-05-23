package lk.ijse.gdse.project.mychatapp.cotroller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ClaintController implements Initializable {

    @FXML
    private Button btnEmoji;

    @FXML
    private Button btnFile;

    @FXML
    private Button btnSend;

    @FXML
    private Pane emojiPane;

    @FXML
    private Label lblClientName;

    @FXML
    private Pane namePane;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField txtField;

    @FXML
    private TextField txtName;

    @FXML
    private Label Command;



    private final String HOST = "localhost";
    private final int PORT = 5000;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String username;
    private boolean connected = false;


    private final String FILE_STORAGE_DIR = "client_files/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
// Initialize the file storage directory
        File directory = new File(FILE_STORAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }


        emojiPane.setVisible(false);


        namePane.setVisible(true);
        txtArea.setVisible(false);
        txtField.setVisible(false);
        btnSend.setVisible(false);
        btnEmoji.setVisible(false);
        btnFile.setVisible(false);

// Set up listener for Enter key in the name field
        txtName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectToServer();
            }
        });
    }

    /**
     * Attempts to connect to the server with the provided username
     */

    private void connectToServer() {
        username = txtName.getText().trim();

        if (username.isEmpty()) {
            appendToChat("Please enter a username.");
            return;
        }

        try {

            socket = new Socket(HOST, PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());


            outputStream.writeUTF(username);


            Platform.runLater(() -> {
                namePane.setVisible(false);
                txtArea.setVisible(true);
                txtField.setVisible(true);
                btnSend.setVisible(true);
                btnEmoji.setVisible(true);
                btnFile.setVisible(true);
                lblClientName.setText(username);
                appendToChat("Connected to server as " + username);
            });

            connected = true;


            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            appendToChat("Could not connect to server: " + e.getMessage());
        }
    }

    /**
     * Continuously listens for incoming messages from the server
     */

    private void receiveMessages() {
        try {
            while (connected) {

                String messageType = inputStream.readUTF();

                switch (messageType) {
                    case "TEXT":

                        String message = inputStream.readUTF();
                        appendToChat(message);
                        break;

                    case "FILE":

                        String fileName = inputStream.readUTF();


                        String sender = inputStream.readUTF();


                        int fileSize = inputStream.readInt();


                        byte[] fileData = new byte[fileSize];
                        inputStream.readFully(fileData);


                        File receivedFile = new File(FILE_STORAGE_DIR + fileName);
                        Files.write(receivedFile.toPath(), fileData);

                        Platform.runLater(() -> {
                            appendToChat("Received file from " + sender + ": " + fileName);
                            appendToChat("File saved to: " + receivedFile.getAbsolutePath());
                        });
                        break;
                }
            }
        } catch (IOException e) {
            if (connected) {
                Platform.runLater(() -> {
                    appendToChat("Connection to server lost: " + e.getMessage());
                    disconnectFromServer();
                });
            }
        }
    }

    /**
     * Disconnects from the server
     */
    private void disconnectFromServer() {
        connected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();


            Platform.runLater(() -> {
                namePane.setVisible(true);
                txtArea.setVisible(false);
                txtField.setVisible(false);
                btnSend.setVisible(false);
                btnEmoji.setVisible(false);
                btnFile.setVisible(false);
                txtName.clear();
            });

        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * Appends a message to the chat area
     * message The message to append
     */
    private void appendToChat(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            txtArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    /**
     * Sends a text message to the server
     * message The message to send
     */
    private void sendMessage(String message) {
        if (!connected || message.trim().isEmpty()) return;

        try {
            outputStream.writeUTF("TEXT");
            outputStream.writeUTF(message);
            outputStream.flush();
            appendToChat("You: " + message);
        } catch (IOException e) {
            appendToChat("Failed to send message: " + e.getMessage());
        }

        clientSendCommand(message);
    }

    /**
     * Sends a file to the server
     *  file The file to send
     */

    private void sendFile(File file) {
        if (!connected || file == null) return;

        try {
            byte[] fileData = Files.readAllBytes(file.toPath());

            outputStream.writeUTF("FILE");
            outputStream.writeUTF(file.getName());
            outputStream.writeInt(fileData.length);
            outputStream.write(fileData);
            outputStream.flush();

            appendToChat("You sent a file: " + file.getName());
        } catch (IOException e) {
            appendToChat("Failed to send file: " + e.getMessage());
        }
    }


    @FXML
    void btnCoolOnAction(ActionEvent event) {
        txtField.appendText("😎");
        emojiPane.setVisible(false);
    }

    @FXML
    void btnCryOnAction(ActionEvent event) {
        txtField.appendText("😢");
        emojiPane.setVisible(false);
    }

    @FXML
    void btnEmojiOnAction(ActionEvent event) {
        emojiPane.setVisible(!emojiPane.isVisible());
    }

    /**
     * client can send to command sever and response
     *  sever send reply for client
     */

    public void clientSendCommand(String command) {
        if (!connected || command == null || command.trim().isEmpty()) return;

        command = command.trim().toLowerCase();
        String response;

        switch (command) {
            case "time":
                response = "Server time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                break;
            case "date":
                response = "Today's date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                break;
            case "uptime":
                response = "Server uptime: 1 Time";
                break;
            case "help":
                response = "time - gives the current time\n" +
                        "date - gives the current date\n" +
                        "uptime - give the uptime\n" +
                        "bye - quit";
                return;
            case "bye":
                appendToChat("Disconnecting...");
                disconnectFromServer();
                return;
            default:
                response = "Unknown command: " + command;
        }

        appendToChat(response);
    }




    @FXML
    void btnFileOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File file = fileChooser.showOpenDialog(btnFile.getScene().getWindow());

        if (file != null) {
            sendFile(file);
        }

        emojiPane.setVisible(false);
    }


    @FXML
    void btnHeartOnAction(ActionEvent event) {
        txtField.appendText("❤");
        emojiPane.setVisible(false);
    }

    @FXML
    void btnLaughOnAction(ActionEvent event) {
        txtField.appendText("😂");
        emojiPane.setVisible(false);
    }

    @FXML
    void btnLikeOnAction(ActionEvent event) {
        txtField.appendText("👍");
        emojiPane.setVisible(false);
    }


    @FXML
    void displayCommand(ActionEvent event) {

    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        String message = txtField.getText().trim();
        if (!message.isEmpty()) {
            if (message.startsWith(">")) {
                clientSendCommand(message.substring(1)); // Remove '>' and send as command
            } else {
                sendMessage(message);
            }
            txtField.clear();
        }

        emojiPane.setVisible(false);
    }

    @FXML
    void btnSmileOnAction(ActionEvent event) {
        txtField.appendText("😊");
        emojiPane.setVisible(false);
    }

    @FXML
    void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            btnSendOnAction(new ActionEvent());
        }
    }

    /**
     * Closes the connection when the application exits
     */
    public void shutdown() {
        disconnectFromServer();
    }
}