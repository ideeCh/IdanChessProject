package com.example.demo1.core.ai;

import com.example.demo1.core.*;

/**
 * Manager class for integrating AI into the game
 */
public class AIManager {
    private final StaticChessAI chessAI;

    /**
     * Creates a new AI Manager
     */
    public AIManager() {
        this.chessAI = new StaticChessAI();
    }

    /**
     * Creates a new AI Manager with debug logging
     *
     * @param enableLogging Whether to enable logging of move evaluations
     */
    public AIManager(boolean enableLogging) {
        this.chessAI = new StaticChessAI(enableLogging);
    }

    /**
     * Get the best move for the current game state according to the AI
     *
     * @param gameState Current state of the game
     * @return The best move found by the AI, or null if no legal moves
     */
    public Move getBestMove(GameState gameState) {
        return chessAI.findBestMove(gameState);
    }

    /**
     * Make the AI move on the given game state
     *
     * @param gameState Current state of the game
     * @return The move that was made, or null if no legal moves
     */
    public Move makeAIMove(GameState gameState) {
        Move move = chessAI.findBestMove(gameState);

        if (move != null) {
            gameState.makeMove(move);
        }

        return move;
    }
}