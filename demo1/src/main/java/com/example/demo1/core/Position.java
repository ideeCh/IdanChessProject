package com.example.demo1.core;

/**
 * Class representing a position on the chess board
 */
public class Position {

    private char file; // a-h
    private int rank;  // 1-8

    /**
     * Constructor for position
     *
     * @param file File (column) on the board (a-h)
     * @param rank Rank (row) on the board (1-8)
     */
    public Position(char file, int rank) {
        this.file = file;
        this.rank = rank;
    }

    public Position(int kingFile, int rank) {
    }

    /**
     * Get the file (column)
     *
     * @return File character (a-h)
     */
    public char getFile() {
        return file;
    }

    /**
     * Get the rank (row)
     *
     * @return Rank number (1-8)
     */
    public int getRank() {
        return rank;
    }

    /**
     * Check if a position is valid (on the board)
     *
     * @return true if the position is valid, false otherwise
     */
    public boolean isValid() {
        return file >= 'a' && file <= 'h' && rank >= 1 && rank <= 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Position other = (Position) obj;
        return file == other.file && rank == other.rank;
    }

    @Override
    public int hashCode() {
        return 31 * (int) file + rank;
    }

    @Override
    public String toString() {
        return "" + file + rank;
    }
}
