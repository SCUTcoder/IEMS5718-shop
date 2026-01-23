#!/bin/bash

# IEMS5718 Shop Backend Startup Script

echo "======================================"
echo "  IEMS5718 Shop Backend Startup"
echo "======================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed!"
    echo "Please install Java 17 or higher."
    exit 1
fi

# Check Java version
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$java_version" -lt 17 ]; then
    echo "❌ Error: Java version must be 17 or higher!"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed!"
    echo "Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo "✅ Maven version: $(mvn -version | head -n 1)"
echo ""

# Build the project
echo "📦 Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""

# Run the application
echo "🚀 Starting IEMS5718 Shop Backend..."
echo "   API will be available at: http://localhost:8080/api"
echo "   Press Ctrl+C to stop the server"
echo ""

mvn spring-boot:run
