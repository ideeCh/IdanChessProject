package com.example.demo1.core.ai.evaluation.components;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;

/**
 * Piece-square tables for positional evaluation
 * Based on the principle that pieces have different values on different squares
 */
public class PieceSquareTable {

    // Opening/Middlegame tables

    // Pawn table (opening/middlegame)
    private static final int[] PAWN_TABLE_MG = {
            0,   0,   0,   0,   0,   0,   0,   0,
            50,  50,  50,  50,  50,  50,  50,  50,
            10,  10,  20,  30,  30,  20,  10,  10,
            5,   5,  10,  25,  25,  10,   5,   5,
            0,   0,   0,  20,  20,   0,   0,   0,
            5,  -5, -10,   0,   0, -10,  -5,   5,
            5,  10,  10, -20, -20,  10,  10,   5,
            0,   0,   0,   0,   0,   0,   0,   0
    };

    // Knight table (opening/middlegame)
    private static final int[] KNIGHT_TABLE_MG = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20,   0,   0,   0,   0, -20, -40,
            -30,   0,  10,  15,  15,  10,   0, -30,
            -30,   5,  15,  20,  20,  15,   5, -30,
            -30,   0,  15,  20,  20,  15,   0, -30,
            -30,   5,  10,  15,  15,  10,   5, -30,
            -40, -20,   0,   5,   5,   0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
    };

    // Bishop table (opening/middlegame)
    private static final int[] BISHOP_TABLE_MG = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10,   0,  10,  10,  10,  10,   0, -10,
            -10,   5,   5,  10,  10,   5,   5, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,  10,  10,  10,  10,  10,  10, -10,
            -10,   5,   0,   0,   0,   0,   5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
    };

    // Rook table (opening/middlegame)
    private static final int[] ROOK_TABLE_MG = {
            0,   0,   0,   0,   0,   0,   0,   0,
            5,  10,  10,  10,  10,  10,  10,   5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            0,   0,   0,   5,   5,   0,   0,   0
    };

    // Queen table (opening/middlegame)
    private static final int[] QUEEN_TABLE_MG = {
            -20, -10, -10,  -5,  -5, -10, -10, -20,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -5,   0,   5,   5,   5,   5,   0,  -5,
            0,   0,   5,   5,   5,   5,   0,  -5,
            -10,   5,   5,   5,   5,   5,   0, -10,
            -10,   0,   5,   0,   0,   0,   0, -10,
            -20, -10, -10,  -5,  -5, -10, -10, -20
    };

    // King table (opening/middlegame)
    private static final int[] KING_TABLE_MG = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20,  20,   0,   0,   0,   0,  20,  20,
            20,  30,  10,   0,   0,  10,  30,  20
    };

    // Endgame tables

    // Pawn table (endgame)
    private static final int[] PAWN_TABLE_EG = {
            0,   0,   0,   0,   0,   0,   0,   0,
            80,  80,  80,  80,  80,  80,  80,  80,
            50,  50,  50,  50,  50,  50,  50,  50,
            30,  30,  30,  30,  30,  30,  30,  30,
            20,  20,  20,  20,  20,  20,  20,  20,
            10,  10,  10,  10,  10,  10,  10,  10,
            10,  10,  10,  10,  10,  10,  10,  10,
            0,   0,   0,   0,   0,   0,   0,   0
    };

    // Knight table (endgame)
    private static final int[] KNIGHT_TABLE_EG = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20,   0,   0,   0,   0, -20, -40,
            -30,   0,  10,  15,  15,  10,   0, -30,
            -30,   5,  15,  20,  20,  15,   5, -30,
            -30,   0,  15,  20,  20,  15,   0, -30,
            -30,   5,  10,  15,  15,  10,   5, -30,
            -40, -20,   0,   5,   5,   0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
    };

    // Bishop table (endgame)
    private static final int[] BISHOP_TABLE_EG = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10,   0,  10,  10,  10,  10,   0, -10,
            -10,   5,   5,  10,  10,   5,   5, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   5,   5,   5,   5,   5,   5, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
    };

    // Rook table (endgame)
    private static final int[] ROOK_TABLE_EG = {
            0,   0,   0,   0,   0,   0,   0,   0,
            5,  10,  10,  10,  10,  10,  10,   5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            0,   0,   0,   5,   5,   0,   0,   0
    };

    // Queen table (endgame)
    private static final int[] QUEEN_TABLE_EG = {
            -50, -30, -30, -30, -30, -30, -30, -50,
            -30, -20, -10,   0,   0, -10, -20, -30,
            -30, -10,  20,  30,  30,  20, -10, -30,
            -30, -10,  30,  40,  40,  30, -10, -30,
            -30, -10,  30,  40,  40,  30, -10, -30,
            -30, -10,  20,  30,  30,  20, -10, -30,
            -30, -30,   0,   0,   0,   0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };

    // King table (endgame) - prioritizes centralization
    private static final int[] KING_TABLE_EG = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10,   0,   0, -10, -20, -30,
            -30, -10,  20,  30,  30,  20, -10, -30,
            -30, -10,  30,  40,  40,  30, -10, -30,
            -30, -10,  30,  40,  40,  30, -10, -30,
            -30, -10,  20,  30,  30,  20, -10, -30,
            -30, -30,   0,   0,   0,   0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };

    /**
     * Evaluate piece placement using piece-square tables
     *
     * @param board The chess board
     * @param sideToEvaluate Perspective to evaluate from
     * @param phaseValue Game phase value (0.0 = opening, 1.0 = endgame)
     * @return Score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate, double phaseValue) {
        int whiteScore = 0;
        int blackScore = 0;

        // Evaluate each piece's placement
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                // Get the piece-square value
                int squareValue = getPieceSquareValue(piece.getType(), file, rank, piece.getColor(), phaseValue);

                if (piece.getColor() == Color.WHITE) {
                    whiteScore += squareValue;
                } else {
                    blackScore += squareValue;
                }
            }
        }

        // Score from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ?
                (whiteScore - blackScore) : (blackScore - whiteScore);
    }

    /**
     * Get the piece-square value for a specific piece and position
     */
    private int getPieceSquareValue(ChessPieceType type, char file, int rank, Color color, double phaseValue) {
        // Convert position to index in the piece-square table
        int index = getTableIndex(file, rank, color);

        // Get middlegame and endgame values
        int mgValue = getMiddlegameValue(type, index);
        int egValue = getEndgameValue(type, index);

        // Interpolate between middlegame and endgame values based on game phase
        return (int)((1 - phaseValue) * mgValue + phaseValue * egValue);
    }

    /**
     * Get the index in the piece-square table for a position
     */
    private int getTableIndex(char file, int rank, Color color) {
        int fileIndex = file - 'a';
        int rankIndex = rank - 1;

        // Flip the board for black
        if (color == Color.BLACK) {
            rankIndex = 7 - rankIndex;
        }

        return rankIndex * 8 + fileIndex;
    }

    /**
     * Get the middlegame value from the piece-square table
     */
    private int getMiddlegameValue(ChessPieceType type, int index) {
        switch (type) {
            case PAWN:
                return PAWN_TABLE_MG[index];
            case KNIGHT:
                return KNIGHT_TABLE_MG[index];
            case BISHOP:
                return BISHOP_TABLE_MG[index];
            case ROOK:
                return ROOK_TABLE_MG[index];
            case QUEEN:
                return QUEEN_TABLE_MG[index];
            case KING:
                return KING_TABLE_MG[index];
            default:
                return 0;
        }
    }

    /**
     * Get the endgame value from the piece-square table
     */
    private int getEndgameValue(ChessPieceType type, int index) {
        switch (type) {
            case PAWN:
                return PAWN_TABLE_EG[index];
            case KNIGHT:
                return KNIGHT_TABLE_EG[index];
            case BISHOP:
                return BISHOP_TABLE_EG[index];
            case ROOK:
                return ROOK_TABLE_EG[index];
            case QUEEN:
                return QUEEN_TABLE_EG[index];
            case KING:
                return KING_TABLE_EG[index];
            default:
                return 0;
        }
    }
}