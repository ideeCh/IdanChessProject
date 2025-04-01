package com.example.demo1.core;

import com.example.demo1.moves.MoveExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing the game state
 */
public class GameState implements Cloneable {

    private Board board;
    private Color currentPlayer;
    private List<Move> moveHistory;
    private Map<Color, List<ChessPiece>> capturedPieces;
    private boolean check;
    private boolean checkmate;
    private boolean stalemate;

    // Added for en passant tracking
    private Position enPassantTarget = null;

    // Added for draw conditions
    private int halfMoveClock = 0;  // For 50-move rule (increments after each move, resets after captures or pawn moves)
    private Map<String, Integer> positionCount = new HashMap<>(); // For threefold repetition

    // Added flags to prevent recursion
    private boolean isUpdatingStatus = false;
    private boolean isSimulation = false;

    /**
     * Constructor for game state
     */
    public GameState() {
        this.board = new Board();
        this.currentPlayer = Color.WHITE;
        this.moveHistory = new ArrayList<>();
        this.capturedPieces = new HashMap<>();
        this.capturedPieces.put(Color.WHITE, new ArrayList<>());
        this.capturedPieces.put(Color.BLACK, new ArrayList<>());
        this.check = false;
        this.checkmate = false;
        this.stalemate = false;
    }
    /**
     * Set the en passant target position
     */
    public void setEnPassantTarget(Position position) {
        this.enPassantTarget = position;
    }

    /**
     * Set the half-move clock value for fifty-move rule
     */
    public void setHalfMoveClock(int halfMoveClock) {
        this.halfMoveClock = halfMoveClock;
    }

    /**
     * Get the position count map for threefold repetition detection
     */
    public Map<String, Integer> getPositionCount() {
        return positionCount;
    }

    /**
     * Set the check status
     */
    public void setCheck(boolean check) {
        this.check = check;
    }

    /**
     * Set the checkmate status
     */
    public void setCheckmate(boolean checkmate) {
        this.checkmate = checkmate;
    }

    /**
     * Set the stalemate status
     */
    public void setStalemate(boolean stalemate) {
        this.stalemate = stalemate;
    }

    /**
     * Switch the current player
     */
    public void switchPlayer() {
        currentPlayer = currentPlayer.getOpposite();
    }

    /**
     * Get the chess board
     *
     * @return The chess board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Get the current player
     *
     * @return The current player's color
     */
    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Get the move history
     *
     * @return List of moves made in the game
     */
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    /**
     * Get the en passant target position, if any
     *
     * @return The position where an en passant capture can be made, or null if none
     */
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    /**
     * Get the half-move clock (for 50-move rule)
     *
     * @return The number of half-moves since the last pawn move or capture
     */
    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    /**
     * Get captured pieces for a specific color
     *
     * @param color Color of the player who captured pieces
     * @return List of captured pieces
     */
    public List<ChessPiece> getCapturedPieces(Color color) {
        return new ArrayList<>(capturedPieces.get(color));
    }

    /**
     * Check if the current player is in check
     *
     * @return true if in check, false otherwise
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * Check if the current player is in checkmate
     *
     * @return true if in checkmate, false otherwise
     */
    public boolean isCheckmate() {
        return checkmate;
    }

    /**
     * Check if the game is in stalemate
     *
     * @return true if in stalemate, false otherwise
     */
    public boolean isStalemate() {
        return stalemate;
    }

    /**
     * Check if the game is a draw by the 50-move rule
     *
     * @return true if the game is a draw by the 50-move rule
     */
    public boolean isFiftyMoveRuleDraw() {
        return halfMoveClock >= 100; // 50 full moves = 100 half-moves
    }

    /**
     * Check if the game is a draw by threefold repetition
     *
     * @return true if the game is a draw by threefold repetition
     */
    public boolean isThreefoldRepetitionDraw() {
        // Check the current position
        String currentPosition = getBoardPositionString();
        return positionCount.getOrDefault(currentPosition, 0) >= 3;
    }

