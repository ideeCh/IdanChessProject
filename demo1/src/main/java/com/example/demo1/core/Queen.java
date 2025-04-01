package com.example.demo1.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Queen
 */
public class Queen extends ChessPiece {

    public Queen(Color color) {
        super(color);
    }

    @Override
    public ChessPieceType getType() {
        return ChessPieceType.QUEEN;
    }

    @Override
    public List<Move> generatePotentialMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Find the queen's position
        Position queenPosition = null;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == this) {
                    queenPosition = pos;
                    break;
                }
            }

            if (queenPosition != null) {
                break;
            }
        }

        if (queenPosition == null) {
            return moves;
        }

        // Generate moves in all 8 directions (like rook + bishop)
        int[] fileOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] rankOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            for (int distance = 1; distance < 8; distance++) {
                char newFile = (char) (queenPosition.getFile() + fileOffsets[i] * distance);
                int newRank = queenPosition.getRank() + rankOffsets[i] * distance;

                Position newPos = new Position(newFile, newRank);

                if (!newPos.isValid()) {
                    break;
                }

                ChessPiece pieceAtTarget = board.getPieceAt(newPos);

                if (pieceAtTarget == null) {
                    moves.add(new Move(queenPosition, newPos));
                } else {
                    if (pieceAtTarget.getColor() != color) {
                        moves.add(new Move(queenPosition, newPos));
                    }
                    break;
                }
            }
        }

        return moves;
    }
}
