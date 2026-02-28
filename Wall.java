import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// The Wall class represents an immovable obstacle in the game world.
public class Wall extends ImageView {
    // Constants for wall dimensions
    public static final int WIDTH = 14;
    public static final int HEIGHT = 16;

    // Constructs a wall at the specified (x, y) location.
    public Wall(double x, double y) {
        // Load and apply the wall image
        Image wallImage = new Image("file:assets/wall.png");
        setImage(wallImage);

        // Set the wall size
        setFitWidth(WIDTH);
        setFitHeight(HEIGHT);

        // Position the wall in the game scene
        setTranslateX(x);
        setTranslateY(y);
    }
}