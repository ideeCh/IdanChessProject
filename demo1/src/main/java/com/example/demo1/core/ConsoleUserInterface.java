package com.example.demo1.core;

import com.example.demo1.special.PawnPromotion;

import java.util.Scanner;

/**
 * Implementation of the console-based user interface
 */
public class ConsoleUserInterface implements UserInterface {

    private GameController gameController;
    private Scanner scanner;

    public ConsoleUserInterface(GameController gameController) {
        this.gameController = gameController;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void initialize() {
        System.out.println("Initializing Console Chess Interface...");
    }

    @Override
    public void display() {
        boolean gameRunning = true;

        while (gameRunning) {
            // Display the current board
            displayBoard(gameController.getGameState());

            // Display game status
            displayGameStatus(gameController.getGameState());

            // Get player input
            if (!gameController.isGameOver()) {
                System.out.print("Enter move (e.g., e2e4) or command: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit")) {
                    gameRunning = false;
                } else if (input.equalsIgnoreCase("undo")) {
                    gameController.undoMove();
                } else if (input.equalsIgnoreCase("help")) {
                    displayHelp();
                } else if (input.equalsIgnoreCase("draw")) {
                    checkDrawConditions();
                } else if (input.equalsIgnoreCase("O-O") || input.equalsIgnoreCase("0-0")) {
                    // Kingside castling shorthand
                    makeKingsideCastling();
                } else if (input.equalsIgnoreCase("O-O-O") || input.equalsIgnoreCase("0-0-0")) {
                    // Queenside castling shorthand
                    makeQueensideCastling();
                } else {
                    try {
                        Move move = parseMove(input);
                        gameController.makeMove(move);
                    } catch (IllegalMoveException e) {
                        System.out.println("Invalid move: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Game is over. Type 'new' for a new game or 'quit' to exit.");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("new")) {
                    gameController.startNewGame();
                } else if (input.equalsIgnoreCase("quit")) {
                    gameRunning = false;
                }
            }
        }
    }
    /**
     * Handle pawn promotion
     */
    private void handlePromotion(Position source, Position target) {
        System.out.println("Promote pawn to: (q)ueen, (r)ook, (b)ishop, (n)knight");
        String input = scanner.nextLine().trim().toLowerCase();

        ChessPieceType promotionType;
        switch (input.charAt(0)) {
            case 'q': promotionType = ChessPieceType.QUEEN; break;
            case 'r': promotionType = ChessPieceType.ROOK; break;
            case 'b': promotionType = ChessPieceType.BISHOP; break;
            case 'n': promotionType = ChessPieceType.KNIGHT; break;
            default:
                System.out.println("Invalid promotion piece. Defaulting to Queen.");
                promotionType = ChessPieceType.QUEEN;
        }

        try {
            Move move = PawnPromotion.createPromotionMove(source, target, promotionType);
            gameController.makeMove(move);
        } catch (IllegalMoveException e) {
            System.out.println("Invalid promotion: " + e.getMessage());
        }
    }

    /**
     * Display help information
     */
    private void displayHelp() {
        System.out.println("\n==== Chess Commands ====");
        System.out.println("- Enter moves in the format: <source><target> (e.g., e2e4)");
        System.out.println("- For pawn promotion, add the piece letter: <source><target><piece> (e.g., e7e8q for queen)");
        System.out.println("- For castling, type O-O (or 0-0) for kingside, O-O-O (or 0-0-0) for queenside");
        System.out.println("- undo: Take back the last move");
        System.out.println("- draw: Check draw conditions");
        System.out.println("- new: Start a new game");
        System.out.println("- quit: Exit the game");
        System.out.println("- help: Show this help information\n");
    }

    /**
     * Display information about draw conditions
     */
    private void checkDrawConditions() {
        GameState gameState = gameController.getGameState();

        System.out.println("\n==== Draw Conditions ====");

        if (gameState.isStalemate()) {
            System.out.println("STALEMATE: The game is a draw due to stalemate.");
        } else {
            System.out.println("Stalemate: No");
        }

        if (gameState.isFiftyMoveRuleDraw()) {
            System.out.println("50-MOVE RULE: The game is a draw due to no captures or pawn moves in the last 50 moves.");
        } else {
            System.out.println("50-move rule: " + (gameState.getHalfMoveClock() / 2) + " moves without capture or pawn movement");
        }

        if (gameState.isThreefoldRepetitionDraw()) {
            System.out.println("THREEFOLD REPETITION: The game is a draw due to the same position occurring three times.");
        } else {
            System.out.println("Threefold repetition: No");
        }

        if (gameState.isInsufficientMaterialDraw()) {
            System.out.println("INSUFFICIENT MATERIAL: The game is a draw due to insufficient material to checkmate.");
        } else {
            System.out.println("Insufficient material: No");
        }

        System.out.println();
    }

    /**
     * Execute kingside castling for the current player
     */
    private void makeKingsideCastling() {
        try {
            Color currentPlayer = gameController.getGameState().getCurrentPlayer();
            int rank = (currentPlayer == Color.WHITE) ? 1 : 8;

            Position kingPos = new Position('e', rank);
            Position targetPos = new Position('g', rank);
            Move castlingMove = new Move(kingPos, targetPos);

            // Set up castling info
            castlingMove.setCastling(true);
            castlingMove.setCastlingRookSource(new Position('h', rank));
            castlingMove.setCastlingRookTarget(new Position('f', rank));

            gameController.makeMove(castlingMove);
        } catch (IllegalMoveException e) {
            System.out.println("Cannot castle kingside: " + e.getMessage());
        }
    }

    /**
     * Execute queenside castling for the current player
     */
    private void makeQueensideCastling() {
        try {
            Color currentPlayer = gameController.getGameState().getCurrentPlayer();
            int rank = (currentPlayer == Color.WHITE) ? 1 : 8;

            Position kingPos = new Position('e', rank);
            Position targetPos = new Position('c', rank);
            Move castlingMove = new Move(kingPos, targetPos);

            // Set up castling info
            castlingMove.setCastling(true);
            castlingMove.setCastlingRookSource(new Position('a', rank));
            castlingMove.setCastlingRookTarget(new Position('d', rank));

            gameController.makeMove(castlingMove);
        } catch (IllegalMoveException e) {
            System.out.println("Cannot castle queenside: " + e.getMessage());
        }
    }

    @Override
    public void updateBoard(GameState gameState) {
        displayBoard(gameState);
    }

    /**
     * Display the chess board in the console
     *
     * @param gameState Current game state
     */
    private void displayBoard(GameState gameState) {
        System.out.println("\n  +---+---+---+---+---+---+---+---+");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");

            for (int file = 0; file < 8; file++) {
                ChessPiece piece = gameState.getBoard().getPieceAt(new Position((char) ('a' + file), rank + 1));
                String pieceSymbol = piece == null ? " " : getPieceSymbol(piece);
                System.out.print("| " + pieceSymbol + " ");
            }

            System.out.println("|\n  +---+---+---+---+---+---+---+---+");
        }

        System.out.println("    a   b   c   d   e   f   g   h\n");
    }

