# Java Chess Game

A chess game implementation in Java using the MVC (Model-View-Controller) pattern and bitboard representation for efficient move calculations.

## Overview

This chess game allows two human players to play against each other through a console-based interface. It implements all standard chess rules including special moves like castling, en passant captures, and pawn promotions.

## Project Structure

The project follows the MVC architecture:

### Model

- **Bitboard**: Represents the chess board using 64-bit integers for efficient operations.
- **Board**: Manages multiple bitboards (one for each piece type) and game state.
- **Piece**: Abstract class with concrete implementations for each piece type.
- **Move**: Represents a chess move with origin, destination, and special move flags.
- **GameState**: Tracks whose turn it is, move history, and game status.

### View

- **ConsoleView**: Displays the board and game information in the console.

### Controller

- **GameController**: Manages player turns and game flow.

## Key Features

1. **Bitboard representation** for efficient move generation and board state tracking
2. **All standard chess rules** including:
    - Castling (kingside and queenside)
    - En passant captures
    - Pawn promotion
    - Check, checkmate, and stalemate detection
    - Draw by insufficient material
    - 50-move rule

3. **Console-based UI** that:
    - Displays the board in a readable format
    - Accepts moves in algebraic notation (e.g., "e2e4")
    - Shows error messages for illegal moves
    - Displays "Check" and "Checkmate" messages

## How to Run

1. Compile the Java source files:
   ```
   javac com/chess/*.java com/chess/model/*.java com/chess/view/*.java com/chess/controller/*.java
   ```

2. Run the application:
   ```
   java com.chess.ChessApplication
   ```

## Playing the Game

1. Enter moves in the format of source square followed by destination square (e.g., "e2e4").
2. For pawn promotions, add the piece letter to promote to (q, r, b, n) - e.g., "e7e8q" to promote to a queen.
3. Type "undo" to take back a move.
4. Type "help" to see available commands.
5. Type "quit" or "exit" to end the game.

## Implementation Details

### Bitboard Representation

- Each piece type has its own 64-bit integer (bitboard) where each bit represents a square on the board.
- This allows for efficient move generation using bitwise operations.

### Move Generation

- Sliding pieces (bishops, rooks, queens) use ray attacks.
- Knights and kings use pre-computed move patterns.
- Pawns have special rules for first move, captures, en passant, and promotion.

### Board State Management

- The board maintains the position of all pieces, castling rights, and en passant squares.
- A stack of move states enables undoing moves.

### Game State Tracking

- The game state tracks whose turn it is, move history, and whether the game is over.
- It detects check, checkmate, stalemate, and other draw conditions.

## Testing

Comprehensive unit tests cover:
- Bitboard operations
- Move execution and undoing
- Board initialization and validation
- Game state transitions

To run the tests, use your favorite testing framework (JUnit is recommended).

## Future Enhancements

Potential future enhancements include:
1. Graphical user interface
2. AI opponent using minimax with alpha-beta pruning
3. Game persistence (save/load)
4. Network play capability
5. Chess clock functionality
6. PGN import/export