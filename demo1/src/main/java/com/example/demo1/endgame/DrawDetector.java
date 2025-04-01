package com.example.demo1.endgame;

import com.example.demo1.core.*;

import java.util.Map;

/**
 * Class for detecting various draw conditions
 */
public class DrawDetector {

    /**
     * Check if the game is a draw by the fifty-move rule
     *
     * @param gameState Current game state
     * @return true if the game is a draw by the fifty-move rule
     */
    public static boolean isFiftyMoveRuleDraw(GameState gameState) {
        // 50 full moves = 100 half-moves (each player's turn is a half-move)
        return gameState.getHalfMoveClock() >= 100;
    }

    /**
     * Check if the game is a draw by threefold repetition
     *
     * @param gameState Current game state
     * @return true if the game is a draw by threefold repetition
     */
    public static boolean isThreefoldRepetitionDraw(GameState gameState) {
        // A position has been repeated three times (with the same player to move)
        String currentPosition = getBoardPositionString(gameState);
        Map<String, Integer> positionCount = gameState.getPositionCount();
        int count = positionCount.getOrDefault(currentPosition, 0);
        return count >= 3;
    }

    /**
     * Get a string representation of the current board position for repetition detection
     *
     * @param gameState Current game state
     * @return A string uniquely identifying the position
     */
    public static String getBoardPositionString(GameState gameState) {
        StringBuilder sb = new StringBuilder();

        // Add pieces on the board
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = gameState.getBoard().getPieceAt(pos);

                if (piece != null) {
                    sb.append(file).append(rank)
                            .append(piece.getColor())
                            .append(piece.getType())
                            .append(';');
                }
            }
        }

        // Add current player
        sb.append("player:").append(gameState.getCurrentPlayer()).append(';');

        // Add castling rights
        for (int rank = 1; rank <= 8; rank += 7) { // White (1) and Black (8) ranks
            Color pieceColor = (rank == 1) ? Color.WHITE : Color.BLACK;
            Position kingPos = new Position('e', rank);
            ChessPiece king = gameState.getBoard().getPieceAt(kingPos);

            if (king != null && king.getType() == ChessPieceType.KING && !king.hasMoved()) {
                // Check kingside rook
                Position kingsideRook = new Position('h', rank);
                ChessPiece rook = gameState.getBoard().getPieceAt(kingsideRook);
                if (rook != null && rook.getType() == ChessPieceType.ROOK && !rook.hasMoved()) {
                    sb.append(pieceColor).append("-O-O;");
                }

                // Check queenside rook
                Position queensideRook = new Position('a', rank);
                rook = gameState.getBoard().getPieceAt(queensideRook);
                if (rook != null && rook.getType() == ChessPieceType.ROOK && !rook.hasMoved()) {
                    sb.append(pieceColor).append("-O-O-O;");
                }
            }
        }

        // Add en passant target
        if (gameState.getEnPassantTarget() != null) {
            sb.append("ep:").append(gameState.getEnPassantTarget().toString()).append(';');
        }

        return sb.toString();
    }

    /**
     * Check if the game is a draw due to insufficient material
     *
     * @param gameState Current game state
     * @return true if there is insufficient material to checkmate
     */
    public static boolean isInsufficientMaterialDraw(GameState gameState) {
        // Count pieces
        int whiteBishops = 0, whiteKnights = 0, whitePieces = 0;
        int blackBishops = 0, blackKnights = 0, blackPieces = 0;
        boolean whiteBishopsOnLight = false, whiteBishopsOnDark = false;
        boolean blackBishopsOnLight = false, blackBishopsOnDark = false;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = gameState.getBoard().getPieceAt(pos);

                if (piece == null) continue;

                if (piece.getColor() == Color.WHITE) {
                    if (piece.getType() != ChessPieceType.KING) whitePieces++;

                    if (piece.getType() == ChessPieceType.BISHOP) {
                        whiteBishops++;
                        // Check bishop's square color
                        boolean isLightSquare = ((rank + (file - 'a')) % 2 == 0);
                        if (isLightSquare) whiteBishopsOnLight = true;
                        else whiteBishopsOnDark = true;
                    } else if (piece.getType() == ChessPieceType.KNIGHT) {
                        whiteKnights++;
                    } else if (piece.getType() != ChessPieceType.KING) {
                        // Any other piece (queen, rook, pawn) can deliver checkmate
                        return false;
                    }
                } else { // BLACK
                    if (piece.getType() != ChessPieceType.KING) blackPieces++;

                    if (piece.getType() == ChessPieceType.BISHOP) {
                        blackBishops++;
                        // Check bishop's square color
                        boolean isLightSquare = ((rank + (file - 'a')) % 2 == 0);
                        if (isLightSquare) blackBishopsOnLight = true;
                        else blackBishopsOnDark = true;
                    } else if (piece.getType() == ChessPieceType.KNIGHT) {
                        blackKnights++;
                    } else if (piece.getType() != ChessPieceType.KING) {
                        // Any other piece (queen, rook, pawn) can deliver checkmate
                        return false;
                    }
                }
            }
        }

        // Insufficient material cases:

        // 1. King vs King
        if (whitePieces == 0 && blackPieces == 0) {
            return true;
        }

        // 2. King and Bishop vs King
        if ((whitePieces == 1 && whiteBishops == 1 && blackPieces == 0) ||
                (blackPieces == 1 && blackBishops == 1 && whitePieces == 0)) {
            return true;
        }

        // 3. King and Knight vs King
        if ((whitePieces == 1 && whiteKnights == 1 && blackPieces == 0) ||
                (blackPieces == 1 && blackKnights == 1 && whitePieces == 0)) {
            return true;
        }

        // 4. King and Bishop(s) vs King and Bishop(s), all bishops on same color squares
        if (whitePieces == whiteBishops && blackPieces == blackBishops) {
            boolean allBishopsOnSameColor =
                    (!whiteBishopsOnLight || !whiteBishopsOnDark) &&
                            (!blackBishopsOnLight || !blackBishopsOnDark);

            if (allBishopsOnSameColor) {
                boolean allBishopsOnLightSquares =
                        (whiteBishopsOnLight && !whiteBishopsOnDark) &&
                                (blackBishopsOnLight && !blackBishopsOnDark);

                boolean allBishopsOnDarkSquares =
                        (!whiteBishopsOnLight && whiteBishopsOnDark) &&
                                (!blackBishopsOnLight && blackBishopsOnDark);

                if (allBishopsOnLightSquares || allBishopsOnDarkSquares) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the game is drawn due to any reason
     *
     * @param gameState Current game state
     * @return true if the game is a draw
     */
// In DrawDetector.java, update isDraw method:
    public static boolean isDraw(GameState gameState) {
        return StalemateDetector.isStalemate(gameState, gameState.getCurrentPlayer()) ||
                isFiftyMoveRuleDraw(gameState) ||
                isThreefoldRepetitionDraw(gameState) ||
                isInsufficientMaterialDraw(gameState);
    }

    /**
     * Update position count for threefold repetition detection
     *
     * @param gameState Current game state
     */
    public static void updatePositionCount(GameState gameState) {
        String positionString = getBoardPositionString(gameState);
        Map<String, Integer> positionCount = gameState.getPositionCount();
        positionCount.put(positionString, positionCount.getOrDefault(positionString, 0) + 1);
    }

    /**
     * Update half-move clock for fifty-move rule
     *
     * @param gameState  Current game state
     * @param isPawnMove Whether a pawn was moved
     * @param isCapture  Whether a piece was captured
     */
    public static void updateHalfMoveClock(GameState gameState, boolean isPawnMove, boolean isCapture) {
        if (isPawnMove || isCapture) {
            gameState.setHalfMoveClock(0); // Reset counter
        } else {
            gameState.setHalfMoveClock(gameState.getHalfMoveClock() + 1); // Increment counter
        }
    }
}
