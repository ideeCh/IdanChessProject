package com.example.idanchessgame;
/**
 * Represents a pawn chess piece.
 */
public class Pawn extends Piece {

    /**
     * Creates a new pawn of the specified color.
     *
     * @param isWhite Whether this is a white pawn
     */
    public Pawn(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_PAWN : PieceType.BLACK_PAWN);
    }

    /**
     * Generates all legal moves for this pawn at the given position.
     *
     * @param square The square the pawn is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long occupied = board.getOccupiedSquares().getValue();

        if (isWhite()) {
            // One square forward
            if (!board.isSquareOccupied(square + 8)) {
                moves.setBit(square + 8);

                // Two squares forward from starting position
                if (square >= 8 && square <= 15 && !board.isSquareOccupied(square + 16)) {
                    moves.setBit(square + 16);
                }
            }

            // Captures
            if ((square & 7) != 0 && // Not on a-file
                    (board.isSquareOccupied(square + 7) && !board.getPieceAt(square + 7).isWhite())) {
                moves.setBit(square + 7);
            }

            if ((square & 7) != 7 && // Not on h-file
                    (board.isSquareOccupied(square + 9) && !board.getPieceAt(square + 9).isWhite())) {
                moves.setBit(square + 9);
            }

            // En passant
            int epSquare = board.getEnPassantSquare();
            if (epSquare != -1) {
                if ((square & 7) != 0 && square + 7 == epSquare) { // Capture to the left
                    moves.setBit(epSquare);
                } else if ((square & 7) != 7 && square + 9 == epSquare) { // Capture to the right
                    moves.setBit(epSquare);
                }
            }
        } else {
            // One square forward
            if (!board.isSquareOccupied(square - 8)) {
                moves.setBit(square - 8);

                // Two squares forward from starting position
                if (square >= 48 && square <= 55 && !board.isSquareOccupied(square - 16)) {
                    moves.setBit(square - 16);
                }
            }

            // Captures
            if ((square & 7) != 7 && // Not on h-file
                    (board.isSquareOccupied(square - 7) && board.getPieceAt(square - 7).isWhite())) {
                moves.setBit(square - 7);
            }

            if ((square & 7) != 0 && // Not on a-file
                    (board.isSquareOccupied(square - 9) && board.getPieceAt(square - 9).isWhite())) {
                moves.setBit(square - 9);
            }

            // En passant
            int epSquare = board.getEnPassantSquare();
            if (epSquare != -1) {
                if ((square & 7) != 7 && square - 7 == epSquare) { // Capture to the right
                    moves.setBit(epSquare);
                } else if ((square & 7) != 0 && square - 9 == epSquare) { // Capture to the left
                    moves.setBit(epSquare);
                }
            }
        }

        return moves;
    }
}