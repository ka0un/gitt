@echo off
echo ========================================
echo  ML Training for GitHub Contribution Patterns
echo ========================================
echo.

echo Compiling ML classes...
javac -d target/classes src/main/java/com/funnygithub/ml/*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: ML compilation failed!
    echo Please check your Java installation and file paths.
    pause
    exit /b 1
)

echo Compiling main application...
javac -cp target/classes -d target/classes src/main/java/com/funnygithub/SimpleGitHubApp.java

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Main app compilation failed!
    pause
    exit /b 1
)

echo.
echo Running ML training data generation...
echo This will create training datasets for the ML model...
echo.

java -cp target/classes com.funnygithub.ml.TrainingDataGenerator

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Training data generation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  TRAINING COMPLETE!
echo ========================================
echo.
echo Generated files:
echo - training_data.txt (Text-to-pattern mappings)
echo - character_patterns.txt (Character intensity distributions)
echo.
echo ML Features now available:
echo - ML Generate: AI-powered pattern creation
echo - Pattern Optimization: Fine-tune with sliders
echo - Intelligent intensity distribution
echo - Realistic activity simulation
echo.
echo You can now run simple-github.bat to use ML features!
echo.
pause
