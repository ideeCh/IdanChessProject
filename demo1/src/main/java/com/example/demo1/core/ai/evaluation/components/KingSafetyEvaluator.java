package com.example.demo1.core.ai.evaluation.components;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;
import java.util.*;

/**
 * Evaluates king safety in different phases of the game
 */
public class KingSafetyEvaluator {

    // King safety constants
    private static final int KING_ZONE_RADIUS = 2;
    private static final int PAWN_SHIELD_BONUS = 10;
    private static final int OPEN_FILE_NEAR_KING_PENALTY = -25;
    private static final int KING_IN_CENTER_PENALTY = -50;

    // Attacker weights for king safety
    private static final int[] ATTACKER_WEIGHTS = {
            0,    // Not used (0 index placeholder)
            0,    // PAWN
            2,    // KNIGHT
            2,    // BISHOP
            3,    // ROOK
            5,    // QUEEN
            0     // KING
    };

    /**
     * Evaluate king safety
     *
     * @param board The chess board
     * @param sideToEvaluate Perspective to evaluate from
     * @param phaseValue Game phase value (0.0 = opening, 1.0 = endgame)
     * @return Score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate, double phaseValue) {
        // In endgame, king safety is less important than king activity
        if (phaseValue > 0.7) {
            return evaluateKingActivity(board, sideToEvaluate);
        }

        int whiteScore = 0;
        int blackScore = 0;

        // Find king positions
        Position whiteKingPos = findKing(board, Color.WHITE);
        Position blackKingPos = findKing(board, Color.BLACK);

        if (whiteKingPos == null || blackKingPos == null) {
            return 0; // Should never happen in a valid game
        }

        // Check if kings are castled (approximate check)
        boolean whiteKingCastled = isKingCastled(whiteKingPos, Color.WHITE);
        boolean blackKingCastled = isKingCastled(blackKingPos, Color.BLACK);

        // Evaluate pawn shield
        whiteScore += evaluatePawnShield(board, whiteKingPos, Color.WHITE, whiteKingCastled);
        blackScore += evaluatePawnShield(board, blackKingPos, Color.BLACK, blackKingCastled);

        // Evaluate open files near king
        whiteScore += evaluateFilesNearKing(board, whiteKingPos, Color.WHITE);
        blackScore += evaluateFilesNearKing(board, blackKingPos, Color.BLACK);

        // Penalize king in center (especially in opening/middlegame)
        if (isKingInCenter(whiteKingPos)) {
            // Penalty is worse in opening, better in endgame
            double centerPenaltyMultiplier = 1.0 - phaseValue;
            whiteScore += (int)(KING_IN_CENTER_PENALTY * centerPenaltyMultiplier);
        }

        if (isKingInCenter(blackKingPos)) {
            double centerPenaltyMultiplier = 1.0 - phaseValue;
            blackScore += (int)(KING_IN_CENTER_PENALTY * centerPenaltyMultiplier);
        }

        // Evaluate king attack danger (pieces threatening the king area)
        whiteScore += evaluateKingAttackDanger(board, whiteKingPos, Color.WHITE, phaseValue);
        blackScore += evaluateKingAttackDanger(board, blackKingPos, Color.BLACK, phaseValue);

        // Scale king safety by game phase (less important in endgame)
        double safetyScale = 1.0 - phaseValue;
        int finalScore = (int)((whiteScore - blackScore) * safetyScale);

        // Return from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ? finalScore : -finalScore;
    }

    /**
     * Find a king on the board
     */
    private Position findKing(Board board, Color color) {
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

        return null; // Should never happen in a valid game
    }

