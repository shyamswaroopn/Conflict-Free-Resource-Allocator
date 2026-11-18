
@echo off
echo ==================================================================
echo  Compiling Conflict-Free Resource Allocation System
echo ==================================================================

if not exist out mkdir out

if not exist lib (
    echo ERROR: lib folder not found!
    pause
    exit /b 1
)

dir /s /B src\*.java > sources.txt

javac -cp "lib\*" -d out -encoding UTF-8 @sources.txt

if errorlevel 1 (
    echo.
    echo COMPILATION FAILED! Check the errors above.
    del sources.txt
    pause
    exit /b 1
)

del sources.txt

echo.
echo Compilation successful!

echo.
echo Running Graph Coloring Test Suite...
java -cp "out;lib\*" com.allocator.test.GraphColoringTestSuite

echo.
echo Launching Java Swing Application...
java -cp "out;lib\*" com.allocator.Main

pause
