package com.example.demo1.core.ai.evaluation.components;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;
import java.util.*;

/**
 * Evaluates piece activity, mobility, and positioning
 */
public class PieceActivityEvaluator {

    // Mobility bonuses (per legal move)
    private static final int KNIGHT_MOBILITY_BONUS = 4;
    private static final int BISHOP_MOBILITY_BONUS = 5;
    private static final int ROOK_MOBILITY_BONUS = 2;
    private static final int QUEEN_MOBILITY_BONUS = 1; // Lower per move since queens are naturally mobile

    // Special position bonuses
    private static final int ROOK_OPEN_FILE_BONUS = 20;
    private static final int ROOK_SEMI_OPEN_FILE_BONUS = 10;
    private static final int ROOK_SEVENTH_RANK_BONUS = 30;
    private static final int KNIGHT_OUTPOST_BONUS = 25;
    private static final int BISHOP_OUTPOST_BONUS = 15;

    /**
     * Evaluate piece activity
     *
     * @param board The chess board
     * @param sideToEvaluate Perspective to evaluate from
     * @param phaseValue Game phase value (0.0 = opening, 1.0 = endgame)
     * @return Score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate, double phaseValue) {
        int whiteScore = 0;
        int blackScore = 0;

        // Adjust mobility importance based on game phase
        // More important in middlegame, less in endgame
        double mobilityMultiplier = 1.0;
        if (phaseValue < 0.3) {
            // Less important in opening
            mobilityMultiplier = 0.7;
        } else if (phaseValue > 0.7) {
            // Less important in endgame
            mobilityMultiplier = 0.8;
        }

        // Evaluate each piece's activity
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                // Skip pawns and kings (handled in other evaluators)
                if (piece.getType() == ChessPieceType.PAWN ||
                        piece.getType() == ChessPieceType.KING) {
                    continue;
                }

                // Get piece-specific activity score
                int activityScore = evaluatePieceActivity(board, pos, piece, phaseValue);

                // Add mobility score with phase-appropriate weighting
                int mobilityScore = (int)(evaluatePieceMobility(board, pos, piece) * mobilityMultiplier);

                // Combined score for this piece
                int pieceScore = activityScore + mobilityScore;

                // Add to appropriate side's score
                if (piece.getColor() == Color.WHITE) {
                    whiteScore += pieceScore;
                } else {
                    blackScore += pieceScore;
                }
            }
        }

        // Special bonus for connected rooks
        whiteScore += evaluateConnectedRooks(board, Color.WHITE);
        blackScore += evaluateConnectedRooks(board, Color.BLACK);

        // Score from the perspective of the side to evaluate
        return (sideToEvaluate == Color.WHITE) ?
                (whiteScore - blackScore) : (blackScore - whiteScore);
    }

    /**
     * Evaluate a specific piece's activity
     */
    private int evaluatePieceActivity(Board board, Position pos, ChessPiece piece, double phaseValue) {
        int score = 0;

        switch (piece.getType()) {
            case KNIGHT:
                score += evaluateKnightActivity(board, pos, piece.getColor());
                break;

            case BISHOP:
                score += evaluateBishopActivity(board, pos, piece.getColor());
                break;

            case ROOK:
                score += evaluateRookActivity(board, pos, piece.getColor(), phaseValue);
                break;

            case QUEEN:
                score += evaluateQueenActivity(board, pos, piece.getColor(), phaseValue);
                break;
        }

        return score;
    }

    /**
     * Evaluate knight positioning and outposts
     */
    private int evaluateKnightActivity(Board board, Position pos, Color color) {
        int score = 0;
        int file = pos.getFile() - 'a';
        int rank = pos.getRank() - 1;

        // Knights are better centralized
        if (file >= 2 && file <= 5 && rank >= 2 && rank <= 5) {
            score += 10; // Center bonus
        }

        // Knights on the rim are dim
        if (file == 0 || file == 7 || rank == 0 || rank == 7) {
            score -= 15;
        }

        // Check for knight outpost
        if (isKnightOutpost(board, pos, color)) {
            score += KNIGHT_OUTPOST_BONUS;

            // Extra bonus if outpost is protected by a pawn
            if (isProtectedByPawn(board, pos, color)) {
                score += 10;
            }
        }

        return score;
    }