    /**
     * Check if a king is castled (approximation based on position)
     */
    private boolean isKingCastled(Position kingPos, Color color) {
        // Check typical castled king positions
        int homeRank = (color == Color.WHITE) ? 1 : 8;

        if (kingPos.getRank() == homeRank) {
            char file = kingPos.getFile();

            // Kingside castling
            if (file == 'g') {
                return true;
            }

            // Queenside castling
            if (file == 'c') {
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluate pawn shield in front of the king
     */
    private int evaluatePawnShield(Board board, Position kingPos, Color color, boolean isCastled) {
        int score = 0;

        // If king is not castled, pawn shield is less important
        if (!isCastled) {
            return score;
        }

        int kingFile = kingPos.getFile() - 'a';
        int kingRank = kingPos.getRank();

        // Define shield positions
        List<Position> shieldPositions = new ArrayList<>();

        // Check if king is on kingside or queenside
        boolean isKingside = kingFile >= 4; // King on e, f, g, h files

        if (color == Color.WHITE) {
            // Shield is one rank ahead of king
            int shieldRank = kingRank + 1;

            // Check the three files in front of king (or fewer if on edge)
            int startFile = Math.max(0, kingFile - 1);
            int endFile = Math.min(7, kingFile + 1);

            for (int file = startFile; file <= endFile; file++) {
                shieldPositions.add(new Position((char)('a' + file), shieldRank));
            }

            // For castled kings, add second rank of shield
            if (kingRank == 1) {
                for (int file = startFile; file <= endFile; file++) {
                    shieldPositions.add(new Position((char)('a' + file), shieldRank + 1));
                }
            }
        } else {
            // Shield for black is one rank below king
            int shieldRank = kingRank - 1;

            // Check the three files in front of king (or fewer if on edge)
            int startFile = Math.max(0, kingFile - 1);
            int endFile = Math.min(7, kingFile + 1);

            for (int file = startFile; file <= endFile; file++) {
                shieldPositions.add(new Position((char)('a' + file), shieldRank));
            }

            // For castled kings, add second rank of shield
            if (kingRank == 8) {
                for (int file = startFile; file <= endFile; file++) {
                    shieldPositions.add(new Position((char)('a' + file), shieldRank - 1));
                }
            }
        }

        // Count pawns in shield positions
        for (Position pos : shieldPositions) {
            ChessPiece piece = board.getPieceAt(pos);
            if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                    piece.getColor() == color) {
                score += PAWN_SHIELD_BONUS;
            } else {
                // Penalty for missing shield pawn
                score -= PAWN_SHIELD_BONUS / 2;
            }
        }

        return score;
    }

    /**
     * Evaluate open files near the king
     */
    private int evaluateFilesNearKing(Board board, Position kingPos, Color color) {
        int score = 0;
        char kingFile = kingPos.getFile();

        // Check files around king
        for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
            char file = (char)(kingFile + fileOffset);

            // Skip invalid files
            if (file < 'a' || file > 'h') continue;

            // Check if file is open or semi-open
            boolean isOpenFile = isOpenFile(board, file);
            boolean isSemiOpenFile = !isOpenFile && isSemiOpenFile(board, file, color);

            // Penalty for open files near king
            if (isOpenFile) {
                score += OPEN_FILE_NEAR_KING_PENALTY;
            }
            // Lesser penalty for semi-open files
            else if (isSemiOpenFile) {
                score += OPEN_FILE_NEAR_KING_PENALTY / 2;
            }
        }

        return score;
    }

    /**
     * Check if a file is completely open (no pawns)
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

        return true;
    }

    /**
     * Check if king is in the center (vulnerable in middlegame)
     */
    private boolean isKingInCenter(Position kingPos) {
        int file = kingPos.getFile() - 'a';
        int rank = kingPos.getRank() - 1;

        // Center defined as d4-e5 and surrounding squares
        return (file >= 2 && file <= 5 && rank >= 2 && rank <= 5);
    }

    /**
     * Evaluate the danger from enemy pieces attacking king's zone
     */
    private int evaluateKingAttackDanger(Board board, Position kingPos, Color kingColor, double phaseValue) {
        int score = 0;
        int kingFile = kingPos.getFile() - 'a';
        int kingRank = kingPos.getRank() - 1;
        Color enemyColor = (kingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Define king zone - all squares around king
        boolean[][] kingZone = new boolean[8][8];

        // Mark king zone squares
        for (int fileOffset = -KING_ZONE_RADIUS; fileOffset <= KING_ZONE_RADIUS; fileOffset++) {
            for (int rankOffset = -KING_ZONE_RADIUS; rankOffset <= KING_ZONE_RADIUS; rankOffset++) {
                int zoneFile = kingFile + fileOffset;
                int zoneRank = kingRank + rankOffset;

                if (zoneFile >= 0 && zoneFile < 8 && zoneRank >= 0 && zoneRank < 8) {
                    kingZone[zoneFile][zoneRank] = true;
                }
            }
        }

        // Count attackers and attack value
        int attackCount = 0;
        int attackValue = 0;

        // Check each enemy piece
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null || piece.getColor() != enemyColor) {
                    continue;
                }

                // Check if piece attacks king zone
                if (attacksKingZone(board, pos, piece, kingZone)) {
                    attackCount++;

                    // Different piece types contribute different danger values
                    int pieceTypeIndex = getPieceTypeIndex(piece.getType());
                    attackValue += ATTACKER_WEIGHTS[pieceTypeIndex];
                }
            }
        }

        // Calculate attack danger - increases exponentially with more attackers
        // This is a critical concept in king safety evaluation
        if (attackCount >= 2) {
            int attackDanger = -attackValue * attackCount;

            // Scale attack value - more dangerous in middlegame than endgame
            double dangerScale = 1.0 - (phaseValue * 0.7);

            score += (int)(attackDanger * dangerScale);
        }

