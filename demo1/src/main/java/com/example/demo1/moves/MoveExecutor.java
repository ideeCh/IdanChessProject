package com.example.demo1.moves;

import com.example.demo1.core.*;
import com.example.demo1.special.*;
import com.example.demo1.endgame.*;

public class MoveExecutor {
    /**
     * Execute a chess move with all special move handling and game state updates
     * @param gameState Current game state
     * @param move The move to execute
     * @param isSimulation Whether this is a simulated move (for validation)
     */
    public static void executeMove(GameState gameState, Move move, boolean isSimulation) {
        // Get the piece being moved
        ChessPiece piece = gameState.getBoard().getPieceAt(move.getSource());

        // IMPORTANT: Store the old en passant target
        Position oldEnPassantTarget = gameState.getEnPassantTarget();

        // Determine if the move is a capture or pawn move (for 50-move rule)
        boolean isPawnMove = piece != null && piece.getType() == ChessPieceType.PAWN;
        boolean isCapture = false;

        // Check if this is an en passant capture BEFORE resetting the target
        boolean isEnPassantCapture = piece != null &&
                piece.getType() == ChessPieceType.PAWN &&
                oldEnPassantTarget != null &&
                move.getTarget().equals(oldEnPassantTarget);

        // Only reset en passant target AFTER checking for en passant captures
        if (!isSimulation && !isEnPassantCapture) {
            gameState.setEnPassantTarget(null);
        }
        // Handle different types of moves
        if (move.isCastling()) {
            System.out.println("Executing castling move: " + move);
            System.out.println("Rook source: " + move.getCastlingRookSource());
            System.out.println("Rook target: " + move.getCastlingRookTarget());
            // Execute castling
            Castling.executeCastling(gameState, move);
        }

        // Handle different types of moves
        if (move.isCastling()) {
            // Execute castling
            Castling.executeCastling(gameState, move);
        }
        else if (isEnPassantCapture) {
            // Now we're sure this is an en passant capture with a valid target
            executeEnPassantCapture(gameState, move, oldEnPassantTarget);
            isCapture = true;

            // Only reset the en passant target after executing the capture
            if (!isSimulation) {
                gameState.setEnPassantTarget(null);
            }
        }
        else if (PawnPromotion.isPromotionMove(gameState, move)) {
            // Execute pawn promotion
            if (move.getPromotionType() != null) {
                PawnPromotion.executePromotion(gameState, move);
            } else {
                // Only use default promotion for simulations, never for actual moves
                if (isSimulation) {
                    Move promotionMove = new Move(move.getSource(), move.getTarget(), ChessPieceType.QUEEN);
                    PawnPromotion.executePromotion(gameState, promotionMove);
                } else {
                    // This should never happen, as UI should handle promotion type
                    throw new IllegalArgumentException("Promotion type must be specified for non-simulation moves");
                }
            }
        }
        else {
            // Check for regular capture
            ChessPiece capturedPiece = gameState.getBoard().getPieceAt(move.getTarget());
            if (capturedPiece != null) {
                isCapture = true;
                if (!isSimulation) {
                    gameState.getCapturedPieces(gameState.getCurrentPlayer()).add(capturedPiece);
                }
            }

            // Execute regular move
            gameState.getBoard().movePiece(move, isSimulation);
        }

        // Update en passant target if a pawn moved two squares (and not an en passant capture)
        if (piece != null && piece.getType() == ChessPieceType.PAWN && !isSimulation && !isEnPassantCapture) {
            int sourceRank = move.getSource().getRank();
            int targetRank = move.getTarget().getRank();

            // Check if it's a two-square move from the starting position
            if (Math.abs(targetRank - sourceRank) == 2) {
                // Calculate the en passant target square (the square "in between")
                char file = move.getSource().getFile();
                int midRank = (sourceRank + targetRank) / 2;
                Position newTarget = new Position(file, midRank);
                gameState.setEnPassantTarget(newTarget);
                System.out.println("Set en passant target: " + newTarget);
            }
        }

        // Update game state tracking (only for non-simulated moves)
        if (!isSimulation) {
            // Update half move clock for 50-move rule
            DrawDetector.updateHalfMoveClock(gameState, isPawnMove, isCapture);

            // Update position count for threefold repetition
            DrawDetector.updatePositionCount(gameState);

            // Add to move history
            gameState.getMoveHistory().add(move);

            // Switch players
            gameState.switchPlayer();

            // Update game status (check, checkmate, stalemate)
            updateGameStatus(gameState);
        }
    }

    /**
     * Execute an en passant capture with explicit target
     * @param gameState Current game state
     * @param move The en passant move
     * @param enPassantTarget The stored en passant target
     */
    private static void executeEnPassantCapture(GameState gameState, Move move, Position enPassantTarget) {
        // Find the position of the pawn to be captured (which is on the same file as the target but on the rank of the capturing pawn)
        char captureFile = enPassantTarget.getFile();
        int captureRank = move.getSource().getRank(); // The rank of the capturing pawn
        Position capturedPawnPos = new Position(captureFile, captureRank);

        // Get the captured pawn
        ChessPiece capturedPawn = gameState.getBoard().getPieceAt(capturedPawnPos);

        // Add to captured pieces list
        if (capturedPawn != null) {
            gameState.getCapturedPieces(gameState.getCurrentPlayer()).add(capturedPawn);

            // Remove the captured pawn from the board
            gameState.getBoard().setPieceAt(capturedPawnPos, null);

            // Log the capture
            System.out.println("En passant capture: Removed pawn at " + capturedPawnPos);
        } else {
            System.out.println("Warning: No pawn found at " + capturedPawnPos + " for en passant capture");
        }

        // Move the capturing pawn
        gameState.getBoard().movePiece(move);
    }

    /**
     * Update the game status (check, checkmate, stalemate)
     * @param gameState Current game state
     */
    // In MoveExecutor.java, update the updateGameStatus method:
    private static void updateGameStatus(GameState gameState) {
        Color currentPlayer = gameState.getCurrentPlayer();

        // Check if the current player is in check
        boolean isInCheck = MoveValidator.isKingInCheck(gameState, currentPlayer);
        gameState.setCheck(isInCheck);

        // Check for checkmate or stalemate
        if (!MoveValidator.hasLegalMoves(gameState, currentPlayer)) {
            if (isInCheck) {
                gameState.setCheckmate(true);
                gameState.setStalemate(false);
            } else {
                gameState.setCheckmate(false);
                gameState.setStalemate(true);
            }
        } else {
            gameState.setCheckmate(false);
            gameState.setStalemate(false);
        }
    }
}