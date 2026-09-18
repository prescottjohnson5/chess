# ♕ Multiplayer Chess Engine

A full-stack multiplayer chess platform built in Java, featuring a command-line client, a networked server, and real-time gameplay over WebSocket. Built for BYU's CS 240.

## Overview

This project implements a complete chess game from the ground up — move validation, check/checkmate detection, and game state — connected through a client/server architecture that supports multiple concurrent users and games over a network.

## Architecture

- **Client** — command-line interface for playing chess over the network
- **Server** — handles network requests, user authentication, and game management
- **Shared** — core chess logic (rules, board state) used by both client and server

Communication happens over HTTP (for user/game management) and WebSocket (for real-time game moves), with persistent storage for users and game state.

## Tech Stack

Java · Maven · WebSocket · HTTP

## Running the Project

Build with Maven, then run the client and server separately:

| Command | Description |
|---|---|
| `mvn compile` | Builds the code |
| `mvn package` | Runs tests and builds an executable jar |
| `mvn package -DskipTests` | Builds the jar without running tests |
| `mvn test` | Runs all tests |
| `mvn -pl client exec:java` | Builds and runs the client |
| `mvn -pl server exec:java` | Builds and runs the server |

Or run directly from the built jar:
```
java -jar client/target/client-jar-with-dependencies.jar
```
