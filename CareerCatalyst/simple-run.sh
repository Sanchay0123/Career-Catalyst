#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Career Planner Application Simple Runner ===${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven to build the application.${NC}"
    exit 1
fi

# Clean and build the project
echo -e "${YELLOW}Building Career Planner Application...${NC}"
mvn clean package

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Build successful!${NC}"
    
    # Run the application
    echo -e "${YELLOW}Starting Career Planner Application...${NC}"
    
    # Run with simple classpath approach - no modules
    java -cp target/classes:target/lib/* \
         -Dhttp.server.port=5000 \
         com.careerplanner.CareerPlannerApp
else
    echo -e "${RED}Build failed. Please check the error messages above.${NC}"
    exit 1
fi