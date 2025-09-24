@echo off
echo Starting GitHub Contribution Graph Generator with ML Features...
echo.

REM Compile the ML classes first
echo Compiling ML classes...
javac -d target/classes src\main\java\com\funnygithub\ml\*.java

if %errorlevel% neq 0 (
    echo ML compilation failed!
    pause
    exit /b 1
)

REM Compile the main app
echo Compiling main application...
javac -cp target/classes -d target/classes src\main\java\com\funnygithub\SimpleGitHubApp.java

if %errorlevel% neq 0 (
    echo Main app compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Check if training data exists, if not generate it
if not exist "training_data.txt" (
    echo Generating ML training data...
    java -cp target/classes com.funnygithub.ml.TrainingDataGenerator
    echo.
)

REM Run the application
echo Starting GitHub Contribution Graph Generator with ML...
echo Features available:
echo - Manual pattern design with intensity levels (1-6)
echo - ML Generate: AI-powered pattern creation
echo - Pattern Optimization: Fine-tune with sliders
echo - Real Git commit generation
echo.
java -cp target/classes com.funnygithub.SimpleGitHubApp

pause
