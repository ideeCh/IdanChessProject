package com.example.demo1.core.ai.evaluation;

import com.example.demo1.core.*;
import java.util.*;

/**
 * Specialized evaluator for the endgame phase of a chess game.
 * Focuses on pawn promotion, king activity, and material conversion.
 */
public class EndgameEvaluator extends PhaseBasedEvaluator {

    // Endgame evaluation constants
    private static final int KING_CENTRALIZATION_BONUS = 10;    // Value of the king being in the center
    private static final int KING_OPPOSITION_BONUS = 15;        // Having the opposition in king vs king
    private static final int KING_PROXIMITY_TO_PAWN_BONUS = 5;  // Per rank closer to the enemy's passed pawn
    private static final int KING_PROXIMITY_ASSIST_BONUS = 3;   // Per rank closer to own passed pawn

    // Passed pawn bonuses
    private static final int PASSED_PAWN_BASE_BONUS = 30;      // Base bonus for passed pawn
    private static final int PASSED_PAWN_RANK_BONUS = 10;      // Per rank advanced (additional)
    private static final int PROTECTED_PASSED_PAWN_BONUS = 15;  // Additional if passed pawn is protected
    private static final int OUTSIDE_PASSED_PAWN_BONUS = 20;    // Additional if it's an outside passed pawn

    // Piece value adjustments for endgame
    private static final double KNIGHT_ENDGAME_DEVALUATION = 0.9;  // Knights lose value in open positions
    private static final double BISHOP_ENDGAME_VALUATION = 1.1;    // Bishops gain value in open positions
    private static final double ROOK_ENDGAME_VALUATION = 1.2;      // Rooks more valuable in endgame

    @Override
    public int evaluate(Board board, Color sideToEvaluate) {
        // Adjusted material score for endgame
        int materialScore = evaluateEndgameMaterial(board);

        // King activity and centralization
        int whiteKingScore = evaluateEndgameKing(board, Color.WHITE);
        int blackKingScore = evaluateEndgameKing(board, Color.BLACK);
        int kingScore = whiteKingScore - blackKingScore;

        // Passed pawn evaluation - critical in endgame
        int whitePawnScore = evaluateEndgamePawns(board, Color.WHITE);
        int blackPawnScore = evaluateEndgamePawns(board, Color.BLACK);
        int pawnScore = whitePawnScore - blackPawnScore;

        // Combine all scores
        int totalScore = materialScore + kingScore + pawnScore;

        // Return from the perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ? totalScore : -totalScore;
    }

