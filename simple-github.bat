@echo off
echo Starting GitHub Contribution Graph Artist with ML Features...
echo.

REM Create target directory if it doesn't exist
if not exist "target\classes" mkdir target\classes

REM Compile the ML classes first (they don't depend on anything)
echo Compiling ML classes...
javac -d target/classes src\main\java\com\GitTimeTraveler\ml\*.java

if %errorlevel% neq 0 (
    echo ML compilation failed!
    pause
    exit /b 1
)

REM Compile the service classes (they depend on ML classes)
echo Compiling service classes...
javac -cp target/classes -d target/classes src\main\java\com\GitTimeTraveler\service\*.java src\main\java\com\GitTimeTraveler\service\impl\*.java

if %errorlevel% neq 0 (
    echo Service compilation failed!
    pause
    exit /b 1
)

REM Compile the main app
echo Compiling main application...
javac -cp target/classes -d target/classes src\main\java\com\GitTimeTraveler\SimpleGitHubApp.java

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
    java -cp target/classes com.GitTimeTraveler.ml.TrainingDataGenerator
    echo.
)

REM Run the application
echo Starting GitHub Contribution Graph Artist...
echo Features available:
echo - Manual pattern design with intensity levels (0-6)
echo - ML Generate: AI-powered pattern creation
echo - Pattern Optimization: Fine-tune with sliders
echo - Real Git commit generation
echo - Pattern saving and loading
echo - Year-based commit management
echo.
java -cp target/classes com.GitTimeTraveler.SimpleGitHubApp

pause
