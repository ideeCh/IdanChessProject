package com.example.idanchessgame;

/**
 * Represents the castling rights in a chess game.
 */
public enum CastlingRight {
    WHITE_KINGSIDE('K'),
    WHITE_QUEENSIDE('Q'),
    BLACK_KINGSIDE('k'),
    BLACK_QUEENSIDE('q');

    private final char notation;

    CastlingRight(char notation) {
        this.notation = notation;
    }

    /**
     * Gets the FEN notation character for this castling right.
     *
     * @return The FEN notation character
     */
    public char getNotation() {
        return notation;
    }

    /**
     * Gets the castling right corresponding to the given FEN notation character.
     *
     * @param notation The FEN notation character
     * @return The corresponding castling right, or null if not found
     */
    public static CastlingRight fromNotation(char notation) {
        for (CastlingRight right : values()) {
            if (right.notation == notation) {
                return right;
            }
        }
        return null;
    }
}