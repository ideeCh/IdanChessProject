package com.example.demo1.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Knight
 */
public class Knight extends ChessPiece {

    public Knight(Color color) {
        super(color);
    }

    @Override
    public ChessPieceType getType() {
        return ChessPieceType.KNIGHT;
    }

    @Override
    public List<Move> generatePotentialMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Find the knight's position
        Position knightPosition = null;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == this) {
                    knightPosition = pos;
                    break;
                }
            }

            if (knightPosition != null) {
                break;
            }
        }

        if (knightPosition == null) {
            return moves;
        }

        // Generate moves in L-pattern
        int[] fileOffsets = {-2, -2, -1, -1, 1, 1, 2, 2};
        int[] rankOffsets = {-1, 1, -2, 2, -2, 2, -1, 1};

        for (int i = 0; i < 8; i++) {
            char newFile = (char) (knightPosition.getFile() + fileOffsets[i]);
            int newRank = knightPosition.getRank() + rankOffsets[i];

            Position newPos = new Position(newFile, newRank);

            if (!newPos.isValid()) {
                continue;
            }

            ChessPiece pieceAtTarget = board.getPieceAt(newPos);

            if (pieceAtTarget == null || pieceAtTarget.getColor() != color) {
                moves.add(new Move(knightPosition, newPos));
            }
        }

        return moves;
    }
}
