package com.example.idanchessgame;

import java.util.Scanner;
/**
 * Console-based view for a chess game.
 */
public class ConsoleView {
    private Scanner scanner;

    /**
     * Creates a new console view.
     */
    public ConsoleView() {
        scanner = new Scanner(System.in);
    }

    /**
     * Displays a welcome message.
     */
    public void displayWelcomeMessage() {
        System.out.println("Welcome to Java Chess!");
        System.out.println("Enter moves in the format 'e2e4' (source square to destination square).");
        System.out.println("For pawn promotions, append the piece letter (q, r, b, n), e.g., 'e7e8q'.");
        System.out.println("Type 'help' for more commands, 'quit' to exit.");
        System.out.println();
    }

    /**
     * Displays the chess board.
     *
     * @param board The board to display
     */
    public void displayBoard(Board board) {
        System.out.println();
        System.out.println("    a b c d e f g h");
        System.out.println("  +-----------------+");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " | ");

            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                PieceType piece = board.getPieceAt(square);

                if (piece == null) {
                    // Empty square
                    System.out.print(((rank + file) % 2 == 0) ? ". " : "- ");
                } else {
                    // Piece
                    System.out.print(piece.getNotation() + " ");
                }
            }

            System.out.println("| " + (rank + 1));
        }

        System.out.println("  +-----------------+");
        System.out.println("    a b c d e f g h");
        System.out.println();
        System.out.println("Turn: " + (board.isWhiteToMove() ? "White" : "Black"));
        System.out.println();
    }

    /**
     * Gets a move from the player.
     *
     * @param isWhiteTurn Whether it's white's turn
     * @return The player's input
     */
    public String getPlayerMove(boolean isWhiteTurn) {
        System.out.print((isWhiteTurn ? "White" : "Black") + " to move: ");
        return scanner.nextLine().trim();
    }

    /**
     * Displays a help message with available commands.
     */
    public void displayHelpMessage() {
        System.out.println("Available commands:");
        System.out.println("  [source][destination]  - Make a move (e.g., 'e2e4')");
        System.out.println("  [source][destination][promotion]  - Promote a pawn (e.g., 'e7e8q')");
        System.out.println("  undo  - Undo the last move");
        System.out.println("  help  - Display this help message");
        System.out.println("  quit  - Exit the game");
        System.out.println();
    }

    /**
     * Displays a check message.
     *
     * @param isWhiteInCheck Whether white is in check
     */
    public void displayCheckMessage(boolean isWhiteInCheck) {
        System.out.println((isWhiteInCheck ? "White" : "Black") + " is in CHECK!");
    }

    /**
     * Displays a general message.
     *
     * @param message The message to display
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Closes the scanner.
     */
    public void close() {
        scanner.close();
    }
}