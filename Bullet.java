import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;

// The Bullet class represents a projectile fired in a specific direction.
public class Bullet extends ImageView {
    // Fields
    private static final int SPEED = 5;
    private AnimationTimer timer;
    private boolean paused = false;

    // Constructs a Bullet object at the given (x, y) position, traveling in the specified direction.
    public Bullet(double x, double y, String direction, GameManager gameManager) {
        Image bulletImage = new Image("file:assets/bullet.png");
        setImage(bulletImage);
        setFitWidth(10);
        setFitHeight(13);
        setTranslateX(x);
        setTranslateY(y);

        int xVelocity = 0;
        int yVelocity = 0;
        int rotationAngle = 0;

        // Determine velocity vector and rotation angle based on direction
        switch (direction) {
            case "UP":
                yVelocity = -1;
                rotationAngle = 270;
                break;
            case "DOWN":
                yVelocity = 1;
                rotationAngle = 90;
                break;
            case "LEFT":
                xVelocity = -1;
                rotationAngle = 180;
                break;
            case "RIGHT":
                xVelocity = 1;
                rotationAngle = 0;
                break;
        }

        final int finalXVelocity = xVelocity;
        final int finalYVelocity = yVelocity;
        setRotate(rotationAngle);

        // Add the bullet to the game scene
        gameManager.getChildren().add(this);

        // Timer that moves the bullet each frame
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused){
                    return; // Do not update if the game is paused
                }

                // Update bullet's position
                setTranslateX(getTranslateX() + finalXVelocity * SPEED);
                setTranslateY(getTranslateY() + finalYVelocity * SPEED);

                // Remove bullet if it goes outside the game area
                if (getTranslateX() < 0 || getTranslateX() > 800 || getTranslateY() < 0 || getTranslateY() > 700) {
                    stop();
                    gameManager.getChildren().remove(Bullet.this);
                    return;
                }

                // Check for collision with Walls
                for (Node node : gameManager.getChildren()) {
                    if (node instanceof Wall && getBoundsInParent().intersects(node.getBoundsInParent())) {
                        stop();
                        new SmallExplosion(getTranslateX(), getTranslateY(), gameManager);
                        gameManager.getChildren().remove(Bullet.this);
                        return;
                    }

                    // Check for collision with Enemies
                    if (node instanceof Enemy && getBoundsInParent().intersects(node.getBoundsInParent())) {
                        stop();
                        ((GameObject) node).destroy(); // Remove or damage the enemy
                        gameManager.addScore(1);        // Increment score
                        gameManager.getChildren().remove(Bullet.this); // Remove bullet
                        return;
                    }
                }
            }
        };
        timer.start(); // Start bullet movement
    }

    // Pauses or resumes bullet movement.
    public void setPaused(boolean value) {
        this.paused = value;
    }
}
