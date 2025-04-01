package com.example.idanchessgame;


/**
 * Represents a bishop chess piece.
 */
public class Bishop extends Piece {

    /**
     * Creates a new bishop of the specified color.
     *
     * @param isWhite Whether this is a white bishop
     */
    public Bishop(boolean isWhite) {
        super(isWhite ? PieceType.WHITE_BISHOP : PieceType.BLACK_BISHOP);
    }

    /**
     * Generates all legal moves for this bishop at the given position.
     *
     * @param square The square the bishop is on (0-63)
     * @param board The current board state
     * @return A bitboard with all legal destination squares
     */
    @Override
    public Bitboard getLegalMoves(int square, Board board) {
        Bitboard moves = new Bitboard();
        long occupied = board.getOccupiedSquares().getValue();

        // Generate attacks in each diagonal direction
        long attacks = generateDiagonalAttacks(square, occupied);

        // Remove squares occupied by friendly pieces
        Bitboard friendlyPieces = isWhite() ? board.getWhiteOccupiedSquares() : board.getBlackOccupiedSquares();
        attacks &= ~friendlyPieces.getValue();

        // Filter moves - only keep diagonal moves
        long validMoves = 0L;
        int fromRank = square / 8;
        int fromFile = square % 8;

        // Iterate through all bits in the attack bitboard
        for (int i = 0; i < 64; i++) {
            // Check if this square is in our attack set
            if (((attacks >> i) & 1L) == 1L) {
                int toRank = i / 8;
                int toFile = i % 8;

                // Check if this is a valid diagonal move
                if (Math.abs(toRank - fromRank) == Math.abs(toFile - fromFile)) {
                    validMoves |= (1L << i);
                }
            }
        }

        moves.setValue(validMoves);
        return moves;
    }

    /**
     * Generates diagonal attacks for the bishop.
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

        for (int r = rank + 1, f = file - 1; r < 8 && f >= 0; r--, f--) {
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
     * Validates if a move follows bishop movement rules (diagonal only)
     *
     * @param fromSquare The source square (0-63)
     * @param toSquare The destination square (0-63)
     * @return true if the move is valid for a bishop
     */
    public static boolean isValidBishopMove(int fromSquare, int toSquare) {
        // Same square is not valid
        if (fromSquare == toSquare) {
            return false;
        }

        int fromRank = fromSquare / 8;
        int fromFile = fromSquare % 8;
        int toRank = toSquare / 8;
        int toFile = toSquare % 8;

        // Diagonal move: change in rank equals change in file
        int rankDiff = Math.abs(toRank - fromRank);
        int fileDiff = Math.abs(toFile - fromFile);

        return rankDiff == fileDiff;
    }
}