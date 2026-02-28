import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.Random;
import javafx.scene.Node;

// The Enemy class represents a tank that moves in random directions
public class Enemy extends ImageView implements GameObject {
    // Fields
    private static final int SPEED = 1;
    private static final int SIZE = 32;
    private Image sprite1;
    private Image sprite2;
    private String direction;
    private AnimationTimer timer;
    private Timeline shootTimer;
    private Timeline directionChangeTimer;
    private boolean paused = false;
    private boolean toggle = false;

    // Constructs a new enemy tank at the given (x, y) coordinates and initializes its behavior.
    public Enemy(double x, double y, GameManager gameManager) {
        // Load and set tank sprites
        sprite1 = new Image("file:assets/whiteTank1.png");
        sprite2 = new Image("file:assets/whiteTank2.png");
        setImage(sprite1);
        setFitWidth(SIZE);
        setFitHeight(SIZE);
        setTranslateX(x);
        setTranslateY(y);

        // Initialize direction and start shooting and movement logic
        chooseNewDirection();
        fire(gameManager);
        startDirectionChangeTimer();

        // Frame-based animation timer for movement
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) move();
            }
        };
        timer.start();

        gameManager.getChildren().add(this);
    }

    // Moves the enemy tank in its current direction.
    public void move() {
        double nextX = getTranslateX();
        double nextY = getTranslateY();

        // Calculate next position based on direction
        switch (direction) {
            case "UP": {
                nextY -= SPEED;
                break;
            }
            case "DOWN": {
                nextY += SPEED;
                break;
            }
            case "LEFT": {
                nextX -= SPEED;
                break;
            }
            case "RIGHT": {
                nextX += SPEED;
                break;
            }
        }

        if (!(getParent() instanceof GameManager)) return;

        GameManager gameManager = (GameManager) getParent();
        boolean canMove = true;

        // Check for wall collision
        for (Node node : gameManager.getChildren()) {
            if (node instanceof Wall) {
                double wallX = node.getTranslateX();
                double wallY = node.getTranslateY();
                double wallWidth = Wall.WIDTH;
                double wallHeight = Wall.HEIGHT;

                boolean overlapsWall =
                        nextX < wallX + wallWidth &&
                                nextX + getFitWidth() > wallX &&
                                nextY < wallY + wallHeight &&
                                nextY + getFitHeight() > wallY;

                if (overlapsWall) {
                    canMove = false;
                    break;
                }
            }
        }

        // Move and toggle sprite if no collision
        if (canMove) {
            setTranslateX(nextX);
            setTranslateY(nextY);
            toggle = !toggle;
            setImage(toggle ? sprite2 : sprite1); // Simulate movement animation
        } else {
            chooseNewDirection(); // Pick a new direction if movement blocked
        }
    }

    // Randomly selects a new direction and updates tank rotation accordingly.
    public void chooseNewDirection() {
        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};
        Random rand = new Random();
        direction = directions[rand.nextInt(directions.length)];

        // Adjust rotation to match movement direction
        switch (direction) {
            case "UP": {
                setRotate(270);
                break;
            }
            case "DOWN": {
                setRotate(90);
                break;
            }
            case "LEFT": {
                setRotate(180);
                break;
            }
            case "RIGHT": {
                setRotate(0);
                break;
            }
        }
    }

    // Starts a timer that changes the enemy's direction at random intervals.
    private void startDirectionChangeTimer() {
        Random rand = new Random();
        double interval = 1.0 + rand.nextDouble() * 1.5;

        directionChangeTimer = new Timeline(new KeyFrame(Duration.seconds(interval), e -> {
            if (!paused) {
                chooseNewDirection();
            }
            startDirectionChangeTimer(); // Restart timer for continuous updates
        }));
        directionChangeTimer.setCycleCount(1);
        directionChangeTimer.play();
    }

    // Returns the current movement direction.
    public String getDirection() {
        return direction;
    }

    // Starts a timer that fires bullets in the current direction.
    public void fire(GameManager gameManager) {
        Random rand = new Random();
        double interval = 1.2 + rand.nextDouble() * 1.6;

        shootTimer = new Timeline(new KeyFrame(Duration.seconds(interval), e -> {
            if (!paused) {
                // Calculate bullet spawn position from center of tank
                double bulletX = getTranslateX() + getFitWidth() / 2 - 5;
                double bulletY = getTranslateY() + getFitHeight() / 2 - 5;
                new EnemyBullet(bulletX, bulletY, getDirection(), gameManager);
            }
            fire(gameManager); // Re-trigger to continue firing
        }));
        shootTimer.setCycleCount(1);
        shootTimer.play();
    }

    // Destroys the enemy with explosion effects.
    public void destroy() {
        destroy(false);
    }

    // Destroys the enemy and optionally skips explosion effects.
    public void destroy(boolean silent) {
        if (timer != null) timer.stop();
        if (shootTimer != null) shootTimer.stop();
        if (directionChangeTimer != null) directionChangeTimer.stop();

        if (!silent) {
            new Explosion(getTranslateX(), getTranslateY(), (GameManager) getParent());
            if (getParent() instanceof GameManager) {
                ((GameManager) getParent()).enemyDestroyed(this);
            }
        }

        if (getParent() instanceof GameManager) {
            ((GameManager) getParent()).getChildren().remove(this);
        }
    }

    //Pauses or resumes enemy behavior including movement, firing, and direction changes.
    public void setPaused(boolean value) {
        paused = value;

        if (shootTimer != null) {
            if (value) shootTimer.pause();
            else shootTimer.play();
        }

        if (directionChangeTimer != null) {
            if (value) directionChangeTimer.pause();
            else directionChangeTimer.play();
        }
    }
}
