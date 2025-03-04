package com.example.idanchessgame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the Board class.
 */
public class BoardTest {

    @Test
    public void testInitialBoardSetup() {
        Board board = new Board();

        // Test white pawns on rank 2
        for (int file = 0; file < 8; file++) {
            assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(8 + file));
        }

        // Test white pieces on rank 1
        assertEquals(PieceType.WHITE_ROOK, board.getPieceAt(0));
        assertEquals(PieceType.WHITE_KNIGHT, board.getPieceAt(1));
        assertEquals(PieceType.WHITE_BISHOP, board.getPieceAt(2));
        assertEquals(PieceType.WHITE_QUEEN, board.getPieceAt(3));
        assertEquals(PieceType.WHITE_KING, board.getPieceAt(4));
        assertEquals(PieceType.WHITE_BISHOP, board.getPieceAt(5));
        assertEquals(PieceType.WHITE_KNIGHT, board.getPieceAt(6));
        assertEquals(PieceType.WHITE_ROOK, board.getPieceAt(7));

        // Test black pawns on rank 7
        for (int file = 0; file < 8; file++) {
            assertEquals(PieceType.BLACK_PAWN, board.getPieceAt(48 + file));
        }

        // Test black pieces on rank 8
        assertEquals(PieceType.BLACK_ROOK, board.getPieceAt(56));
        assertEquals(PieceType.BLACK_KNIGHT, board.getPieceAt(57));
        assertEquals(PieceType.BLACK_BISHOP, board.getPieceAt(58));
        assertEquals(PieceType.BLACK_QUEEN, board.getPieceAt(59));
        assertEquals(PieceType.BLACK_KING, board.getPieceAt(60));
        assertEquals(PieceType.BLACK_BISHOP, board.getPieceAt(61));
        assertEquals(PieceType.BLACK_KNIGHT, board.getPieceAt(62));
        assertEquals(PieceType.BLACK_ROOK, board.getPieceAt(63));

        // Test empty squares on ranks 3-6
        for (int square = 16; square < 48; square++) {
            assertNull(board.getPieceAt(square));
        }

        // Test initial game state
        assertTrue(board.isWhiteToMove());
        assertEquals(-1, board.getEnPassantSquare());

