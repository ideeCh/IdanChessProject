package com.example.idanchessgame;

/**
 * Represents a chess move with origin and destination squares, moved piece,
 * captured piece, and special move flags.
 */
public class Move {
    private int fromSquare;
    private int toSquare;
    private PieceType movedPiece;
    private PieceType capturedPiece;
    private boolean isPromotion;
    private PieceType promotionPiece;
    private boolean isCastling;
    private boolean isEnPassant;
    private boolean isCapture;

    /**
     * Creates a basic move without captures or special moves.
     *
     * @param fromSquare The source square (0-63)
     * @param toSquare The destination square (0-63)
     * @param movedPiece The type of piece being moved
     */
    public Move(int fromSquare, int toSquare, PieceType movedPiece) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.movedPiece = movedPiece;
        this.capturedPiece = null;
        this.isPromotion = false;
        this.promotionPiece = null;
        this.isCastling = false;
        this.isEnPassant = false;
        this.isCapture = false;
    }

    /**
     * Creates a complete move with all details.
     *
     * @param fromSquare The source square (0-63)
     * @param toSquare The destination square (0-63)
     * @param movedPiece The type of piece being moved
     * @param capturedPiece The type of piece being captured (null if no capture)
     * @param isPromotion Whether this move is a pawn promotion
     * @param promotionPiece The piece type to promote to (if isPromotion is true)
     * @param isCastling Whether this move is a castling move
     * @param isEnPassant Whether this move is an en passant capture
     */
    public Move(int fromSquare, int toSquare, PieceType movedPiece,
                PieceType capturedPiece, boolean isPromotion,
                PieceType promotionPiece, boolean isCastling,
                boolean isEnPassant) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.isPromotion = isPromotion;
        this.promotionPiece = promotionPiece;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.isCapture = capturedPiece != null;
    }

    /**
     * Executes this move on the given board.
     *
     * @param board The board to apply the move to
     */
    public void execute(Board board) {
        System.out.println("Executing move: " + this);

        // Handle captures first (before moving pieces)
        if (isCapture) {
            if (isEnPassant) {
                int capturedPawnSquare = (fromSquare & 0x38) | (toSquare & 0x07);
                System.out.println("Removing en passant captured piece at " + squareToAlgebraic(capturedPawnSquare));
                board.removePiece(capturedPawnSquare, capturedPiece);
            } else {
                System.out.println("Removing captured piece at " + squareToAlgebraic(toSquare));
                board.removePiece(toSquare, capturedPiece);
            }
        }

        // Move the piece
        System.out.println("Moving " + movedPiece + " from " + squareToAlgebraic(fromSquare) +
                " to " + squareToAlgebraic(toSquare));
        board.movePiece(fromSquare, toSquare, movedPiece);

        // Handle pawn promotion
        if (isPromotion) {
            System.out.println("Promoting pawn to " + promotionPiece + " at " + squareToAlgebraic(toSquare));
            board.removePiece(toSquare, movedPiece);
            board.addPiece(toSquare, promotionPiece);
        }

        // Handle castling
        if (isCastling) {
            boolean isKingside = toSquare > fromSquare;
            int rookFromFile = isKingside ? 7 : 0;
            int rookToFile = isKingside ? 5 : 3;
            int rank = fromSquare / 8;
            int rookFromSquare = rank * 8 + rookFromFile;
            int rookToSquare = rank * 8 + rookToFile;

            PieceType rookType = movedPiece.isWhite() ? PieceType.WHITE_ROOK : PieceType.BLACK_ROOK;
            System.out.println("Castling: Moving rook from " + squareToAlgebraic(rookFromSquare) +
                    " to " + squareToAlgebraic(rookToSquare));
            board.movePiece(rookFromSquare, rookToSquare, rookType);
        }

        // Update castling rights and en passant square handled by Board.makeMove
    }

    /**
     * Undoes this move on the given board.
     *
     * @param board The board to undo the move on
     */
    public void undo(Board board) {
        // For promotion, we need to move the pawn back, not the promoted piece
        PieceType originalPiece = isPromotion ?
                (movedPiece.isWhite() ? PieceType.WHITE_PAWN : PieceType.BLACK_PAWN) :
                movedPiece;

        // Move the piece back
        board.movePiece(toSquare, fromSquare, originalPiece);

        // Restore captured piece
        if (isCapture) {
            if (isEnPassant) {
                int capturedPawnSquare = (fromSquare & 0x38) | (toSquare & 0x07);
                board.addPiece(capturedPawnSquare, capturedPiece);
            } else {
                board.addPiece(toSquare, capturedPiece);
            }
        }

        // Undo castling
        if (isCastling) {
            int rookFromSquare, rookToSquare;

            // Determine if it's kingside or queenside castling
            if (toSquare > fromSquare) {
                // Kingside
                rookFromSquare = (fromSquare & 0x38) | 5; // f-file of the same rank
                rookToSquare = (fromSquare & 0x38) | 7;   // h-file of the same rank
            } else {
                // Queenside
                rookFromSquare = (fromSquare & 0x38) | 3; // d-file of the same rank
                rookToSquare = fromSquare & 0x38;         // a-file of the same rank
            }

            // Move the rook back
            PieceType rookType = movedPiece.isWhite() ?
                    PieceType.WHITE_ROOK : PieceType.BLACK_ROOK;
            board.movePiece(rookFromSquare, rookToSquare, rookType);
        }

        // Restore castling rights and en passant square from the previous state
        // This is handled by the Board class which keeps track of previous states
    }


    /**
     * Updates castling rights based on the move.
     *
     * @param board The board to update castling rights on
     */
    public void updateCastlingRights(Board board) {
        System.out.println("Updating castling rights for move: " + this);
        
        // If king moves, lose both castling rights for that color
        if (movedPiece == PieceType.WHITE_KING) {
            System.out.println("White king moved, disabling white castling rights");
            board.setCastlingRight(CastlingRight.WHITE_KINGSIDE, false);
            board.setCastlingRight(CastlingRight.WHITE_QUEENSIDE, false);
        } else if (movedPiece == PieceType.BLACK_KING) {
            System.out.println("Black king moved, disabling black castling rights");
            board.setCastlingRight(CastlingRight.BLACK_KINGSIDE, false);
            board.setCastlingRight(CastlingRight.BLACK_QUEENSIDE, false);
        }
        
        // If rook moves, lose that side's castling right
        if (movedPiece == PieceType.WHITE_ROOK) {
            if (fromSquare == 0) { // a1
                System.out.println("White queenside rook moved, disabling white queenside castling");
                board.setCastlingRight(CastlingRight.WHITE_QUEENSIDE, false);
            } else if (fromSquare == 7) { // h1
                System.out.println("White kingside rook moved, disabling white kingside castling");
                board.setCastlingRight(CastlingRight.WHITE_KINGSIDE, false);
            }
        } else if (movedPiece == PieceType.BLACK_ROOK) {
            if (fromSquare == 56) { // a8
                System.out.println("Black queenside rook moved, disabling black queenside castling");
                board.setCastlingRight(CastlingRight.BLACK_QUEENSIDE, false);
            } else if (fromSquare == 63) { // h8
                System.out.println("Black kingside rook moved, disabling black kingside castling");
                board.setCastlingRight(CastlingRight.BLACK_KINGSIDE, false);
            }
        }
        
        // If a rook is captured, lose castling rights for that rook
        if (isCapture) {
            if (capturedPiece == PieceType.WHITE_ROOK) {
                if (toSquare == 0) { // a1
                    System.out.println("White queenside rook captured, disabling white queenside castling");
                    board.setCastlingRight(CastlingRight.WHITE_QUEENSIDE, false);
                } else if (toSquare == 7) { // h1
                    System.out.println("White kingside rook captured, disabling white kingside castling");
                    board.setCastlingRight(CastlingRight.WHITE_KINGSIDE, false);
                }
            } else if (capturedPiece == PieceType.BLACK_ROOK) {
                if (toSquare == 56) { // a8
                    System.out.println("Black queenside rook captured, disabling black queenside castling");
                    board.setCastlingRight(CastlingRight.BLACK_QUEENSIDE, false);
                } else if (toSquare == 63) { // h8
                    System.out.println("Black kingside rook captured, disabling black kingside castling");
                    board.setCastlingRight(CastlingRight.BLACK_KINGSIDE, false);
                }
            }
        }
    }
    private void updateEnPassantSquare(Board board) {
        // Clear previous en passant square
        board.setEnPassantSquare(-1);

        // Set new en passant square if this is a two-square pawn move
        if ((movedPiece == PieceType.WHITE_PAWN && fromSquare >= 8 && fromSquare <= 15 && toSquare == fromSquare + 16) ||
                (movedPiece == PieceType.BLACK_PAWN && fromSquare >= 48 && fromSquare <= 55 && toSquare == fromSquare - 16)) {
            board.setEnPassantSquare((fromSquare + toSquare) / 2);
        }
    }

    // Getters and setters

    public int getFromSquare() {
        return fromSquare;
    }

    public int getToSquare() {
        return toSquare;
    }

    public PieceType getMovedPiece() {
        return movedPiece;
    }

    public PieceType getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public PieceType getPromotionPiece() {
        return promotionPiece;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public boolean isCapture() {
        return isCapture;
    }

    /**
     * Converts a square index (0-63) to algebraic notation (e.g., "e4").
     *
     * @param square The square index
     * @return The algebraic notation for the square
     */
    public static String squareToAlgebraic(int square) {
        int file = square & 7;
        int rank = square >> 3;
        return "" + (char)('a' + file) + (rank + 1);
    }

    /**
     * Converts algebraic notation (e.g., "e4") to a square index (0-63).
     *
     * @param algebraic The algebraic notation for the square
     * @return The square index
     */
    public static int algebraicToSquare(String algebraic) {
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);

        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        return rank * 8 + file;
    }

    /**
     * Returns a string representation of this move in algebraic notation.
     *
     * @return The move in algebraic notation (e.g., "e2e4")
     */
    @Override
    public String toString() {
        String move = squareToAlgebraic(fromSquare) + squareToAlgebraic(toSquare);

        if (isPromotion) {
            move += String.valueOf(promotionPiece.getNotation()).toLowerCase();
        }

        return move;
    }
}