# Clean Common Library

A shared Java 21 library for the Clean platform, providing reusable components and utilities for Spring Boot applications.

## Overview

This library serves as the common foundation for:
- clean-backoffice-api
- clean-authentication-api
- Other Clean platform services

## Technology Stack

- **Java**: 21
- **Build Tool**: Gradle 9.0
- **Testing**: JUnit 5
- **Utilities**: Lombok

## Project Structure

```
src/
├── main/
│   ├── java/com/clean/common/     # Library source code
│   └── resources/                  # Configuration files
└── test/
    ├── java/com/clean/common/     # Test source code
    └── resources/                  # Test resources
```

## Building the Project

### Prerequisites

- Java 21 (JDK 21 or later)
- Gradle 9.0 (via wrapper)

### Build Commands

```bash
# Build the library
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# Install to Maven Local (for consumption by other projects)
./gradlew publishToMavenLocal
```

## Using the Library

### In Other Projects

Add the dependency to your `build.gradle`:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.clean:common-lib:0.0.1-SNAPSHOT'
}
```

## Development

### IDE Setup

This project is configured for:
- **VS Code**: Java extension with JDK 21 configured in `.vscode/settings.json`
- **IntelliJ IDEA**: Import as Gradle project
- **Eclipse**: Import as existing Gradle project

### Code Conventions

- Use Lombok annotations to reduce boilerplate
- Follow Java naming conventions
- Write unit tests for all public APIs

## Testing

Run tests with:
```bash
./gradlew test
```

Test output includes:
- Passed tests
- Skipped tests
- Failed tests with full exception traces

## Version

Current version: **0.0.1-SNAPSHOT**

## License

[Your License Here]

## Contributing

[Your contribution guidelines here]
