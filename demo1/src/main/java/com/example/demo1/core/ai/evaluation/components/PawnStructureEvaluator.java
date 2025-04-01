package com.example.demo1.core.ai.evaluation.components;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;
import java.util.*;

/**
 * Evaluates pawn structure features including doubled pawns,
 * isolated pawns, backward pawns, and passed pawns
 */
public class PawnStructureEvaluator {

    /**
     * Evaluate pawn structure
     *
     * @param board The chess board
     * @param sideToEvaluate Perspective to evaluate from
     * @param phaseValue Game phase value (0.0 = opening, 1.0 = endgame)
     * @return Score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate, double phaseValue) {
        int whiteScore = 0;
        int blackScore = 0;

        // Count pawns in each file
        int[] whitePawnsInFile = new int[8];
        int[] blackPawnsInFile = new int[8];

        // Track the most advanced pawn in each file
        int[] whitePawnRanks = new int[8];
        int[] blackPawnRanks = new int[8];

        // Initialize arrays
        for (int i = 0; i < 8; i++) {
            whitePawnsInFile[i] = 0;
            blackPawnsInFile[i] = 0;
            whitePawnRanks[i] = -1;
            blackPawnRanks[i] = 8;
        }

        // Count pawns and track their positions
        for (int rank = 1; rank <= 8; rank++) {
            for (char fileChar = 'a'; fileChar <= 'h'; fileChar++) {
                Position pos = new Position(fileChar, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN) {
                    int file = fileChar - 'a';
                    if (piece.getColor() == Color.WHITE) {
                        whitePawnsInFile[file]++;
                        whitePawnRanks[file] = Math.max(whitePawnRanks[file], rank);
                    } else {
                        blackPawnsInFile[file]++;
                        blackPawnRanks[file] = Math.min(blackPawnRanks[file], rank);
                    }
                }
            }
        }

        // Adjust penalty/bonus multipliers based on game phase
        double doubledPawnMultiplier = 1.0 + 0.5 * phaseValue; // Gets worse in endgame
        double isolatedPawnMultiplier = 1.0 + 0.7 * phaseValue; // Gets worse in endgame
        double backwardPawnMultiplier = 1.0 + 0.2 * phaseValue; // Slightly worse in endgame
        double passedPawnMultiplier = 1.0 + phaseValue; // Much more important in endgame

        // Evaluate doubled pawns
        for (int file = 0; file < 8; file++) {
            if (whitePawnsInFile[file] > 1) {
                whiteScore += (int)(EvaluationConstants.DOUBLED_PAWN_PENALTY *
                        (whitePawnsInFile[file] - 1) * doubledPawnMultiplier);
            }
            if (blackPawnsInFile[file] > 1) {
                blackScore += (int)(EvaluationConstants.DOUBLED_PAWN_PENALTY *
                        (blackPawnsInFile[file] - 1) * doubledPawnMultiplier);
            }
        }

        // Evaluate isolated and backward pawns
        for (int file = 0; file < 8; file++) {
            // Skip files with no pawns
            if (whitePawnsInFile[file] > 0) {
                // Check for isolated white pawns
                boolean isIsolated = (file == 0 || whitePawnsInFile[file-1] == 0) &&
                        (file == 7 || whitePawnsInFile[file+1] == 0);

                if (isIsolated) {
                    whiteScore += (int)(EvaluationConstants.ISOLATED_PAWN_PENALTY * isolatedPawnMultiplier);
                } else {
                    // Check for backward pawns (only if not isolated)
                    boolean isBackward = false;

                    // Compare with adjacent files
                    if (file > 0 && whitePawnsInFile[file-1] > 0) {
                        if (whitePawnRanks[file-1] > whitePawnRanks[file]) {
                            isBackward = true;
                        }
                    }
                    if (file < 7 && whitePawnsInFile[file+1] > 0) {
                        if (whitePawnRanks[file+1] > whitePawnRanks[file]) {
                            isBackward = true;
                        }
                    }

                    if (isBackward) {
                        whiteScore += (int)(EvaluationConstants.BACKWARD_PAWN_PENALTY * backwardPawnMultiplier);
                    }
                }
            }

            // Same for black pawns
            if (blackPawnsInFile[file] > 0) {
                // Check for isolated black pawns
                boolean isIsolated = (file == 0 || blackPawnsInFile[file-1] == 0) &&
                        (file == 7 || blackPawnsInFile[file+1] == 0);

                if (isIsolated) {
                    blackScore += (int)(EvaluationConstants.ISOLATED_PAWN_PENALTY * isolatedPawnMultiplier);
                } else {
                    // Check for backward pawns (only if not isolated)
                    boolean isBackward = false;

                    // Compare with adjacent files
                    if (file > 0 && blackPawnsInFile[file-1] > 0) {
                        if (blackPawnRanks[file-1] < blackPawnRanks[file]) {
                            isBackward = true;
                        }
                    }
                    if (file < 7 && blackPawnsInFile[file+1] > 0) {
                        if (blackPawnRanks[file+1] < blackPawnRanks[file]) {
                            isBackward = true;
                        }
                    }

                    if (isBackward) {
                        blackScore += (int)(EvaluationConstants.BACKWARD_PAWN_PENALTY * backwardPawnMultiplier);
                    }
                }
            }
        }

        // Evaluate passed pawns
        for (int file = 0; file < 8; file++) {
            // Check for white passed pawns
            if (whitePawnsInFile[file] > 0 && whitePawnRanks[file] > 0) {
                int rank = whitePawnRanks[file];
                boolean isPassed = true;

                // Check if any black pawns can block it
                for (int f = Math.max(0, file-1); f <= Math.min(7, file+1); f++) {
                    if (blackPawnsInFile[f] > 0 && blackPawnRanks[f] > rank) {
                        isPassed = false;
                        break;
                    }
                }

                if (isPassed) {
                    // Base passed pawn bonus
                    int bonus = (int)(EvaluationConstants.PASSED_PAWN_BONUS * passedPawnMultiplier);

                    // Additional bonus based on how advanced the pawn is
                    bonus += (rank - 2) * 10; // Rank 2 is starting position for white

                    // Even bigger bonus for far advanced pawns in endgame
                    if (phaseValue > 0.7 && rank >= 6) {
                        bonus += (rank - 5) * 20;
                    }

                    whiteScore += bonus;
                }
            }

            // Check for black passed pawns
            if (blackPawnsInFile[file] > 0 && blackPawnRanks[file] < 9) {
                int rank = blackPawnRanks[file];
                boolean isPassed = true;

                // Check if any white pawns can block it
                for (int f = Math.max(0, file-1); f <= Math.min(7, file+1); f++) {
                    if (whitePawnsInFile[f] > 0 && whitePawnRanks[f] < rank) {
                        isPassed = false;
                        break;
                    }
                }

                if (isPassed) {
                    // Base passed pawn bonus
                    int bonus = (int)(EvaluationConstants.PASSED_PAWN_BONUS * passedPawnMultiplier);

                    // Additional bonus based on how advanced the pawn is
                    bonus += (7 - rank) * 10; // Rank 7 is starting position for black

                    // Even bigger bonus for far advanced pawns in endgame
                    if (phaseValue > 0.7 && rank <= 3) {
                        bonus += (4 - rank) * 20;
                    }

                    blackScore += bonus;
                }
            }
        }

        // Evaluate pawn chains, pawn islands, etc.
        int whitePawnScore = evaluatePawnChains(whitePawnsInFile, whitePawnRanks, Color.WHITE);
        int blackPawnScore = evaluatePawnChains(blackPawnsInFile, blackPawnRanks, Color.BLACK);

        whiteScore += whitePawnScore;
        blackScore += blackPawnScore;

        // Final score from white's perspective
        int finalScore = whiteScore - blackScore;

        // Return from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ? finalScore : -finalScore;
    }

    /**
     * Evaluate pawn chains, islands, etc.
     */
    private int evaluatePawnChains(int[] pawnsInFile, int[] pawnRanks, Color color) {
        int score = 0;

        // Count pawn islands (groups of connected pawns)
        int islands = 0;
        boolean inIsland = false;

        for (int file = 0; file < 8; file++) {
            if (pawnsInFile[file] > 0) {
                if (!inIsland) {
                    // Start of new island
                    islands++;
                    inIsland = true;
                }
            } else {
                inIsland = false;
            }
        }

        // Penalty for multiple pawn islands
        if (islands > 1) {
            score -= (islands - 1) * 10;
        }

        // Evaluate pawn chains
        for (int file = 0; file < 7; file++) {
            if (pawnsInFile[file] > 0 && pawnsInFile[file + 1] > 0) {
                // Adjacent pawns - check if they form a chain (one protecting the other)
                int leftRank = pawnRanks[file];
                int rightRank = pawnRanks[file + 1];

                if (color == Color.WHITE) {
                    if (leftRank == rightRank - 1 || rightRank == leftRank - 1) {
                        // Pawns that protect each other are good
                        score += 5;
                    }
                } else { // BLACK
                    if (leftRank == rightRank + 1 || rightRank == leftRank + 1) {
                        // Pawns that protect each other are good
                        score += 5;
                    }
                }
            }
        }

        return score;
    }
}