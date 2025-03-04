package com.example.idanchessgame;

/**
 * Controller for a chess game, managing the interaction between the model and view.
 */
public class GameController {
    private GameState gameState;
    private ConsoleView view;

    /**
     * Creates a new game controller.
     */
    public GameController() {
        gameState = new GameState();
        view = new ConsoleView();
    }

    /**
     * Starts a new chess game.
     */
    public void startGame() {
        view.displayWelcomeMessage();

        while (!gameState.isGameOver()) {
            view.displayBoard(gameState.getBoard());

            if (gameState.isInCheck()) {
                view.displayCheckMessage(gameState.isWhiteToMove());
            }

            String playerInput = view.getPlayerMove(gameState.isWhiteToMove());

            // Process commands
            if (playerInput.equalsIgnoreCase("quit") || playerInput.equalsIgnoreCase("exit")) {
                break;
            } else if (playerInput.equalsIgnoreCase("undo")) {
                if (gameState.undoMove()) {
                    view.displayMessage("Move undone.");
                } else {
                    view.displayMessage("No moves to undo.");
                }
                continue;
            } else if (playerInput.equalsIgnoreCase("help")) {
                view.displayHelpMessage();
                continue;
            }

            // Process move input
            try {
                // Parse move input
                String[] parts = playerInput.trim().split("\\s+");

                if (parts.length < 1 || parts.length > 2) {
                    view.displayMessage("Invalid input format. Please use format 'e2e4' or 'e7e8q' for promotions.");
                    continue;
                }

                String moveStr = parts[0];

                if (moveStr.length() < 4 || moveStr.length() > 5) {
                    view.displayMessage("Invalid move format. Please use format 'e2e4' or 'e7e8q' for promotions.");
                    continue;
                }

                String fromSquare = moveStr.substring(0, 2);
                String toSquare = moveStr.substring(2, 4);

                PieceType promotionPiece = null;
                if (moveStr.length() == 5) {
                    char promotionChar = Character.toLowerCase(moveStr.charAt(4));

                    switch (promotionChar) {
                        case 'q':
                            promotionPiece = gameState.isWhiteToMove() ? PieceType.WHITE_QUEEN : PieceType.BLACK_QUEEN;
                            break;
                        case 'r':
                            promotionPiece = gameState.isWhiteToMove() ? PieceType.WHITE_ROOK : PieceType.BLACK_ROOK;
                            break;
                        case 'b':
                            promotionPiece = gameState.isWhiteToMove() ? PieceType.WHITE_BISHOP : PieceType.BLACK_BISHOP;
                            break;
                        case 'n':
                            promotionPiece = gameState.isWhiteToMove() ? PieceType.WHITE_KNIGHT : PieceType.BLACK_KNIGHT;
                            break;
                        default:
                            view.displayMessage("Invalid promotion piece. Please use q/r/b/n.");
                            continue;
                    }
                }

                boolean moveMade = gameState.makeMove(fromSquare, toSquare, promotionPiece);

                if (!moveMade) {
                    view.displayMessage("Illegal move. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                view.displayMessage("Invalid input: " + e.getMessage());
            }
        }

        // Game over
        view.displayBoard(gameState.getBoard());

        if (gameState.getStatus() == GameState.GameStatus.WHITE_WINS) {
            view.displayMessage("Checkmate! White wins.");
        } else if (gameState.getStatus() == GameState.GameStatus.BLACK_WINS) {
            view.displayMessage("Checkmate! Black wins.");
        } else if (gameState.getStatus() == GameState.GameStatus.DRAW) {
            view.displayMessage("Game drawn.");
        } else {
            view.displayMessage("Game ended.");
        }
    }

    /**
     * Gets the game state.
     *
     * @return The game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Gets the view.
     *
     * @return The view
     */
    public ConsoleView getView() {
        return view;
    }
}