    /**
     * Check if a knight is on an outpost
     */
    private boolean isKnightOutpost(Board board, Position pos, Color color) {
        // An outpost is a square that cannot be attacked by enemy pawns
        int rank = pos.getRank();

        // Knights need to be in enemy territory
        boolean inEnemyTerritory = (color == Color.WHITE) ? rank >= 5 : rank <= 4;

        if (!inEnemyTerritory) {
            return false;
        }

        // Check if knight can be attacked by enemy pawns
        return !canBeAttackedByEnemyPawn(board, pos, color);
    }

    /**
     * Check if a position can be attacked by an enemy pawn
     */
    private boolean canBeAttackedByEnemyPawn(Board board, Position pos, Color pieceColor) {
        int file = pos.getFile() - 'a';
        int rank = pos.getRank();
        Color enemyColor = (pieceColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check diagonally behind (where enemy pawns would attack from)
        int pawnRank = (pieceColor == Color.WHITE) ? rank + 1 : rank - 1;

        // Make sure rank is valid
        if (pawnRank < 1 || pawnRank > 8) {
            return false;
        }

        // Check left diagonal
        if (file > 0) {
            Position leftPos = new Position((char)('a' + file - 1), pawnRank);
            ChessPiece leftPiece = board.getPieceAt(leftPos);

            if (leftPiece != null && leftPiece.getType() == ChessPieceType.PAWN &&
                    leftPiece.getColor() == enemyColor) {
                return true;
            }
        }

        // Check right diagonal
        if (file < 7) {
            Position rightPos = new Position((char)('a' + file + 1), pawnRank);
            ChessPiece rightPiece = board.getPieceAt(rightPos);

            if (rightPiece != null && rightPiece.getType() == ChessPieceType.PAWN &&
                    rightPiece.getColor() == enemyColor) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a piece is protected by a friendly pawn
     */
    private boolean isProtectedByPawn(Board board, Position pos, Color pieceColor) {
        int file = pos.getFile() - 'a';
        int rank = pos.getRank();

        // Check diagonally behind (where protecting pawns would be)
        int pawnRank = (pieceColor == Color.WHITE) ? rank - 1 : rank + 1;

        // Make sure rank is valid
        if (pawnRank < 1 || pawnRank > 8) {
            return false;
        }

        // Check left diagonal
        if (file > 0) {
            Position leftPos = new Position((char)('a' + file - 1), pawnRank);
            ChessPiece leftPiece = board.getPieceAt(leftPos);

            if (leftPiece != null && leftPiece.getType() == ChessPieceType.PAWN &&
                    leftPiece.getColor() == pieceColor) {
                return true;
            }
        }

        // Check right diagonal
        if (file < 7) {
            Position rightPos = new Position((char)('a' + file + 1), pawnRank);
            ChessPiece rightPiece = board.getPieceAt(rightPos);

            if (rightPiece != null && rightPiece.getType() == ChessPieceType.PAWN &&
                    rightPiece.getColor() == pieceColor) {
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluate bishop positioning
     */
    private int evaluateBishopActivity(Board board, Position pos, Color color) {
        int score = 0;

        // Check for bishop pair
        if (hasBishopPair(board, color)) {
            score += 10; // Small bonus already applied in material evaluator, just extra here
        }

        // Check if bishop is on a good diagonal
        score += evaluateBishopDiagonals(board, pos);

        // Check for bishop outpost
        if (isBishopOutpost(board, pos, color)) {
            score += BISHOP_OUTPOST_BONUS;
        }

        // Penalty for "bad bishop" trapped behind own pawns
        score += evaluateBadBishop(board, pos, color);

        return score;
    }

    /**
     * Check if a side has the bishop pair
     */
    private boolean hasBishopPair(Board board, Color color) {
        boolean hasLightSquareBishop = false;
        boolean hasDarkSquareBishop = false;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.BISHOP &&
                        piece.getColor() == color) {
                    // Determine bishop's square color
                    boolean isLightSquare = ((pos.getFile() - 'a' + pos.getRank()) % 2 == 0);

                    if (isLightSquare) {
                        hasLightSquareBishop = true;
                    } else {
                        hasDarkSquareBishop = true;
                    }
                }
            }
        }

        return hasLightSquareBishop && hasDarkSquareBishop;
    }

    /**
     * Evaluate bishop diagonals
     */
    private int evaluateBishopDiagonals(Board board, Position pos) {
        int score = 0;
        int openDiagonals = 0;
        int blockedDiagonals = 0;

        // Directions for diagonal checks
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            boolean isOpen = true;

            // Check diagonal in this direction
            for (int dist = 1; dist <= 7; dist++) {
                int newFile = pos.getFile() - 'a' + dir[0] * dist;
                int newRank = pos.getRank() - 1 + dir[1] * dist;

                // Check if position is on the board
                if (newFile < 0 || newFile > 7 || newRank < 0 || newRank > 7) {
                    break;
                }

                Position newPos = new Position((char)('a' + newFile), newRank + 1);
                ChessPiece piece = board.getPieceAt(newPos);

                if (piece != null) {
                    // Diagonal is blocked
                    isOpen = false;
                    break;
                }
            }

            if (isOpen) {
                openDiagonals++;
            } else {
                blockedDiagonals++;
            }
        }

        // Score based on number of open diagonals
        score += openDiagonals * 5;
        score -= blockedDiagonals * 2;

        return score;
    }

    /**
     * Check if a bishop is on an outpost
     */
    private boolean isBishopOutpost(Board board, Position pos, Color color) {
        return isKnightOutpost(board, pos, color); // Same criteria as knight outpost
    }

    /**
     * Evaluate if a bishop is "bad" (blocked by own pawns)
     */
    private int evaluateBadBishop(Board board, Position pos, Color color) {
        // Determine bishop's square color
        boolean isLightSquared = ((pos.getFile() - 'a' + pos.getRank()) % 2 == 0);
        int score = 0;

        // Count own pawns on same-colored squares
        int pawnsOnSameColor = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pawnPos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pawnPos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    // Check if pawn is on same colored square as bishop
                    boolean isPawnOnLightSquare = ((pawnPos.getFile() - 'a' + pawnPos.getRank()) % 2 == 0);

                    if (isPawnOnLightSquare == isLightSquared) {
                        pawnsOnSameColor++;
                    }
                }
            }
        }

