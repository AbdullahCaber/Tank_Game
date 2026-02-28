import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Entry point of the TANK2025 game application.
public class Main extends Application {
    // Called automatically when the JavaFX application is launched.
    @Override
    public void start(Stage primaryStage) {
        // Create the main game controller (root node)
        GameManager gameManager = new GameManager();

        // Set up the scene with specified dimensions
        Scene scene = new Scene(gameManager, 800, 700);

        // Configure and display the main window
        primaryStage.setTitle("TANK2025");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start the game logic and input handling
        gameManager.startGame(scene);
    }

    // Standard main method to launch the application.
    public static void main(String[] args) {
        launch(args);
    }
}
