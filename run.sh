#!/bin/bash

# Define classpath
JARS=".:sqlite-jdbc-3.43.2.0.jar:slf4j-api-2.0.9.jar:slf4j-simple-2.0.9.jar:json-20240303.jar"

echo "🛠️ Compiling Java files..."
javac -cp "$JARS" ChatApp.java DBManager.java ai.java Login.java

if [ $? -eq 0 ]; then
  echo "🚀 Launching ChatApp GUI..."
  java -cp "$JARS" ChatApp
else
  echo "❌ Compilation failed."
fi
