package com.example.demo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Add imports for your specific classes
import com.example.demo1.core.*;
import com.example.demo1.special.*;

import java.util.List;

/**
 * Comprehensive test suite for chess game functionality
 */
public class ChessGameTest {

    private GameController gameController;
    private GameState gameState;
    private Board board;
    private MoveValidator moveValidator;

    @BeforeEach
    void setUp() {
        gameController = new GameController();
        gameController.initialize();
        gameState = gameController.getGameState();
        board = gameState.getBoard();
        moveValidator = new MoveValidator();
    }

    @Nested
    @DisplayName("Stalemate Tests")
    class StalemateTests {

        @Test
        @DisplayName("King vs King and Queen stalemate position")
        void testKingVsKingQueenStalemate() {
            // Clear board for custom position
            clearBoard();

            // Set up a classic stalemate position
            // Black king at a8, White king at c6, White queen at b6
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('a', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('c', 6));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('b', 6));

            // Set black to move
            setPlayerToMove(Color.BLACK);

            // Validate that this is indeed a stalemate
            assertFalse(moveValidator.isKingInCheck(gameState, Color.BLACK), "King should not be in check");
            assertFalse(gameState.isCheckmate(), "Should not be checkmate");
            assertTrue(gameState.isStalemate(), "Should be stalemate");
        }

        @Test
        @DisplayName("King blocked by pawns stalemate")
        void testKingBlockedByPawnsStalemate() {
            clearBoard();

            // Black king at h8, surrounded by its own pawns at g7, h7 and white pawn at g8
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('h', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('g', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('h', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('g', 8));

            // Add white pieces that control remaining squares
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('f', 6));

            setPlayerToMove(Color.BLACK);

            assertFalse(moveValidator.isKingInCheck(gameState, Color.BLACK), "King should not be in check");
            assertFalse(gameState.isCheckmate(), "Should not be checkmate");
            assertTrue(gameState.isStalemate(), "Should be stalemate");
        }
    }

    @Nested
    @DisplayName("50-Move Rule Tests")
    class FiftyMoveRuleTests {

        @Test
        @DisplayName("50-move rule should be triggered after 100 half-moves without pawn move or capture")
        void test50MoveRule() {
            // Start with a simplified position
            clearBoard();
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('a', 1));

            // Set half-move clock to 99
            gameState.setHalfMoveClock(99);

            // Make one more move
            Move move = new Move(new Position('a', 1), new Position('a', 2));
            try {
                gameController.makeMove(move);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Verify 50-move rule is detected
            assertTrue(gameState.isFiftyMoveRuleDraw(), "50-move rule should be triggered");
            assertTrue(gameState.isDraw(), "Game should be a draw");
        }

        @Test
        @DisplayName("Pawn move should reset 50-move counter")
        void testPawnMoveResets50MoveCounter() {
            clearBoard();
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('d', 2));

            // Set half-move clock to 10
            gameState.setHalfMoveClock(10);

            // Make a pawn move
            Move move = new Move(new Position('d', 2), new Position('d', 4));
            try {
                gameController.makeMove(move);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Verify counter is reset
            assertEquals(0, gameState.getHalfMoveClock(), "Half-move clock should be reset to 0");
        }

        @Test
        @DisplayName("Capture should reset 50-move counter")
        void testCaptureResets50MoveCounter() {
            clearBoard();
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('d', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.BISHOP, Color.BLACK), new Position('d', 8));

            // Set half-move clock to 10
            gameState.setHalfMoveClock(10);

            // Make a capture move
            Move move = new Move(new Position('d', 1), new Position('d', 8));
            try {
                gameController.makeMove(move);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Verify counter is reset
            assertEquals(0, gameState.getHalfMoveClock(), "Half-move clock should be reset to 0");
        }
    }

    @Nested
    @DisplayName("Castling Tests")
    class CastlingTests {

