package com.example.idanchessgame;

/**
 * Represents the state of the board after a move, used for undoing moves.
 */
public class MoveState {
    private boolean[] castlingRights;
    private int enPassantSquare;
    private int halfMoveClock;
    private Move move;

    /**
     * Creates a new move state with the given information.
     *
     * @param castlingRights The castling rights at the time of the move
     * @param enPassantSquare The en passant square at the time of the move
     * @param halfMoveClock The half-move clock at the time of the move
     * @param move The move that was made
     */
    public MoveState(boolean[] castlingRights, int enPassantSquare, int halfMoveClock, Move move) {
        this.castlingRights = castlingRights;
        this.enPassantSquare = enPassantSquare;
        this.halfMoveClock = halfMoveClock;
        this.move = move;
    }

    /**
     * Gets the castling rights at the time of the move.
     *
     * @return The castling rights
     */
    public boolean[] getCastlingRights() {
        return castlingRights;
    }

    /**
     * Gets the en passant square at the time of the move.
     *
     * @return The en passant square
     */
    public int getEnPassantSquare() {
        return enPassantSquare;
    }

    /**
     * Gets the half-move clock at the time of the move.
     *
     * @return The half-move clock
     */
    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    /**
     * Gets the move that was made.
     *
     * @return The move
     */
    public Move getMove() {
        return move;
    }
}