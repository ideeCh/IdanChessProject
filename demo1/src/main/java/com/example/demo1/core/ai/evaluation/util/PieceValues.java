package com.example.demo1.core.ai.evaluation.util;

import com.example.demo1.core.ChessPieceType;
import  com.example.demo1.core.*;
import com.example.demo1.core.ai.GamePhase;

import java.util.EnumMap;
import java.util.Map;

import static com.example.demo1.core.ai.GamePhase.*;

/**
 * Utility class for accessing piece values in different game phases
 */
public class PieceValues {

    private static final Map<ChessPieceType, Integer> PIECE_INDICES;
    private static final Map<ChessPieceType, Integer> MOBILITY_VALUES;

    static {
        PIECE_INDICES = new EnumMap<>(ChessPieceType.class);
        PIECE_INDICES.put(ChessPieceType.PAWN, 1);
        PIECE_INDICES.put(ChessPieceType.KNIGHT, 2);
        PIECE_INDICES.put(ChessPieceType.BISHOP, 3);
        PIECE_INDICES.put(ChessPieceType.ROOK, 4);
        PIECE_INDICES.put(ChessPieceType.QUEEN, 5);
        PIECE_INDICES.put(ChessPieceType.KING, 6);

        MOBILITY_VALUES = new EnumMap<>(ChessPieceType.class);
        MOBILITY_VALUES.put(ChessPieceType.PAWN, 1);
        MOBILITY_VALUES.put(ChessPieceType.KNIGHT, 8);
        MOBILITY_VALUES.put(ChessPieceType.BISHOP, 13);
        MOBILITY_VALUES.put(ChessPieceType.ROOK, 14);
        MOBILITY_VALUES.put(ChessPieceType.QUEEN, 27);
        MOBILITY_VALUES.put(ChessPieceType.KING, 8);
    }

    // Standard piece values in centipawns
    private static final int[] STANDARD_VALUES = {
            0,      // Not used (0-index placeholder)
            100,    // PAWN
            320,    // KNIGHT
            330,    // BISHOP
            500,    // ROOK
            900,    // QUEEN
            20000   // KING (arbitrary high value)
    };

    // Middlegame piece values
    private static final int[] MIDDLEGAME_VALUES = {
            0,      // Not used (0-index placeholder)
            100,    // PAWN
            320,    // KNIGHT
            330,    // BISHOP
            500,    // ROOK
            900,    // QUEEN
            20000   // KING
    };

    // Endgame piece values
    private static final int[] ENDGAME_VALUES = {
            0,      // Not used (0-index placeholder)
            100,    // PAWN
            290,    // KNIGHT (less valuable in open positions)
            360,    // BISHOP (more valuable in open positions)
            600,    // ROOK (more valuable in endgame)
            900,    // QUEEN
            20000   // KING
    };

    /**
     * Get the value of a piece in a specific game phase
     *
     * @param pieceType The type of chess piece
     * @param phase The current game phase
     * @return The piece value in centipawns
     */
    public static int getValue(ChessPieceType pieceType, GamePhase phase) {
        int index = getIndex(pieceType);

        if (phase == OPENING || phase == MIDDLEGAME) {
                return MIDDLEGAME_VALUES[index];
        } else if (phase == ENDGAME) {
                return ENDGAME_VALUES[index];
        }
                return STANDARD_VALUES[index];
        }

    /**
     * Get the value of a piece, interpolating between middlegame and endgame values
     *
     * @param pieceType The type of chess piece
     * @param phaseValue Phase value between 0.0 (opening) and 1.0 (endgame)
     * @return Interpolated piece value
     */
    public static int getInterpolatedValue(ChessPieceType pieceType, double phaseValue) {
        int index = getIndex(pieceType);

        // Clamp phase value between 0 and 1
        phaseValue = Math.max(0.0, Math.min(1.0, phaseValue));

        // Interpolate between middlegame and endgame values
        return (int)((1 - phaseValue) * MIDDLEGAME_VALUES[index] +
                phaseValue * ENDGAME_VALUES[index]);
    }

    /**
     * Get the standard value of a piece (middlegame)
     *
     * @param pieceType The type of chess piece
     * @return Standard piece value in centipawns
     */
    public static int getStandardValue(ChessPieceType pieceType) {
        return STANDARD_VALUES[getIndex(pieceType)];
    }

    /**
     * Convert piece type to array index
     */
    private static int getIndex(ChessPieceType pieceType) {
        return PIECE_INDICES.getOrDefault(pieceType, 0);
    }

    /**
     * Get a piece's approximate mobility potential
     *
     * @param pieceType The type of chess piece
     * @return Approximate maximum mobility
     */
    public static int getMobilityPotential(ChessPieceType pieceType) {
        return MOBILITY_VALUES.getOrDefault(pieceType, 0);
    }

    /**
     * Get piece value ratio for computing exchanges
     *
     * @param attacker Attacking piece type
     * @param victim Victim piece type
     * @return Exchange value ratio (>1 means favorable, <1 means unfavorable)
     */
    public static double getExchangeRatio(ChessPieceType attacker, ChessPieceType victim) {
        int attackerValue = getStandardValue(attacker);
        int victimValue = getStandardValue(victim);

        if (attackerValue == 0) return 0;
        return (double)victimValue / attackerValue;
    }

    /**
     * Check if an exchange is favorable
     *
     * @param attacker Attacking piece type
     * @param victim Victim piece type
     * @return True if the exchange is favorable for the attacker
     */
    public static boolean isFavorableExchange(ChessPieceType attacker, ChessPieceType victim) {
        return getExchangeRatio(attacker, victim) >= 1.0;
    }
}