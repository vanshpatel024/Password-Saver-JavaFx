package App.passwordsaver;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class HomePageController implements Initializable {

    @FXML AnchorPane topBar;
    @FXML AnchorPane homePageAnchor;
    @FXML AnchorPane createPageAnchor;
    @FXML AnchorPane displayPageAnchor;
    @FXML AnchorPane changeMyKeyPageAnchor;
    @FXML AnchorPane editPane;
    @FXML AnchorPane aboutUsPageAnchor;
    @FXML AnchorPane aboutUsBg;
    private AnchorPane recentEntriesPane;

    @FXML Button homeButton;
    @FXML Button createButtonNav;
    @FXML Button displayButton;
    @FXML Button changeMyKeyButton;
    @FXML Button extraButton;

    @FXML TextField websiteNameTextField;
    @FXML TextField usernameTextField;
    @FXML TextField commentTextField;
    @FXML TextField searchBar;
    @FXML TextField editWebsiteField;
    @FXML TextField editUsernameField;
    @FXML TextField editCommentField;

    @FXML PasswordField passwordPassField;
    @FXML PasswordField prevMasterKeyField;
    @FXML PasswordField newMasterKeyField;
    @FXML PasswordField confirmMasterKeyField;
    @FXML PasswordField editPasswordField;

    @FXML Label welcomeLabel;
    @FXML Label errorLabel;
    @FXML Label errorLabel3;
    @FXML Label succesLabel;
    @FXML Label succesLabel3;
    @FXML Label errorLabel5;
    @FXML Label succesLabel5;
    @FXML Label noEntriesLabel;
    @FXML Label aboutUsLabel1;
    @FXML Label aboutUsLabel2;
    @FXML Label aboutUsLabel3;
    @FXML Label aboutUsLabel4;
    Label totalEntriesLabel;


    @FXML private VBox displayContainer;
    @FXML private VBox navVbox;
    VBox labelContainer;


    @FXML ScrollPane scrollPane;

    private String websiteName;
    private String username;
    private String password;
    private String comment;
    private String prevMasterKey;
    private String newMasterKey;
    private String confirmMasterKey;
    public static String userHome = System.getProperty("user.home");

    private File file;

    private int editingIndex = -1;
    private double windowX = 0, windowY = 0;

    private Rectangle backgroundOverlay;
    private Rectangle highlight;


    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        homePageAnchor.setMaxWidth(700);

        noEntriesLabel.setVisible(false);

        // Create the label to display the username
        welcomeLabel = new Label("Welcome, " + WelcomePageController.getUsername());
        welcomeLabel.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 32px;");

        // Create ImageView for the edit button icon
        Image editImage = new Image(Main.class.getResourceAsStream("edit.png"));
        ImageView editIcon = new ImageView(editImage);
        editIcon.setFitWidth(16); // Set the width of the image
        editIcon.setFitHeight(16); // Set the height of the image

        // Apply color adjustments to the icon
        ColorAdjust editColorAdjust = new ColorAdjust();
        editColorAdjust.setSaturation(0); // Remove color saturation to make it grayscale
        editColorAdjust.setBrightness(1); // Set brightness to maximum
        editColorAdjust.setContrast(1); // Normal contrast
        editIcon.setEffect(editColorAdjust);

        // Create the Edit button
        Button editButton = new Button();
        editButton.setGraphic(editIcon);
        editButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Hover effect for Edit button
        editButton.setOnMouseEntered(event -> editButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10;"));
        editButton.setOnMouseExited(event -> editButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10;"));
        editButton.setOnAction(e -> openEditUsernamePane());

        // Create an HBox to place the label and the button side by side
        HBox welcomeContainer = new HBox(10); // 10 pixels spacing between label and button
        welcomeContainer.setLayoutY(50);
        welcomeContainer.setMinWidth(700);
        Platform.runLater(() -> {
            welcomeContainer.setAlignment(Pos.CENTER); // Center-align the contents
        });

        // Create the label to display the total number of entries
        totalEntriesLabel = new Label();
        totalEntriesLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 18px; -fx-text-fill: #d9d8d8;");
        updateTotalEntries(); // Initialize the total entries label with the current count

        // Add the label and button to the HBox
        welcomeContainer.getChildren().addAll(welcomeLabel, editButton);

        // Create an HBox to place the username and total entries label vertically
        labelContainer = new VBox(10); // 10 pixels spacing between elements
        labelContainer.getChildren().addAll(welcomeContainer, totalEntriesLabel);
        labelContainer.setLayoutY(50);
        labelContainer.setMinWidth(700);
        Platform.runLater(() -> {
            labelContainer.setAlignment(Pos.CENTER); // Center-align the contents
        });

        // Add the HBox to the main layout (e.g., root pane, etc.)
        homePageAnchor.getChildren().add(labelContainer);

        initializeRecentEntriesPane();

        showRecentEntries();
        updateTotalEntries();

        //setting button active at initial stage
        setActiveButton(homeButton);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                displayPasswords(); // Display all entries when search bar is empty
            } else {
                performSearch(newValue);
            }
        });

        //functionality for dragging the window
        topBar.setOnMousePressed(e ->{
            windowX = e.getSceneX();
            windowY = e.getSceneY();
        } );

        topBar.setOnMouseDragged(e ->{
            Main.stageCopy.setX(e.getScreenX() - windowX);
            Main.stageCopy.setY(e.getScreenY() - windowY);
        });

        homeButton.fire();

        initializeUI();
    }

    private void initializeRecentEntriesPane() {
        // Create the recentEntriesPane and style it
        recentEntriesPane = new AnchorPane();
        recentEntriesPane.setStyle("-fx-background-color: #180119; -fx-border-radius: 10; -fx-padding: 10;");
        recentEntriesPane.setMinHeight(300); // Adjust the height
        recentEntriesPane.setMinWidth(680); // Adjust the width
        recentEntriesPane.setPrefHeight(300); // Adjust the height
        recentEntriesPane.setPrefWidth(680); // Adjust the width
        AnchorPane.setTopAnchor(recentEntriesPane, 120.0); // Position below total entries
        AnchorPane.setLeftAnchor(recentEntriesPane, 50.0);

        // Add the pane to the main anchor
        homePageAnchor.getChildren().add(recentEntriesPane);
    }

    private void showRecentEntries() {
        // Clear any existing content in the recent entries pane
        if (recentEntriesPane != null) {
            homePageAnchor.getChildren().remove(recentEntriesPane);
        }

        // Create the recent entries pane
        recentEntriesPane = new AnchorPane();
        recentEntriesPane.setStyle("-fx-background-color: #180119; -fx-border-radius: 10; -fx-padding: 10;");
        recentEntriesPane.setPrefHeight(300); // Adjust height as needed
        recentEntriesPane.setPrefWidth(680); // Adjust width as needed
        AnchorPane.setBottomAnchor(recentEntriesPane, 80.0);
        AnchorPane.setLeftAnchor(recentEntriesPane, 20.0);

        // Create VBox to hold the rows (HBox)
        VBox recentEntriesVBox = new VBox(15); // Vertical space between rows
        recentEntriesVBox.setAlignment(Pos.TOP_CENTER); // Align rows to the top center
        recentEntriesVBox.setStyle("-fx-padding: 10;");

        String filePath = userHome + "\\AppData\\Roaming\\EntriesData\\data.txt";
        File file = new File(filePath);

        // Check if the file exists and can be read
        if (!file.exists() || file.length() == 0) {
            // File doesn't exist or is empty, show "No recent entries" message
            showNoEntriesLabel();
            return;
        }

        try {
            List<String[]> recentEntries = new LinkedList<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder entryBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.equals("----")) { // A separator for entries
                    // Process the current entry when we encounter the separator
                    String entryData = entryBuilder.toString();
                    String[] entryParts = parseEntryData(entryData);
                    if (entryParts != null) {
                        if (recentEntries.size() == 4) {
                            recentEntries.remove(0); // Keep only the last 4 entries
                        }
                        recentEntries.add(entryParts);
                    }
                    entryBuilder.setLength(0); // Reset the entry builder for the next entry
                } else {
                    entryBuilder.append(line).append("\n");
                }
            }

            // If there are no entries, show the "No entries" message
            if (recentEntries.isEmpty()) {
                showNoEntriesLabel();
                return;
            }

            // Create a sequential transition to handle the animations
            SequentialTransition sequentialTransition = new SequentialTransition();

            HBox currentRow = null; // Current row (HBox)
            for (int i = 0; i < recentEntries.size(); i++) {
                String[] entry = recentEntries.get(i);

                // Safely check for null or empty comment
                String comment = (entry[3] != null && !entry[3].isEmpty()) ? entry[3] : "";

                // Create the data block for the entry
                AnchorPane entryBlock = createDataBlockForRecentEntries(entry[0], entry[1], entry[2], comment);

                // Set initial properties for animation
                entryBlock.setOpacity(0); // Initially invisible
                entryBlock.setTranslateY(20); // Start slightly below its final position

                // Add the entry block to the current row (HBox)
                if (currentRow == null || currentRow.getChildren().size() == 2) {
                    // Create a new row if no current row exists or the current row is full
                    currentRow = new HBox(15); // Horizontal space between panes
                    currentRow.setAlignment(Pos.CENTER); // Center items horizontally
                    recentEntriesVBox.getChildren().add(currentRow); // Add the new row to the VBox
                }
                currentRow.getChildren().add(entryBlock);

                // Prepare the fade-in and slide-up animation
                FadeTransition fade = new FadeTransition(Duration.millis(150), entryBlock);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);

                TranslateTransition translate = new TranslateTransition(Duration.millis(275), entryBlock);
                translate.setFromY(20); // Start 20 pixels below
                translate.setToY(0); // End at the original position

                // Combine the animations
                ParallelTransition parallelTransition = new ParallelTransition(fade, translate);
                sequentialTransition.getChildren().add(parallelTransition);
            }

            // Add the VBox to the recent entries pane
            recentEntriesPane.getChildren().add(recentEntriesVBox);
            homePageAnchor.getChildren().add(recentEntriesPane);

            // Play the animations after adding all the entries
            sequentialTransition.play();

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading recent entries.");
            errorLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-text-fill: red;");
            recentEntriesVBox.getChildren().add(errorLabel);

            // Add the VBox to the recent entries pane
            recentEntriesPane.getChildren().add(recentEntriesVBox);
            homePageAnchor.getChildren().add(recentEntriesPane);
        }
    }

    private void showNoEntriesLabel() {
        noEntriesLabel.setVisible(true);
        noEntriesLabel.setText("No recent entries to display.");
        noEntriesLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-text-fill: #d9d8d8;");

        // Initially set opacity to 0 for fade-in effect
        noEntriesLabel.setOpacity(0);
        noEntriesLabel.setTranslateY(20); // Start slightly below

        // Trigger the animation after UI components are fully loaded
        Platform.runLater(() -> {
            noEntriesLabel.setLayoutX((700 - noEntriesLabel.getWidth())/2);
            noEntriesLabel.setLayoutY(350);

            // Apply fade-in and translate animations
            FadeTransition fade = new FadeTransition(Duration.millis(300), noEntriesLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            TranslateTransition translate = new TranslateTransition(Duration.millis(300), noEntriesLabel);
            translate.setFromY(20); // Start 20 pixels below
            translate.setToY(0); // End at the original position

            // Play the animations
            ParallelTransition parallelTransition = new ParallelTransition(fade, translate);
            parallelTransition.play();
        });
    }

    private AnchorPane createDataBlockForRecentEntries(String website, String username, String password, String comment) {
        noEntriesLabel.setVisible(false);
        AnchorPane entryBlock = new AnchorPane();
        entryBlock.setPrefSize(309, 200);
        entryBlock.setStyle("-fx-background-color: #1d011e; -fx-padding: 10; -fx-text-fill: #d9d8d8;" +
                "-fx-background-radius: 15; -fx-border-radius: 15;");

        entryBlock.setOnMouseEntered(event -> entryBlock.setStyle("-fx-background-color: #210122; -fx-padding: 10; -fx-text-fill: #d9d8d8;" +
                "-fx-background-radius: 15; -fx-border-radius: 15;"));

        entryBlock.setOnMouseExited(event -> entryBlock.setStyle("-fx-background-color: #1d011e; -fx-padding: 10; -fx-text-fill: #d9d8d8;" +
                "-fx-background-radius: 15; -fx-border-radius: 15;"));

        VBox vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);

        // Add labels for website, username, and password
        vBox.getChildren().add(createLabeledButton("Website", website));
        vBox.getChildren().add(createLabeledButton("Username", username));
        vBox.getChildren().add(createLabeledButton("Password", password));

        // Add a comment if present
        if (!comment.isEmpty()) {
            vBox.getChildren().add(createLabeledButton("Comment", comment));
        }

        // Create an HBox for side-by-side buttons (only "Copy" button here)
        HBox buttonBox = new HBox(15); // 15 is the spacing between buttons
        buttonBox.setAlignment(Pos.CENTER);

        // --- Copy Button Setup ---
        // Create ImageView for the copy button icon
        Image copyImage = new Image(Main.class.getResourceAsStream("copy.png"));
        ImageView copyIcon = new ImageView(copyImage);
        copyIcon.setFitWidth(20); // Set the width of the image
        copyIcon.setFitHeight(18); // Set the height of the image

        // Apply a ColorAdjust effect to change the icon color to white
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(0); // Remove color saturation to make it grayscale
        colorAdjust.setBrightness(1); // Set brightness to maximum
        colorAdjust.setContrast(1); // Normal contrast
        copyIcon.setEffect(colorAdjust);

        // Create a button with the copy icon
        Button copyButton = new Button();
        copyButton.setGraphic(copyIcon);
        copyButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Boolean flag to check if the button is in the "pressed" state
        BooleanProperty isPressed = new SimpleBooleanProperty(false);

        // Button hover effect
        copyButton.setOnMouseEntered(event -> {
            if (!isPressed.get()) {
                copyButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10; -fx-border-radius: 10;");
            }
        });
        copyButton.setOnMouseExited(event -> {
            if (!isPressed.get()) {
                copyButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10; -fx-border-radius: 10;");
            }
        });

        // Copy action
        copyButton.setOnAction(e -> {
            copyToClipboard(website); // Example action, copy website to clipboard

            // Set the "pressed" state to true
            isPressed.set(true);

            // Change the background color to green when pressed
            copyButton.setStyle("-fx-background-color: #28a745; -fx-border-radius: 10; -fx-background-radius: 10;");

            // Create a Timeline to reset the background color after .5 seconds (500 milliseconds)
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(500), event1 -> {
                        // Revert the background color to the original state
                        copyButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");
                        // Set "pressed" flag back to false after 1 second
                        isPressed.set(false);
                    })
            );
            timeline.play(); // Start the timeline
        });

        entryBlock.getChildren().add(vBox);
        AnchorPane.setTopAnchor(vBox, 10.0);
        AnchorPane.setBottomAnchor(vBox, 10.0);
        AnchorPane.setLeftAnchor(vBox, 10.0);
        AnchorPane.setRightAnchor(vBox, 10.0);

        return entryBlock;
    }

    private String[] parseEntryData(String entryData) throws Exception {
        String[] parts = new String[4]; // 4 fields: website, username, password, comment
        String[] lines = entryData.split("\n");

        for (String line : lines) {
            if (line.startsWith("Web.")) {
                parts[0] = decryptData(line.substring(4), getDecryptedMasterKey());
            } else if (line.startsWith("Usnm.")) {
                parts[1] = decryptData(line.substring(5), getDecryptedMasterKey());
            } else if (line.startsWith("Ps.")) {
                parts[2] = decryptData(line.substring(3), getDecryptedMasterKey());
            } else if (line.startsWith("Cmt.")) {
                parts[3] = decryptData(line.substring(4), getDecryptedMasterKey());
                if (parts[3] == null || parts[3].isEmpty()) {
                    parts[3] = ""; // If comment is null or empty, initialize to empty string
                }
            }
        }
        return parts;
    }

    private void updateTotalEntries() {
        try {
            Path filePath = Paths.get(userHome + "\\AppData\\Roaming\\EntriesData\\data.txt"); // Path to your data file
            if (Files.exists(filePath)) {
                long totalEntries = Files.lines(filePath)
                        .filter(line -> line.contains("----"))
                        .count();
                totalEntriesLabel.setText("Total entries: " + totalEntries);
            } else {
                totalEntriesLabel.setText("Total entries: 0");
            }
        } catch (IOException e) {
            totalEntriesLabel.setText("Error reading entries");
            e.printStackTrace();
        }
    }

    private void openEditUsernamePane() {
        // Create the edit pane similar to the delete confirmation layout
        AnchorPane editPane = new AnchorPane();
        editPane.setStyle("-fx-background-color: #1d011e; -fx-border-radius: 15; -fx-background-radius: 15;");
        editPane.setPrefSize(400, 200);

        // Create a background blur effect
        Rectangle backgroundOverlay = new Rectangle(homePageAnchor.getWidth(), homePageAnchor.getHeight());
        backgroundOverlay.setFill(Color.color(0, 0, 0, 0.5));  // Semi-transparent black
        backgroundOverlay.setEffect(new GaussianBlur(10));  // Applying blur effect

        // Add the background overlay to the display container (displayPageAnchor)
        homePageAnchor.getChildren().add(backgroundOverlay);

        // Create a TextField for the new username
        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Enter new username...");
        newUsernameField.setStyle("-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-background-color: #270128; " +
                "-fx-text-fill: #d9d8d8;");
        newUsernameField.setPrefWidth(300); // Set width of the text field
        newUsernameField.setAlignment(Pos.CENTER); // Center the text inside the TextField
        AnchorPane.setTopAnchor(newUsernameField, 70.0);
        AnchorPane.setLeftAnchor(newUsernameField, 50.0);
        AnchorPane.setRightAnchor(newUsernameField, 50.0);

        // Add hover effect for border color change
        newUsernameField.setOnMouseEntered(event -> newUsernameField.setStyle("-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-background-color: #270128; " +
                "-fx-text-fill: #d9d8d8; " +
                "-fx-border-color: #ab72c3;"));
        newUsernameField.setOnMouseExited(event -> newUsernameField.setStyle("-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-background-color: #270128; " +
                "-fx-text-fill: #d9d8d8;"));

        // Create Yes and No buttons in a side-by-side layout (HBox)
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #534e53; -fx-text-fill: #d9d8d8; -fx-border-radius: 10; -fx-background-radius: 10;");
        saveButton.setOnMouseEntered(event -> saveButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        saveButton.setOnMouseExited(event -> saveButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        saveButton.setOnAction(e -> {
            String newUsername = newUsernameField.getText().trim();
            if (!newUsername.isEmpty()) {
                // Update the username
                WelcomePageController.updateUsername(newUsername);
                // Refresh the welcome label to show the updated username
                welcomeLabel.setText("Welcome, " + WelcomePageController.getUsername());
                // Remove the edit pane and background overlay
                homePageAnchor.getChildren().remove(editPane);
                homePageAnchor.getChildren().remove(backgroundOverlay);
            } else {
                // Handle empty username input
                holdAndShowError("Username cannot be empty.", "err");
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #534e53; -fx-text-fill: #d9d8d8; -fx-border-radius: 10; -fx-background-radius: 10;");
        cancelButton.setOnMouseEntered(event -> cancelButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        cancelButton.setOnMouseExited(event -> cancelButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        cancelButton.setOnAction(e -> {
            // Close the edit pane and background overlay
            homePageAnchor.getChildren().remove(editPane);
            homePageAnchor.getChildren().remove(backgroundOverlay);
        });

        // Create an HBox for the buttons to be side-by-side
        HBox buttonBox = new HBox(20, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER); // Align buttons to the center
        AnchorPane.setBottomAnchor(buttonBox, 40.0);
        AnchorPane.setLeftAnchor(buttonBox, 20.0);
        AnchorPane.setRightAnchor(buttonBox, 20.0);

        // Add the TextField and buttonBox to the editPane
        editPane.getChildren().addAll(newUsernameField, buttonBox);

        // Center the edit pane in the display container (displayPageAnchor)
        AnchorPane.setTopAnchor(editPane, (homePageAnchor.getHeight() - editPane.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(editPane, (homePageAnchor.getWidth() - editPane.getPrefWidth()) / 2);

        // Add the edit pane to the display container
        homePageAnchor.getChildren().add(editPane);
    }

    public void checkAndSaveEntries() {

        if (websiteNameTextField.getText().isEmpty()){
            holdAndShowError("Please enter a website name", "err");
            return;
        } else if (usernameTextField.getText().isEmpty()){
            holdAndShowError("Please enter username", "err");
            return;
        } else if (passwordPassField.getText().isEmpty()){
            holdAndShowError("Please enter your password", "err");
            return;
        }

        String decryptedMasterKey = null;
        try {
            decryptedMasterKey = getDecryptedMasterKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Retrieve values from the text fields
        websiteName = websiteNameTextField.getText();
        username = usernameTextField.getText();
        password = passwordPassField.getText();
        comment = commentTextField.getText();

        String encryptedWebsiteName = null;
        String encryptedUsername = null;
        String encryptedPassword = null;
        String encryptedComment = null;

        try {
            // Encrypt the data using the decrypted master key
            encryptedWebsiteName = encryptData(websiteName, decryptedMasterKey);
            encryptedUsername = encryptData(username, decryptedMasterKey);
            encryptedPassword = encryptData(password, decryptedMasterKey);
            encryptedComment = encryptData(comment, decryptedMasterKey);
        } catch (Exception e) {
            holdAndShowError("Encryption failed.", "err");
            e.printStackTrace();
            return; // Exit the function if encryption fails
        }

        // Path for the folder and file
        String folderPath = userHome +"\\AppData\\Roaming\\EntriesData";
        String filePath = folderPath + "\\data.txt";

        // Ensure the folder and file exist
        File folder = new File(folderPath);
        File file = new File(filePath);

        try {
            if (!folder.exists()) {
                folder.mkdirs(); // Create the folder if it does not exist
            }

            if (!file.exists()) {
                file.createNewFile(); // Create the file if it does not exist
            }

            // Write data to the file
            try (FileWriter writer = new FileWriter(file, true)) {
                // Append the entry data
                writer.write("Web." + encryptedWebsiteName + "\n");
                writer.write("Usnm." + encryptedUsername + "\n");
                writer.write("Ps." + encryptedPassword + "\n");
                writer.write("Cmt." + encryptedComment + "\n");
                writer.write("----\n"); // Separator for clarity
            }

        } catch (IOException e) {
            holdAndShowError("An error occurred while saving your data.", "err");
            e.printStackTrace();
            return; // Exit function if an exception occurs
        }

        // Clear input fields after saving
        websiteNameTextField.clear();
        usernameTextField.clear();
        passwordPassField.clear();
        commentTextField.clear();

        updateTotalEntries();
        showRecentEntries();

        // Show success message
        holdAndShowError("Details saved successfully!", "succ");
    }

    private void performSearch(String query) {
        displayContainer.getChildren().clear(); // Clear existing displayed data

        Label noResultsLabel = new Label("No matching entries found.");
        noResultsLabel.setStyle("-fx-text-fill: #d9d8d8; -fx-font-size: 12;");
        noResultsLabel.setVisible(false); // Initially hidden
        noResultsLabel.setLayoutX((530 - noResultsLabel.getWidth()) / 2);
        noResultsLabel.setLayoutY(50);
        displayPageAnchor.getChildren().add(noResultsLabel); // Add the label to the UI container

        String filePath = userHome +"\\AppData\\Roaming\\EntriesData\\data.txt";
        File file = new File(filePath);

        if (!file.exists()) {
            holdAndShowError("No data found to search.", "err");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String website = "", username = "", password = "", comment = "";
            HBox currentRow = null; // Container for the current row
            int columnCount = 0; // Track the number of columns in the current row
            int currentIndex = 0; // Track the index of the current entry

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Web.")) {
                    website = decryptData(line.substring(4), getDecryptedMasterKey());
                } else if (line.startsWith("Usnm.")) {
                    username = decryptData(line.substring(5), getDecryptedMasterKey());
                } else if (line.startsWith("Ps.")) {
                    password = decryptData(line.substring(3), getDecryptedMasterKey());
                } else if (line.startsWith("Cmt.")) {
                    comment = decryptData(line.substring(4), getDecryptedMasterKey());
                } else if (line.equals("----")) {
                    // Check if the entry matches the query
                    if (website.toLowerCase().contains(query.toLowerCase())) {
                        // Create a data block with the Edit button functionality
                        AnchorPane dataBlock = createDataBlock(website, username, password, comment, currentIndex);

                        // Create a new row if necessary
                        if (currentRow == null || columnCount == 2) { // Two columns per row
                            currentRow = new HBox(20); // Spacing between columns
                            currentRow.setStyle("-fx-padding: 10; -fx-background-color: #180119;");
                            displayContainer.getChildren().add(currentRow);
                            columnCount = 0;
                        }

                        // Add the data block to the current row
                        currentRow.getChildren().add(dataBlock);
                        columnCount++;
                    }

                    // Reset variables for the next entry
                    website = "";
                    username = "";
                    password = "";
                    comment = "";
                    currentIndex++;
                }
            }

            // If no matches were found, display the label
            if (displayContainer.getChildren().isEmpty()) {
                noResultsLabel.setVisible(true);

                // Fade in the label
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), noResultsLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                // Pause for 2 seconds
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event -> {
                    // Fade out the label after 2 seconds
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), noResultsLabel);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> noResultsLabel.setVisible(false)); // Hide the label after fading out
                    fadeOut.play();
                });
                pause.play();
            }

        } catch (Exception e) {
            e.printStackTrace();
            holdAndShowError("Error while searching data.", "err");
        }
    }

    public static String readAndDecryptMasterKeyFromFile() throws Exception {
        // Step 1: Read the username:encryptedMasterKey from the file
        File file = new File(userHome +"\\AppData\\Roaming\\Checks.txt");
        StringBuilder fileContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line).append("\n");  // Read the whole file content
            }
        }

        String fileData = fileContent.toString().trim();  // Remove extra newlines
        String[] parts = fileData.split(":");  // Step 2: Split content into username and encrypted master key

        if (parts.length != 2) {
            System.out.println("Invalid data in the file. Expected format: username:encryptedMasterKey");
        }

        String encryptedMasterKey = parts[1]; // Extract the encrypted master key

        // Step 3: Decrypt the encrypted master key using the decrypt function
        return WelcomePageController.decrypt(encryptedMasterKey);  // Return the decrypted master key
    }

    public void displayPasswords() {
        // Clear the container before adding new data
        displayContainer.getChildren().clear();

        String filePath = userHome + "\\AppData\\Roaming\\EntriesData\\data.txt";
        File file = new File(filePath);

        if (!file.exists()) {
            holdAndShowError("No data found to display.", "err");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String website = "", username = "", password = "", comment = "";
            HBox currentRow = null; // Container for the current row
            int columnCount = 0; // Track the number of columns in the current row
            int currentIndex = 0; // Track the index of the current entry

            SequentialTransition sequentialTransition = new SequentialTransition(); // To manage the animations

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Web.")) {
                    website = decryptData(line.substring(4), getDecryptedMasterKey());
                } else if (line.startsWith("Usnm.")) {
                    username = decryptData(line.substring(5), getDecryptedMasterKey());
                } else if (line.startsWith("Ps.")) {
                    password = decryptData(line.substring(3), getDecryptedMasterKey());
                } else if (line.startsWith("Cmt.")) {
                    comment = decryptData(line.substring(4), getDecryptedMasterKey());
                } else if (line.equals("----")) {
                    // Create a new data block for the current entry
                    AnchorPane dataBlock = createDataBlock(website, username, password, comment, currentIndex);

                    // Set initial properties for animation
                    dataBlock.setOpacity(0); // Initially invisible
                    dataBlock.setTranslateY(20); // Start slightly below its final position

                    // Add the block to the current row or create a new row if needed
                    if (currentRow == null || columnCount == 2) { // Two columns per row
                        currentRow = new HBox(20); // Spacing between columns
                        currentRow.setStyle("-fx-padding: 10; -fx-background-color: #180119;");
                        displayContainer.getChildren().add(currentRow);
                        columnCount = 0;
                    }
                    currentRow.getChildren().add(dataBlock);
                    columnCount++;

                    // Faster fade and translate animation
                    FadeTransition fade = new FadeTransition(Duration.millis(150), dataBlock);
                    fade.setFromValue(0.0);
                    fade.setToValue(1.0);

                    TranslateTransition translate = new TranslateTransition(Duration.millis(275), dataBlock);
                    translate.setFromY(20); // Start 20 pixels below its final position
                    translate.setToY(0);

                    ParallelTransition parallelTransition = new ParallelTransition(fade, translate); // Combine fade and translate
                    sequentialTransition.getChildren().add(parallelTransition); // Add to sequential transition

                    // Reset entry data
                    website = "";
                    username = "";
                    password = "";
                    comment = "";

                    // Increment the index for the next entry
                    currentIndex++;
                }
            }

            // Play animations
            sequentialTransition.play();

        } catch (Exception e) {
            e.printStackTrace();
            holdAndShowError("Error reading or decrypting data.", "err");
        }
    }

    private AnchorPane createDataBlock(String website, String username, String password, String comment, int index) {
        AnchorPane block = new AnchorPane();
        block.setPrefSize(320, 200);
        block.setStyle("-fx-background-color: #1d011e; -fx-padding: 10; -fx-text-fill: #d9d8d8;" +
                "-fx-background-radius: 15; -fx-border-radius: 15;");

        block.setOnMouseEntered(event -> block.setStyle("-fx-background-color: #210122; -fx-padding: 10; -fx-text-fill: #d9d8d8;" +
                "-fx-background-radius: 15; -fx-border-radius: 15;"));

        block.setOnMouseExited(event -> block.setStyle("-fx-background-color: #1d011e; -fx-padding: 10; -fx-text-fill: #d9d8d8;" +
                "-fx-background-radius: 15; -fx-border-radius: 15;"));

        VBox vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);

        // Add labels for website, username, and password
        vBox.getChildren().add(createLabeledButton("Website", website));
        vBox.getChildren().add(createLabeledButton("Username", username));
        vBox.getChildren().add(createLabeledButton("Password", password));

        // Add a comment if present
        if (!comment.isEmpty()) {
            vBox.getChildren().add(createLabeledButton("Comment", comment));
        }

        // Create an HBox for side-by-side buttons
        HBox buttonBox = new HBox(15); // 15 is the spacing between buttons
        buttonBox.setAlignment(Pos.CENTER);

        // --- Edit Button Setup ---
        // Create ImageView for the edit button icon
        Image editImage = new Image(Main.class.getResourceAsStream("edit.png"));
        ImageView editIcon = new ImageView(editImage);
        editIcon.setFitWidth(18); // Set the width of the image
        editIcon.setFitHeight(18); // Set the height of the image

        // Apply color adjustments to the icon
        ColorAdjust editColorAdjust = new ColorAdjust();
        editColorAdjust.setSaturation(0); // Remove color saturation to make it grayscale
        editColorAdjust.setBrightness(1); // Set brightness to maximum
        editColorAdjust.setContrast(1); // Normal contrast
        editIcon.setEffect(editColorAdjust);

        // Create the Edit button
        Button editButton = new Button();
        editButton.setGraphic(editIcon);
        editButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Hover effect for Edit button
        editButton.setOnMouseEntered(event -> editButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10;"));
        editButton.setOnMouseExited(event -> editButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10;"));
        editButton.setOnAction(e -> openEditPane(index, website, username, password, comment));

        // --- Delete Button Setup ---
        // Create ImageView for the delete button icon
        Image deleteImage = new Image(Main.class.getResourceAsStream("delete.png"));
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setFitWidth(20); // Set the width of the image
        deleteIcon.setFitHeight(18); // Set the height of the image

        // Apply color adjustments to the icon
        ColorAdjust deleteColorAdjust = new ColorAdjust();
        deleteColorAdjust.setSaturation(0); // Remove color saturation to make it grayscale
        deleteColorAdjust.setBrightness(1); // Set brightness to maximum
        deleteColorAdjust.setContrast(1); // Normal contrast
        deleteIcon.setEffect(deleteColorAdjust);

        // Create the Delete button
        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Hover effect for Delete button
        deleteButton.setOnMouseEntered(event -> deleteButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10;"));
        deleteButton.setOnMouseExited(event -> deleteButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10;"));
        deleteButton.setOnAction(e -> showDeleteConfirmation(index));

        // Add both buttons to the buttonBox
        buttonBox.getChildren().addAll(editButton, deleteButton);

        // Add the buttonBox to the vBox
        vBox.getChildren().add(buttonBox);

        block.getChildren().add(vBox);
        AnchorPane.setTopAnchor(vBox, 10.0);
        AnchorPane.setBottomAnchor(vBox, 10.0);
        AnchorPane.setLeftAnchor(vBox, 10.0);
        AnchorPane.setRightAnchor(vBox, 10.0);

        updateTotalEntries();
        return block;
    }

    private void showDeleteConfirmation(int index) {
        // Create the confirmation pane
        AnchorPane confirmationPane = new AnchorPane();
        confirmationPane.setStyle("-fx-background-color: #1d011e; -fx-border-radius: 15; -fx-background-radius: 15;");
        confirmationPane.setPrefSize(300, 200);

        // Create a background blur effect
        backgroundOverlay = new Rectangle(displayPageAnchor.getWidth(), displayPageAnchor.getHeight());
        backgroundOverlay.setFill(Color.color(0, 0, 0, 0.5));  // Semi-transparent black
        backgroundOverlay.setEffect(new GaussianBlur(10));  // Applying blur effect

        // Add the background overlay to the display container (displayPageAnchor)
        displayPageAnchor.getChildren().add(backgroundOverlay);

        // Add the message label
        Label messageLabel = new Label("Are you sure you want to delete?");
        messageLabel.wrapTextProperty().set(true);
        messageLabel.setStyle("-fx-text-fill: #d9d8d8; -fx-font-size: 16px; -fx-font-weight: bold;");
        AnchorPane.setTopAnchor(messageLabel, 40.0);
        AnchorPane.setLeftAnchor(messageLabel, 40.0);
        AnchorPane.setRightAnchor(messageLabel, 40.0);

        // Create Yes and No buttons
        Button yesButton = new Button("Yes");
        yesButton.setStyle("-fx-background-color: #534e53; -fx-text-fill: #d9d8d8; -fx-border-radius: 10; -fx-background-radius: 10;");
        yesButton.setOnMouseEntered(event -> yesButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        yesButton.setOnMouseExited(event -> yesButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        yesButton.setOnAction(e -> deleteEntry(index, confirmationPane));

        Button noButton = new Button("No");
        noButton.setStyle("-fx-background-color: #534e53; -fx-text-fill: #d9d8d8; -fx-border-radius: 10; -fx-background-radius: 10;");
        noButton.setOnMouseEntered(event -> noButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        noButton.setOnMouseExited(event -> noButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #d9d8d8;"));
        noButton.setOnAction(e -> {
            displayPageAnchor.getChildren().remove(confirmationPane);
            displayPageAnchor.getChildren().remove(backgroundOverlay);  // Remove the overlay when closed
        });

        HBox buttonBox = new HBox(20, yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);
        AnchorPane.setBottomAnchor(buttonBox, 40.0);
        AnchorPane.setLeftAnchor(buttonBox, 20.0);
        AnchorPane.setRightAnchor(buttonBox, 20.0);

        confirmationPane.getChildren().addAll(messageLabel, buttonBox);

        // Add the confirmation pane to the display container
        displayPageAnchor.getChildren().add(confirmationPane);

        // Center the confirmation pane in the displayContainer
        AnchorPane.setTopAnchor(confirmationPane, (displayPageAnchor.getHeight() - confirmationPane.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(confirmationPane, (displayPageAnchor.getWidth() - confirmationPane.getPrefWidth()) / 2);
    }

    private void deleteEntry(int index, AnchorPane confirmationPane) {
        // Remove the confirmation pane from the display
        displayPageAnchor.getChildren().remove(confirmationPane);
        displayPageAnchor.getChildren().remove(backgroundOverlay);

        // File paths
        String filePath = userHome +"\\AppData\\Roaming\\EntriesData\\data.txt";
        File file = new File(filePath);

        if (!file.exists()) {
            holdAndShowError("Data file not found.", "err");
            return;
        }

        List<String> lines = new ArrayList<>();

        // Read all lines into memory
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            holdAndShowError("Error reading the data file.", "err");
            return;
        }

        // Now, let's identify and remove the entry to be deleted
        int currentIndex = 0;
        boolean isDeletingEntry = false;
        int lineCount = 0;
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("Web.")) {
                if (currentIndex == index) {
                    // We are at the entry to delete, so skip the next 5 lines
                    isDeletingEntry = true;
                    lineCount = 0;
                } else {
                    isDeletingEntry = false;
                }
            }

            // Add line to the updated list if it's not part of the deleted entry
            if (!isDeletingEntry) {
                updatedLines.add(line);
            }

            // Stop skipping after the 5 lines of the entry
            if (isDeletingEntry) {
                lineCount++;
                if (lineCount == 5) {
                    isDeletingEntry = false;  // Stop deleting after 5 lines (the entry)
                }
            }

            if (line.equals("----")) {
                if (!isDeletingEntry) {
                    currentIndex++;  // Increment the index only for completed entries
                }
            }
        }

        // Rewrite the file with the updated lines
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String updatedLine : updatedLines) {
                pw.println(updatedLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            holdAndShowError("Error writing to the data file.", "err");
            return;
        }

        // Refresh the display after deletion
        showRecentEntries();
        updateTotalEntries();
        displayPasswords();
    }

    private void openEditPane(int index, String website, String username, String password, String comment) {
        // Store the index of the entry being edited
        editingIndex = index;

        // Hide the display container and show the edit pane
        displayPageAnchor.setVisible(false);
        editPane.setVisible(true);

        // Populate the edit fields with the entry data
        editWebsiteField.setText(website);
        editUsernameField.setText(username);
        editPasswordField.setText(password);
        editCommentField.setText(comment);
    }

    public void saveEditedEntry() {
        // Get the edited data from the fields
        String editedWebsite = editWebsiteField.getText().trim();
        String editedUsername = editUsernameField.getText().trim();
        String editedPassword = editPasswordField.getText().trim();
        String editedComment = editCommentField.getText().trim(); // Optional field

        // Validation checks for empty fields
        if (editedWebsite.isEmpty()) {
            holdAndShowError("Website name cannot be empty.", "err5");
            return;
        }
        if (editedUsername.isEmpty()) {
            holdAndShowError("Username cannot be empty.", "err5");
            return;
        }
        if (editedPassword.isEmpty()) {
            holdAndShowError("Password cannot be empty.", "err5");
            return;
        }

        // If all fields are valid, proceed to update the data in the file
        updateEntryInFile(editingIndex, editedWebsite, editedUsername, editedPassword, editedComment);

        // Reset the edit index and hide the edit pane
        editingIndex = -1;
        editPane.setVisible(false);
        displayPageAnchor.setVisible(true);

        // Refresh the displayed data
        displayPasswords();

        // Show success message
        holdAndShowError("Entry updated successfully!", "succ5");
    }

    public void cancelEdit() {
        // Simply reset the edit index and hide the edit pane
        editingIndex = -1;
        editPane.setVisible(false);
        displayPageAnchor.setVisible(true);

        // Refresh the displayed data
        displayPasswords();
    }

    private void updateEntryInFile(int index, String website, String username, String password, String comment) {
        String filePath = userHome +"\\AppData\\Roaming\\EntriesData\\data.txt";
        File file = new File(filePath);

        if (!file.exists()) {
            holdAndShowError("Data file not found.", "err");
            return;
        }

        try {
            // Read all lines from the file
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));

            // Decrypt the master key
            String decryptedMasterKey = getDecryptedMasterKey();

            // Locate the start and end indices of the entry
            int currentIndex = -1; // Track the entry index while iterating
            int entryStart = -1;   // Start line of the entry to update
            int entryEnd = -1;     // End line of the entry to update

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.equals("----")) {
                    currentIndex++;
                    if (currentIndex == index) {
                        entryEnd = i; // Set the end of the entry
                        break;
                    }
                    entryStart = i + 1; // The next line after "----"
                }
            }

            // Handle the first entry case
            if (index == 0 && entryStart == -1) {
                entryStart = 0; // Start from the first line
            }

            if (entryStart == -1 || entryEnd == -1) {
                holdAndShowError("Failed to locate the entry to update.", "err");
                return;
            }

            // Encrypt the updated data
            String encryptedWebsite = encryptData(website, decryptedMasterKey);
            String encryptedUsername = encryptData(username, decryptedMasterKey);
            String encryptedPassword = encryptData(password, decryptedMasterKey);
            String encryptedComment = encryptData(comment, decryptedMasterKey);

            // Prepare the updated lines for the entry
            List<String> updatedEntry = new ArrayList<>();
            updatedEntry.add("Web." + encryptedWebsite);
            updatedEntry.add("Usnm." + encryptedUsername);
            updatedEntry.add("Ps." + encryptedPassword);
            updatedEntry.add("Cmt." + encryptedComment);
            updatedEntry.add("----");

            // Replace the lines in the file
            lines.subList(entryStart, entryEnd + 1).clear(); // Remove old entry
            lines.addAll(entryStart, updatedEntry);          // Insert updated entry

            // Write the updated lines back to the file
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);

            holdAndShowError("Entry updated successfully!", "info");

        } catch (Exception e) {
            e.printStackTrace();
            holdAndShowError("Error updating the entry.", "err");
        }
    }

    private HBox createLabeledButton(String labelText, String value) {
        // Create the label for the text (e.g., Website, Username, etc.)
        Label label = new Label(labelText + ":");
        label.setStyle("-fx-text-fill: #d9d8d8; -fx-font-weight: bold;");

        // Create the value label (e.g., the actual website name, username, etc.)
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #d9d8d8;");

        // Create ImageView for the copy button icon
        Image copyImage = new Image(Main.class.getResourceAsStream("copy.png"));
        ImageView copyIcon = new ImageView(copyImage);
        copyIcon.setFitWidth(20); // Set the width of the image
        copyIcon.setFitHeight(18); // Set the height of the image

        // Apply a ColorAdjust effect to change the icon color to white
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(0); // Remove color saturation to make it grayscale
        colorAdjust.setBrightness(1); // Set brightness to maximum
        colorAdjust.setContrast(1); // Normal contrast
        copyIcon.setEffect(colorAdjust);

        // Create a button with the copy icon
        Button copyButton = new Button();
        copyButton.setGraphic(copyIcon);
        copyButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Boolean flag to check if the button is in the "pressed" state
        BooleanProperty isPressed = new SimpleBooleanProperty(false);

        // Button hover effect
        copyButton.setOnMouseEntered(event -> {
            if (!isPressed.get()) {
                copyButton.setStyle("-fx-background-color: #ab72c3; -fx-background-radius: 10; -fx-border-radius: 10;");
            }
        });
        copyButton.setOnMouseExited(event -> {
            if (!isPressed.get()) {
                copyButton.setStyle("-fx-background-color: #534e53; -fx-background-radius: 10; -fx-border-radius: 10;");
            }
        });

        // Copy action
        copyButton.setOnAction(e -> {
            copyToClipboard(value);

            // Set the "pressed" state to true
            isPressed.set(true);

            // Change the background color to green when pressed
            copyButton.setStyle("-fx-background-color: #28a745; -fx-border-radius: 10; -fx-background-radius: 10;");

            // Create a Timeline to reset the background color after .5 seconds (500 milliseconds)
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(500), event1 -> {
                        // Revert the background color to the original state
                        copyButton.setStyle("-fx-background-color: #534e53; -fx-border-radius: 10; -fx-background-radius: 10;");
                        // Set "pressed" flag back to false after 1 second
                        isPressed.set(false);
                    })
            );
            timeline.play(); // Start the timeline
        });

        // Create HBox and set spacing
        HBox hBox = new HBox(10); // Spacing between label, value, and button
        hBox.setAlignment(Pos.CENTER_LEFT); // Align contents to the left by default

        // Add label and value to the left
        hBox.getChildren().addAll(label, valueLabel);

        // Add a spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Ensures the spacer takes all available space
        hBox.getChildren().addAll(spacer, copyButton); // Add spacer and button to HBox

        // Disable focus traversal
        hBox.setFocusTraversable(false);
        label.setFocusTraversable(false);
        valueLabel.setFocusTraversable(false);

        return hBox;
    }

    public void ChangeMasterKey() throws Exception {

        prevMasterKey = prevMasterKeyField.getText();
        newMasterKey = newMasterKeyField.getText();
        confirmMasterKey = confirmMasterKeyField.getText();

        prevMasterKey = padKey(prevMasterKey,16);

        String currentMasterKey = getDecryptedMasterKey();

        // Validate inputs
        if (prevMasterKeyField.getText().isEmpty()) {
            holdAndShowError("Please Enter Your Previous Master Key", "err3");
            return;
        } else if (newMasterKeyField.getText().isEmpty()) {
            holdAndShowError("Please Enter A New Master Key", "err3");
            return;
        } else if (confirmMasterKeyField.getText().isEmpty()) {
            holdAndShowError("Please Confirm Your Master Key", "err3");
            return;
        } else if (!prevMasterKey.equals(currentMasterKey)) {
            holdAndShowError("Previous Master Key Doesn't Match", "err3");
            return;
        } else if (!newMasterKey.equals(confirmMasterKey)) {
            holdAndShowError("New Keys Don't Match!", "err3");
            return;
        }

        String folderPath = userHome +"\\AppData\\Roaming\\EntriesData";
        String filePath = folderPath + "\\data.txt";

        File dataFile = new File(filePath);

        if (!dataFile.exists()) {
            holdAndShowError("No data found to update.", "err3");
            return;
        }

        newMasterKey = padKey(newMasterKey, 16);

        // Read, decrypt, re-encrypt, and rewrite all data
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            StringBuilder updatedData = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Web.")) {
                    String decryptedWebsite = decryptData(line.substring(4), currentMasterKey);
                    updatedData.append("Web.").append(encryptData(decryptedWebsite, newMasterKey)).append("\n");
                } else if (line.startsWith("Usnm.")) {
                    String decryptedUsername = decryptData(line.substring(5), currentMasterKey);
                    updatedData.append("Usnm.").append(encryptData(decryptedUsername, newMasterKey)).append("\n");
                } else if (line.startsWith("Ps.")) {
                    String decryptedPassword = decryptData(line.substring(3), currentMasterKey);
                    updatedData.append("Ps.").append(encryptData(decryptedPassword, newMasterKey)).append("\n");
                } else if (line.startsWith("Cmt.")) {
                    String decryptedComment = decryptData(line.substring(4), currentMasterKey);
                    updatedData.append("Cmt.").append(encryptData(decryptedComment, newMasterKey)).append("\n");
                } else {
                    updatedData.append(line).append("\n"); // Preserve separators or blank lines
                }
            }

            // Write the updated data back to the file
            try (FileWriter writer = new FileWriter(dataFile, false)) {
                writer.write(updatedData.toString());
            }

        } catch (Exception e) {
            holdAndShowError("An error occurred while updating your data.", "err3");
            e.printStackTrace();
            return;
        }

        // Update the master key in Checks.txt
        try {
            File checksFile = new File(userHome +"\\AppData\\Roaming\\Checks.txt");
            String username = WelcomePageController.getUsername(); // Assuming this method exists to retrieve the username
            String encryptedNewMasterKey = WelcomePageController.encrypt(newMasterKey);

            try (FileWriter writer = new FileWriter(checksFile, false)) {
                writer.write(username + ":" + encryptedNewMasterKey);
            }

        } catch (IOException e) {
            holdAndShowError("Failed to update the master key in Checks.txt.", "err3");
            e.printStackTrace();
            return;
        }

        // Clear input fields and show success message
        prevMasterKeyField.clear();
        newMasterKeyField.clear();
        confirmMasterKeyField.clear();

        holdAndShowError("Master Key changed successfully!", "succ3");
    }

    // AES encryption function
    public static String encryptData(String input, String decryptedMasterKey) throws Exception {
        // Convert the decrypted master key to a byte array
        SecretKeySpec keySpec = new SecretKeySpec(decryptedMasterKey.getBytes(), "AES");

        // Create the cipher and initialize it for encryption
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // Encrypt the input string
        byte[] encryptedBytes = cipher.doFinal(input.getBytes("UTF-8"));

        // Return the encrypted data as a Base64 string
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES decryption function
    public static String decryptData(String encryptedInput, String decryptedMasterKey) throws Exception {
        // Convert the decrypted master key to a byte array
        SecretKeySpec keySpec = new SecretKeySpec(decryptedMasterKey.getBytes(), "AES");

        // Create the cipher and initialize it for decryption
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // Decode the Base64 encrypted input
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedInput);

        // Decrypt the bytes
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Return the decrypted data as a string
        return new String(decryptedBytes, "UTF-8");
    }

    public static String padKey(String key, int length) {
        while (key.length() < length) {
            key += "0"; // Padding with zeros (you can choose other padding strategies if needed)
        }
        return key.substring(0, length); // Truncate if necessary
    }

    private void holdAndShowError(String message, String func) {
        if (func.equals("err")) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else if (func.equals("err3")) {
            errorLabel3.setText(message);
            errorLabel3.setVisible(true);
        } else if (func.equals("succ")) {
            succesLabel.setText(message);
            succesLabel.setVisible(true);
        } else if (func.equals("succ3")) {
            succesLabel3.setText(message);
            succesLabel3.setVisible(true);
        } else if (func.equals("err5")) {
            errorLabel5.setText(message);
            errorLabel5.setVisible(true);
        } else if (func.equals("succ5")) {
            succesLabel5.setText(message);
            succesLabel5.setVisible(true);
        }

        // Create a PauseTransition to hide the error or success label after 2 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            if (func.equals("err")) {
                errorLabel.setVisible(false);
            } else if (func.equals("err3")) {
                errorLabel3.setVisible(false);
            } else if (func.equals("succ")) {
                succesLabel.setVisible(false);
            } else if (func.equals("succ3")) {
                succesLabel3.setVisible(false);
            } else if (func.equals("err5")) {
                errorLabel5.setVisible(false);
            } else if (func.equals("succ5")) {
                succesLabel5.setVisible(false);
            }
        });

        // Start the pause transition
        pause.play();
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
        holdAndShowError("Copied to clipboard!", "succ");
    }

    public void initializeUI() {
        displayContainer.setStyle("-fx-background-color: #180119;"); // Background for AnchorPane
        displayContainer.setFocusTraversable(false);

        scrollPane.setStyle("-fx-background: #180119; -fx-border-color: transparent;");
        scrollPane.setPrefHeight(700); // You can change this value as per your design
        scrollPane.setFitToWidth(true);
        scrollPane.setFocusTraversable(false);

        homePageAnchor.setStyle("-fx-focus-color: transparent;");
        displayPageAnchor.setStyle("-fx-focus-color: transparent;");
        createPageAnchor.setStyle("-fx-focus-color: transparent;");
        changeMyKeyPageAnchor.setStyle("-fx-focus-color: transparent;");
    }

    public static String getDecryptedMasterKey() throws Exception {
        String masterKey = readAndDecryptMasterKeyFromFile();
        return padKey(masterKey, 16); // Pad the key as needed
    }

    public void CloseWindow(ActionEvent event){
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void MinimizeWindow(ActionEvent event){
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    public void GoToHomePage(){
        hideAllPages();
        homePageAnchor.setVisible(true);
        showRecentEntries();
        updateTotalEntries();
        setActiveButton(homeButton);  // Set home button as active
    }

    public void GoToCreatePage(){
        hideAllPages();
        createPageAnchor.setVisible(true);
        setActiveButton(createButtonNav);  // Set create button as active
    }

    public void GoToDisplayPage(){
        hideAllPages();
        displayPageAnchor.setVisible(true);
        displayPasswords();
        setActiveButton(displayButton);  // Set display button as active
    }

    public void GoToChangeMyKeyPage(){
        hideAllPages();
        changeMyKeyPageAnchor.setVisible(true);
        setActiveButton(changeMyKeyButton);  // Set change key button as active
    }

    public void GoToAboutUsPage() {
        hideAllPages();
        aboutUsPageAnchor.setVisible(true);

        // Reset positions and visibility for animations
        resetElements();

        // Create animations for aboutUsLabel1
        SequentialTransition label1Transition = createFadeAndTranslateAnimation(aboutUsLabel1, 0);

        // Create animations for aboutUsBg
        SequentialTransition bgTransition = createFadeAndTranslateAnimation(aboutUsBg, 50);

        // Create animations for aboutUsLabel2
        SequentialTransition label2Transition = createFadeAndTranslateAnimation(aboutUsLabel2, 100);

        // Create animations for aboutUsLabel3
        SequentialTransition label3Transition = createFadeAndTranslateAnimation(aboutUsLabel3, 150);

        SequentialTransition label4Transition = createFadeAndTranslateAnimation(aboutUsLabel4, 200);

        // Play animations in sequence
        SequentialTransition allTransitions = new SequentialTransition(
                label1Transition,
                bgTransition,
                label2Transition,
                label3Transition,
                label4Transition
        );

        allTransitions.setOnFinished(event -> {
            try {
                setActiveButton(extraButton);
            } catch (Exception e) {
                System.out.println("just ignore " + e);
            }
        });

        allTransitions.play();
    }

    private void resetElements() {
        aboutUsLabel1.setOpacity(0);
        aboutUsLabel2.setOpacity(0);
        aboutUsLabel3.setOpacity(0);
        aboutUsLabel4.setOpacity(0);
        aboutUsBg.setOpacity(0);

        aboutUsLabel1.setTranslateY(20);
        aboutUsLabel2.setTranslateY(20);
        aboutUsLabel3.setTranslateY(20);
        aboutUsLabel4.setTranslateY(20);
        aboutUsBg.setTranslateY(20);
    }

    private SequentialTransition createFadeAndTranslateAnimation(Object node, int delayMillis) {
        // Fade transition
        FadeTransition fade = new FadeTransition(Duration.millis(120), (javafx.scene.Node) node);
        fade.setFromValue(0);
        fade.setToValue(1);

        // Translate transition
        TranslateTransition translate = new TranslateTransition(Duration.millis(220), (javafx.scene.Node) node);
        translate.setFromY(20);
        translate.setToY(0);

        // Combine transitions
        SequentialTransition transition = new SequentialTransition(
                fade,
                translate
        );
        transition.setDelay(Duration.millis(delayMillis));
        return transition;
    }

    private void hideAllPages() {
        homePageAnchor.setVisible(false);
        createPageAnchor.setVisible(false);
        displayPageAnchor.setVisible(false);
        changeMyKeyPageAnchor.setVisible(false);
        aboutUsPageAnchor.setVisible(false);
    }

    public void clearFields(){
        // Clear input fields after saving
        websiteNameTextField.clear();
        usernameTextField.clear();
        passwordPassField.clear();
        commentTextField.clear();
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons to normal state
        homeButton.getStyleClass().remove("active");
        createButtonNav.getStyleClass().remove("active");
        displayButton.getStyleClass().remove("active");
        changeMyKeyButton.getStyleClass().remove("active");

        // Set the selected button to active
        activeButton.getStyleClass().add("active");
    }

}
