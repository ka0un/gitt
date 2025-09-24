@echo off
echo Starting Simple GitHub Contribution Graph Generator...
echo.

REM Compile the simple app
echo Compiling...
javac -d . src\main\java\com\funnygithub\SimpleGitHubApp.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Run the simple app
echo Starting Simple GitHub App...
java -cp . com.funnygithub.SimpleGitHubApp

pause
