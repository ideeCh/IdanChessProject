package com.example.demo1.core.ai.state;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.*;

/**
 * State implementation that delegates to an existing phase-based evaluator.
 */
public class PhaseState implements ChessState {
    private final String name;
    private final PhaseBasedEvaluator evaluator;

    public PhaseState(String name, PhaseBasedEvaluator evaluator) {
        this.name = name;
        this.evaluator = evaluator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int evaluatePosition(Board board, Color sideToEvaluate) {
        return evaluator.evaluate(board, sideToEvaluate);
    }
}