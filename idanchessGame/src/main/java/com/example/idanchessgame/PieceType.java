package com.example.idanchessgame;

/**
 * Represents the different types of chess pieces.
 */
public enum PieceType {
    WHITE_PAWN('P', true),
    WHITE_KNIGHT('N', true),
    WHITE_BISHOP('B', true),
    WHITE_ROOK('R', true),
    WHITE_QUEEN('Q', true),
    WHITE_KING('K', true),
    BLACK_PAWN('p', false),
    BLACK_KNIGHT('n', false),
    BLACK_BISHOP('b', false),
    BLACK_ROOK('r', false),
    BLACK_QUEEN('q', false),
    BLACK_KING('k', false);

    private final char notation;
    private final boolean isWhite;

    PieceType(char notation, boolean isWhite) {
        this.notation = notation;
        this.isWhite = isWhite;
    }

    /**
     * Gets the notation character for this piece type.
     *
     * @return The notation character
     */
    public char getNotation() {
        return notation;
    }

    /**
     * Checks if this piece is white.
     *
     * @return true if white, false if black
     */
    public boolean isWhite() {
        return isWhite;
    }

    /**
     * Gets the piece type corresponding to the given notation character.
     *
     * @param notation The notation character
     * @return The corresponding piece type, or null if not found
     */
    public static PieceType fromNotation(char notation) {
        for (PieceType type : values()) {
            if (type.notation == notation) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the pawn type (white or black) based on the color.
     *
     * @param isWhite Whether the pawn is white
     * @return The corresponding pawn type
     */
    public static PieceType getPawnType(boolean isWhite) {
        return isWhite ? WHITE_PAWN : BLACK_PAWN;
    }

    /**
     * Gets the knight type (white or black) based on the color.
     *
     * @param isWhite Whether the knight is white
     * @return The corresponding knight type
     */
    public static PieceType getKnightType(boolean isWhite) {
        return isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
    }

    /**
     * Gets the bishop type (white or black) based on the color.
     *
     * @param isWhite Whether the bishop is white
     * @return The corresponding bishop type
     */
    public static PieceType getBishopType(boolean isWhite) {
        return isWhite ? WHITE_BISHOP : BLACK_BISHOP;
    }

    /**
     * Gets the rook type (white or black) based on the color.
     *
     * @param isWhite Whether the rook is white
     * @return The corresponding rook type
     */
    public static PieceType getRookType(boolean isWhite) {
        return isWhite ? WHITE_ROOK : BLACK_ROOK;
    }

    /**
     * Gets the queen type (white or black) based on the color.
     *
     * @param isWhite Whether the queen is white
     * @return The corresponding queen type
     */
    public static PieceType getQueenType(boolean isWhite) {
        return isWhite ? WHITE_QUEEN : BLACK_QUEEN;
    }

    /**
     * Gets the king type (white or black) based on the color.
     *
     * @param isWhite Whether the king is white
     * @return The corresponding king type
     */
    public static PieceType getKingType(boolean isWhite) {
        return isWhite ? WHITE_KING : BLACK_KING;
    }
}