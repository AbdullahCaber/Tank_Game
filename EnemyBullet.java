import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;

// The EnemyBullet class represents a projectile fired by an enemy tank.
public class EnemyBullet extends ImageView {
    // Fields
    private static final int SPEED = 5;
    private AnimationTimer timer;
    private boolean paused = false;

    // Constructs an EnemyBullet at the specified location and direction.
    public EnemyBullet(double x, double y, String direction, GameManager gameManager) {
        Image bulletImage = new Image("file:assets/bullet.png");
        setImage(bulletImage);
        setFitWidth(10);
        setFitHeight(13);
        setTranslateX(x);
        setTranslateY(y);

        int xVelocity = 0;
        int yVelocity = 0;
        int rotationAngle = 0;

        // Determine movement vector and rotation angle based on direction
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

        // Add bullet to game scene
        gameManager.getChildren().add(this);

        // AnimationTimer to move the bullet each frame
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused) return;

                // Move bullet in the specified direction
                setTranslateX(getTranslateX() + finalXVelocity * SPEED);
                setTranslateY(getTranslateY() + finalYVelocity * SPEED);

                // Remove bullet if it leaves screen boundaries
                if (getTranslateX() < 0 || getTranslateX() > 800 ||
                        getTranslateY() < 0 || getTranslateY() > 700) {
                    stop();
                    gameManager.getChildren().remove(EnemyBullet.this);
                    return;
                }

                // Collision detection with player and walls
                for (Node node : gameManager.getChildren()) {
                    // Hit the player
                    if (node instanceof Player &&
                            getBoundsInParent().intersects(node.getBoundsInParent())) {
                        stop();
                        gameManager.getChildren().remove(EnemyBullet.this);
                        gameManager.hitPlayer();
                        break;
                    }

                    // Hit a wall
                    if (node instanceof Wall &&
                            getBoundsInParent().intersects(node.getBoundsInParent())) {
                        stop();
                        new SmallExplosion(getTranslateX(), getTranslateY(), gameManager);
                        gameManager.getChildren().remove(EnemyBullet.this);
                        break;
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
