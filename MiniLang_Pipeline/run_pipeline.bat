@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
title MiniLang Pipeline - Test Suite and Demo
cls
echo ============================================================
echo          MiniLang Pipeline - Test Suite (6 cases)
echo ============================================================
echo.

REM --- Resolve a JDK 21 runtime (PATH java may be 1.8)
set "JEXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JEXE=%JAVA_HOME%\bin\java.exe"
if exist "C:\Program Files\Java\jdk-21\bin\java.exe" set "JEXE=C:\Program Files\Java\jdk-21\bin\java.exe"

REM --- Resolve JDK 21 compiler (PATH javac may differ)
set "JAVAC=javac"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"
if exist "C:\Program Files\Java\jdk-21\bin\javac.exe" set "JAVAC=C:\Program Files\Java\jdk-21\bin\javac.exe"

REM --- Auto-compile the Java stage from source
echo Compiling Java stage...
if exist "build\classes" rmdir /s /q "build\classes"
if not exist "build" mkdir "build"
mkdir "build\classes"
dir /s /b "src\*.java" > "build\sources.txt"
"%JAVAC%" -encoding UTF-8 -d "build\classes" "@build\sources.txt"
if errorlevel 1 (
    echo.
    echo [ERROR] Java compilation failed. Fix errors above.
    pause
    exit /b 1
)
echo [OK] Java stage compiled.

REM --- Ensure test cases exist
if not exist "test_cases\test1_valid_full.mini" (
    echo [ERROR] Test cases missing in test_cases\
    pause
    exit /b 1
)

set PASS=0
set FAIL=0
call :run_case 1 OK
call :run_case 2 ERR1
call :run_case 3 ERR1
call :run_case 4 OK
call :run_case 5 OK
call :run_case 6 OK

echo.
echo ============================================================
echo   RESULTS: PASS=!PASS!  FAIL=!FAIL!
echo ============================================================
echo.
echo Restoring canonical demo artifacts in output\ ...
copy /Y "test_cases\test1_valid_full.mini" "programa.mini" >nul
del /Q "output\programa.ir" "output\resultado.txt" "output\firma.txt" 2>nul
"%JEXE%" -cp build\classes minilang.MiniLang_Pipeline >nul 2>&1
python python\etapa2_functional.py >nul 2>&1
java -jar Mars.jar mips\etapa3_checksum.asm sm pa >nul 2>&1
if exist output\firma.txt (
    echo [OK] Demo artifacts: programa.ir, resultado.txt, firma.txt
) else (
    echo [WARN] Demo artifacts incomplete, review output\
)
echo.
pause
exit /b 0

:run_case
set CASE=%1
set EXPECT=%2
echo.
echo ---------- Case %CASE% (expected: %EXPECT%) ----------
copy /Y "test_cases\test%CASE%_*.mini" "programa.mini" >nul
del /Q "output\programa.ir" "output\resultado.txt" "output\firma.txt" 2>nul
if "%EXPECT%"=="ERR1" (
    "%JEXE%" -cp build\classes minilang.MiniLang_Pipeline
    set RC1=!errorlevel!
    if !RC1! NEQ 0 (
        if not exist output\programa.ir (
            echo [PASS] Case %CASE%: stopped correctly, no IR
            set /a PASS+=1
        ) else (
            echo [FAIL] Case %CASE%: IR generated despite error
            set /a FAIL+=1
        )
    ) else (
        echo [FAIL] Case %CASE%: expected error but succeeded
        set /a FAIL+=1
    )
    goto :eof
)
"%JEXE%" -cp build\classes minilang.MiniLang_Pipeline >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Case %CASE%: stage 1 failed unexpectedly
    set /a FAIL+=1
    goto :eof
)
python python\etapa2_functional.py >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Case %CASE%: stage 2 failed
    set /a FAIL+=1
    goto :eof
)
java -jar Mars.jar mips\etapa3_checksum.asm sm pa >nul 2>&1
if exist output\firma.txt (
    echo [PASS] Case %CASE%: full pipeline OK
    set /a PASS+=1
) else (
    echo [FAIL] Case %CASE%: firma.txt missing
    set /a FAIL+=1
)
goto :eof