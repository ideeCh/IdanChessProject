package com.example.demo1.core.ai.evaluation;

import com.example.demo1.core.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Specialized evaluator for the middlegame phase of a chess game.
 * Focuses on piece activity, king safety, pawn structure, and tactical opportunities.
 */
public class MiddlegameEvaluator extends PhaseBasedEvaluator {

    // Middlegame evaluation constants
    private static final int MOBILITY_BONUS = 5;            // Per legal move
    private static final int ATTACKED_PIECE_PENALTY = -10;  // Per undefended piece under attack
    private static final int KING_TROPISM_BONUS = 3;        // Per piece near enemy king
    private static final int KING_ZONE_RADIUS = 2;          // Squares around king considered part of king zone

    // Pawn structure penalties
    private static final int ISOLATED_PAWN_PENALTY = -20;
    private static final int DOUBLED_PAWN_PENALTY = -25;
    private static final int BACKWARD_PAWN_PENALTY = -15;
    private static final int PASSED_PAWN_BONUS = 25;

    // Rook specific bonuses
    private static final int ROOK_OPEN_FILE_BONUS = 20;
    private static final int ROOK_SEMI_OPEN_FILE_BONUS = 10;
    private static final int ROOK_SEVENTH_RANK_BONUS = 30;
    private static final int CONNECTED_ROOKS_BONUS = 15;

    // King safety
    private static final int PAWN_SHIELD_BONUS = 10;
    private static final int OPEN_FILE_NEAR_KING_PENALTY = -25;
    private static final int KING_IN_CENTER_PENALTY = -40;

    @Override
    public int evaluate(Board board, Color sideToEvaluate) {
        // Material score
        int materialScore = calculateMaterialScore(board);

        // Piece activity and mobility
        int whiteMobilityScore = evaluatePieceActivity(board, Color.WHITE);
        int blackMobilityScore = evaluatePieceActivity(board, Color.BLACK);
        int mobilityScore = whiteMobilityScore - blackMobilityScore;

        // King safety
        int whiteKingSafety = evaluateKingSafety(board, Color.WHITE);
        int blackKingSafety = evaluateKingSafety(board, Color.BLACK);
        int kingSafetyScore = whiteKingSafety - blackKingSafety;

        // Pawn structure
        int whitePawnStructure = evaluatePawnStructure(board, Color.WHITE);
        int blackPawnStructure = evaluatePawnStructure(board, Color.BLACK);
        int pawnStructureScore = whitePawnStructure - blackPawnStructure;

        // Tactical opportunities
        int whiteTactical = evaluateTacticalOpportunities(board, Color.WHITE);
        int blackTactical = evaluateTacticalOpportunities(board, Color.BLACK);
        int tacticalScore = whiteTactical - blackTactical;

        // Combine all scores
        int totalScore = materialScore + mobilityScore + kingSafetyScore +
                pawnStructureScore + tacticalScore;

        // Return from the perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ? totalScore : -totalScore;
    }

    /**
     * Evaluate piece activity and mobility
     */
    private int evaluatePieceActivity(Board board, Color color) {
        int score = 0;

        // Evaluate each piece's placement and mobility
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece == null || piece.getColor() != color) continue;

                // Check piece-specific positioning
                Map<ChessPieceType, Function<Position, Integer>> pieceEvaluations = Map.of(
                    ChessPieceType.KNIGHT, p -> evaluateKnightPosition(board, p),
                    ChessPieceType.BISHOP, p -> evaluateBishopPosition(board, p),
                    ChessPieceType.ROOK, p -> evaluateRookPosition(board, p, color),
                    ChessPieceType.QUEEN, p -> evaluateQueenPosition(board, p)
                );
                Function<Position, Integer> evaluator = pieceEvaluations.get(piece.getType());
                if (evaluator != null) {
                    score += evaluator.apply(pos);
                }

