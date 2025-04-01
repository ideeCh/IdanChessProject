package com.example.demo1.core.ai.evaluation;

import com.example.demo1.core.*;

/**
 * Abstract base class for phase-specific evaluators.
 * Provides common methods and structures for all chess phase evaluators.
 */
public abstract class PhaseBasedEvaluator {

    // Common evaluation constants
    protected static final int PAWN_VALUE = 100;
    protected static final int KNIGHT_VALUE = 320;
    protected static final int BISHOP_VALUE = 330;
    protected static final int ROOK_VALUE = 500;
    protected static final int QUEEN_VALUE = 900;

    // Base class evaluation method all phase evaluators must implement
    public abstract int evaluate(Board board, Color sideToEvaluate);

    /**
     * Find the king of a specific color on the board
     *
     * @param board The chess board
     * @param color The king's color to find
     * @return Position of the king or null if not found (should never happen)
     */
    protected Position findKing(Board board, Color color) {
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.KING &&
                        piece.getColor() == color) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Calculate the simple material score (ignoring positional factors)
     *
     * @param board The chess board
     * @return Material score (positive means White has advantage)
     */
    protected int calculateMaterialScore(Board board) {
        int whiteScore = 0;
        int blackScore = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                int value = getPieceValue(piece.getType());
                if (piece.getColor() == Color.WHITE) {
                    whiteScore += value;
                } else {
                    blackScore += value;
                }
            }
        }

        return whiteScore - blackScore;
    }

    /**
     * Get the standard value of a chess piece
     *
     * @param type The type of chess piece
     * @return Value in centipawns
     */
    protected int getPieceValue(ChessPieceType type) {
        switch (type) {
            case PAWN: return PAWN_VALUE;
            case KNIGHT: return KNIGHT_VALUE;
            case BISHOP: return BISHOP_VALUE;
            case ROOK: return ROOK_VALUE;
            case QUEEN: return QUEEN_VALUE;
            case KING: return 0; // Kings aren't given material value as they can't be captured
            default: return 0;
        }
    }

    /**
     * Count pieces of a specific type and color on the board
     *
     * @param board The chess board
     * @param color The color of pieces to count
     * @param type The type of pieces to count
     * @return Count of matching pieces
     */
    protected int countPieces(Board board, Color color, ChessPieceType type) {
        int count = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == type && piece.getColor() == color) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Check if a square is empty
     *
     * @param board The chess board
     * @param file File (column)
     * @param rank Rank (row)
     * @return True if the square is empty
     */
    protected boolean isSquareEmpty(Board board, char file, int rank) {
        Position pos = new Position(file, rank);
        return board.getPieceAt(pos) == null;
    }

    /**
     * Check if a square contains a specific piece
     *
     * @param board The chess board
     * @param file File (column)
     * @param rank Rank (row)
     * @param color Color of the piece
     * @param type Type of the piece
     * @return True if the square contains the specified piece
     */
    protected boolean hasPiece(Board board, char file, int rank, Color color, ChessPieceType type) {
        Position pos = new Position(file, rank);
        ChessPiece piece = board.getPieceAt(pos);

        return piece != null && piece.getType() == type && piece.getColor() == color;
    }

    /**
     * Check if a file is open (has no pawns on it)
     *
     * @param board The chess board
     * @param file The file to check
     * @return True if the file is open
     */
    protected boolean isOpenFile(Board board, char file) {
        for (int rank = 1; rank <= 8; rank++) {
            Position pos = new Position(file, rank);
            ChessPiece piece = board.getPieceAt(pos);

            if (piece != null && piece.getType() == ChessPieceType.PAWN) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a file is semi-open (has no pawns of a specific color)
     *
     * @param board The chess board
     * @param file The file to check
     * @param color The color of pawns to check for
     * @return True if the file has no pawns of the specified color
     */
    protected boolean isSemiOpenFile(Board board, char file, Color color) {
        for (int rank = 1; rank <= 8; rank++) {
            Position pos = new Position(file, rank);
            ChessPiece piece = board.getPieceAt(pos);

            if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                    piece.getColor() == color) {
                return false;
            }
        }

        return true;
    }
}