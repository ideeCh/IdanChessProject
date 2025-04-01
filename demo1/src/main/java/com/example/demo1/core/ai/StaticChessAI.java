package com.example.demo1.core.ai;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.state.StateManager;
import com.example.demo1.core.ai.state.ChessState;
import com.example.demo1.special.Castling;
import java.util.*;

/**
 * Implementation of a chess AI that uses static evaluation with state-based context awareness.
 */
public class StaticChessAI implements GameAI {

    // Add the StateManager field
    private final StateManager stateManager;

    private final GamePhaseDetector phaseDetector;
    private boolean verbose;

    /**
     * Create a new StaticChessAI with default settings
     */
    public StaticChessAI() {
        this(false);
    }

    /**
     * Create a new StaticChessAI
     *
     * @param verbose Whether to output verbose information about decisions
     */
    public StaticChessAI(boolean verbose) {
        // Initialize the state manager
        this.stateManager = new StateManager(verbose);

        // Keep your existing initializations
        this.phaseDetector = new GamePhaseDetector();
        this.verbose = verbose;
    }

    @Override
    public Move findBestMove(GameState gameState) {
        // Update the state based on current game state
        stateManager.updateState(gameState);

        // Get the current state for evaluation
        ChessState currentState = stateManager.getCurrentState();

        if (verbose) {
            System.out.println("[StaticChessAI] Using state: " + currentState.getName());
        }

        // Get all legal moves
        List<Move> legalMoves = generateLegalMoves(gameState);

        if (legalMoves.isEmpty()) {
            return null; // No legal moves (checkmate or stalemate)
        }

        // Evaluate each move by making it on a board copy and scoring the resulting position
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        Map<Move, Integer> moveScores = new HashMap<>();

        Color aiColor = gameState.getCurrentPlayer();

        for (Move move : legalMoves) {
            // Make a copy of the game state and execute the move
            try {
                GameState tempState = (GameState) gameState.clone();
                tempState.makeMove(move, true); // simulation mode

                // Use the current state to evaluate the position
                int score = currentState.evaluatePosition(tempState.getBoard(), aiColor);

                // Store score for this move
                moveScores.put(move, score);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } catch (CloneNotSupportedException e) {
                if (verbose) {
                    System.err.println("[StaticChessAI] Error cloning game state: " + e.getMessage());
                }
            }
        }

        // Log the evaluation if verbose mode is on
        if (verbose) {
            logMoveEvaluation(moveScores, bestMove);
        }

        return bestMove;
    }

    /**
     * Generate all legal moves for the current player
     */
    private List<Move> generateLegalMoves(GameState gameState) {
        List<Move> legalMoves = new ArrayList<>();
        Color currentPlayer = gameState.getCurrentPlayer();
        Board board = gameState.getBoard();

        // Find all pieces of the current player
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == currentPlayer) {
                    // Get potential moves for this piece
                    List<Move> basicMoves = MoveValidator.generateBasicMoves(gameState, piece);

                    // Filter out moves that would leave the king in check
                    for (Move move : basicMoves) {
                        try {
                            GameState tempState = (GameState) gameState.clone();
                            tempState.makeMove(move, true); // simulation mode

                            if (!MoveValidator.isKingInCheck(tempState, currentPlayer)) {
                                legalMoves.add(move);
                            }
                        } catch (CloneNotSupportedException e) {
                            if (verbose) {
                                System.err.println("[StaticChessAI] Error cloning game state: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        // Add castling moves if they're legal
        addCastlingMoves(gameState, legalMoves);

        return legalMoves;
    }

    /**
     * Add castling moves if they are legal
     */
    private void addCastlingMoves(GameState gameState, List<Move> moves) {
        Color currentPlayer = gameState.getCurrentPlayer();

        // Try kingside castling
        try {
            Move kingsideCastling = Castling.createKingsideCastlingMove(currentPlayer);
            if (Castling.isValidCastling(gameState, kingsideCastling)) {
                moves.add(kingsideCastling);
            }
        } catch (Exception e) {
            if (verbose) {
                System.err.println("[StaticChessAI] Error checking kingside castling: " + e.getMessage());
            }
        }

        // Try queenside castling
        try {
            Move queensideCastling = Castling.createQueensideCastlingMove(currentPlayer);
            if (Castling.isValidCastling(gameState, queensideCastling)) {
                moves.add(queensideCastling);
            }
        } catch (Exception e) {
            if (verbose) {
                System.err.println("[StaticChessAI] Error checking queenside castling: " + e.getMessage());
            }
        }
    }

    /**
     * Log the evaluation of all considered moves
     */
    private void logMoveEvaluation(Map<Move, Integer> moveScores, Move bestMove) {
        System.out.println("[StaticChessAI] Move Evaluations:");
        List<Map.Entry<Move, Integer>> sortedEntries = new ArrayList<>(moveScores.entrySet());
        sortedEntries.sort(Map.Entry.<Move, Integer>comparingByValue().reversed());

        for (Map.Entry<Move, Integer> entry : sortedEntries) {
            String marker = entry.getKey().equals(bestMove) ? " <<< SELECTED" : "";
            System.out.println(entry.getKey() + ": " + entry.getValue() + marker);
        }

        // Add state information to log
        System.out.println("[StaticChessAI] Current state: " + stateManager.getCurrentState().getName());
    }

    @Override
    public int getDifficultyRating() {
        return 6; // Medium-high strength
    }

    @Override
    public String getName() {
        return "Context-Aware Evaluator";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        stateManager.setLogging(verbose);
    }
}