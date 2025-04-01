package com.example.demo1.core.ai.evaluation.components;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.util.EvaluationConstants;
import java.util.*;

/**
 * Evaluates tactical features and immediate threats
 */
public class TacticalEvaluator {

    // Constants for tactical evaluation
    private static final int HANGING_PIECE_PENALTY = -35;
    private static final int CHECK_BONUS = 20;
    private static final int FORK_POTENTIAL_BONUS = 15;
    private static final int PIN_POTENTIAL_BONUS = 15;
    private static final int DISCOVERED_ATTACK_BONUS = 25;

    /**
     * Evaluate tactical features
     *
     * @param board The chess board
     * @param sideToEvaluate Perspective to evaluate from
     * @return Score in centipawns
     */
    public int evaluate(Board board, Color sideToEvaluate) {
        int whiteScore = 0;
        int blackScore = 0;

        // Evaluate immediate tactical threats/opportunities

        // Check for hanging (undefended) pieces
        whiteScore += evaluateHangingPieces(board, Color.WHITE);
        blackScore += evaluateHangingPieces(board, Color.BLACK);

        // Check for pieces under attack
        whiteScore += evaluatePiecesUnderAttack(board, Color.WHITE);
        blackScore += evaluatePiecesUnderAttack(board, Color.BLACK);

        // Check for fork potential
        whiteScore += evaluateForkPotential(board, Color.WHITE);
        blackScore += evaluateForkPotential(board, Color.BLACK);

        // Check for pins and discoveries
        whiteScore += evaluatePinsAndDiscoveries(board, Color.WHITE);
        blackScore += evaluatePinsAndDiscoveries(board, Color.BLACK);

        // Check for checking possibilities
        whiteScore += evaluateCheckPotential(board, Color.WHITE);
        blackScore += evaluateCheckPotential(board, Color.BLACK);

        // Final score from perspective of side to evaluate
        return (sideToEvaluate == Color.WHITE) ?
                (whiteScore - blackScore) : (blackScore - whiteScore);
    }

