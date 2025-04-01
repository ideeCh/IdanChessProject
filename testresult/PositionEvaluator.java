package com.example.demo1.core.ai;

import com.example.demo1.core.*;
import java.util.*;

/**
 * Evaluates chess positions using comprehensive static heuristics
 */
public class PositionEvaluator {
    // Standard piece values in centipawns
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    // Bonuses and penalties
    private static final int BISHOP_PAIR_BONUS = 50;
    private static final int DOUBLED_PAWN_PENALTY = -25;
    private static final int ISOLATED_PAWN_PENALTY = -20;
    private static final int BACKWARD_PAWN_PENALTY = -15;
    private static final int PASSED_PAWN_BONUS = 20; // Base value, increases with rank
    private static final int ROOK_OPEN_FILE_BONUS = 15;
    private static final int ROOK_SEMI_OPEN_FILE_BONUS = 7;
    private static final int ROOK_SEVENTH_RANK_BONUS = 20;
    private static final int KNIGHT_OUTPOST_BONUS = 30;
    private static final int PAWN_SHIELD_BONUS = 10; // Per pawn shielding the king

    /**
     * Evaluates a position from the perspective of the given color
     *
     * @param board          The current board state
     * @param sideToEvaluate The color from whose perspective to evaluate
     * @return Score in centipawns (positive is good for the given color)
     */
    public int evaluate(Board board, Color sideToEvaluate) {
        // Get the game phase
        double gamePhase = calculateGamePhase(board);

        // Calculate scores for different components
        int materialScore = evaluateMaterial(board);
        int pawnStructureScore = evaluatePawnStructure(board);
        int pieceActivityScore = evaluatePieceActivity(board, gamePhase);
        int kingSafetyScore = evaluateKingSafety(board, gamePhase);

        // Combine all components
        int totalScore = materialScore + pawnStructureScore + pieceActivityScore + kingSafetyScore;

        // Return from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ? totalScore : -totalScore;
    }

