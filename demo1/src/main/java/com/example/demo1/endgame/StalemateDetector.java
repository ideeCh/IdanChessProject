// In StalemateDetector.java:
package com.example.demo1.endgame;

import com.example.demo1.core.*;
import com.example.demo1.core.MoveValidator;
import com.example.demo1.core.Color;
import com.example.demo1.core.GameState;
import com.example.demo1.core.Move;
import com.example.demo1.core.Position;

import java.util.List;

import static com.example.demo1.core.MoveValidator.generateBasicMoves;
import static com.example.demo1.core.MoveValidator.isKingInCheck;

public class StalemateDetector {

    /**
     * Check if a player is in stalemate
     * @param gameState Current game state
     * @param color Color of the player to check
     * @return true if the player is in stalemate
     */
    public static boolean isStalemate(GameState gameState, Color color) {
        // If the king is in check, it's not stalemate
        if (isKingInCheck(gameState, color)) {
            return false;
        }

        // Check if there are any legal moves
        return !MoveValidator.hasLegalMoves(gameState, color);
    }



    /**
     * Check if a player has any legal moves
     * @param gameState Current game state
     * @param color Color of the player to check
     * @return true if the player has at least one legal move
     */
    public boolean hasLegalMoves(GameState gameState, Color color) {
        Board board = gameState.getBoard();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                // Skip empty squares and opponent's pieces
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                // Get basic moves for this piece
                List<Move> basicMoves = generateBasicMoves(gameState, piece);

                // Check each move to see if it would leave the king in check
                for (Move move : basicMoves) {
                    try {
                        // Simulate the move
                        GameState tempState = (GameState) gameState.clone();
                        tempState.makeMove(move, true);

                        // If this move doesn't leave king in check, we found a legal move
                        if (!isKingInCheck(tempState, color)) {
                            return true;
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // If we get here, there are no legal moves
        return false;
    }
}
