package App.passwordsaver;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.ResourceBundle;

public class WelcomePageController implements Initializable {

    @FXML Button confirmButton;
    @FXML Button detailsConfirmButton;

    @FXML AnchorPane topBar;
    @FXML AnchorPane firstTimeDetailsAnchor;
    @FXML AnchorPane detailsAnchor;

    @FXML TextField usernameField;
    String username;

    @FXML PasswordField masterKeyField;
    @FXML PasswordField masterKeyField2;
    @FXML PasswordField confirmMasterKeyField;
    String masterKey;
    String confirmMasterKey;

    @FXML Tooltip confirmPassToolTip;

    @FXML Label errorLabel;
    @FXML Label errorLabel2;

    private double windowX = 0, windowY = 0;

    private boolean detailsEntered = true;

    private static final int LOCALKEY = 7697;

    private static String currentUsername;

    public static File file;

    public String userHome = System.getProperty("user.home");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        file = new File(userHome + "\\AppData\\Roaming\\Checks.txt");
        try {
            if (file.createNewFile()) {
                // First time opening, handle first-time entry
                detailsAnchor.setVisible(false);
                firstTimeDetailsAnchor.setVisible(true);

                confirmButton.setOnAction(e -> handleFirstTimeDetailsEntered(e));
            } else {
                // File exists, now check if there's any content inside the file
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                boolean isFileNotEmpty = false;

                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) { // Check if the line is not empty
                        isFileNotEmpty = true;
                        break; // No need to check further, as we found some content
                    }
                }
                reader.close();

                if (isFileNotEmpty) {
                    // If there's text in the file, proceed to handle details
                    firstTimeDetailsAnchor.setVisible(false);
                    detailsAnchor.setVisible(true);

                    detailsConfirmButton.setOnAction(e -> handleDetails(e));
                } else {
                    // If the file is empty, handle first-time details entry
                    firstTimeDetailsAnchor.setVisible(true);
                    detailsAnchor.setVisible(false);

                    confirmButton.setOnAction(e -> handleFirstTimeDetailsEntered(e));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        //functionality for dragging the window
        topBar.setOnMousePressed(e ->{
            windowX = e.getSceneX();
            windowY = e.getSceneY();
        } );

        topBar.setOnMouseDragged(e ->{
            Main.stageCopy.setX(e.getScreenX() - windowX);
            Main.stageCopy.setY(e.getScreenY() - windowY);
        });

    }

    public void GoToHomePage(ActionEvent event) throws IOException {

        Scene homePageScene = new Scene(FXMLLoader.load(Main.class.getResource("HomePage.fxml")));

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setScene(homePageScene);
        centerStage(currentStage);

    }

