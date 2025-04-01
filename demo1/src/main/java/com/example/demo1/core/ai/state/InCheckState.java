package com.example.demo1.core.ai.state;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.components.*;

/**
 * Special state for when the king is in check, focusing on resolving the threat.
 */
public class InCheckState implements ChessState {
    private final KingSafetyEvaluator kingSafetyEvaluator;
    private final TacticalEvaluator tacticalEvaluator;
    private final MaterialEvaluator materialEvaluator;

    public InCheckState() {
        this.kingSafetyEvaluator = new KingSafetyEvaluator();
        this.tacticalEvaluator = new TacticalEvaluator();
        this.materialEvaluator = new MaterialEvaluator();
    }

    @Override
    public String getName() {
        return "InCheck";
    }

    @Override
    public int evaluatePosition(Board board, Color sideToEvaluate) {
        int score = 0;

        // Basic material value
        score += materialEvaluator.evaluate(board, sideToEvaluate);

        // When in check, king safety is the absolute priority
        score += kingSafetyEvaluator.evaluate(board, sideToEvaluate, 0.5) * 3.0;

        // Tactical evaluation to find ways to block or capture checking piece
        score += tacticalEvaluator.evaluate(board, sideToEvaluate) * 1.5;

        return score;
    }
}