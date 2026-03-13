#!/bin/bash
# ========================================
# FurnitureVision - Compile & Run Script
# ========================================
# This script compiles all Java source files and
# runs the application with the SQLite JDBC driver.
#
# Usage:  ./run.sh
# ========================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/out"
LIB_DIR="$PROJECT_DIR/lib"
CLASSPATH="$OUT_DIR:$LIB_DIR/*"

echo "╔══════════════════════════════════════════════╗"
echo "║    FurnitureVision - Build & Run             ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# Step 1: Clean output directory
echo "[1/3] Cleaning output directory..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Step 2: Compile all Java files
echo "[2/3] Compiling Java source files..."
find "$SRC_DIR" -name "*.java" > /tmp/fv_sources.txt

javac -d "$OUT_DIR" -cp "$CLASSPATH" @/tmp/fv_sources.txt
if [ $? -ne 0 ]; then
    echo ""
    echo "✗ Compilation FAILED. See errors above."
    exit 1
fi
echo "  ✓ Compilation successful."

# Step 3: Run the application
echo "[3/3] Launching FurnitureVision..."
echo ""
java -cp "$CLASSPATH" com.furniturevision.Main
