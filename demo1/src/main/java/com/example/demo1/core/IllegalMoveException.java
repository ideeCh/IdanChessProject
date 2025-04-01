package com.example.demo1.core;

/**
 * Exception thrown when an illegal move is attempted
 */
public class IllegalMoveException extends Exception {
    public IllegalMoveException(String message) {
        super(message);
    }
}
