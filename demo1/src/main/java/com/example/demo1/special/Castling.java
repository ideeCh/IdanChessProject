package com.example.demo1.special;

import com.example.demo1.core.*;

/**
 * Implementation of Castling special move
 */
public class Castling {

    /**
     * Check if castling is valid
     *
     * @param gameState Current game state
     * @param move      The castling move to validate
     * @return true if the castling move is valid
     */
    public static boolean isValidCastling(GameState gameState, Move move) {
        // Get the king
        ChessPiece king = gameState.getBoard().getPieceAt(move.getSource());

        // Verify it's a king that hasn't moved
        if (king == null || king.getType() != ChessPieceType.KING || king.hasMoved()) {
            System.out.println("Invalid castling: King has moved or is not present");
            return false;
        }

        // Verify the rook is present and hasn't moved
        Position rookPos = move.getCastlingRookSource();
        ChessPiece rook = gameState.getBoard().getPieceAt(rookPos);

        if (rook == null || rook.getType() != ChessPieceType.ROOK || rook.hasMoved()) {
            System.out.println("Invalid castling: Rook has moved or is not present");
            return false;
        }

        // Verify no pieces between king and rook
        int kingFile = move.getSource().getFile() - 'a';
        int rookFile = rookPos.getFile() - 'a';
        int rank = move.getSource().getRank() - 1; // Convert to 0-based

        int step = (rookFile > kingFile) ? 1 : -1;
        for (int file = kingFile + step; file != rookFile; file += step) {
            Position pos = new Position((char) ('a' + file), rank + 1);
            if (gameState.getBoard().getPieceAt(pos) != null) {
                System.out.println("Invalid castling: Pieces between king and rook");
                return false;
            }
        }

        // Verify king is not in check
        MoveValidator validator = new MoveValidator();
        if (validator.isKingInCheck(gameState, king.getColor())) {
            System.out.println("Invalid castling: King is in check");
            return false;
        }

        // Verify king doesn't pass through or end up in check
        int targetFile = move.getTarget().getFile() - 'a';
        step = (targetFile > kingFile) ? 1 : -1;

        for (int file = kingFile + step; file != targetFile + step; file += step) {
            Position pos = new Position((char) ('a' + file), rank + 1);

            // Create temporary game state to check if king would be in check
            GameState tempState;
            try {
                tempState = (GameState) gameState.clone();

                // Move king to this square
                Position kingPos = new Position((char) ('a' + kingFile), rank + 1);
                ChessPiece kingPiece = tempState.getBoard().getPieceAt(kingPos);
                tempState.getBoard().setPieceAt(kingPos, null);
                tempState.getBoard().setPieceAt(pos, kingPiece);

                if (validator.isKingInCheck(tempState, king.getColor())) {
                    System.out.println("Invalid castling: King passes through or ends in check");
                    return false;
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Execute a castling move
     *
     * @param gameState Current game state
     * @param move      The castling move
     */
    public static void executeCastling(GameState gameState, Move move) {
        // Move the king
        gameState.getBoard().movePiece(move);

        // Move the rook
        Position rookSource = move.getCastlingRookSource();
        Position rookTarget = move.getCastlingRookTarget();
        ChessPiece rook = gameState.getBoard().getPieceAt(rookSource);

        if (rook != null) {
            gameState.getBoard().setPieceAt(rookTarget, rook);
            gameState.getBoard().setPieceAt(rookSource, null);
            rook.setHasMoved(true);
        }
    }

    /**
     * Create a kingside castling move for the current player
     *
     * @param currentPlayer Color of the current player
     * @return A move representing kingside castling
     */
    public static Move createKingsideCastlingMove(Color currentPlayer) {
        int rank = (currentPlayer == Color.WHITE) ? 1 : 8;

        Position kingPos = new Position('e', rank);
        Position targetPos = new Position('g', rank);
        Move castlingMove = new Move(kingPos, targetPos);

        // Set up castling info
        castlingMove.setCastling(true);
        castlingMove.setCastlingRookSource(new Position('h', rank));
        castlingMove.setCastlingRookTarget(new Position('f', rank));

        return castlingMove;
    }

    /**
     * Create a queenside castling move for the current player
     *
     * @param currentPlayer Color of the current player
     * @return A move representing queenside castling
     */
    public static Move createQueensideCastlingMove(Color currentPlayer) {
        int rank = (currentPlayer == Color.WHITE) ? 1 : 8;

        Position kingPos = new Position('e', rank);
        Position targetPos = new Position('c', rank);
        Move castlingMove = new Move(kingPos, targetPos);

        // Set up castling info
        castlingMove.setCastling(true);
        castlingMove.setCastlingRookSource(new Position('a', rank));
        castlingMove.setCastlingRookTarget(new Position('d', rank));

        return castlingMove;
    }
}
