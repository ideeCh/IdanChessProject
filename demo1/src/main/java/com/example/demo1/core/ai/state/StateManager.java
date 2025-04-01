package com.example.demo1.core.ai.state;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.GamePhaseDetector;
import com.example.demo1.core.ai.evaluation.*;

/**
 * Manages chess game states and transitions between them based on board conditions.
 */
public class StateManager {
    private ChessState currentState;
    private ChessState previousState; // For returning from temporary states

    // State instances
    private final ChessState openingState;
    private final ChessState middlegameState;
    private final ChessState endgameState;
    private final ChessState inCheckState;

    private final GamePhaseDetector phaseDetector;
    private boolean useLogging;

    /**
     * Create a new StateManager with default settings
     */
    public StateManager() {
        this(false);
    }

    /**
     * Create a new StateManager
     *
     * @param useLogging Whether to log state transitions
     */
    public StateManager(boolean useLogging) {
        this.useLogging = useLogging;

        // Initialize state objects using existing evaluators
        this.openingState = new PhaseState("Opening", (PhaseBasedEvaluator) new OpeningEvaluator());
        this.middlegameState = new PhaseState("Middlegame", (PhaseBasedEvaluator) new MiddlegameEvaluator());
        this.endgameState = new PhaseState("Endgame", new EndgameEvaluator());
        this.inCheckState = new InCheckState();

        // Start in opening state by default
        this.currentState = openingState;
        this.previousState = openingState;

        // Create phase detector for transition logic
        this.phaseDetector = new GamePhaseDetector();
    }

    /**
     * Update the current state based on the board situation.
     *
     * @param gameState The current game state
     */
    public void updateState(GameState gameState) {
        Board board = gameState.getBoard();
        Color color = gameState.getCurrentPlayer();

        // Store previous state before transition
        previousState = currentState;

        // First check for "emergency" states
        if (MoveValidator.isKingInCheck(gameState, color)) {
            if (currentState != inCheckState) {
                if (useLogging) {
                    System.out.println("State transition: " + currentState.getName() + " -> " + inCheckState.getName());
                }
                currentState = inCheckState;
            }
            return;
        }

        // If we were in an emergency state, return to regular phase state
        if (currentState == inCheckState) {
            // Determine the appropriate phase state to return to
            determinePhaseState(board);
            if (useLogging) {
                System.out.println("State transition: " + inCheckState.getName() + " -> " + currentState.getName());
            }
            return;
        }

        // Update phase-based state if necessary
        ChessState newPhaseState = determinePhaseState(board);

        // If phase has changed, update current state
        if (newPhaseState != currentState) {
            if (useLogging) {
                System.out.println("State transition: " + currentState.getName() + " -> " + newPhaseState.getName());
            }
            currentState = newPhaseState;
        }
    }

    /**
     * Determine the appropriate phase-based state (Opening, Middlegame, Endgame)
     *
     * @param board The current chess board
     * @return The appropriate phase state
     */
    private ChessState determinePhaseState(Board board) {
        // Use the phase detector to get a continuous phase value
        double phaseValue = phaseDetector.calculatePhaseValue(board);

        // Convert to discrete state
        if (phaseValue >= 0.7) {
            return endgameState;
        } else if (phaseValue >= 0.3) {
            return middlegameState;
        } else {
            return openingState;
        }
    }

    /**
     * Get the current state.
     *
     * @return The current chess state
     */
    public ChessState getCurrentState() {
        return currentState;
    }

    /**
     * Get the previous state (before most recent transition).
     *
     * @return The previous chess state
     */
    public ChessState getPreviousState() {
        return previousState;
    }

    /**
     * Enable or disable logging of state transitions.
     *
     * @param enabled Whether logging should be enabled
     */
    public void setLogging(boolean enabled) {
        this.useLogging = enabled;
    }
}