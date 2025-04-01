package com.example.demo1.core;

import com.example.demo1.special.Castling;

import java.util.ArrayList;
import java.util.List;

/**
 * Move validator class
 */
public class MoveValidator {

    /**
     * Check if a move is valid according to chess rules
     *
     * @param gameState Current game state
     * @param move      Move to validate
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(GameState gameState, Move move) {
        // Get the piece at the source position
        ChessPiece piece = gameState.getBoard().getPieceAt(move.getSource());

        // Check if there is a piece at the source position
        if (piece == null) {
            System.out.println("Invalid move: No piece at source position " + move.getSource());
            return false;
        }

        // Check if the piece belongs to the current player
        if (piece.getColor() != gameState.getCurrentPlayer()) {
            System.out.println("Invalid move: Piece at " + move.getSource() + " does not belong to current player");
            return false;
        }

        // Special validation for castling moves
        if (move.isCastling()) {
            return isValidCastling(gameState, move);
        }

        // Get all legal moves for this piece
        List<Move> legalMoves = generateLegalMoves(gameState, piece);

        // Check if the requested move is in the list of legal moves
        for (Move legalMove : legalMoves) {
            if (legalMove.getSource().equals(move.getSource()) &&
                    legalMove.getTarget().equals(move.getTarget())) {
                // Handle promotion if needed
                if (legalMove.getPromotionType() != null) {
                    if (move.getPromotionType() == null) {
                        // If the legal move requires promotion but the requested move doesn't specify it,
                        // default to Queen for simplicity (or you could return false)
                        return true;
                    }
                }
                return true;
            }
        }

        System.out.println("Invalid move: Move " + move + " is not in the list of legal moves for piece at " + move.getSource());
        return false;
    }

    // In MoveValidator.java, modify isValidMove or add this helper method:

    private boolean isValidPromotionMove(GameState gameState, Move move) {
        ChessPiece piece = gameState.getBoard().getPieceAt(move.getSource());

        // Must be a pawn
        if (piece == null || piece.getType() != ChessPieceType.PAWN) {
            return false;
        }

        // Must belong to current player
        if (piece.getColor() != gameState.getCurrentPlayer()) {
            return false;
        }

        // Must be reaching the promotion rank
        int targetRank = move.getTarget().getRank();
        boolean isPromotionRank = (piece.getColor() == Color.WHITE && targetRank == 8) ||
                (piece.getColor() == Color.BLACK && targetRank == 1);
        if (!isPromotionRank) {
            return false;
        }

        // Check if the basic move is valid (diagonal capture or forward move)
        List<Move> basicPawnMoves = generateBasicMoves(gameState, piece);
        for (Move basicMove : basicPawnMoves) {
            if (basicMove.getTarget().equals(move.getTarget())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate a castling move
     */
    private boolean isValidCastling(GameState gameState, Move move) {
        return Castling.isValidCastling(gameState, move);
    }

    /**
     * Generate basic moves for a piece, without checking for check conditions
     *
     * @param gameState Current game state
     * @param piece     The piece to generate moves for
     * @return List of basic moves for the piece
     */
    public static List<Move> generateBasicMoves(GameState gameState, ChessPiece piece) {
        List<Move> moves = new ArrayList<>();

        // If the piece is null, return an empty list
        if (piece == null) {
            return moves;
        }

        // Find the piece position
        Position piecePosition = findPiecePosition(gameState.getBoard(), piece);
        if (piecePosition == null) {
            return moves;
        }

        // Get all potential moves based on the piece type
        List<Move> potentialMoves;
        // Special handling for Pawn to include en passant moves
        if (piece.getType() == ChessPieceType.PAWN) {
            potentialMoves = ((Pawn) piece).generatePotentialMoves(gameState.getBoard(), gameState);
        } else {
            potentialMoves = piece.generatePotentialMoves(gameState.getBoard());
        }

        // Filter moves that would capture pieces of the same color
        for (Move move : potentialMoves) {
            // Skip moves that don't start from our piece position
            if (!move.getSource().equals(piecePosition)) {
                continue;
            }

            // For castling moves, we need special validation
            if (move.isCastling()) {
                moves.add(move);
                continue;
            }

            ChessPiece targetPiece = gameState.getBoard().getPieceAt(move.getTarget());

            // Skip if target has a piece of same color
            if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
                continue;
            }

            // Handle en passant special case
            if (piece.getType() == ChessPieceType.PAWN &&
                    move.getTarget().equals(gameState.getEnPassantTarget())) {
                // This is a valid en passant move
                moves.add(move);
                continue;
            }

            moves.add(move);
        }

