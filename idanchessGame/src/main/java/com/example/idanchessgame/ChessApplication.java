package com.example.idanchessgame;

import com.example.idanchessgame.GameController;

/**
 * Main application class for the chess game.
 */
public class ChessApplication {

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        GameController controller = new GameController();
        controller.startGame();
    }
}