                // Add mobility bonus (simplified - in a real implementation would count legal moves)
                score += estimatePieceMobility(board, pos) * MOBILITY_BONUS;
            }
        }

        // Check for connected rooks
        score += evaluateConnectedRooks(board, color);

        return score;
    }

    /**
     * Estimate mobility of a piece
     */
    private int estimatePieceMobility(Board board, Position pos) {
        // This is a simplified approximation - a real implementation would
        // generate all legal moves for the piece and count them

        ChessPiece piece = board.getPieceAt(pos);
        if (piece == null) return 0;

        int mobility = 0;

        // Approximate mobility based on piece type and surrounding squares
        // Just counting empty adjacent squares as a very rough estimate
        Map<ChessPieceType, int[][]> pieceDirections = Map.of(
            ChessPieceType.KNIGHT, new int[][] {
                        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                        {1, -2}, {1, 2}, {2, -1}, {2, 1}
            },
            ChessPieceType.BISHOP, new int[][] {
                        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
            },
            ChessPieceType.ROOK, new int[][] {
                        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
            },
            ChessPieceType.QUEEN, new int[][] {
                        {-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
                        {0, 1}, {1, -1}, {1, 0}, {1, 1}
            }
        );

        int[][] directions;
        if (piece.getType() == ChessPieceType.PAWN) {
            directions = piece.getColor() == Color.WHITE ?
                new int[][] {{0, 1}, {-1, 1}, {1, 1}} :
                new int[][] {{0, -1}, {-1, -1}, {1, -1}};
                } else {
            directions = pieceDirections.getOrDefault(piece.getType(), new int[0][0]);
        }

        int fileIdx = pos.getFile() - 'a';
        int rankIdx = pos.getRank() - 1;

        // Check each direction
        for (int[] dir : directions) {
            int newFile = fileIdx + dir[0];
            int newRank = rankIdx + dir[1];

            // Check if square is on the board
            if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                Position newPos = new Position((char)('a' + newFile), newRank + 1);
                ChessPiece targetPiece = board.getPieceAt(newPos);

                // Empty square or opponent's piece contributes to mobility
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    mobility++;
                }
            }
        }

        return mobility;
    }

    /**
     * Evaluate knight positioning
     */
    private int evaluateKnightPosition(Board board, Position pos) {
        int score = 0;
        ChessPiece knight = board.getPieceAt(pos);
        if (knight == null || knight.getType() != ChessPieceType.KNIGHT) return 0;

        // Center control bonus
        int fileIdx = pos.getFile() - 'a';
        int rankIdx = pos.getRank() - 1;

        // Central knights are better
        if (fileIdx >= 2 && fileIdx <= 5 && rankIdx >= 2 && rankIdx <= 5) {
            score += 10;
        }

        // Knights on the rim are dim
        if (fileIdx == 0 || fileIdx == 7 || rankIdx == 0 || rankIdx == 7) {
            score -= 15;
        }

        // Check for knight outposts (knights protected by friendly pawns)
        Color color = knight.getColor();
        Color opponent = color == Color.WHITE ? Color.BLACK : Color.WHITE;
        int pawnDirection = color == Color.WHITE ? 1 : -1;

        // Check if knight can be attacked by enemy pawns
        boolean safeFromPawns = true;
        for (int fileOffset : new int[] {-1, 1}) {
            int attackFile = fileIdx + fileOffset;
            int attackRank = rankIdx + pawnDirection;

            if (attackFile >= 0 && attackFile < 8 && attackRank >= 0 && attackRank < 8) {
                Position attackPos = new Position((char)('a' + attackFile), attackRank + 1);
                ChessPiece attacker = board.getPieceAt(attackPos);

                safeFromPawns = safeFromPawns && (attacker == null ||
                    attacker.getType() != ChessPieceType.PAWN ||
                    attacker.getColor() != opponent);
            }
        }

        // Protected by own pawn?
        boolean protectedByPawn = false;
        for (int fileOffset : new int[] {-1, 1}) {
            int protectFile = fileIdx + fileOffset;
            int protectRank = rankIdx - pawnDirection;

            if (protectFile >= 0 && protectFile < 8 && protectRank >= 0 && protectRank < 8) {
                Position protectPos = new Position((char)('a' + protectFile), protectRank + 1);
                ChessPiece protector = board.getPieceAt(protectPos);

                protectedByPawn = protectedByPawn || (protector != null &&
                    protector.getType() == ChessPieceType.PAWN &&
                    protector.getColor() == color);
            }
        }

        // Knight outpost bonus
        if (safeFromPawns && protectedByPawn) {
            // More valuable if in opponents half
            boolean inEnemyTerritory = (color == Color.WHITE && rankIdx >= 4) ||
                    (color == Color.BLACK && rankIdx <= 3);
            score += inEnemyTerritory ? 25 : 15;
        }

        return score;
    }

    /**
     * Evaluate bishop positioning
     */
    private int evaluateBishopPosition(Board board, Position pos) {
        int score = 0;
        ChessPiece bishop = board.getPieceAt(pos);
        if (bishop == null || bishop.getType() != ChessPieceType.BISHOP) return 0;

        Color color = bishop.getColor();

        // Check for bishop pair
        boolean hasBishopPair = false;
        int otherBishopCount = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position checkPos = new Position(file, rank);
                if (checkPos.equals(pos)) continue;  // Skip the current bishop

                ChessPiece piece = board.getPieceAt(checkPos);
                if (piece != null && piece.getType() == ChessPieceType.BISHOP &&
                        piece.getColor() == color) {
                    otherBishopCount++;
                }
            }
        }

        if (otherBishopCount > 0) {
            hasBishopPair = true;
            score += 30;  // Bishop pair bonus
        }

        // Bishop mobility is critical
        // Count pawns that could block bishop's diagonals
        int blockedDiagonals = 0;
        int fileIdx = pos.getFile() - 'a';
        int rankIdx = pos.getRank() - 1;

        // Check all diagonal directions
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] dir : directions) {
            boolean blocked = false;

            for (int step = 1; step < 8; step++) {
                int newFile = fileIdx + dir[0] * step;
                int newRank = rankIdx + dir[1] * step;

                if (newFile < 0 || newFile >= 8 || newRank < 0 || newRank >= 8) {
                    break;  // Out of board bounds
                }

                Position newPos = new Position((char)('a' + newFile), newRank + 1);
                ChessPiece piece = board.getPieceAt(newPos);

                if (piece != null) {
                    if (piece.getType() == ChessPieceType.PAWN && piece.getColor() == color) {
                        blocked = true;
                    }
                    break;  // Any piece blocks the diagonal
                }
            }

            if (blocked) {
                blockedDiagonals++;
            }
        }

        // Penalty for blocked diagonals
        score -= blockedDiagonals * 5;

        return score;
    }

    /**
     * Evaluate rook positioning
     */
    private int evaluateRookPosition(Board board, Position pos, Color color) {
        int score = 0;
        ChessPiece rook = board.getPieceAt(pos);
        if (rook == null || rook.getType() != ChessPieceType.ROOK) return 0;

        // Rook on open file
        if (isOpenFile(board, pos.getFile())) {
            score += ROOK_OPEN_FILE_BONUS;
        }
        // Rook on semi-open file
        else if (isSemiOpenFile(board, pos.getFile(), color)) {
            score += ROOK_SEMI_OPEN_FILE_BONUS;
        }

        // Rook on 7th rank (2nd rank for black)
        int seventhRank = (color == Color.WHITE) ? 7 : 2;
        if (pos.getRank() == seventhRank) {
            score += ROOK_SEVENTH_RANK_BONUS;
        }

        return score;
    }

    /**
     * Evaluate queen positioning
     */
    private int evaluateQueenPosition(Board board, Position pos) {
        int score = 0;
        ChessPiece queen = board.getPieceAt(pos);
        if (queen == null || queen.getType() != ChessPieceType.QUEEN) return 0;

        // In middlegame, queen should be centralized but not too early
        int fileIdx = pos.getFile() - 'a';
        int rankIdx = pos.getRank() - 1;

        // Central queen is good
        if (fileIdx >= 2 && fileIdx <= 5 && rankIdx >= 2 && rankIdx <= 5) {
            score += 5;
        }

        // Penalty for queen too close to edge
        if (fileIdx == 0 || fileIdx == 7 || rankIdx == 0 || rankIdx == 7) {
            score -= 10;
        }

        return score;
    }

    /**
     * Check if rooks are connected (on same rank with no pieces between)
     */
    private int evaluateConnectedRooks(Board board, Color color) {
        List<Position> rookPositions = new ArrayList<>();

        // Find all rooks of given color
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

        // Need at least 2 rooks to connect
        if (rookPositions.size() < 2) {
            return 0;
        }

        // Check if any two rooks are connected
        for (int i = 0; i < rookPositions.size() - 1; i++) {
            for (int j = i + 1; j < rookPositions.size(); j++) {
                Position rook1 = rookPositions.get(i);
                Position rook2 = rookPositions.get(j);

                // Check if rooks are on same rank
                if (rook1.getRank() == rook2.getRank()) {
                    boolean connected = true;

                    // Check if path between rooks is clear
                    int minFile = Math.min(rook1.getFile(), rook2.getFile());
                    int maxFile = Math.max(rook1.getFile(), rook2.getFile());

                    for (char file = (char)(minFile + 1); file < maxFile; file++) {
                        Position between = new Position(file, rook1.getRank());
                        if (board.getPieceAt(between) != null) {
                            connected = false;
                            break;
                        }
                    }

                    if (connected) {
                        return CONNECTED_ROOKS_BONUS;
                    }
                }

                // Check if rooks are on same file
                if (rook1.getFile() == rook2.getFile()) {
                    boolean connected = true;

                    // Check if path between rooks is clear
                    int minRank = Math.min(rook1.getRank(), rook2.getRank());
                    int maxRank = Math.max(rook1.getRank(), rook2.getRank());

                    for (int rank = minRank + 1; rank < maxRank; rank++) {
                        Position between = new Position(rook1.getFile(), rank);
                        if (board.getPieceAt(between) != null) {
                            connected = false;
                            break;
                        }
                    }

                    if (connected) {
                        return CONNECTED_ROOKS_BONUS;
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Evaluate pawn structure
     */
    private int evaluatePawnStructure(Board board, Color color) {
        int score = 0;

        // Track pawns by file
        int[] pawnsPerFile = new int[8];
        int[] pawnRanks = new int[8];
        Arrays.fill(pawnRanks, color == Color.WHITE ? 0 : 9);

        // Find all pawns
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    int fileIdx = file - 'a';
                    pawnsPerFile[fileIdx]++;

                    // Track most advanced pawn on each file
                    if (color == Color.WHITE) {
                        pawnRanks[fileIdx] = Math.max(pawnRanks[fileIdx], rank);
                    } else {
                        pawnRanks[fileIdx] = Math.min(pawnRanks[fileIdx], rank);
                    }
                }
            }
        }

        // Evaluate doubled pawns
        for (int fileIdx = 0; fileIdx < 8; fileIdx++) {
            if (pawnsPerFile[fileIdx] > 1) {
                score += DOUBLED_PAWN_PENALTY * (pawnsPerFile[fileIdx] - 1);
            }
        }

        // Evaluate isolated and backward pawns, and passed pawns
        for (int fileIdx = 0; fileIdx < 8; fileIdx++) {
            if (pawnsPerFile[fileIdx] == 0) continue;

            // Check for isolated pawns (no friendly pawns on adjacent files)
            boolean isIsolated = true;
            if (fileIdx > 0 && pawnsPerFile[fileIdx-1] > 0) {
                isIsolated = false;
            }
            if (fileIdx < 7 && pawnsPerFile[fileIdx+1] > 0) {
                isIsolated = false;
            }

            if (isIsolated) {
                score += ISOLATED_PAWN_PENALTY;
            }

            // Check for backward pawns
            if (!isIsolated) {
                boolean isBackward = false;

                // For white, a pawn is backward if it's behind friendly pawns on adjacent files
                if (color == Color.WHITE) {
                    int pawnRank = pawnRanks[fileIdx];
                    if ((fileIdx > 0 && pawnRanks[fileIdx-1] > pawnRank) ||
                            (fileIdx < 7 && pawnRanks[fileIdx+1] > pawnRank)) {
                        isBackward = true;
                    }
                } else {
                    // For black, it's the opposite
                    int pawnRank = pawnRanks[fileIdx];
                    if ((fileIdx > 0 && pawnRanks[fileIdx-1] < pawnRank) ||
                            (fileIdx < 7 && pawnRanks[fileIdx+1] < pawnRank)) {
                        isBackward = true;
                    }
                }

                if (isBackward) {
                    score += BACKWARD_PAWN_PENALTY;
                }
            }

            // Check for passed pawns
            boolean isPassed = true;
            Color enemyColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

            // A passed pawn has no enemy pawns blocking its path or on adjacent files
            int pawnRank = pawnRanks[fileIdx];

            for (int checkFile = Math.max(0, fileIdx-1); checkFile <= Math.min(7, fileIdx+1); checkFile++) {
                for (int checkRank = 1; checkRank <= 8; checkRank++) {
                    // For white pawns, look for black pawns ahead
                    // For black pawns, look for white pawns behind
                    if ((color == Color.WHITE && checkRank > pawnRank) ||
                            (color == Color.BLACK && checkRank < pawnRank)) {

                        Position checkPos = new Position((char)('a' + checkFile), checkRank);
                        ChessPiece piece = board.getPieceAt(checkPos);

                        if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                                piece.getColor() == enemyColor) {
                            isPassed = false;
                            break;
                        }
                    }
                }
                if (!isPassed) break;
            }

            if (isPassed) {
                // Base passed pawn bonus
                score += PASSED_PAWN_BONUS;

                // Additional bonus based on how advanced the pawn is
                if (color == Color.WHITE) {
                    score += (pawnRank - 2) * 5; // Rank 2 is starting position for white
                } else {
                    score += (7 - pawnRank) * 5; // Rank 7 is starting position for black
                }
            }
        }

        return score;
    }

    /**
     * Evaluate king safety
     */
    private int evaluateKingSafety(Board board, Color color) {
        int score = 0;
        Position kingPos = findKing(board, color);

        if (kingPos == null) return 0;

        // Check if king is castled
        boolean isCastled = isKingCastled(board, kingPos, color);

        // King in center is bad in middlegame
        if (isKingInCenter(kingPos)) {
            score += KING_IN_CENTER_PENALTY;
        }

        // Pawn shield analysis
        if (isCastled) {
            score += analyzePawnShield(board, kingPos, color);
        }

        // Check open files near king
        score += analyzeFilesNearKing(board, kingPos, color);

        // King tropism - enemy pieces near king
        score += analyzeKingTropism(board, kingPos, color);

        return score;
    }

    /**
     * Check if king is castled (approximation based on king position)
     */
    private boolean isKingCastled(Board board, Position kingPos, Color color) {
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
     * Check if king is in the center (vulnerable in middlegame)
     */
    private boolean isKingInCenter(Position kingPos) {
        int file = kingPos.getFile() - 'a';
        int rank = kingPos.getRank() - 1;

        // Center defined as d4-e5 and surrounding squares
        return (file >= 2 && file <= 5 && rank >= 2 && rank <= 5);
    }

    /**
     * Analyze pawn shield in front of the king
     */
    private int analyzePawnShield(Board board, Position kingPos, Color color) {
        int score = 0;
        int kingFile = kingPos.getFile() - 'a';
        int kingRank = kingPos.getRank();

        // Define shield positions
        List<Position> shieldPositions = new ArrayList<>();
        int shieldRank = (color == Color.WHITE) ? kingRank + 1 : kingRank - 1;

        // Check the three files in front of king (or two if on edge)
        int startFile = Math.max(0, kingFile - 1);
        int endFile = Math.min(7, kingFile + 1);

        for (int file = startFile; file <= endFile; file++) {
            shieldPositions.add(new Position((char)('a' + file), shieldRank));
        }

        // Check second rank of shield if king is on home rank
        if ((color == Color.WHITE && kingRank == 1) || (color == Color.BLACK && kingRank == 8)) {
            int secondShieldRank = (color == Color.WHITE) ? kingRank + 2 : kingRank - 2;
            for (int file = startFile; file <= endFile; file++) {
                shieldPositions.add(new Position((char)('a' + file), secondShieldRank));
            }
        }

        // Count pawns in shield positions
        for (Position pos : shieldPositions) {
            if (pos.isValid()) {
                ChessPiece piece = board.getPieceAt(pos);
                if (piece != null && piece.getType() == ChessPieceType.PAWN &&
                        piece.getColor() == color) {
                    score += PAWN_SHIELD_BONUS;
                } else {
                    score -= PAWN_SHIELD_BONUS/2; // Missing shield pawn penalty
                }
            }
        }

        return score;
    }

    /**
     * Analyze open files near the king
     */
    private int analyzeFilesNearKing(Board board, Position kingPos, Color color) {
        int score = 0;
        int kingFile = kingPos.getFile() - 'a';

        // Check files around king
        for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
            int file = kingFile + fileOffset;
            if (file < 0 || file > 7) continue;

            char fileChar = (char)('a' + file);

            // Penalty for open files next to king
            if (isOpenFile(board, fileChar)) {
                score += OPEN_FILE_NEAR_KING_PENALTY;
            }
            // Lesser penalty for semi-open files
            else if (isSemiOpenFile(board, fileChar, color)) {
                score += OPEN_FILE_NEAR_KING_PENALTY / 2;
            }
        }

        return score;
    }

    /**
     * Analyze enemy pieces close to the king (king tropism)
     */
    private int analyzeKingTropism(Board board, Position kingPos, Color color) {
        int score = 0;
        int kingFile = kingPos.getFile() - 'a';
        int kingRank = kingPos.getRank() - 1;
        Color enemyColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Define king zone - squares around king
        boolean[][] kingZone = new boolean[8][8];

        // Mark all squares in king zone
        for (int fileOffset = -KING_ZONE_RADIUS; fileOffset <= KING_ZONE_RADIUS; fileOffset++) {
            for (int rankOffset = -KING_ZONE_RADIUS; rankOffset <= KING_ZONE_RADIUS; rankOffset++) {
                int zoneFile = kingFile + fileOffset;
                int zoneRank = kingRank + rankOffset;

                if (zoneFile >= 0 && zoneFile < 8 && zoneRank >= 0 && zoneRank < 8) {
                    kingZone[zoneFile][zoneRank] = true;
                }
            }
        }

        // Count enemy pieces attacking king zone with weighted values
        int attackCount = 0;
        int attackWeight = 0;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == enemyColor) {
                    int pieceFile = file - 'a';
                    int pieceRank = rank - 1;

                    // Check if piece is in or attacking king zone
                    if (isAttackingKingZone(board, pos, piece, kingZone)) {
                        attackCount++;

                        // Weight by piece type
                        switch (piece.getType()) {
                            case QUEEN:
                                attackWeight += 4;
                                break;
                            case ROOK:
                                attackWeight += 3;
                                break;
                            case BISHOP:
                            case KNIGHT:
                                attackWeight += 2;
                                break;
                            case PAWN:
                                attackWeight += 1;
                                break;
                        }
                    }
                }
            }
        }

        // More attackers = exponentially worse
        if (attackCount >= 2) {
            score -= (attackWeight * attackCount);
        }

        return score;
    }

    /**
     * Check if a piece is attacking the king zone
     */
    private boolean isAttackingKingZone(Board board, Position piecePos, ChessPiece piece, boolean[][] kingZone) {
        // Simplified approach - in a real implementation would use attack patterns
        int pieceFile = piecePos.getFile() - 'a';
        int pieceRank = piecePos.getRank() - 1;

        // Check if piece is in king zone
        if (kingZone[pieceFile][pieceRank]) {
            return true;
        }

        // For long-range pieces, check if they're aligned with king zone
        switch (piece.getType()) {
            case QUEEN:
            case ROOK:
            case BISHOP:
                // Check if piece could potentially attack king zone
                for (int file = 0; file < 8; file++) {
                    for (int rank = 0; rank < 8; rank++) {
                        if (kingZone[file][rank]) {
                            if (canPieceAttack(board, piecePos, piece, new Position((char)('a' + file), rank + 1))) {
                                return true;
                            }
                        }
                    }
                }
                break;

            case KNIGHT:
                // Knight's L-shaped moves
                int[][] knightMoves = {
                        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                        {1, -2}, {1, 2}, {2, -1}, {2, 1}
                };

                for (int[] move : knightMoves) {
                    int targetFile = pieceFile + move[0];
                    int targetRank = pieceRank + move[1];

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
     * Check if a piece can attack a target square
     */
    /**
     * Check if a piece can attack a target square
     */
    private boolean canPieceAttack(Board board, Position piecePos, ChessPiece piece, Position targetPos) {
        int dx = targetPos.getFile() - piecePos.getFile();
        int dy = targetPos.getRank() - piecePos.getRank();

        // Attack strategy map
        Map<ChessPieceType, BiFunction<Integer, Integer, Boolean>> attackStrategies = Map.of(
                ChessPieceType.QUEEN, (x, y) -> canRookAttack(x, y) || canBishopAttack(x, y),
                ChessPieceType.ROOK, this::canRookAttack,
                ChessPieceType.BISHOP, this::canBishopAttack,
                ChessPieceType.KNIGHT, (x, y) ->
                        (Math.abs(x) == 1 && Math.abs(y) == 2) || (Math.abs(x) == 2 && Math.abs(y) == 1),
                ChessPieceType.PAWN, (x, y) -> {
                    int forward = (piece.getColor() == Color.WHITE) ? 1 : -1;
                    return y == forward && Math.abs(x) == 1;
                }
        );

        // Retrieve and use the appropriate attack strategy
        return attackStrategies.getOrDefault(piece.getType(), (x, y) -> false)
                .apply(dx, dy);
    }


    private boolean canRookAttack(int dx, int dy) {
        return (dx == 0 || dy == 0);
    }

    private boolean canBishopAttack(int dx, int dy) {
        return Math.abs(dx) == Math.abs(dy);
    }

    /**
     * Evaluate tactical opportunities
     */
    private int evaluateTacticalOpportunities(Board board, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check for hanging pieces (undefended pieces that could be captured)
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null) {
                    // Check opponent's hanging pieces (good for us)
                    if (piece.getColor() == opponent && isHangingPiece(board, pos)) {
                        score += getPieceValue(piece.getType()) / 10; // Bonus for potential capture
                    }

                    // Check our hanging pieces (bad for us)
                    if (piece.getColor() == color && isHangingPiece(board, pos)) {
                        score += ATTACKED_PIECE_PENALTY;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Check if a piece is hanging (undefended and can be captured)
     */
    /**
     * Check if a piece is hanging (undefended and can be captured)
     */
    private boolean isHangingPiece(Board board, Position pos) {
        ChessPiece piece = board.getPieceAt(pos);
        if (piece == null) return false;

        // Kings can't be hanging
        if (piece.getType() == ChessPieceType.KING) {
            return false;
        }

        Color pieceColor = piece.getColor();
        Color opponent = (pieceColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check if any opponent pieces can attack this piece
        boolean isAttacked = false;
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position attackerPos = new Position(file, rank);
                ChessPiece attacker = board.getPieceAt(attackerPos);

                if (attacker != null && attacker.getColor() == opponent) {
                    if (canPieceAttack(board, attackerPos, attacker, pos)) {
                        isAttacked = true;
                    }
                }
            }
        }

        if (!isAttacked) {
            return false; // Piece is not attacked
        }

        // Check if piece is defended by any friendly pieces
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position defenderPos = new Position(file, rank);
                ChessPiece defender = board.getPieceAt(defenderPos);

                if (defender != null && defender.getColor() == pieceColor && !defenderPos.equals(pos)) {
                    if (canPieceAttack(board, defenderPos, defender, pos)) {
                        return false; // Piece is defended
                    }
                }
            }
        }

        // Piece is attacked and not defended
        return true;
    }
}