    public void CloseWindow(ActionEvent event){
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void MinimizeWindow(ActionEvent event){
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    //centering the new stage
    private void centerStage(Stage stage) {
        // Get screen dimensions (primary screen)
        Screen screen = Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();

        // Get the stage's dimensions
        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();

        // Calculate the new position for the stage to center it
        double xPosition = (screenWidth - stageWidth) / 2;
        double yPosition = (screenHeight - stageHeight) / 2;

        // Set the position of the stage
        stage.setX(xPosition);
        stage.setY(yPosition);
    }

    public void handleFirstTimeDetailsEntered(ActionEvent e) {
        try {
            // Validate the username field
            if (usernameField.getText().isEmpty()) {
                holdAndShowError("Please Enter Username");
                return; // Exit the method if the username is empty
            }

            // Retrieve values
            username = usernameField.getText();

            // Check if fields are empty before parsing
            if (masterKeyField.getText().isEmpty()) {
                holdAndShowError("Please Enter Master Key");
                return; // Exit the method if the master key is empty
            }

            if (confirmMasterKeyField.getText().isEmpty()) {
                holdAndShowError("Please Confirm Your Master Key");
                return; // Exit the method if the confirm master key is empty
            }

            // Parse only if the fields are not empty
            masterKey = masterKeyField.getText();
            confirmMasterKey = confirmMasterKeyField.getText();

            // Check if master keys match
            if (!masterKey.equals(confirmMasterKey)) {
                confirmMasterKeyField.setText(""); // Clear only the confirmation field
                holdAndShowError("Keys Don't Match!");
            } else {
                // Correct details entered

                // Encrypt the master key
                String encryptedMasterKey = encrypt(masterKey);

                // Check if the file exists
                if (!file.exists()) {
                    // If file does not exist, create it
                    boolean fileCreated = file.createNewFile();
                    if (!fileCreated) {
                        throw new IOException("Failed to create the file.");
                    }
                }

                // Create a FileWriter to append username and encrypted master key into the file
                try (FileWriter writer = new FileWriter(file, true)) { // 'true' to append to the file
                    // Write the username and encrypted master key into the file
                    writer.write(username + ":" + encryptedMasterKey + "\n");
                } catch (IOException ex) {
                    // Handle exceptions that may occur during file writing
                    ex.printStackTrace();
                    holdAndShowError("Failed to save credentials.");
                    return; // Avoid throwing runtime exception, show user-friendly message
                }

                currentUsername = username;

                // Go to the home page after successfully saving the data
                GoToHomePage(e);
            }

        } catch (NumberFormatException ex) {
            // This should now only occur when there is a non-numeric value in master key or confirm master key fields
            if (!masterKeyField.getText().isEmpty()) {
                holdAndShowError("Enter Only Integers In Master Key Field!");
            }
        } catch (IOException ex) {
            // Handle any I/O exceptions that may occur during file creation or writing
            ex.printStackTrace();
            holdAndShowError("An error occurred while working with the file.");
        } catch (Exception ex) {
            ex.printStackTrace();
            holdAndShowError("An unexpected error occurred.");
        }
    }

    public void handleDetails(ActionEvent e) {
        try {
            masterKey = masterKeyField2.getText();

            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            bufferedReader.close();

            if (line == null || line.isEmpty()) {
                holdAndShowError("No data found in the file.");
                return;
            }

            String[] parts = line.split(":");
            if (parts.length == 2) {
                String savedUsername = parts[0];
                String encryptedMasterKey = parts[1];

                try {
                    String savedMasterKey = decrypt(encryptedMasterKey);

                    masterKey = HomePageController.padKey(masterKey, 16);
                    savedMasterKey = HomePageController.padKey(savedMasterKey, 16);

                    if (masterKey.equals(savedMasterKey)) {
                        currentUsername = savedUsername;
                        GoToHomePage(e);
                    } else {
                        holdAndShowError("Master Key Doesn't Match!");
                    }
                } catch (Exception decryptException) {
                    System.err.println("Decryption Failed");
                    decryptException.printStackTrace();
                    holdAndShowError("Error Decrypting Master Key");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            holdAndShowError("An unexpected error occurred");
        }
    }

    private void holdAndShowError(String message){
        errorLabel.setText(message);
        errorLabel2.setText(message);
        errorLabel.setVisible(true);
        errorLabel2.setVisible(true);

        // Create a PauseTransition to hide the error label after 2 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            errorLabel.setVisible(false);
            errorLabel2.setVisible(false);
        });

        // Start the pause transition
        pause.play();
    }

    // Encrypt function using AES
    public static String encrypt(String masterKey) throws Exception {
        // Generate a secure key using SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(String.valueOf(LOCALKEY).getBytes());

        // Truncate to 16 bytes for AES-128
        byte[] truncatedKey = Arrays.copyOf(keyBytes, 16);

        // Generate AES key specification
        SecretKeySpec key = new SecretKeySpec(truncatedKey, "AES");

        // Create a cipher object
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // Initialize the cipher in encryption mode
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // Convert the master key into a byte array
        byte[] masterKeyBytes = masterKey.getBytes("UTF-8");

        // Encrypt the master key
        byte[] encrypted = cipher.doFinal(masterKeyBytes);

        // Return the encrypted byte array as a Base64 encoded string
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedMasterKey) throws Exception {
        try {
            // Generate a secure key using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(String.valueOf(LOCALKEY).getBytes());

            // Truncate to 16 bytes for AES-128
            byte[] truncatedKey = Arrays.copyOf(keyBytes, 16);

            // Generate AES key specification
            SecretKeySpec key = new SecretKeySpec(truncatedKey, "AES");

            // Create a cipher object
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // Initialize the cipher in decryption mode
            cipher.init(Cipher.DECRYPT_MODE, key);

            // Decode the Base64 string back into byte array
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMasterKey);

            // Decrypt the encrypted byte array
            byte[] decrypted = cipher.doFinal(encryptedBytes);

            // Convert the decrypted byte array back to a string (master key)
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            // Detailed error logging
            System.err.println("Decryption Error Details:");
            e.printStackTrace();
            throw e; // Re-throw to maintain original error handling
        }
    }

    public static String getUsername(){
        return currentUsername;
    }

    // Function to update the username in the checks.txt file
    public static void updateUsername(String newUsername) {
        try {
            // Read the current file content
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder fileContent = new StringBuilder();
            String line;

            // Read through each line and update the username
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String savedUsername = parts[0];
                    String encryptedMasterKey = parts[1];

                    // If this is the line we need to update
                    if (savedUsername.equals(currentUsername)) {
                        fileContent.append(newUsername).append(":").append(encryptedMasterKey).append("\n");
                    } else {
                        fileContent.append(line).append("\n");
                    }
                }
            }

            // Close the reader
            bufferedReader.close();

            // Now write the updated content back to the file
            FileWriter writer = new FileWriter(file);
            writer.write(fileContent.toString());
            writer.close();

            // Update the currentUsername
            currentUsername = newUsername;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //------------- ANIMATIONS -----------------

}
