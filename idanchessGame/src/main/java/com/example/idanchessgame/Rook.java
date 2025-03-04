package com.example.idanchessgame;
/**
 * Represents a rook chess piece.
 */
public class Rook extends Piece {

    /**
     * Creates a new rook of the specified color.
     *
     * @param isWhite Whether this is a white rook
     */
    public Rook(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_ROOK : PieceType.BLACK_ROOK);
    }

    /**
     * Generates all legal moves for this rook at the given position.
     *
     * @param square The square the rook is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long occupied = board.getOccupiedSquares().getValue();

        // Generate attacks in each orthogonal direction
        long attacks = generateOrthogonalAttacks(square, occupied);

        // Remove squares occupied by friendly pieces
        Bitboard friendlyPieces = isWhite() ? board.getWhiteOccupiedSquares() : board.getBlackOccupiedSquares();
        attacks &= ~friendlyPieces.getValue();

        moves.setValue(attacks);
        return moves;
    }

    /**
     * Generates orthogonal attacks for the rook.
     */
    private long generateOrthogonalAttacks(int square, long occupied) {
        long attacks = 0L;

        // Rays in all orthogonal directions
        attacks |= generateRayAttacks(square, occupied, 8, Bitboard.RANK_8); // N
        attacks |= generateRayAttacks(square, occupied, 1, Bitboard.FILE_H); // E
        attacks |= generateRayAttacks(square, occupied, -8, Bitboard.RANK_1); // S
        attacks |= generateRayAttacks(square, occupied, -1, Bitboard.FILE_A); // W

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