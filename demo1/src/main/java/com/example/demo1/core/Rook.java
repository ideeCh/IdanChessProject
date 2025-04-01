package com.example.demo1.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Rook
 */
public class Rook extends ChessPiece {

    public Rook(Color color) {
        super(color);
    }

    @Override
    public ChessPieceType getType() {
        return ChessPieceType.ROOK;
    }

    @Override
    public List<Move> generatePotentialMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Find the rook's position
        Position rookPosition = null;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == this) {
                    rookPosition = pos;
                    break;
                }
            }

            if (rookPosition != null) {
                break;
            }
        }

        if (rookPosition == null) {
            return moves;
        }

        // Generate moves in 4 directions (horizontal and vertical)
        int[] fileOffsets = {-1, 0, 0, 1};
        int[] rankOffsets = {0, -1, 1, 0};

        for (int i = 0; i < 4; i++) {
            for (int distance = 1; distance < 8; distance++) {
                char newFile = (char) (rookPosition.getFile() + fileOffsets[i] * distance);
                int newRank = rookPosition.getRank() + rankOffsets[i] * distance;

                Position newPos = new Position(newFile, newRank);

                if (!newPos.isValid()) {
                    break;
                }

                ChessPiece pieceAtTarget = board.getPieceAt(newPos);

                if (pieceAtTarget == null) {
                    moves.add(new Move(rookPosition, newPos));
                } else {
                    if (pieceAtTarget.getColor() != color) {
                        moves.add(new Move(rookPosition, newPos));
                    }
                    break;
                }
            }
        }

        return moves;
    }
}
