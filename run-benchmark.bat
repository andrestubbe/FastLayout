@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===========================================
echo FastLayout OpenJDK JMH Benchmark Suite
echo ===========================================
echo.

echo [+] Compiling FastLayout with JMH Annotations...
call mvn -q test-compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Benchmark compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [+] Executing JMH Benchmarks...
echo.
call mvn test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="fastlayout.benchmark.FastLayoutBenchmark"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Benchmark execution failed.
    pause
    exit /b %ERRORLEVEL%
)

pause
