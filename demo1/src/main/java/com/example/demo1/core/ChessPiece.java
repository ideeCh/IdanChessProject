package com.example.demo1.core;

import java.util.List;

/**
 * Abstract class representing a chess piece
 */
public abstract class ChessPiece implements Cloneable {

    protected Color color;
    protected boolean hasMoved;

    /**
     * Constructor for chess piece
     *
     * @param color Color of the piece
     */
    public ChessPiece(Color color) {
        this.color = color;
        this.hasMoved = false;
    }

    /**
     * Get the color of the piece
     *
     * @return Color of the piece
     */
    public Color getColor() {
        return color;
    }

    /**
     * Check if the piece has moved
     *
     * @return true if the piece has moved, false otherwise
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Set whether the piece has moved
     *
     * @param hasMoved true if the piece has moved, false otherwise
     */
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    /**
     * Get the type of the piece
     *
     * @return Type of the piece
     */
    public abstract ChessPieceType getType();

    /**
     * Generate all potential moves for this piece
     *
     * @param board Current board state
     * @return List of potential moves
     */
    public abstract List<Move> generatePotentialMoves(Board board);

    /**
     * Create a deep copy of the piece
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ChessPiece clone = (ChessPiece) super.clone();
        return clone;
    }
}
