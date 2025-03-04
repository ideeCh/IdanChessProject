package com.example.idanchessgame;


/**
 * Represents a bishop chess piece.
 */
public class Bishop extends Piece {

    /**
     * Creates a new bishop of the specified color.
     *
     * @param isWhite Whether this is a white bishop
     */
    public Bishop(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_BISHOP : PieceType.BLACK_BISHOP);
    }

    /**
     * Generates all legal moves for this bishop at the given position.
     *
     * @param square The square the bishop is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long occupied = board.getOccupiedSquares().getValue();

        // Generate attacks in each diagonal direction
        long attacks = generateDiagonalAttacks(square, occupied);

        // Remove squares occupied by friendly pieces
        Bitboard friendlyPieces = isWhite() ? board.getWhiteOccupiedSquares() : board.getBlackOccupiedSquares();
        attacks &= ~friendlyPieces.getValue();

        moves.setValue(attacks);
        return moves;
    }

    /**
     * Generates diagonal attacks for the bishop.
     */
    private long generateDiagonalAttacks(int square, long occupied) {
        long attacks = 0L;

        // Rays in all diagonal directions
        attacks |= generateRayAttacks(square, occupied, 9, Bitboard.FILE_A | Bitboard.RANK_8); // NE
        attacks |= generateRayAttacks(square, occupied, 7, Bitboard.FILE_H | Bitboard.RANK_8); // NW
        attacks |= generateRayAttacks(square, occupied, -7, Bitboard.FILE_A | Bitboard.RANK_1); // SE
        attacks |= generateRayAttacks(square, occupied, -9, Bitboard.FILE_H | Bitboard.RANK_1); // SW

        return attacks;
    }

    /**
     * Generates ray attacks in a specific direction.
     */
    private long generateRayAttacks(int square, long occupied, int shift, long edgeMask) {
        long attacks = 0L;
        long ray = 0L;
        long pos = 1L << square;

        // Generate ray in the specified direction until edge or occupied square
        while ((pos & edgeMask) == 0) {
            if (shift > 0) {
                pos <<= shift;
            } else {
                pos >>>= -shift;
            }
            ray |= pos;
            if ((pos & occupied) != 0) {
                break;
            }
        }

        attacks |= ray;
        return attacks;
    }
}