@echo off
setlocal

echo =======================================================
echo   Enviando projeto para o GitHub:
echo   https://github.com/MathHenrique065/compiladores-jackcompiler
echo =======================================================
echo.

cd /d "%~dp0"

echo [1/5] Verificando se o Git esta inicializado...
if not exist ".git" (
    git init
    echo Git inicializado com sucesso.
)

echo.
echo [2/5] Adicionando arquivos...
git add .

echo.
echo [3/5] Criando commit...
git commit -m "feat: implementacao completa do analisador lexico Jack (Nand2Tetris)"

echo.
echo [4/5] Configurando branch principal (main)...
git branch -M main

echo.
echo [5/5] Configurando repositorio remoto e fazendo push...
git remote remove origin 2>nul
git remote add origin https://github.com/MathHenrique065/compiladores-jackcompiler.git

echo.
echo Executando 'git push -u origin main'...
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =======================================================
    echo   [SUCESSO] Projeto enviado para o GitHub com sucesso!
    echo =======================================================
) else (
    echo.
    echo =======================================================
    echo   [AVISO] Se o push falhou, certifique-se de que:
    echo   1. O repositorio remoto existe no GitHub.
    echo   2. Voce tem permissao de escrita.
    echo   3. Se o repositorio remoto ja tem README ou commits,
    echo      rode: git pull origin main --rebase
    echo      e depois: git push origin main
    echo =======================================================
)

pause
endlocal
