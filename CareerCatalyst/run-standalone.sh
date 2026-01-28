#!/bin/bash

# Compile the main application
mvn clean package

# Compile the standalone class
javac -cp target/classes MainApp.java

# Run the standalone class with all dependencies in classpath
java -cp .:target/classes:target/lib/* MainApp