    /**
     * Calculates the game phase based on remaining material
     *
     * @return Value between 0 (opening) and 1 (endgame)
     */
    private double calculateGamePhase(Board board) {
        int piecesRemaining = 0;
        int maxPieces = 32; // Total pieces at start

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                if (board.getPieceAt(pos) != null) {
                    piecesRemaining++;
                }
            }
        }

        // Convert to a 0-1 scale where 0 is opening, 1 is endgame
        double phase = 1.0 - (piecesRemaining / (double) maxPieces);

        // Adjust the scale - middle game is around 0.3-0.7
        return Math.min(1.0, Math.max(0.0, phase));
    }

    /**
     * Evaluates material balance
     */
    private int evaluateMaterial(Board board) {
        int whiteMaterial = 0;
        int blackMaterial = 0;

        int whiteNumBishops = 0;
        int blackNumBishops = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                int value = getPieceValue(piece.getType());

                if (piece.getColor() == Color.WHITE) {
                    whiteMaterial += value;
                    if (piece.getType() == ChessPieceType.BISHOP) {
                        whiteNumBishops++;
                    }
                } else {
                    blackMaterial += value;
                    if (piece.getType() == ChessPieceType.BISHOP) {
                        blackNumBishops++;
                    }
                }
            }
        }

        // Add bishop pair bonus
        if (whiteNumBishops >= 2) whiteMaterial += BISHOP_PAIR_BONUS;
        if (blackNumBishops >= 2) blackMaterial += BISHOP_PAIR_BONUS;

        return whiteMaterial - blackMaterial;
    }

    /**
     * Gets the standard value of a piece type
     */
    private int getPieceValue(ChessPieceType type) {
        switch (type) {
            case PAWN:
                return PAWN_VALUE;
            case KNIGHT:
                return KNIGHT_VALUE;
            case BISHOP:
                return BISHOP_VALUE;
            case ROOK:
                return ROOK_VALUE;
            case QUEEN:
                return QUEEN_VALUE;
            case KING:
                return 0; // King's value is not counted in material
            default:
                return 0;
        }
    }

    /**
     * Evaluates pawn structure
     */
    private int evaluatePawnStructure(Board board) {
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

        // Evaluate doubled pawns
        for (int file = 0; file < 8; file++) {
            if (whitePawnsInFile[file] > 1) {
                whiteScore += DOUBLED_PAWN_PENALTY * (whitePawnsInFile[file] - 1);
            }
            if (blackPawnsInFile[file] > 1) {
                blackScore += DOUBLED_PAWN_PENALTY * (blackPawnsInFile[file] - 1);
            }
        }

        // Evaluate isolated and backward pawns
        for (int file = 0; file < 8; file++) {
            boolean whiteHasPawn = whitePawnsInFile[file] > 0;
            boolean blackHasPawn = blackPawnsInFile[file] > 0;

            // Check for isolated pawns (no friendly pawns on adjacent files)
            boolean whiteIsolated = whiteHasPawn &&
                    (file == 0 || whitePawnsInFile[file - 1] == 0) &&
                    (file == 7 || whitePawnsInFile[file + 1] == 0);

            boolean blackIsolated = blackHasPawn &&
                    (file == 0 || blackPawnsInFile[file - 1] == 0) &&
                    (file == 7 || blackPawnsInFile[file + 1] == 0);

            if (whiteIsolated) whiteScore += ISOLATED_PAWN_PENALTY;
            if (blackIsolated) blackScore += ISOLATED_PAWN_PENALTY;

            // Evaluate backward pawns (simplified)
            boolean whiteBackward = whiteHasPawn && !whiteIsolated &&
                    ((file > 0 && whitePawnRanks[file - 1] > whitePawnRanks[file]) ||
                            (file < 7 && whitePawnRanks[file + 1] > whitePawnRanks[file]));

            boolean blackBackward = blackHasPawn && !blackIsolated &&
                    ((file > 0 && blackPawnRanks[file - 1] < blackPawnRanks[file]) ||
                            (file < 7 && blackPawnRanks[file + 1] < blackPawnRanks[file]));

            if (whiteBackward) whiteScore += BACKWARD_PAWN_PENALTY;
            if (blackBackward) blackScore += BACKWARD_PAWN_PENALTY;
        }

        // Evaluate passed pawns
        for (int file = 0; file < 8; file++) {
            // Check for white passed pawns
            if (whitePawnsInFile[file] > 0 && whitePawnRanks[file] > 0) {
                int rank = whitePawnRanks[file];
                boolean isPassed = true;

                // Check if any black pawns can block it
                for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
                    if (blackPawnsInFile[f] > 0 && blackPawnRanks[f] > rank) {
                        isPassed = false;
                        break;
                    }
                }

                if (isPassed) {
                    // More valuable in endgame and as they advance
                    int bonus = PASSED_PAWN_BONUS + (rank * 10);
                    whiteScore += bonus;
                }
            }

            // Check for black passed pawns
            if (blackPawnsInFile[file] > 0 && blackPawnRanks[file] < 9) {
                int rank = blackPawnRanks[file];
                boolean isPassed = true;

                // Check if any white pawns can block it
                for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
                    if (whitePawnsInFile[f] > 0 && whitePawnRanks[f] < rank) {
                        isPassed = false;
                        break;
                    }
                }

                if (isPassed) {
                    // More valuable in endgame and as they advance
                    int bonus = PASSED_PAWN_BONUS + ((9 - rank) * 10); // 9-rank because rank 1 is most advanced for black
                    blackScore += bonus;
                }
            }
        }

        return whiteScore - blackScore;
    }

    /**
     * Evaluates piece activity
     */
    private int evaluatePieceActivity(Board board, double gamePhase) {
        int whiteScore = 0;
        int blackScore = 0;

        // This is a simplified implementation focusing on piece placement

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null) continue;

                int squareValue = evaluatePieceSquare(piece.getType(), pos, piece.getColor(), gamePhase);

                if (piece.getColor() == Color.WHITE) {
                    whiteScore += squareValue;
                } else {
                    blackScore += squareValue;
                }

                // Special piece placement bonuses
                if (piece.getType() == ChessPieceType.ROOK) {
                    // Rook on open file
                    if (isOpenFile(board, file)) {
                        if (piece.getColor() == Color.WHITE) {
                            whiteScore += ROOK_OPEN_FILE_BONUS;
                        } else {
                            blackScore += ROOK_OPEN_FILE_BONUS;
                        }
                    }
                    // Rook on semi-open file
                    else if (isSemiOpenFile(board, file, piece.getColor())) {
                        if (piece.getColor() == Color.WHITE) {
                            whiteScore += ROOK_SEMI_OPEN_FILE_BONUS;
                        } else {
                            blackScore += ROOK_SEMI_OPEN_FILE_BONUS;
                        }
                    }

                    // Rook on 7th rank (2nd rank for black)
                    if ((piece.getColor() == Color.WHITE && rank == 7) ||
                            (piece.getColor() == Color.BLACK && rank == 2)) {
                        if (piece.getColor() == Color.WHITE) {
                            whiteScore += ROOK_SEVENTH_RANK_BONUS;
                        } else {
                            blackScore += ROOK_SEVENTH_RANK_BONUS;
                        }
                    }
                }

                // Knight outposts
                if (piece.getType() == ChessPieceType.KNIGHT) {
                    if (isKnightOutpost(board, pos, piece.getColor())) {
                        if (piece.getColor() == Color.WHITE) {
                            whiteScore += KNIGHT_OUTPOST_BONUS;
                        } else {
                            blackScore += KNIGHT_OUTPOST_BONUS;
                        }
                    }
                }
            }
        }

        return whiteScore - blackScore;
    }

    /**
     * Evaluates a piece's placement using simplified piece-square tables
     */
    private int evaluatePieceSquare(ChessPieceType type, Position pos, Color color, double gamePhase) {
        int file = pos.getFile() - 'a';
        int rank = pos.getRank() - 1; // Convert to 0-7 for array indexing

        // For black pieces, flip the rank
        if (color == Color.BLACK) {
            rank = 7 - rank;
        }

        // Center control bonus
        int centerBonus = 0;
        if (file >= 3 && file <= 4 && rank >= 3 && rank <= 4) {
            centerBonus = 10; // Strong center
        } else if (file >= 2 && file <= 5 && rank >= 2 && rank <= 5) {
            centerBonus = 5;  // Extended center
        }

        // Opening/middlegame piece values
        int openingValue = 0;

        switch (type) {
            case PAWN:
                // Pawns get better as they advance
                openingValue = rank * 5;

                // Center pawns are good
                if (file == 3 || file == 4) {
                    openingValue += 5;
                }
                break;

            case KNIGHT:
                // Knights are best in center, bad on rim
                openingValue = centerBonus;

                // Knights on rim are dim
                if (file == 0 || file == 7 || rank == 0 || rank == 7) {
                    openingValue -= 20;
                }
                break;

            case BISHOP:
                // Bishops like diagonals and extended center
                openingValue = centerBonus;
                break;

            case ROOK:
                // Rooks like open files and 7th rank
                openingValue = 0; // Neutral in opening
                break;

            case QUEEN:
                // Queen likes safety in opening, activity in middlegame
                openingValue = centerBonus / 2; // Less central in opening
                break;

            case KING:
                // King wants safety in opening/middlegame
                openingValue = -centerBonus; // Penalize center play

                // Castled position bonus
                if (rank == 0 && (file <= 2 || file >= 6)) {
                    openingValue += 20;
                }
                break;
        }

        // Endgame piece values
        int endgameValue = 0;

        switch (type) {
            case PAWN:
                // Passed pawns already handled elsewhere
                endgameValue = rank * 10; // Advancing is critical
                break;

            case KNIGHT:
                // Less valuable but still needs centralization
                endgameValue = centerBonus;
                break;

            case BISHOP:
                // Good in open positions
                endgameValue = centerBonus + 5;
                break;

            case ROOK:
                // More active in endgame
                endgameValue = 5;
                break;

            case QUEEN:
                // Queen centralization
                endgameValue = centerBonus;
                break;

            case KING:
                // King should be active in endgame
                endgameValue = centerBonus * 2;
                break;
        }

        // Interpolate between opening and endgame values
        return (int) ((1 - gamePhase) * openingValue + gamePhase * endgameValue);
    }

    /**
     * Evaluates king safety
     */
    private int evaluateKingSafety(Board board, double gamePhase) {
        // In endgame, king safety is less important than king activity
        if (gamePhase > 0.7) { // Deep endgame
            return evaluateKingActivity(board);
        }

        int whiteScore = 0;
        int blackScore = 0;

        // Find king positions
        Position whiteKingPos = findKing(board, Color.WHITE);
        Position blackKingPos = findKing(board, Color.BLACK);

        if (whiteKingPos != null) {
            // Evaluate pawn shield
            whiteScore += evaluatePawnShield(board, whiteKingPos, Color.WHITE);
        }

        if (blackKingPos != null) {
            blackScore += evaluatePawnShield(board, blackKingPos, Color.BLACK);
        }

        // Scale king safety by game phase (less important in endgame)
        double safetyScale = 1.0 - gamePhase;
        return (int) ((whiteScore - blackScore) * safetyScale);
    }

    /**
     * Evaluates king activity (for endgame)
     */
    private int evaluateKingActivity(Board board) {
        int whiteScore = 0;
        int blackScore = 0;

        Position whiteKingPos = findKing(board, Color.WHITE);
        Position blackKingPos = findKing(board, Color.BLACK);

        if (whiteKingPos != null) {
            // King centralization bonus
            whiteScore += evaluateKingCentralization(whiteKingPos);
        }

        if (blackKingPos != null) {
            blackScore += evaluateKingCentralization(blackKingPos);
        }

        return whiteScore - blackScore;
    }

    /**
     * Evaluates king centralization (for endgame)
     */
    private int evaluateKingCentralization(Position kingPos) {
        // Distance from center (smaller is better)
        int centerFile = 4; // Between 'd' (4) and 'e' (5)
        int centerRank = 4; // Between ranks 4 and 5

        int fileValue = kingPos.getFile() - 'a' + 1;
        int fileDistance = Math.abs(fileValue - centerFile);
        int rankDistance = Math.abs(kingPos.getRank() - centerRank);

        int distFromCenter = fileDistance + rankDistance;

        // Give more points for being closer to center
        return (6 - distFromCenter) * 10;
    }

    /**
     * Evaluates pawn shield for a king
     */
    private int evaluatePawnShield(Board board, Position kingPos, Color kingColor) {
        int score = 0;
        int kingFile = kingPos.getFile() - 'a';
        int kingRank = kingPos.getRank();

        // Check if king is castled (approximation)
        boolean isCastled = (kingColor == Color.WHITE && kingRank == 1 && (kingFile <= 2 || kingFile >= 6)) ||
                (kingColor == Color.BLACK && kingRank == 8 && (kingFile <= 2 || kingFile >= 6));

        if (isCastled) {
            // Define expected shield positions based on king location
            List<Position> shieldPositions = new ArrayList<>();

            if (kingColor == Color.WHITE) {
                if (kingFile >= 5) { // Kingside
                    shieldPositions.add(new Position((char) ('a' + kingFile - 1), kingRank + 1));
                    shieldPositions.add(new Position((char) ('a' + kingFile), kingRank + 1));
                    if (kingFile < 7) {
                        shieldPositions.add(new Position((char) ('a' + kingFile + 1), kingRank + 1));
                    }
                } else { // Queenside
                    if (kingFile > 0) {
                        shieldPositions.add(new Position((char) ('a' + kingFile - 1), kingRank + 1));
                    }
                    shieldPositions.add(new Position((char) ('a' + kingFile), kingRank + 1));
                    shieldPositions.add(new Position((char) ('a' + kingFile + 1), kingRank + 1));
                }
            } else { // BLACK
                if (kingFile >= 5) { // Kingside
                    shieldPositions.add(new Position((char) ('a' + kingFile - 1), kingRank - 1));
                    shieldPositions.add(new Position((char) ('a' + kingFile), kingRank - 1));
                    if (kingFile < 7) {
                        shieldPositions.add(new Position((char) ('a' + kingFile + 1), kingRank - 1));
                    }
                } else { // Queenside
                    if (kingFile > 0) {
                        shieldPositions.add(new Position((char) ('a' + kingFile - 1), kingRank - 1));
                    }
                    shieldPositions.add(new Position((char) ('a' + kingFile), kingRank - 1));
                    shieldPositions.add(new Position((char) ('a' + kingFile + 1), kingRank - 1));
                }
            }

            // Check each shield position for a friendly pawn
            for (Position pos : shieldPositions) {
                ChessPiece piece = board.getPieceAt(pos);
                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == kingColor) {
                    score += PAWN_SHIELD_BONUS;
                } else {
                    // Penalty for missing shield pawn
                    score -= PAWN_SHIELD_BONUS;
                }
            }
        }

        return score;
    }

    /**
     * Finds the king of a given color
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

        return null; // Should never happen in a valid board
    }

    /**
     * Checks if a file is open (no pawns)
     */
    private boolean isOpenFile(Board board, char fileChar) {
        for (int rank = 1; rank <= 8; rank++) {
            Position pos = new Position(fileChar, rank);
            ChessPiece piece = board.getPieceAt(pos);

            if (piece != null && piece.getType() == ChessPieceType.PAWN) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a file is semi-open (no friendly pawns)
     */
    private boolean isSemiOpenFile(Board board, char fileChar, Color color) {
        for (int rank = 1; rank <= 8; rank++) {
            Position pos = new Position(fileChar, rank);
            ChessPiece piece = board.getPieceAt(pos);

            if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                    piece.getColor() == color) {
                return false;
            }
        }

        return !isOpenFile(board, fileChar);
    }

    /**
     * Checks if a knight is on an outpost
     */
    private boolean isKnightOutpost(Board board, Position pos, Color knightColor) {
        int rank = pos.getRank();
        char file = pos.getFile();

        // Knights need to be in enemy territory to be outposts
        boolean inEnemyTerritory = (knightColor == Color.WHITE) ?
                rank >= 5 : rank <= 4;

        if (!inEnemyTerritory) {
            return false;
        }

        // Check if knight can be attacked by enemy pawns
        Color enemyColor = (knightColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        int pawnAttackRank = (knightColor == Color.WHITE) ? rank + 1 : rank - 1;

        if (pawnAttackRank < 1 || pawnAttackRank > 8) {
            // Can't be attacked by pawns because it's on the edge
            return true;
        }

        // Check left and right diagonals for enemy pawns
        for (int offset = -1; offset <= 1; offset += 2) {
            char attackFile = (char) (file + offset);

            if (attackFile >= 'a' && attackFile <= 'h') {
                Position attackPos = new Position(attackFile, pawnAttackRank);
                ChessPiece piece = board.getPieceAt(attackPos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == enemyColor) {
                    return false;
                }
            }
        }

        return true;
    }
}