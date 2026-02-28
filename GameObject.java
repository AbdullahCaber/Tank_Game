// The GameObject interface defines common behaviors for all game entities that can move or be destroyed.
// Makes it easier to implement new game objects such as second player or a new enemy type.
public interface GameObject {

    // Optional method for objects that support parameterless movement
    default void move() {}

    // Optional method for objects that move in a specific direction.
    default void move(String direction) {}

    // Required method to handle the destruction of the game object.
    void destroy();
}