    /**
     * Check if the game is a draw due to insufficient material
     *
     * @return true if there is insufficient material to checkmate
     */
    public boolean isInsufficientMaterialDraw() {
        // Count pieces
        int whiteBishops = 0, whiteKnights = 0, whitePieces = 0;
        int blackBishops = 0, blackKnights = 0, blackPieces = 0;
        boolean whiteBishopsOnLight = false, whiteBishopsOnDark = false;
        boolean blackBishopsOnLight = false, blackBishopsOnDark = false;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

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
     * @return true if the game is a draw
     */
    public boolean isDraw() {
        return stalemate || isFiftyMoveRuleDraw() || isThreefoldRepetitionDraw() || isInsufficientMaterialDraw();
    }

    /**
     * Get a string representation of the current board position for threefold repetition detection
     *
     * @return A string uniquely identifying the current position
     */
    private String getBoardPositionString() {
        StringBuilder sb = new StringBuilder();

        // Add pieces on the board
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null) {
                    sb.append(file).append(rank)
                            .append(piece.getColor())
                            .append(piece.getType())
                            .append(';');
                }
            }
        }

        // Add current player
        sb.append("player:").append(currentPlayer).append(';');

        // Add castling rights
        for (int rank = 1; rank <= 8; rank += 7) { // White (1) and Black (8) ranks
            Color pieceColor = (rank == 1) ? Color.WHITE : Color.BLACK;
            Position kingPos = new Position('e', rank);
            ChessPiece king = board.getPieceAt(kingPos);

            if (king != null && king.getType() == ChessPieceType.KING && !king.hasMoved()) {
                // Check kingside rook
                Position kingsideRook = new Position('h', rank);
                ChessPiece rook = board.getPieceAt(kingsideRook);
                if (rook != null && rook.getType() == ChessPieceType.ROOK && !rook.hasMoved()) {
                    sb.append(pieceColor).append("-O-O;");
                }

                // Check queenside rook
                Position queensideRook = new Position('a', rank);
                rook = board.getPieceAt(queensideRook);
                if (rook != null && rook.getType() == ChessPieceType.ROOK && !rook.hasMoved()) {
                    sb.append(pieceColor).append("-O-O-O;");
                }
            }
        }

        // Add en passant target
        if (enPassantTarget != null) {
            sb.append("ep:").append(enPassantTarget.toString()).append(';');
        }

        return sb.toString();
    }

    /**
     * Make a move in the game with option to specify if it's a simulation
     * @param move Move to make
     * @param isSimulation Flag indicating if this is a simulated move
     */
    public void makeMove(Move move, boolean isSimulation) {
        // If this is a promotion move, ensure promotion type is specified
        if (!isSimulation &&
                com.example.demo1.special.PawnPromotion.isPromotionMove(this, move) &&
                move.getPromotionType() == null) {
            throw new IllegalArgumentException("Promotion type must be specified for pawn promotion");
        }

        // Delegate to the MoveExecutor
        com.example.demo1.moves.MoveExecutor.executeMove(this, move, isSimulation);
    }

    /**
     * Make a move in the game (non-simulation version)
     * @param move Move to make
     */
    public void makeMove(Move move) {
        makeMove(move, false);
    }
    /**
     * Update the game status (check, checkmate, stalemate)
     */
    // In GameState.java or in the updateGameStatus method:

    private void updateGameStatus() {
        MoveValidator validator = new MoveValidator();

        // Update check status
        check = validator.isKingInCheck(this, currentPlayer);

        // Check for checkmate or stalemate
        if (!validator.hasLegalMoves(this, currentPlayer)) {
            if (check) {
                checkmate = true;
                stalemate = false;
            } else {
                checkmate = false;
                stalemate = true;
            }
        } else {
            checkmate = false;
            stalemate = false;
        }
    }

    /**
     * Create a deep copy of the game state
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        GameState clone = (GameState) super.clone();

        clone.board = (Board) board.clone();
        clone.moveHistory = new ArrayList<>(moveHistory);

        // Clone en passant target
        clone.enPassantTarget = enPassantTarget;

        // Clone the half move clock and position counts
        clone.halfMoveClock = halfMoveClock;
        clone.positionCount = new HashMap<>(positionCount);

        clone.capturedPieces = new HashMap<>();
        clone.capturedPieces.put(Color.WHITE, new ArrayList<>(capturedPieces.get(Color.WHITE)));
        clone.capturedPieces.put(Color.BLACK, new ArrayList<>(capturedPieces.get(Color.BLACK)));

        return clone;
    }
}
