# AGENTS.md

## Project Description

This is a Minecraft Mod NeoForge 26.2 project. The project goal is to port Noita's game mechanics to Minecraft.

## Skills & MCPs
Prioritize using the neoforge-docs MCP to learn how to use the NeoForge API.
When it cannot meet the requirements, use the external-lib-sources Skill to obtain source code for more detailed information.
**Do not extract external library source code into project files.** Use the external-lib-sources Skill to read source code directly from jars without extracting them.

## Code Design Principles

All code written in this project must adhere to the following SOLID principles:

- **Single Responsibility Principle (SRP)**: Each class should have only one reason to change. A class should focus on a single responsibility.
- **Open/Closed Principle (OCP)**: Software entities should be open for extension but closed for modification. Prefer extending behavior through interfaces, inheritance, or composition rather than modifying existing code.
- **Liskov Substitution Principle (LSP)**: Subtypes must be substitutable for their base types without altering the correctness of the program. Derived classes must not violate the contracts of their base classes.
- **Interface Segregation Principle (ISP)**: Clients should not be forced to depend on interfaces they do not use. Prefer small, focused interfaces over large, monolithic ones.
- **Dependency Inversion Principle (DIP)**: Depend on abstractions, not on concretions. High-level modules should not depend on low-level modules; both should depend on abstractions.

## Important Restrictions

**Do NOT run the Minecraft server or client.** These are long-running interactive processes that cannot be stopped programmatically — only the user can manually stop them. Do not execute any Gradle tasks such as `runClient`, `runServer`, or similar that launch the game.
