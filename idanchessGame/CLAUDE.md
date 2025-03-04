# CLAUDE.md - Guidelines for Chess Game Project

## Build & Test Commands
- Build project: `mvn clean install`
- Run application: `mvn javafx:run`
- Run all tests: `mvn test`
- Run single test: `mvn test -Dtest=TestClassName#testMethodName`
- Run test class: `mvn test -Dtest=TestClassName`

## Code Style Guidelines
- **Naming**: camelCase for methods/variables, PascalCase for classes, UPPER_SNAKE_CASE for constants
- **Imports**: Organized by package hierarchy, no wildcard imports
- **Documentation**: Javadoc for all public methods and classes
- **Error Handling**: Null checks before operations, meaningful exceptions
- **Architecture**: Follow MVC pattern (Model-View-Controller)
- **Testing**: JUnit 5 with descriptive display names and assertions
- **Chess Logic**: Use bitboards and bitwise operations for efficient move calculation
- **Comments**: Use comments to explain complex bitwise operations or chess logic