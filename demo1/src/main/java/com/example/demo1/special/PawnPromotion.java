package com.example.demo1.special;

import com.example.demo1.core.*;

/**
 * Implementation of Pawn Promotion special move
 */
public class PawnPromotion {

    /**
     * Check if a move would result in pawn promotion
     *
     * @param gameState Current game state
     * @param move      The move to check
     * @return true if the move would result in pawn promotion
     */
    public static boolean isPromotionMove(GameState gameState, Move move) {
        ChessPiece piece = gameState.getBoard().getPieceAt(move.getSource());

        if (piece == null || piece.getType() != ChessPieceType.PAWN) {
            return false;
        }

        int targetRank = move.getTarget().getRank();

        // Promotion happens when a pawn reaches the last rank
        return (piece.getColor() == Color.WHITE && targetRank == 8) ||
                (piece.getColor() == Color.BLACK && targetRank == 1);
    }

    /**
     * Execute a pawn promotion
     *
     * @param gameState Current game state
     * @param move      The promotion move, including the promotion type
     * @throws IllegalArgumentException if promotion type is not specified for a promotion move
     */

    public static void executePromotion(GameState gameState, Move move) {
        if (move.getPromotionType() == null) {
            throw new IllegalArgumentException("Promotion type must be specified");
        }

        ChessPiece pawn = gameState.getBoard().getPieceAt(move.getSource());
        if (pawn == null || pawn.getType() != ChessPieceType.PAWN) {
            throw new IllegalArgumentException("Source piece must be a pawn");
        }

        Color pawnColor = pawn.getColor();

        // Check for capture
        ChessPiece capturedPiece = gameState.getBoard().getPieceAt(move.getTarget());
        if (capturedPiece != null) {
            // Make sure to add captured piece to the correct player's list
            gameState.getCapturedPieces(pawnColor).add(capturedPiece);
        }

        // Remove pawn from source
        gameState.getBoard().setPieceAt(move.getSource(), null);

        // Create promoted piece with the correct color
        ChessPiece promotedPiece = ChessPieceFactory.createPiece(move.getPromotionType(), pawnColor);

        // Place promoted piece at target
        gameState.getBoard().setPieceAt(move.getTarget(), promotedPiece);
    }
    /**
     * Create a promotion move
     *
     * @param source        Source position (pawn position)
     * @param target        Target position (on the last rank)
     * @param promotionType Type of piece to promote to
     * @return A move representing pawn promotion
     */
    public static Move createPromotionMove(Position source, Position target, ChessPieceType promotionType) {
        if (promotionType == ChessPieceType.KING || promotionType == ChessPieceType.PAWN) {
            throw new IllegalArgumentException("Cannot promote to king or pawn");
        }

        return new Move(source, target, promotionType);
    }

    /**
     * Get all possible promotion types
     *
     * @return Array of valid promotion piece types
     */
    public static ChessPieceType[] getPromotionTypes() {
        return new ChessPieceType[]{
                ChessPieceType.QUEEN,
                ChessPieceType.ROOK,
                ChessPieceType.BISHOP,
                ChessPieceType.KNIGHT
        };
    }
}
