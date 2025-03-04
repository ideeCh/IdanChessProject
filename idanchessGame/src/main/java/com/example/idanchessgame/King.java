package com.example.idanchessgame;


/**
 * Represents a king chess piece.
 */
public class King extends Piece {

    /**
     * Creates a new king of the specified color.
     *
     * @param isWhite Whether this is a white king
     */
    public King(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_KING : PieceType.BLACK_KING);
    }

    /**
     * Generates all legal moves for this king at the given position.
     *
     * @param square The square the king is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long king = 1L << square;
        long attacks = 0L;

        // King move patterns: 1 square in any direction
        attacks |= (king << 8); // N
        attacks |= (king << 9) & ~Bitboard.FILE_A; // NE
        attacks |= (king << 1) & ~Bitboard.FILE_A; // E
        attacks |= (king >> 7) & ~Bitboard.FILE_A; // SE
        attacks |= (king >> 8); // S
        attacks |= (king >> 9) & ~Bitboard.FILE_H; // SW
        attacks |= (king >> 1) & ~Bitboard.FILE_H; // W
        attacks |= (king << 7) & ~Bitboard.FILE_H; // NW

        // Remove squares occupied by friendly pieces
        Bitboard friendlyPieces = isWhite() ? board.getWhiteOccupiedSquares() : board.getBlackOccupiedSquares();
        attacks &= ~friendlyPieces.getValue();

        // Add normal moves
        moves.setValue(attacks);

        // Add castling moves
        addCastlingMoves(square, board, moves);

        return moves;
    }

    /**
     * Adds castling moves if they are legal.
     */
    private void addCastlingMoves(int square, Board board, Bitboard moves) {
        if (isWhite()) {
            // White king must be on e1
            if (square != 4) {
                return;
            }

            // Check if castling rights are available
            if (board.getCastlingRight(CastlingRight.WHITE_KINGSIDE)) {
                // Kingside castling
                if (!board.isSquareOccupied(5) && !board.isSquareOccupied(6) &&
                        !board.isSquareAttacked(4, false) && !board.isSquareAttacked(5, false) &&
                        !board.isSquareAttacked(6, false)) {
                    moves.setBit(6);
                }
            }

            if (board.getCastlingRight(CastlingRight.WHITE_QUEENSIDE)) {
                // Queenside castling
                if (!board.isSquareOccupied(1) && !board.isSquareOccupied(2) && !board.isSquareOccupied(3) &&
                        !board.isSquareAttacked(4, false) && !board.isSquareAttacked(3, false) &&
                        !board.isSquareAttacked(2, false)) {
                    moves.setBit(2);
                }
            }
        } else {
            // Black king must be on e8
            if (square != 60) {
                return;
            }

            // Check if castling rights are available
            if (board.getCastlingRight(CastlingRight.BLACK_KINGSIDE)) {
                // Kingside castling
                if (!board.isSquareOccupied(61) && !board.isSquareOccupied(62) &&
                        !board.isSquareAttacked(60, true) && !board.isSquareAttacked(61, true) &&
                        !board.isSquareAttacked(62, true)) {
                    moves.setBit(62);
                }
            }

            if (board.getCastlingRight(CastlingRight.BLACK_QUEENSIDE)) {
                // Queenside castling
                if (!board.isSquareOccupied(57) && !board.isSquareOccupied(58) && !board.isSquareOccupied(59) &&
                        !board.isSquareAttacked(60, true) && !board.isSquareAttacked(59, true) &&
                        !board.isSquareAttacked(58, true)) {
                    moves.setBit(58);
                }
            }
        }
    }
}