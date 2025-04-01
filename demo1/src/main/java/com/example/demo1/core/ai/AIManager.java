package com.example.demo1.core.ai;

import com.example.demo1.core.*;

/**
 * Simple AI Manager that connects to the StaticChessAI.
 * Acts as a bridge between GameController and the AI system.
 */
public class AIManager {

    private StaticChessAI chessAI;
    private boolean useLogging;

    /**
     * Constructor with default settings
     */
    public AIManager() {
        this(false);
    }

    /**
     * Constructor with logging option
     *
     * @param useLogging Whether to enable verbose logging
     */
    public AIManager(boolean useLogging) {
        this.useLogging = useLogging;
        this.chessAI = new StaticChessAI(useLogging);

        if (useLogging) {
            System.out.println("AI Manager initialized");
        }
    }

    /**
     * Get the best move for the current game state
     *
     * @param gameState Current state of the chess game
     * @return The best move found by the AI
     */
    public Move getBestMove(GameState gameState) {
        if (useLogging) {
            System.out.println("AI thinking...");
        }

        long startTime = System.currentTimeMillis();

        // Get the best move from the AI
        Move bestMove = chessAI.findBestMove(gameState);

        long endTime = System.currentTimeMillis();

        if (useLogging) {
            System.out.println("AI selected move: " + bestMove + " (in " + (endTime - startTime) + "ms)");
        }

        return bestMove;
    }

    /**
     * Enable or disable AI logging
     *
     * @param enabled Whether logging should be enabled
     */
    public void setLogging(boolean enabled) {
        this.useLogging = enabled;
        this.chessAI = new StaticChessAI(enabled);
    }
}