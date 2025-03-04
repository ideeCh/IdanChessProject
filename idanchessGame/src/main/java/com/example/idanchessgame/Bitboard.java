package com.example.idanchessgame;

/**
 * Represents a chess board using a 64-bit long integer (bitboard).
 * Each bit corresponds to a square on the chess board.
 */
public class Bitboard {
    private long bitboard;

    /**
     * Creates an empty bitboard (all bits set to 0).
     */
    public Bitboard() {
        this.bitboard = 0L;
    }

    /**
     * Creates a bitboard with the specified value.
     *
     * @param bitboard The 64-bit value to initialize the bitboard with
     */
    public Bitboard(long bitboard) {
        this.bitboard = bitboard;
    }

    /**
     * Gets the raw 64-bit value of this bitboard.
     *
     * @return The 64-bit long integer representing this bitboard
     */
    public long getValue() {
        return bitboard;
    }

    /**
     * Sets the raw 64-bit value of this bitboard.
     *
     * @param bitboard The 64-bit long integer to set this bitboard to
     */
    public void setValue(long bitboard) {
        this.bitboard = bitboard;
    }

    /**
     * Sets the bit at the specified square to 1.
     *
     * @param square The square index (0-63) to set
     */
    public void setBit(int square) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException("Square index must be between 0 and 63");
        }
        
        // Debug check to see if bit is already set
        if ((bitboard & (1L << square)) != 0) {
            System.out.println("Warning: Bit at square " + square + " is already set");
            return; // Don't attempt to set it again if it's already set
        }
        
        long oldValue = bitboard;
        bitboard |= (1L << square);
        
        // Debug output to verify bit operation was correct
        if (oldValue == bitboard) {
            System.out.println("No change after setting bit at square " + square);
        }
    }

    /**
     * Clears the bit at the specified square (sets it to 0).
     *
     * @param square The square index (0-63) to clear
     */
    public void clearBit(int square) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException("Square index must be between 0 and 63");
        }
        
        // Debug check to see if bit is already cleared
        if ((bitboard & (1L << square)) == 0) {
            System.out.println("Warning: Bit at square " + square + " is already cleared");
        }
        
        long oldValue = bitboard;
        bitboard &= ~(1L << square);
        
        // Debug output to verify bit operation was correct
        if (oldValue == bitboard) {
            System.out.println("No change after clearing bit at square " + square);
        }
    }
    /**
     * Checks if the bit at the specified square is set to 1.
     *
     * @param square The square index (0-63) to check
     * @return true if the bit is set, false otherwise
     */
    public boolean isBitSet(int square) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException("Square index must be between 0 and 63");
        }
        return (bitboard & (1L << square)) != 0;
    }

    /**
     * Returns a new bitboard with the bit at the specified square set to 1.
     *
     * @param square The square index (0-63) to set
     * @return A new bitboard with the specified bit set
     */
    public Bitboard withBitSet(int square) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException("Square index must be between 0 and 63");
        }
        return new Bitboard(bitboard | (1L << square));
    }

    /**
     * Returns a new bitboard with the bit at the specified square cleared (set to 0).
     *
     * @param square The square index (0-63) to clear
     * @return A new bitboard with the specified bit cleared
     */
    public Bitboard withBitCleared(int square) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException("Square index must be between 0 and 63");
        }
        return new Bitboard(bitboard & ~(1L << square));
    }

    /**
     * Gets a bitboard representing all the squares that are occupied
     * (bitwise OR of all piece bitboards).
     *
     * @param whitePawns Bitboard for white pawns
     * @param whiteKnights Bitboard for white knights
     * @param whiteBishops Bitboard for white bishops
     * @param whiteRooks Bitboard for white rooks
     * @param whiteQueens Bitboard for white queens
     * @param whiteKing Bitboard for white king
     * @param blackPawns Bitboard for black pawns
     * @param blackKnights Bitboard for black knights
     * @param blackBishops Bitboard for black bishops
     * @param blackRooks Bitboard for black rooks
     * @param blackQueens Bitboard for black queens
     * @param blackKing Bitboard for black king
     * @return Bitboard with all occupied squares
     */
    public static Bitboard getOccupiedSquares(
            Bitboard whitePawns, Bitboard whiteKnights, Bitboard whiteBishops,
            Bitboard whiteRooks, Bitboard whiteQueens, Bitboard whiteKing,
            Bitboard blackPawns, Bitboard blackKnights, Bitboard blackBishops,
            Bitboard blackRooks, Bitboard blackQueens, Bitboard blackKing) {

        long occupied = whitePawns.getValue() | whiteKnights.getValue() | whiteBishops.getValue() |
                whiteRooks.getValue() | whiteQueens.getValue() | whiteKing.getValue() |
                blackPawns.getValue() | blackKnights.getValue() | blackBishops.getValue() |
                blackRooks.getValue() | blackQueens.getValue() | blackKing.getValue();

        return new Bitboard(occupied);
    }

    /**
     * Gets a bitboard representing all the squares that are occupied by white pieces.
     *
     * @param whitePawns Bitboard for white pawns
     * @param whiteKnights Bitboard for white knights
     * @param whiteBishops Bitboard for white bishops
     * @param whiteRooks Bitboard for white rooks
     * @param whiteQueens Bitboard for white queens
     * @param whiteKing Bitboard for white king
     * @return Bitboard with all white-occupied squares
     */
    public static Bitboard getWhiteOccupiedSquares(
            Bitboard whitePawns, Bitboard whiteKnights, Bitboard whiteBishops,
            Bitboard whiteRooks, Bitboard whiteQueens, Bitboard whiteKing) {

        long occupied = whitePawns.getValue() | whiteKnights.getValue() | whiteBishops.getValue() |
                whiteRooks.getValue() | whiteQueens.getValue() | whiteKing.getValue();

        return new Bitboard(occupied);
    }

    /**
     * Gets a bitboard representing all the squares that are occupied by black pieces.
     *
     * @param blackPawns Bitboard for black pawns
     * @param blackKnights Bitboard for black knights
     * @param blackBishops Bitboard for black bishops
     * @param blackRooks Bitboard for black rooks
     * @param blackQueens Bitboard for black queens
     * @param blackKing Bitboard for black king
     * @return Bitboard with all black-occupied squares
     */
    public static Bitboard getBlackOccupiedSquares(
            Bitboard blackPawns, Bitboard blackKnights, Bitboard blackBishops,
            Bitboard blackRooks, Bitboard blackQueens, Bitboard blackKing) {

        long occupied = blackPawns.getValue() | blackKnights.getValue() | blackBishops.getValue() |
                blackRooks.getValue() | blackQueens.getValue() | blackKing.getValue();

        return new Bitboard(occupied);
    }

    // Constants for ranks and files
    public static final long RANK_1 = 0xFFL;
    public static final long RANK_2 = 0xFF00L;
    public static final long RANK_3 = 0xFF0000L;
    public static final long RANK_4 = 0xFF000000L;
    public static final long RANK_5 = 0xFF00000000L;
    public static final long RANK_6 = 0xFF0000000000L;
    public static final long RANK_7 = 0xFF000000000000L;
    public static final long RANK_8 = 0xFF00000000000000L;

    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_B = 0x0202020202020202L;
    public static final long FILE_C = 0x0404040404040404L;
    public static final long FILE_D = 0x0808080808080808L;
    public static final long FILE_E = 0x1010101010101010L;
    public static final long FILE_F = 0x2020202020202020L;
    public static final long FILE_G = 0x4040404040404040L;
    public static final long FILE_H = 0x8080808080808080L;

    /**
     * Prints the bitboard in a human-readable format (8x8 grid).
     */
    public void print() {
        System.out.println("  a b c d e f g h");
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                System.out.print(isBitSet(square) ? "1 " : "0 ");
            }
            System.out.println(rank + 1);
        }
        System.out.println("  a b c d e f g h");
    }

    /**
     * Gets the least significant bit position (index of the rightmost set bit).
     *
     * @return The index (0-63) of the least significant bit, or -1 if no bits are set
     */
    public int getLeastSignificantBitIndex() {
        if (bitboard == 0) {
            return -1;
        }
        return Long.numberOfTrailingZeros(bitboard);
    }

    /**
     * Clears the least significant bit (sets the rightmost 1 bit to 0).
     */
    public void clearLeastSignificantBit() {
        if (bitboard != 0) {
            bitboard &= (bitboard - 1);
        }
    }

    /**
     * Counts the number of bits set to 1 in this bitboard (population count).
     *
     * @return The number of bits set
     */
    public int countBits() {
        return Long.bitCount(bitboard);
    }

    /**
     * Combines this bitboard with another using bitwise OR.
     *
     * @param other The other bitboard
     * @return A new bitboard representing the union
     */
    public Bitboard or(Bitboard other) {
        return new Bitboard(this.bitboard | other.bitboard);
    }

    /**
     * Combines this bitboard with another using bitwise AND.
     *
     * @param other The other bitboard
     * @return A new bitboard representing the intersection
     */
    public Bitboard and(Bitboard other) {
        return new Bitboard(this.bitboard & other.bitboard);
    }

    /**
     * Creates a new bitboard with all bits flipped.
     *
     * @return A new bitboard representing the complement
     */
    public Bitboard not() {
        return new Bitboard(~this.bitboard);
    }

    /**
     * Creates a new bitboard with bits shifted left (or up on a chess board).
     *
     * @param squares The number of squares to shift
     * @return A new bitboard with bits shifted left
     */
    public Bitboard shiftLeft(int squares) {
        return new Bitboard(this.bitboard << squares);
    }

    /**
     * Creates a new bitboard with bits shifted right (or down on a chess board).
     *
     * @param squares The number of squares to shift
     * @return A new bitboard with bits shifted right
     */
    public Bitboard shiftRight(int squares) {
        return new Bitboard(this.bitboard >>> squares);
    }
}