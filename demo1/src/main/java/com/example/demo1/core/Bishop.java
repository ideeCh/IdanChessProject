package com.example.demo1.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Bishop
 */
public class Bishop extends ChessPiece {

    public Bishop(Color color) {
        super(color);
    }

    @Override
    public ChessPieceType getType() {
        return ChessPieceType.BISHOP;
    }

    @Override
    public List<Move> generatePotentialMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Find the bishop's position
        Position bishopPosition = null;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == this) {
                    bishopPosition = pos;
                    break;
                }
            }

            if (bishopPosition != null) {
                break;
            }
        }

        if (bishopPosition == null) {
            return moves;
        }

        // Generate moves in 4 diagonal directions
        int[] fileOffsets = {-1, -1, 1, 1};
        int[] rankOffsets = {-1, 1, -1, 1};

        for (int i = 0; i < 4; i++) {
            for (int distance = 1; distance < 8; distance++) {
                char newFile = (char) (bishopPosition.getFile() + fileOffsets[i] * distance);
                int newRank = bishopPosition.getRank() + rankOffsets[i] * distance;

                Position newPos = new Position(newFile, newRank);

                if (!newPos.isValid()) {
                    break;
                }

                ChessPiece pieceAtTarget = board.getPieceAt(newPos);

                if (pieceAtTarget == null) {
                    moves.add(new Move(bishopPosition, newPos));
                } else {
                    if (pieceAtTarget.getColor() != color) {
                        moves.add(new Move(bishopPosition, newPos));
                    }
                    break;
                }
            }
        }

        return moves;
    }
}
