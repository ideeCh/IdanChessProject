package com.example.idanchessgame;

/**
 * Represents a queen chess piece.
 */
public class Queen extends Piece {

    /**
     * Creates a new queen of the specified color.
     *
     * @param isWhite Whether this is a white queen
     */
    public Queen(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_QUEEN : PieceType.BLACK_QUEEN);
    }

    /**
     * Generates all legal moves for this queen at the given position.
     *
     * @param square The square the queen is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long occupied = board.getOccupiedSquares().getValue();

        // Generate attacks in both diagonal and orthogonal directions
        long attacks = generateDiagonalAttacks(square, occupied) | generateOrthogonalAttacks(square, occupied);

        // Remove squares occupied by friendly pieces
        Bitboard friendlyPieces = isWhite() ? board.getWhiteOccupiedSquares() : board.getBlackOccupiedSquares();
        attacks &= ~friendlyPieces.getValue();

        moves.setValue(attacks);
        return moves;
    }

    /**
     * Generates diagonal attacks for the queen.
     */
    private long generateDiagonalAttacks(int square, long occupied) {
        long attacks = 0L;

        // Rays in all diagonal directions
        attacks |= generateNorthEastRay(square, occupied);
        attacks |= generateNorthWestRay(square, occupied);
        attacks |= generateSouthEastRay(square, occupied);
        attacks |= generateSouthWestRay(square, occupied);

        return attacks;
    }

    /**
     * Generates orthogonal attacks for the queen.
     */
    private long generateOrthogonalAttacks(int square, long occupied) {
        long attacks = 0L;

        // Rays in all orthogonal directions
        attacks |= generateNorthRay(square, occupied);
        attacks |= generateEastRay(square, occupied);
        attacks |= generateSouthRay(square, occupied);
        attacks |= generateWestRay(square, occupied);

        return attacks;
    }

    /**
     * Generates ray attacks in the north direction (up vertical).
     */
    private long generateNorthRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank - 1; r >= 0; r--) {
            int targetSquare = r * 8 + file;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the east direction (right horizontal).
     */
    private long generateEastRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int f = file + 1; f < 8; f++) {
            int targetSquare = rank * 8 + f;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the south direction (down vertical).
     */
    private long generateSouthRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank + 1; r < 8; r++) {
            int targetSquare = r * 8 + file;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the west direction (left horizontal).
     */
    private long generateWestRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int f = file - 1; f >= 0; f--) {
            int targetSquare = rank * 8 + f;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the northeast direction (up-right diagonal).
     */
    private long generateNorthEastRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank - 1, f = file + 1; r >= 0 && f < 8; r--, f++) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the northwest direction (up-left diagonal).
     */
    private long generateNorthWestRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the southeast direction (down-right diagonal).
     */
    private long generateSouthEastRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank + 1, f = file + 1; r < 8 && f < 8; r++, f++) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Generates ray attacks in the southwest direction (down-left diagonal).
     */
    private long generateSouthWestRay(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank + 1, f = file - 1; r < 8 && f >= 0; r++, f--) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;

            attacks |= targetBit;

            // If we hit an occupied square, stop ray generation
            if ((occupied & targetBit) != 0) {
                break;
            }
        }

        return attacks;
    }

    /**
     * Validates if a move follows queen movement rules (horizontal, vertical, or diagonal)
     *
     * @param fromSquare The source square (0-63)
     * @param toSquare The destination square (0-63)
     * @return true if the move is valid for a queen
     */
    public static boolean isValidQueenMove(int fromSquare, int toSquare) {
        // Same square is not valid
        if (fromSquare == toSquare) {
            return false;
        }

        int fromRank = fromSquare / 8;
        int fromFile = fromSquare % 8;
        int toRank = toSquare / 8;
        int toFile = toSquare % 8;

        // Vertical move: same file, different rank
        if (fromFile == toFile) {
            return true;
        }

        // Horizontal move: same rank, different file
        if (fromRank == toRank) {
            return true;
        }

        // Diagonal move: change in rank equals change in file
        int rankDiff = Math.abs(toRank - fromRank);
        int fileDiff = Math.abs(toFile - fromFile);

        return rankDiff == fileDiff;
    }
}