        return moves;
    }

    // In MoveValidator.java, add this method and make it static:
    public static boolean hasLegalMoves(GameState gameState, Color color) {
        Board board = gameState.getBoard();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                // Skip empty squares and opponent's pieces
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                // Get basic moves for this piece
                List<Move> moves = generateBasicMoves(gameState, piece);

                // Check each move to see if it would leave the king in check
                for (Move move : moves) {
                    try {
                        // Simulate the move
                        GameState tempState = (GameState) gameState.clone();
                        tempState.makeMove(move, true);

                        // If this move doesn't leave king in check, we found a legal move
                        if (!isKingInCheck(tempState, color)) {
                            return true;
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // If we get here, there are no legal moves
        return false;
    }

    /**
     * Generate all legal moves for a given piece
     *
     * @param gameState Current game state
     * @param piece     The piece to generate moves for
     * @return List of legal moves for the piece
     */
    private List<Move> generateLegalMoves(GameState gameState, ChessPiece piece) {
        List<Move> moves = new ArrayList<>();

        // If the piece is null, return an empty list
        if (piece == null) {
            return moves;
        }

        // Find the piece position
        Position piecePosition = findPiecePosition(gameState.getBoard(), piece);
        if (piecePosition == null) {
            return moves;
        }

        // First, get all potential moves based on the piece type
        List<Move> potentialMoves = generateBasicMoves(gameState, piece);

        // Then filter out moves that would leave the king in check
        for (Move move : potentialMoves) {
            // Create a temporary copy of the game state
            GameState tempState;
            try {
                tempState = (GameState) gameState.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                continue;
            }

            // Make the move in the temporary state, specifying this is a simulation
            tempState.makeMove(move, true);

            // Check if the king is in check after the move
            if (!isKingInCheck(tempState, piece.getColor())) {
                moves.add(move);
            }
        }

        return moves;
    }

    /**
     * Find the position of a piece on the board
     *
     * @param board The chess board
     * @param piece The piece to find
     * @return The position of the piece, or null if not found
     */
    private static Position findPiecePosition(Board board, ChessPiece piece) {
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                if (board.getPieceAt(pos) == piece) {
                    return pos;
                }
            }
        }
        return null;
    }

    // File: src/main/java/com/example/demo1/MoveValidator.java

    /**
     * Check if a king is in check
     * @param gameState Current game state
     * @param color Color of the king to check
     * @return true if the king is in check, false otherwise
     */
    public static boolean isKingInCheck(GameState gameState, Color color) {
        // Find the king's position
        Position kingPosition = null;
        Board board = gameState.getBoard();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.KING && piece.getColor() == color) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) break;
        }

        if (kingPosition == null) {
            System.err.println("Error: King not found for " + color);
            return false;  // Should not happen in a valid game
        }

        // Check if any opponent piece can attack the king
        Color opponentColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == opponentColor) {
                    // Get basic moves without check validation to avoid infinite recursion
                    List<Move> basicMoves;
                    if (piece.getType() == ChessPieceType.PAWN) {
                        basicMoves = ((Pawn) piece).generatePotentialMoves(board, gameState);
                    } else {
                        basicMoves = piece.generatePotentialMoves(board);
                    }

                    // Check if any move targets the king
                    for (Move move : basicMoves) {
                        if (move.getTarget().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
