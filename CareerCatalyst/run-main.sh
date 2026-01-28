#!/bin/bash

# Compile the project and package dependencies
mvn clean package

# Run the main class with all dependencies
java -jar target/CareerPlanner-1.0-SNAPSHOT.jar