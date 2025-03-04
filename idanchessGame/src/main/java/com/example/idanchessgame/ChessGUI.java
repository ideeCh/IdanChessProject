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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChessGUI extends Application {

    private static final int SQUARE_SIZE = 64;
    private static final Color LIGHT_COLOR = Color.rgb(234, 233, 210); // Light square color
    private static final Color DARK_COLOR = Color.rgb(75, 115, 53);    // Dark square color

    private GameState gameState;
    private GridPane chessboard;
    private Map<PieceType, Image> pieceImages;
    private Square[][] squares;
    private Square selectedSquare;
    private Label statusLabel;

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
            }
        });

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> {
            gameState = new GameState();
            updateBoardFromGameState();
            updateStatusLabel();
            selectedSquare = null;
        });

        // Debug button to test game end dialog
        Button testEndButton = new Button("Test End Game");
        testEndButton.setOnAction(e -> {
            // Force a game end state for testing the dialog
            try {
                // Use reflection to set the game status field
                java.lang.reflect.Field statusField = GameState.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(gameState, GameState.GameStatus.WHITE_WINS);

                // Update UI
                updateStatusLabel();

                // Show dialog
                showGameEndDialog("White wins (Test)");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox controls = new HBox(10, statusLabel, undoButton, newGameButton, testEndButton);
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
            pieceImages.put(PieceType.WHITE_PAWN, new Image(getClass().getResourceAsStream("/images/white_pawn.png")));
            pieceImages.put(PieceType.WHITE_KNIGHT, new Image(getClass().getResourceAsStream("/images/white_knight.png")));
            pieceImages.put(PieceType.WHITE_BISHOP, new Image(getClass().getResourceAsStream("/images/white_bishop.png")));
            pieceImages.put(PieceType.WHITE_ROOK, new Image(getClass().getResourceAsStream("/images/white_rook.png")));
            pieceImages.put(PieceType.WHITE_QUEEN, new Image(getClass().getResourceAsStream("/images/white_queen.png")));
            pieceImages.put(PieceType.WHITE_KING, new Image(getClass().getResourceAsStream("/images/white_king.png")));

            pieceImages.put(PieceType.BLACK_PAWN, new Image(getClass().getResourceAsStream("/images/black_pawn.png")));
            pieceImages.put(PieceType.BLACK_KNIGHT, new Image(getClass().getResourceAsStream("/images/black_knight.png")));
            pieceImages.put(PieceType.BLACK_BISHOP, new Image(getClass().getResourceAsStream("/images/black_bishop.png")));
            pieceImages.put(PieceType.BLACK_ROOK, new Image(getClass().getResourceAsStream("/images/black_rook.png")));
            pieceImages.put(PieceType.BLACK_QUEEN, new Image(getClass().getResourceAsStream("/images/black_queen.png")));
            pieceImages.put(PieceType.BLACK_KING, new Image(getClass().getResourceAsStream("/images/black_king.png")));

            // Log successful loading
            System.out.println("Successfully loaded all chess piece images");
        } catch (Exception e) {
            System.err.println("Failed to load chess piece images: " + e.getMessage());
            e.printStackTrace();

            // Use fallback (unicode symbols or blank)
            createFallbackPieceImages();
        }
    }

    private void createFallbackPieceImages() {
        System.out.println("Using fallback piece representation");
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
        System.out.println("Square clicked: " + row + "," + col);

        if (gameState.isGameOver()) {
            System.out.println("Game is already over. No more moves allowed.");
            return; // Game is over, no more moves allowed
        }

        int squareIndex = row * 8 + col;
        Square clickedSquare = squares[row][col];

        if (selectedSquare == null) {
            // No square selected yet - select this square if it has a piece of the current player's color
            PieceType pieceType = gameState.getBoard().getPieceAt(squareIndex);
            if (pieceType != null && pieceType.isWhite() == gameState.isWhiteToMove()) {
                selectedSquare = clickedSquare;
                selectedSquare.setSelected(true);
                System.out.println("Selected piece: " + pieceType + " at " + row + "," + col);
            }
        } else {
            // A square is already selected - try to make a move
            int fromRow = selectedSquare.getRow();
            int fromCol = selectedSquare.getCol();
            int fromSquare = fromRow * 8 + fromCol;

            String fromAlgebraic = Move.squareToAlgebraic(fromSquare);
            String toAlgebraic = Move.squareToAlgebraic(squareIndex);

            System.out.println("Attempting move from " + fromAlgebraic + " to " + toAlgebraic);

            // Check if we need a promotion (pawn moving to the last rank)
            PieceType pieceType = gameState.getBoard().getPieceAt(fromSquare);
            PieceType promotionPiece = null;

            boolean isPawnPromotion = false;
            if (pieceType == PieceType.WHITE_PAWN && row == 7) {
                isPawnPromotion = true;
                System.out.println("White pawn promotion detected");
            } else if (pieceType == PieceType.BLACK_PAWN && row == 0) {
                isPawnPromotion = true;
                System.out.println("Black pawn promotion detected");
            }

            // Deselect the square
            selectedSquare.setSelected(false);
            selectedSquare = null;

            boolean moveMade = false;

            if (isPawnPromotion) {
                // Show promotion dialog and wait for user selection
                boolean isWhite = pieceType.isWhite();
                promotionPiece = showPromotionDialog(isWhite);

                if (promotionPiece == null) {
                    // User canceled promotion
                    System.out.println("Promotion canceled by user");
                    return;
                }

                System.out.println("User selected promotion to: " + promotionPiece);

                // Create explicit promotion move
                Move promotionMove = new Move(
                        fromSquare,
                        squareIndex,
                        pieceType,
                        gameState.getBoard().getPieceAt(squareIndex),  // Captured piece (if any)
                        true,                                         // isPromotion = true
                        promotionPiece,                               // The piece we're promoting to
                        false,                                        // isCastling = false
                        false                                         // isEnPassant = false
                );

                // Make the promotion move
                moveMade = gameState.makeMove(promotionMove);
                System.out.println("Promotion move made: " + moveMade);
            } else {
                // Regular move
                moveMade = gameState.makeMove(fromAlgebraic, toAlgebraic, null);
                System.out.println("Regular move made: " + moveMade);
            }

            if (moveMade) {
                // Update the board to reflect the new state
                updateBoardFromGameState();

                // Check game status after the move
                if (gameState.isGameOver()) {
                    System.out.println("Game over detected. Status: " + gameState.getStatus());

                    String status = "";
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

                    // Make sure dialog appears on JavaFX thread
                    final String finalStatus = status;
                    Platform.runLater(() -> {
                        showGameEndDialog(finalStatus);
                    });
                }
            }
        }
    }

    private void updateStatusLabel() {
        if (statusLabel == null) {
            System.out.println("Warning: statusLabel is null in updateStatusLabel()");
            return;
        }

        if (gameState.isGameOver()) {
            switch (gameState.getStatus()) {
                case WHITE_WINS:
                    statusLabel.setText("Checkmate! White wins.");
                    break;
                case BLACK_WINS:
                    statusLabel.setText("Checkmate! Black wins.");
                    break;
                case DRAW:
                    statusLabel.setText("Game drawn.");
                    break;
                default:
                    statusLabel.setText("Game ended.");
                    break;
            }
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
        // This ensures we get the correct enum instances
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
        System.out.println("Showing game end dialog: " + message);

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
            System.out.println("Starting new game");
            // Start a new game
            gameState = new GameState();
            updateBoardFromGameState();
        } else {
            System.out.println("Dialog closed without starting new game");
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

        public void setSelected(boolean selected) {
            if (selected) {
                originalColor = (Color) getFill();
                setFill(Color.YELLOW);
            } else {
                setFill(originalColor);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}