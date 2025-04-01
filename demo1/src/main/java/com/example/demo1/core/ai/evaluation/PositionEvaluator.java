package com.example.demo1.core.ai.evaluation;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.GamePhase;
import com.example.demo1.core.ai.evaluation.components.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;

/**
 * Main evaluator class that combines all evaluation components
 * and applies phase-specific weights
 */
public class PositionEvaluator {
    // Evaluation components
    private final MaterialEvaluator materialEvaluator;
    private final PawnStructureEvaluator pawnStructureEvaluator;
    private final KingSafetyEvaluator kingSafetyEvaluator;
    private final PieceActivityEvaluator pieceActivityEvaluator;
    private final TacticalEvaluator tacticalEvaluator;
    private final PieceSquareTable pieceSquareTable;

    // Phase-specific evaluators
    private final OpeningEvaluator openingEvaluator;
    private final MiddlegameEvaluator middlegameEvaluator;
    private final EndgameEvaluator endgameEvaluator;

    /**
     * Create a new position evaluator with all components
     */
    public PositionEvaluator() {
        this.materialEvaluator = new MaterialEvaluator();
        this.pawnStructureEvaluator = new PawnStructureEvaluator();
        this.kingSafetyEvaluator = new KingSafetyEvaluator();
        this.pieceActivityEvaluator = new PieceActivityEvaluator();
        this.tacticalEvaluator = new TacticalEvaluator();
        this.pieceSquareTable = new PieceSquareTable();

        this.openingEvaluator = new OpeningEvaluator();
        this.middlegameEvaluator = new MiddlegameEvaluator();
        this.endgameEvaluator = new EndgameEvaluator();
    }

    /**
     * Evaluate a chess position
     *
     * @param board The chess board to evaluate
     * @param sideToEvaluate The perspective to evaluate from
     * @return Score in centipawns (positive is good for the side to evaluate)
     */
    public int evaluate(Board board, Color sideToEvaluate) {
        // Calculate phase
        double phaseValue = calculatePhaseValue(board);
        GamePhase gamePhase = determineGamePhase(phaseValue);

        // Component scores
        int materialScore = materialEvaluator.evaluate(board, sideToEvaluate);
        int pawnStructureScore = pawnStructureEvaluator.evaluate(board, sideToEvaluate, phaseValue);
        int kingSafetyScore = kingSafetyEvaluator.evaluate(board, sideToEvaluate, phaseValue);
        int pieceActivityScore = pieceActivityEvaluator.evaluate(board, sideToEvaluate, phaseValue);
        int tacticalScore = tacticalEvaluator.evaluate(board, sideToEvaluate);
        int pieceSquareScore = pieceSquareTable.evaluate(board, sideToEvaluate, phaseValue);

        // Phase-specific evaluation
        int phaseSpecificScore = 0;
        if (gamePhase == GamePhase.OPENING) {
            phaseSpecificScore = openingEvaluator.evaluate(board, sideToEvaluate);
        } else if (gamePhase == GamePhase.MIDDLEGAME) {
            phaseSpecificScore = middlegameEvaluator.evaluate(board, sideToEvaluate);
        } else {
            phaseSpecificScore = endgameEvaluator.evaluate(board, sideToEvaluate);
        }
        // Combine component scores with phase-appropriate weights
        int totalScore = materialScore;

        // Apply variable weights to components based on phase
        if (phaseValue < 0.3) {
            // Opening weights
            totalScore += pawnStructureScore * 0.7;
            totalScore += kingSafetyScore * 1.2;
            totalScore += pieceActivityScore * 1.3;
            totalScore += tacticalScore * 0.6;
            totalScore += pieceSquareScore * 1.5;
            totalScore += phaseSpecificScore;
        }
        else if (phaseValue < 0.7) {
            // Middlegame weights
            totalScore += pawnStructureScore;
            totalScore += kingSafetyScore * 1.5;
            totalScore += pieceActivityScore * 1.5;
            totalScore += tacticalScore;
            totalScore += pieceSquareScore;
            totalScore += phaseSpecificScore;
        }
        else {
            // Endgame weights
            totalScore += pawnStructureScore * 1.5;
            totalScore += kingSafetyScore * 0.5;
            totalScore += pieceActivityScore;
            totalScore += tacticalScore * 0.8;
            totalScore += pieceSquareScore * 0.7;
            totalScore += phaseSpecificScore * 1.5;
        }

        return totalScore;
    }

    /**
     * Calculate the game phase based on material remaining
     *
     * @param board The chess board
     * @return Value between 0.0 (opening) and 1.0 (endgame)
     */
    private double calculatePhaseValue(Board board) {
        int totalMaterial = 0;
        final int maxMaterial = 7800; // Total material at start minus kings

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() != ChessPieceType.KING) {
                    totalMaterial += getMaterialValue(piece.getType());
                }
            }
        }

        // Convert to a 0-1 scale where 0 is opening, 1 is endgame
        double phaseValue = 1.0 - (totalMaterial / (double)maxMaterial);

        // Ensure the value is between 0 and 1
        return Math.max(0.0, Math.min(1.0, phaseValue));
    }

    /**
     * Determine the discrete game phase from a continuous phase value
     *
     * @param phaseValue Phase value between 0.0 and 1.0
     * @return GamePhase enum value
     */
    private GamePhase determineGamePhase(double phaseValue) {
        if (phaseValue < 0.3) {
            return GamePhase.OPENING;
        } else if (phaseValue < 0.7) {
            return GamePhase.MIDDLEGAME;
        } else {
            return GamePhase.ENDGAME;
        }
    }

    /**
     * Get the material value of a piece type
     *
     * @param type The chess piece type
     * @return Value in centipawns
     */
    private int getMaterialValue(ChessPieceType type) {
            if (type == ChessPieceType.PAWN) return EvaluationConstants.PAWN_VALUE;
            if (type == ChessPieceType.KNIGHT) return EvaluationConstants.KNIGHT_VALUE;
            if (type == ChessPieceType.BISHOP) return EvaluationConstants.BISHOP_VALUE;
            if (type == ChessPieceType.ROOK) return EvaluationConstants.ROOK_VALUE;
            if (type == ChessPieceType.QUEEN) return EvaluationConstants.QUEEN_VALUE;
            return 0;
    }
}