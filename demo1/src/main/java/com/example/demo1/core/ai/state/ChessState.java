package com.example.demo1.core.ai.state;

import com.example.demo1.core.*;

/**
 * Interface for chess game states.
 * Each state represents a different phase or situation in the game
 * with its own evaluation strategy.
 */
public interface ChessState {
    /**
     * Evaluate the current position from the perspective of the given color.
     *
     * @param board The current chess board
     * @param sideToEvaluate The side from whose perspective to evaluate
     * @return Evaluation score in centipawns (positive is good for the side)
     */
    int evaluatePosition(Board board, Color sideToEvaluate);

    /**
     * Get the name of this state for debugging and logging.
     *
     * @return State name
     */
    String getName();
}