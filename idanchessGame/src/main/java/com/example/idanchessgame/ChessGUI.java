package com.example.idanchessgame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChessGUI extends Application {

    // Configure logger to suppress console output
    private static final Logger logger = Logger.getLogger(ChessGUI.class.getName());
    static {
        // Set the logger level to only show SEVERE errors
        logger.setLevel(Level.SEVERE);

        // Remove all handlers to prevent any output
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for(Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
    }

    private static final int SQUARE_SIZE = 64;
    private static final Color LIGHT_COLOR = Color.rgb(234, 233, 210); // Light square color
    private static final Color DARK_COLOR = Color.rgb(75, 115, 53);    // Dark square color
    private static final Color SELECTED_COLOR = Color.rgb(255, 255, 0, 0.7);    // Yellow with transparency for selected piece

    private GameState gameState;
    private GridPane chessboard;
    private Map<PieceType, Image> pieceImages;
    private Square[][] squares;
    private Square selectedSquare;
    private Label statusLabel;
    private List<Move> currentLegalMoves; // Store legal moves for the selected piece

    @Override
    public void start(Stage primaryStage) {
        gameState = new GameState();

        // Load piece images
        loadPieceImages();

        // Create UI components
        BorderPane root = new BorderPane();
        chessboard = createChessboard();
        updateBoardFromGameState();

        // Create status and controls
        statusLabel = new Label("White to move");
        Button undoButton = new Button("Undo Move");
        undoButton.setOnAction(e -> {
            if (gameState.undoMove()) {
                updateBoardFromGameState();
                updateStatusLabel();
                clearSelection(); // Clear selection when undoing
                selectedSquare = null;
            }
        });

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> {
            gameState = new GameState();
            updateBoardFromGameState();
            updateStatusLabel();
            clearSelection(); // Clear selection for new game
            selectedSquare = null;
        });

        HBox controls = new HBox(10, statusLabel, undoButton, newGameButton);
        controls.setPadding(new Insets(10));

        // Add components to root layout
        root.setCenter(chessboard);
        root.setBottom(controls);

        // Create scene and show stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private GridPane createChessboard() {
        GridPane board = new GridPane();
        squares = new Square[8][8];

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square square = new Square(row, col);
                squares[row][col] = square;

                // Set square color
                Color squareColor = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
                square.setFill(squareColor);
                square.setOriginalColor(squareColor); // Store the original color

                // Add click event to handle moves
                final int finalRow = row;
                final int finalCol = col;
                square.setOnMouseClicked(e -> handleSquareClick(finalRow, finalCol));

                // Add to grid (note: chess notation has row 0 at the bottom, so we invert the row)
                board.add(square, col, 7 - row);
            }
        }

        return board;
    }

    private void loadPieceImages() {
        pieceImages = new HashMap<>();

        try {
            // Try to load all piece images
            pieceImages.put(PieceType.WHITE_PAWN, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_pawn.png"))));
            pieceImages.put(PieceType.WHITE_KNIGHT, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_knight.png"))));
            pieceImages.put(PieceType.WHITE_BISHOP, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_bishop.png"))));
            pieceImages.put(PieceType.WHITE_ROOK, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_rook.png"))));
            pieceImages.put(PieceType.WHITE_QUEEN, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_queen.png"))));
            pieceImages.put(PieceType.WHITE_KING, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_king.png"))));

            pieceImages.put(PieceType.BLACK_PAWN, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_pawn.png"))));
            pieceImages.put(PieceType.BLACK_KNIGHT, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_knight.png"))));
            pieceImages.put(PieceType.BLACK_BISHOP, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_bishop.png"))));
            pieceImages.put(PieceType.BLACK_ROOK, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_rook.png"))));
            pieceImages.put(PieceType.BLACK_QUEEN, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_queen.png"))));
            pieceImages.put(PieceType.BLACK_KING, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_king.png"))));
        } catch (Exception e) {
            // Use fallback (unicode symbols or blank)
            createFallbackPieceImages();
        }
    }

    private void createFallbackPieceImages() {
        // For now pieces will be invisible if images can't be loaded
        // In a full implementation, you might create images with text/symbols
    }

    private void updateBoardFromGameState() {
        // Clear all pieces from the visual board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setPiece(null);
            }
        }

        // Add pieces based on the game state
        Board board = gameState.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int squareIndex = row * 8 + col;
                PieceType pieceType = board.getPieceAt(squareIndex);
                if (pieceType != null) {
                    squares[row][col].setPiece(pieceType);
                }
            }
        }

        // Update status label if initialized
        if (statusLabel != null) {
            updateStatusLabel();
        }
    }

    private void handleSquareClick(int row, int col) {
        if (gameState.isGameOver()) {
            return; // Game is over, no more moves allowed
        }

        int squareIndex = row * 8 + col;
        Square clickedSquare = squares[row][col];
        PieceType pieceType = gameState.getBoard().getPieceAt(squareIndex);

        // If we already have a selected piece
        if (selectedSquare != null) {
            // Try to make a move from the selected square to the clicked square
            makeMove(selectedSquare, clickedSquare);
            return;
        }

        // Clear previous selection
        clearSelection();

        // No square selected yet - select this square if it has a piece of the current player's color
        if (pieceType != null && pieceType.isWhite() == gameState.isWhiteToMove()) {
            selectedSquare = clickedSquare;
            selectedSquare.setSelected(true);

            // Store legal moves for validation (but don't highlight them)
            storeLegalMoves(squareIndex);
        } else {
            selectedSquare = null;
        }
    }

    private void makeMove(Square fromSquare, Square toSquare) {
        int fromRow = fromSquare.getRow();
        int fromCol = fromSquare.getCol();
        int fromSquareIndex = fromRow * 8 + fromCol;

        int toRow = toSquare.getRow();
        int toCol = toSquare.getCol();
        int toSquareIndex = toRow * 8 + toCol;

        String fromAlgebraic = Move.squareToAlgebraic(fromSquareIndex);
        String toAlgebraic = Move.squareToAlgebraic(toSquareIndex);

        // Get the piece type
        PieceType pieceType = gameState.getBoard().getPieceAt(fromSquareIndex);

        // Additional validation for sliding pieces
        boolean isValid = true;
        if (pieceType == PieceType.WHITE_BISHOP || pieceType == PieceType.BLACK_BISHOP) {
            isValid = Bishop.isValidBishopMove(fromSquareIndex, toSquareIndex);
        } else if (pieceType == PieceType.WHITE_QUEEN || pieceType == PieceType.BLACK_QUEEN) {
            isValid = Queen.isValidQueenMove(fromSquareIndex, toSquareIndex);
        }

        if (!isValid) {
            // Clear selection
            clearSelection();
            selectedSquare = null;
            return;
        }

        // Validate that this is a legal move by checking our stored legal moves
        boolean isLegalMove = isLegalMove(fromSquareIndex, toSquareIndex);
        if (!isLegalMove) {
            // Clear selection
            clearSelection();
            selectedSquare = null;
            return;
        }

        // Check if we need a promotion (pawn moving to the last rank)
        PieceType promotionPiece = null;

        boolean isPawnPromotion = false;
        if (pieceType == PieceType.WHITE_PAWN && toRow == 7) {
            isPawnPromotion = true;
        } else if (pieceType == PieceType.BLACK_PAWN && toRow == 0) {
            isPawnPromotion = true;
        }

        // Clear selection
        clearSelection();
        selectedSquare = null;

        if (isPawnPromotion) {
            // Show promotion dialog and wait for user selection
            boolean isWhite = pieceType.isWhite();
            promotionPiece = showPromotionDialog(isWhite);

            if (promotionPiece == null) {
                // User canceled promotion
                return;
            }
        }

        // Make the move
        boolean moveMade = gameState.makeMove(fromAlgebraic, toAlgebraic, promotionPiece);

        if (moveMade) {
            // Update the board to reflect the new state
            updateBoardFromGameState();

            // Check game status after the move
            if (gameState.isGameOver()) {
                String status = getGameEndStatus();

                // Make sure dialog appears on JavaFX thread
                final String finalStatus = status;
                Platform.runLater(() -> showGameEndDialog(finalStatus));
            }
        }
    }

    private String getGameEndStatus() {
        String status;
        switch (gameState.getStatus()) {
            case WHITE_WINS:
                status = "Checkmate! White wins.";
                break;
            case BLACK_WINS:
                status = "Checkmate! Black wins.";
                break;
            case DRAW:
                status = "Game drawn.";
                break;
            default:
                status = "Game ended.";
                break;
        }
        return status;
    }

    private void storeLegalMoves(int squareIndex) {
        Board board = gameState.getBoard();
        PieceType pieceType = board.getPieceAt(squareIndex);

        if (pieceType == null) {
            return;
        }

        // Get all legal moves from the board
        List<Move> legalMoves = board.getLegalMoves();
        currentLegalMoves = new ArrayList<>();

        // Filter moves for the selected piece
        for (Move move : legalMoves) {
            if (move.getFromSquare() == squareIndex) {
                int toSquareIndex = move.getToSquare();

                // Extra validation for queen and bishop moves
                boolean validMove = true;
                if (pieceType == PieceType.WHITE_QUEEN || pieceType == PieceType.BLACK_QUEEN) {
                    validMove = Queen.isValidQueenMove(squareIndex, toSquareIndex);
                } else if (pieceType == PieceType.WHITE_BISHOP || pieceType == PieceType.BLACK_BISHOP) {
                    validMove = Bishop.isValidBishopMove(squareIndex, toSquareIndex);
                }

                if (!validMove) {
                    continue;
                }

                // Add to our current legal moves list
                currentLegalMoves.add(move);
            }
        }
    }

    private boolean isLegalMove(int fromSquare, int toSquare) {
        if (currentLegalMoves == null) {
            return false;
        }

        for (Move move : currentLegalMoves) {
            if (move.getFromSquare() == fromSquare && move.getToSquare() == toSquare) {
                return true;
            }
        }

        return false;
    }

    private void clearSelection() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setSelected(false);
            }
        }
        currentLegalMoves = null;
    }

    private void updateStatusLabel() {
        if (statusLabel == null) {
            return;
        }

        if (gameState.isGameOver()) {
            statusLabel.setText(getGameEndStatus());
        } else {
            statusLabel.setText(gameState.isWhiteToMove() ? "White to move" : "Black to move");
            if (gameState.isInCheck()) {
                statusLabel.setText(statusLabel.getText() + " (CHECK)");
            }
        }
    }

    /**
     * Shows a dialog for pawn promotion, allowing the user to choose which piece to promote to.
     *
     * @param isWhite Whether the pawn is white or black
     * @return The selected piece type, or null if the dialog was canceled
     */
    private PieceType showPromotionDialog(boolean isWhite) {
        Dialog<PieceType> dialog = new Dialog<>();
        dialog.setTitle("Pawn Promotion");
        dialog.setHeaderText("Choose piece to promote to:");

        // Create buttons for each promotion option
        VBox options = new VBox(10);

        // Create promotion piece types using the static methods from PieceType
        PieceType[] promotionOptions = {
                PieceType.getQueenType(isWhite),   // Queen
                PieceType.getRookType(isWhite),    // Rook
                PieceType.getBishopType(isWhite),  // Bishop
                PieceType.getKnightType(isWhite)   // Knight
        };

        String[] pieceNames = {"Queen", "Rook", "Bishop", "Knight"};

        for (int i = 0; i < promotionOptions.length; i++) {
            final PieceType pieceType = promotionOptions[i];
            Button button = new Button(pieceNames[i]);

            // If images are loaded, add them to the buttons
            if (pieceImages.containsKey(pieceType) && pieceImages.get(pieceType) != null) {
                ImageView imageView = new ImageView(pieceImages.get(pieceType));
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                button.setGraphic(imageView);
            }

            button.setPrefWidth(200);
            button.setOnAction(e -> {
                dialog.setResult(pieceType);
                dialog.close();
            });

            options.getChildren().add(button);
        }

        // Add a cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(200);
        cancelButton.setOnAction(e -> {
            dialog.setResult(null);
            dialog.close();
        });
        options.getChildren().add(cancelButton);

        dialog.getDialogPane().setContent(options);

        // Remove the default buttons
        dialog.getDialogPane().getButtonTypes().clear();

        // Show the dialog and wait for result
        return dialog.showAndWait().orElse(null);
    }

    /**
     * Displays a dialog showing the game result and offering to start a new game.
     *
     * @param message The game result message to display
     */
    private void showGameEndDialog(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Ended");
        alert.setContentText(message);

        // Add buttons for "New Game" and "Close"
        ButtonType newGameButton = new ButtonType("New Game");
        ButtonType closeButton = new ButtonType("Close");

        alert.getButtonTypes().setAll(newGameButton, closeButton);

        // Show the dialog and wait for user response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == newGameButton) {
            // Start a new game
            gameState = new GameState();
            updateBoardFromGameState();
        }
    }

    /**
     * Custom component for a chess square that can display a piece
     */
    private class Square extends Rectangle {
        private final int row;
        private final int col;
        private ImageView pieceView;
        private Color originalColor;

        public Square(int row, int col) {
            this.row = row;
            this.col = col;

            // Set size and appearance
            setWidth(SQUARE_SIZE);
            setHeight(SQUARE_SIZE);

            // Create an ImageView to display the piece (initially empty)
            pieceView = new ImageView();
            pieceView.setFitWidth(SQUARE_SIZE * 0.9);
            pieceView.setFitHeight(SQUARE_SIZE * 0.9);
            pieceView.setTranslateX(SQUARE_SIZE * 0.05);
            pieceView.setTranslateY(SQUARE_SIZE * 0.05);

            // Add the ImageView to the parent GridPane
            pieceView.setMouseTransparent(true); // Allow clicks to pass through to the square
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public void setPiece(PieceType pieceType) {
            if (pieceType == null) {
                pieceView.setImage(null);
                if (pieceView.getParent() != null) {
                    ((GridPane) pieceView.getParent()).getChildren().remove(pieceView);
                }
            } else {
                Image pieceImage = pieceImages.get(pieceType);
                pieceView.setImage(pieceImage);
                if (pieceView.getParent() == null) {
                    chessboard.add(pieceView, col, 7 - row);
                }
            }
        }

        public void setOriginalColor(Color color) {
            this.originalColor = color;
        }

        public void setSelected(boolean selected) {
            if (selected) {
                setFill(SELECTED_COLOR);
            } else {
                setFill(originalColor);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}