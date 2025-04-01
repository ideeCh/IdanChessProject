package com.example.demo1.core.ai;

import com.example.demo1.core.Board;
import com.example.demo1.core.Color;
import com.example.demo1.core.Move;
import com.example.demo1.core.GameState;

/**
 * Interface for chess AI implementations.
 * Provides contract for AI behavior including move calculation and position evaluation.
 */
public interface GameAI {
    Move findBestMove(GameState gameState);
    int getDifficultyRating();
    String getName();
    void setVerbose(boolean verbose);
}