        return score;
    }

    /**
     * Check if a piece attacks any square in the king zone
     */
    private boolean attacksKingZone(Board board, Position piecePos, ChessPiece piece, boolean[][] kingZone) {
        int pieceFile = piecePos.getFile() - 'a';
        int pieceRank = piecePos.getRank() - 1;

        // Simplistic approach - just check if piece is in zone
        if (kingZone[pieceFile][pieceRank]) {
            return true;
        }

        // Check if the piece could attack into the zone
        // This is a simplified implementation - a real one would check attack patterns

        // For distant pieces, just see if they're aligned with king zone
        switch (piece.getType()) {
            case QUEEN:
            case ROOK:
            case BISHOP:
                // Check if piece could potentially attack king zone squares
                for (int file = 0; file < 8; file++) {
                    for (int rank = 0; rank < 8; rank++) {
                        if (kingZone[file][rank]) {
                            Position zonePos = new Position((char)('a' + file), rank + 1);
                            if (canPieceAttackSquare(board, piecePos, piece, zonePos)) {
                                return true;
                            }
                        }
                    }
                }
                break;

            case KNIGHT:
                // Check knight's potential moves
                int[][] knightOffsets = {
                        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                        {1, -2}, {1, 2}, {2, -1}, {2, 1}
                };

                for (int[] offset : knightOffsets) {
                    int targetFile = pieceFile + offset[0];
                    int targetRank = pieceRank + offset[1];

                    if (targetFile >= 0 && targetFile < 8 && targetRank >= 0 && targetRank < 8) {
                        if (kingZone[targetFile][targetRank]) {
                            return true;
                        }
                    }
                }
                break;
        }

        return false;
    }

    /**
     * Check if a piece can attack a specific square
     */
    private boolean canPieceAttackSquare(Board board, Position piecePos, ChessPiece piece, Position targetPos) {
        // Get direction vectors from piece to target
        int dx = targetPos.getFile() - piecePos.getFile();
        int dy = targetPos.getRank() - piecePos.getRank();

        // Normalize direction to get step vector
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        // Different piece types have different attack patterns
        switch (piece.getType()) {
            case QUEEN:
                // Queens can move like rooks or bishops
                return (canRookAttack(dx, dy) || canBishopAttack(dx, dy)) &&
                        pathIsClear(board, piecePos, targetPos, stepX, stepY);

            case ROOK:
                return canRookAttack(dx, dy) && pathIsClear(board, piecePos, targetPos, stepX, stepY);

            case BISHOP:
                return canBishopAttack(dx, dy) && pathIsClear(board, piecePos, targetPos, stepX, stepY);

            case KNIGHT:
                // Knight's L-shaped moves - no need to check for blockers
                return (Math.abs(dx) == 1 && Math.abs(dy) == 2) ||
                        (Math.abs(dx) == 2 && Math.abs(dy) == 1);

            case PAWN:
                // Pawns capture diagonally
                int forward = (piece.getColor() == Color.WHITE) ? 1 : -1;
                return dy == forward && Math.abs(dx) == 1;

            default:
                return false;
        }
    }

    /**
     * Check if the path between two positions is clear
     */
    private boolean pathIsClear(Board board, Position from, Position to, int stepX, int stepY) {
        int currentX = from.getFile() - 'a' + stepX;
        int currentY = from.getRank() - 1 + stepY;
        int targetX = to.getFile() - 'a';
        int targetY = to.getRank() - 1;

        while (currentX != targetX || currentY != targetY) {
            Position currentPos = new Position((char)('a' + currentX), currentY + 1);
            if (board.getPieceAt(currentPos) != null) {
                return false; // Path is blocked
            }

            currentX += stepX;
            currentY += stepY;
        }

        return true;
    }

    /**
     * Check if a rook could attack along these offsets
     */
    private boolean canRookAttack(int dx, int dy) {
        return dx == 0 || dy == 0;
    }

    /**
     * Check if a bishop could attack along these offsets
     */
    private boolean canBishopAttack(int dx, int dy) {
        return Math.abs(dx) == Math.abs(dy);
    }

    /**
     * Convert piece type to array index
     */
    private int getPieceTypeIndex(ChessPieceType type) {
        switch (type) {
            case PAWN: return 1;
            case KNIGHT: return 2;
            case BISHOP: return 3;
            case ROOK: return 4;
            case QUEEN: return 5;
            case KING: return 6;
            default: return 0;
        }
    }

    /**
     * Evaluate king activity (for endgame)
     */
    private int evaluateKingActivity(Board board, Color sideToEvaluate) {
        int whiteScore = 0;
        int blackScore = 0;

        Position whiteKingPos = findKing(board, Color.WHITE);
        Position blackKingPos = findKing(board, Color.BLACK);

        if (whiteKingPos != null) {
            // In endgame, king centralization is good
            whiteScore += evaluateKingCentralization(whiteKingPos);

            // King proximity to passed pawns
            whiteScore += evaluateKingProximityToPawns(board, whiteKingPos, Color.WHITE);
        }

        if (blackKingPos != null) {
            // King centralization
            blackScore += evaluateKingCentralization(blackKingPos);

            // King proximity to passed pawns
            blackScore += evaluateKingProximityToPawns(board, blackKingPos, Color.BLACK);
        }

        // Return from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ?
                (whiteScore - blackScore) : (blackScore - whiteScore);
    }

    /**
     * Calculate king centralization score
     */
    private int evaluateKingCentralization(Position kingPos) {
        int fileDistance = getCentralizationDistance(kingPos.getFile() - 'a', 3.5);
        int rankDistance = getCentralizationDistance(kingPos.getRank() - 1, 3.5);

        // Maximum distance from center is 7 (corner to center)
        int totalDistance = fileDistance + rankDistance;
        int maxDistance = 7;

        // Bonus is proportional to how close the king is to the center
        return (maxDistance - totalDistance) * 5;
    }

    /**
     * Helper to calculate distance to center
     */
    private int getCentralizationDistance(double coordinate, double center) {
        return (int)Math.abs(coordinate - center);
    }

    /**
     * Evaluate king proximity to passed pawns
     */
    private int evaluateKingProximityToPawns(Board board, Position kingPos, Color kingColor) {
        int score = 0;
        Color enemyColor = (kingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Find passed pawns of both colors
        List<Position> ownPassedPawns = findPassedPawns(board, kingColor);
        List<Position> enemyPassedPawns = findPassedPawns(board, enemyColor);

        // King closeness to enemy passed pawns is good (to stop them)
        for (Position pawnPos : enemyPassedPawns) {
            int distance = calculateDistance(kingPos, pawnPos);
            score += (14 - distance) * 3; // 14 is max possible distance
        }

        // King closeness to own passed pawns depends on how advanced they are
        for (Position pawnPos : ownPassedPawns) {
            // For far advanced pawns, king shouldn't block them
            int pawnRank = pawnPos.getRank();
            boolean farAdvanced = (kingColor == Color.WHITE && pawnRank >= 6) ||
                    (kingColor == Color.BLACK && pawnRank <= 3);

            int distance = calculateDistance(kingPos, pawnPos);

            if (farAdvanced) {
                // For far advanced pawns, king should not be too close
                if (distance <= 2) {
                    score -= 5; // Penalty for blocking own pawn
                }
            } else {
                // For less advanced pawns, king should support them
                score += (7 - distance) * 2;
            }
        }

        return score;
    }

    /**
     * Find all passed pawns of a specific color
     */
    private List<Position> findPassedPawns(Board board, Color color) {
        List<Position> passedPawns = new ArrayList<>();

        // Check all pawns of given color
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    // Check if pawn is passed
                    if (isPassed(board, pos, color)) {
                        passedPawns.add(pos);
                    }
                }
            }
        }

        return passedPawns;
    }

    /**
     * Check if a pawn is passed
     */
    private boolean isPassed(Board board, Position pawnPos, Color pawnColor) {
        int file = pawnPos.getFile() - 'a';
        int rank = pawnPos.getRank();
        Color enemyColor = (pawnColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check all files that could block (same and adjacent)
        for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
            // Check all relevant ranks ahead
            if (pawnColor == Color.WHITE) {
                // White pawns move up in rank
                for (int r = rank + 1; r <= 8; r++) {
                    Position pos = new Position((char)('a' + f), r);
                    ChessPiece piece = board.getPieceAt(pos);

                    if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                            piece.getColor() == enemyColor) {
                        return false; // Not passed
                    }
                }
            } else {
                // Black pawns move down in rank
                for (int r = rank - 1; r >= 1; r--) {
                    Position pos = new Position((char)('a' + f), r);
                    ChessPiece piece = board.getPieceAt(pos);

                    if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                            piece.getColor() == enemyColor) {
                        return false; // Not passed
                    }
                }
            }
        }

        return true; // Passed pawn
    }

    /**
     * Calculate Manhattan distance between two positions
     */
    private int calculateDistance(Position pos1, Position pos2) {
        return Math.abs(pos1.getFile() - pos2.getFile()) +
                Math.abs(pos1.getRank() - pos2.getRank());
    }
}