    /**
     * Evaluate hanging (undefended) pieces
     */
    private int evaluateHangingPieces(Board board, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check all opponent's pieces that are not defended
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                // Skip empty squares, own pieces, and pawns (less significant)
                if (piece == null || piece.getColor() != opponent ||
                        piece.getType() == ChessPieceType.PAWN) {
                    continue;
                }

                // Check if piece is defended
                boolean isDefended = isPieceDefended(board, pos, opponent);

                if (!isDefended) {
                    // Undefended piece - opportunity to capture
                    int pieceValue = getPieceValue(piece.getType());

                    // Bonus for potentially capturing an undefended piece
                    // (scaled by piece value)
                    score += pieceValue / 10;

                    // Extra bonus if the piece can be safely captured
                    if (canSafelyCapture(board, pos, color)) {
                        score += pieceValue / 5;
                    }
                }
            }
        }

        // Penalty for own hanging pieces
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                // Skip empty squares, opponent pieces, kings, and pawns
                if (piece == null || piece.getColor() != color ||
                        piece.getType() == ChessPieceType.KING ||
                        piece.getType() == ChessPieceType.PAWN) {
                    continue;
                }

                // Check if piece is hanging (not defended)
                if (!isPieceDefended(board, pos, color)) {
                    // Check if it's under attack
                    if (isUnderAttack(board, pos, color, opponent)) {
                        score += HANGING_PIECE_PENALTY;
                    } else {
                        // Just undefended but not under attack - smaller penalty
                        score += HANGING_PIECE_PENALTY / 2;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Check if a piece is defended by any friendly piece
     */
    private boolean isPieceDefended(Board board, Position pos, Color pieceColor) {
        // Check all squares for defenders
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position defenderPos = new Position(file, rank);
                ChessPiece defender = board.getPieceAt(defenderPos);

                // Skip empty squares and opponent pieces
                if (defender == null || defender.getColor() != pieceColor) {
                    continue;
                }

                // Check if defender can move to the piece's position
                if (canPieceAttackSquare(board, defenderPos, defender, pos)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a piece can attack a specific square
     */
    private boolean canPieceAttackSquare(Board board, Position piecePos, ChessPiece piece, Position targetPos) {
        // Skip if same position
        if (piecePos.equals(targetPos)) {
            return false;
        }

        // Get direction vectors from piece to target
        int dx = targetPos.getFile() - piecePos.getFile();
        int dy = targetPos.getRank() - piecePos.getRank();

        // Normalize direction to get step vector
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        // Different piece types have different attack patterns
        switch (piece.getType()) {
            case KING:
                // King attacks one square in any direction
                return Math.abs(dx) <= 1 && Math.abs(dy) <= 1;

            case QUEEN:
                // Queen moves like rook or bishop
                return (canRookAttack(dx, dy) || canBishopAttack(dx, dy)) &&
                        pathIsClear(board, piecePos, targetPos, stepX, stepY);

            case ROOK:
                return canRookAttack(dx, dy) &&
                        pathIsClear(board, piecePos, targetPos, stepX, stepY);

            case BISHOP:
                return canBishopAttack(dx, dy) &&
                        pathIsClear(board, piecePos, targetPos, stepX, stepY);

            case KNIGHT:
                // Knight moves in L-shape
                return (Math.abs(dx) == 1 && Math.abs(dy) == 2) ||
                        (Math.abs(dx) == 2 && Math.abs(dy) == 1);

            case PAWN:
                // Pawns capture diagonally forward
                int forwardDir = (piece.getColor() == Color.WHITE) ? 1 : -1;
                return dy == forwardDir && Math.abs(dx) == 1;

            default:
                return false;
        }
    }

    /**
     * Check if a rook can attack along these offsets
     */
    private boolean canRookAttack(int dx, int dy) {
        // Rooks move horizontally or vertically
        return dx == 0 || dy == 0;
    }

    /**
     * Check if a bishop can attack along these offsets
     */
    private boolean canBishopAttack(int dx, int dy) {
        // Bishops move diagonally
        return Math.abs(dx) == Math.abs(dy);
    }

    /**
     * Check if the path between two positions is clear
     */
    private boolean pathIsClear(Board board, Position from, Position to, int stepX, int stepY) {
        int fromFile = from.getFile() - 'a';
        int fromRank = from.getRank() - 1;
        int toFile = to.getFile() - 'a';
        int toRank = to.getRank() - 1;

        int currentFile = fromFile + stepX;
        int currentRank = fromRank + stepY;

        while (currentFile != toFile || currentRank != toRank) {
            // Check if we've reached the target (shouldn't happen)
            if (currentFile == toFile && currentRank == toRank) {
                break;
            }

            // Check current square
            Position currentPos = new Position((char)('a' + currentFile), currentRank + 1);
            if (board.getPieceAt(currentPos) != null) {
                return false; // Path is blocked
            }

            // Move to next square
            currentFile += stepX;
            currentRank += stepY;
        }

        return true;
    }

    /**
     * Check if a piece is under attack
     */
    private boolean isUnderAttack(Board board, Position pos, Color pieceColor, Color attackerColor) {
        // Check all squares for attackers
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position attackerPos = new Position(file, rank);
                ChessPiece attacker = board.getPieceAt(attackerPos);

                // Skip empty squares and non-attacker pieces
                if (attacker == null || attacker.getColor() != attackerColor) {
                    continue;
                }

                // Check if attacker can move to the piece's position
                if (canPieceAttackSquare(board, attackerPos, attacker, pos)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a piece can be safely captured (capture value > attacker value)
     */
    private boolean canSafelyCapture(Board board, Position targetPos, Color attackerColor) {
        ChessPiece targetPiece = board.getPieceAt(targetPos);
        if (targetPiece == null) return false;

        int targetValue = getPieceValue(targetPiece.getType());

        // Find the least valuable attacker that can capture the target
        int minAttackerValue = Integer.MAX_VALUE;

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position attackerPos = new Position(file, rank);
                ChessPiece attacker = board.getPieceAt(attackerPos);

                // Skip empty squares and non-attacker pieces
                if (attacker == null || attacker.getColor() != attackerColor) {
                    continue;
                }

                // Check if attacker can move to the target's position
                if (canPieceAttackSquare(board, attackerPos, attacker, targetPos)) {
                    int attackerValue = getPieceValue(attacker.getType());
                    minAttackerValue = Math.min(minAttackerValue, attackerValue);
                }
            }
        }

        // Safe to capture if target value > attacker value
        return minAttackerValue < Integer.MAX_VALUE && targetValue > minAttackerValue;
    }

    /**
     * Evaluate pieces under attack
     */
    private int evaluatePiecesUnderAttack(Board board, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check for own pieces under attack
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                // Skip empty squares and opponent pieces
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                // Check if piece is under attack
                if (isUnderAttack(board, pos, color, opponent)) {
                    // Check if piece is defended
                    boolean isDefended = isPieceDefended(board, pos, color);

                    if (!isDefended) {
                        // Undefended piece under attack - big problem
                        int pieceValue = getPieceValue(piece.getType());
                        score -= pieceValue / 5; // Scale penalty by piece value
                    } else {
                        // Attacked but defended - still a concern
                        score -= 5;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Evaluate fork potential (one piece attacking multiple targets)
     */
    private int evaluateForkPotential(Board board, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check knight fork potential
        score += evaluateKnightForkPotential(board, color, opponent);

        // Check pawn fork potential
        score += evaluatePawnForkPotential(board, color, opponent);

        return score;
    }

    /**
     * Evaluate knight fork potential
     */
    private int evaluateKnightForkPotential(Board board, Color color, Color opponent) {
        int score = 0;

        // Find all knights
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position knightPos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(knightPos);

                // Skip non-knights and opponent knights
                if (piece == null || piece.getType() != ChessPieceType.KNIGHT ||
                        piece.getColor() != color) {
                    continue;
                }

                // Check all possible knight moves
                int[][] knightMoves = {
                        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                        {1, -2}, {1, 2}, {2, -1}, {2, 1}
                };

                for (int[] move : knightMoves) {
                    int newFile = knightPos.getFile() - 'a' + move[0];
                    int newRank = knightPos.getRank() - 1 + move[1];

                    // Skip invalid positions
                    if (newFile < 0 || newFile > 7 || newRank < 0 || newRank > 7) {
                        continue;
                    }

                    Position newPos = new Position((char)('a' + newFile), newRank + 1);

                    // Count valuable targets that can be attacked from this position
                    int targetsCount = countValuableTargetsFromSquare(board, newPos, color, opponent);

                    if (targetsCount >= 2) {
                        // Potential fork - bonus for each target beyond the first
                        score += FORK_POTENTIAL_BONUS * (targetsCount - 1);
                    }
                }
            }
        }

        return score;
    }

    /**
     * Evaluate pawn fork potential
     */
    private int evaluatePawnForkPotential(Board board, Color color, Color opponent) {
        int score = 0;

        // Find all pawns
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pawnPos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pawnPos);

                // Skip non-pawns and opponent pawns
                if (piece == null || piece.getType() != ChessPieceType.PAWN ||
                        piece.getColor() != color) {
                    continue;
                }

                // Check pawn's forward move
                int forward = (color == Color.WHITE) ? 1 : -1;
                int newRank = pawnPos.getRank() - 1 + forward;

                // Skip invalid ranks
                if (newRank < 0 || newRank > 7) {
                    continue;
                }

                // Check left and right capture
                for (int fileOffset : new int[]{-1, 1}) {
                    int newFile = pawnPos.getFile() - 'a' + fileOffset;

                    // Skip invalid files
                    if (newFile < 0 || newFile > 7) {
                        continue;
                    }

                    Position capturePos = new Position((char)('a' + newFile), newRank + 1);

                    // Count targets that can be captured from this position
                    int targetsCount = countValuableTargetsFromSquare(board, capturePos, color, opponent);

                    if (targetsCount >= 2) {
                        // Potential fork - bonus for each target beyond the first
                        score += FORK_POTENTIAL_BONUS * (targetsCount - 1);
                    }
                }
            }
        }

        return score;
    }

    /**
     * Count valuable targets that can be attacked from a square
     */
    private int countValuableTargetsFromSquare(Board board, Position fromPos, Color attackerColor, Color targetColor) {
        int targetCount = 0;

        // Temporarily create a virtual piece at the position
        // (knight for simplicity, as we mainly check forks)
        ChessPiece virtualPiece = new Knight(attackerColor);

        // Check all squares for potential targets
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position targetPos = new Position(file, rank);
                ChessPiece targetPiece = board.getPieceAt(targetPos);

                // Skip empty squares, own pieces, and pawns (less valuable targets)
                if (targetPiece == null || targetPiece.getColor() != targetColor ||
                        targetPiece.getType() == ChessPieceType.PAWN) {
                    continue;
                }

                // Check if the virtual piece can attack this target
                if (canPieceAttackSquare(board, fromPos, virtualPiece, targetPos)) {
                    targetCount++;
                }
            }
        }

        return targetCount;
    }

    /**
     * Evaluate pins and discovered attacks
     */
    private int evaluatePinsAndDiscoveries(Board board, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Check for pins
        score += evaluatePins(board, color, opponent);

        // Check for discovered attacks
        score += evaluateDiscoveredAttacks(board, color, opponent);

        return score;
    }

    /**
     * Evaluate pins (piece is pinned against a more valuable piece)
     */
    private int evaluatePins(Board board, Color color, Color opponent) {
        int score = 0;

        // Find all sliding pieces (queen, rook, bishop)
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pinnerPos = new Position(file, rank);
                ChessPiece pinner = board.getPieceAt(pinnerPos);

                // Skip non-sliding pieces and opponent pieces
                if (pinner == null || pinner.getColor() != color ||
                        (pinner.getType() != ChessPieceType.QUEEN &&
                                pinner.getType() != ChessPieceType.ROOK &&
                                pinner.getType() != ChessPieceType.BISHOP)) {
                    continue;
                }

                // Check all possible directions
                int[][] directions;
                if (pinner.getType() == ChessPieceType.ROOK) {
                    directions = new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                } else if (pinner.getType() == ChessPieceType.BISHOP) {
                    directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                } else { // QUEEN
                    directions = new int[][]{
                            {0, 1}, {1, 0}, {0, -1}, {-1, 0},
                            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                    };
                }

                // Check each direction
                for (int[] dir : directions) {
                    int firstPieceFile = -1;
                    int firstPieceRank = -1;
                    ChessPiece firstPiece = null;

                    int secondPieceFile = -1;
                    int secondPieceRank = -1;
                    ChessPiece secondPiece = null;

                    // Look along the direction
                    for (int dist = 1; dist < 8; dist++) {
                        int newFile = pinnerPos.getFile() - 'a' + dir[0] * dist;
                        int newRank = pinnerPos.getRank() - 1 + dir[1] * dist;

                        // Skip invalid positions
                        if (newFile < 0 || newFile > 7 || newRank < 0 || newRank > 7) {
                            break;
                        }

                        Position newPos = new Position((char)('a' + newFile), newRank + 1);
                        ChessPiece piece = board.getPieceAt(newPos);

                        if (piece != null) {
                            if (firstPiece == null) {
                                firstPiece = piece;
                                firstPieceFile = newFile;
                                firstPieceRank = newRank;
                            } else {
                                secondPiece = piece;
                                secondPieceFile = newFile;
                                secondPieceRank = newRank;
                                break;
                            }
                        }
                    }

                    // Check for pin conditions
                    if (firstPiece != null && secondPiece != null) {
                        // For a pin, we need first piece to be opponent's and second piece to be valuable
                        if (firstPiece.getColor() == opponent &&
                                secondPiece.getColor() == opponent &&
                                secondPiece.getType() == ChessPieceType.KING) {

                            // Found a pin! Award bonus based on pinned piece value
                            score += PIN_POTENTIAL_BONUS;
                        }
                    }
                }
            }
        }

        return score;
    }

    /**
     * Evaluate potential for discovered attacks
     */
    private int evaluateDiscoveredAttacks(Board board, Color color, Color opponent) {
        int score = 0;

        // Check for pieces that could potentially move to reveal attacks
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position frontPiecePos = new Position(file, rank);
                ChessPiece frontPiece = board.getPieceAt(frontPiecePos);

                // Skip empty squares and opponent pieces
                if (frontPiece == null || frontPiece.getColor() != color) {
                    continue;
                }

                // Check for sliding pieces behind this piece
                Position backPiecePos = findSlidingPieceBehind(board, frontPiecePos, color);

                if (backPiecePos != null) {
                    ChessPiece backPiece = board.getPieceAt(backPiecePos);

                    // Find what the back piece would attack if front piece moved
                    if (canRevealAttackOnValuablePiece(board, frontPiecePos, backPiecePos, opponent)) {
                        score += DISCOVERED_ATTACK_BONUS;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Find a sliding piece behind another piece
     */
    private Position findSlidingPieceBehind(Board board, Position frontPos, Color color) {
        int frontFile = frontPos.getFile() - 'a';
        int frontRank = frontPos.getRank() - 1;

        // Check all directions
        int[][] directions = {
                {0, 1}, {1, 0}, {0, -1}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            // Look in the opposite direction to find the "behind" piece
            int behindFile = frontFile - dir[0];
            int behindRank = frontRank - dir[1];

            while (behindFile >= 0 && behindFile < 8 && behindRank >= 0 && behindRank < 8) {
                Position behindPos = new Position((char)('a' + behindFile), behindRank + 1);
                ChessPiece behindPiece = board.getPieceAt(behindPos);

                if (behindPiece != null) {
                    if (behindPiece.getColor() == color &&
                            (behindPiece.getType() == ChessPieceType.QUEEN ||
                                    behindPiece.getType() == ChessPieceType.ROOK ||
                                    behindPiece.getType() == ChessPieceType.BISHOP)) {

                        // Check if this sliding piece can attack along this line
                        boolean canAttackAlongLine =
                                (behindPiece.getType() == ChessPieceType.QUEEN) ||
                                        (behindPiece.getType() == ChessPieceType.ROOK && (dir[0] == 0 || dir[1] == 0)) ||
                                        (behindPiece.getType() == ChessPieceType.BISHOP && (Math.abs(dir[0]) == Math.abs(dir[1])));

                        if (canAttackAlongLine) {
                            return behindPos;
                        }
                    }
                    break; // Stop at first piece found
                }

                // Move further in opposite direction
                behindFile -= dir[0];
                behindRank -= dir[1];
            }
        }

        return null;
    }

    /**
     * Check if moving a front piece would reveal an attack on a valuable piece
     */
    private boolean canRevealAttackOnValuablePiece(Board board, Position frontPos, Position backPos, Color opponent) {
        ChessPiece backPiece = board.getPieceAt(backPos);
        if (backPiece == null) return false;

        // Get direction from back piece to front piece
        int dx = frontPos.getFile() - backPos.getFile();
        int dy = frontPos.getRank() - backPos.getRank();

        // Normalize direction
        int dirX = Integer.compare(dx, 0);
        int dirY = Integer.compare(dy, 0);

        // Look beyond the front piece for valuable targets
        int currentFile = frontPos.getFile() - 'a' + dirX;
        int currentRank = frontPos.getRank() - 1 + dirY;

        while (currentFile >= 0 && currentFile < 8 && currentRank >= 0 && currentRank < 8) {
            Position currentPos = new Position((char)('a' + currentFile), currentRank + 1);
            ChessPiece targetPiece = board.getPieceAt(currentPos);

            if (targetPiece != null) {
                // Found a piece - check if it's an opponent piece
                if (targetPiece.getColor() == opponent &&
                        (targetPiece.getType() == ChessPieceType.QUEEN ||
                                targetPiece.getType() == ChessPieceType.ROOK ||
                                targetPiece.getType() == ChessPieceType.KING)) {
                    return true;
                }
                break; // Stop at first piece found
            }

            // Move to next position
            currentFile += dirX;
            currentRank += dirY;
        }

        return false;
    }

    /**
     * Evaluate potential for checking the opponent
     */
    private int evaluateCheckPotential(Board board, Color color) {
        int score = 0;
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Find opponent's king
        Position kingPos = null;
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getType() == ChessPieceType.KING &&
                        piece.getColor() == opponent) {
                    kingPos = pos;
                    break;
                }
            }
            if (kingPos != null) break;
        }

        if (kingPos == null) return 0; // Should never happen

        // Check if any piece is one move away from giving check
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position piecePos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(piecePos);

                // Skip empty squares and opponent pieces
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                // Check if piece is directly giving check
                if (canPieceAttackSquare(board, piecePos, piece, kingPos)) {
                    score += CHECK_BONUS;
                }

                // Check potential check moves
                List<Position> potentialMoves = getPotentialMoves(board, piecePos, piece);
                for (Position targetPos : potentialMoves) {
                    // Skip if targetPos is occupied by own piece
                    ChessPiece targetPiece = board.getPieceAt(targetPos);
                    if (targetPiece != null && targetPiece.getColor() == color) {
                        continue;
                    }

                    // Check if moving to targetPos would result in check
                    if (canPieceAttackSquare(board, targetPos, piece, kingPos)) {
                        score += CHECK_BONUS / 2; // Half bonus for potential check
                    }
                }
            }
        }

        return score;
    }

    /**
     * Get potential moves for a piece
     */
    private List<Position> getPotentialMoves(Board board, Position piecePos, ChessPiece piece) {
        List<Position> moves = new ArrayList<>();

        switch (piece.getType()) {
            case PAWN:
                getPawnMoves(board, piecePos, piece, moves);
                break;

            case KNIGHT:
                getKnightMoves(board, piecePos, piece, moves);
                break;

            case BISHOP:
                getBishopMoves(board, piecePos, piece, moves);
                break;

            case ROOK:
                getRookMoves(board, piecePos, piece, moves);
                break;

            case QUEEN:
                getBishopMoves(board, piecePos, piece, moves); // Diagonal moves
                getRookMoves(board, piecePos, piece, moves);   // Horizontal/vertical moves
                break;

            case KING:
                getKingMoves(board, piecePos, piece, moves);
                break;
        }

        return moves;
    }

    /**
     * Get potential pawn moves
     */
    private void getPawnMoves(Board board, Position piecePos, ChessPiece piece, List<Position> moves) {
        int file = piecePos.getFile() - 'a';
        int rank = piecePos.getRank() - 1;
        int forward = (piece.getColor() == Color.WHITE) ? 1 : -1;

        // Forward move
        int newRank = rank + forward;
        if (newRank >= 0 && newRank < 8) {
            Position forwardPos = new Position((char)('a' + file), newRank + 1);
            if (board.getPieceAt(forwardPos) == null) {
                moves.add(forwardPos);

                // Double move from starting position
                if ((piece.getColor() == Color.WHITE && rank == 1) ||
                        (piece.getColor() == Color.BLACK && rank == 6)) {

                    int doubleRank = rank + 2 * forward;
                    if (doubleRank >= 0 && doubleRank < 8) {
                        Position doublePos = new Position((char)('a' + file), doubleRank + 1);
                        if (board.getPieceAt(doublePos) == null) {
                            moves.add(doublePos);
                        }
                    }
                }
            }
        }

        // Captures
        for (int fileOffset : new int[]{-1, 1}) {
            int captureFile = file + fileOffset;

            if (captureFile >= 0 && captureFile < 8 && newRank >= 0 && newRank < 8) {
                Position capturePos = new Position((char)('a' + captureFile), newRank + 1);

                // Regular capture
                ChessPiece targetPiece = board.getPieceAt(capturePos);
                if (targetPiece != null && targetPiece.getColor() != piece.getColor()) {
                    moves.add(capturePos);
                }

                // En passant captures could be added here
            }
        }
    }

    /**
     * Get potential knight moves
     */
    private void getKnightMoves(Board board, Position piecePos, ChessPiece piece, List<Position> moves) {
        int file = piecePos.getFile() - 'a';
        int rank = piecePos.getRank() - 1;

        // Knight's L-shaped moves
        int[][] offsets = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] offset : offsets) {
            int newFile = file + offset[0];
            int newRank = rank + offset[1];

            if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                Position newPos = new Position((char)('a' + newFile), newRank + 1);

                // Add if square is empty or has enemy piece
                ChessPiece targetPiece = board.getPieceAt(newPos);
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    moves.add(newPos);
                }
            }
        }
    }

    /**
     * Get potential bishop moves
     */
    private void getBishopMoves(Board board, Position piecePos, ChessPiece piece, List<Position> moves) {
        int file = piecePos.getFile() - 'a';
        int rank = piecePos.getRank() - 1;

        // Bishop's diagonal directions
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            for (int dist = 1; dist < 8; dist++) {
                int newFile = file + dir[0] * dist;
                int newRank = rank + dir[1] * dist;

                if (newFile < 0 || newFile >= 8 || newRank < 0 || newRank >= 8) {
                    break; // Off the board
                }

                Position newPos = new Position((char)('a' + newFile), newRank + 1);
                ChessPiece targetPiece = board.getPieceAt(newPos);

                if (targetPiece == null) {
                    // Empty square
                    moves.add(newPos);
                } else {
                    // Square has a piece
                    if (targetPiece.getColor() != piece.getColor()) {
                        // Enemy piece (can capture)
                        moves.add(newPos);
                    }
                    break; // Stop at any piece (can't move through pieces)
                }
            }
        }
    }

    /**
     * Get potential rook moves
     */
    private void getRookMoves(Board board, Position piecePos, ChessPiece piece, List<Position> moves) {
        int file = piecePos.getFile() - 'a';
        int rank = piecePos.getRank() - 1;

        // Rook's horizontal and vertical directions
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            for (int dist = 1; dist < 8; dist++) {
                int newFile = file + dir[0] * dist;
                int newRank = rank + dir[1] * dist;

                if (newFile < 0 || newFile >= 8 || newRank < 0 || newRank >= 8) {
                    break; // Off the board
                }

                Position newPos = new Position((char)('a' + newFile), newRank + 1);
                ChessPiece targetPiece = board.getPieceAt(newPos);

                if (targetPiece == null) {
                    // Empty square
                    moves.add(newPos);
                } else {
                    // Square has a piece
                    if (targetPiece.getColor() != piece.getColor()) {
                        // Enemy piece (can capture)
                        moves.add(newPos);
                    }
                    break; // Stop at any piece (can't move through pieces)
                }
            }
        }
    }

    /**
     * Get potential king moves
     */
    private void getKingMoves(Board board, Position piecePos, ChessPiece piece, List<Position> moves) {
        int file = piecePos.getFile() - 'a';
        int rank = piecePos.getRank() - 1;

        // King moves one square in any direction
        for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (int rankOffset = -1; rankOffset <= 1; rankOffset++) {
                // Skip current position
                if (fileOffset == 0 && rankOffset == 0) continue;

                int newFile = file + fileOffset;
                int newRank = rank + rankOffset;

                if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                    Position newPos = new Position((char)('a' + newFile), newRank + 1);

                    // Add if square is empty or has enemy piece
                    ChessPiece targetPiece = board.getPieceAt(newPos);
                    if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                        moves.add(newPos);
                    }
                }
            }
        }

        // Castling moves could be added here
    }

    /**
     * Get the value of a piece
     */
    private int getPieceValue(ChessPieceType type) {
        switch (type) {
            case PAWN: return EvaluationConstants.PAWN_VALUE;
            case KNIGHT: return EvaluationConstants.KNIGHT_VALUE;
            case BISHOP: return EvaluationConstants.BISHOP_VALUE;
            case ROOK: return EvaluationConstants.ROOK_VALUE;
            case QUEEN: return EvaluationConstants.QUEEN_VALUE;
            case KING: return 20000; // Arbitrary high value
            default: return 0;
        }
    }
}