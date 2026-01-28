#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Career Planner Application Runner ===${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven to build the application.${NC}"
    exit 1
fi

# Set up directories if they don't exist
mkdir -p target
mkdir -p src/main/resources/data
mkdir -p src/main/resources/images

# Clean and build the project
echo -e "${YELLOW}Building Career Planner Application...${NC}"
mvn clean package

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Build successful!${NC}"
    
    # Run the application
    echo -e "${YELLOW}Starting Career Planner Application...${NC}"
    
    # Set up JavaFX module path using Maven repository
    JAVAFX_PATH="/home/runner/.m2/repository/org/openjfx"
    JAVAFX_VERSION="17.0.2"
    
    # Run with JavaFX modules in headless mode with Java Web Server
    java -Djava.awt.headless=true \
         -Djavafx.platform=monocle \
         -Dmonocle.platform=Headless \
         -Dprism.order=sw \
         -Dhttp.server.port=5000 \
         --module-path target/lib \
         --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.graphics,javafx.media \
         -cp target/CareerPlanner-1.0-SNAPSHOT.jar:target/lib/* \
         com.careerplanner.CareerPlannerApp
else
    echo -e "${RED}Build failed. Please check the error messages above.${NC}"
    exit 1
fi