module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;

    // Only include the basic JavaFX dependencies we actually need
    requires java.desktop;
    requires java.logging;

    // Export all of your packages
    exports com.example.demo1;
    exports com.example.demo1.core;

    // Add these new exports for your special moves and endgame packages
    exports com.example.demo1.special;
    exports com.example.demo1.endgame;
    exports com.example.demo1.moves;

    // If you have internal package implementation details that shouldn't be
    // accessed by other modules, use the 'opens' directive instead of 'exports'
    // For example:
    // opens com.example.demo1.special to com.example.demo1;
}