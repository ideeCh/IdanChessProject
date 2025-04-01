package com.example.demo1;

import com.example.demo1.core.*;
import com.example.demo1.core.Color;
import com.example.demo1.core.ai.AIManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX implementation of the graphical user interface for the chess application.
 * This class extends JavaFX's Application class and serves as the entry point for the GUI version.
 */
public class ChessApplicationFX extends Application {

    // Configure logger to reduce console output
    private static final Logger logger = Logger.getLogger(ChessApplicationFX.class.getName());
    static {
        logger.setLevel(Level.WARNING);
    }

    // Use alias for JavaFX Color to avoid conflicts with game's Color enum
    private static final javafx.scene.paint.Color LIGHT_COLOR = javafx.scene.paint.Color.rgb(238, 238, 210); // Light square color
    private static final javafx.scene.paint.Color DARK_COLOR = javafx.scene.paint.Color.rgb(118, 150, 86);   // Dark square color

    // Enhanced highlight colors with more defined appearance
    private static final javafx.scene.paint.Color SELECTED_COLOR = javafx.scene.paint.Color.rgb(255, 215, 0, 0.7); // Gold with less transparency
    private static final javafx.scene.paint.Color LEGAL_MOVE_COLOR = javafx.scene.paint.Color.rgb(106, 168, 79, 0.8); // Brighter green
    private static final javafx.scene.paint.Color CAPTURE_COLOR = javafx.scene.paint.Color.rgb(244, 67, 54, 0.8); // Brighter red
    private static final javafx.scene.paint.Color CHECK_COLOR = javafx.scene.paint.Color.rgb(255, 0, 0, 0.5); // Red for king in check

    // UI Constants
    private static final int SQUARE_SIZE = 70;

    // Game components
    private GameController gameController;
    private GridPane chessBoard;
    private BorderPane root;
    private Label statusLabel;
    private StackPane[][] boardSquares;
    private Map<String, Image> pieceImages;
    private Stage primaryStage;
    private Popup checkPopup;
    private Timeline checkAnimation;

    // AI components
    private CheckBox aiEnabledCheckbox;
    private ToggleGroup aiColorGroup;
    private RadioButton whiteAIRadio;
    private RadioButton blackAIRadio;

    // Move tracking
    private Position selectedPosition;
    private final List<Position> highlightedPositions = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        gameController = new GameController();
        gameController.initialize();

        // Set up UI callback for the game controller
        gameController.setUICallback(new GameController.GameUICallback() {
            @Override
            public void onBoardUpdated() {
                updateBoardDisplay();
            }

            @Override
            public void onGameOver(String message) {
                showGameEndDialog();
            }
        });

        // Root layout
        root = new BorderPane();

        // Initialize piece images
        loadPieceImages();

        // Create chess board
        createChessBoard();

        // Create status bar
        statusLabel = new Label("White's turn");
        statusLabel.setPadding(new Insets(10));
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Create toolbar with buttons
        ToolBar toolbar = createToolbar();

        // Create side panel for move history and captured pieces
        VBox sidePanel = createSidePanel();

        // Add components to root layout
        root.setTop(toolbar);
        root.setCenter(chessBoard);
        root.setBottom(statusLabel);
        root.setRight(sidePanel);

        // Set background color for the entire scene
        root.setStyle("-fx-background-color: #2E2E2E;"); // Dark background

        // Create scene
        Scene scene = new Scene(root, 900, 700);

        // Set up stage
        primaryStage.setTitle("Java Chess Application");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize check popup
        createCheckPopup();

        // Update the board display
        updateBoardDisplay();

        // Set default AI settings - enabled and playing as Black
        gameController.setAIColor(Color.BLACK);
        gameController.setAIEnabled(true);

