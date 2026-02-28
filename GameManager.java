import javafx.scene.layout.Pane;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Node;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.*;

// The GameManager class is the central controller of the game.
public class GameManager extends Pane {
    // Fields
    private Player player;
    private int score = 0;
    private int lives = 3;
    private Text scoreText;
    private Text livesText;
    private AnimationTimer inputHandler;
    private Pane gameOverOverlay;
    private Pane pauseOverlay;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private final int MAX_ENEMIES = 10;
    private List<Enemy> activeEnemies = new ArrayList<>();
    private Set<KeyCode> activeKeys = new HashSet<>();
    private List<PauseTransition> pendingEnemySpawns = new ArrayList<>();
    private PauseTransition pendingPlayerRespawn = null;

    // Constructor for GameManager.
    public GameManager() {
        this.setStyle("-fx-background-color: black;");
    }

    // Starts the game by spawning player/enemies, and setting up input.
    public void startGame(Scene scene) {
        // Initialize score and lives display
        scoreText = new Text("Score: 0");
        scoreText.setFont(new Font(20));
        scoreText.setFill(Color.WHITE);
        scoreText.setTranslateX(24);
        scoreText.setTranslateY(35);

        livesText = new Text("Lives: 3");
        livesText.setFont(new Font(20));
        livesText.setFill(Color.WHITE);
        livesText.setTranslateX(24);
        livesText.setTranslateY(55);

        this.getChildren().addAll(scoreText, livesText);
        this.setPrefSize(800, 700);

        // Spawn player
        player = new Player(400, 500);
        this.getChildren().add(player);

        // Create wall boundaries and obstacles
        createWalls();

        // Spawn initial enemies
        spawnInitialEnemies();

        // Input handling: key press
        scene.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());

            if (isGameOver) {
                if (e.getCode() == KeyCode.R) restartGame(scene);
                else if (e.getCode() == KeyCode.ESCAPE) System.exit(0);
                return;
            }

            if (isPaused) {
                if (e.getCode() == KeyCode.P) resumeGame();
                else if (e.getCode() == KeyCode.R) restartGame(scene);
                else if (e.getCode() == KeyCode.ESCAPE) System.exit(0);
                return;
            }

