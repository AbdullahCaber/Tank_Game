import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

// The Explosion class represents an effect shown when a game object is destroyed.
public class Explosion extends ImageView {
    private PauseTransition transition; // Used to remove the explosion after a delay

    // Constructs an Explosion effect at the specified (x, y) location.
    public Explosion(double x, double y, GameManager gameManager) {
        // Load and configure the explosion image
        Image explosionImage = new Image("file:assets/explosion.png");
        setImage(explosionImage);
        setFitWidth(60);
        setFitHeight(68);

        // Position explosion centered on (x, y)
        setTranslateX(x - 30); // 60 / 2
        setTranslateY(y - 34); // 68 / 2

        // Add to game scene
        gameManager.getChildren().add(this);

        // Remove explosion after 0.5 seconds
        transition = new PauseTransition(Duration.seconds(0.5));
        transition.setOnFinished(e -> gameManager.getChildren().remove(this));
        transition.play();
    }

    // Pauses the explosion timer
    public void pause() {
        if (transition != null) {
            transition.pause();
        }
    }

    // Resumes the explosion timer
    public void resume() {
        if (transition != null) {
            transition.play();
        }
    }
}
