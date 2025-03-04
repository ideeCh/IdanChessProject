package com.example.idanchessgame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for the Move class.
 */
public class MoveTest {

    @Test
    public void testBasicMove() {
        Move move = new Move(12, 28, PieceType.WHITE_PAWN);
        assertEquals(12, move.getFromSquare());
        assertEquals(28, move.getToSquare());
        assertEquals(PieceType.WHITE_PAWN, move.getMovedPiece());
        assertNull(move.getCapturedPiece());
        assertFalse(move.isPromotion());
        assertNull(move.getPromotionPiece());
        assertFalse(move.isCastling());
        assertFalse(move.isEnPassant());
        assertFalse(move.isCapture());
    }

    @Test
    public void testCaptureMove() {
        Move move = new Move(28, 35, PieceType.WHITE_PAWN, PieceType.BLACK_PAWN, false, null, false, false);
        assertEquals(28, move.getFromSquare());
        assertEquals(35, move.getToSquare());
        assertEquals(PieceType.WHITE_PAWN, move.getMovedPiece());
        assertEquals(PieceType.BLACK_PAWN, move.getCapturedPiece());
        assertFalse(move.isPromotion());
        assertNull(move.getPromotionPiece());
        assertFalse(move.isCastling());
        assertFalse(move.isEnPassant());
        assertTrue(move.isCapture());
    }

    @Test
    public void testPromotionMove() {
        Move move = new Move(52, 60, PieceType.WHITE_PAWN, null, true, PieceType.WHITE_QUEEN, false, false);
        assertEquals(52, move.getFromSquare());
        assertEquals(60, move.getToSquare());
        assertEquals(PieceType.WHITE_PAWN, move.getMovedPiece());
        assertNull(move.getCapturedPiece());
        assertTrue(move.isPromotion());
        assertEquals(PieceType.WHITE_QUEEN, move.getPromotionPiece());
        assertFalse(move.isCastling());
        assertFalse(move.isEnPassant());
        assertFalse(move.isCapture());
    }

    @Test
    public void testPromotionWithCaptureMove() {
        Move move = new Move(54, 63, PieceType.WHITE_PAWN, PieceType.BLACK_ROOK, true, PieceType.WHITE_QUEEN, false, false);
        assertEquals(54, move.getFromSquare());
        assertEquals(63, move.getToSquare());
        assertEquals(PieceType.WHITE_PAWN, move.getMovedPiece());
        assertEquals(PieceType.BLACK_ROOK, move.getCapturedPiece());
        assertTrue(move.isPromotion());
        assertEquals(PieceType.WHITE_QUEEN, move.getPromotionPiece());
        assertFalse(move.isCastling());
        assertFalse(move.isEnPassant());
        assertTrue(move.isCapture());
    }

    @Test
    public void testCastlingMove() {
        // White kingside castling
        Move move = new Move(4, 6, PieceType.WHITE_KING, null, false, null, true, false);
        assertEquals(4, move.getFromSquare());
        assertEquals(6, move.getToSquare());
        assertEquals(PieceType.WHITE_KING, move.getMovedPiece());
        assertNull(move.getCapturedPiece());
        assertFalse(move.isPromotion());
        assertNull(move.getPromotionPiece());
        assertTrue(move.isCastling());
        assertFalse(move.isEnPassant());
        assertFalse(move.isCapture());
    }

    @Test
    public void testEnPassantMove() {
        // White en passant capture
        Move move = new Move(36, 45, PieceType.WHITE_PAWN, PieceType.BLACK_PAWN, false, null, false, true);
        assertEquals(36, move.getFromSquare());
        assertEquals(45, move.getToSquare());
        assertEquals(PieceType.WHITE_PAWN, move.getMovedPiece());
        assertEquals(PieceType.BLACK_PAWN, move.getCapturedPiece());
        assertFalse(move.isPromotion());
        assertNull(move.getPromotionPiece());
        assertFalse(move.isCastling());
        assertTrue(move.isEnPassant());
        assertTrue(move.isCapture());
    }

    @Test
    public void testSquareToAlgebraic() {
        assertEquals("a1", Move.squareToAlgebraic(0));
        assertEquals("h1", Move.squareToAlgebraic(7));
        assertEquals("a8", Move.squareToAlgebraic(56));
        assertEquals("h8", Move.squareToAlgebraic(63));
        assertEquals("e4", Move.squareToAlgebraic(28));
        assertEquals("d6", Move.squareToAlgebraic(43));
    }

    @Test
    public void testAlgebraicToSquare() {
        assertEquals(0, Move.algebraicToSquare("a1"));
        assertEquals(7, Move.algebraicToSquare("h1"));
        assertEquals(56, Move.algebraicToSquare("a8"));
        assertEquals(63, Move.algebraicToSquare("h8"));
        assertEquals(28, Move.algebraicToSquare("e4"));
        assertEquals(43, Move.algebraicToSquare("d6"));
    }

    @Test
    public void testInvalidAlgebraicNotation() {
        assertThrows(IllegalArgumentException.class, () -> Move.algebraicToSquare("i1"));
        assertThrows(IllegalArgumentException.class, () -> Move.algebraicToSquare("a9"));
        assertThrows(IllegalArgumentException.class, () -> Move.algebraicToSquare("a"));
        assertThrows(IllegalArgumentException.class, () -> Move.algebraicToSquare("a1b2"));
    }

    @Test
    public void testToString() {
        Move move = new Move(12, 28, PieceType.WHITE_PAWN);
        assertEquals("e2e4", move.toString());

        Move castlingMove = new Move(4, 6, PieceType.WHITE_KING, null, false, null, true, false);
        assertEquals("e1g1", castlingMove.toString());

        Move promotionMove = new Move(52, 60, PieceType.WHITE_PAWN, null, true, PieceType.WHITE_QUEEN, false, false);
        assertEquals("e7e8q", promotionMove.toString());
    }

    @Test
    public void testMoveExecution() {
        // Create a fresh board
        Board board = new Board();

        // Test a simple pawn move (e2-e4)
        Move pawnMove = new Move(12, 28, PieceType.WHITE_PAWN);
        pawnMove.execute(board);

        // Verify the pawn has moved
        assertNull(board.getPieceAt(12));
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(28));

        // Test undoing the move
        pawnMove.undo(board);

        // Verify the pawn has moved back
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(12));
        assertNull(board.getPieceAt(28));
    }

    @Test
    public void testCaptureExecution() {
        // Create a board with a capture scenario
        Board board = new Board();

        // First move a white pawn to a position where it can capture
        board.movePiece(12, 28, PieceType.WHITE_PAWN); // e2-e4

        // Place a black pawn at d5 (where it can be captured by the white pawn)
        board.movePiece(51, 35, PieceType.BLACK_PAWN); // d7-d5

        // Create the capture move: e4 captures d5
        Move captureMove = new Move(28, 35, PieceType.WHITE_PAWN, PieceType.BLACK_PAWN, false, null, false, false);

        // Execute the capture
        captureMove.execute(board);

        // Verify the capture
        assertNull(board.getPieceAt(28));
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(35));

        // Undo the capture
        captureMove.undo(board);

        // Verify the undo
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(28));
        assertEquals(PieceType.BLACK_PAWN, board.getPieceAt(35));
    }
}