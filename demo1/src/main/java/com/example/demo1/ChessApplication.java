// File: src/main/java/com/example/demo1/ChessApplication.java
package com.example.demo1;

import com.example.demo1.core.ConsoleUserInterface;
import com.example.demo1.core.GameController;
import com.example.demo1.core.GraphicalUserInterface;
import com.example.demo1.core.UserInterface;

/**
 * Main entry point for the chess application.
 */
public class ChessApplication {

    private final GameController gameController;
    private UserInterface userInterface;
    private boolean useGraphicalInterface;

    /**
     * Constructor for the Chess Application
     * @param useGraphicalInterface Flag to determine whether to use GUI or console interface
     */
    public ChessApplication(boolean useGraphicalInterface) {
        this.useGraphicalInterface = useGraphicalInterface;
        this.gameController = new GameController();

        if (useGraphicalInterface) {
            this.userInterface = new GraphicalUserInterface(this.gameController);
        } else {
            this.userInterface = new ConsoleUserInterface(this.gameController);
        }
    }

    /**
     * Initialize the application
     */
    public void initialize() {
        // Load configurations, initialize components
        gameController.initialize();
        userInterface.initialize();
    }

    /**
     * Start the application
     */
    public void start() {
        userInterface.display();
    }

    /**
     * Main method - entry point
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Parse command line arguments
        boolean useGraphicalInterface = true;

        if (args.length > 0 && args[0].equalsIgnoreCase("--console")) {
            useGraphicalInterface = false;
        }

        ChessApplication app = new ChessApplication(useGraphicalInterface);
        app.initialize();
        app.start();
    }
}

