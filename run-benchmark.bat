@echo off
setlocal
echo ========================================================
echo   FastLayout JMH Performance Benchmark (Zero-GC / SoA)
echo ========================================================

cd /d "%~dp0"

echo [1/2] Compiling benchmark test classes...
call mvn test-compile -q
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

echo [2/2] Running JMH Benchmark Suite (In-Process)...
call mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass=fastlayout.benchmark.FastLayoutBenchmark

echo.
echo Benchmark completed successfully.
pause
