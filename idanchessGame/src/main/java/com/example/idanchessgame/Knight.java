package com.example.idanchessgame;

/**
 * Represents a knight chess piece.
 */
public class Knight extends Piece {

    /**
     * Creates a new knight of the specified color.
     *
     * @param isWhite Whether this is a white knight
     */
    public Knight(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_KNIGHT : PieceType.BLACK_KNIGHT);
    }

    /**
     * Generates all legal moves for this knight at the given position.
     *
     * @param square The square the knight is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long knight = 1L << square;
        long attacks = 0L;

        // Knight move patterns: 2 squares in one direction, 1 square perpendicular
        attacks |= (knight << 17) & ~Bitboard.FILE_A; // NNE
        attacks |= (knight << 10) & ~(Bitboard.FILE_A | Bitboard.FILE_B); // ENE
        attacks |= (knight >> 6) & ~(Bitboard.FILE_A | Bitboard.FILE_B); // ESE
        attacks |= (knight >> 15) & ~Bitboard.FILE_A; // SSE
        attacks |= (knight >> 17) & ~Bitboard.FILE_H; // SSW
        attacks |= (knight >> 10) & ~(Bitboard.FILE_G | Bitboard.FILE_H); // WSW
        attacks |= (knight << 6) & ~(Bitboard.FILE_G | Bitboard.FILE_H); // WNW
        attacks |= (knight << 15) & ~Bitboard.FILE_H; // NNW

        // Remove squares occupied by friendly pieces
        Bitboard friendlyPieces = isWhite() ? board.getWhiteOccupiedSquares() : board.getBlackOccupiedSquares();
        attacks &= ~friendlyPieces.getValue();

        moves.setValue(attacks);
        return moves;
    }
}