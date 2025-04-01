package com.example.demo1.core;

import com.example.demo1.special.EnPassant;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Pawn
 */
public class Pawn extends ChessPiece {

    public Pawn(Color color) {
        super(color);
    }

    @Override
    public ChessPieceType getType() {
        return ChessPieceType.PAWN;
    }

    @Override
    public List<Move> generatePotentialMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Find the pawn's position
        Position pawnPosition = null;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == this) {
                    pawnPosition = pos;
                    break;
                }
            }

            if (pawnPosition != null) {
                break;
            }
        }

        if (pawnPosition == null) {
            return moves;
        }

        // Direction depends on color
        int direction = (color == Color.WHITE) ? 1 : -1;

        // Forward move
        Position oneForward = new Position(pawnPosition.getFile(), pawnPosition.getRank() + direction);

        if (oneForward.isValid() && board.getPieceAt(oneForward) == null) {
            // Check for promotion
            if ((color == Color.WHITE && oneForward.getRank() == 8) ||
                    (color == Color.BLACK && oneForward.getRank() == 1)) {
                // Add promotion moves
                moves.add(new Move(pawnPosition, oneForward, ChessPieceType.QUEEN));
                moves.add(new Move(pawnPosition, oneForward, ChessPieceType.ROOK));
                moves.add(new Move(pawnPosition, oneForward, ChessPieceType.BISHOP));
                moves.add(new Move(pawnPosition, oneForward, ChessPieceType.KNIGHT));
            } else {
                moves.add(new Move(pawnPosition, oneForward));
            }

            // Two squares forward if hasn't moved
            if (!hasMoved) {
                Position twoForward = new Position(pawnPosition.getFile(), pawnPosition.getRank() + 2 * direction);

                if (twoForward.isValid() && board.getPieceAt(twoForward) == null) {
                    moves.add(new Move(pawnPosition, twoForward));
                }
            }
        }

        // Regular captures
        for (int fileOffset : new int[]{-1, 1}) {
            char newFile = (char) (pawnPosition.getFile() + fileOffset);
            int newRank = pawnPosition.getRank() + direction;

            Position capturePos = new Position(newFile, newRank);

            if (capturePos.isValid()) {
                ChessPiece pieceAtTarget = board.getPieceAt(capturePos);

                if (pieceAtTarget != null && pieceAtTarget.getColor() != color) {
                    // Check for promotion
                    if ((color == Color.WHITE && capturePos.getRank() == 8) ||
                            (color == Color.BLACK && capturePos.getRank() == 1)) {
                        // Add promotion moves
                        moves.add(new Move(pawnPosition, capturePos, ChessPieceType.QUEEN));
                        moves.add(new Move(pawnPosition, capturePos, ChessPieceType.ROOK));
                        moves.add(new Move(pawnPosition, capturePos, ChessPieceType.BISHOP));
                        moves.add(new Move(pawnPosition, capturePos, ChessPieceType.KNIGHT));
                    } else {
                        moves.add(new Move(pawnPosition, capturePos));
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Generate all potential moves for this pawn, including en passant captures
     *
     * @param board     Current board state
     * @param gameState Current game state for en passant information
     * @return List of potential moves
     */
    public List<Move> generatePotentialMoves(Board board, GameState gameState) {
        List<Move> moves = generatePotentialMoves(board);

        // Find the pawn's position
        Position pawnPosition = null;
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);
                if (piece == this) {
                    pawnPosition = pos;
                    break;
                }
            }
            if (pawnPosition != null) break;
        }

        if (pawnPosition != null) {
            // Check for en passant capture
            Position enPassantCapturePos =
                    EnPassant.getEnPassantCapturePosition(gameState, pawnPosition);
            if (enPassantCapturePos != null) {
                moves.add(new Move(pawnPosition, enPassantCapturePos));
            }
        }

        return moves;
    }
}