            if (e.getCode() == KeyCode.P) pauseGame();
        });

        // Input handling: key release
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        if (inputHandler != null) inputHandler.stop();

        // Per-frame input check
        inputHandler = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused || isGameOver || player == null) return;

                if (activeKeys.contains(KeyCode.LEFT)) player.move("LEFT");
                if (activeKeys.contains(KeyCode.RIGHT)) player.move("RIGHT");
                if (activeKeys.contains(KeyCode.UP)) player.move("UP");
                if (activeKeys.contains(KeyCode.DOWN)) player.move("DOWN");
                if (activeKeys.contains(KeyCode.X)) player.fire(GameManager.this);
            }
        };
        inputHandler.start();
    }

    // Builds the outer and inner wall layout of the game arena.
    private void createWalls() {
        // Borders
        for (int i = 0; i < 800; i += Wall.WIDTH) {
            this.getChildren().add(new Wall(i, 0));
            this.getChildren().add(new Wall(i, 700 - Wall.HEIGHT));
        }
        for (int i = Wall.HEIGHT; i < 700 - Wall.HEIGHT; i += Wall.HEIGHT) {
            this.getChildren().add(new Wall(0, i));
            this.getChildren().add(new Wall(800 - Wall.WIDTH, i));
        }

        // Interior walls (symmetrical design)
        for (int i = 0; i < 14; i++) {
            int y = 350 + i * Wall.HEIGHT;
            this.getChildren().add(new Wall(56, y));
            this.getChildren().add(new Wall(70, y));
        }

        for (int i = 0; i < 10; i++) {
            int y = 414 + i * Wall.HEIGHT;
            this.getChildren().add(new Wall(140, y));
            this.getChildren().add(new Wall(154, y));
        }

        for (int i = 0; i < 14; i++) {
            int x = 304 + i * Wall.WIDTH;
            this.getChildren().add(new Wall(x, 350));
        }

        for (int i = 0; i < 14; i++) {
            int y = 350 + i * Wall.HEIGHT;
            this.getChildren().add(new Wall(730, y));
            this.getChildren().add(new Wall(716, y));
        }

        for (int i = 0; i < 10; i++) {
            int y = 414 + i * Wall.HEIGHT;
            this.getChildren().add(new Wall(646, y));
            this.getChildren().add(new Wall(632, y));
        }
    }

    // Spawns a single enemy after a short randomized delay, avoiding overlaps.
    public void spawnEnemy() {
        final int maxAttempts = 50;
        final int cellWidth = Wall.WIDTH;
        final int cellHeight = Wall.HEIGHT;
        final int maxX = 800 / cellWidth;
        final int maxY = (700 / 2) / cellHeight;
        Random random = new Random();
        int attempts = 0;

        while (attempts < maxAttempts) {
            double spawnX = random.nextInt(maxX) * cellWidth;
            double spawnY = random.nextInt(maxY) * cellHeight;
            boolean isLocationValid = true;

            // Check for overlaps
            for (Node node : this.getChildren()) {
                if (node instanceof Wall || node instanceof Enemy || node instanceof Player) {
                    double nodeX = node.getTranslateX();
                    double nodeY = node.getTranslateY();
                    double nodeWidth = (node instanceof Wall) ? cellWidth : ((ImageView) node).getFitWidth();
                    double nodeHeight = (node instanceof Wall) ? cellHeight : ((ImageView) node).getFitHeight();
                    boolean horizontallyOverlapping = spawnX + 32 > nodeX && spawnX < nodeX + nodeWidth;
                    boolean verticallyOverlapping = spawnY + 32 > nodeY && spawnY < nodeY + nodeHeight;

                    if (horizontallyOverlapping && verticallyOverlapping) {
                        isLocationValid = false;
                        break;
                    }
                }
            }

            if (isLocationValid) {
                double delaySeconds = 1 + Math.random();
                PauseTransition respawnDelay = new PauseTransition(Duration.seconds(delaySeconds));
                pendingEnemySpawns.add(respawnDelay);

                respawnDelay.setOnFinished(e -> {
                    if (!isPaused) {
                        Enemy enemy = new Enemy(spawnX, spawnY, this);
                        activeEnemies.add(enemy);
                        pendingEnemySpawns.remove(respawnDelay);
                    }
                });
                respawnDelay.play();
                break;
            }

            attempts++;
        }
    }

    // Immediately spawns an enemy if space is available.
    public void spawnEnemyImmediate() {
        Random rand = new Random();
        int attempts = 0;

        while (attempts < 50) {
            double spawnX = rand.nextInt(800 / Wall.WIDTH) * Wall.WIDTH;
            double spawnY = rand.nextInt((700 / 2) / Wall.HEIGHT) * Wall.HEIGHT;
            boolean canSpawn = true;

            for (Node node : this.getChildren()) {
                if (node instanceof Wall || node instanceof Enemy || node instanceof Player) {
                    double nodeX = node.getTranslateX();
                    double nodeY = node.getTranslateY();
                    double nodeWidth = (node instanceof Wall) ? Wall.WIDTH : ((ImageView) node).getFitWidth();
                    double nodeHeight = (node instanceof Wall) ? Wall.HEIGHT : ((ImageView) node).getFitHeight();

                    boolean horizontallyOverlapping = spawnX + 32 > nodeX && spawnX < nodeX + nodeWidth;
                    boolean verticallyOverlapping = spawnY + 32 > nodeY && spawnY < nodeY + nodeHeight;

                    if (horizontallyOverlapping && verticallyOverlapping) {
                        canSpawn = false;
                        break;
                    }
                }
            }

            if (canSpawn) {
                Enemy enemy = new Enemy(spawnX, spawnY, this);
                activeEnemies.add(enemy);
                break;
            }

            attempts++;
        }
    }

    // Spawns the initial batch of enemies at game start.
    public void spawnInitialEnemies() {
        for (int i = 0; i < MAX_ENEMIES; i++)
            spawnEnemyImmediate();
    }

    // Called when an enemy is destroyed; spawns a new one.
    public void enemyDestroyed(Enemy e) {
        activeEnemies.remove(e);
        spawnEnemy();
    }

    // Increases score.
    public void addScore(int value) {
        score += value * 10;
        scoreText.setText("Score: " + score);
    }

    // Called when the player is hit by an enemy bullet.
    public void hitPlayer() {
        if (isGameOver) return;

        new Explosion(player.getTranslateX(), player.getTranslateY(), this);
        this.getChildren().remove(player);
        player = null;
        lives--;
        livesText.setText("Lives: " + lives);

        if (lives <= 0) {
            gameOver();
        } else {
            PauseTransition respawnDelay = new PauseTransition(Duration.seconds(1.5));
            pendingPlayerRespawn = respawnDelay;
            respawnDelay.setOnFinished(e -> {
                if (!isPaused) {
                    player = new Player(400, 500);
                    this.getChildren().add(player);
                    pendingPlayerRespawn = null;
                }
            });
            respawnDelay.play();
        }
    }

    // Displays pause overlay and stops all timers/animations.
    private void pauseGame() {
        isPaused = true;

        pauseOverlay = new Pane();
        pauseOverlay.setPrefSize(800, 700);

        Text pauseText = new Text("PAUSED");
        pauseText.setFont(new Font(40));
        pauseText.setFill(Color.WHITE);
        pauseText.setTranslateX(330);
        pauseText.setTranslateY(270);

        Text resumeText = new Text("Press P to Resume");
        Text restartText = new Text("Press R to Restart");
        Text exitText = new Text("Press Esc to Exit");

        resumeText.setFont(new Font(30));
        restartText.setFont(new Font(30));
        exitText.setFont(new Font(30));

        resumeText.setFill(Color.WHITE);
        restartText.setFill(Color.WHITE);
        exitText.setFill(Color.WHITE);

        resumeText.setTranslateX(280);
        resumeText.setTranslateY(395);
        restartText.setTranslateX(285);
        restartText.setTranslateY(435);
        exitText.setTranslateX(295);
        exitText.setTranslateY(475);

        pauseOverlay.getChildren().addAll(pauseText, resumeText, restartText, exitText);
        this.getChildren().add(pauseOverlay);

        pauseAllGameObjects(true);
    }

    // Resumes gameplay after pause.
    private void resumeGame() {
        isPaused = false;
        this.getChildren().remove(pauseOverlay);
        pauseOverlay = null;
        pauseAllGameObjects(false);
    }

    // Pauses/resumes all game objects and animations.
    private void pauseAllGameObjects(boolean pause) {
        for (Node node : this.getChildren()) {
            if (node instanceof Enemy) {
                ((Enemy) node).setPaused(pause);
            } else if (node instanceof Bullet) {
                ((Bullet) node).setPaused(pause);
            } else if (node instanceof EnemyBullet) {
                ((EnemyBullet) node).setPaused(pause);
            } else if (node instanceof Explosion) {
                if (pause) ((Explosion) node).pause(); else ((Explosion) node).resume();
            } else if (node instanceof SmallExplosion) {
                if (pause) ((SmallExplosion) node).pause(); else ((SmallExplosion) node).resume();
            }
        }

        if (pause) {
            if (pendingPlayerRespawn != null) pendingPlayerRespawn.pause();
            for (PauseTransition transition : pendingEnemySpawns) transition.pause();
        } else {
            if (pendingPlayerRespawn != null) pendingPlayerRespawn.play();
            for (PauseTransition transition : pendingEnemySpawns) transition.play();
        }
    }

    // Restarts the game.
    private void restartGame(Scene scene) {
        for (Node node : new ArrayList<>(getChildren())) {
            if (node instanceof Enemy) ((Enemy) node).destroy(true);
            else if (node instanceof GameObject) ((GameObject) node).destroy();
        }

        getChildren().clear();
        activeKeys.clear();
        isPaused = false;
        isGameOver = false;
        player = null;
        lives = 3;
        score = 0;

        startGame(scene);
    }

    // Displays Game Over screen and halts all gameplay.
    private void gameOver() {
        isGameOver = true;
        pauseAllGameObjects(true);

        gameOverOverlay = new Pane();
        gameOverOverlay.setPrefSize(800, 700);

        Text overText = new Text("GAME OVER");
        overText.setFont(new Font(40));
        overText.setFill(Color.DARKRED);
        overText.setTranslateX(290);
        overText.setTranslateY(270);

        Text scoreText = new Text("Your Score: " + score);
        scoreText.setFont(new Font(30));
        scoreText.setFill(Color.DARKRED);
        scoreText.setTranslateX(310);
        scoreText.setTranslateY(310);

        Text restartText = new Text("Press R to Restart");
        restartText.setFont(new Font(30));
        restartText.setFill(Color.DARKRED);
        restartText.setTranslateX(285);
        restartText.setTranslateY(395);

        Text exitText = new Text("Press Esc to Exit");
        exitText.setFont(new Font(30));
        exitText.setFill(Color.DARKRED);
        exitText.setTranslateX(295);
        exitText.setTranslateY(435);

        gameOverOverlay.getChildren().addAll(overText, scoreText, restartText, exitText);
        this.getChildren().add(gameOverOverlay);
    }
}
