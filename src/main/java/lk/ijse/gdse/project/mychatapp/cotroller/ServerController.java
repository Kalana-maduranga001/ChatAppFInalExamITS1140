package lk.ijse.gdse.project.chatappfinalexam.cotroller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerController implements Initializable {

    @FXML
    private Button addclient;

    @FXML
    private Button btnEmoji;

    @FXML
    private Button btnFile;

    @FXML
    private Button btnSend;

    @FXML
    private Pane emojiPane;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField txtField;

    @FXML
    private Label connectedClientsLabel;

    @FXML
    private Label serverStatusLabel;

    @FXML
    private VBox userListVBox;

    // Server settings
    private final int PORT = 5000;
    private ServerSocket serverSocket;

    // Map to store client handlers with their usernames
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // Base directory for file storage
    private final String FILE_STORAGE_DIR = "server_files/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the file storage directory
        File directory = new File(FILE_STORAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Hide emoji panel initially
        emojiPane.setVisible(false);

        // Start server in a separate thread
        new Thread(this::startServer).start();

        // Set up event listener for Enter key in text field
        txtField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    btnSendOnAction(new ActionEvent());
                    break;
            }
        });
    }

    /**
     * Starts the server and listens for client connections
     */
    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);

            Platform.runLater(() -> {
                appendToChat("Server started on port " + PORT);
                serverStatusLabel.setText("Online");
                serverStatusLabel.setTextFill(Color.web("#2ecc71"));
            });

            while (true) {
                // Wait for client connection
                Socket clientSocket = serverSocket.accept();

                // Create a data input stream to read client's username
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                String username = inputStream.readUTF();

                // Create and start a new client handler
                ClientHandler clientHandler = new ClientHandler(clientSocket, username);
                clients.put(username, clientHandler);
                new Thread(clientHandler).start();

                Platform.runLater(() -> {
                    appendToChat("Client connected: " + username);
                    updateConnectedClientsCount();
                    addUserToList(username);

                    // Broadcast the new client connection to all clients
                    broadcast("SERVER: " + username + " has joined the chat.", null);
                });
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                appendToChat("Server error: " + e.getMessage());
                serverStatusLabel.setText("Offline");
                serverStatusLabel.setTextFill(Color.web("#e74c3c"));
            });
        }
    }

    /**
     * Updates the connected clients count label
     */
    private void updateConnectedClientsCount() {
        Platform.runLater(() -> {
            connectedClientsLabel.setText(String.valueOf(clients.size()));
        });
    }

    /**
     * Adds a user to the sidebar list
     * @param username The username to add
     */
    private void addUserToList(String username) {
        HBox userBox = new HBox(10);
        userBox.setId("user-" + username);

        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-size: 14px;");

        Pane statusIndicator = new Pane();
        statusIndicator.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 5;");
        statusIndicator.setPrefSize(10, 10);

        userBox.getChildren().addAll(statusIndicator, userLabel);
        userBox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        Platform.runLater(() -> {
            userListVBox.getChildren().add(userBox);
        });
    }

    /**
     * Removes a user from the sidebar list
     * @param username The username to remove
     */
    private void removeUserFromList(String username) {
        Platform.runLater(() -> {
            userListVBox.getChildren().removeIf(node -> {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    return hbox.getId().equals("user-" + username);
                }
                return false;
            });
        });
    }

    /**
     * Appends a message to the chat area
     * @param message The message to append
     */
    private void appendToChat(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            txtArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    /**
     * Broadcasts a message to all connected clients
     * @param message The message to broadcast
     * @param excludeClient The client to exclude from broadcast (can be null)
     */
    private void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients.values()) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Broadcasts a file to all connected clients
     * @param fileName The file name
     * @param fileData The file data
     * @param sender The client who sent the file
     */
    private void broadcastFile(String fileName, byte[] fileData, String sender) {
        for (ClientHandler client : clients.values()) {
            if (!client.getUsername().equals(sender)) {
                client.sendFile(fileName, fileData, sender);
            }
        }
    }

    @FXML
    void AddCltent(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/Client.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Chat Client");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            appendToChat("Error opening client window: " + e.getMessage());
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

    @FXML
    void btnFileOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File file = fileChooser.showOpenDialog(btnFile.getScene().getWindow());

        if (file != null) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                String serverMessage = "SERVER: Sending file " + file.getName() + " to all clients";
                appendToChat(serverMessage);

                // Save file to server directory
                File serverFile = new File(FILE_STORAGE_DIR + file.getName());
                Files.write(serverFile.toPath(), fileData);

                // Create a message for all clients about the file
                broadcast("SERVER: Sending file: " + file.getName(), null);

                // Send file to all clients
                for (ClientHandler client : clients.values()) {
                    client.sendFile(file.getName(), fileData, "SERVER");
                }

            } catch (IOException e) {
                appendToChat("Error sending file: " + e.getMessage());
            }
        }
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
    void btnSendOnAction(ActionEvent event) {
        String message = txtField.getText().trim();
        if (!message.isEmpty()) {
            String serverMessage = "SERVER: " + message;
            appendToChat(serverMessage);
            broadcast(serverMessage, null);
            txtField.clear();
        }
    }

    @FXML
    void btnSmileOnAction(ActionEvent event) {
        txtField.appendText("😊");
        emojiPane.setVisible(false);
    }

    /**
     * Closes the server socket and all client connections when the application exits
     */
    public void shutdown() {
        try {
            broadcast("SERVER: Server is shutting down.", null);

            // Close all client connections
            for (ClientHandler client : clients.values()) {
                client.close();
            }

            // Clear clients map
            clients.clear();

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    /**
     * Inner class to handle client connections
     */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private String username;
        private boolean running = true;

        public ClientHandler(Socket socket, String username) {
            this.socket = socket;
            this.username = username;
            try {
                this.inputStream = new DataInputStream(socket.getInputStream());
                this.outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                appendToChat("Error setting up client streams: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                while (running) {
                    // Read message type
                    String messageType = inputStream.readUTF();

                    switch (messageType) {
                        case "TEXT":
                            String message = inputStream.readUTF();
                            Platform.runLater(() -> {
                                appendToChat(username + ": " + message);
                            });
                            broadcast(username + ": " + message, this);
                            break;

                        case "FILE":
                            // Read file name
                            String fileName = inputStream.readUTF();

                            // Read file size
                            int fileSize = inputStream.readInt();

                            // Read file data
                            byte[] fileData = new byte[fileSize];
                            inputStream.readFully(fileData);

                            // Save file to server directory
                            File serverFile = new File(FILE_STORAGE_DIR + fileName);
                            Files.write(serverFile.toPath(), fileData);

                            Platform.runLater(() -> {
                                appendToChat(username + " sent a file: " + fileName);
                            });

                            // Broadcast file to other clients
                            broadcastFile(fileName, fileData, username);
                            break;
                    }
                }
            } catch (IOException e) {
                // Handle client disconnection
                Platform.runLater(() -> {
                    appendToChat(username + " disconnected: " + e.getMessage());
                    clients.remove(username);
                    updateConnectedClientsCount();
                    removeUserFromList(username);
                    broadcast("SERVER: " + username + " has left the chat.", null);
                });
            } finally {
                close();
            }
        }

        /**
         * Sends a text message to the client
         * @param message The message to send
         */
        public void sendMessage(String message) {
            try {
                outputStream.writeUTF("TEXT");
                outputStream.writeUTF(message);
                outputStream.flush();
            } catch (IOException e) {
                appendToChat("Error sending message to " + username + ": " + e.getMessage());
            }
        }

        /**
         * Sends a file to the client
         * @param fileName The file name
         * @param fileData The file data
         * @param sender The sender of the file
         */
        public void sendFile(String fileName, byte[] fileData, String sender) {
            try {
                outputStream.writeUTF("FILE");
                outputStream.writeUTF(fileName);
                outputStream.writeUTF(sender);
                outputStream.writeInt(fileData.length);
                outputStream.write(fileData);
                outputStream.flush();
            } catch (IOException e) {
                appendToChat("Error sending file to " + username + ": " + e.getMessage());
            }
        }

        /**
         * Closes the client connection
         */
        public void close() {
            running = false;
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client resources: " + e.getMessage());
            }
        }

        /**
         * Gets the username of the client
         * @return The username
         */
        public String getUsername() {
            return username;
        }
    }
}