    /**
     * Get the symbol representing a chess piece
     *
     * @param piece The chess piece
     * @return Symbol representing the piece
     */
    private String getPieceSymbol(ChessPiece piece) {
        String symbol;

        switch (piece.getType()) {
            case KING:
                symbol = "K";
                break;
            case QUEEN:
                symbol = "Q";
                break;
            case ROOK:
                symbol = "R";
                break;
            case BISHOP:
                symbol = "B";
                break;
            case KNIGHT:
                symbol = "N";
                break;
            case PAWN:
                symbol = "P";
                break;
            default:
                symbol = "?";
        }

        return piece.getColor() == Color.WHITE ? symbol : symbol.toLowerCase();
    }

    /**
     * Display the current status of the game
     *
     * @param gameState Current game state
     */
    private void displayGameStatus(GameState gameState) {
        System.out.println("Current player: " + gameState.getCurrentPlayer());

        if (gameState.isCheck()) {
            System.out.println("CHECK!");
        }

        if (gameState.isCheckmate()) {
            System.out.println("CHECKMATE! " + gameState.getCurrentPlayer().getOpposite() + " wins!");
        } else if (gameState.isStalemate()) {
            System.out.println("STALEMATE! Game is a draw.");
        } else if (gameState.isFiftyMoveRuleDraw()) {
            System.out.println("DRAW by 50-move rule.");
        } else if (gameState.isThreefoldRepetitionDraw()) {
            System.out.println("DRAW by threefold repetition.");
        } else if (gameState.isInsufficientMaterialDraw()) {
            System.out.println("DRAW by insufficient material.");
        }

        // Display captured pieces
        System.out.print("Captured by WHITE: ");
        for (ChessPiece piece : gameState.getCapturedPieces(Color.BLACK)) {
            System.out.print(getPieceSymbol(piece) + " ");
        }

        System.out.print("\nCaptured by BLACK: ");
        for (ChessPiece piece : gameState.getCapturedPieces(Color.WHITE)) {
            System.out.print(getPieceSymbol(piece) + " ");
        }

        System.out.println("\n");
    }

    /**
     * Parse move input from the user
     *
     * @param input User input string
     * @return Move object representing the parsed move
     * @throws IllegalArgumentException if the input format is invalid
     */
    private Move parseMove(String input) throws IllegalArgumentException {
        // Support standard algebraic notation (e.g., "e2e4")
        if (input.length() < 4) {
            throw new IllegalArgumentException("Invalid move format. Use format like 'e2e4'.");
        }

        char sourceFile = input.charAt(0);
        int sourceRank = Character.getNumericValue(input.charAt(1));
        char targetFile = input.charAt(2);
        int targetRank = Character.getNumericValue(input.charAt(3));

        Position source = new Position(sourceFile, sourceRank);
        Position target = new Position(targetFile, targetRank);

        // Check for special castling moves
        if (source.getFile() == 'e' &&
                (source.getRank() == 1 || source.getRank() == 8) &&
                Math.abs(targetFile - sourceFile) == 2) {

            boolean isKingside = targetFile == 'g';
            boolean isQueenside = targetFile == 'c';

            if (isKingside || isQueenside) {
                Move castlingMove = new Move(source, target);
                castlingMove.setCastling(true);

                int rank = source.getRank();
                if (isKingside) {
                    castlingMove.setCastlingRookSource(new Position('h', rank));
                    castlingMove.setCastlingRookTarget(new Position('f', rank));
                } else {
                    castlingMove.setCastlingRookSource(new Position('a', rank));
                    castlingMove.setCastlingRookTarget(new Position('d', rank));
                }

                return castlingMove;
            }
        }

        // Check for promotion
        ChessPieceType promotionType = null;
        if (input.length() > 4) {
            char promotionChar = input.charAt(4);
            switch (Character.toUpperCase(promotionChar)) {
                case 'Q':
                    promotionType = ChessPieceType.QUEEN;
                    break;
                case 'R':
                    promotionType = ChessPieceType.ROOK;
                    break;
                case 'B':
                    promotionType = ChessPieceType.BISHOP;
                    break;
                case 'N':
                    promotionType = ChessPieceType.KNIGHT;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid promotion piece.");
            }
        }

        return new Move(source, target, promotionType);
    }
}
