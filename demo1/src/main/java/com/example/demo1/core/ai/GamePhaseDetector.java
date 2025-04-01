package com.example.demo1.core.ai;

import com.example.demo1.core.*;

/**
 * Detects the current phase of a chess game based on material,
 * piece development, and other board characteristics.
 */
public class GamePhaseDetector {

    // Constants for phase detection thresholds
    private static final int OPENING_PHASE_PIECE_THRESHOLD = 28; // Most pieces still on board
    private static final int ENDGAME_PHASE_THRESHOLD = 15; // Few pieces remaining
    private static final int MID_DEVELOPMENT_THRESHOLD = 8; // Number of developed pieces to signal middlegame
    private static final int MIN_MOVES_FOR_MIDDLEGAME = 10; // Minimum moves before considering middlegame

    /**
     * Detects the current game phase based on board state
     *
     * @param gameState Current game state
     * @return The detected game phase
     */
    public GamePhase detectPhase(GameState gameState) {
        // Get piece count
        int pieceCount = countPieces(gameState.getBoard());

        // Check move count (helps determine if still in opening)
        int moveCount = gameState.getMoveHistory().size();

        // Early endgame detection - check for reduced material
        if (pieceCount <= ENDGAME_PHASE_THRESHOLD) {
            return GamePhase.ENDGAME;
        }

        // Check for queens off the board (strong endgame indicator)
        if (pieceCount <= 20 && isQueenlessPosition(gameState.getBoard())) {
            return GamePhase.ENDGAME;
        }

        // Opening detection - early moves and many pieces still on board
        if (pieceCount >= OPENING_PHASE_PIECE_THRESHOLD) {
            // However, if development is complete, it's probably middlegame
            if (moveCount >= MIN_MOVES_FOR_MIDDLEGAME &&
                    isDevelopmentComplete(gameState.getBoard())) {
                return GamePhase.MIDDLEGAME;
            }
            return GamePhase.OPENING;
        }

        // Default to middlegame
        return GamePhase.MIDDLEGAME;
    }

    /**
     * Calculate a continuous phase value from 0.0 (opening) to 1.0 (endgame)
     * for smoother transition between evaluation phases
     *
     * @param gameState Current game state
     * @return A floating point value between 0.0 and 1.0
     */
    public double calculatePhaseValue(Board gameState) {
        // Count total material value on board
        int totalMaterial = calculateTotalMaterial(gameState);

        // Starting material value (32 pieces minus kings)
        int startingMaterial = 7800; // Full board value in centipawns

        // Calculate phase as percentage of material removed
        double phase = 1.0 - (totalMaterial / (double) startingMaterial);

        // Ensure phase is between 0 and 1
        return Math.max(0.0, Math.min(1.0, phase));
    }

    /**
     * Count the total number of pieces on the board
     */
    private int countPieces(Board board) {
        int count = 0;
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                if (board.getPieceAt(pos) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Check if both queens are off the board
     */
    private boolean isQueenlessPosition(Board board) {
        boolean whiteQueenPresent = false;
        boolean blackQueenPresent = false;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);
                if (piece != null && piece.getType() == ChessPieceType.QUEEN) {
                    if (piece.getColor() == Color.WHITE) {
                        whiteQueenPresent = true;
                    } else {
                        blackQueenPresent = true;
                    }
                }
            }
        }

        return !whiteQueenPresent && !blackQueenPresent;
    }

    /**
     * Calculate the total material value on the board in centipawns
     */
    private int calculateTotalMaterial(Board board) {
        int totalValue = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null) {
                    switch (piece.getType()) {
                        case PAWN: totalValue += 100; break;
                        case KNIGHT: totalValue += 320; break;
                        case BISHOP: totalValue += 330; break;
                        case ROOK: totalValue += 500; break;
                        case QUEEN: totalValue += 900; break;
                        // Kings not counted in material value
                    }
                }
            }
        }

        return totalValue;
    }

    /**
     * Check if most pieces are developed (signals transition from opening to middlegame)
     */
    private boolean isDevelopmentComplete(Board board) {
        int developedPieceCount = 0;

        // Check knight development (moved from starting squares)
        if (isSquareEmpty(board, 'b', 1)) developedPieceCount++;
        if (isSquareEmpty(board, 'g', 1)) developedPieceCount++;
        if (isSquareEmpty(board, 'b', 8)) developedPieceCount++;
        if (isSquareEmpty(board, 'g', 8)) developedPieceCount++;

        // Check bishop development
        if (isSquareEmpty(board, 'c', 1)) developedPieceCount++;
        if (isSquareEmpty(board, 'f', 1)) developedPieceCount++;
        if (isSquareEmpty(board, 'c', 8)) developedPieceCount++;
        if (isSquareEmpty(board, 'f', 8)) developedPieceCount++;

        // Check king development (castling)
        if (isSquareEmpty(board, 'e', 1)) developedPieceCount += 2; // White king moved (likely castled)
        if (isSquareEmpty(board, 'e', 8)) developedPieceCount += 2; // Black king moved (likely castled)

        // If most minor pieces are developed, transition to middlegame
        return developedPieceCount >= MID_DEVELOPMENT_THRESHOLD;
    }

    /**
     * Check if a square is empty (used to detect if pieces have moved)
     */
    private boolean isSquareEmpty(Board board, char file, int rank) {
        Position pos = new Position(file, rank);
        return board.getPieceAt(pos) == null;
    }
}