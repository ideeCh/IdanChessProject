package com.example.demo1.core;

/**
 * Interface defining the user interface capabilities
 */
public interface UserInterface {
    void initialize();

    void display();

    void updateBoard(GameState gameState);
}
