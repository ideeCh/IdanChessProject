package com.example.demo1.core;

/**
 * Enum representing player colors
 */
public enum Color {
    WHITE, BLACK;

    /**
     * Get the opposite color
     *
     * @return The opposite color
     */
    public Color getOpposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
