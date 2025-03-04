package com.example.idanchessgame;

/**
 * Abstract base class for all chess pieces.
 */
public abstract class Piece {
    protected PieceType type;

    /**
     * Creates a new piece of the specified type.
     *
     * @param type The piece type
     */
    public Piece(PieceType type) {
        this.type = type;
    }

    /**
     * Gets the type of this piece.
     *
     * @return The piece type
     */
    public PieceType getType() {
        return type;
    }

    /**
     * Checks if this is a white piece.
     *
     * @return true if white, false if black
     */
    public boolean isWhite() {
        return type.isWhite();
    }

    /**
     * Gets the notation character for this piece.
     *
     * @return The notation character
     */
    public char getNotation() {
        return type.getNotation();
    }

    /**
     * Generates all legal moves for this piece at the given position.
     *
     * @param square The square the piece is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    public abstract Bitboard getLegalMoves(int square, Board board);
}