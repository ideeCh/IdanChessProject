package com.example.demo1.core.ai.evaluation;
import com.example.demo1.core.*;

/**
 * Specialized evaluator for the opening phase of a chess game.
 * Focuses on piece development, center control, king safety, and
 * following classical opening principles.
 */
public class OpeningEvaluator extends PhaseBasedEvaluator {

    // Constants for opening evaluation
    private static final int DEVELOPMENT_BONUS = 15;
    private static final int CENTER_CONTROL_BONUS = 10;
    private static final int KING_SAFETY_BONUS = 30;
    private static final int EARLY_QUEEN_MOVE_PENALTY = -15;
    private static final int DOUBLE_MOVE_MINOR_PIECE_PENALTY = -10;
    private static final int FIANCHETTO_BONUS = 15;
    private static final int BLOCKING_CENTER_PAWN_PENALTY = -15;

    /**
     * Evaluate a position from an opening phase perspective
     *
     * @param board The current board state
     * @param sideToEvaluate Side from whose perspective to evaluate
     * @return Evaluation score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate) {
        int whiteDevelopment = evaluateDevelopment(board, Color.WHITE);
        int blackDevelopment = evaluateDevelopment(board, Color.BLACK);

        int whiteCenterControl = evaluateCenterControl(board, Color.WHITE);
        int blackCenterControl = evaluateCenterControl(board, Color.BLACK);

        int whiteKingSafety = evaluateKingSafety(board, Color.WHITE);
        int blackKingSafety = evaluateKingSafety(board, Color.BLACK);

        int whiteSpaceControl = evaluateSpaceControl(board, Color.WHITE);
        int blackSpaceControl = evaluateSpaceControl(board, Color.BLACK);

        int totalScore = (whiteDevelopment - blackDevelopment) +
                (whiteCenterControl - blackCenterControl) +
                (whiteKingSafety - blackKingSafety) +
                (whiteSpaceControl - blackSpaceControl);

        return sideToEvaluate == Color.WHITE ? totalScore : -totalScore;
    }

    /**
     * Evaluate piece development in opening
     */
    private int evaluateDevelopment(Board board, Color color) {
        int score = 0;
        int homeRank = color == Color.WHITE ? 1 : 8;

        // Check knights development
        if (isSquareEmpty(board, 'b', homeRank)) score += DEVELOPMENT_BONUS;
        if (isSquareEmpty(board, 'g', homeRank)) score += DEVELOPMENT_BONUS;

        // Check bishop development
        if (isSquareEmpty(board, 'c', homeRank)) score += DEVELOPMENT_BONUS;
        if (isSquareEmpty(board, 'f', homeRank)) score += DEVELOPMENT_BONUS;

        // Check if bishops have fianchettoed (valuable development pattern)
        if (hasFianchetto(board, color, true)) { // Kingside fianchetto
            score += FIANCHETTO_BONUS;
        }
        if (hasFianchetto(board, color, false)) { // Queenside fianchetto
            score += FIANCHETTO_BONUS;
        }

        // Penalize early queen development
        if (isSquareEmpty(board, 'd', homeRank) && !isMinorPiecesDeveloped(board, color)) {
            score += EARLY_QUEEN_MOVE_PENALTY;
        }

        // Penalize minor pieces blocking center pawns
        if (isMinorPieceBlockingCenterPawn(board, color)) {
            score += BLOCKING_CENTER_PAWN_PENALTY;
        }

        // Check rook connectivity (are they connected along the back rank)
        if (areRooksConnected(board, color)) {
            score += 10;
        }

        // Check castling (major bonus)
        if (hasKingCastled(board, color)) {
            score += KING_SAFETY_BONUS;
        }

        return score;
    }

    /**
     * Check if most minor pieces are developed
     */
    private boolean isMinorPiecesDeveloped(Board board, Color color) {
        int developedCount = 0;
        int homeRank = color == Color.WHITE ? 1 : 8;

        // Count developed knights and bishops
        if (isSquareEmpty(board, 'b', homeRank)) developedCount++;
        if (isSquareEmpty(board, 'g', homeRank)) developedCount++;
        if (isSquareEmpty(board, 'c', homeRank)) developedCount++;
        if (isSquareEmpty(board, 'f', homeRank)) developedCount++;

        // Consider development complete if most pieces are developed
        return developedCount >= 3;
    }

    /**
     * Check if a bishop is fianchettoed (developed to b2/g2 for white or b7/g7 for black)
     */
    private boolean hasFianchetto(Board board, Color color, boolean kingSide) {
        int rank = color == Color.WHITE ? 2 : 7;
        char file = kingSide ? 'g' : 'b';

        Position bishopPos = new Position(file, rank);
        ChessPiece piece = board.getPieceAt(bishopPos);

        return piece != null && piece.getType() == ChessPieceType.BISHOP &&
                piece.getColor() == color;
    }

