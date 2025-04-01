package com.example.demo1.core;

/**
 * Class representing the chess board
 */
public class Board implements Cloneable {

    private ChessPiece[][] squares;

    /**
     * Constructor for the chess board
     */
    public Board() {
        this.squares = new ChessPiece[8][8];
        setupInitialPosition();
    }

    /**
     * Set up the initial position of pieces on the board
     */
    private void setupInitialPosition() {
        // Place pieces in their initial positions

        // Place pawns
        for (int file = 0; file < 8; file++) {
            squares[1][file] = ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE);
            squares[6][file] = ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK);
        }

        // Place rooks
        squares[0][0] = ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE);
        squares[0][7] = ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE);
        squares[7][0] = ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.BLACK);
        squares[7][7] = ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.BLACK);

        // Place knights
        squares[0][1] = ChessPieceFactory.createPiece(ChessPieceType.KNIGHT, Color.WHITE);
        squares[0][6] = ChessPieceFactory.createPiece(ChessPieceType.KNIGHT, Color.WHITE);
        squares[7][1] = ChessPieceFactory.createPiece(ChessPieceType.KNIGHT, Color.BLACK);
        squares[7][6] = ChessPieceFactory.createPiece(ChessPieceType.KNIGHT, Color.BLACK);

        // Place bishops
        squares[0][2] = ChessPieceFactory.createPiece(ChessPieceType.BISHOP, Color.WHITE);
        squares[0][5] = ChessPieceFactory.createPiece(ChessPieceType.BISHOP, Color.WHITE);
        squares[7][2] = ChessPieceFactory.createPiece(ChessPieceType.BISHOP, Color.BLACK);
        squares[7][5] = ChessPieceFactory.createPiece(ChessPieceType.BISHOP, Color.BLACK);

        // Place queens
        squares[0][3] = ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE);
        squares[7][3] = ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.BLACK);

        // Place kings
        squares[0][4] = ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE);
        squares[7][4] = ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK);
    }

    /**
     * Get the piece at a specific position
     *
     * @param position Position on the board
     * @return The chess piece at the position, or null if empty
     */
    public ChessPiece getPieceAt(Position position) {
        int rank = position.getRank() - 1;
        int file = position.getFile() - 'a';

        if (rank < 0 || rank >= 8 || file < 0 || file >= 8) {
            return null;
        }

        return squares[rank][file];
    }

    /**
     * Set a piece at a specific position
     *
     * @param position Position on the board
     * @param piece    The chess piece to place
     */
    public void setPieceAt(Position position, ChessPiece piece) {
        int rank = position.getRank() - 1;
        int file = position.getFile() - 'a';

        if (rank >= 0 && rank < 8 && file >= 0 && file < 8) {
            squares[rank][file] = piece;
        }
    }

    /**
     * Move a piece on the board
     *
     * @param move         The move to make
     * @param isSimulation Whether this is a simulated move
     */
    public void movePiece(Move move, boolean isSimulation) {
        ChessPiece piece = getPieceAt(move.getSource());

        if (piece != null) {
            // If this is a simulation, we need to create a copy of the piece
            // to avoid modifying the original
            if (isSimulation) {
                try {
                    // Clone the piece for the simulation
                    piece = (ChessPiece) piece.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }

            setPieceAt(move.getTarget(), piece);
            setPieceAt(move.getSource(), null);

            // Update piece's move status
            // Only update the hasMoved flag in real moves, not simulations
            if (!isSimulation) {
                piece.setHasMoved(true);
            }
        }
    }

    /**
     * Move a piece on the board (non-simulation version)
     *
     * @param move The move to make
     */
    public void movePiece(Move move) {
        movePiece(move, false);
    }

    /**
     * Create a deep copy of the board
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Board clone = (Board) super.clone();

        clone.squares = new ChessPiece[8][8];

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                if (squares[rank][file] != null) {
                    clone.squares[rank][file] = (ChessPiece) squares[rank][file].clone();
                }
            }
        }

        return clone;
    }
}