        @Test
        @DisplayName("Valid kingside castling")
        void testValidKingsideCastling() {
            clearBoard();

            // Setup castling position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('h', 1));

            // Ensure pieces haven't moved (get fresh pieces)
            ChessPiece king = board.getPieceAt(new Position('e', 1));
            ChessPiece rook = board.getPieceAt(new Position('h', 1));
            king.setHasMoved(false);
            rook.setHasMoved(false);

            // Create castling move
            Move castlingMove = com.example.demo1.special.Castling.createKingsideCastlingMove(Color.WHITE);

            // Verify move is valid
            assertTrue(moveValidator.isValidMove(gameState, castlingMove), "Kingside castling should be valid");

            // Execute the move
            try {
                gameController.makeMove(castlingMove);
            } catch (IllegalMoveException e) {
                fail("Legal castling move should not throw exception: " + e.getMessage());
            }

            // Verify the king and rook have moved to their correct positions
            assertNull(board.getPieceAt(new Position('e', 1)), "King should no longer be at e1");
            assertNull(board.getPieceAt(new Position('h', 1)), "Rook should no longer be at h1");
            assertNotNull(board.getPieceAt(new Position('g', 1)), "King should be at g1");
            assertNotNull(board.getPieceAt(new Position('f', 1)), "Rook should be at f1");
        }

        @Test
        @DisplayName("Valid queenside castling")
        void testValidQueensideCastling() {
            clearBoard();

            // Setup castling position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('a', 1));

            // Ensure pieces haven't moved
            ChessPiece king = board.getPieceAt(new Position('e', 1));
            ChessPiece rook = board.getPieceAt(new Position('a', 1));
            king.setHasMoved(false);
            rook.setHasMoved(false);

            // Create castling move
            Move castlingMove = com.example.demo1.special.Castling.createQueensideCastlingMove(Color.WHITE);

            // Verify move is valid
            assertTrue(moveValidator.isValidMove(gameState, castlingMove), "Queenside castling should be valid");

            // Execute the move
            try {
                gameController.makeMove(castlingMove);
            } catch (IllegalMoveException e) {
                fail("Legal castling move should not throw exception: " + e.getMessage());
            }

            // Verify the king and rook have moved to their correct positions
            assertNull(board.getPieceAt(new Position('e', 1)), "King should no longer be at e1");
            assertNull(board.getPieceAt(new Position('a', 1)), "Rook should no longer be at a1");
            assertNotNull(board.getPieceAt(new Position('c', 1)), "King should be at c1");
            assertNotNull(board.getPieceAt(new Position('d', 1)), "Rook should be at d1");
        }

        @Test
        @DisplayName("Castling through check is invalid")
        void testCastlingThroughCheckInvalid() {
            clearBoard();

            // Setup position where king would castle through check
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('h', 1));

