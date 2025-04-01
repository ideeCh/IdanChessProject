package com.example.demo1.core;

/**
 * Factory class for creating chess pieces
 */
public class ChessPieceFactory {

    /**
     * Create a chess piece
     *
     * @param type  Type of piece
     * @param color Color of piece
     * @return Created chess piece
     */
    public static ChessPiece createPiece(ChessPieceType type, Color color) {
        switch (type) {
            case KING:
                return new King(color);
            case QUEEN:
                return new Queen(color);
            case ROOK:
                return new Rook(color);
            case BISHOP:
                return new Bishop(color);
            case KNIGHT:
                return new Knight(color);
            case PAWN:
                return new Pawn(color);
            default:
                throw new IllegalArgumentException("Unknown piece type: " + type);
        }
    }
}