    /**
     * Check if a minor piece is blocking a center pawn
     */
    private boolean isMinorPieceBlockingCenterPawn(Board board, Color color) {
        int pawnRank = color == Color.WHITE ? 2 : 7;
        int pieceRank = color == Color.WHITE ? 3 : 6;

        // Check d-pawn blocked by piece
        Position dPawn = new Position('d', pawnRank);
        Position dBlock = new Position('d', pieceRank);

        ChessPiece pawn1 = board.getPieceAt(dPawn);
        ChessPiece block1 = board.getPieceAt(dBlock);

        boolean dPawnBlocked = pawn1 != null && pawn1.getType() == ChessPieceType.PAWN &&
                pawn1.getColor() == color && block1 != null &&
                block1.getColor() == color;

        // Check e-pawn blocked by piece
        Position ePawn = new Position('e', pawnRank);
        Position eBlock = new Position('e', pieceRank);

        ChessPiece pawn2 = board.getPieceAt(ePawn);
        ChessPiece block2 = board.getPieceAt(eBlock);

        boolean ePawnBlocked = pawn2 != null && pawn2.getType() == ChessPieceType.PAWN &&
                pawn2.getColor() == color && block2 != null &&
                block2.getColor() == color;

        return dPawnBlocked || ePawnBlocked;
    }

    /**
     * Check if rooks are connected (can move between each other)
     */
    private boolean areRooksConnected(Board board, Color color) {
        int rank = color == Color.WHITE ? 1 : 8;

        // Check if squares between rooks are empty
        boolean kingInTheWay = !isSquareEmpty(board, 'e', rank);
        boolean blockedQueenside = !isSquareEmpty(board, 'b', rank) ||
                !isSquareEmpty(board, 'c', rank) ||
                !isSquareEmpty(board, 'd', rank);
        boolean blockedKingside = !isSquareEmpty(board, 'f', rank) ||
                !isSquareEmpty(board, 'g', rank);

        return !(kingInTheWay || blockedQueenside || blockedKingside);
    }

    /**
     * Check if king has castled
     */
    private boolean hasKingCastled(Board board, Color color) {
        int rank = color == Color.WHITE ? 1 : 8;
        Position originalPos = new Position('e', rank);

        // King not on original square
        if (board.getPieceAt(originalPos) == null) {
            // Check if king is on typical castled squares
            Position kingsideCastle = new Position('g', rank);
            Position queensideCastle = new Position('c', rank);

            ChessPiece kingside = board.getPieceAt(kingsideCastle);
            ChessPiece queenside = board.getPieceAt(queensideCastle);

            boolean onKingside = (kingside != null &&
                    kingside.getType() == ChessPieceType.KING &&
                    kingside.getColor() == color);

            boolean onQueenside = (queenside != null &&
                    queenside.getType() == ChessPieceType.KING &&
                    queenside.getColor() == color);

            return onKingside || onQueenside;
        }

        return false;
    }

    /**
     * Evaluate center control in opening
     */
    private int evaluateCenterControl(Board board, Color color) {
        int score = 0;

        // Central squares (d4, e4, d5, e5)
        Position[] centralSquares = {
                new Position('d', 4),
                new Position('e', 4),
                new Position('d', 5),
                new Position('e', 5)
        };

        // Check occupation of central squares
        for (Position pos : centralSquares) {
            ChessPiece piece = board.getPieceAt(pos);
            if (piece != null && piece.getColor() == color) {
                // Pawns and knights are especially good in center
                if (piece.getType() == ChessPieceType.PAWN) {
                    score += CENTER_CONTROL_BONUS * 2;
                } else if (piece.getType() == ChessPieceType.KNIGHT) {
                    score += CENTER_CONTROL_BONUS;
                } else {
                    score += CENTER_CONTROL_BONUS / 2;
                }
            }

            // Also count attacks on center squares
            int attackCount = countAttacksOnSquare(board, pos, color);
            score += attackCount * 3; // 3 points per attack on center
        }

        // Extended center (c3-f3-c6-f6)
        Position[] extendedCenter = {
                new Position('c', 3), new Position('d', 3), new Position('e', 3), new Position('f', 3),
                new Position('c', 4), new Position('f', 4),
                new Position('c', 5), new Position('f', 5),
                new Position('c', 6), new Position('d', 6), new Position('e', 6), new Position('f', 6)
        };

        // Check occupation of extended center (less valuable than strict center)
        for (Position pos : extendedCenter) {
            ChessPiece piece = board.getPieceAt(pos);
            if (piece != null && piece.getColor() == color) {
                score += 3; // Small bonus for extended center control
            }
        }

        return score;
    }

    /**
     * Count how many pieces of a given color attack a specific square
     * This is a simplified implementation - a real one would check attack patterns
     */
    private int countAttacksOnSquare(Board board, Position pos, Color attackerColor) {
        // Simplified - in a real implementation, you'd use your attack detection
        // logic from the chess engine
        return 0;
    }

