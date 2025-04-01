package com.example.demo1.core;


/**
 * Bitboard representation for the chess board.
 * A bitboard is a 64-bit long where each bit represents a square on the chess board.
 * This provides very efficient operations for move generation, position analysis, etc.
 */
public class Bitboard {

    /**
     * Constants for bitboard operations
     */
    // Files (columns)
    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_B = FILE_A << 1;
    public static final long FILE_C = FILE_A << 2;
    public static final long FILE_D = FILE_A << 3;
    public static final long FILE_E = FILE_A << 4;
    public static final long FILE_F = FILE_A << 5;
    public static final long FILE_G = FILE_A << 6;
    public static final long FILE_H = FILE_A << 7;

    // Ranks (rows)
    public static final long RANK_1 = 0x00000000000000FFL;
    public static final long RANK_2 = RANK_1 << (8 * 1);
    public static final long RANK_3 = RANK_1 << (8 * 2);
    public static final long RANK_4 = RANK_1 << (8 * 3);
    public static final long RANK_5 = RANK_1 << (8 * 4);
    public static final long RANK_6 = RANK_1 << (8 * 5);
    public static final long RANK_7 = RANK_1 << (8 * 6);
    public static final long RANK_8 = RANK_1 << (8 * 7);

    // Diagonals
    public static final long DIAG_A1_H8 = 0x8040201008040201L;
    public static final long ANTI_DIAG_H1_A8 = 0x0102040810204080L;

    // Colors
    public static final long LIGHT_SQUARES = 0x55AA55AA55AA55AAL;
    public static final long DARK_SQUARES = ~LIGHT_SQUARES;

    // All squares
    public static final long ALL_SQUARES = ~0L;

    // Empty board
    public static final long EMPTY = 0L;

    /**
     * Convert a square position (file, rank) to a bitboard with a single bit set
     * @param file File (0-7, where 0 is a-file)
     * @param rank Rank (0-7, where 0 is 1st rank)
     * @return Bitboard with a single bit set at the specified position
     */
    public static long squareToBitboard(int file, int rank) {
        return 1L << (rank * 8 + file);
    }

    /**
     * Convert a square position in algebraic notation (e.g., "e4") to a bitboard
     * @param algebraic Algebraic notation of the square (e.g., "e4")
     * @return Bitboard with a single bit set at the specified position
     */
    public static long squareToBitboard(String algebraic) {
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        return squareToBitboard(file, rank);
    }

    /**
     * Convert a bitboard to a square position (index 0-63)
     * @param bitboard Bitboard with a single bit set
     * @return Index of the set bit (0-63)
     */
    public static int bitboardToSquare(long bitboard) {
        if (bitboard == 0 || (bitboard & (bitboard - 1)) != 0) {
            throw new IllegalArgumentException("Bitboard must have exactly one bit set");
        }

        return Long.numberOfTrailingZeros(bitboard);
    }

    /**
     * Convert a square index (0-63) to algebraic notation (e.g., "e4")
     * @param square Square index (0-63)
     * @return Algebraic notation
     */
    public static String squareToAlgebraic(int square) {
        int file = square % 8;
        int rank = square / 8;

        return "" + (char)('a' + file) + (char)('1' + rank);
    }

    /**
     * Convert a bitboard to algebraic notation
     * @param bitboard Bitboard with a single bit set
     * @return Algebraic notation
     */
    public static String bitboardToAlgebraic(long bitboard) {
        return squareToAlgebraic(bitboardToSquare(bitboard));
    }

    /**
     * Count the number of set bits in a bitboard
     * @param bitboard Bitboard
     * @return Number of set bits
     */
    public static int popCount(long bitboard) {
        return Long.bitCount(bitboard);
    }

    /**
     * Get the least significant bit of a bitboard
     * @param bitboard Bitboard
     * @return Bitboard with only the least significant bit set, or 0 if bitboard is 0
     */
    public static long lsb(long bitboard) {
        return bitboard & -bitboard;
    }

    /**
     * Get the index of the least significant bit of a bitboard
     * @param bitboard Bitboard
     * @return Index of the least significant bit (0-63), or -1 if bitboard is 0
     */
    public static int lsbIndex(long bitboard) {
        if (bitboard == 0) {
            return -1;
        }
        return Long.numberOfTrailingZeros(bitboard);
    }

    /**
     * Remove the least significant bit from a bitboard
     * @param bitboard Bitboard
     * @return Bitboard with the least significant bit removed
     */
    public static long popLsb(long bitboard) {
        return bitboard & (bitboard - 1);
    }