        // Test castling rights
        assertTrue(board.getCastlingRight(CastlingRight.WHITE_KINGSIDE));
        assertTrue(board.getCastlingRight(CastlingRight.WHITE_QUEENSIDE));
        assertTrue(board.getCastlingRight(CastlingRight.BLACK_KINGSIDE));
        assertTrue(board.getCastlingRight(CastlingRight.BLACK_QUEENSIDE));
    }

    @Test
    public void testMovePiece() {
        Board board = new Board();

        // Move the white e-pawn
        board.movePiece(12, 28, PieceType.WHITE_PAWN);

        // Check that the pawn moved correctly
        assertNull(board.getPieceAt(12));
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(28));

        // Move the black e-pawn
        board.movePiece(52, 36, PieceType.BLACK_PAWN);

        // Check that the pawn moved correctly
        assertNull(board.getPieceAt(52));
        assertEquals(PieceType.BLACK_PAWN, board.getPieceAt(36));
    }

    @Test
    public void testMakeAndUndoMove() {
        Board board = new Board();

        // Create a move: e2-e4
        Move move = new Move(12, 28, PieceType.WHITE_PAWN);

        // Make the move
        board.makeMove(move);

        // Check that the move was made
        assertNull(board.getPieceAt(12));
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(28));
        assertFalse(board.isWhiteToMove());

        // Undo the move
        board.undoMove();

        // Check that the move was undone
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(12));
        assertNull(board.getPieceAt(28));
        assertTrue(board.isWhiteToMove());
    }

    @Test
    public void testCastlingMove() {
        Board board = new Board();

        // Clear pieces between king and rook
        board.removePiece(1, PieceType.WHITE_KNIGHT);
        board.removePiece(2, PieceType.WHITE_BISHOP);
        board.removePiece(3, PieceType.WHITE_QUEEN);

        // Create a queenside castling move
        Move castlingMove = new Move(4, 2, PieceType.WHITE_KING, null, false, null, true, false);

        // Make the castling move
        board.makeMove(castlingMove);

        // Check that the king and rook moved correctly
        assertNull(board.getPieceAt(4));
        assertEquals(PieceType.WHITE_KING, board.getPieceAt(2));
        assertNull(board.getPieceAt(0));
        assertEquals(PieceType.WHITE_ROOK, board.getPieceAt(3));

        // Undo the castling move
        board.undoMove();

        // Check that the king and rook returned to their starting positions
        assertEquals(PieceType.WHITE_KING, board.getPieceAt(4));
        assertNull(board.getPieceAt(2));
        assertEquals(PieceType.WHITE_ROOK, board.getPieceAt(0));
        assertNull(board.getPieceAt(3));
    }

    @Test
    public void testEnPassantMove() {
        Board board = new Board();

        // Move white e-pawn to e5
        board.makeMove(new Move(12, 28, PieceType.WHITE_PAWN));
        board.makeMove(new Move(50, 34, PieceType.BLACK_PAWN)); // g7-g5

        // Move white e-pawn to e6
        board.makeMove(new Move(28, 44, PieceType.WHITE_PAWN));

        // Move black d-pawn two squares forward (enabling en passant)
        Move blackPawnMove = new Move(51, 35, PieceType.BLACK_PAWN);
        board.makeMove(blackPawnMove);

        // Check that en passant square is set correctly
        assertEquals(43, board.getEnPassantSquare());

        // Create an en passant capture move
        Move enPassantMove = new Move(44, 43, PieceType.WHITE_PAWN, PieceType.BLACK_PAWN, false, null, false, true);

        // Make the en passant capture
        board.makeMove(enPassantMove);

        // Check that the en passant capture was performed correctly
        assertNull(board.getPieceAt(44));
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(43));
        assertNull(board.getPieceAt(35));

        // Undo the en passant capture
        board.undoMove();

        // Check that the en passant capture was undone correctly
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(44));
        assertNull(board.getPieceAt(43));
        assertEquals(PieceType.BLACK_PAWN, board.getPieceAt(35));
    }

    @Test
    public void testPawnPromotion() {
        Board board = new Board();

        // Place a white pawn on the 7th rank
        board.removePiece(12, PieceType.WHITE_PAWN);
        board.addPiece(52, PieceType.WHITE_PAWN);

        // Create a promotion move
        Move promotionMove = new Move(52, 60, PieceType.WHITE_PAWN, null, true, PieceType.WHITE_QUEEN, false, false);

        // Make the promotion move
        board.makeMove(promotionMove);

        // Check that the pawn was promoted correctly
        assertNull(board.getPieceAt(52));
        assertEquals(PieceType.WHITE_QUEEN, board.getPieceAt(60));

        // Undo the promotion move
        board.undoMove();

        // Check that the promotion was undone correctly
        assertEquals(PieceType.WHITE_PAWN, board.getPieceAt(52));
        assertNull(board.getPieceAt(60));
    }

    @Test
    public void testCastlingRights() {
        Board board = new Board();

        // Move white king, should lose both castling rights
        board.makeMove(new Move(4, 5, PieceType.WHITE_KING));
        board.makeMove(new Move(50, 34, PieceType.BLACK_PAWN)); // Some black move
        board.makeMove(new Move(5, 4, PieceType.WHITE_KING)); // Move king back

        // Check that castling rights are lost
        assertFalse(board.getCastlingRight(CastlingRight.WHITE_KINGSIDE));
        assertFalse(board.getCastlingRight(CastlingRight.WHITE_QUEENSIDE));
        assertTrue(board.getCastlingRight(CastlingRight.BLACK_KINGSIDE));
        assertTrue(board.getCastlingRight(CastlingRight.BLACK_QUEENSIDE));

        // Move black h-rook, should lose kingside castling right
        board.makeMove(new Move(12, 28, PieceType.WHITE_PAWN)); // Some white move
        board.makeMove(new Move(63, 55, PieceType.BLACK_ROOK));

        // Check that kingside castling right is lost
        assertFalse(board.getCastlingRight(CastlingRight.WHITE_KINGSIDE));
        assertFalse(board.getCastlingRight(CastlingRight.WHITE_QUEENSIDE));
        assertFalse(board.getCastlingRight(CastlingRight.BLACK_KINGSIDE)); // Moving the rook should lose kingside castling rights
        assertTrue(board.getCastlingRight(CastlingRight.BLACK_QUEENSIDE));
    }

    @Test
    public void testGetLegalMoves() {
        Board board = new Board();

        // In the initial position, each player should have 20 legal moves
        List<Move> legalMoves = board.getLegalMoves();
        assertEquals(20, legalMoves.size());

        // Make a move and check legal moves again
        board.makeMove(new Move(12, 28, PieceType.WHITE_PAWN)); // e2-e4
        legalMoves = board.getLegalMoves();
        assertEquals(20, legalMoves.size()); // Black should also have 20 legal moves
    }

    @Test
    @DisplayName("Test basic check detection with direct attack")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testCheckDetection() {
        Board board = new Board();

        // Clear the board for a clean test
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }
        
        // Set up a simple position: Black king on e8, White queen on e7 (direct check)
        board.addPiece(60, PieceType.BLACK_KING); // e8
        board.addPiece(52, PieceType.WHITE_QUEEN); // e7
        
        // Print debug info
        System.out.println("*** Basic Check Test ***");
        System.out.println("Black king position: " + board.getKingPosition(false) + " (e8)");
        System.out.println("White queen position: 52 (e7)");
        
        // Debug attacks
        System.out.println("Is e8 (60) attacked by white: " + board.isSquareAttacked(60, true));
        board.print();
        
        // Check that black king is in check
        assertTrue(board.isKingInCheck(false), "Black king should be in check from white queen");
        
        // Try another position: Black king on e8, White rook on e2, with a black pawn on e4 blocking
        board.removePiece(52, PieceType.WHITE_QUEEN);
        board.addPiece(12, PieceType.WHITE_ROOK);  // e2
        board.addPiece(28, PieceType.BLACK_PAWN);  // e4 - blocking piece
        
        System.out.println("*** Not in Check Test with Blocking Piece ***");
        System.out.println("Black king position: " + board.getKingPosition(false) + " (e8)");
        System.out.println("White rook position: 12 (e2)");
        System.out.println("Black pawn blocking on e4: " + (board.getPieceAt(28) == PieceType.BLACK_PAWN));
        System.out.println("Is e8 (60) attacked by white: " + board.isSquareAttacked(60, true));
        board.print();
        
        // Check that black king is not in check due to the blocking pawn
        assertFalse(board.isKingInCheck(false), "Black king should not be in check from white rook on e2 because of blocking pawn");
        
        // Move rook to e8 rank - now it should be in check
        board.removePiece(12, PieceType.WHITE_ROOK);
        board.addPiece(56, PieceType.WHITE_ROOK); // a8
        
        System.out.println("*** Check from Rook Test ***");
        System.out.println("Black king position: " + board.getKingPosition(false) + " (e8)");
        System.out.println("White rook position: 56 (a8)");
        System.out.println("Is e8 (60) attacked by white: " + board.isSquareAttacked(60, true));
        board.print();
        
        // Check that black king is in check
        assertTrue(board.isKingInCheck(false), "Black king should be in check from white rook on a8");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Test checkmate detection with a Scholar's mate position")
    public void testCheckmateDetection() {
        Board board = new Board();

        // Clear the board
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }
        
        // Set up a true checkmate position
        board.addPiece(60, PieceType.BLACK_KING);   // e8
        board.addPiece(52, PieceType.BLACK_PAWN);   // e7 (to block e7 escape)
        board.addPiece(51, PieceType.BLACK_PAWN);   // d7 (to block d7 escape)
        board.addPiece(53, PieceType.BLACK_PAWN);   // f7 (to block f7 escape)
        // Attack with a white rook - can't be blocked or captured
        board.addPiece(20, PieceType.WHITE_ROOK);   // e3 (giving check)
        
        // The key is to set the turn correctly for checkmate detection
        board.setWhiteToMove(false);
        
        // Show the position
        System.out.println("== Checkmate Test Position ==");
        board.print();
        
        // Check if black king is in check
        boolean kingInCheck = board.isKingInCheck(false);
        System.out.println("Black king in check: " + kingInCheck);
        assertTrue(kingInCheck, "Black king should be in check");
        
        // Check legal moves for debugging
        List<Move> legalMoves = board.getLegalMovesSimple();
        System.out.println("Legal moves: " + legalMoves.size());
        for (Move move : legalMoves) {
            System.out.println("  " + move);
        }
        
        // Test the checkmate detection
        boolean isCheckmate = board.isCheckmate();
        System.out.println("Is checkmate: " + isCheckmate);
        assertTrue(isCheckmate, "Position should be checkmate");
    }
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Test stalemate detection with a simple king and queen position")
    public void testStalemateDetection() {
        Board board = new Board();

        // Clear the board
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }

        // Set up a basic stalemate position:
        // - White king on h8 (63)
        // - White queen on g7 (54)
        // - Black king on h6 (47)
        board.addPiece(63, PieceType.WHITE_KING);
        board.addPiece(54, PieceType.WHITE_QUEEN);
        board.addPiece(47, PieceType.BLACK_KING);
        board.setWhiteToMove(false);

        // Debug info
        System.out.println("Black king in check: " + board.isKingInCheck(false));
        System.out.println("Legal moves count: " + board.getLegalMoves().size());
        board.print();
        
        // Verify initial position setup
        assertEquals(PieceType.WHITE_KING, board.getPieceAt(63), "White king should be on h8");
        assertEquals(PieceType.WHITE_QUEEN, board.getPieceAt(54), "White queen should be on g7");
        assertEquals(PieceType.BLACK_KING, board.getPieceAt(47), "Black king should be on h6");
        assertFalse(board.isWhiteToMove(), "Should be black's turn to move");

        // Verify stalemate conditions
        List<Move> legalMoves = board.getLegalMovesSimple();
        assertFalse(board.isKingInCheck(false), "Black king should not be in check");
        assertTrue(legalMoves.isEmpty(), "Black should have no legal moves");
        assertTrue(board.isStalemate(), "Position should be stalemate");

        // Additional verification for board state
        assertFalse(board.isCheckmate(), "Position should not be checkmate");
        assertEquals(3, board.getOccupiedSquares().countBits(), "Board should have exactly 3 pieces");
    }
    
    @Test
    public void testSquareAttacked() {
        Board board = new Board();

        // Check initial position attacks
        assertTrue(board.isSquareAttacked(48, true)); // a7 is attacked by white pawn
        assertTrue(board.isSquareAttacked(49, true)); // b7 is attacked by white pawn
        assertFalse(board.isSquareAttacked(32, true)); // a4 is not attacked by white

        assertTrue(board.isSquareAttacked(8, false)); // a2 is attacked by black pawn
        assertTrue(board.isSquareAttacked(9, false)); // b2 is attacked by black pawn
        assertFalse(board.isSquareAttacked(24, false)); // a3 is not attacked by black

        // Move pieces and check attacks again
        board.makeMove(new Move(1, 16, PieceType.WHITE_KNIGHT)); // Nb1-c3
        board.makeMove(new Move(62, 45, PieceType.BLACK_KNIGHT)); // Ng8-f6

        assertTrue(board.isSquareAttacked(10, true)); // c2 is now attacked by white knight
        assertTrue(board.isSquareAttacked(26, true)); // c4 is attacked by white knight

        assertTrue(board.isSquareAttacked(38, false)); // g5 is now attacked by black knight
        assertTrue(board.isSquareAttacked(28, false)); // e4 is attacked by black knight
    }

    @Test
    public void testInsufficientMaterialDraw() {
        Board board = new Board();

        // Clear the board
        for (int square = 0; square < 64; square++) {
            PieceType piece = board.getPieceAt(square);
            if (piece != null) {
                board.removePiece(square, piece);
            }
        }

        // King vs King (draw)
        board.addPiece(4, PieceType.WHITE_KING);
        board.addPiece(60, PieceType.BLACK_KING);
        assertTrue(board.isInsufficientMaterial());

        // King and knight vs King (draw)
        board.addPiece(1, PieceType.WHITE_KNIGHT);
        assertTrue(board.isInsufficientMaterial());

        // King and bishop vs King (draw)
        board.removePiece(1, PieceType.WHITE_KNIGHT);
        board.addPiece(2, PieceType.WHITE_BISHOP);
        assertTrue(board.isInsufficientMaterial());

        // King and pawn vs King (not draw)
        board.removePiece(2, PieceType.WHITE_BISHOP);
        board.addPiece(12, PieceType.WHITE_PAWN);
        assertFalse(board.isInsufficientMaterial());
    }
}