    /**
     * Evaluate king safety in opening
     */
    private int evaluateKingSafety(Board board, Color color) {
        int score = 0;

        // Major bonus for having castled
        if (hasKingCastled(board, color)) {
            score += KING_SAFETY_BONUS;

            // Check pawn shield
            Position kingPos = findKing(board, color);
            if (kingPos != null) {
                score += evaluatePawnShield(board, kingPos, color);
            }
        } else {
            // Penalty for king in center if game has progressed
            if (countDevelopedPieces(board) >= 6) {
                Position kingPos = findKing(board, color);
                if (kingPos != null && isKingInCenter(kingPos)) {
                    score -= 20;
                }
            }
        }

        return score;
    }

    /**
     * Check if king is in the vulnerable center
     */
    private boolean isKingInCenter(Position kingPos) {
        char file = kingPos.getFile();
        int rank = kingPos.getRank();

        // Center area
        return (file >= 'c' && file <= 'f' && rank >= 3 && rank <= 6);
    }

    /**
     * Evaluate pawn shield for a castled king
     */
    private int evaluatePawnShield(Board board, Position kingPos, Color kingColor) {
        int score = 0;
        int kingFile = kingPos.getFile() - 'a';
        int kingRank = kingPos.getRank();

        // Define shield positions based on castling type
        boolean isKingside = kingFile >= 5;

        if (kingColor == Color.WHITE) {
            // Check pawns in front of king
            for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
                int shieldFile = kingFile + fileOffset;
                if (shieldFile < 0 || shieldFile > 7) continue;

                Position shieldPos = new Position((char)('a' + shieldFile), kingRank + 1);
                ChessPiece piece = board.getPieceAt(shieldPos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == kingColor) {
                    score += 5;
                } else {
                    score -= 5; // Penalty for missing shield pawn
                }
            }
        } else { // BLACK
            // Check pawns in front of king
            for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
                int shieldFile = kingFile + fileOffset;
                if (shieldFile < 0 || shieldFile > 7) continue;

                Position shieldPos = new Position((char)('a' + shieldFile), kingRank - 1);
                ChessPiece piece = board.getPieceAt(shieldPos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == kingColor) {
                    score += 5;
                } else {
                    score -= 5; // Penalty for missing shield pawn
                }
            }
        }

        return score;
    }

    /**
     * Evaluate space control and pawn structure
     */
    private int evaluateSpaceControl(Board board, Color color) {
        int score = 0;
        Color enemy = color == Color.WHITE ? Color.BLACK : Color.WHITE;

        // Count pawns by rank to measure space control
        int[] pawnsByRank = new int[9]; // 1-8 ranks

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    pawnsByRank[rank]++;
                }
            }
        }

        // Advanced pawns control more space
        if (color == Color.WHITE) {
            // White gets points for pawns beyond rank 3
            score += pawnsByRank[3] * 2;
            score += pawnsByRank[4] * 4;
            score += pawnsByRank[5] * 6;
            score += pawnsByRank[6] * 8;
        } else {
            // Black gets points for pawns below rank 6
            score += pawnsByRank[6] * 2;
            score += pawnsByRank[5] * 4;
            score += pawnsByRank[4] * 6;
            score += pawnsByRank[3] * 8;
        }

        // Penalize opening too many pawn fronts
        int pawnFiles = 0;
        for (char file = 'a'; file <= 'h'; file++) {
            boolean hasPawnMoved = false;

            for (int rank = 1; rank <= 8; rank++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    int homeRank = color == Color.WHITE ? 2 : 7;
                    if (rank != homeRank) {
                        hasPawnMoved = true;
                        break;
                    }
                }
            }

            if (hasPawnMoved) {
                pawnFiles++;
            }
        }

        // Penalty for moving too many different pawns in opening
        if (pawnFiles > 3) {
            score -= (pawnFiles - 3) * 5;
        }

        return score;
    }

    /**
     * Find the king of a given color
     */
    public Position findKing(Board board, Color color) {
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.KING &&
                        piece.getColor() == color) {
                    return pos;
                }
            }
        }

        return null; // Should never happen in a valid board
    }

    /**
     * Count the total number of pieces that have moved from initial position
     */
    private int countDevelopedPieces(Board board) {
        int count = 0;

        // Original minor piece positions
        Position[] originalPositions = {
                new Position('b', 1), new Position('g', 1),
                new Position('c', 1), new Position('f', 1),
                new Position('b', 8), new Position('g', 8),
                new Position('c', 8), new Position('f', 8)
        };

        for (Position pos : originalPositions) {
            if (board.getPieceAt(pos) == null) {
                count++;
            }
        }

        return count;
    }

    /**
     * Check if a square is empty (used to check if pieces have moved)
     */
    public boolean isSquareEmpty(Board board, char file, int rank) {
        Position pos = new Position(file, rank);
        return board.getPieceAt(pos) == null;
    }
}