            // Place attacking black rook that puts f1 under attack
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.BLACK), new Position('f', 8));

            // Ensure pieces haven't moved
            ChessPiece king = board.getPieceAt(new Position('e', 1));
            ChessPiece rook = board.getPieceAt(new Position('h', 1));
            king.setHasMoved(false);
            rook.setHasMoved(false);

            // Create castling move
            Move castlingMove = com.example.demo1.special.Castling.createKingsideCastlingMove(Color.WHITE);

            // Verify move is invalid
            assertFalse(moveValidator.isValidMove(gameState, castlingMove),
                    "Kingside castling should be invalid when king passes through check");
        }

        @Test
        @DisplayName("Castling when king is in check is invalid")
        void testCastlingInCheckInvalid() {
            clearBoard();

            // Setup position where king is in check
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('h', 1));

            // Place attacking black rook
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.BLACK), new Position('e', 8));

            // Ensure pieces haven't moved
            ChessPiece king = board.getPieceAt(new Position('e', 1));
            ChessPiece rook = board.getPieceAt(new Position('h', 1));
            king.setHasMoved(false);
            rook.setHasMoved(false);

            // Create castling move
            Move castlingMove = com.example.demo1.special.Castling.createKingsideCastlingMove(Color.WHITE);

            // Verify move is invalid
            assertFalse(moveValidator.isValidMove(gameState, castlingMove),
                    "Castling should be invalid when king is in check");
        }

        @Test
        @DisplayName("Castling when king has moved is invalid")
        void testCastlingAfterKingMovedInvalid() {
            clearBoard();

            // Setup castling position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('h', 1));

            // Set king as having moved
            ChessPiece king = board.getPieceAt(new Position('e', 1));
            ChessPiece rook = board.getPieceAt(new Position('h', 1));
            king.setHasMoved(true);
            rook.setHasMoved(false);

            // Create castling move
            Move castlingMove = com.example.demo1.special.Castling.createKingsideCastlingMove(Color.WHITE);

            // Verify move is invalid
            assertFalse(moveValidator.isValidMove(gameState, castlingMove),
                    "Castling should be invalid when king has moved");
        }
    }

    @Nested
    @DisplayName("En Passant Tests")
    class EnPassantTests {

        @Test
        @DisplayName("Valid en passant capture")
        void testValidEnPassantCapture() {
            clearBoard();

            // Setup en passant position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('e', 5));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('d', 7));

            // Move black pawn two squares to set up en passant
            Move twoSquareMove = new Move(new Position('d', 7), new Position('d', 5));
            try {
                gameController.makeMove(twoSquareMove);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Verify en passant target is set
            assertNotNull(gameState.getEnPassantTarget(), "En passant target should be set");
            assertEquals(new Position('d', 6), gameState.getEnPassantTarget(),
                    "En passant target should be d6");

            // Create en passant capture move
            Move enPassantMove = new Move(new Position('e', 5), new Position('d', 6));

            // Execute the en passant capture
            try {
                gameController.makeMove(enPassantMove);
            } catch (IllegalMoveException e) {
                fail("Legal en passant move should not throw exception: " + e.getMessage());
            }

            // Verify capture occurred correctly
            assertNull(board.getPieceAt(new Position('e', 5)), "White pawn should no longer be at e5");
            assertNull(board.getPieceAt(new Position('d', 5)), "Black pawn should have been captured");
            assertNotNull(board.getPieceAt(new Position('d', 6)), "White pawn should be at d6");
        }

        @Test
        @DisplayName("En passant opportunity expires after one move")
        void testEnPassantExpiresAfterOneMove() {
            clearBoard();

            // Setup en passant position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('e', 5));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('d', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));

            // Move black pawn two squares to set up en passant
            Move twoSquareMove = new Move(new Position('d', 7), new Position('d', 5));
            try {
                gameController.makeMove(twoSquareMove);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Make a different move instead of capturing en passant
            Move differentMove = new Move(new Position('e', 1), new Position('e', 2));
            try {
                gameController.makeMove(differentMove);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Make another move
            Move anotherMove = new Move(new Position('e', 8), new Position('e', 7));
            try {
                gameController.makeMove(anotherMove);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Now try to capture en passant (should fail)
            Move enPassantMove = new Move(new Position('e', 5), new Position('d', 6));

            // En passant target should be null
            assertNull(gameState.getEnPassantTarget(), "En passant target should be null after one move");

            // Move should be invalid
            assertFalse(moveValidator.isValidMove(gameState, enPassantMove),
                    "En passant should be invalid after opportunity expires");
        }
    }

    @Nested
    @DisplayName("Check and Checkmate Tests")
    class CheckAndCheckmateTests {

        @Test
        @DisplayName("Simple check detection")
        void testSimpleCheckDetection() {
            clearBoard();

            // Setup a simple check position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('e', 7));

            setPlayerToMove(Color.BLACK);

            // Verify check is detected
            assertTrue(moveValidator.isKingInCheck(gameState, Color.BLACK), "Black king should be in check");
            assertTrue(gameState.isCheck(), "Check state should be set");
            assertFalse(gameState.isCheckmate(), "Should not be checkmate yet");
        }

        @Test
        @DisplayName("Scholar's Mate checkmate pattern")
        void testScholarsMateCheckmate() {
            clearBoard();

            // Setup Scholar's Mate position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('d', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('e', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('f', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('h', 5));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.BISHOP, Color.WHITE), new Position('c', 4));

            setPlayerToMove(Color.BLACK);

            // Verify checkmate is detected
            assertTrue(moveValidator.isKingInCheck(gameState, Color.BLACK), "Black king should be in check");
            assertTrue(gameState.isCheck(), "Check state should be set");
            assertTrue(gameState.isCheckmate(), "Should be checkmate");
            assertFalse(gameState.isStalemate(), "Should not be stalemate");
        }

        @Test
        @DisplayName("Back rank checkmate pattern")
        void testBackRankCheckmate() {
            clearBoard();

            // Setup back rank checkmate
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('d', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('e', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.BLACK), new Position('f', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.WHITE), new Position('a', 8));

            setPlayerToMove(Color.BLACK);

            // Verify checkmate is detected
            assertTrue(moveValidator.isKingInCheck(gameState, Color.BLACK), "Black king should be in check");
            assertTrue(gameState.isCheck(), "Check state should be set");
            assertTrue(gameState.isCheckmate(), "Should be checkmate");
            assertFalse(gameState.isStalemate(), "Should not be stalemate");
        }

        @Test
        @DisplayName("King can move out of check")
        void testKingCanMoveOutOfCheck() {
            clearBoard();

            // Setup position where king can escape check
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('e', 7));

            setPlayerToMove(Color.BLACK);

            // Verify check is detected but not checkmate
            assertTrue(moveValidator.isKingInCheck(gameState, Color.BLACK), "Black king should be in check");
            assertTrue(gameState.isCheck(), "Check state should be set");
            assertFalse(gameState.isCheckmate(), "Should not be checkmate");

            // Verify king can move to d8
            Move escapeMove = new Move(new Position('e', 8), new Position('d', 8));
            assertTrue(moveValidator.isValidMove(gameState, escapeMove),
                    "King should be able to escape to d8");
        }

        @Test
        @DisplayName("Piece can block check")
        void testPieceCanBlockCheck() {
            clearBoard();

            // Setup position where a piece can block check
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('e', 4));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KNIGHT, Color.BLACK), new Position('c', 6));

            setPlayerToMove(Color.BLACK);

            // Verify check is detected but not checkmate
            assertTrue(moveValidator.isKingInCheck(gameState, Color.BLACK), "Black king should be in check");
            assertTrue(gameState.isCheck(), "Check state should be set");
            assertFalse(gameState.isCheckmate(), "Should not be checkmate");

            // Verify knight can block check
            Move blockMove = new Move(new Position('c', 6), new Position('e', 7));
            assertTrue(moveValidator.isValidMove(gameState, blockMove),
                    "Knight should be able to block check by moving to e7");
        }

        @Test
        @DisplayName("Piece can capture checking piece")
        void testPieceCanCaptureChecker() {
            clearBoard();

            // Setup position where a piece can capture the checking piece
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.QUEEN, Color.WHITE), new Position('e', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.BLACK), new Position('a', 7));

            setPlayerToMove(Color.BLACK);

            // Verify check is detected but not checkmate
            assertTrue(moveValidator.isKingInCheck(gameState, Color.BLACK), "Black king should be in check");
            assertTrue(gameState.isCheck(), "Check state should be set");
            assertFalse(gameState.isCheckmate(), "Should not be checkmate");

            // Verify rook can capture queen
            Move captureMove = new Move(new Position('a', 7), new Position('e', 7));
            assertTrue(moveValidator.isValidMove(gameState, captureMove),
                    "Rook should be able to capture the checking queen");
        }
    }

    @Nested
    @DisplayName("Pawn Promotion Tests")
    class PawnPromotionTests {

        @Test
        @DisplayName("Pawn promotion to queen")
        void testPawnPromotionToQueen() {
            clearBoard();

            // Setup promotion position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('e', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));

            // Create promotion move
            Move promotionMove = new Move(new Position('e', 7), new Position('e', 8), ChessPieceType.QUEEN);

            // Execute the move
            try {
                gameController.makeMove(promotionMove);
            } catch (IllegalMoveException e) {
                fail("Legal promotion move should not throw exception: " + e.getMessage());
            }

            // Verify promotion occurred
            ChessPiece promotedPiece = board.getPieceAt(new Position('e', 8));
            assertNotNull(promotedPiece, "A piece should exist at e8");
            assertEquals(ChessPieceType.QUEEN, promotedPiece.getType(), "Piece should be a queen");
            assertEquals(Color.WHITE, promotedPiece.getColor(), "Piece should be white");
        }

        @Test
        @DisplayName("Pawn promotion to knight")
        void testPawnPromotionToKnight() {
            clearBoard();

            // Setup promotion position
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('e', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('e', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('e', 8));

            // Move black king out of the way
            Move kingMove = new Move(new Position('e', 8), new Position('d', 8));
            try {
                gameController.makeMove(kingMove);
            } catch (IllegalMoveException e) {
                fail("Legal move should not throw exception: " + e.getMessage());
            }

            // Create promotion move
            Move promotionMove = new Move(new Position('e', 7), new Position('e', 8), ChessPieceType.KNIGHT);

            // Execute the move
            try {
                gameController.makeMove(promotionMove);
            } catch (IllegalMoveException e) {
                fail("Legal promotion move should not throw exception: " + e.getMessage());
            }

            // Verify promotion occurred
            ChessPiece promotedPiece = board.getPieceAt(new Position('e', 8));
            assertNotNull(promotedPiece, "A piece should exist at e8");
            assertEquals(ChessPieceType.KNIGHT, promotedPiece.getType(), "Piece should be a knight");
            assertEquals(Color.WHITE, promotedPiece.getColor(), "Piece should be white");
        }

        @Test
        @DisplayName("Pawn promotion with capture")
        void testPawnPromotionWithCapture() {
            clearBoard();

            // Setup promotion position with capture
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.PAWN, Color.WHITE), new Position('d', 7));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.ROOK, Color.BLACK), new Position('e', 8));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.WHITE), new Position('a', 1));
            placePiece(ChessPieceFactory.createPiece(ChessPieceType.KING, Color.BLACK), new Position('h', 8));

            // Create promotion move with capture
            Move promotionMove = new Move(new Position('d', 7), new Position('e', 8), ChessPieceType.QUEEN);

            // Execute the move
            try {
                gameController.makeMove(promotionMove);
            } catch (IllegalMoveException e) {
                fail("Legal promotion move with capture should not throw exception: " + e.getMessage());
            }

            // Verify promotion and capture occurred
            ChessPiece promotedPiece = board.getPieceAt(new Position('e', 8));
            assertNotNull(promotedPiece, "A piece should exist at e8");
            assertEquals(ChessPieceType.QUEEN, promotedPiece.getType(), "Piece should be a queen");
            assertEquals(Color.WHITE, promotedPiece.getColor(), "Piece should be white");

            // Verify black rook was captured
            List<ChessPiece> capturedPieces = gameState.getCapturedPieces(Color.WHITE);
            boolean rookCaptured = capturedPieces.stream()
                    .anyMatch(p -> p.getType() == ChessPieceType.ROOK && p.getColor() == Color.BLACK);
            assertTrue(rookCaptured, "Black rook should have been captured");
        }
    }

    // Helper methods for test setup

    private void clearBoard() {
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                board.setPieceAt(new Position(file, rank), null);
            }
        }
    }

    private void placePiece(ChessPiece piece, Position position) {
        board.setPieceAt(position, piece);
    }

    private void setPlayerToMove(Color color) {
        // This would depend on how your game state manages the current player
        // For example:
        while (gameState.getCurrentPlayer() != color) {
            gameState.switchPlayer();
        }
    }
}