        // Penalty based on number of pawns on same colored squares
        score -= pawnsOnSameColor * 3;

        return score;
    }

    /**
     * Evaluate rook positioning
     */
    private int evaluateRookActivity(Board board, Position pos, Color color, double phaseValue) {
        int score = 0;
        char file = pos.getFile();
        int rank = pos.getRank();

        // Check for rook on open file
        if (isOpenFile(board, file)) {
            score += ROOK_OPEN_FILE_BONUS;
        }
        // Check for rook on semi-open file
        else if (isSemiOpenFile(board, file, color)) {
            score += ROOK_SEMI_OPEN_FILE_BONUS;
        }

        // Check for rook on 7th rank (2nd rank for black)
        int seventhRank = (color == Color.WHITE) ? 7 : 2;
        if (rank == seventhRank) {
            // More valuable in endgame when attacking enemy king
            int seventhRankBonus = ROOK_SEVENTH_RANK_BONUS;
            if (phaseValue > 0.5) {
                seventhRankBonus += 10; // Extra bonus in endgame
            }
            score += seventhRankBonus;
        }

        return score;
    }

    /**
     * Check if a file is open (no pawns)
     */
    private boolean isOpenFile(Board board, char file) {
        for (int rank = 1; rank <= 8; rank++) {
            Position pos = new Position(file, rank);
            ChessPiece piece = board.getPieceAt(pos);

            if (piece != null && piece.getType() == ChessPieceType.PAWN) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a file is semi-open (no pawns of a specific color)
     */
    private boolean isSemiOpenFile(Board board, char file, Color color) {
        for (int rank = 1; rank <= 8; rank++) {
            Position pos = new Position(file, rank);
            ChessPiece piece = board.getPieceAt(pos);

            if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                    piece.getColor() == color) {
                return false;
            }
        }

        return !isOpenFile(board, file); // Has enemy pawns but no friendly pawns
    }

    /**
     * Evaluate queen activity
     */
    private int evaluateQueenActivity(Board board, Position pos, Color color, double phaseValue) {
        int score = 0;

        // In opening, penalize early queen development
        if (phaseValue < 0.3) {
            // Check if it's early development
            int homeRank = (color == Color.WHITE) ? 1 : 8;

            if (pos.getRank() != homeRank) {
                // Queen has moved from starting square

                // Check if minor pieces are developed
                boolean minorPiecesDeveloped = areMostMinorPiecesDeveloped(board, color);

                if (!minorPiecesDeveloped) {
                    score -= 15; // Penalty for early queen development
                }
            }
        }

        // In middlegame, centralized queen is good
        if (phaseValue >= 0.3 && phaseValue <= 0.7) {
            int file = pos.getFile() - 'a';
            int rank = pos.getRank() - 1;

            // Central queen is good in middlegame
            if (file >= 2 && file <= 5 && rank >= 2 && rank <= 5) {
                score += 10;
            }
        }

        return score;
    }

    /**
     * Check if most minor pieces are developed
     */
    private boolean areMostMinorPiecesDeveloped(Board board, Color color) {
        int homeRank = (color == Color.WHITE) ? 1 : 8;
        int developedCount = 0;
        int totalMinorPieces = 0;

        // Check knights and bishops
        for (char file = 'a'; file <= 'h'; file++) {
            Position homePos = new Position(file, homeRank);
            ChessPiece piece = board.getPieceAt(homePos);

            if (piece != null && piece.getColor() == color &&
                    (piece.getType() == ChessPieceType.KNIGHT ||
                            piece.getType() == ChessPieceType.BISHOP)) {
                totalMinorPieces++;
                developedCount--; // Piece still on home square is not developed
            }
        }

        // Count minor pieces not on home squares
        for (int rank = 1; rank <= 8; rank++) {
            if (rank == homeRank) continue; // Already counted these above

            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == color &&
                        (piece.getType() == ChessPieceType.KNIGHT ||
                                piece.getType() == ChessPieceType.BISHOP)) {
                    totalMinorPieces++;
                    developedCount++; // Piece not on home square is developed
                }
            }
        }

        // Most pieces are developed if more than half are off home squares
        return developedCount > totalMinorPieces / 2;
    }

    /**
     * Evaluate connected rooks
     */
    private int evaluateConnectedRooks(Board board, Color color) {
        // Find all rooks
        List<Position> rookPositions = new ArrayList<>();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.ROOK &&
                        piece.getColor() == color) {
                    rookPositions.add(pos);
                }
            }
        }

        // Need at least 2 rooks to be connected
        if (rookPositions.size() < 2) {
            return 0;
        }

        // Check if rooks are connected (on same rank with no pieces between)
        for (int i = 0; i < rookPositions.size() - 1; i++) {
            for (int j = i + 1; j < rookPositions.size(); j++) {
                Position rook1 = rookPositions.get(i);
                Position rook2 = rookPositions.get(j);

                // Check if rooks are on same rank
                if (rook1.getRank() == rook2.getRank()) {
                    // Check if path is clear
                    boolean pathClear = true;

                    // Get range of files between rooks
                    char minFile = (char)Math.min(rook1.getFile(), rook2.getFile());
                    char maxFile = (char)Math.max(rook1.getFile(), rook2.getFile());

                    // Check each square between rooks
                    for (char file = (char)(minFile + 1); file < maxFile; file++) {
                        Position pos = new Position(file, rook1.getRank());
                        if (board.getPieceAt(pos) != null) {
                            pathClear = false;
                            break;
                        }
                    }

                    if (pathClear) {
                        return 15; // Bonus for connected rooks
                    }
                }

                // Check if rooks are on same file
                if (rook1.getFile() == rook2.getFile()) {
                    // Check if path is clear
                    boolean pathClear = true;

                    // Get range of ranks between rooks
                    int minRank = Math.min(rook1.getRank(), rook2.getRank());
                    int maxRank = Math.max(rook1.getRank(), rook2.getRank());

                    // Check each square between rooks
                    for (int rank = minRank + 1; rank < maxRank; rank++) {
                        Position pos = new Position(rook1.getFile(), rank);
                        if (board.getPieceAt(pos) != null) {
                            pathClear = false;
                            break;
                        }
                    }

                    if (pathClear) {
                        return 15; // Bonus for connected rooks
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Evaluate piece mobility
     */
    private int evaluatePieceMobility(Board board, Position pos, ChessPiece piece) {
        // Count legal moves as a simplified mobility metric
        int moveCount = countLegalMoves(board, pos, piece);

        // Different piece types get different mobility bonuses
        switch (piece.getType()) {
            case KNIGHT:
                return moveCount * KNIGHT_MOBILITY_BONUS;

            case BISHOP:
                return moveCount * BISHOP_MOBILITY_BONUS;

            case ROOK:
                return moveCount * ROOK_MOBILITY_BONUS;

            case QUEEN:
                return moveCount * QUEEN_MOBILITY_BONUS;

            default:
                return 0;
        }
    }

    /**
     * Count legal moves for a piece (simplified mobility estimation)
     */
    private int countLegalMoves(Board board, Position pos, ChessPiece piece) {
        int moveCount = 0;

        // Different movement patterns for each piece
        switch (piece.getType()) {
            case KNIGHT:
                moveCount = countKnightMoves(board, pos, piece.getColor());
                break;

            case BISHOP:
                moveCount = countBishopMoves(board, pos, piece.getColor());
                break;

            case ROOK:
                moveCount = countRookMoves(board, pos, piece.getColor());
                break;

            case QUEEN:
                moveCount = countBishopMoves(board, pos, piece.getColor()) +
                        countRookMoves(board, pos, piece.getColor());
                break;
        }

        return moveCount;
    }

    /**
     * Count knight moves
     */
    private int countKnightMoves(Board board, Position pos, Color color) {
        int moveCount = 0;
        int file = pos.getFile() - 'a';
        int rank = pos.getRank() - 1;

        // Knight's L-shaped moves
        int[][] offsets = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] offset : offsets) {
            int newFile = file + offset[0];
            int newRank = rank + offset[1];

            // Check if new position is on the board
            if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                Position newPos = new Position((char)('a' + newFile), newRank + 1);
                ChessPiece targetPiece = board.getPieceAt(newPos);

                // Square is empty or has enemy piece
                if (targetPiece == null || targetPiece.getColor() != color) {
                    moveCount++;
                }
            }
        }

        return moveCount;
    }

    /**
     * Count bishop moves
     */
    private int countBishopMoves(Board board, Position pos, Color color) {
        int moveCount = 0;
        int file = pos.getFile() - 'a';
        int rank = pos.getRank() - 1;

        // Bishop's diagonal directions
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            for (int dist = 1; dist < 8; dist++) {
                int newFile = file + dir[0] * dist;
                int newRank = rank + dir[1] * dist;

                // Check if new position is on the board
                if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                    Position newPos = new Position((char)('a' + newFile), newRank + 1);
                    ChessPiece targetPiece = board.getPieceAt(newPos);

                    if (targetPiece == null) {
                        // Empty square
                        moveCount++;
                    } else {
                        // Square has a piece
                        if (targetPiece.getColor() != color) {
                            // Enemy piece (can capture)
                            moveCount++;
                        }

                        // Stop looking in this direction
                        break;
                    }
                } else {
                    // Off the board
                    break;
                }
            }
        }

        return moveCount;
    }

    /**
     * Count rook moves
     */
    private int countRookMoves(Board board, Position pos, Color color) {
        int moveCount = 0;
        int file = pos.getFile() - 'a';
        int rank = pos.getRank() - 1;

        // Rook's horizontal and vertical directions
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            for (int dist = 1; dist < 8; dist++) {
                int newFile = file + dir[0] * dist;
                int newRank = rank + dir[1] * dist;

                // Check if new position is on the board
                if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                    Position newPos = new Position((char)('a' + newFile), newRank + 1);
                    ChessPiece targetPiece = board.getPieceAt(newPos);

                    if (targetPiece == null) {
                        // Empty square
                        moveCount++;
                    } else {
                        // Square has a piece
                        if (targetPiece.getColor() != color) {
                            // Enemy piece (can capture)
                            moveCount++;
                        }

                        // Stop looking in this direction
                        break;
                    }
                } else {
                    // Off the board
                    break;
                }
            }
        }

        return moveCount;
    }
}