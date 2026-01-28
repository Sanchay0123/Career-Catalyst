#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Career Planner Development Runner ===${NC}"

# Clean previous build files
echo -e "${YELLOW}Cleaning previous build files...${NC}"
rm -rf bin
mkdir -p bin

# Compile Java files
echo -e "${YELLOW}Compiling Java files...${NC}"
javac -d bin --module-path /nix/store/2vwkssqpzykk37r996cafq7x63imf4sp-openjdk-21+35/lib/ --add-modules javafx.controls,javafx.fxml,javafx.web -cp lib/*:bin src/main/java/com/careerplanner/CareerPlannerApp.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Compilation successful!${NC}"
    
    # Run the application
    echo -e "${YELLOW}Starting Career Planner Application...${NC}"
    java --module-path /nix/store/2vwkssqpzykk37r996cafq7x63imf4sp-openjdk-21+35/lib/ --add-modules javafx.controls,javafx.fxml,javafx.web -cp bin:src/main/resources:lib/* com.careerplanner.CareerPlannerApp
else
    echo -e "${RED}Compilation failed. Please check the error messages above.${NC}"
    exit 1
fi