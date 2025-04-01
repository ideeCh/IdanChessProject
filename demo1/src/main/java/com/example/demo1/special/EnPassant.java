// File: src/main/java/com/example/demo1/special/EnPassant.java
package com.example.demo1.special;

import com.example.demo1.core.*;

/**
 * Implementation of En Passant special move
 */
public class EnPassant {

    /**
     * Check if an en passant capture is possible
     *
     * @param gameState    Current game state
     * @param pawnPosition Position of the pawn that might capture
     * @return Position where en passant capture can be made, or null if not possible
     */
    public static Position getEnPassantCapturePosition(GameState gameState, Position pawnPosition) {
        Position enPassantTarget = gameState.getEnPassantTarget();
        if (enPassantTarget == null) {
            return null;
        }

        // Get the pawn
        ChessPiece pawn = gameState.getBoard().getPieceAt(pawnPosition);
        if (pawn == null || pawn.getType() != ChessPieceType.PAWN) {
            return null;
        }

        // Check if the pawn is in position for an en passant capture
        // The pawn must be on the 5th rank (for white) or 4th rank (for black)
        int correctRank = (pawn.getColor() == Color.WHITE) ? 5 : 4;

        if (pawnPosition.getRank() == correctRank) {
            // Check if the en passant target is adjacent diagonally
            int fileDistance = Math.abs(pawnPosition.getFile() - enPassantTarget.getFile());

            // For an en passant capture, the file distance should be 1 and the target should be ahead
            if (fileDistance == 1) {
                int expectedRank = pawnPosition.getRank() + (pawn.getColor() == Color.WHITE ? 1 : -1);
                if (enPassantTarget.getRank() == expectedRank) {
                    return enPassantTarget;
                }
            }
        }

        return null;
    }


    // File: src/main/java/com/example/demo1/special/EnPassant.java

    public static void executeEnPassantCapture(GameState gameState, Move move) {
        Position enPassantTarget = gameState.getEnPassantTarget();

        if (enPassantTarget == null) {
            System.out.println("Warning: Attempted en passant capture but no target is set");
            gameState.getBoard().movePiece(move);
            return;
        }

        // Get the capturing pawn
        ChessPiece capturingPawn = gameState.getBoard().getPieceAt(move.getSource());

        // Find the position of the pawn to be captured (same file as en passant target, same rank as capturing pawn)
        char captureFile = enPassantTarget.getFile();
        int captureRank = move.getSource().getRank();
        Position capturedPawnPos = new Position(captureFile, captureRank);

        // Get the captured pawn
        ChessPiece capturedPawn = gameState.getBoard().getPieceAt(capturedPawnPos);

        // Add to captured pieces list
        if (capturedPawn != null) {
            gameState.getCapturedPieces(gameState.getCurrentPlayer()).add(capturedPawn);

            // Remove the captured pawn from the board
            gameState.getBoard().setPieceAt(capturedPawnPos, null);

            System.out.println("En passant capture: Removed pawn at " + capturedPawnPos);
        } else {
            System.out.println("Warning: No pawn found at " + capturedPawnPos + " for en passant capture");
        }

        // Move the capturing pawn to the en passant target square
        gameState.getBoard().movePiece(move);
    }

    public static void updateEnPassantTarget(GameState gameState, Move move) {
        // Reset en passant target at the beginning of each move
        gameState.setEnPassantTarget(null);

        // Get the piece that moved
        ChessPiece piece = gameState.getBoard().getPieceAt(move.getTarget());

        // Check if it's a pawn that moved two squares
        if (piece != null && piece.getType() == ChessPieceType.PAWN) {
            int sourceRank = move.getSource().getRank();
            int targetRank = move.getTarget().getRank();

            if (Math.abs(targetRank - sourceRank) == 2) {
                // Calculate the en passant target square (middle square)
                char file = move.getSource().getFile();
                int midRank = (sourceRank + targetRank) / 2;
                Position enPassantTarget = new Position(file, midRank);

                gameState.setEnPassantTarget(enPassantTarget);
                System.out.println("Set en passant target: " + enPassantTarget);
            }
        }
    }
}
