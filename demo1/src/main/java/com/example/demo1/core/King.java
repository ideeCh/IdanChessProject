package com.example.demo1.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a King
 */
public class King extends ChessPiece {

    public King(Color color) {
        super(color);
    }

    @Override
    public ChessPieceType getType() {
        return ChessPieceType.KING;
    }

    @Override
    public List<Move> generatePotentialMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Find the king's position
        Position kingPosition = null;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == this) {
                    kingPosition = pos;
                    break;
                }
            }

            if (kingPosition != null) {
                break;
            }
        }

        if (kingPosition == null) {
            return moves;
        }

        // Generate moves in all 8 directions
        int[] fileOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] rankOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            char newFile = (char) (kingPosition.getFile() + fileOffsets[i]);
            int newRank = kingPosition.getRank() + rankOffsets[i];

            Position newPos = new Position(newFile, newRank);

            if (!newPos.isValid()) {
                continue;
            }

            ChessPiece pieceAtTarget = board.getPieceAt(newPos);

            if (pieceAtTarget == null || pieceAtTarget.getColor() != color) {
                moves.add(new Move(kingPosition, newPos));
            }
        }

        // Add castling moves if king hasn't moved
        if (!hasMoved) {
            // King's rank depends on color
            int kingRank = (color == Color.WHITE) ? 1 : 8;

            // Check kingside castling (O-O)
            boolean canCastleKingside = true;
            // Check if squares between king and rook are empty
            for (char file = 'f'; file <= 'g'; file++) {
                Position pos = new Position(file, kingRank);
                if (board.getPieceAt(pos) != null) {
                    canCastleKingside = false;
                    break;
                }
            }

            // Check if kingside rook is in place and hasn't moved
            Position kingsideRookPos = new Position('h', kingRank);
            ChessPiece kingsideRook = board.getPieceAt(kingsideRookPos);

            if (canCastleKingside && kingsideRook != null &&
                    kingsideRook.getType() == ChessPieceType.ROOK &&
                    kingsideRook.getColor() == color &&
                    !kingsideRook.hasMoved()) {

                // Add kingside castling move
                Position target = new Position('g', kingRank);
                Move castlingMove = new Move(kingPosition, target);
                castlingMove.setCastling(true);
                castlingMove.setCastlingRookSource(kingsideRookPos);
                castlingMove.setCastlingRookTarget(new Position('f', kingRank));
                moves.add(castlingMove);
            }

            // Check queenside castling (O-O-O)
            boolean canCastleQueenside = true;
            // Check if squares between king and rook are empty
            for (char file = 'b'; file <= 'd'; file++) {
                Position pos = new Position(file, kingRank);
                if (board.getPieceAt(pos) != null) {
                    canCastleQueenside = false;
                    break;
                }
            }

            // Check if queenside rook is in place and hasn't moved
            Position queensideRookPos = new Position('a', kingRank);
            ChessPiece queensideRook = board.getPieceAt(queensideRookPos);

            if (canCastleQueenside && queensideRook != null &&
                    queensideRook.getType() == ChessPieceType.ROOK &&
                    queensideRook.getColor() == color &&
                    !queensideRook.hasMoved()) {

                // Add queenside castling move
                Position target = new Position('c', kingRank);
                Move castlingMove = new Move(kingPosition, target);
                castlingMove.setCastling(true);
                castlingMove.setCastlingRookSource(queensideRookPos);
                castlingMove.setCastlingRookTarget(new Position('d', kingRank));
                moves.add(castlingMove);
            }
        }

        return moves;
    }
}
