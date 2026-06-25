<div align="center">
<img alt="logo" src="https://github.com/user-attachments/assets/fe3130d3-a488-4abb-be2d-22484c57975b" />

# IABot - AI Chatbot
</div>

IABot is a desktop AI chatbot application built with Java Swing. It provides a user-friendly interface for chatting with an AI, along with features for managing conversations and user accounts.

## Features

*   **User Authentication**: Secure login and registration system for users.
*   **Conversation Management**: Users can create, rename, and delete their conversations.
*   **Chat History**: All conversations are saved and can be revisited later.
*   **AI Integration**: The chatbot uses an AI to generate responses to user prompts.
*   **Intuitive UI**: A clean and modern user interface built with Java Swing.

## Technologies Used

*   **Java**: The core programming language for the application.
*   **Java Swing**: Used for creating the graphical user interface.
*   **SQLite**: The database used for storing user data and chat history.
*   **JDBC**: For connecting to the SQLite database.

## Dependencies

The project relies on the following external libraries:

*   `sqlite-jdbc-3.43.2.0.jar`: The JDBC driver for SQLite.
*   `slf4j-api-2.0.9.jar`: A logging facade.
*   `slf4j-simple-2.0.9.jar`: A simple implementation of the SLF4J API.
*   `json-20240303.jar`: A library for working with JSON data.

## How to Run

To run the application, you can use the provided shell script:

```bash
./run.sh
```

This script will first compile the Java source files and then launch the application.

Make sure the script has execute permissions:
```bash
chmod +x run.sh
```
