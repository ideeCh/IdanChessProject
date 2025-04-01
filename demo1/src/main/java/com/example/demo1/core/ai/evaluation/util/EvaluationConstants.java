package com.example.demo1.core.ai.evaluation.util;

import com.example.demo1.core.Position;

/**
 * Constants used in chess position evaluation
 */
public class EvaluationConstants {
    // Standard piece values in centipawns
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;

    // Pawn structure
    public static final int DOUBLED_PAWN_PENALTY = -25;
    public static final int ISOLATED_PAWN_PENALTY = -20;
    public static final int BACKWARD_PAWN_PENALTY = -15;
    public static final int PASSED_PAWN_BONUS = 20;
    public static final int PROTECTED_PAWN_BONUS = 5;

    // Bishop-specific
    public static final int BISHOP_PAIR_BONUS = 50;
    public static final int BAD_BISHOP_PENALTY = -10;

    // Knight-specific
    public static final int KNIGHT_OUTPOST_BONUS = 30;
    public static final int KNIGHT_EDGE_PENALTY = -10;

    // Rook-specific
    public static final int ROOK_OPEN_FILE_BONUS = 20;
    public static final int ROOK_SEMI_OPEN_FILE_BONUS = 10;
    public static final int ROOK_SEVENTH_RANK_BONUS = 30;
    public static final int CONNECTED_ROOKS_BONUS = 15;

    // King safety (middlegame)
    public static final int PAWN_SHIELD_BONUS = 10;
    public static final int KING_SAFETY_CHECK_PENALTY = -50;
    public static final int KING_OPEN_FILE_PENALTY = -25;

    // King activity (endgame)
    public static final int KING_CENTRALIZATION_BONUS = 10;
    public static final int KING_OPPOSITION_BONUS = 20;
    public static final int KING_PROXIMITY_BONUS = 5;

    // Mobility
    public static final int MOBILITY_BONUS = 3;

    // Development (opening)
    public static final int DEVELOPMENT_BONUS = 15;
    public static final int EARLY_QUEEN_MOVE_PENALTY = -15;
    public static final int CASTLING_BONUS = 40;
    public static final int FIANCHETTO_BONUS = 15;

    // Center control
    public static final int CENTER_CONTROL_BONUS = 10;

    // Space
    public static final int SPACE_BONUS = 5;

    // Tempo
    public static final int TEMPO_BONUS = 10;

    // Material adjustments for endgame
    public static final double KNIGHT_ENDGAME_FACTOR = 0.9;
    public static final double BISHOP_ENDGAME_FACTOR = 1.1;
    public static final double ROOK_ENDGAME_FACTOR = 1.2;

    // Center squares
    public static final Position[] CENTER_SQUARES = {
            new Position('d', 4), new Position('e', 4),
            new Position('d', 5), new Position('e', 5)
    };

    // Extended center squares
    public static final Position[] EXTENDED_CENTER_SQUARES = {
            new Position('c', 3), new Position('d', 3), new Position('e', 3), new Position('f', 3),
            new Position('c', 4), new Position('f', 4),
            new Position('c', 5), new Position('f', 5),
            new Position('c', 6), new Position('d', 6), new Position('e', 6), new Position('f', 6)
    };
}