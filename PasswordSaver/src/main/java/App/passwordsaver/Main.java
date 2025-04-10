package App.passwordsaver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {

    public static Stage stageCopy;

    @Override
    public void start(Stage stage) throws IOException {

        Scene welcomePageScene = new Scene(FXMLLoader.load(Main.class.getResource("WelcomePage.fxml")));

        Image appIcon = new Image(getClass().getResourceAsStream("/icon.png"));
        stage.getIcons().add(appIcon);

        //removing top bar
        stage.initStyle(StageStyle.UNDECORATED);

        stageCopy = stage;
        stage.setScene(welcomePageScene);
        stage.show();

    }

    public static void main(String[] args) {

        System.setProperty("prism.dirtyopts", "false");
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.pixelshader", "true");
        System.setProperty("prism.forceGPU", "true");

        // Set font smoothing and rendering for clearer fonts
        System.setProperty("javafx.fonts.smoothing.type", "lcd");

        // Force use of hardware acceleration (GPU) for better graphics rendering
        System.setProperty("prism.forceGPU", "true");

        // Ensure the application uses the correct rendering engine for graphics
        System.setProperty("prism.order", "direct3d"); // for Windows (Direct3D)

        launch();
    }
}