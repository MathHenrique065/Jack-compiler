@echo off
setlocal

echo [1/2] Creating target/classes directory...
if not exist "target\classes" mkdir "target\classes"

echo [2/2] Compiling Java source files with javac...
javac -encoding UTF-8 -d target\classes src\main\java\nand2tetris\lexer\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  Build successful! Classes compiled to target/classes
    echo ============================================
) else (
    echo.
    echo [ERROR] Compilation failed. Please check that JDK 17+ is installed and on your PATH.
)

endlocal
