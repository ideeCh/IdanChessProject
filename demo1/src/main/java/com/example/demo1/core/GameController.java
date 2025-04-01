package com.example.demo1.core;

import com.example.demo1.core.ai.AIManager;
import com.example.demo1.endgame.CheckmateDetector;
import com.example.demo1.endgame.DrawDetector;
import com.example.demo1.endgame.StalemateDetector;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Game controller class that manages the chess game logic
 */
public class GameController {

    private GameState gameState;
    private List<GameState> history;
    private MoveValidator moveValidator;

    // AI related fields
    private AIManager aiManager;
    private boolean aiEnabled = false;
    private Color aiColor = Color.BLACK; // Default AI color

    // UI callback interface
    private GameUICallback uiCallback;

    public GameController() {
        this.history = new ArrayList<>();
        this.moveValidator = new MoveValidator();
        this.aiManager = new AIManager();
    }

    /**
     * Interface for UI callbacks
     */
    public interface GameUICallback {
        void onBoardUpdated();
        void onGameOver(String message);
    }

    /**
     * Set the UI callback
     */
    public void setUICallback(GameUICallback callback) {
        this.uiCallback = callback;
    }

    /**
     * Initialize the game controller
     */
    public void initialize() {
        startNewGame();
    }

    /**
     * Start a new chess game
     */
    public void startNewGame() {
        this.gameState = new GameState();
        this.history.clear();
        // Save initial state
        saveGameState();

        // Update UI
        if (uiCallback != null) {
            Platform.runLater(uiCallback::onBoardUpdated);
        }

        // If AI is enabled and it's AI's turn, make AI move
        checkAndMakeAIMove();
    }

    /**
     * Get the current game state
     *
     * @return Current game state
     */
    public GameState getGameState() {
        return this.gameState;
    }

    /**
     * Make a move in the chess game
     *
     * @param move The move to make
     * @throws IllegalMoveException if the move is invalid
     */
    public void makeMove(Move move) throws IllegalMoveException {
        // Validate the move
        if (!moveValidator.isValidMove(gameState, move)) {
            throw new IllegalMoveException("Invalid move");
        }

        // Make the move with explicit simulation=false parameter
        gameState.makeMove(move, false);

        // Save to history
        saveGameState();

        // Update the UI
        if (uiCallback != null) {
            Platform.runLater(uiCallback::onBoardUpdated);
        }

        // Check for game over
        if (isGameOver()) {
            if (uiCallback != null) {
                final String endingStatus = getGameEndingStatus();
                Platform.runLater(() -> uiCallback.onGameOver(endingStatus));
            }
            return;
        }

        // If AI is enabled and it's AI's turn, make AI move
        checkAndMakeAIMove();
    }

    /**
     * Save the current game state to history
     */
    private void saveGameState() {
        try {
            this.history.add((GameState) gameState.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Undo the last move
     *
     * @return true if a move was undone, false if no moves to undo
     */
    public boolean undoMove() {
        if (history.size() <= 1) {
            return false;
        }

        // Remove the current state from history
        history.remove(history.size() - 1);

        // Restore the previous state
        try {
            gameState = (GameState) history.get(history.size() - 1).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return false;
        }

        // Update the UI
        if (uiCallback != null) {
            Platform.runLater(uiCallback::onBoardUpdated);
        }

        return true;
    }

    /**
     * Enable or disable the AI
     *
     * @param enabled Whether the AI should be enabled
     */
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;

        // If enabling the AI and it's AI's turn, make a move
        if (enabled) {
            checkAndMakeAIMove();
        }
    }

    /**
     * Check if the AI is enabled
     *
     * @return Whether the AI is enabled
     */
    public boolean isAIEnabled() {
        return aiEnabled;
    }

    /**
     * Set the color the AI will play as
     *
     * @param color The color for the AI
     */
    public void setAIColor(Color color) {
        this.aiColor = color;

        // If AI is enabled and it's now AI's turn, make a move
        if (aiEnabled) {
            checkAndMakeAIMove();
        }
    }

    /**
     * Get the color the AI is playing as
     *
     * @return AI's color
     */
    public Color getAIColor() {
        return aiColor;
    }

    /**
     * Check if it's AI's turn and make a move if it is
     */
    private void checkAndMakeAIMove() {
        if (aiEnabled && gameState.getCurrentPlayer() == aiColor && !isGameOver()) {
            // Use a separate thread for AI move to avoid UI freezing
            new Thread(() -> {
                try {
                    // Add a small delay so the UI can update
                    Thread.sleep(500);

                    // Make the AI move
                    Move aiMove = aiManager.getBestMove(gameState);

                    if (aiMove != null) {
                        // Make the move in the game
                        try {
                            gameState.makeMove(aiMove, false);

                            // Save to history
                            saveGameState();

                            // Update the UI - MUST use Platform.runLater
                            if (uiCallback != null) {
                                Platform.runLater(uiCallback::onBoardUpdated);
                            }

                            // Check for game over
                            if (isGameOver()) {
                                if (uiCallback != null) {
                                    final String endingStatus = getGameEndingStatus();
                                    Platform.runLater(() -> uiCallback.onGameOver(endingStatus));
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error making AI move: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("AI couldn't find a valid move.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Check if the game is over for any reason
     */
    public boolean isGameOver() {
        return CheckmateDetector.isCheckmate(gameState, gameState.getCurrentPlayer()) ||
                StalemateDetector.isStalemate(gameState, gameState.getCurrentPlayer()) ||
                DrawDetector.isDraw(gameState);
    }

    /**
     * Get detailed game ending status message
     */
    public String getGameEndingStatus() {
        if (CheckmateDetector.isCheckmate(gameState, gameState.getCurrentPlayer())) {
            return "CHECKMATE! " + (gameState.getCurrentPlayer() == Color.WHITE ? Color.BLACK : Color.WHITE) + " wins!";
        } else if (StalemateDetector.isStalemate(gameState, gameState.getCurrentPlayer())) {
            return "STALEMATE! Game is a draw.";
        } else if (DrawDetector.isFiftyMoveRuleDraw(gameState)) {
            return "DRAW by 50-move rule.";
        } else if (DrawDetector.isThreefoldRepetitionDraw(gameState)) {
            return "DRAW by threefold repetition.";
        } else if (DrawDetector.isInsufficientMaterialDraw(gameState)) {
            return "DRAW by insufficient material.";
        }
        return "";
    }
}