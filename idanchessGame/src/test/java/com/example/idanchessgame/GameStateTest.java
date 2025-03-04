package com.example.idanchessgame;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for the GameState class.
 */
public class GameStateTest {

    @Test
    public void testInitialState() {
        GameState gameState = new GameState();

        // Check initial game status
        assertEquals(GameState.GameStatus.ACTIVE, gameState.getStatus());
        assertTrue(gameState.isWhiteToMove());
        assertFalse(gameState.isInCheck());
        assertFalse(gameState.isGameOver());
        assertTrue(gameState.getMoveHistory().isEmpty());
    }

    @Test
    public void testMakeMove() {
        GameState gameState = new GameState();

        // Make a legal move
        boolean moveMade = gameState.makeMove("e2", "e4", null);

        // Check that the move was successful
        assertTrue(moveMade);
        assertFalse(gameState.isWhiteToMove());
        assertEquals(1, gameState.getMoveHistory().size());
        assertEquals(GameState.GameStatus.ACTIVE, gameState.getStatus());

        // Try to make an illegal move
        moveMade = gameState.makeMove("a7", "a6", null);

        // Check that the illegal move was rejected
        assertFalse(moveMade);
        assertFalse(gameState.isWhiteToMove());
        assertEquals(1, gameState.getMoveHistory().size());
    }

    @Test
    public void testUndoMove() {
        GameState gameState = new GameState();

        // Make a move
        gameState.makeMove("e2", "e4", null);

        // Undo the move
        boolean undoSuccessful = gameState.undoMove();

        // Check that the move was undone
        assertTrue(undoSuccessful);
        assertTrue(gameState.isWhiteToMove());
        assertTrue(gameState.getMoveHistory().isEmpty());

        // Try to undo again (should fail)
        undoSuccessful = gameState.undoMove();
        assertFalse(undoSuccessful);
    }

    @Test
    public void testCheckDetection() {
        GameState gameState = new GameState();

        // Play to a check position (scholar's mate setup)
        gameState.makeMove("e2", "e4", null);
        gameState.makeMove("e7", "e5", null);
        gameState.makeMove("d1", "h5", null);
        gameState.makeMove("b8", "c6", null);
        gameState.makeMove("f1", "c4", null);
        gameState.makeMove("g8", "f6", null);

        // Queen delivers check
        gameState.makeMove("h5", "f7", null);

        // Check that black is in check
        assertTrue(gameState.isInCheck());
        assertEquals(GameState.GameStatus.ACTIVE, gameState.getStatus());
    }

    @Test
    public void testCheckmateDetection() {
        GameState gameState = new GameState();

        // Play to a checkmate position (fool's mate)
        gameState.makeMove("f2", "f3", null);
        gameState.makeMove("e7", "e5", null);
        gameState.makeMove("g2", "g4", null);
        gameState.makeMove("d8", "h4", null);

        // Check that white is in checkmate
        assertTrue(gameState.isInCheck());
        assertTrue(gameState.isGameOver());
        assertEquals(GameState.GameStatus.BLACK_WINS, gameState.getStatus());
    }
    @Test
    public void testStalemateDetection() {
        GameState gameState = new GameState();

        // Create a new game state with a stalemate position
        Board board = gameState.getBoard();

        // Clear the board
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }

        // Set up a stalemate position with just kings and a queen
        board.addPiece(0, PieceType.BLACK_KING);  // a1
        board.addPiece(2, PieceType.WHITE_KING);  // c1
        board.addPiece(9, PieceType.WHITE_QUEEN); // b2

        // Set black to move for stalemate
        board.setWhiteToMove(false);

        // Make a move to update the move history
        board.makeMove(new Move(9, 8, PieceType.WHITE_QUEEN));

        // Update the game status
        gameState.updateGameStatus();

        // Check that the game is a draw due to stalemate
        assertFalse(gameState.isInCheck(), "King should not be in check");
        assertTrue(gameState.isGameOver(), "Game should be over");
        assertEquals(GameState.GameStatus.DRAW, gameState.getStatus(), "Status should be DRAW");
    }

    @Test
    public void testInsufficientMaterialDraw() {
        GameState gameState = new GameState();

        // Create a new game state with insufficient material
        Board board = gameState.getBoard();

        // Clear the board
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }

        // Set up a king vs king position
        board.addPiece(4, PieceType.WHITE_KING);
        board.addPiece(60, PieceType.BLACK_KING);

        // Update the game status
        gameState.updateGameStatus();

        // Check that the game is a draw due to insufficient material
        assertFalse(gameState.isInCheck());
        assertTrue(gameState.isGameOver());
        assertEquals(GameState.GameStatus.DRAW, gameState.getStatus());
    }

    @Test
    public void testFiftyMoveRule() {
        GameState gameState = new GameState();

        // Create a new game state
        Board board = gameState.getBoard();

        // Clear the board
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }

        // Add just kings and one knight (to avoid immediate insufficient material draw)
        board.addPiece(4, PieceType.WHITE_KING);
        board.addPiece(60, PieceType.BLACK_KING);
        board.addPiece(1, PieceType.WHITE_KNIGHT);

        // Make 50 moves (100 half-moves) without captures or pawn moves
        for (int i = 0; i < 50; i++) {
            // White king moves back and forth
            board.makeMove(new Move(4, 5, PieceType.WHITE_KING));
            board.makeMove(new Move(60, 59, PieceType.BLACK_KING));
            board.makeMove(new Move(5, 4, PieceType.WHITE_KING));
            board.makeMove(new Move(59, 60, PieceType.BLACK_KING));
        }

        // Update the game status
        gameState.updateGameStatus();

        // Check that the game is a draw due to the 50-move rule
        assertTrue(gameState.isGameOver());
        assertEquals(GameState.GameStatus.DRAW, gameState.getStatus());
    }

    @Test
    public void testPawnPromotion() {
        GameState gameState = new GameState();

        // Setup a position where white can promote a pawn
        Board board = gameState.getBoard();

        // Place a white pawn on the 7th rank
        board.removePiece(12, PieceType.WHITE_PAWN);
        board.addPiece(52, PieceType.WHITE_PAWN);

        // Promote to a queen
        boolean moveMade = gameState.makeMove("e7", "e8", PieceType.WHITE_QUEEN);

        // Check that the promotion was successful
        assertTrue(moveMade);
        assertEquals(PieceType.WHITE_QUEEN, board.getPieceAt(60));
        assertFalse(gameState.isWhiteToMove());

        // Undo the promotion
        gameState.undoMove();

        // Check that the promotion was undone
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(52));
        assertNull(board.getPieceAt(60));
        assertTrue(gameState.isWhiteToMove());

        // Try again with implicit promotion to queen
        moveMade = gameState.makeMove("e7", "e8", null);

        // Check that the default promotion to queen was successful
        assertTrue(moveMade);
        assertEquals(PieceType.WHITE_QUEEN, board.getPieceAt(60));
    }
}