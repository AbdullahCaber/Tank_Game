import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

// The SmallExplosion class represents a visual effect for small explosions.
public class SmallExplosion extends ImageView {
    private PauseTransition transition;  // Controls how long the explosion stays visible

    // Creates and displays a small explosion at the specified (x, y) coordinates.
    public SmallExplosion(double x, double y, GameManager gameManager) {
        // Load the explosion image
        Image explosionImage = new Image("file:assets/smallExplosion.png");
        setImage(explosionImage);
        setFitWidth(32);
        setFitHeight(32);

        // Center the image around (x, y)
        setTranslateX(x - 16); // 32 / 2
        setTranslateY(y - 16); // 32 / 2

        // Add explosion to the game scene
        gameManager.getChildren().add(this);

        // Automatically remove the explosion after 0.3 seconds
        transition = new PauseTransition(Duration.seconds(0.3));
        transition.setOnFinished(e -> gameManager.getChildren().remove(this));
        transition.play();
    }

    // Pauses the explosion's disappearance.
    public void pause() {
        if (transition != null) {
            transition.pause();
        }
    }

    // Resumes the explosion's disappearance.
    public void resume() {
        if (transition != null) {
            transition.play();
        }
    }
}
