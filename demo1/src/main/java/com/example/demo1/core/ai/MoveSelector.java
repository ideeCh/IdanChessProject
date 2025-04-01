package com.example.demo1.core.ai;

import com.example.demo1.core.*;
import com.example.demo1.core.ai.evaluation.PositionEvaluator;

import java.util.*;

/**
 * Selects the best move for the AI using static evaluation
 */
public class MoveSelector {
    private final PositionEvaluator evaluator;
    private final GamePhaseDetector phaseDetector;
    private final boolean useLogging;

    /**
     * Create a move selector with default settings
     */
    public MoveSelector() {
        this(false);
    }

    /**
     * Create a move selector with specified logging setting
     *
     * @param useLogging Whether to log evaluation details
     */
    public MoveSelector(boolean useLogging) {
        this.evaluator = new PositionEvaluator();
        this.phaseDetector = new GamePhaseDetector();
        this.useLogging = useLogging;
    }

    /**
     * Select the best move for a player based on static evaluation
     *
     * @param gameState Current game state
     * @param color     Color to move
     * @return The best move found or null if no legal moves available
     */
    public Move selectBestMove(GameState gameState, Color color) {
        // Get current game phase
        GamePhase phase = phaseDetector.detectPhase(gameState);
        double phaseValue = phaseDetector.calculatePhaseValue(gameState.getBoard());

        if (useLogging) {
            System.out.println("Selecting move for " + color);
            System.out.println("Current phase: " + phase);
            System.out.println("Phase value: " + phaseValue);
        }

        // Generate all legal moves
        List<Move> legalMoves = generateLegalMoves(gameState, color);

        if (legalMoves.isEmpty()) {
            return null; // No legal moves available
        }

        // Evaluate each move
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        Map<Move, Integer> moveScores = new HashMap<>();

        for (Move move : legalMoves) {
            try {
                // Create a copy of the game state to simulate the move
                GameState tempState = (GameState) gameState.clone();
                tempState.makeMove(move, true); // Simulate the move

                // Evaluate the resulting position
                int score = evaluator.evaluate(tempState.getBoard(), color);

                // Apply tactical adjustments
                score += getTacticalBonus(gameState, move, color, phase);

                moveScores.put(move, score);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } catch (CloneNotSupportedException e) {
                System.err.println("Error cloning game state: " + e.getMessage());
            }
        }

        // Handle ties using tiebreaking logic
        List<Move> tiedMoves = new ArrayList<>();
        for (Map.Entry<Move, Integer> entry : moveScores.entrySet()) {
            if (entry.getValue() == bestScore) {
                tiedMoves.add(entry.getKey());
            }
        }

        if (tiedMoves.size() > 1) {
            bestMove = breakTies(gameState, tiedMoves, color, phase);
        }

        // Log evaluation if enabled
        if (useLogging) {
            logMoveEvaluation(moveScores, bestMove);
        }

        return bestMove;
    }