        // Make sure UI reflects initial settings
        aiEnabledCheckbox.setSelected(true);
        blackAIRadio.setSelected(true);
    }

    /**
     * Create a popup to show when player is in check
     */
    private void createCheckPopup() {
        checkPopup = new Popup();
        checkPopup.setAutoHide(true);

        Label checkLabel = new Label("CHECK!");
        checkLabel.setPadding(new Insets(15));
        checkLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        checkLabel.setStyle("-fx-background-color: rgba(255, 0, 0, 0.8); -fx-text-fill: white; " +
                "-fx-background-radius: 10;");

        // Add a glow effect to make it more visible
        DropShadow shadow = new DropShadow();
        shadow.setColor(javafx.scene.paint.Color.RED);
        shadow.setRadius(15);
        checkLabel.setEffect(shadow);

        checkPopup.getContent().add(checkLabel);
    }

    /**
     * Show the check popup and animate the king's square
     */
    private void showCheckNotification() {
        // Find the king's position
        Position kingPosition = null;
        Color currentPlayer = gameController.getGameState().getCurrentPlayer();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = gameController.getGameState().getBoard().getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.KING &&
                        piece.getColor() == currentPlayer) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) break;
        }

        // If king found, highlight it
        if (kingPosition != null) {
            int file = kingPosition.getFile() - 'a';
            int rank = 8 - kingPosition.getRank(); // Convert to display coordinates

            // Highlight the king's square
            StackPane kingSquare = boardSquares[rank][file];

            // Store original background
            Background originalBackground = kingSquare.getBackground();

            // Create a pulsing effect for the king's square
            if (checkAnimation != null) {
                checkAnimation.stop();
            }

            checkAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> kingSquare.setBackground(new Background(new BackgroundFill(
                            CHECK_COLOR, CornerRadii.EMPTY, Insets.EMPTY)))),
                    new KeyFrame(Duration.seconds(0.5), e -> kingSquare.setBackground(originalBackground))
            );
            checkAnimation.setCycleCount(6); // Pulse 3 times
            checkAnimation.setAutoReverse(true);
            checkAnimation.play();
        }

        // Show the popup
        checkPopup.setX(primaryStage.getX() + primaryStage.getWidth()/2 - 75);
        checkPopup.setY(primaryStage.getY() + primaryStage.getHeight()/2 - 75);
        checkPopup.show(primaryStage);

        // Hide popup after 2 seconds
        Timeline hidePopup = new Timeline(new KeyFrame(Duration.seconds(2), e -> checkPopup.hide()));
        hidePopup.play();
    }

    /**
     * Load piece images from resources
     */
    private void loadPieceImages() {
        pieceImages = new HashMap<>();

        // Create a map to easily load the images
        String[] colors = {"white", "black"};
        String[] pieces = {"king", "queen", "rook", "bishop", "knight", "pawn"};

        try {
            for (String color : colors) {
                for (String piece : pieces) {
                    String key = color + "_" + piece;
                    String imagePath = "/images/" + key + ".png";

                    // Load the image from resources
                    InputStream resourceStream = getClass().getResourceAsStream(imagePath);
                    if (resourceStream != null) {
                        Image image = new Image(resourceStream);
                        pieceImages.put(key, image);
                    }
                }
            }
            System.out.println("Successfully loaded chess piece images");
        } catch (Exception e) {
            System.err.println("Error loading chess piece images: " + e.getMessage());
            // Fall back to file system if resources not found
            loadPieceImagesFromFileSystem();
        }
    }

    /**
     * Alternative method to load images from file system if resources aren't working
     */
    private void loadPieceImagesFromFileSystem() {
        try {
            String[] colors = {"white", "black"};
            String[] pieces = {"king", "queen", "rook", "bishop", "knight", "pawn"};

            // Try a few common locations
            String[] possiblePaths = {
                    "src/main/resources/images/",
                    "resources/images/",
                    "images/"
            };

            for (String basePath : possiblePaths) {
                File dir = new File(basePath);
                if (dir.exists() && dir.isDirectory()) {
                    for (String color : colors) {
                        for (String piece : pieces) {
                            String key = color + "_" + piece;
                            String imagePath = basePath + key + ".png";

                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                Image image = new Image(imageFile.toURI().toString());
                                pieceImages.put(key, image);
                            }
                        }
                    }

                    // If we successfully loaded at least some images, break
                    if (!pieceImages.isEmpty()) {
                        System.out.println("Loaded images from: " + basePath);
                        break;
                    }
                }
            }

            // If we still don't have images, log a warning
            if (pieceImages.isEmpty()) {
                System.out.println("No image files found. Using Unicode symbols as fallback.");
            }
        } catch (Exception e) {
            System.err.println("Error loading chess piece images from file system: " + e.getMessage());
        }
    }

    /**
     * Create the chess board grid
     */
    private void createChessBoard() {
        chessBoard = new GridPane();
        boardSquares = new StackPane[8][8];

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                // Create square
                StackPane square = new StackPane();
                square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

                // Set square color
                boolean isLightSquare = (rank + file) % 2 == 0;
                javafx.scene.paint.Color squareColor = isLightSquare ? LIGHT_COLOR : DARK_COLOR;
                square.setBackground(new Background(new BackgroundFill(
                        squareColor,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));

                // Store the original color as a property
                square.getProperties().put("originalColor", squareColor);

                // Store reference to the square
                boardSquares[rank][file] = square;

                // Add event handlers for clicks
                final int finalRank = 7 - rank;  // Flip for chess coordinates
                final int finalFile = file;

                square.setOnMouseClicked(event -> handleSquareClick(finalFile, finalRank));

                // Add to grid
                chessBoard.add(square, file, rank);
            }
        }

        // Add a border around the board
        chessBoard.setPadding(new Insets(10));
        chessBoard.setStyle("-fx-background-color: #4D4D4D; -fx-padding: 10; -fx-border-color: #333333; -fx-border-width: 5;");
    }

    /**
     * Create the toolbar with game control buttons
     */
    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #4D4D4D; -fx-padding: 10;");

        Button newGameButton = new Button("New Game");
        styleButton(newGameButton);
        newGameButton.setOnAction(e -> {
            gameController.startNewGame();
            updateBoardDisplay();
            statusLabel.setText("White's turn");
            clearSelection();
        });

        Button undoButton = new Button("Undo Move");
        styleButton(undoButton);
        undoButton.setOnAction(e -> {
            if (gameController.undoMove()) {
                updateBoardDisplay();
                updateStatusLabel();
                clearSelection();
            }
        });

        // Add AI controls
        Label aiLabel = new Label("AI:");
        aiLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        aiEnabledCheckbox = new CheckBox("Enable AI");
        aiEnabledCheckbox.setStyle("-fx-text-fill: white;");
        aiEnabledCheckbox.setSelected(true);
        aiEnabledCheckbox.setOnAction(e -> {
            gameController.setAIEnabled(aiEnabledCheckbox.isSelected());
        });

        Label aiColorLabel = new Label("AI plays as:");
        aiColorLabel.setStyle("-fx-text-fill: white;");

        aiColorGroup = new ToggleGroup();

        whiteAIRadio = new RadioButton("White");
        whiteAIRadio.setToggleGroup(aiColorGroup);
        whiteAIRadio.setStyle("-fx-text-fill: white;");
        whiteAIRadio.setOnAction(e -> {
            if (whiteAIRadio.isSelected()) {
                gameController.setAIColor(Color.WHITE);
            }
        });

        blackAIRadio = new RadioButton("Black");
        blackAIRadio.setToggleGroup(aiColorGroup);
        blackAIRadio.setStyle("-fx-text-fill: white;");
        blackAIRadio.setSelected(true); // Default AI plays as black
        blackAIRadio.setOnAction(e -> {
            if (blackAIRadio.isSelected()) {
                gameController.setAIColor(Color.BLACK);
            }
        });

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #777777;");

        // Add buttons to toolbar
        toolbar.getItems().addAll(
                newGameButton,
                undoButton,
                new Separator(),
                aiLabel,
                aiEnabledCheckbox,
                aiColorLabel,
                whiteAIRadio,
                blackAIRadio
        );

        return toolbar;
    }

    /**
     * Apply standard styling to buttons
     */
    private void styleButton(Button button) {
        button.setStyle(
                "-fx-background-color: #555555; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 15 8 15;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #666666; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8 15 8 15;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #555555; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8 15 8 15;"
                )
        );
    }

    /**
     * Create the side panel for move history and captured pieces
     */
    private VBox createSidePanel() {
        VBox sidePanel = new VBox(10);
        sidePanel.setPadding(new Insets(10));
        sidePanel.setPrefWidth(200);
        sidePanel.setStyle("-fx-background-color: #3E3E3E;");

        // Move history
        TitledPane moveHistoryPane = new TitledPane();
        moveHistoryPane.setText("Move History");
        moveHistoryPane.setStyle("-fx-text-fill: white;");

        ListView<String> moveHistoryList = new ListView<>();
        moveHistoryList.setStyle("-fx-control-inner-background: #2E2E2E; -fx-text-fill: white;");
        // Add recent moves to history view
        List<Move> moveHistory = gameController.getGameState().getMoveHistory();
        for (Move move : moveHistory) {
            moveHistoryList.getItems().add(move.toString());
        }
        moveHistoryPane.setContent(moveHistoryList);

        // Captured pieces
        TitledPane capturedPiecesPane = new TitledPane();
        capturedPiecesPane.setText("Captured Pieces");
        capturedPiecesPane.setStyle("-fx-text-fill: white;");

        HBox capturedPiecesBox = new HBox(5);
        capturedPiecesBox.setStyle("-fx-background-color: #2E2E2E;");
        capturedPiecesPane.setContent(capturedPiecesBox);

        // Add components to side panel
        sidePanel.getChildren().addAll(moveHistoryPane, capturedPiecesPane);

        return sidePanel;
    }

    /**
     * Update the board display based on the current game state
     */
    private void updateBoardDisplay() {
        // Clear all squares
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                boardSquares[rank][file].getChildren().clear();

                // Reset square color to original
                javafx.scene.paint.Color originalColor = (javafx.scene.paint.Color) boardSquares[rank][file].getProperties().get("originalColor");
                boardSquares[rank][file].setBackground(new Background(new BackgroundFill(
                        originalColor,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));
            }
        }

        // Add pieces based on game state
        GameState gameState = gameController.getGameState();
        Board board = gameState.getBoard();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null) {
                    // Create an image view for the piece
                    ImageView pieceView = createPieceImageView(piece);

                    // Add to the appropriate square (flip rank for display)
                    int displayRank = 8 - rank;
                    int displayFile = file - 'a';

                    boardSquares[displayRank][displayFile].getChildren().add(pieceView);
                }
            }
        }

        // Update move history
        updateMoveHistory();
        updateCapturedPieces();

        // Update status
        updateStatusLabel();

        // Show check notification if in check
        if (gameState.isCheck()) {
            showCheckNotification();
        }
    }

    /**
     * Update the status label based on the current game state
     */
    private void updateStatusLabel() {
        GameState gameState = gameController.getGameState();
        Color currentPlayer = gameState.getCurrentPlayer();
        boolean isPlayerAI = (currentPlayer == gameController.getAIColor() && gameController.isAIEnabled());

        if (gameState.isCheckmate()) {
            Color winningColor = currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
            statusLabel.setText("CHECKMATE! " + winningColor + " wins!");
            statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: red;");
        } else if (gameState.isStalemate()) {
            statusLabel.setText("STALEMATE! Game is a draw.");
            statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: blue;");
        } else if (gameState.isCheck()) {
            String playerType = isPlayerAI ? "AI" : "You";
            statusLabel.setText("CHECK! " + playerType + " (" + currentPlayer + ") must move out of check!");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            String playerType = isPlayerAI ? "AI" : "Your";
            statusLabel.setText(playerType + " turn (" + currentPlayer + ")");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        }
    }

    /**
     * Update move history panel
     */
    private void updateMoveHistory() {
        // Find the ListView in the side panel
        TitledPane moveHistoryPane = (TitledPane) ((VBox) root.getRight()).getChildren().get(0);
        @SuppressWarnings("unchecked")
        ListView<String> moveHistoryList = (ListView<String>) moveHistoryPane.getContent();

        // Clear and update
        moveHistoryList.getItems().clear();
        List<Move> moveHistory = gameController.getGameState().getMoveHistory();
        for (int i = 0; i < moveHistory.size(); i++) {
            Move move = moveHistory.get(i);
            String moveText = (i % 2 == 0 ? ((i/2) + 1) + ". " : "") + move.toString();
            moveHistoryList.getItems().add(moveText);
        }

        // Scroll to the bottom to show latest move
        if (!moveHistoryList.getItems().isEmpty()) {
            moveHistoryList.scrollTo(moveHistoryList.getItems().size() - 1);
        }
    }

    /**
     * Update captured pieces panel
     */
    private void updateCapturedPieces() {
        // Find the HBox in the side panel
        TitledPane capturedPiecesPane = (TitledPane) ((VBox) root.getRight()).getChildren().get(1);
        HBox capturedPiecesBox = (HBox) capturedPiecesPane.getContent();

        // Clear and update
        capturedPiecesBox.getChildren().clear();

        // Add white's captured pieces
        VBox whiteCaptured = new VBox(5);
        Label whiteLabel = new Label("White captured:");
        whiteLabel.setStyle("-fx-text-fill: white;");
        whiteCaptured.getChildren().add(whiteLabel);

        HBox whitePieces = new HBox(2);
        for (ChessPiece piece : gameController.getGameState().getCapturedPieces(Color.BLACK)) {
            ImageView pieceView = createPieceImageView(piece);
            pieceView.setFitWidth(25);
            pieceView.setFitHeight(25);
            whitePieces.getChildren().add(pieceView);
        }
        whiteCaptured.getChildren().add(whitePieces);

        // Add black's captured pieces
        VBox blackCaptured = new VBox(5);
        Label blackLabel = new Label("Black captured:");
        blackLabel.setStyle("-fx-text-fill: white;");
        blackCaptured.getChildren().add(blackLabel);

        HBox blackPieces = new HBox(2);
        for (ChessPiece piece : gameController.getGameState().getCapturedPieces(Color.WHITE)) {
            ImageView pieceView = createPieceImageView(piece);
            pieceView.setFitWidth(25);
            pieceView.setFitHeight(25);
            blackPieces.getChildren().add(pieceView);
        }
        blackCaptured.getChildren().add(blackPieces);

        capturedPiecesBox.getChildren().addAll(whiteCaptured, blackCaptured);
    }

    /**
     * Create an image view for a chess piece
     */
    private ImageView createPieceImageView(ChessPiece piece) {
        // Determine image key based on piece color and type
        String colorStr = (piece.getColor() == Color.WHITE) ? "white" : "black";
        String pieceStr = piece.getType().toString().toLowerCase();
        String key = colorStr + "_" + pieceStr;

        // Try to use images if available
        if (pieceImages.containsKey(key) && pieceImages.get(key) != null) {
            ImageView imageView = new ImageView(pieceImages.get(key));
            imageView.setFitWidth(SQUARE_SIZE * 0.8);  // Set appropriate size
            imageView.setFitHeight(SQUARE_SIZE * 0.8);
            return imageView;
        }

        // Fall back to Unicode symbols if no images available
        Label label = new Label(getPieceUnicodeSymbol(piece));
        label.setStyle("-fx-font-size: 36; -fx-font-weight: bold;");

        // Use proper color from game logic, not JavaFX color
        if (piece.getColor() == Color.WHITE) {
            label.setTextFill(javafx.scene.paint.Color.IVORY);
        } else {
            label.setTextFill(javafx.scene.paint.Color.BLACK);
        }

        StackPane pieceView = new StackPane(label);
        pieceView.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        // Create an ImageView wrapper
        ImageView imageView = new ImageView();
        imageView.setUserData(pieceView);

        return imageView;
    }

    /**
     * Get Unicode symbol for a chess piece
     */
    private String getPieceUnicodeSymbol(ChessPiece piece) {
        if (piece.getColor() == Color.WHITE) {
            switch (piece.getType()) {
                case KING: return "♔";
                case QUEEN: return "♕";
                case ROOK: return "♖";
                case BISHOP: return "♗";
                case KNIGHT: return "♘";
                case PAWN: return "♙";
                default: return "?";
            }
        } else {
            switch (piece.getType()) {
                case KING: return "♚";
                case QUEEN: return "♛";
                case ROOK: return "♜";
                case BISHOP: return "♝";
                case KNIGHT: return "♞";
                case PAWN: return "♟";
                default: return "?";
            }
        }
    }

    /**
     * Handle click on a chess square
     */
    private void handleSquareClick(int file, int rank) {
        // Convert to chess coordinates
        char fileChar = (char) ('a' + file);
        int rankNum = rank + 1;
        Position clickedPosition = new Position(fileChar, rankNum);

        System.out.println("Clicked: " + clickedPosition);

        // Get the piece at this position
        ChessPiece piece = gameController.getGameState().getBoard().getPieceAt(clickedPosition);

        // If the game is over, don't allow moves
        if (gameController.isGameOver()) {
            showGameEndDialog();
            return;
        }

        // If it's AI's turn and AI is enabled, don't allow player to move
        Color currentPlayer = gameController.getGameState().getCurrentPlayer();
        boolean isAITurn = currentPlayer == gameController.getAIColor() && gameController.isAIEnabled();

        if (isAITurn) {
            statusLabel.setText("It's AI's turn. Please wait...");
            return;
        }

        // If no piece is selected yet
        if (selectedPosition == null) {
            // Only allow selecting pieces of the current player's color
            if (piece != null && piece.getColor() == currentPlayer) {
                selectedPosition = clickedPosition;
                highlightSquare(file, 7-rank, SELECTED_COLOR);
                highlightLegalMoves(piece);
            }
            return;
        }

        // If the same square is clicked again, deselect it
        if (selectedPosition.equals(clickedPosition)) {
            clearSelection();
            return;
        }

        // Get the piece at selected position
        ChessPiece selectedPiece = gameController.getGameState().getBoard().getPieceAt(selectedPosition);

        // Variable to hold our move
        Move move;

        // Check for castling - this is critical for castling to work
        if (selectedPiece != null && selectedPiece.getType() == ChessPieceType.KING &&
                selectedPosition.getFile() == 'e') {

            int kingRank = selectedPosition.getRank();
            // Check if this is a castling move (king moving two squares)
            if (clickedPosition.getRank() == kingRank) {
                // Kingside castling
                if (clickedPosition.getFile() == 'g') {
                    move = com.example.demo1.special.Castling.createKingsideCastlingMove(selectedPiece.getColor());
                    System.out.println("Kingside castling detected");
                }
                // Queenside castling
                else if (clickedPosition.getFile() == 'c') {
                    move = com.example.demo1.special.Castling.createQueensideCastlingMove(selectedPiece.getColor());
                    System.out.println("Queenside castling detected");
                }
                else {
                    // Regular king move
                    move = new Move(selectedPosition, clickedPosition);
                }
            }
            else {
                // Regular king move (change in rank)
                move = new Move(selectedPosition, clickedPosition);
            }
        }
        else {
            // Regular move or pawn promotion
            move = new Move(selectedPosition, clickedPosition);
        }

        // Check for pawn promotion
        if (selectedPiece != null && selectedPiece.getType() == ChessPieceType.PAWN) {
            // Check if this is a promotion move (pawn reaching the last rank)
            int targetRank = clickedPosition.getRank();
            boolean isPromotion = (selectedPiece.getColor() == Color.WHITE && targetRank == 8) ||
                    (selectedPiece.getColor() == Color.BLACK && targetRank == 1);

            if (isPromotion) {
                ChessPieceType promotionType = showPromotionDialog(selectedPiece.getColor() == Color.WHITE);
                if (promotionType != null) {
                    move = new Move(selectedPosition, clickedPosition, promotionType);
                } else {
                    // User canceled promotion
                    clearSelection();
                    return;
                }
            }
        }

        // Try to make the move
        try {
            gameController.makeMove(move);
            // Board display will be updated by the UI callback

            // Clear selection
            clearSelection();
        } catch (IllegalMoveException e) {
            System.out.println("Invalid move: " + e.getMessage());
            clearSelection();
        }
    }

    /**
     * Highlight a square with a specific color
     * @param file The file (column) coordinate
     * @param rank The rank (row) coordinate
     * @param highlightColor The color to use for highlighting
     */
    private void highlightSquare(int file, int rank, javafx.scene.paint.Color highlightColor) {
        if (file < 0 || file >= 8 || rank < 0 || rank >= 8) {
            System.out.println("Invalid square coordinates for highlighting: " + file + "," + rank);
            return;
        }

        StackPane square = boardSquares[rank][file];

        // Add a more defined look to highlighted squares with border and inner shadow
        square.setBackground(new Background(new BackgroundFill(
                highlightColor,
                new CornerRadii(4), // Slightly rounded corners
                Insets.EMPTY
        )));

        // Add an inner shadow effect for more definition
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(javafx.scene.paint.Color.BLACK);
        innerShadow.setRadius(4);
        innerShadow.setChoke(0.2);

        // Add a slight glow for selected and capture squares
        if (highlightColor == SELECTED_COLOR || highlightColor == CAPTURE_COLOR) {
            Glow glow = new Glow(0.4);
            square.setEffect(glow);
        } else {
            square.setEffect(innerShadow);
        }

        // Add a border
        square.setBorder(new Border(new BorderStroke(
                javafx.scene.paint.Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(4),
                new BorderWidths(2)
        )));
    }

    /**
     * Highlight legal moves for a piece
     * @param piece The chess piece to show legal moves for
     */
    private void highlightLegalMoves(ChessPiece piece) {
        // Use the MoveValidator to get legal moves
        MoveValidator validator = new MoveValidator();
        List<Move> legalMoves = new ArrayList<>();

        try {
            // Find all possible moves for the piece
            GameState gameState = gameController.getGameState();
            Position piecePosition = selectedPosition; // We already know the position from the selection

            // Get all basic moves without check validation first
            List<Move> basicMoves = validator.generateBasicMoves(gameState, piece);

            // Filter out moves that would leave the king in check
            for (Move move : basicMoves) {
                // Make sure the move starts from our selected position
                if (!move.getSource().equals(piecePosition)) continue;

                // Simulate the move to check if it leaves the king in check
                GameState tempState;
                try {
                    tempState = (GameState) gameState.clone();
                    tempState.makeMove(move, true); // true means this is a simulation

                    if (!validator.isKingInCheck(tempState, piece.getColor())) {
                        legalMoves.add(move);
                    }
                } catch (CloneNotSupportedException e) {
                    System.err.println("Error cloning game state: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating legal moves: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("Legal moves: " + legalMoves.size());

        // Highlight each legal move
        for (Move move : legalMoves) {
            Position target = move.getTarget();

            // Calculate the display position (convert chess position to board indices)
            int displayFile = target.getFile() - 'a';
            int displayRank = 7 - (target.getRank() - 1);

            // Check if there's a piece at the target (capture)
            ChessPiece targetPiece = gameController.getGameState().getBoard().getPieceAt(target);

            if (targetPiece != null) {
                // Capture move - highlight in red
                highlightSquare(displayFile, displayRank, CAPTURE_COLOR);
            } else {
                // Normal move - highlight in green
                highlightSquare(displayFile, displayRank, LEGAL_MOVE_COLOR);
            }

            // Keep track of highlighted squares
            highlightedPositions.add(target);
        }
    }

    /**
     * Clear all highlights and selection
     */
    private void clearSelection() {
        selectedPosition = null;

        // Reset all squares to their original colors and remove effects
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                javafx.scene.paint.Color originalColor = (javafx.scene.paint.Color) boardSquares[rank][file].getProperties().get("originalColor");
                boardSquares[rank][file].setBackground(new Background(new BackgroundFill(
                        originalColor,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));
                boardSquares[rank][file].setEffect(null);
                boardSquares[rank][file].setBorder(null);
            }
        }

        highlightedPositions.clear();
    }

    /**
     * Show dialog for pawn promotion
     */
    private ChessPieceType showPromotionDialog(boolean isWhite) {
        Dialog<ChessPieceType> dialog = new Dialog<>();
        dialog.setTitle("Pawn Promotion");
        dialog.setHeaderText("Choose a piece to promote to:");

        // Create buttons for each piece type
        VBox options = new VBox(10);
        options.setPadding(new Insets(10));
        options.setSpacing(10);

        // Create the button for each piece type
        ChessPieceType[] pieceTypes = {
                ChessPieceType.QUEEN,
                ChessPieceType.ROOK,
                ChessPieceType.BISHOP,
                ChessPieceType.KNIGHT
        };

        String[] pieceNames = {"Queen", "Rook", "Bishop", "Knight"};

        for (int i = 0; i < pieceTypes.length; i++) {
            final ChessPieceType type = pieceTypes[i];
            Button button = new Button(pieceNames[i]);

            // Create a piece for the image
            ChessPiece piece = ChessPieceFactory.createPiece(type, isWhite ? Color.WHITE : Color.BLACK);
            ImageView pieceView = createPieceImageView(piece);
            pieceView.setFitWidth(40);
            pieceView.setFitHeight(40);

            button.setGraphic(pieceView);
            button.setPrefWidth(200);
            button.setContentDisplay(ContentDisplay.LEFT);

            // Style the buttons
            button.setStyle("-fx-background-color: #4D4D4D; -fx-text-fill: white; -fx-font-weight: bold;");

            button.setOnMouseEntered(e ->
                    button.setStyle("-fx-background-color: #666666; -fx-text-fill: white; -fx-font-weight: bold;")
            );

            button.setOnMouseExited(e ->
                    button.setStyle("-fx-background-color: #4D4D4D; -fx-text-fill: white; -fx-font-weight: bold;")
            );

            button.setOnAction(e -> {
                dialog.setResult(type);
                dialog.close();
            });

            options.getChildren().add(button);
        }

        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #3E3E3E;");
        dialogPane.lookup(".label").setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        dialogPane.setContent(options);

        // No default button types
        dialogPane.getButtonTypes().clear();

        return dialog.showAndWait().orElse(null);
    }

    /**
     * Show dialog when the game ends
     */
    private void showGameEndDialog() {
        GameState gameState = gameController.getGameState();

        String message;
        if (gameState.isCheckmate()) {
            Color winner = (gameState.getCurrentPlayer() == Color.WHITE) ? Color.BLACK : Color.WHITE;
            message = "Checkmate! " + winner + " wins!";
        } else if (gameState.isStalemate()) {
            message = "Stalemate! The game is a draw.";
        } else {
            message = "Game over!";
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Ended");
        alert.setContentText(message);

        // Add new game button
        ButtonType newGameButton = new ButtonType("New Game");
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(newGameButton, closeButton);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #3E3E3E;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #2E2E2E;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == newGameButton) {
                gameController.startNewGame();
                updateBoardDisplay();
            }
        });
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}