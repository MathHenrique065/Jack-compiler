@echo off
setlocal

if not exist "target\classes\nand2tetris\lexer\LexerValidator.class" (
    echo [INFO] Compiled classes not found. Building project first...
    call "%~dp0build.bat"
)

java -Dfile.encoding=UTF-8 -cp "target\classes" nand2tetris.lexer.LexerValidator %*

endlocal