    /**
     * Generate all legal moves for a player
     */
    private List<Move> generateLegalMoves(GameState gameState, Color color) {
        List<Move> legalMoves = new ArrayList<>();

        // Make sure we're generating moves for the current player
        if (gameState.getCurrentPlayer() != color) {
            System.err.println("Warning: Generating moves for " + color +
                    " but current player is " + gameState.getCurrentPlayer());
            return legalMoves;
        }

        // Find all pieces of the current player and generate their moves
        Board board = gameState.getBoard();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == color) {
                    // Generate basic moves for this piece
                    List<Move> pieceMoves = MoveValidator.generateBasicMoves(gameState, piece);

                    // Filter out moves that would leave the king in check
                    for (Move move : pieceMoves) {
                        try {
                            GameState tempState = (GameState) gameState.clone();
                            tempState.makeMove(move, true); // Simulate the move

                            if (!MoveValidator.isKingInCheck(tempState, color)) {
                                legalMoves.add(move);
                            }
                        } catch (CloneNotSupportedException e) {
                            System.err.println("Error cloning game state: " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Add castling moves if available
        addCastlingMoves(gameState, legalMoves, color);

        return legalMoves;
    }

    /**
     * Add castling moves if they are legal
     */
    private void addCastlingMoves(GameState gameState, List<Move> moves, Color currentPlayer) {
        // Check kingside castling
        try {
            // Create kingside castling move
            Move kingsideCastling = new Move(
                    new Position('e', currentPlayer == Color.WHITE ? 1 : 8),
                    new Position('g', currentPlayer == Color.WHITE ? 1 : 8)
            );
            kingsideCastling.setCastling(true);
            kingsideCastling.setCastlingRookSource(
                    new Position('h', currentPlayer == Color.WHITE ? 1 : 8)
            );
            kingsideCastling.setCastlingRookTarget(
                    new Position('f', currentPlayer == Color.WHITE ? 1 : 8)
            );

            // Check if it's valid
            if (com.example.demo1.special.Castling.isValidCastling(gameState, kingsideCastling)) {
                moves.add(kingsideCastling);
            }
        } catch (Exception e) {
            System.err.println("Error checking kingside castling: " + e.getMessage());
        }

        // Check queenside castling
        try {
            // Create queenside castling move
            Move queensideCastling = new Move(
                    new Position('e', currentPlayer == Color.WHITE ? 1 : 8),
                    new Position('c', currentPlayer == Color.WHITE ? 1 : 8)
            );
            queensideCastling.setCastling(true);
            queensideCastling.setCastlingRookSource(
                    new Position('a', currentPlayer == Color.WHITE ? 1 : 8)
            );
            queensideCastling.setCastlingRookTarget(
                    new Position('d', currentPlayer == Color.WHITE ? 1 : 8)
            );

            // Check if it's valid
            if (com.example.demo1.special.Castling.isValidCastling(gameState, queensideCastling)) {
                moves.add(queensideCastling);
            }
        } catch (Exception e) {
            System.err.println("Error checking queenside castling: " + e.getMessage());
        }
    }

    /**
     * Apply tactical bonuses for specific move types
     */
    private int getTacticalBonus(GameState gameState, Move move, Color color, GamePhase phase) {
        int bonus = 0;
        Board board = gameState.getBoard();

        // Favor captures, especially favorable ones
        ChessPiece capturedPiece = board.getPieceAt(move.getTarget());
        if (capturedPiece != null && capturedPiece.getColor() != color) {
            ChessPiece capturingPiece = board.getPieceAt(move.getSource());

            // Calculate capture value using the Most Valuable Victim / Least Valuable Attacker principle
            int captureValue = getPieceValue(capturedPiece.getType());
            int attackerValue = getPieceValue(capturingPiece.getType());

            // Bonus for capturing a more valuable piece with a less valuable one
            bonus += captureValue - (attackerValue / 10);
        }

        // Check if the move gives check
        try {
            GameState tempState = (GameState) gameState.clone();
            tempState.makeMove(move, true);

            Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
            if (MoveValidator.isKingInCheck(tempState, opponent)) {
                bonus += 30; // Bonus for giving check
            }
        } catch (CloneNotSupportedException e) {
            System.err.println("Error checking for check: " + e.getMessage());
        }

        // Development bonus in opening
        if (phase == GamePhase.OPENING) {
            ChessPiece piece = board.getPieceAt(move.getSource());
            if (piece != null) {
                boolean isMinorPiece = piece.getType() == ChessPieceType.KNIGHT ||
                        piece.getType() == ChessPieceType.BISHOP;
                boolean isQueen = piece.getType() == ChessPieceType.QUEEN;
                int homeRank = (color == Color.WHITE) ? 1 : 8;

                if (isMinorPiece && move.getSource().getRank() == homeRank) {
                    bonus += 20;
                }
                if (isQueen && move.getSource().getRank() == homeRank &&
                        !isDevelopmentComplete(board, color)) {
                    bonus -= 15;
                }
            }
        }

        // Castling bonus
        if (move.isCastling()) {
            // Higher bonus in opening and middlegame
            bonus += (phase == GamePhase.ENDGAME) ? 30 : 60;
        }

        // Promotion bonus
        if (move.getPromotionType() != null) {
            int promotionBonus = 0;
            if (move.getPromotionType() == ChessPieceType.QUEEN) {
                promotionBonus = 500;
            } else if (move.getPromotionType() == ChessPieceType.ROOK) {
                promotionBonus = 300;
            } else if (move.getPromotionType() == ChessPieceType.BISHOP ||
                    move.getPromotionType() == ChessPieceType.KNIGHT) {
                promotionBonus = 200;
            }
            bonus += promotionBonus;
        }

        return bonus;
    }

    /**
     * Check if development is complete (minor pieces moved out)
     */
    private boolean isDevelopmentComplete(Board board, Color color) {
        int homeRank = (color == Color.WHITE) ? 1 : 8;
        int developedCount = 0;

        // Check if knights and bishops have moved
        if (isSquareEmpty(board, 'b', homeRank)) developedCount++;
        if (isSquareEmpty(board, 'g', homeRank)) developedCount++;
        if (isSquareEmpty(board, 'c', homeRank)) developedCount++;
        if (isSquareEmpty(board, 'f', homeRank)) developedCount++;

        // Development considered complete if at least 3 minor pieces moved out
        return developedCount >= 3;
    }

    /**
     * Check if a square is empty
     */
    private boolean isSquareEmpty(Board board, char file, int rank) {
        Position pos = new Position(file, rank);
        return board.getPieceAt(pos) == null;
    }

    /**
     * Get the standard value of a piece
     */
    private int getPieceValue(ChessPieceType type) {
        if (type == ChessPieceType.PAWN) return 100;
        if (type == ChessPieceType.KNIGHT) return 320;
        if (type == ChessPieceType.BISHOP) return 330;
        if (type == ChessPieceType.ROOK) return 500;
        if (type == ChessPieceType.QUEEN) return 900;
        if (type == ChessPieceType.KING) return 20000; // Arbitrary high value
        return 0;
    }

    /**
     * Break ties between equally scored moves by considering positional factors
     */
    private Move breakTies(GameState gameState, List<Move> tiedMoves, Color color, GamePhase phase) {
        // If there's only one tied move, return it
        if (tiedMoves.size() == 1) {
            return tiedMoves.get(0);
        }

        // Create a map to store secondary scores for moves
        Map<Move, Integer> secondaryScores = new HashMap<>();
        Board board = gameState.getBoard();

        for (Move move : tiedMoves) {
            int score = 0;
            ChessPiece piece = board.getPieceAt(move.getSource());
            Position target = move.getTarget();

            // Prefer central squares in opening and middlegame
            if (phase != GamePhase.ENDGAME) {
                int centralityBonus = calculateCentrality(target);
                score += centralityBonus;
            }

            // Prefer moves toward the opponent's side
            int forwardBonus = calculateForwardness(target, color);
            score += forwardBonus;

            // Prefer moves that protect our pieces
            score += calculateProtectionBonus(gameState, move, color);

            // In endgame, prefer moves toward the center
            if (phase == GamePhase.ENDGAME) {
                score += calculateCentrality(target) * 2;
            }

            secondaryScores.put(move, score);
        }

        // Find the move with the highest secondary score
        return secondaryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> {
                    // If all secondary scores are equal, fall back to random selection
                    Random rand = new Random();
                    return tiedMoves.get(rand.nextInt(tiedMoves.size()));
                });
    }

    private int calculateCentrality(Position pos) {
        int fileScore = 4 - Math.abs(pos.getFile() - 'd');
        int rankScore = 4 - Math.abs(pos.getRank() - 4);
        return (fileScore + rankScore) * 5;
    }

    private int calculateForwardness(Position pos, Color color) {
        return color == Color.WHITE ? pos.getRank() - 1 : 8 - pos.getRank();
    }

    private int calculateProtectionBonus(GameState gameState, Move move, Color color) {
        int bonus = 0;
        try {
            GameState tempState = (GameState) gameState.clone();
            tempState.makeMove(move, true);
            Board tempBoard = tempState.getBoard();

            // Check if the move protects friendly pieces
            for (int rank = 1; rank <= 8; rank++) {
                for (char file = 'a'; file <= 'h'; file++) {
                    Position pos = new Position(file, rank);
                    ChessPiece piece = tempBoard.getPieceAt(pos);
                    if (piece != null && piece.getColor() == color) {
                        if (isSquareProtectedByFriendly(tempState, pos, color)) {
                            bonus += 5;
                        }
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            System.err.println("Error in protection calculation: " + e.getMessage());
        }
        return bonus;
    }

    /**
     * Log the evaluation of moves
     */
    private void logMoveEvaluation(Map<Move, Integer> moveScores, Move bestMove) {
        System.out.println("Move Evaluations:");

        // Sort moves by score
        List<Map.Entry<Move, Integer>> sortedEntries = new ArrayList<>(moveScores.entrySet());
        sortedEntries.sort(Map.Entry.<Move, Integer>comparingByValue().reversed());

        // Print each move and its score
        for (Map.Entry<Move, Integer> entry : sortedEntries) {
            String marker = entry.getKey().equals(bestMove) ? " <<< SELECTED" : "";
            System.out.println(entry.getKey() + ": " + entry.getValue() + marker);
        }
    }

    /**
     * Check if a square is protected by friendly pieces
     */
    private boolean isSquareProtectedByFriendly(GameState state, Position targetPos, Color friendlyColor) {
        Board board = state.getBoard();
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = board.getPieceAt(pos);
                if (piece != null && piece.getColor() == friendlyColor) {
                    List<Move> moves = MoveValidator.generateBasicMoves(state, piece);
                    for (Move move : moves) {
                        if (move.getTarget().equals(targetPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