    /**
     * Get the most significant bit of a bitboard
     * @param bitboard Bitboard
     * @return Bitboard with only the most significant bit set, or 0 if bitboard is 0
     */
    public static long msb(long bitboard) {
        if (bitboard == 0) {
            return 0;
        }

        bitboard |= (bitboard >> 1);
        bitboard |= (bitboard >> 2);
        bitboard |= (bitboard >> 4);
        bitboard |= (bitboard >> 8);
        bitboard |= (bitboard >> 16);
        bitboard |= (bitboard >> 32);

        return bitboard & ~(bitboard >> 1);
    }

    /**
     * Get the index of the most significant bit of a bitboard
     * @param bitboard Bitboard
     * @return Index of the most significant bit (0-63), or -1 if bitboard is 0
     */
    public static int msbIndex(long bitboard) {
        if (bitboard == 0) {
            return -1;
        }
        return 63 - Long.numberOfLeadingZeros(bitboard);
    }

    /**
     * Shift a bitboard in a particular direction
     * @param bitboard Bitboard
     * @param direction Direction to shift (N, NE, E, SE, S, SW, W, NW)
     * @return Shifted bitboard
     */
    public static long shift(long bitboard, Direction direction) {
        switch (direction) {
            case NORTH: return (bitboard << 8) & ALL_SQUARES;
            case NORTH_EAST: return ((bitboard & ~FILE_H) << 9) & ALL_SQUARES;
            case EAST: return ((bitboard & ~FILE_H) << 1) & ALL_SQUARES;
            case SOUTH_EAST: return ((bitboard & ~FILE_H) >> 7) & ALL_SQUARES;
            case SOUTH: return (bitboard >> 8) & ALL_SQUARES;
            case SOUTH_WEST: return ((bitboard & ~FILE_A) >> 9) & ALL_SQUARES;
            case WEST: return ((bitboard & ~FILE_A) >> 1) & ALL_SQUARES;
            case NORTH_WEST: return ((bitboard & ~FILE_A) << 7) & ALL_SQUARES;
            default: throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    /**
     * Fill in all squares in a particular direction
     * @param bitboard Bitboard
     * @param direction Direction to fill
     * @return Filled bitboard
     */
    public static long fill(long bitboard, Direction direction) {
        long result = bitboard;

        for (int i = 0; i < 7; i++) {
            result |= shift(result, direction);
        }

        return result;
    }

    /**
     * Generate attacks for sliding pieces (queen, rook, bishop)
     * @param square Square index (0-63)
     * @param occupied Bitboard of occupied squares
     * @param directions Array of directions the piece can move
     * @return Bitboard of attack squares
     */
    public static long slidingAttacks(int square, long occupied, Direction[] directions) {
        long attacks = 0;
        long piece = 1L << square;

        for (Direction direction : directions) {
            long ray = 0;
            long blocker = 0;

            switch (direction) {
                case NORTH:
                    ray = fill(shift(piece, Direction.NORTH), Direction.NORTH);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(lsb(blocker), Direction.NORTH), Direction.NORTH);
                    }
                    break;
                case NORTH_EAST:
                    ray = fill(shift(piece, Direction.NORTH_EAST), Direction.NORTH_EAST);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(lsb(blocker), Direction.NORTH_EAST), Direction.NORTH_EAST);
                    }
                    break;
                case EAST:
                    ray = fill(shift(piece, Direction.EAST), Direction.EAST);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(lsb(blocker), Direction.EAST), Direction.EAST);
                    }
                    break;
                case SOUTH_EAST:
                    ray = fill(shift(piece, Direction.SOUTH_EAST), Direction.SOUTH_EAST);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(lsb(blocker), Direction.SOUTH_EAST), Direction.SOUTH_EAST);
                    }
                    break;
                case SOUTH:
                    ray = fill(shift(piece, Direction.SOUTH), Direction.SOUTH);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(msb(blocker), Direction.SOUTH), Direction.SOUTH);
                    }
                    break;
                case SOUTH_WEST:
                    ray = fill(shift(piece, Direction.SOUTH_WEST), Direction.SOUTH_WEST);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(msb(blocker), Direction.SOUTH_WEST), Direction.SOUTH_WEST);
                    }
                    break;
                case WEST:
                    ray = fill(shift(piece, Direction.WEST), Direction.WEST);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(msb(blocker), Direction.WEST), Direction.WEST);
                    }
                    break;
                case NORTH_WEST:
                    ray = fill(shift(piece, Direction.NORTH_WEST), Direction.NORTH_WEST);
                    blocker = ray & occupied;
                    if (blocker != 0) {
                        ray ^= fill(shift(lsb(blocker), Direction.NORTH_WEST), Direction.NORTH_WEST);
                    }
                    break;
            }

            attacks |= ray;
        }

        return attacks;
    }

    /**
     * Generate knight attacks
     * @param square Square index (0-63)
     * @return Bitboard of knight attack squares
     */
    public static long knightAttacks(int square) {
        long knight = 1L << square;
        long attacks = 0;

        // Knight can move in L-shape: 2 squares in one direction, 1 square perpendicular
        attacks |= (knight & ~FILE_A & ~FILE_B) << 6;  // NNW
        attacks |= (knight & ~FILE_A) << 15;          // WNW
        attacks |= (knight & ~FILE_H) << 17;          // ENE
        attacks |= (knight & ~FILE_G & ~FILE_H) << 10; // NNE
        attacks |= (knight & ~FILE_H & ~FILE_G) >> 6;  // SSE
        attacks |= (knight & ~FILE_H) >> 15;          // ESE
        attacks |= (knight & ~FILE_A) >> 17;          // WSW
        attacks |= (knight & ~FILE_A & ~FILE_B) >> 10; // SSW

        return attacks;
    }

    /**
     * Generate king attacks
     * @param square Square index (0-63)
     * @return Bitboard of king attack squares
     */
    public static long kingAttacks(int square) {
        long king = 1L << square;
        long attacks = 0;

        // King can move one square in any direction
        attacks |= shift(king, Direction.NORTH);
        attacks |= shift(king, Direction.NORTH_EAST);
        attacks |= shift(king, Direction.EAST);
        attacks |= shift(king, Direction.SOUTH_EAST);
        attacks |= shift(king, Direction.SOUTH);
        attacks |= shift(king, Direction.SOUTH_WEST);
        attacks |= shift(king, Direction.WEST);
        attacks |= shift(king, Direction.NORTH_WEST);

        return attacks;
    }

    /**
     * Generate pawn attacks
     * @param square Square index (0-63)
     * @param isWhite Color of the pawn (true for white, false for black)
     * @return Bitboard of pawn attack squares
     */
    public static long pawnAttacks(int square, boolean isWhite) {
        long pawn = 1L << square;
        long attacks = 0;

        if (isWhite) {
            attacks |= shift(pawn, Direction.NORTH_EAST);
            attacks |= shift(pawn, Direction.NORTH_WEST);
        } else {
            attacks |= shift(pawn, Direction.SOUTH_EAST);
            attacks |= shift(pawn, Direction.SOUTH_WEST);
        }

        return attacks;
    }

    /**
     * Generate pawn pushes (non-capture moves)
     * @param square Square index (0-63)
     * @param isWhite Color of the pawn (true for white, false for black)
     * @param occupied Bitboard of occupied squares
     * @return Bitboard of pawn push squares
     */
    public static long pawnPushes(int square, boolean isWhite, long occupied) {
        long pawn = 1L << square;
        long pushes = 0;

        if (isWhite) {
            long singlePush = shift(pawn, Direction.NORTH) & ~occupied;
            pushes |= singlePush;

            // Double push is possible only from the 2nd rank and if the path is clear
            if ((pawn & RANK_2) != 0 && singlePush != 0) {
                pushes |= shift(singlePush, Direction.NORTH) & ~occupied;
            }
        } else {
            long singlePush = shift(pawn, Direction.SOUTH) & ~occupied;
            pushes |= singlePush;

            // Double push is possible only from the 7th rank and if the path is clear
            if ((pawn & RANK_7) != 0 && singlePush != 0) {
                pushes |= shift(singlePush, Direction.SOUTH) & ~occupied;
            }
        }

        return pushes;
    }

    /**
     * Generate rook attacks
     * @param square Square index (0-63)
     * @param occupied Bitboard of occupied squares
     * @return Bitboard of rook attack squares
     */
    public static long rookAttacks(int square, long occupied) {
        Direction[] directions = {
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
        };

        return slidingAttacks(square, occupied, directions);
    }

    /**
     * Generate bishop attacks
     * @param square Square index (0-63)
     * @param occupied Bitboard of occupied squares
     * @return Bitboard of bishop attack squares
     */
    public static long bishopAttacks(int square, long occupied) {
        Direction[] directions = {
                Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST
        };

        return slidingAttacks(square, occupied, directions);
    }

    /**
     * Generate queen attacks
     * @param square Square index (0-63)
     * @param occupied Bitboard of occupied squares
     * @return Bitboard of queen attack squares
     */
    public static long queenAttacks(int square, long occupied) {
        return rookAttacks(square, occupied) | bishopAttacks(square, occupied);
    }

    /**
     * Print a bitboard to the console
     * @param bitboard Bitboard to print
     */
    public static void print(long bitboard) {
        System.out.println("  +-----------------+");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " | ");

            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                boolean isSet = ((bitboard >>> square) & 1) == 1;

                System.out.print(isSet ? "X " : ". ");
            }

            System.out.println("|");
        }

        System.out.println("  +-----------------+");
        System.out.println("    a b c d e f g h");
        System.out.println("Hexadecimal: 0x" + Long.toHexString(bitboard));
    }

    /**
     * Directions for piece movement
     */
    public enum Direction {
        NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
    }
}