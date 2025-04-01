package com.example.demo1.core.ai.evaluation.components;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;

/**
 * Evaluates material balance and material-related features
 */
public class MaterialEvaluator {

    /**
     * Evaluate material balance
     *
     * @param board The chess board
     * @param sideToEvaluate Perspective to evaluate from
     * @return Score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate) {
        int whiteMaterial = 0;
        int blackMaterial = 0;

        // Count bishops for bishop pair bonus
        int whiteBishops = 0;
        int blackBishops = 0;

        // Count pieces on different colored squares for bishop pair effectiveness
        boolean whiteHasLightSquareBishop = false;
        boolean whiteHasDarkSquareBishop = false;
        boolean blackHasLightSquareBishop = false;
        boolean blackHasDarkSquareBishop = false;

        // Scan the board for all pieces
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                // Calculate standard material value
                int value = getStandardValue(piece.getType());

                if (piece.getColor() == Color.WHITE) {
                    whiteMaterial += value;

                    // Count bishops and their square colors
                    if (piece.getType() == ChessPieceType.BISHOP) {
                        whiteBishops++;

                        // Check bishop's square color
                        boolean isLightSquare = isLightSquare(pos);
                        if (isLightSquare) {
                            whiteHasLightSquareBishop = true;
                        } else {
                            whiteHasDarkSquareBishop = true;
                        }
                    }
                } else {
                    blackMaterial += value;

                    // Count bishops and their square colors
                    if (piece.getType() == ChessPieceType.BISHOP) {
                        blackBishops++;

                        // Check bishop's square color
                        boolean isLightSquare = isLightSquare(pos);
                        if (isLightSquare) {
                            blackHasLightSquareBishop = true;
                        } else {
                            blackHasDarkSquareBishop = true;
                        }
                    }
                }
            }
        }

        // Add bishop pair bonus if appropriate
        if (whiteBishops >= 2) {
            // Extra bonus for bishops on opposite colors
            if (whiteHasLightSquareBishop && whiteHasDarkSquareBishop) {
                whiteMaterial += EvaluationConstants.BISHOP_PAIR_BONUS;
            } else {
                // Smaller bonus for same-colored bishops
                whiteMaterial += EvaluationConstants.BISHOP_PAIR_BONUS / 2;
            }
        }

        if (blackBishops >= 2) {
            // Extra bonus for bishops on opposite colors
            if (blackHasLightSquareBishop && blackHasDarkSquareBishop) {
                blackMaterial += EvaluationConstants.BISHOP_PAIR_BONUS;
            } else {
                // Smaller bonus for same-colored bishops
                blackMaterial += EvaluationConstants.BISHOP_PAIR_BONUS / 2;
            }
        }

        // Adjust for imbalances (e.g., rook vs bishop+pawn, etc.)
        // This is a simplified implementation - could be expanded

        int materialScore = whiteMaterial - blackMaterial;

        // Return from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ? materialScore : -materialScore;
    }

    /**
     * Get the standard value of a piece type
     */
    private int getStandardValue(ChessPieceType type) {
        switch (type) {
            case PAWN: return EvaluationConstants.PAWN_VALUE;
            case KNIGHT: return EvaluationConstants.KNIGHT_VALUE;
            case BISHOP: return EvaluationConstants.BISHOP_VALUE;
            case ROOK: return EvaluationConstants.ROOK_VALUE;
            case QUEEN: return EvaluationConstants.QUEEN_VALUE;
            case KING: return 0; // Kings not counted in material value
            default: return 0;
        }
    }

    /**
     * Determine if a square is light or dark
     *
     * @param pos The position to check
     * @return True if it's a light square, false if dark
     */
    private boolean isLightSquare(Position pos) {
        int file = pos.getFile() - 'a';
        int rank = pos.getRank() - 1;

        // Light squares have even sum of file and rank
        return (file + rank) % 2 == 0;
    }
}