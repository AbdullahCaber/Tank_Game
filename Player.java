import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;

// The Player class represents the controllable tank in the game.
public class Player extends ImageView implements GameObject {
    // Fields
    private static final double SPEED = 1;
    private static final int SIZE = 32;
    private static final long RELOAD_INTERVAL_MS = 500;
    private Image sprite1;
    private Image sprite2;
    private boolean spriteToggle = false;
    private String facingDirection = "RIGHT";
    private long lastFireTime = 0;

    // Constructs the player tank at the given (x, y) position.
    public Player(double x, double y) {
        sprite1 = new Image("file:assets/yellowTank1.png");
        sprite2 = new Image("file:assets/yellowTank2.png");

        setImage(sprite1);
        setFitWidth(SIZE);
        setFitHeight(SIZE);
        setTranslateX(x);
        setTranslateY(y);
    }

    // Moves the player tank in the specified direction if no wall collision occurs.
    @Override
    public void move(String direction) {
        this.facingDirection = direction;

        double nextX = getTranslateX();
        double nextY = getTranslateY();

        // Calculate next position and rotate sprite accordingly
        switch (direction) {
            case "UP":
                nextY -= SPEED;
                setRotate(270);
                break;
            case "DOWN":
                nextY += SPEED;
                setRotate(90);
                break;
            case "LEFT":
                nextX -= SPEED;
                setRotate(180);
                break;
            case "RIGHT":
                nextX += SPEED;
                setRotate(0);
                break;
        }

        boolean canMove = true;

        // Check collision with walls
        if (getParent() instanceof GameManager) {
            GameManager gamePane = (GameManager) getParent();

            for (Node node : gamePane.getChildren()) {
                if (node instanceof Wall) {
                    double wallX = node.getTranslateX();
                    double wallY = node.getTranslateY();

                    boolean horizontallyOverlapping = nextX + getFitWidth() > wallX &&
                            nextX < wallX + Wall.WIDTH;
                    boolean verticallyOverlapping = nextY + getFitHeight() > wallY &&
                            nextY < wallY + Wall.HEIGHT;
                    boolean collidesWithWall = horizontallyOverlapping && verticallyOverlapping;

                    if (collidesWithWall) {
                        canMove = false;
                        break;
                    }
                }
            }
        }

        // Apply movement and toggle sprite if no collision
        if (canMove) {
            setTranslateX(nextX);
            setTranslateY(nextY);
            spriteToggle = !spriteToggle;
            setImage(spriteToggle ? sprite2 : sprite1); // Simulate movement animation
        }
    }

    //Fires a bullet in the direction the player is currently facing.
    //Enforces a reload delay between shots.
    public void fire(GameManager gameManager) {
        long now = System.currentTimeMillis();
        if (now - lastFireTime < RELOAD_INTERVAL_MS) return;

        lastFireTime = now;

        // Bullet starts at center of player sprite
        double bulletX = getTranslateX() + getFitWidth() / 2 - 5;
        double bulletY = getTranslateY() + getFitHeight() / 2 - 5;

        new Bullet(bulletX, bulletY, facingDirection, gameManager);
    }

    // Triggers an explosion effect when the player is destroyed.
    @Override
    public void destroy() {
        new Explosion(getTranslateX(), getTranslateY(), (GameManager) getParent());
    }
}
