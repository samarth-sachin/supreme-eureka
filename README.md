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
<img width="1911" height="862" alt="image" src="https://github.com/user-attachments/assets/9ea0fa99-83ab-4dbd-ad60-6c5edb1c561b" />


```plaintext
deploy sat1;
move sat1 to (150, 250);
print "Mission initiated";