    /**
     * Evaluate material with adjusted piece values for endgame
     */
    private int evaluateEndgameMaterial(Board board) {
        int whiteScore = 0;
        int blackScore = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                int baseValue = getPieceValue(piece.getType());
                double adjustedValue = baseValue;

                // Adjust values based on piece type for endgame
                switch (piece.getType()) {
                    case KNIGHT:
                        adjustedValue *= KNIGHT_ENDGAME_DEVALUATION;
                        break;
                    case BISHOP:
                        adjustedValue *= BISHOP_ENDGAME_VALUATION;
                        break;
                    case ROOK:
                        adjustedValue *= ROOK_ENDGAME_VALUATION;
                        break;
                }

                if (piece.getColor() == Color.WHITE) {
                    whiteScore += (int)adjustedValue;
                } else {
                    blackScore += (int)adjustedValue;
                }
            }
        }

        return whiteScore - blackScore;
    }

    /**
     * Evaluate king activity and centralization in endgame
     */
    private int evaluateEndgameKing(Board board, Color color) {
        int score = 0;
        Position kingPos = findKing(board, color);

        if (kingPos == null) return 0;

        // King centralization - king should be active in endgame
        score += kingCentralizationScore(kingPos);

        // King proximity to passed pawns (both own and enemy)
        score += evaluateKingProximityToPawns(board, kingPos, color);

        // King opposition evaluation
        score += evaluateKingOpposition(board, kingPos, color);

        return score;
    }

    /**
     * Calculate king centralization score - more central is better in endgame
     */
    private int kingCentralizationScore(Position kingPos) {
        int fileDistance = getCentralizationDistance(kingPos.getFile() - 'a', 3.5);
        int rankDistance = getCentralizationDistance(kingPos.getRank() - 1, 3.5);

        // Maximum distance from center is 3.5 (corner to center)
        int totalDistance = fileDistance + rankDistance;

        // Convert to bonus: max points at center (0 distance), min at corner
        return KING_CENTRALIZATION_BONUS * (7 - totalDistance);
    }

    /**
     * Helper method to calculate distance to the board center
     */
    private int getCentralizationDistance(double coordinate, double center) {
        return (int)Math.abs(coordinate - center);
    }

    /**
     * Evaluate king proximity to passed pawns
     */
    private int evaluateKingProximityToPawns(Board board, Position kingPos, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Find all passed pawns on the board
        List<Position> ownPassedPawns = findPassedPawns(board, color);
        List<Position> enemyPassedPawns = findPassedPawns(board, opponent);

        // King's proximity to enemy passed pawns (to stop them)
        for (Position pawnPos : enemyPassedPawns) {
            int distanceToPromotionSquare = getManhattanDistance(kingPos, getPromotionSquare(pawnPos, opponent));
            score += KING_PROXIMITY_TO_PAWN_BONUS * (14 - distanceToPromotionSquare); // 14 is max Manhattan distance
        }

        // King's proximity to own passed pawns (to assist them)
        for (Position pawnPos : ownPassedPawns) {
            // In endgames, we want king behind our passed pawn supporting its advance
            int distanceToPosition = getManhattanDistance(kingPos, pawnPos);
            score += KING_PROXIMITY_ASSIST_BONUS * (7 - distanceToPosition); // 7 is max relevant distance
        }

        return score;
    }

    /**
     * Calculate Manhattan distance between two positions
     */
    private int getManhattanDistance(Position pos1, Position pos2) {
        return Math.abs(pos1.getFile() - pos2.getFile()) +
                Math.abs(pos1.getRank() - pos2.getRank());
    }

    /**
     * Get the promotion square for a pawn
     */
    private Position getPromotionSquare(Position pawnPos, Color pawnColor) {
        int promotionRank = (pawnColor == Color.WHITE) ? 8 : 1;
        return new Position(pawnPos.getFile(), promotionRank);
    }

    /**
     * Evaluate king opposition (important in king-pawn endgames)
     */
    private int evaluateKingOpposition(Board board, Position kingPos, Color color) {
        int score = 0;
        Position enemyKingPos = findKing(board, (color == Color.WHITE) ? Color.BLACK : Color.WHITE);

        if (enemyKingPos == null) return 0;

        // Check direct opposition on same file or rank
        boolean directOpposition = false;

        // Opposition on a file
        if (kingPos.getFile() == enemyKingPos.getFile()) {
            int rankDiff = Math.abs(kingPos.getRank() - enemyKingPos.getRank());
            if (rankDiff == 2) {
                directOpposition = true;
            }
        }

        // Opposition on a rank
        if (kingPos.getRank() == enemyKingPos.getRank()) {
            int fileDiff = Math.abs(kingPos.getFile() - enemyKingPos.getFile());
            if (fileDiff == 2) {
                directOpposition = true;
            }
        }

        // Diagonal opposition
        int fileDiff = Math.abs(kingPos.getFile() - enemyKingPos.getFile());
        int rankDiff = Math.abs(kingPos.getRank() - enemyKingPos.getRank());
        if (fileDiff == rankDiff && fileDiff == 2) {
            directOpposition = true;
        }

        if (directOpposition) {
            // Bonus for having the opposition (depends on to-move in real games)
            // Here we simplify by giving a small bonus if kings are in opposition
            score += KING_OPPOSITION_BONUS;
        }

        return score;
    }

    /**
     * Find all passed pawns of a specific color
     */
    private List<Position> findPassedPawns(Board board, Color color) {
        List<Position> passedPawns = new ArrayList<>();
        Color enemy = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check each pawn of the specified color
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    // Check if pawn is passed
                    boolean isPassed = true;

                    // Check if there are any enemy pawns that can block it
                    int fileIdx = file - 'a';

                    // Check the pawn's file and adjacent files
                    for (int adjFile = Math.max(0, fileIdx - 1);
                         adjFile <= Math.min(7, fileIdx + 1);
                         adjFile++) {

                        // Check all squares in front of the pawn
                        if (color == Color.WHITE) {
                            for (int r = rank + 1; r <= 8; r++) {
                                Position checkPos = new Position((char)('a' + adjFile), r);
                                ChessPiece obstacle = board.getPieceAt(checkPos);

                                if (obstacle != null &&
                                        obstacle.getType() == ChessPieceType.PAWN &&
                                        obstacle.getColor() == enemy) {
                                    isPassed = false;
                                    break;
                                }
                            }
                        } else { // BLACK
                            for (int r = rank - 1; r >= 1; r--) {
                                Position checkPos = new Position((char)('a' + adjFile), r);
                                ChessPiece obstacle = board.getPieceAt(checkPos);

                                if (obstacle != null &&
                                        obstacle.getType() == ChessPieceType.PAWN &&
                                        obstacle.getColor() == enemy) {
                                    isPassed = false;
                                    break;
                                }
                            }
                        }

                        if (!isPassed) break;
                    }

                    if (isPassed) {
                        passedPawns.add(pos);
                    }
                }
            }
        }

        return passedPawns;
    }

    /**
     * Evaluate pawns in endgame
     */
    private int evaluateEndgamePawns(Board board, Color color) {
        int score = 0;

        // Find all pawns
        List<Position> pawns = new ArrayList<>();
        int[] pawnsPerFile = new int[8];

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    pawns.add(pos);
                    pawnsPerFile[file - 'a']++;
                }
            }
        }

        // Find passed pawns
        List<Position> passedPawns = findPassedPawns(board, color);

        // Evaluate each passed pawn
        for (Position pawnPos : passedPawns) {
            // Base bonus for passed pawns
            score += PASSED_PAWN_BASE_BONUS;

            // Additional bonus based on how advanced the pawn is
            int rank = pawnPos.getRank();
            if (color == Color.WHITE) {
                score += PASSED_PAWN_RANK_BONUS * (rank - 2); // Rank 2 is starting position for white
            } else {
                score += PASSED_PAWN_RANK_BONUS * (7 - rank); // Rank 7 is starting position for black
            }

            // Check if pawn is protected by another pawn
            if (isPawnProtected(board, pawnPos, color)) {
                score += PROTECTED_PASSED_PAWN_BONUS;
            }

            // Check if it's an outside passed pawn
            if (isOutsidePassedPawn(pawnPos, pawnsPerFile)) {
                score += OUTSIDE_PASSED_PAWN_BONUS;
            }

            // Evaluate pawn's path to promotion
            score += evaluatePathToPromotion(board, pawnPos, color);
        }

        // Connected passed pawns are particularly strong
        score += evaluateConnectedPassedPawns(passedPawns, color);

        return score;
    }

    /**
     * Check if a pawn is protected by another pawn
     */
    private boolean isPawnProtected(Board board, Position pawnPos, Color color) {
        int fileIdx = pawnPos.getFile() - 'a';
        int rank = pawnPos.getRank();

        // Check for protecting pawns on adjacent files
        for (int adjFile = Math.max(0, fileIdx - 1); adjFile <= Math.min(7, fileIdx + 1); adjFile += 2) {
            // Skip the pawn's own file
            if (adjFile == fileIdx) continue;

            // For white, protecting pawns must be one rank below
            // For black, protecting pawns must be one rank above
            int protectingRank = (color == Color.WHITE) ? rank - 1 : rank + 1;

            if (protectingRank >= 1 && protectingRank <= 8) {
                Position protectingPos = new Position((char)('a' + adjFile), protectingRank);
                ChessPiece piece = board.getPieceAt(protectingPos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a pawn is an outside passed pawn (far from the center)
     */
    private boolean isOutsidePassedPawn(Position pawnPos, int[] pawnsPerFile) {
        int fileIdx = pawnPos.getFile() - 'a';

        // A pawn is an outside passer if it's on the a, b, g, or h files
        // and there are no other pawns between it and the nearest edge
        if (fileIdx <= 1) {
            // Check if there are no other pawns between this pawn and the a-file
            for (int f = 0; f < fileIdx; f++) {
                if (pawnsPerFile[f] > 0) {
                    return false;
                }
            }
            return true;
        }
        else if (fileIdx >= 6) {
            // Check if there are no other pawns between this pawn and the h-file
            for (int f = fileIdx + 1; f < 8; f++) {
                if (pawnsPerFile[f] > 0) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Evaluate a pawn's path to promotion
     */
    private int evaluatePathToPromotion(Board board, Position pawnPos, Color color) {
        int score = 0;
        int fileIdx = pawnPos.getFile() - 'a';
        int rank = pawnPos.getRank();
        int promotionRank = (color == Color.WHITE) ? 8 : 1;

        // Check the path to promotion
        int step = (color == Color.WHITE) ? 1 : -1;
        boolean pathClear = true;

        for (int r = rank + step; (color == Color.WHITE) ? r <= promotionRank : r >= promotionRank; r += step) {
            Position pathPos = new Position(pawnPos.getFile(), r);
            if (board.getPieceAt(pathPos) != null) {
                pathClear = false;
                break;
            }
        }

        if (pathClear) {
            // Bonus for clear path - more valuable the closer to promotion
            int distanceToPromotion = (color == Color.WHITE) ? promotionRank - rank : rank - promotionRank;
            score += 10 * (8 - distanceToPromotion);
        }

        return score;
    }

    /**
     * Evaluate connected passed pawns
     */
    private int evaluateConnectedPassedPawns(List<Position> passedPawns, Color color) {
        int score = 0;

        // Sort passed pawns by file
        passedPawns.sort(Comparator.comparing(Position::getFile));

        // Check for adjacent passed pawns
        for (int i = 0; i < passedPawns.size() - 1; i++) {
            Position pawn1 = passedPawns.get(i);
            Position pawn2 = passedPawns.get(i + 1);

            // Check if pawns are on adjacent files
            if (pawn2.getFile() - pawn1.getFile() == 1) {
                // Connected passed pawns are especially dangerous
                score += 40;
            }
        }

        return score;
    }
}