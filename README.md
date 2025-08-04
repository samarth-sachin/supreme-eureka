# SatOpsDSL - Satellite Operations Domain-Specific Language Simulator

SatOpsDSL is a backend system that interprets and executes satellite operation commands written in a simple domain-specific language (DSL). It allows users to simulate satellite control logic by sending plain text scripts to a Spring Boot-powered API, which parses the instructions using ANTLR and provides execution feedback.

## Features

- Custom DSL to control basic satellite actions
- Commands include `deploy`, `move`, and `print`
- Built with ANTLR4 for grammar parsing and Java 21
- Exposes a REST endpoint (`/dsl/run`) to execute DSL scripts
- Easily testable using tools like Postman or curl
- Designed for future extension to support complex space operations

## Example Input

The following script demonstrates a sample mission sequence:

```plaintext
deploy sat1;
move sat1 to (150, 250);
print "Mission initiated";
