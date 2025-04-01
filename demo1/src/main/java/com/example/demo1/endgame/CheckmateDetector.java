package com.example.demo1.endgame;

import com.example.demo1.core.*;

import java.util.List;

/**
 * Class for detecting checkmate conditions
 */
public class CheckmateDetector {

    /**
     * Check if a player is in checkmate
     * @param gameState Current game state
     * @param color Color of the player to check
     * @return true if the player is in checkmate
     */
    public static boolean isCheckmate(GameState gameState, Color color) {
        // First, check if the king is in check
        if (!MoveValidator.isKingInCheck(gameState, color)) {
            return false;
        }

        // Then, check if there are any legal moves that can escape check
        return !MoveValidator.hasLegalMoves(gameState, color);
    }


    /**
     * Check if a player has any legal moves
     *
     * @param gameState Current game state
     * @param color     Color of the player to check
     * @return true if the player has at least one legal move
     */
    public static boolean hasLegalMoves(GameState gameState, Color color) {
        MoveValidator validator = new MoveValidator();

        // Check all pieces of the given color
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = gameState.getBoard().getPieceAt(pos);

                if (piece != null && piece.getColor() == color) {
                    // Get all potential moves for this piece
                    List<Move> potentialMoves = validator.generateBasicMoves(gameState, piece);

                    // Check each move to see if it would leave the king in check
                    for (Move move : potentialMoves) {
                        // Create a temporary game state to simulate the move
                        try {
                            GameState tempState = (GameState) gameState.clone();
                            tempState.makeMove(move, true); // Simulate the move

                            // If the king is not in check after the move, it's a legal move
                            if (!validator.isKingInCheck(tempState, color)) {
                                return true;
                            }
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // If we get here, there are no legal moves
        return false;
    }
}
