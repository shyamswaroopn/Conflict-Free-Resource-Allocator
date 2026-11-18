#!/usr/bin/env bash
set -e

echo "=================================================================="
echo " Compiling Conflict-Free Resource Allocation System (Java Swing)"
echo "=================================================================="

mkdir -p out

# Find all .java files and compile using standard javac
find src -name "*.java" > sources.txt
javac -d out -encoding UTF-8 @sources.txt
rm sources.txt

echo "Compilation successful! Compiled classes placed in ./out"
echo ""
echo "Running Automated Graph Coloring Test Suite..."
java -cp out com.allocator.test.GraphColoringTestSuite

echo ""
echo "To launch the Java Swing Desktop Application with MySQL JDBC:"
echo "  java -cp \"out:lib/*\" com.allocator.Main"
